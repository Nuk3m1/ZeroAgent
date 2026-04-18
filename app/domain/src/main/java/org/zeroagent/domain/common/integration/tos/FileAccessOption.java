package org.zeroagent.domain.common.integration.tos;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.Instant;

/**
 *
 * @author Nuk3m1
 * @version 2026年03月14日  16时13分
 */
@Data(staticConstructor = "of")
@Accessors(fluent = true)
public class FileAccessOption {
    /**
     * 读取权限
     */
    private FileAcl acl;
    /**
     *  文件为私有读时，指定过期时间
     */
    private Instant expiredTime;
}
