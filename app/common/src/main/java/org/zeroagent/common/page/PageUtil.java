package org.zeroagent.common.page;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;
import org.zeroagent.common.utils.Asserts;

/**
 * @author Nuk3m1
 * @version 2026年03月08日  15时18分
 * @Description:
 */
@UtilityClass
public class PageUtil {
    /**
     * 获取首页页码
     *
     * @param pageNumberOneIndexed 页码是否从1开始
     * @return 首页页码
     */
    public static int getFirstPageNumber(boolean pageNumberOneIndexed) {
        return pageNumberOneIndexed ? 1 : 0;
    }

    /**
     *  计算总页数
     * @param total 总记录数
     * @param pageSize 每页数量
     * @return 总页数
     */
    public static int calcLastPageNumber(long total, int pageSize) {
        return (int) Math.ceil((double) total / (double) pageSize);
    }

    /**
     *  计算位移量
     * @param firstPageNumber 首页号码
     * @param pageNumber 页码
     * @param pageSize 每页数量
     * @return
     */
    public static int calcOffset(int firstPageNumber, int pageNumber, int pageSize) {
        return (pageNumber - firstPageNumber) * pageSize;
    }

    /**
     * 纠正分页边界错误
     * @param pageable 分页条件
     * @param total 总记录数
     * @return 纠正后的分页条件
     */
    @NotNull
    public static Pageable fixEdge(Pageable pageable, long total) {
        Asserts.notNull(pageable, "pageable must not be null");
        if (pageable.getOffset() >= total) {
            return pageable.jumpTo(calcLastPageNumber(total, pageable.getPageSize()));
        }
        return pageable;
    }
}
