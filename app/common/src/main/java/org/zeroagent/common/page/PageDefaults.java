package org.zeroagent.common.page;

/**
 *
 *  分页参数默认值
 * @author Nuk3m1
 * @version 2026年03月08日  16时12分
 */
public interface PageDefaults {

    /**
     * 最大每页数量
     */
    int     MAX_PAGE_SIZE           = 2000;
    /**
     * 默认起始页码
     */
    int     PAGE_NUMBER             = 1;
    /**
     * 默认每页数量
     */
    int     PAGE_SIZE               = 20;
    /**
     * 默认要查询总记录数
     */
    boolean NEED_TOTAL              = true;
    /**
     * 默认要查询记录列表
     */
    boolean NEED_CONTENT            = true;
    /**
     * 默认自动修正分页参数
     */
    boolean IS_FIX_EDGE             = true;
    /**
     * 默认起始页码为1
     */
    boolean PAGE_NUMBER_ONE_INDEXED = true;
}

