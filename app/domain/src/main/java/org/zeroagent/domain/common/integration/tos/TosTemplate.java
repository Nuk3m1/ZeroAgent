package org.zeroagent.domain.common.integration.tos;

import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.Map;

public interface TosTemplate {
    /**
     *  获取存储空间所在地域
     * @param bucket 存储空间
     * @return 地域
     */
    String getBucketRegion(String bucket);
    /**
     * 列举目录
     *
     * @param bucket      存储空间
     * @param prefix      公共前缀（以/结尾）
     * @param previousDir 本地列举目录的起点（不包含）
     * @param limit       列举目录的最大个数
     * @return 目录列表结果
     */
    DirListResult listDirs(String bucket, String prefix, String previousDir, int limit);

    /**
     * 上传文件
     *  注：⚠️⚠️ key只能包含数字、英文字母（大小写）和中划线
     * @param bucket 存储空间
     * @param key 文件对象key
     * @param acl 文件访问控制
     * @param inputStream 文件输入流
     * @return TOS协议下的文件地址
     */
    default String upload(String bucket, String key, FileAcl acl, InputStream inputStream) {
        return this.upload(bucket, key, acl, inputStream, null);
    }

    /**
     *  上传文件
     * @param bucket 存储空间
     * @param key 文件对象key
     * @param acl 文件访问控制
     * @param inputStream 文件输入流
     * @param userMetadata 自定义元数据
     * @return TOS协议下的文件地址
     */
    String upload(String bucket, String key, FileAcl acl, InputStream inputStream, @Nullable Map<String, String> userMetadata);

    /**
     * 下载文件
     * @param tosUrl TOS文件地址 形如 tos://bucket/key
     * @return 文件流（⚠️注意关流）
     */
    InputStream download(String tosUrl);

    /**
     * 下载文件
     * @param bucket 存储空间
     * @param key    文件对象key
     * @return 文件流（⚠️注意关流）
     */
    InputStream download(String bucket, String key);

    /**
     * 获取文件HTTP URL (纯本地计算，无网络IO开销)
     * @param bucket                存储空间
     * @param key                   文件对象key
     * @param accessOption          访问选项
     * @return                      文件访问链接
     */
    default String getHttpUrl(String bucket, String key, FileAccessOption accessOption) {
        return this.getHttpUrl(bucket, key, accessOption, null);
    }

    /**
     * 获取文件HTTP URL (纯本地计算，无网络IO开销)
     * @param bucket                存储空间
     * @param key                   文件对象key
     * @param accessOption          访问选项
     * @param fileProcessOption     文件处理选项
     * @return                      文件访问链接
     */
    String getHttpUrl(String bucket, String key, FileAccessOption accessOption, @Nullable FileProcessOption fileProcessOption);

    /**
     * 获取文件HTTP URL (纯本地计算，无网络IO开销)
     * @param tosUrl                TOS文件地址(tos://bucket/key)
     * @param accessOption          访问选项
     * @return                      文件访问链接
     */
    default String getHttpUrl(String tosUrl, FileAccessOption accessOption) {
        return this.getHttpUrl(tosUrl, accessOption, null);
    }

    /**
     * 获取文件HTTP URL (纯本地计算，无网络IO开销)
     * @param tosUrl                TOS文件地址(tos://bucket/key)
     * @param accessOption          访问选项
     * @param fileProcessOption     文件处理选项
     * @return                      文件访问链接
     */
    String getHttpUrl(String tosUrl, FileAccessOption accessOption, @Nullable FileProcessOption fileProcessOption);

    /**
     * 获取指定TOS对象的元数据
     * @param bucket                存储空间
     * @param key                   文件对象key
     * @return                      用户自定义文件元数据
     */
    JSONObject getUserMetadata(String bucket, String key);

    /**
     * 检查指定文件是否存在
     * @param bucket                存储空间
     * @param key                   文件对象key
     * @return                      是否存在
     */
    boolean isFilePresent(String bucket, String key);

    /**
     * 检查指定文件是否存在
     * @param tosUrl                TOS文件地址(tos://bucket/key)
     * @return                      是否存在
     */
    boolean isFilePresent(String tosUrl);




}
