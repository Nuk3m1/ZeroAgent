package org.zeroagent.common.utils;


import lombok.experimental.UtilityClass;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.zeroagent.common.problem.error.CommonErrorCode;
import org.zeroagent.common.problem.error.ErrorCode;
import org.zeroagent.common.problem.exception.SysException;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 * 断言工具类。违反条件后都抛出{@link SysException}
 * <p/>
 * 注意：
 * <li>断言场景只能用于系统异常，正常编码和正常业务输入下不可能出现的异常，可以用断言。断言是一种防御性编程手段，不用于常规的参数校验。<li/>
 * <li>一旦违反断言条件，那么一定是抛出的系统异常，是代码有bug或数据有问题，是要修复的！！！<li/>
 * <li>比如数据库里出现脏数据，代码可以直接断言，这不是正常操作造成的。<li/>
 * <li>再比如底层按照主键ID更新数据库记录，那主键ID必须不能为空，也可以断言。<li/>
 * <li>但如果这个ID是前端传过来的，那么web层就要做好参数校验，参数校验不能断言。所以正常情况下，空ID不可能流入底层，所以底层可以使用断言。<li/>
 * <p/>
 * 如果要使用常规校验，请查看：Validates 类
 *
 * @author kxz
 * @version $Id: Asserts.java, v 0.1 2020年04月15日 8:12 PM kxz Exp $
 */
@UtilityClass
public class Asserts {

    /*-----------------------------------------fail-----------------------------------------*/

    /**
     * 断言失败
     *
     * @param message 错误信息
     */
    @Contract(value = "_ -> fail", pure = true)
    public static void fail(final String message) {
        throw new SysException(CommonErrorCode.UNSPECIFIED, message);
    }

    /**
     * 断言失败
     *
     * @param message 错误信息
     * @param args    格式化参数
     */
    @Contract(value = "_, _ -> fail", pure = true)
    public static void fail(final String message, final Object... args) {
        throw new SysException(CommonErrorCode.UNSPECIFIED, FormatUtil.format(message, args));
    }

    /**
     * 断言失败
     *
     * @param errorCode 错误枚举
     */
    @Contract(value = "_ -> fail", pure = true)
    public static void fail(final ErrorCode errorCode) {
        throw new SysException(errorCode);
    }

    /**
     * 断言失败
     *
     * @param errorCode 错误枚举
     * @param message   错误信息
     */
    @Contract(value = "_, _ -> fail", pure = true)
    public static void fail(final ErrorCode errorCode, final String message) {
        throw new SysException(errorCode, message);
    }

    /**
     * 断言失败
     *
     * @param errorCode 错误枚举
     * @param args      格式化参数
     */
    @Contract(value = "_, _ -> fail", pure = true)
    public static void fail(final ErrorCode errorCode, final Object... args) {
        throw new SysException(errorCode, args);
    }

    /*-----------------------------------------isTrue-----------------------------------------*/

    /**
     * 断言布尔表达式必须为true
     *
     * @param expression 断言条件
     * @param message    错误信息
     */
    @Contract(value = "false, _ -> fail", pure = true)
    public static void isTrue(final boolean expression, final String message) {
        if (!expression) {
            throw new SysException(CommonErrorCode.UNSPECIFIED, message);
        }
    }

    /**
     * 断言布尔表达式必须为true
     *
     * @param expression 断言条件
     * @param message    错误信息（支持占位符{}）
     * @param args       格式化参数
     */
    @Contract(value = "false, _, _ -> fail", pure = true)
    public static void isTrue(final boolean expression, final String message, final Object... args) {
        if (!expression) {
            throw new SysException(CommonErrorCode.UNSPECIFIED, FormatUtil.format(message, args));
        }
    }

    /**
     * 断言布尔表达式必须为true
     *
     * @param expression 断言条件
     * @param errorCode  错误枚举
     */
    @Contract(value = "false, _ -> fail", pure = true)
    public static void isTrue(final boolean expression, final ErrorCode errorCode) {
        if (!expression) {
            throw new SysException(errorCode);
        }
    }

    /**
     * 断言布尔表达式必须为true
     *
     * @param expression 断言条件
     * @param errorCode  错误枚举
     * @param message    错误信息
     */
    @Contract(value = "false, _, _ -> fail", pure = true)
    public static void isTrue(final boolean expression, final ErrorCode errorCode, final String message) {
        if (!expression) {
            throw new SysException(errorCode, message);
        }
    }

    /**
     * 断言布尔表达式必须为true
     *
     * @param expression 断言条件
     * @param errorCode  错误枚举
     * @param args       格式化参数
     */
    @Contract(value = "false, _, _ -> fail", pure = true)
    public static void isTrue(final boolean expression, final ErrorCode errorCode, final Object... args) {
        if (!expression) {
            throw new SysException(errorCode, args);
        }
    }

    /*-----------------------------------------isFalse-----------------------------------------*/

    /**
     * 断言布尔表达式必须为false
     *
     * @param expression 断言条件
     * @param errorCode  错误枚举
     */
    @Contract(value = "true, _ -> fail", pure = true)
    public static void isFalse(final boolean expression, final ErrorCode errorCode) {
        if (expression) {
            throw new SysException(errorCode);
        }
    }

    /**
     * 断言布尔表达式必须为false
     *
     * @param expression 断言条件
     * @param errorCode  错误枚举
     * @param message    错误消息
     */
    @Contract(value = "true, _, _ -> fail", pure = true)
    public static void isFalse(final boolean expression, final ErrorCode errorCode, final String message) {
        if (expression) {
            throw new SysException(errorCode, message);
        }
    }

    /**
     * 断言布尔表达式必须为false
     *
     * @param expression 断言条件
     * @param errorCode  错误枚举
     * @param args       格式化参数
     */
    @Contract(value = "true, _, _ -> fail", pure = true)
    public static void isFalse(final boolean expression, final ErrorCode errorCode, final Object... args) {
        if (expression) {
            throw new SysException(errorCode, args);
        }
    }

    /*-----------------------------------------isNull-----------------------------------------*/

    /**
     * 断言对象为空
     *
     * @param object    断言对象
     * @param errorCode 错误枚举
     */
    @Contract(value = "!null, _ -> fail", pure = true)
    public static void isNull(@Nullable Object object, final ErrorCode errorCode) {
        if (object != null) {
            throw new SysException(errorCode);
        }
    }

    /*-----------------------------------------notNull-----------------------------------------*/

    /**
     * 断言对象非空
     *
     * @param object 断言对象
     */
    @Contract(value = "null -> fail", pure = true)
    public static void notNull(@Nullable Object object) {
        if (null == object) {
            throw new SysException(CommonErrorCode.UNSPECIFIED);
        }
    }

    /**
     * 断言对象非空
     *
     * @param object  断言对象
     * @param message 错误信息
     */
    @Contract(value = "null, _ -> fail", pure = true)
    public static void notNull(@Nullable Object object, String message) {
        if (null == object) {
            throw new SysException(CommonErrorCode.UNSPECIFIED, message);
        }
    }

    /**
     * 断言对象非空
     *
     * @param object  断言对象
     * @param message 错误信息（支持占位符{}）
     * @param args    格式化参数
     */
    @Contract(value = "null, _, _ -> fail", pure = true)
    public static void notNull(@Nullable Object object, String message, Object... args) {
        if (null == object) {
            throw new SysException(CommonErrorCode.UNSPECIFIED, FormatUtil.format(message, args));
        }
    }

    /**
     * 断言对象非空
     *
     * @param object    断言对象
     * @param errorCode 错误枚举
     */
    @Contract(value = "null, _ -> fail", pure = true)
    public static void notNull(@Nullable Object object, ErrorCode errorCode) {
        if (null == object) {
            throw new SysException(errorCode);
        }
    }

    /**
     * 断言对象非空
     *
     * @param object    断言对象
     * @param errorCode 错误枚举
     * @param message   错误消息
     */
    @Contract(value = "null, _, _ -> fail", pure = true)
    public static void notNull(@Nullable Object object, ErrorCode errorCode, String message) {
        if (null == object) {
            throw new SysException(errorCode, message);
        }
    }

    /**
     * 断言对象非空
     *
     * @param object    断言对象
     * @param errorCode 错误枚举
     * @param args      格式化参数
     */
    @Contract(value = "null, _, _ -> fail", pure = true)
    public static void notNull(@Nullable Object object, ErrorCode errorCode, Object... args) {
        if (null == object) {
            throw new SysException(errorCode, args);
        }
    }

    /*-----------------------------------------notBlank-----------------------------------------*/

    /**
     * 断言字符串不能为空
     *
     * @param value 字符串
     */
    @Contract(value = "null -> fail", pure = true)
    public static void notBlank(@Nullable String value) {
        if (StringUtils.isBlank(value)) {
            throw new SysException(CommonErrorCode.UNSPECIFIED);
        }
    }

    /**
     * 断言字符串不能为空
     *
     * @param value   字符串
     * @param message 描述
     */
    @Contract(value = "null, _ -> fail", pure = true)
    public static void notBlank(@Nullable String value, final String message) {
        if (StringUtils.isBlank(value)) {
            throw new SysException(CommonErrorCode.UNSPECIFIED, message);
        }
    }

    /**
     * 断言字符串不能为空
     *
     * @param value   字符串
     * @param message 错误信息（支持占位符{}）
     * @param args    格式化参数
     */
    @Contract(value = "null, _, _ -> fail", pure = true)
    public static void notBlank(@Nullable String value, final String message, final Object... args) {
        if (StringUtils.isBlank(value)) {
            throw new SysException(CommonErrorCode.UNSPECIFIED, FormatUtil.format(message, args));
        }
    }

    /**
     * 断言字符串不能为空
     *
     * @param value     断言字符串
     * @param errorCode 错误枚举
     */
    @Contract(value = "null, _ -> fail", pure = true)
    public static void notBlank(@Nullable String value, final ErrorCode errorCode) {
        if (StringUtils.isBlank(value)) {
            throw new SysException(errorCode);
        }
    }

    /**
     * 断言字符串不能为空
     *
     * @param value     断言字符串
     * @param errorCode 错误枚举
     * @param message   错误消息
     */
    @Contract(value = "null, _, _ -> fail", pure = true)
    public static void notBlank(@Nullable String value, final ErrorCode errorCode, final String message) {
        if (StringUtils.isBlank(value)) {
            throw new SysException(errorCode, message);
        }
    }

    /**
     * 断言字符串不能为空
     *
     * @param value     断言字符串
     * @param errorCode 错误枚举
     * @param args      格式化参数
     */
    @Contract(value = "null, _, _ -> fail", pure = true)
    public static void notBlank(@Nullable String value, final ErrorCode errorCode, final Object... args) {
        if (StringUtils.isBlank(value)) {
            throw new SysException(errorCode, args);
        }
    }

    /*-----------------------------------------isEmpty-----------------------------------------*/

    /**
     * 断言Map必须为空
     *
     * @param map       Map对象
     * @param errorCode 错误枚举
     */
    @Contract(value = "!null, _ -> fail", pure = true)
    public static void isEmpty(@Nullable Map<?, ?> map, final ErrorCode errorCode) {
        if (MapUtils.isNotEmpty(map)) {
            throw new SysException(errorCode);
        }
    }

    /*-----------------------------------------notEmpty-----------------------------------------*/

    /**
     * 断言字符串不能为空
     *
     * @param value   字符串
     * @param message 描述
     */
    @Contract(value = "null, _ -> fail", pure = true)
    public static void notEmpty(@Nullable String value, final String message) {
        if (value == null || value.isEmpty()) {
            throw new SysException(CommonErrorCode.UNSPECIFIED, message);
        }
    }

    /**
     * 断言Map不为空
     *
     * @param map       Map对象
     * @param errorCode 错误枚举
     */
    @Contract(value = "null, _ -> fail", pure = true)
    public static void notEmpty(@Nullable Map<?, ?> map, final ErrorCode errorCode) {
        if (MapUtils.isEmpty(map)) {
            throw new SysException(errorCode);
        }
    }

    /**
     * 断言Map不为空
     *
     * @param map       Map对象
     * @param errorCode 错误枚举
     * @param message   错误信息（支持占位符{}）
     */
    @Contract(value = "null, _, _ -> fail", pure = true)
    public static void notEmpty(@Nullable Map<?, ?> map, final ErrorCode errorCode, final String message) {
        if (MapUtils.isEmpty(map)) {
            throw new SysException(errorCode, message);
        }
    }

    /**
     * 断言Map不为空
     *
     * @param map       Map对象
     * @param errorCode 错误枚举
     * @param args      错误信息参数
     */
    @Contract(value = "null, _, _ -> fail", pure = true)
    public static void notEmpty(@Nullable Map<?, ?> map, final ErrorCode errorCode, final Object... args) {
        if (MapUtils.isEmpty(map)) {
            throw new SysException(errorCode, args);
        }
    }

    /**
     * 断言集合不为空
     *
     * @param collection 断言集合
     * @param message    错误信息
     */
    @Contract(value = "null, _ -> fail", pure = true)
    public static void notEmpty(@Nullable Collection<?> collection, final String message) {
        if (CollectionUtils.isEmpty(collection)) {
            throw new SysException(CommonErrorCode.UNSPECIFIED, message);
        }
    }

    /**
     * 断言集合不为空
     *
     * @param collection 断言集合
     * @param errorCode  错误枚举
     */
    @Contract(value = "null, _ -> fail", pure = true)
    public static void notEmpty(@Nullable Collection<?> collection, final ErrorCode errorCode) {
        if (CollectionUtils.isEmpty(collection)) {
            throw new SysException(errorCode);
        }
    }

    /**
     * 断言集合不为空
     *
     * @param collection 断言集合
     * @param errorCode  错误枚举
     * @param message    错误消息
     */
    @Contract(value = "null, _, _ -> fail", pure = true)
    public static void notEmpty(@Nullable Collection<?> collection, final ErrorCode errorCode, final String message) {
        if (CollectionUtils.isEmpty(collection)) {
            throw new SysException(errorCode, message);
        }
    }

    /**
     * 断言集合不为空
     *
     * @param collection 断言集合
     * @param errorCode  错误枚举
     * @param args       格式化参数
     */
    @Contract(value = "null, _, _ -> fail", pure = true)
    public static void notEmpty(@Nullable Collection<?> collection, final ErrorCode errorCode, final Object... args) {
        if (CollectionUtils.isEmpty(collection)) {
            throw new SysException(errorCode, args);
        }
    }

    /*-----------------------------------------objectEquals-----------------------------------------*/

    /**
     * 断言两个对象相同
     *
     * @param o1 对象1
     * @param o2 对象2
     */
    public static <T> void objectEquals(final T o1, final T o2) {
        if (!Objects.equals(o1, o2)) {
            throw new SysException(CommonErrorCode.UNSPECIFIED);
        }
    }

    /**
     * 断言两个对象相同
     *
     * @param o1      对象1
     * @param o2      对象2
     * @param message 错误信息
     */
    public static <T> void objectEquals(final T o1, final T o2, final String message) {
        if (!Objects.equals(o1, o2)) {
            throw new SysException(CommonErrorCode.UNSPECIFIED, message);
        }
    }
}

