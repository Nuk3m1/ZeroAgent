package org.zeroagent.domain.core.utils;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 文本工具类
 *
 * @author Nuk3m1
 * @version 2026年03月10日  15时49分
 */
@UtilityClass
public class TextUtil {
    public static final Pattern CHINESE_CHAR_PATTERN = Pattern.compile("[\\u4e00-\\u9fa5]");
    /**
     * 包含了拉丁字符，中文字符，日文字符，泰文字符
     * </p>
     * <a href="https://symbl.cc/cn/unicode/table">Unicode码表对照</a>
     */
    public static final Pattern WORD_PATTERN         = Pattern.compile("([^\\w\\s])|(\\b[0-9a-zA-Z\\u00c0-\\u024f\\u1e00-\\u1eff\\u2c60-\\u2c7f\\ua720-\\ua7ff\\uaa80-\\uaadf\\uab30-\\uab6f\\x8a\\x8e\\x9a\\x9e\\x9f]+\\b)|[\\u4e00-\\u9fa5\\u3040-\\u30ff\\u31f0-\\u31ff\\u0e00-\\u0e7f]");

    /**
     *  文本中是否包含中文汉字
     * @param text 文本
     * @return 是否包含中文汉字
     */
    public static boolean containsChineseCharacter(String text) {
        if (StringUtils.isBlank(text)) {
            return false;
        }
        Matcher matcher = CHINESE_CHAR_PATTERN.matcher(text);
        return matcher.find();
    }

    /**
     *  获取文本的前 N 个字
     * @param text 文本
     * @param n n个数
     * @return 子串
     */
    public static String getTopWords(String text, int n) {
        if (StringUtils.isBlank(text)) {
            return text;
        }
        StringBuilder result = new StringBuilder();
        Matcher matcher = WORD_PATTERN.matcher(text);
        int count = 0;
        while (matcher.find()) {
            result.append(matcher.group());
            count++;
            if (count >= n) {
                break;
            }
        }
        return result.toString();
    }

    /**
     *  统计文本里的字数
     * @param text 文本
     * @return 字数
     */
    public static int countWords(String text) {
        if (StringUtils.isBlank(text)) {
            return 0;
        }
        Matcher matcher = WORD_PATTERN.matcher(text);
        int cont = 0;
        while (matcher.find()) {
            cont++;
        }
        return cont;
    }
}
