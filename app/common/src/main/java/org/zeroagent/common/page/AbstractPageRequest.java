package org.zeroagent.common.page;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.apache.commons.lang3.ArrayUtils;
import org.zeroagent.common.sort.Order;
import org.zeroagent.common.sort.Sort;

/**
 * 分页条件请求基类
 *
 * @author joton
 * @version AbstractPageRequest.java, v 0.1 2023-01-07 16:33 joton
 */
@ToString
@EqualsAndHashCode
abstract class AbstractPageRequest implements Pageable {

    /**
     * 页码
     */
    private final int page;
    /**
     * 每页数量
     */
    private final int size;
    /**
     * 默认每页数量
     */
    @Getter
    private final int defaultPageSize;
    /**
     * 最大每页数量
     */
    @Getter
    private final int maxPageSize;
    /**
     * 是否需要查询总记录数
     */
    private final boolean needTotal;
    /**
     * 是否需要查询记录列表
     */
    private final boolean needContent;
    /**
     * 是否纠正分页边界错误
     */
    @Getter
    private final boolean fixEdge;
    /**
     * 页码是否从1开始
     */
    @Getter
    private final boolean pageNumberOneIndexed;
    /**
     * 排序参数
     */
    @Getter
    private Sort sort;

    /**
     * Creates a new {@link AbstractPageRequest}.
     *
     * @param page 页码
     * @param size 每页数量
     */
    public AbstractPageRequest(final int page, final int size) {
        this(page, size,
                PageDefaults.PAGE_SIZE,
                PageDefaults.MAX_PAGE_SIZE,
                PageDefaults.NEED_TOTAL,
                PageDefaults.NEED_CONTENT,
                PageDefaults.IS_FIX_EDGE,
                PageDefaults.PAGE_NUMBER_ONE_INDEXED
        );
    }

    /**
     * Creates a new {@link AbstractPageRequest}.
     *
     * @param page                 页码
     * @param size                 每页数量
     * @param defaultPageSize      默认每页数量
     * @param maxPageSize          最大每页数量
     * @param needTotal            是否需要查询总记录数
     * @param needContent          是否需要查询记录列表
     * @param fixEdge              是否纠正分页边界错误，比如当page<起始页时，自动设置page=起始页
     * @param pageNumberOneIndexed 页码是否从1开始
     * @param orders               排序规则
     */
    public AbstractPageRequest(final int page,
                               final int size,
                               final int defaultPageSize,
                               final int maxPageSize,
                               final boolean needTotal,
                               final boolean needContent,
                               final boolean fixEdge,
                               final boolean pageNumberOneIndexed,
                               final Order... orders) {
        this.defaultPageSize = defaultPageSize;
        this.maxPageSize = maxPageSize;
        this.needTotal = needTotal;
        this.needContent = needContent;
        this.fixEdge = fixEdge;
        this.pageNumberOneIndexed = pageNumberOneIndexed;
        this.size = size < 1 ? defaultPageSize : Math.min(size, maxPageSize);
        if (fixEdge) {
            this.page = Math.max(page, getFirstPageNumber());
        } else {
            this.page = page;
        }
        if (this.page < getFirstPageNumber()) {
            throw new PageRequestException("page number must not be less than " + getFirstPageNumber());
        }
        if (ArrayUtils.isNotEmpty(orders)) {
            this.sort = new Sort(orders);
        }
    }

    /**
     * @see Pageable#getPageSize()
     */
    @Override
    public int getPageSize() {
        return size;
    }

    /**
     * @see Pageable#getPageNumber()
     */
    @Override
    public int getPageNumber() {
        return page;
    }

    /**
     * @see Pageable#needTotal()
     */
    @Override
    public boolean needTotal() {
        return needTotal;
    }

    /**
     * @see Pageable#needContent()
     */
    @Override
    public boolean needContent() {
        return needContent;
    }
}

