package org.zeroagent.domain.common.integration.tos;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.zeroagent.common.problem.error.CommonErrorCode;
import org.zeroagent.common.problem.exception.SysException;
import org.zeroagent.common.utils.Asserts;
import org.zeroagent.common.utils.FormatUtil;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 *
 * @author Nuk3m1
 * @version 2026年03月14日  16时20分
 */
@UtilityClass
public class TosUtil {
    /**
     *  转换为文件的tos地址
     * @param bucket 存储空间
     * @param key    文件对象key
     * @return 文件TOS地址
     */
    public String toTosUrl(String bucket, String key) {
        return FormatUtil.format("tos://{}/{}", bucket, key);
    }

    /**
     *  解析文件对象Bucket
     * @param url 文件地址（支持tos和https）
     * @return 存储空间Bucket
     */
    public String resolveBucket(String url) {
        Asserts.notBlank(url, CommonErrorCode.UNSPECIFIED);
        URI uri;
        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            throw new SysException(CommonErrorCode.UNSPECIFIED, e);
        }
        if ("tos".equalsIgnoreCase(uri.getScheme())) {
            return uri.getHost();
        }
        if ("https".equalsIgnoreCase(uri.getScheme())) {
            return uri.getHost().split("\\.", 2)[0];
        }
        throw new SysException(CommonErrorCode.UNSPECIFIED, "[resolveBucket]unknown scheme");
    }
    /**
     * 解析文件对象key
     *
     * @param url 文件url地址（支持tos协议和http协议）
     * @return 文件对象key
     */
    public String resolveKey(String url) {
        Asserts.notBlank(url, CommonErrorCode.UNSPECIFIED);
        try {
            URI uri = new URI(url);
            String key = uri.getPath();
            if (StringUtils.startsWith(key, "/")) {
                key = StringUtils.substring(key, 1);
            }
            return key;
        } catch (URISyntaxException e) {
            throw new SysException(CommonErrorCode.UNSPECIFIED, e);
        }
    }

}
