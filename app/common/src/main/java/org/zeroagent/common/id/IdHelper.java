package org.zeroagent.common.id;

import lombok.experimental.UtilityClass;
import org.zeroagent.common.utils.Asserts;

import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 *  ID生成工具
 */
@UtilityClass
public class IdHelper {
    private final Object lock = new Object();
    private static volatile IdGenerator idGenerator ;
    /**
     * 根据当前时间戳转换为base62编码
     * <p>
     * 注意：如果在1ms内并发生成，无法保证全局唯一
     */
//    public static String getCurrentMillisToBase62() {
//        long mills = System.currentTimeMillis();
//        return Base62.encodeBase62(mills);
//    }

    /**
     * 全局分布式唯一序号（年月日开头）
     *
     * @return 全局分布式唯一序号
     */
    public static String getPrettyId() {
        init();
        return idGenerator.nextNo(ChronoUnit.DAYS);
    }

    /**
     * 全局分布式唯一ID
     *
     * @return 全局分布式唯一ID
     */
    public static long getId() {
        init();
        return idGenerator.nextId();
    }

    public static String getStrId() {
        return String.valueOf(getId());
    }

    /**
     * 获取短一点的ID（最大不会超过15位整数）
     * <p>
     * 注意：如果在1ms内并发生成，无法保证全局唯一
     *
     * @param twoDigitsPrefix 两位数前缀
     */
    public static long getShorterId(int twoDigitsPrefix) {
        Asserts.isTrue(twoDigitsPrefix >= 0 && twoDigitsPrefix <= 99, "前缀必须是两位数");
        // 毫秒时间戳目前为13位，从1970年开始算起，在317年内都不会超过13位
        String mills = String.valueOf(System.currentTimeMillis());
        Asserts.isTrue(mills.length() <= 13, "时间戳长度不能超过13位");
        String id = twoDigitsPrefix + mills;
        return Long.parseLong(id);
    }

    /**
     * 是否是短ID
     *
     * @param id ID
     * @return 是否是短ID
     */
    public static boolean isShorterId(long id) {
        return id <= 999999999999999L;
    }

    /**
     * 32位UUID
     *
     * @return 32位UUID
     */
    public static String get32UUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 获取ID生成器
     *
     * @return ID生成器
     */
    public static IdGenerator getGenerator() {
        init();
        return idGenerator;
    }

    /**
     * 重设参数
     *
     * @param nodeId        机器节点ID
     * @param baseTimestamp 基准时间戳
     * @param sequenceBits  自增序列长度
     */
    public static void reload(Integer nodeId, Long baseTimestamp, Integer sequenceBits) {
        if (nodeId == null && baseTimestamp == null && sequenceBits == null) {
            return;
        }
        synchronized (lock) {
            idGenerator = new IdGenerator(nodeId, baseTimestamp, sequenceBits);
        }
    }

    /**
     * 初始化
     */
    private static void init() {
        if (idGenerator != null) {
            return;
        }
        synchronized (lock) {
            if (idGenerator == null) {
                idGenerator = new IdGenerator();
            }
        }
    }
}
