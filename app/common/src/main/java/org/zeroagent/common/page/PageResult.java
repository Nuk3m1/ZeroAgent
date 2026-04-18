package org.zeroagent.common.page;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 *
 *
 * @author Nuk3m1
 * @version 2026年03月08日  16时18分
 */
@Data
@Accessors(chain = true)
public class PageResult<T> {

    /**
     * 结果集
     */
    private List<T> content;
    /**
     * 总记录数
     */
    private Long    total;
}
