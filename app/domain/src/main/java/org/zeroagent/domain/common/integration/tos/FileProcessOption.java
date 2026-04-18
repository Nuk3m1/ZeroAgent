package org.zeroagent.domain.common.integration.tos;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 *  文件处理选项
 * @author Nuk3m1
 * @version 2026年03月14日  23时24分
 */
@Data(staticConstructor = "of")
@Accessors(fluent = true)
public class FileProcessOption {

    /**
     * 文件样式调整： 旋转、裁剪等
     * 例如 "image/resize,m_fixed,w_100,h_100/rotate,90"
     */
    private final String style;
}
