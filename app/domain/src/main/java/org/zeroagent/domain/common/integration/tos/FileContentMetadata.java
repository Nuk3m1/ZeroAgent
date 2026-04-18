package org.zeroagent.domain.common.integration.tos;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 *
 * @author Nuk3m1
 * @version 2026年03月14日  16时05分
 */
@Data
@Accessors(chain = true)
public class FileContentMetadata {

    /**
     * 字节数
     */
    private Long   length;
    /**
     * 内容格式
     */
    private String type;
    /**
     * 内容Content-MD5
     */
    private String md5;
    /**
     * 内容校验码 CRC-64
     */
    private Long   crc;
}
