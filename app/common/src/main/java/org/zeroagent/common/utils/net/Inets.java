package org.zeroagent.common.utils.net;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.zeroagent.common.problem.error.CommonErrorCode;
import org.zeroagent.common.problem.exception.InternalException;
import org.zeroagent.common.utils.concurrent.ThreadPools;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

@UtilityClass
public class Inets {
    public final static  String                       IP_SYSTEM_KEY      = "HOST_IP";
    private static final int                          MIN_PORT           = 0;
    private static final int                          MAX_PORT           = 65535;
    private final static String                       FALLBACK_IP        = "0.0.0.0";
    private final static Inet4Address                 FALLBACK_ADDRESS;
    private static final AtomicBoolean                INITIALIZED        = new AtomicBoolean(false);
    private static final ExecutorService              SINGLE_POOL        = ThreadPools.newSingleton("INETS_SINGLE");
    private static final ExecutorService              HOST_ADDRESS_POOL  = ThreadPools.newFixedThreadPool(16, 128, "INETS_HOST_ADDRESS");
    private static final LoadingCache<String, String> HOST_ADDRESS_CACHE = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(1, TimeUnit.HOURS)
            .build(Inets::fetchRemoteIp);

    private volatile static Inet4Address localInet4Address;
    private volatile static String       localHostAddress;
    private volatile static String       localHostName;

    static {
        Inet4Address address;
        try {
            address = (Inet4Address) InetAddress.getByName(FALLBACK_IP);
        } catch (Exception ignored) {
            try {
                address = (Inet4Address) InetAddress.getByName(FALLBACK_IP);
            } catch (Exception e1) {
                throw new IllegalArgumentException("Cannot find fallbackIp", e1);
            }
        }
        FALLBACK_ADDRESS = address;
        initialize();
    }
    /**
     * 获取本地IP地址
     *
     * @param timeoutSeconds 超时时间
     * @return 本地IP地址
     * @throws InternalException if thread exception
     */
    public static int fetchLocalIpAsInt(int timeoutSeconds) throws InternalException {
        Future<Integer> result = SINGLE_POOL.submit((Callable<Integer>) Inets::fetchLocalIpAsInt);
        try {
            return result.get(timeoutSeconds, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new InternalException(CommonErrorCode.FETCH_IP_ERROR, e);
        }
    }

    /**
     * 获取本地IP地址
     *
     * @param timeoutMills 超时时间
     * @return 本地IP地址
     * @throws InternalException if thread exception
     */
    public static String fetchLocalIp(int timeoutMills) throws InternalException {
        Future<String> result = SINGLE_POOL.submit((Callable<String>) Inets::fetchLocalIp);
        try {
            return result.get(timeoutMills, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new InternalException(CommonErrorCode.FETCH_IP_ERROR, e);
        }
    }

    /**
     * @return 将ip地址以整数的形式返回
     */
    public static int fetchLocalIpAsInt() {
        initialize();
        return ByteBuffer.wrap(localInet4Address.getAddress()).getInt();
    }

    /**
     * 获取本地IP地址
     *
     * @return 本地IP地址
     */
    public static String fetchLocalIp() {
        initialize();
        return localHostAddress;
    }
    /**
     * 获取本地主机名称
     *
     * @return 本地主机名称
     */
    public static String fetchLocalHostName() {
        initialize();
        return localHostName;
    }
    /**
     * 获取远程IP地址
     *
     * @param host 远程主机
     * @return 远程IP地址
     */
    @Nullable
    private static String fetchRemoteIp(String host) {
        try {
            InetAddress inetAddress = Inet4Address.getByName(host);
            return inetAddress.getHostAddress();
        } catch (UnknownHostException e) {
            return null;
        }
    }
    /**
     * 初始化
     */
    private static void initialize() {
        if (INITIALIZED.compareAndSet(false, true)) {
            localInet4Address = getPriorLocalInet4Address();
            localHostAddress = localInet4Address.getHostAddress();
            localHostName = localInet4Address.getHostName();
            System.setProperty(IP_SYSTEM_KEY, localHostAddress);
        }
    }
    /**
     * 获取权重高的IPV4地址
     *
     * @return IPV4地址
     */
    @NotNull
    private static Inet4Address getPriorLocalInet4Address() {
        // JVM argument: -DHOST_IP=1.2.3.4
        String ip = System.getProperty(IP_SYSTEM_KEY);
        if (ip == null) {
            // Environment variable: HOST_IP=1.2.3.4
            ip = System.getenv(IP_SYSTEM_KEY);
        }
        if (ip != null) {
            try {
                return (Inet4Address) InetAddress.getByName(ip);
            } catch (Exception e) {
                throw new RuntimeException("获取本地IP地址失败", e);
            }
        }

        List<Inet4Address> inet4Addresses = getLocalInet4AddressList();
        Inet4Address local = FALLBACK_ADDRESS;
        int maxWeight = -1;
        for (Inet4Address inet4Address : inet4Addresses) {
            int weight = 0;
            if (inet4Address.isSiteLocalAddress()) {
                weight += 8;
            }
            try {
                if (inet4Address.isReachable(500)) {
                    weight += 6;
                }
            } catch (ConnectException e) {
                // IP地址不可达
                continue;
            } catch (IOException e) {
                throw new RuntimeException("获取本地IP地址失败", e);
            }
            if (inet4Address.isLinkLocalAddress()) {
                weight -= 1;
            }
            if (inet4Address.isLoopbackAddress()) {
                weight -= 1;
            }
            // 有独立的HostName
            if (!inet4Address.getHostAddress().equals(inet4Address.getHostName())) {
                weight += 1;
            }
            if (weight > maxWeight) {
                maxWeight = weight;
                local = inet4Address;
            }
        }
        return local;
    }

    /**
     *  遍历服务器所有IPV4地址
     * @return IPV4地址
     */
    private static List<Inet4Address> getLocalInet4AddressList() {
        List<Inet4Address> ipList = new ArrayList<>();
        Enumeration<NetworkInterface> allNetInterfaces;
        try {
            allNetInterfaces = NetworkInterface.getNetworkInterfaces();
        } catch (Exception ignored) {
            return ipList;
        }
        while (allNetInterfaces.hasMoreElements()) {
            NetworkInterface netInterface = allNetInterfaces.nextElement();
            Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
            while (addresses.hasMoreElements()) {
                InetAddress ip = addresses.nextElement();
                if (ip instanceof Inet4Address) {
                    ipList.add((Inet4Address) ip);
                }
            }
        }
        return ipList;

    }
}
