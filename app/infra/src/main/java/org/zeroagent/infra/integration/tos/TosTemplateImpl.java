package org.zeroagent.infra.integration.tos;

import com.volcengine.tos.TOSV2;
import com.volcengine.tos.TosException;
import com.volcengine.tos.comm.HttpMethod;
import com.volcengine.tos.comm.common.ACLType;
import com.volcengine.tos.internal.util.TosUtils;
import com.volcengine.tos.model.bucket.GetBucketLocationInput;
import com.volcengine.tos.model.object.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;
import org.springframework.stereotype.Component;
import org.zeroagent.common.problem.error.CommonErrorCode;
import org.zeroagent.common.problem.exception.SysException;
import org.zeroagent.common.utils.Asserts;
import org.zeroagent.common.utils.json.JSON;
import org.zeroagent.domain.common.integration.tos.*;
import org.zeroagent.infra.integration.tos.config.VolTosProperties;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * TOS操作 第三方实现
 * @author Nuk3m1
 * @version 2026年03月14日  16时42分
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class TosTemplateImpl implements TosTemplate {
    /**
     * 用户自定义元数据的key只能包含数字、英文字母（大小写）和中划线。
     */
    private final static Pattern            USER_MATADATA_KEY_PATTERN = Pattern.compile("^[a-zA-Z0-9-]+$");
    private final Map<String, String>       BUCKET_REGION_CACHE = new ConcurrentHashMap<>();
    private final TOSV2                     tosClient;
    private final VolTosProperties          volTosProperties;
    @Override
    public String getBucketRegion(String bucket) {
        BUCKET_REGION_CACHE.computeIfAbsent(bucket, b -> {
            GetBucketLocationInput input = new GetBucketLocationInput().setBucket(b);
            return tosClient.getBucketLocation(input).getRegion();
        });
        return BUCKET_REGION_CACHE.get(bucket);
    }

    @Override
    public DirListResult listDirs(String bucket, String prefix, String previousDir, int limit) {
        Asserts.isTrue(prefix.endsWith("/"), "prefix must end with '/'");
        ListObjectsType2Input input = new ListObjectsType2Input()
                .setBucket(bucket)
                .setPrefix(prefix)
                .setDelimiter("/")
                .setMaxKeys(limit);
        if (StringUtils.isNotBlank(previousDir)) {
            input.setContinuationToken(previousDir);
        }
        ListObjectsType2Output output = tosClient.listObjectsType2(input);
        final List<String> dirNames = new ArrayList<>();
        if (output.getCommonPrefixes() != null) {
            output.getCommonPrefixes().forEach(commonPrefix -> {
                String dirName = StringUtils.substringAfter(commonPrefix.getPrefix(), prefix);
                dirNames.add(dirName);
            });
        }
        DirListResult result = new DirListResult()
                .setDirNames(dirNames)
                .setPrefix(prefix)
                .setHasMore(output.isTruncated());
        return result;

    }

    @Override
    public String upload(String bucket, String key, FileAcl acl, InputStream inputStream, @Nullable Map<String, String> userMetadata) {
        if (userMetadata != null) {
            userMetadata.keySet()
                    .stream()
                    .map(USER_MATADATA_KEY_PATTERN::matcher)
                    .forEach(matcher -> {Asserts.isTrue(matcher.matches(), "User metadata are illegal");});
        }
        ObjectMetaRequestOptions metadata = new ObjectMetaRequestOptions();
        if (acl != null) {
            if (acl == FileAcl.PRIVATE) {
                // 私有读文件
                metadata.setAclType(ACLType.ACL_PRIVATE);
            } else {
                // 公有读文件
                metadata.setAclType(ACLType.ACL_PUBLIC_READ);
            }
        }
        if (userMetadata != null) {
            metadata.setCustomMetadata(userMetadata);
        }
        PutObjectInput input = new PutObjectInput()
                .setBucket(bucket)
                .setKey(key)
                .setContent(inputStream)
                .setOptions(metadata);
        try {
            tosClient.putObject(input);
        } catch (TosException e) {
            throw new SysException(CommonErrorCode.UNSPECIFIED, e);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
        return TosUtil.toTosUrl(bucket, key);
    }

    @Override
    public InputStream download(String tosUrl) {
        try {
            URI uri = new URI(tosUrl);
            String bucket = uri.getHost();
            String key = resolveKey(uri);
            return this.download(bucket, key);
        } catch (URISyntaxException e) {
            throw new SysException(CommonErrorCode.UNSPECIFIED, e.getMessage());
        }
    }

    @Override
    public InputStream download(String bucket, String key) {
        GetObjectV2Input input = new GetObjectV2Input().setBucket(bucket).setKey(key);
        GetObjectV2Output output = tosClient.getObject(input);
        return output.getContent();
    }

    @Override
    public String getHttpUrl(String bucket, String key, FileAccessOption accessOption, @Nullable FileProcessOption fileProcessOption) {
        Asserts.notBlank(bucket, "bucket must not be blank");
        Asserts.notBlank(key, "key must not be blank");

        final String url;
        if (accessOption.acl() == null || accessOption.acl() == FileAcl.PRIVATE) {
            // 私有读（默认）
            Asserts.notNull(accessOption.expiredTime(), "expiredTime must not be null");
            PreSignedURLInput input = new PreSignedURLInput()
                    .setBucket(bucket)
                    .setKey(key)
                    .setHttpMethod(HttpMethod.GET)
                    .setExpires(Duration.between(Instant.now(), accessOption.expiredTime()).toSeconds())
                    // 强行覆盖成外网endpoint
                    .setAlternativeEndpoint(volTosProperties.getReadEndpoint());
            try {
                url = tosClient.preSignedURL(input).getSignedUrl();
            } catch (TosException e) {
                log.error("failed to generate pre-signed url for bucket {}, key {}", bucket, key);
                throw new SysException(CommonErrorCode.UNSPECIFIED, e.getMessage());
            }
        } else {
            // 公有读
            url = buildPublicUrl(bucket, volTosProperties.getReadEndpoint(), key);
        }
        return url;
    }

    @Override
    public String getHttpUrl(String tosUrl, FileAccessOption accessOption, @Nullable FileProcessOption fileProcessOption) {
        Asserts.notBlank(tosUrl, "tosUrl must not be blank");
        try {
            URI uri = new URI(tosUrl);
            String bucket = uri.getHost();
            String key = resolveKey(uri);
            String query = uri.getQuery();
            if (StringUtils.isNotBlank(query)) {
                if (fileProcessOption != null && StringUtils.isNotBlank(fileProcessOption.style())) {
                    fileProcessOption = FileProcessOption.of(fileProcessOption.style() + "&" + query);
                } else {
                    fileProcessOption = FileProcessOption.of(query);
                }
            }
            return this.getHttpUrl(bucket, key, accessOption, fileProcessOption);
        } catch (URISyntaxException e) {
            log.error("failed to get HttpUrl");
            throw new SysException(CommonErrorCode.UNSPECIFIED, e.getMessage());
        }
    }

    @Override
    public JSONObject getUserMetadata(String bucket, String key) {
        Asserts.notBlank(bucket, "bucket must not be blank");
        Asserts.notBlank(key, "key must not be blank");

        GetObjectV2Input input = new GetObjectV2Input().setBucket(bucket).setKey(key);
        Map<String, String> metadata = tosClient.getObject(input).getCustomMetadata();
        return JSON.toJSONObject(metadata);
    }


    @Override
    public boolean isFilePresent(String bucket, String key) {
        GetObjectV2Input input = new GetObjectV2Input().setBucket(bucket).setKey(key);
        try {
            tosClient.getObject(input);
            return true;
        } catch (TosException e) {
            if (e.getCode().equals("NoSuchBucket") || e.getCode().equals("NoSuchKey")) {
                return false;
            }
            throw e;
        }
    }

    @Override
    public boolean isFilePresent(String tosUrl) {
        try {
            URI uri = new URI(tosUrl);
            String bucket = uri.getHost();
            String key = resolveKey(uri);
            return this.isFilePresent(bucket, key);
        } catch (URISyntaxException e) {
            throw new SysException(CommonErrorCode.UNSPECIFIED, e.getMessage());
        }
    }

    private static String resolveKey(URI tosUri) {
        String key = tosUri.getPath();
        if (StringUtils.startsWith(key, "/")) {
            key = StringUtils.substring(key, 1);
        }
        return key;
    }

    private static String buildPublicUrl(String bucket, String endpoint, String key) {
        String cleanKey = key.startsWith("/") ? key.substring(1) : key;
        return String.format("https://%s.%s/%s", bucket, endpoint, cleanKey);
    }

}
