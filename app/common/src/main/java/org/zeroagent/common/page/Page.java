package org.zeroagent.common.page;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

public interface Page<T> extends Iterable<T> {
    /**
     *  结果集
     * @return 结果集
     */
    @NotNull
    List<T> getContent();

    /**
     *  总数 （若不做count查询，返回null）
     * @return 总数
     */
    Long getTotal();

    /**
     * 总页数
     * @return 总页数
     */
    @Nullable
    default Integer getLastPageNumber() {
        if (getTotal() == null) {
            return getContent().size() != current().getPageSize() ? current().getPageNumber() : null;
        }
        return PageUtil.calcLastPageNumber(getTotal(), current().getPageSize());
    }
    /**
     * 当前页是否为第一页
     *
     * @return 是否为第一页
     */
    default boolean isFirst() {
        return current().getPageNumber() == current().getFirstPageNumber();
    }

    /**
     * 当前页是否为最后一页（如果不做count查询，则只要结果集大小不等于每页数量，就返回false）
     *
     * @return 是否为最后一页
     */
    default boolean isLast() {
        if (getLastPageNumber() == null) {
            return false;
        }
        return current().getPageNumber() == getLastPageNumber();
    }

    /**
     * 当前页是否有上一页
     *
     * @return 是否有上一页
     */
    default boolean hasPrevious() {
        return current().getPageNumber() > current().getFirstPageNumber();
    }

    /**
     * 当前页是否有下一页（如果不做count查询，则只要结果集大小等于每页数量，就返回true）
     *
     * @return 是否有下一页
     */
    default boolean hasNext() {
        if (getLastPageNumber() == null) {
            return true;
        }
        return current().getPageNumber() < getLastPageNumber();
    }

    /**
     * 第一页
     *
     * @return 第一页的分页参数
     */
    @NotNull
    default Pageable first() {
        return current().jumpTo(current().getFirstPageNumber());
    }

    /**
     * 上一页
     *
     * @return 上一页的分页参数
     */
    @NotNull
    default Pageable previous() {
        return hasPrevious() ? current().jumpTo(current().getPageNumber() - 1) : first();
    }

    /**
     * 当前页
     *
     * @return 当前页的分页参数
     */
    @NotNull
    Pageable current();

    /**
     * 下一页 1.如果有下一页，则返回下一页 2.如果没有最后一页，则始终跳至下一页
     * <p>
     * 注：若无count查询，则等到结果集数量小于每页数量，一定会产生最后一页
     *
     * @return 下一页的分页参数
     */
    @NotNull
    default Pageable next() {
        if (hasNext()) {
            return current().jumpTo(current().getPageNumber() + 1);
        }
        return current().copy();
    }

    /**
     * 最后一页
     * <p>
     * 1.如果没有下一页，则当前页就是最后一页 2.如果不做count查询，则为null
     * <p>
     * 注：若无count查询，则等到结果集数量小于每页数量，一定会产生最后一页
     *
     * @return 最后一页的分页参数
     */
    @Nullable
    default Pageable last() {
        if (!hasNext()) {
            return current().copy();
        }
        return getLastPageNumber() == null ? null : current().jumpTo(getLastPageNumber());
    }

    /**
     * Returns a new {@link Page} with the content of the current one mapped by the given {@link Function}.
     *
     * @param mapper must not be {@literal null}.
     */
    @NotNull <S> Page<S> map(Function<? super T, ? extends S> mapper);

    /**
     * Returns a new {@link Page} with the content mapped by the given {@link Function}.
     *
     * @param mapper must not be {@literal null}.
     * @param <S>    the content of the current one type
     * @return Page
     */
    @NotNull <S> Page<S> mapAll(Function<List<T>, List<S>> mapper);

    /**
     * 获取结果集迭代器
     *
     * @return 结果集迭代器
     */
    @NotNull
    @Override
    default Iterator<T> iterator() {
        return getContent().iterator();
    }

    /**
     * 转换为分页结果对象（支持JSON序列化，可用于缓存）
     *
     * @return 分页结果对象
     */
    default PageResult<T> toPageResult() {
        return new PageResult<T>().setContent(this.getContent()).setTotal(this.getTotal());
    }

    /**
     * 构造空分页结果
     *
     * @return 分页结果
     */
    @NotNull
    static <R> Page<R> empty(int pageNum, int pageSize) {
        Pageable pageable = PageBuilder.page(pageNum).size(pageSize).build();
        return Page.empty(pageable);
    }

    /**
     * 构造空分页结果
     *
     * @param pageable 分页条件
     * @param <R>      结果集元素类型
     * @return 空分页结果
     */
    static <R> Page<R> empty(Pageable pageable) {
        return new PageImpl<>(new ArrayList<>(), pageable, 0L);
    }

    /**
     * 分页转换
     *
     * @return 分页结果
     */
    @NotNull
    static <R> Page<R> of(final List<R> content, final Pageable pageable, final Long total) {
        return new PageImpl<>(content, pageable, total);
    }

    /**
     * 分页转换
     *
     * @return 分页结果
     */
    @NotNull
    static <R> Page<R> of(final List<R> content, final Pageable pageable, final Integer total) {
        return new PageImpl<>(content, pageable, Long.valueOf(total));
    }

    /**
     * 分页转换
     *
     * @return 分页结果
     */
    @NotNull
    static <R> Page<R> of(final List<R> content, final Pageable pageable) {
        return new PageImpl<>(content, pageable);
    }
}
