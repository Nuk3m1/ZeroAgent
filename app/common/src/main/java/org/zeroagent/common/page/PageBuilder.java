package org.zeroagent.common.page;

import org.jetbrains.annotations.Nullable;
import org.zeroagent.common.sort.Order;
import org.zeroagent.common.sort.Sort;

import java.util.ArrayList;
import java.util.List;

/**
 *
 *  分页条件构造器
 * @author Nuk3m1
 * @version 2026年03月08日  16时14分
 */
public class PageBuilder {

    private int     page;
    private int     size                 = PageDefaults.PAGE_SIZE;
    private int     defaultPageSize      = PageDefaults.PAGE_SIZE;
    private int     maxPageSize          = PageDefaults.MAX_PAGE_SIZE;
    private boolean needTotal            = PageDefaults.NEED_TOTAL;
    private boolean needContent          = PageDefaults.NEED_CONTENT;
    private boolean fixEdge              = PageDefaults.IS_FIX_EDGE;
    private boolean pageNumberOneIndexed = PageDefaults.PAGE_NUMBER_ONE_INDEXED;

    private final List<Order> orders = new ArrayList<>();

    /**
     * 构造方法
     *
     * @param page 页码
     */
    private PageBuilder(int page) {
        this.page = page;
    }

    /**
     * 第一页
     *
     * @return 构造器
     */
    public static PageBuilder firstPage() {
        return page(PageDefaults.PAGE_NUMBER);
    }

    /**
     * 设置页码
     *
     * @param page 页码
     * @return 构造器
     */
    public static PageBuilder page(int page) {
        return new PageBuilder(page);
    }

    /**
     * 构造分页条件
     *
     * @param page 页码
     * @param size 每页数量
     * @return 分页条件
     */
    public static Pageable build(int page, int size) {
        return PageBuilder.page(page).size(size).build();
    }

    /**
     * 构造分页条件
     *
     * @param pageable spring的分页对象
     * @return 分页条件
     */
    public static Pageable build(org.springframework.data.domain.Pageable pageable) {
        return PageBuilder.page(pageable.getPageNumber()).size(pageable.getPageSize()).build();
    }

    /**
     * 构造分页条件
     *
     * @param pageable spring的分页对象
     * @return 分页条件
     */
    public static Pageable buildFrom(org.springframework.data.domain.Pageable pageable) {
        return PageBuilder.page(pageable.getPageNumber()).size(pageable.getPageSize()).build();
    }

    /**
     * 设置每页数量
     *
     * @param size 每页数量
     * @return 构造器
     */
    public PageBuilder size(int size) {
        this.size = size;
        return this;
    }

    /**
     * 设置每页默认数量
     *
     * @param defaultPageSize 每页默认数量
     * @return 构造器
     */
    public PageBuilder defaultPageSize(int defaultPageSize) {
        this.defaultPageSize = defaultPageSize;
        return this;
    }

    /**
     * 设置每页最大数量
     *
     * @param maxPageSize 每页最大数量
     * @return 构造器
     */
    public PageBuilder maxPageSize(int maxPageSize) {
        this.maxPageSize = maxPageSize;
        return this;
    }

    /**
     * 设置是否需要查询总数
     *
     * @param needTotal 是否需要查询总数
     * @return 构造器
     */
    public PageBuilder needTotal(boolean needTotal) {
        this.needTotal = needTotal;
        return this;
    }

    /**
     * 设置是否需要查询结果集
     *
     * @param needContent 是否需要查询结果集
     * @return 构造器
     */
    public PageBuilder needContent(boolean needContent) {
        this.needContent = needContent;
        return this;
    }

    /**
     * 设置是否修复分页边界
     *
     * @param fixEdge 是否修复分页边界
     * @return 构造器
     */
    public PageBuilder fixEdge(boolean fixEdge) {
        this.fixEdge = fixEdge;
        return this;
    }

    /**
     * 设置起始页是否从1开始
     *
     * @param pageNumberOneIndexed 起始页是否从1开始
     * @return 构造器
     */
    public PageBuilder pageNumberOneIndexed(boolean pageNumberOneIndexed) {
        this.pageNumberOneIndexed = pageNumberOneIndexed;
        this.page = Math.max(PageUtil.getFirstPageNumber(pageNumberOneIndexed), this.page);
        return this;
    }

    /**
     * 设置排序参数
     *
     * @param sort 排序参数
     * @return 构造器
     */
    public PageBuilder sort(@Nullable Sort sort) {
        if (sort != null) {
            sort.forEach(this.orders::add);
        }
        return this;
    }

    /**
     * 构造分页条件实例
     *
     * @return 分页条件实例
     */
    public Pageable build() {
        return new PageRequest(
                page,
                size,
                defaultPageSize,
                maxPageSize,
                needTotal,
                needContent,
                fixEdge,
                pageNumberOneIndexed,
                orders.toArray(new Order[]{})
        );
    }
}

