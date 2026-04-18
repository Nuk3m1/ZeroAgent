package org.zeroagent.common.page;


/**
 *  分页条件配置类
 */
public enum PageConfigKey {
    /**
     *  查询总数
     */
    NEED_TOTAL,
    /**
     * 查询内容
     */
    NEED_CONTENT,
    /**
     * 边界纠正
     */
    FIX_EDGE,
    /**
     * 起始页码基于1
     */
    PAGE_NUMBER_ONE_INDEXED
}
