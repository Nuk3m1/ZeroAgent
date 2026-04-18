package org.zeroagent.domain.common.integration.tos;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 *
 * @author Nuk3m1
 * @version 2026年03月14日  16时08分
 */
@Data
@Accessors(chain = true)
public class DirListResult {
    /**
     * 公共前缀
     */
    private String prefix;
    /**
     * 目录前缀 （不带/）
     */
    private List<String> dirNames;
    /**
     * 是否还有更多
     */
    private boolean hasMore;
}
