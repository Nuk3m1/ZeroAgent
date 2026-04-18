package org.zeroagent.domain.core.utils;

import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ID加密解密工具类
 * 使用AES-GCM算法确保安全性和完整性
 * 采用确定性加密方式，相同ID生成相同密文
 *
 * @author Nuk3m1
 * @version 2026年03月09日  23时49分
 */
@Slf4j
public class IdCryptoUtil {
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 16;

    // 默认密钥，生产环境建议通过配置文件或环境变量设置
    private static final String DEFAULT_SECRET_KEY = "Nuk3m1@tly5kyS3cur3K3y2026!#$%^&*()_+";

    private static final SecretKeySpec SECRET_KEY;
    static {
        SECRET_KEY = new SecretKeySpec(normalizeKey().getBytes(StandardCharsets.UTF_8), ALGORITHM);
    }
    // 私有构造函数，防止实例化
    private IdCryptoUtil() {
    }

    /**
     *  加密ID
     * @param id 原始ID
     * @return 加密后的字符串
     */
    public static String encrypt(Long id) {
        if (id == null) {
            return null;
        }
        try {
            // 为每个ID生成确定性IV
            byte[] iv = generateDeterminedIV(id);

            // 初始化加密器
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_IV_LENGTH * 8, iv);
            cipher.init(Cipher.ENCRYPT_MODE, SECRET_KEY, gcmParameterSpec);

            // 加密ID
            byte[] plainText = id.toString().getBytes(StandardCharsets.UTF_8);
            byte[] ciphertext = cipher.doFinal(plainText);

            // 组合IV和密文
            byte[] encryptedWithIv = new byte[iv.length + ciphertext.length];
            System.arraycopy(iv, 0, encryptedWithIv, 0, iv.length);
            System.arraycopy(ciphertext, 0, encryptedWithIv, iv.length, ciphertext.length);


            return Base64.getEncoder().encodeToString(encryptedWithIv);

        } catch (Exception e) {
            log.error("ID加密失败", e);
            throw new RuntimeException("ID加密失败", e);
        }
    }

    /**
     *  解密ID
     * @param encryptedId 需要解密的字符串
     * @return 原始ID
     */
    public static Long decrypt(String encryptedId) {
        if (encryptedId == null || encryptedId.trim().isEmpty()) {
            return null;
        }
        try {
            // Base64解码
            byte[] encryptedWithIv = Base64.getDecoder().decode(encryptedId);

            // 分离IV和密文
            byte[] iv = new byte[GCM_IV_LENGTH];
            byte[] ciphertext = new byte[encryptedWithIv.length - GCM_IV_LENGTH];
            System.arraycopy(encryptedWithIv, 0, iv, 0, iv.length);
            System.arraycopy(encryptedWithIv, iv.length, ciphertext, 0, ciphertext.length);

            // 初始化解密器
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.DECRYPT_MODE, SECRET_KEY, parameterSpec);

            // 解密
            byte[] plaintext = cipher.doFinal(ciphertext);
            String decryptedString = new String(plaintext, StandardCharsets.UTF_8);

            return Long.parseLong(decryptedString);
        } catch (Exception e) {
            log.error("ID解密失败", e);
            throw new RuntimeException("ID解密失败", e);
        }

    }


    /**
     *  生成确定性IV
     * @param id 需要加密的ID
     * @return 确定性IV
     */
    private static byte[] generateDeterminedIV(Long id) {
        try {
            // 将ID和密钥组合后取哈希，确保每个ID唯一对应一个IV
            String input = id + DEFAULT_SECRET_KEY;
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] hash = messageDigest.digest(input.getBytes(StandardCharsets.UTF_8));

            // 取前GCM_IV_LENGTH个字节作为IV
            byte[] iv = new byte[GCM_IV_LENGTH];
            System.arraycopy(hash, 0, iv, 0, GCM_IV_LENGTH);
            return iv;

        } catch (Exception e) {
            log.error("生成确定性IV失败",e);
            throw new RuntimeException("生成确定性IV失败",e);
        }
    }

    /**
     *  批量加密ID
     * @param ids 原始ID列表
     * @return 加密后的字符串列表
     */
    public static List<String> encryptBatch(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }

        return ids.stream()
                .map(IdCryptoUtil::encrypt)
                .collect(Collectors.toList());
    }

    /**
     *  批量解密ID
     * @param ids 密文字符串列表
     * @return 原始ID列表
     */
    public static List<Long> decryptBatch(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return ids.stream()
                .map(IdCryptoUtil::decrypt)
                .collect(Collectors.toList());
    }


    /**
     * 标准化密钥长度
     */
    private static String normalizeKey() {
        return IdCryptoUtil.DEFAULT_SECRET_KEY.substring(0, 32);
    }
}
