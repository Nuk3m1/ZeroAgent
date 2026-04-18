package org.zeroagent.common.page;

import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;
import org.zeroagent.common.sort.Order;

/**
 * 分页请求，通常使用{@link PageBuilder}来构造实例
 *
 * @author joton
 * @version PageRequest.java, v 0.1 2023-01-07 16:36 joton
 */
@EqualsAndHashCode(callSuper = true)
class PageRequest extends AbstractPageRequest {

    /**
     * 构造方法
     */
    public PageRequest() {
        super(PageDefaults.PAGE_NUMBER, PageDefaults.PAGE_SIZE);
    }

    /**
     * 构造方法
     *
     * @param page 页码
     */
    public PageRequest(final int page) {
        super(page, PageDefaults.PAGE_SIZE);
    }

    /**
     * 构造方法
     *
     * @param page 页码
     * @param size 每页数量
     */
    public PageRequest(final int page, final int size) {
        super(page, size);
    }

    /**
     * Creates a new {@link PageRequest}.
     */
    public PageRequest(final int page,
                       final int size,
                       final int defaultPageSize,
                       final int maxPageSize,
                       final boolean needTotal,
                       final boolean needContent,
                       final boolean fixEdge,
                       final boolean pageNumberOneIndexed,
                       final Order... orders) {
        super(page, size, defaultPageSize, maxPageSize, needTotal, needContent, fixEdge, pageNumberOneIndexed, orders);
    }

    /**
     * @see Pageable#jumpTo(int)
     */
    @NotNull
    @Override
    public Pageable jumpTo(int page) {
        return PageBuilder.page(page)
                .size(getPageSize())
                .defaultPageSize(getDefaultPageSize())
                .maxPageSize(getMaxPageSize())
                .needTotal(needTotal())
                .needContent(needContent())
                .fixEdge(isFixEdge())
                .pageNumberOneIndexed(isPageNumberOneIndexed())
                .sort(getSort())
                .build();
    }

    /**
     * @see Pageable#config(PageConfigKey, boolean)
     */
    @NotNull
    @Override
    public Pageable config(PageConfigKey key, boolean enabled) {
        return PageBuilder.page(getPageNumber())
                .size(getPageSize())
                .defaultPageSize(getDefaultPageSize())
                .maxPageSize(getMaxPageSize())
                .needTotal(key == PageConfigKey.NEED_TOTAL ? enabled : needTotal())
                .needContent(key == PageConfigKey.NEED_CONTENT ? enabled : needTotal())
                .fixEdge(key == PageConfigKey.FIX_EDGE ? enabled : isFixEdge())
                .pageNumberOneIndexed(key == PageConfigKey.PAGE_NUMBER_ONE_INDEXED ? enabled : isPageNumberOneIndexed())
                .sort(getSort())
                .build();
    }
}

