package org.zeroagent.common.page;


import jodd.util.StringPool;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.zeroagent.common.sort.Sort;

/**
 * 分页条件
 *
 * @author joton
 * @version Pageable.java, v 0.1 2023-01-07 15:20 joton
 */
public interface Pageable {

    /**
     * Returns the page to be returned.
     *
     * @return the page to be returned.
     */
    int getPageNumber();

    /**
     * Returns the number of items to be returned.
     *
     * @return the number of items of that page
     */
    int getPageSize();

    /**
     * Returns the offset to be taken according to the underlying page and page size.
     *
     * @return the offset to be taken
     */
    default int getOffset() {
        return PageUtil.calcOffset(getFirstPageNumber(), getPageNumber(), getPageSize());
    }

    /**
     * Returns the sorting parameters.
     *
     * @return sorting parameters.
     */
    @Nullable
    Sort getSort();

    /**
     * 是否需要查询总记录数
     *
     * @return 是否需要查询总记录数
     */
    boolean needTotal();

    /**
     * 是否需要查询记录列表
     *
     * @return 是否需要查询记录列表
     */
    boolean needContent();

    /**
     * 是否纠正分页边界错误，比如当page<1时，自动设置page=1
     * <p>
     * 不合法的分页参数情况（如果fixEdge=false，则前3个应抛异常）： 1.page < first_page_number 2.size < 1 3.size > max_page_size 4.page >
     * last_page_number（做count查询后才能发现）
     *
     * @return 是否纠正分页边界错误
     * @see PageUtil#fixEdge(Pageable, long)
     */
    boolean isFixEdge();

    /**
     * 页码是否从1开始
     *
     * @return 页码是否从1开始
     */
    boolean isPageNumberOneIndexed();

    /**
     * 获取起始页页码
     *
     * @return 起始页页码
     */
    default int getFirstPageNumber() {
        return PageUtil.getFirstPageNumber(isPageNumberOneIndexed());
    }

    /**
     * 跳到指定页
     *
     * @param pageNumber 页码
     * @return 指定页的分页条件
     */
    @NotNull
    Pageable jumpTo(int pageNumber);

    /**
     * 克隆当前分页条件
     *
     * @return 分页条件
     */
    @NotNull
    default Pageable copy() {
        return jumpTo(getPageNumber());
    }

    /**
     * 启用分页条件配置项
     *
     * @param key 分页条件配置项
     * @return 修改后的分页条件
     */
    @NotNull
    default Pageable enable(PageConfigKey key) {
        return config(key, true);
    }

    /**
     * 禁用分页条件配置项
     *
     * @param key 分页条件配置项
     * @return 修改后的分页条件
     */
    @NotNull
    default Pageable disable(PageConfigKey key) {
        return config(key, false);
    }

    /**
     * 设置分页条件配置项
     *
     * @param key     分页条件配置项
     * @param enabled 是否启用
     * @return 修改后的分页条件
     */
    @NotNull Pageable config(PageConfigKey key, boolean enabled);

    /**
     * Returns a string identity of the object.
     *
     * @return a string identity of the object.
     */
    default String toIdentity() {
        return (getSort() == null ? StringUtils.EMPTY : getSort().toIdentity() + StringPool.COLON)
                + getPageNumber() + StringPool.UNDERSCORE
                + getPageSize() + StringPool.UNDERSCORE
                + BooleanUtils.toInteger(needTotal())
                + BooleanUtils.toInteger(needContent())
                + BooleanUtils.toInteger(isFixEdge())
                + BooleanUtils.toInteger(isPageNumberOneIndexed());
    }
}

