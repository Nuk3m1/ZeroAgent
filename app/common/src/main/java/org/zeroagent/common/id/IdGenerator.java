package org.zeroagent.common.id;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.zeroagent.common.problem.error.CommonErrorCode;
import org.zeroagent.common.problem.exception.InternalException;
import org.zeroagent.common.problem.exception.SysException;
import org.zeroagent.common.utils.net.Inets;
import org.zeroagent.common.utils.time.Dates;


import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 *  ID生成器
 */
@Slf4j
public class IdGenerator {
    /**
     * 机器码占11位
     */
    private final static int NODE_ID_BITS = 11;
    /**
     * 机器号范围: 0~1023
     */
    private final static int MAX_NODE_ID = ~(-1 << NODE_ID_BITS);

    private final ReentrantLock lock = new ReentrantLock();
    /**
     * 同一秒到序列号从 0 开始
     */
    private int sequence = 0;
    private long lastTimestamp = -1L;
    /**
     * 基准时间戳
     */
    @Getter
    private final long baseTimestamp;
    /**
     * 机器号，默认取IP地址
     */
    private final long nodeId;
    /**
     * 序列号位数
     */
    private final int sequenceBits;
    /**
     * 序列号最大值
     */
    private final int sequenceMask;
    /**
     * 时间毫秒数左移位数
     */
    @Getter
    private final int timestampLeftShift;
    public IdGenerator() {
        this(null);
    }
    public IdGenerator(@Nullable Integer nodeId) {
        this(nodeId, null, null);
    }
    public IdGenerator(@Nullable Integer nodeId, @Nullable Long baseTimestamp, @Nullable Integer sequenceBits) {
        // 默认值：基准时间戳：2023-06-25T02:44:56.857398+08:00[Asia/Shanghai]
        final long defaultBaseTimestamp = 1687632296857L;
        final int defaultSequenceBits = 11;
        if (nodeId == null) {
            nodeId = getDefaultNodeId();
        }
        if (baseTimestamp == null) {
            baseTimestamp = defaultBaseTimestamp;
        }
        if (sequenceBits == null) {
            sequenceBits = defaultSequenceBits;
        }
        //校验
        if (nodeId > MAX_NODE_ID || nodeId < 0) {
            throw new IllegalArgumentException(
                    String.format("node Id can't be greater than %d or less than 0", MAX_NODE_ID));
        }
        if (sequenceBits > 11 || sequenceBits < 0) {
            throw new IllegalArgumentException(
                    String.format("sequence bits can't be greater than %d or less than 0", 11));
        }
        if (baseTimestamp > currentTimeMillis()) {
            throw new IllegalArgumentException("base timestamp can't be greater than now");
        }

        //初始化
        this.baseTimestamp = baseTimestamp;
        this.nodeId = nodeId;
        this.sequenceBits = sequenceBits;
        this.sequenceMask = ~(-1 << sequenceBits);
        this.timestampLeftShift = NODE_ID_BITS + sequenceBits;

    }
    /**
     * 取下一个ID
     *
     * @return ID
     */
    public long nextId() {
        lock.lock();
        try {
            // 更新时间戳和序列号
            updateTimestampAndSequence();

            // 时间戳增量
            final long timestampInc = lastTimestamp - baseTimestamp;

            //  000000000000000000000000000000000000000000  0000000000               000000000000
            //  timestamp(41b)                              nodeId(11b)              sequence(11b)
            return (timestampInc << timestampLeftShift) | (nodeId << sequenceBits) | sequence;
        } finally {
            lock.unlock();
        }
    }
    /**
     * 取下一个带有可读时间前缀的ID
     *
     * @param truncatedTo 精确到指定的时间单位
     * @return 序号
     */
    public String nextNo(@NotNull ChronoUnit truncatedTo) {
        lock.lock();
        try {
            // 更新时间戳和序列号
            updateTimestampAndSequence();

            // 基量时间戳
            ZonedDateTime zonedDateTime = Instant.ofEpochMilli(lastTimestamp).atZone(ZoneId.systemDefault());
            long baseTimestamp = zonedDateTime.truncatedTo(truncatedTo).toInstant().toEpochMilli();
            // 时间戳增量
            long timestampInc = lastTimestamp - baseTimestamp;
            // 时间前缀
            final String timePrefix = switch (truncatedTo) {
                case DAYS -> Dates.FORMATTER_DATE_COMPACT.format(zonedDateTime);
                case SECONDS -> Dates.FORMATTER_DATE_TIME_COMPACT.format(zonedDateTime);
                default ->
                        throw new SysException(CommonErrorCode.UNSPECIFIED, "truncatedTo[" + truncatedTo + "] not supported");
            };

            // 000000000000000000000000000000000000000000        0000000000                 000000000000
            // timestamp(41b)                                    nodeId(11b)                sequence(11b)
            long suffix = (timestampInc << timestampLeftShift) | (nodeId << sequenceBits) | sequence;

            return timePrefix + suffix;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 更新时间戳和序列号
     */
    private void updateTimestampAndSequence() {
        // 获取当前毫秒数
        long timestamp = currentTimeMillis();
        // 如果服务器时间有问题(时钟后退) 报错。
        if (timestamp < lastTimestamp) {
            String msg = String.format("Clock moved backwards.  Refusing to generate id for %d milliseconds", lastTimestamp - timestamp);
            throw new SysException(CommonErrorCode.ID_GENERATE_FAIL, msg);
        }
        // 如果上次生成时间和当前时间相同，在同一毫秒内
        if (lastTimestamp == timestamp) {
            // sequence自增，因为sequence只有11bit，所以和sequenceMask相与一下，去掉高位
            sequence = (sequence + 1) & sequenceMask;
            // 判断是否溢出,也就是每毫秒内超过2047，当为2048时，与sequenceMask相与，sequence就等于0
            if (sequence == 0) {
                // 自旋等待到下一毫秒
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            // 如果和上次生成时间不同，重置sequence，就是下一毫秒开始，sequence计数重新从0开始累加
            sequence = 0;
        }
        lastTimestamp = timestamp;
    }

    /**
     * 自旋到下一毫秒
     *
     * @param lastTimestamp 上一次生成的毫秒
     * @return 最新毫秒
     */
    private static long tilNextMillis(long lastTimestamp) {
        long timestamp = currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = currentTimeMillis();
        }
        return timestamp;
    }

    /**
     * 当前时间戳
     * @return 当前时间戳
     */
    private static long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    /**
     *  默认节点ID
     * @return 节点ID
     */
    private static int getDefaultNodeId() {
        try {
            int ipAddressAsInt = Inets.fetchLocalIpAsInt(3);
            return Math.abs(ipAddressAsInt) & MAX_NODE_ID;
        } catch (InternalException e) {
            log.error("[getDefaultNodeId][ERROR]fetch local ip error", e);
            return 0;
        }
    }
}
