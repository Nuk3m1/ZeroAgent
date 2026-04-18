package org.zeroagent.common.utils;

/**
 *
 * @author Nuk3m1
 * @version 2026年03月15日  20时50分
 */

import lombok.experimental.UtilityClass;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.zeroagent.common.problem.error.CommonErrorCode;
import org.zeroagent.common.problem.error.ErrorCode;
import org.zeroagent.common.problem.exception.BizException;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 * 常规校验工具类。违反条件后都抛出{@link BizException}
 *
 * @author joton
 * @version : Validates.java, v 0.1 2023-01-05 19:30 joton
 * @see BizException
 * @see CommonErrorCode#ILLEGAL_PARAM
 * @see Asserts
 */
@UtilityClass
public class Validates {

    /*---------------------------------------requireXxx---------------------------------------*/

    /**
     * Check that the given String is a long number
     *
     * @param text      the String to check
     * @param fieldName the exception fieldName to use if the validation fails
     * @return the long number converted from the given String
     * @throws BizException if the text is not a long number
     */
    @Contract(value = "null, _ -> fail", pure = true)
    public long requireLong(@Nullable String text, final String fieldName) {
        try {
            notNull(text, fieldName + " must be a long number");
            return Long.parseLong(text);
        } catch (NumberFormatException e) {
            throw new BizException(CommonErrorCode.ILLEGAL_PARAM, fieldName + " must be a long number");
        }
    }

    /**
     * Check that the given String is a long number
     *
     * @param text      the String to check
     * @param fieldName the exception fieldName to use if the validation fails
     * @return the long number converted from the given String
     * @throws BizException if the text is not a long number
     */
    @Contract(value = "null, _ -> fail", pure = true)
    public int requireInteger(@Nullable String text, final String fieldName) {
        try {
            notNull(text, fieldName + " must be a int number");
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            throw new BizException(CommonErrorCode.ILLEGAL_PARAM, fieldName + " must be a int number");
        }
    }

    /*-----------------------------------------isTrue-----------------------------------------*/

    /**
     * Check a boolean expression must be true.
     *
     * @param expression a boolean expression
     * @throws BizException if the test result is {@code false}.
     */
    @Contract(value = "false -> fail", pure = true)
    public void isTrue(final boolean expression) {
        if (!expression) {
            throw new BizException(CommonErrorCode.ILLEGAL_PARAM);
        }
    }

    /**
     * Check a boolean expression must be true.
     *
     * @param expression a boolean expression
     * @param message    错误信息
     * @throws BizException if the test result is {@code false}.
     */
    @Contract(value = "false, _ -> fail", pure = true)
    public void isTrue(final boolean expression, final String message) {
        if (!expression) {
            throw new BizException(CommonErrorCode.ILLEGAL_PARAM, message);
        }
    }

    /**
     * Check a boolean expression must be true.
     *
     * @param expression a boolean expression
     * @param message    错误信息（如果有占位符，请使用{}，和log4j2一样的风格）
     * @param args       错误参数
     * @throws BizException if the test result is {@code false}.
     */
    @Contract(value = "false, _, _ -> fail", pure = true)
    public void isTrue(final boolean expression, final String message, final Object... args) {
        if (!expression) {
            throw new BizException(CommonErrorCode.ILLEGAL_PARAM, FormatUtil.buildMessage(message, args));
        }
    }

    /**
     * Check a boolean expression must be true.
     *
     * @param expression a boolean expression
     * @param errorCode  错误码
     * @throws BizException if the test result is {@code false}.
     */
    @Contract(value = "false, _ -> fail", pure = true)
    public static void isTrue(final boolean expression, final ErrorCode errorCode) {
        if (!expression) {
            throw new BizException(errorCode);
        }
    }

    /**
     * Check a boolean expression must be true.
     *
     * @param expression a boolean expression
     * @param errorCode  错误码（如果有占位符，请使用{}，和log4j2一样的风格）
     * @param message    错误信息（也可以作为占位符参数）
     * @throws BizException if the test result is {@code false}.
     */
    @Contract(value = "false, _, _ -> fail", pure = true)
    public void isTrue(final boolean expression, final ErrorCode errorCode, final String message) {
        if (!expression) {
            throw new BizException(errorCode, message);
        }
    }

    /**
     * Check a boolean expression must be true.
     *
     * @param expression a boolean expression
     * @param errorCode  错误码（如果有占位符，请使用{}，和log4j2一样的风格）
     * @param args       错误参数
     * @throws BizException if the test result is {@code false}.
     */
    @Contract(value = "false, _, _ -> fail", pure = true)
    public static void isTrue(final boolean expression, final ErrorCode errorCode, final Object... args) {
        if (!expression) {
            throw new BizException(errorCode, args);
        }
    }

    /*-----------------------------------------isNull-----------------------------------------*/

    /**
     * Check an object must be {@code null}, throwing {@link BizException} if the object is {@code null}.
     *
     * @param object  the object to check
     * @param message the exception message to use if the validation fails
     * @throws BizException if the object is not {@code null}
     */
    @Contract(value = "!null, _ -> fail", pure = true)
    public static void isNull(@Nullable Object object, final String message) {
        if (object != null) {
            throw new BizException(CommonErrorCode.ILLEGAL_PARAM, message);
        }
    }

    /**
     * Check an object must be {@code null}, throwing {@link BizException} if the object is {@code null}.
     *
     * @param object  the object to check
     * @param message the exception message to use if the validation fails
     * @param args    错误参数
     * @throws BizException if the object is not {@code null}
     */
    @Contract(value = "!null, _, _ -> fail", pure = true)
    public static void isNull(@Nullable Object object, final String message, final Object... args) {
        if (object != null) {
            throw new BizException(CommonErrorCode.ILLEGAL_PARAM, FormatUtil.buildMessage(message, args));
        }
    }

    /**
     * Check that an object must be {@code null} .
     *
     * @param object    the object to check
     * @param errorCode 错误码
     * @throws BizException if the object is not {@code null}
     */
    @Contract(value = "!null, _ -> fail", pure = true)
    public static void isNull(@Nullable Object object, final ErrorCode errorCode) {
        if (object != null) {
            throw new BizException(errorCode);
        }
    }

    /**
     * Check an object must be {@code null}
     *
     * @param object    the object to check
     * @param errorCode 错误码（如果有占位符，请使用{}，和log4j2一样的风格）
     * @param message   错误信息（也可以作为占位符参数）
     * @throws BizException if the object is not {@code null}
     */
    @Contract(value = "!null, _, _ -> fail", pure = true)
    public static void isNull(@Nullable Object object, final ErrorCode errorCode, final String message) {
        if (object != null) {
            throw new BizException(errorCode, message);
        }
    }

    /**
     * Check an object must be {@code null}
     *
     * @param object    the object to check
     * @param errorCode 错误码（如果有占位符，请使用{}，和log4j2一样的风格）
     * @param args      错误参数
     * @throws BizException if the object is not {@code null}
     */
    @Contract(value = "!null, _, _ -> fail", pure = true)
    public static void isNull(@Nullable Object object, final ErrorCode errorCode, final Object... args) {
        if (object != null) {
            throw new BizException(errorCode, args);
        }
    }

    /*-----------------------------------------notNull-----------------------------------------*/

    /**
     * Check that an object is not {@code null} .
     *
     * @param object  the object to check
     * @param message the exception message to use if the validation fails
     * @throws BizException if the object is {@code null}
     */
    @Contract(value = "null, _ -> fail", pure = true)
    public static void notNull(@Nullable Object object, String message) {
        if (object == null) {
            throw new BizException(CommonErrorCode.ILLEGAL_PARAM, message);
        }
    }

    /**
     * Check that an object is not {@code null} .
     *
     * @param object  the object to check
     * @param message the exception message to use if the validation fails
     * @param args    错误参数
     * @throws BizException if the object is {@code null}
     */
    @Contract(value = "null, _, _ -> fail", pure = true)
    public static void notNull(@Nullable Object object, String message, Object... args) {
        if (object == null) {
            throw new BizException(CommonErrorCode.ILLEGAL_PARAM, FormatUtil.buildMessage(message, args));
        }
    }

    /**
     * Check that an object is not {@code null} .
     *
     * @param object    the object to check
     * @param errorCode 错误码
     * @throws BizException if the object is {@code null}
     */
    @Contract(value = "null, _ -> fail", pure = true)
    public static void notNull(@Nullable Object object, ErrorCode errorCode) {
        if (object == null) {
            throw new BizException(errorCode);
        }
    }

    /**
     * Check that an object is not {@code null} .
     *
     * @param object    the object to check
     * @param errorCode 错误码（如果有占位符，请使用{}，和log4j2一样的风格）
     * @param args      错误参数
     * @throws BizException if the object is {@code null}
     */
    @Contract(value = "null, _, _ -> fail", pure = true)
    public static void notNull(@Nullable Object object, ErrorCode errorCode, Object... args) {
        if (object == null) {
            throw new BizException(errorCode, args);
        }
    }

    /*-----------------------------------------notEmpty-----------------------------------------*/

    /**
     * Check that a collection has elements; that is, it must not be {@code null} and must have at least one element.
     *
     * @param collection the collection to check
     * @throws BizException if the collection is {@code null} or has no elements
     */
    @Contract(value = "null -> fail", pure = true)
    public static void notEmpty(@Nullable Collection<?> collection) {
        if (CollectionUtils.isEmpty(collection)) {
            throw new BizException(CommonErrorCode.ILLEGAL_PARAM);
        }
    }

    /**
     * Check that a collection has elements; that is, it must not be {@code null} and must have at least one element.
     *
     * @param collection the collection to check
     * @param message    the exception message to use if the validation fails
     * @throws BizException if the collection is {@code null} or has no elements
     */
    @Contract(value = "null, _ -> fail", pure = true)
    public static void notEmpty(@Nullable Collection<?> collection, final String message) {
        if (CollectionUtils.isEmpty(collection)) {
            throw new BizException(CommonErrorCode.ILLEGAL_PARAM, message);
        }
    }

    /**
     * Check that a collection has elements; that is, it must not be {@code null} and must have at least one element.
     *
     * @param collection the collection to check
     * @param message    the exception message to use if the validation fails
     * @param args       错误参数
     * @throws BizException if the collection is {@code null} or has no elements
     */
    @Contract(value = "null, _, _ -> fail", pure = true)
    public static void notEmpty(@Nullable Collection<?> collection, final String message, final Object... args) {
        if (CollectionUtils.isEmpty(collection)) {
            throw new BizException(CommonErrorCode.ILLEGAL_PARAM, FormatUtil.buildMessage(message, args));
        }
    }

    /**
     * Check that a collection has elements; that is, it must not be {@code null} and must have at least one element.
     *
     * @param collection the collection to check
     * @param errorCode  错误码（如果有占位符，请使用{}，和log4j2一样的风格）
     * @throws BizException if the collection is {@code null} or has no elements
     */
    @Contract(value = "null, _ -> fail", pure = true)
    public static void notEmpty(@Nullable Collection<?> collection, final ErrorCode errorCode) {
        if (CollectionUtils.isEmpty(collection)) {
            throw new BizException(errorCode);
        }
    }

    /**
     * Check that a collection has elements; that is, it must not be {@code null} and must have at least one element.
     *
     * @param collection the collection to check
     * @param errorCode  错误码（如果有占位符，请使用{}，和log4j2一样的风格）
     * @param message    错误信息（也可以作为占位符参数）
     * @throws BizException if the collection is {@code null} or has no elements
     */
    @Contract(value = "null, _, _ -> fail", pure = true)
    public static void notEmpty(@Nullable Collection<?> collection, final ErrorCode errorCode, final String message) {
        if (CollectionUtils.isEmpty(collection)) {
            throw new BizException(errorCode, message);
        }
    }

    /**
     * Check that a collection has elements; that is, it must not be {@code null} and must have at least one element.
     *
     * @param collection the collection to check
     * @param errorCode  错误码
     * @param args       错误参数
     * @throws BizException if the collection is {@code null} or has no elements
     */
    @Contract(value = "null, _, _ -> fail", pure = true)
    public static void notEmpty(@Nullable Collection<?> collection, final ErrorCode errorCode, final Object... args) {
        if (CollectionUtils.isEmpty(collection)) {
            throw new BizException(errorCode, args);
        }
    }

    /**
     * Check that a map has elements; that is, it must not be {@code null} and must have at least one element.
     *
     * @param map     the map to check
     * @param message the exception message to use if the validation fails
     * @throws BizException if the map is {@code null} or has no elements
     */
    @Contract(value = "null, _ -> fail", pure = true)
    public static void notEmpty(@Nullable Map<?, ?> map, String message) {
        if (MapUtils.isEmpty(map)) {
            throw new BizException(CommonErrorCode.ILLEGAL_PARAM, message);
        }
    }

    /**
     * Check that a map has elements; that is, it must not be {@code null} and must have at least one element.
     *
     * @param map     the map to check
     * @param message the exception message to use if the validation fails
     * @param args    错误参数
     * @throws BizException if the map is {@code null} or has no elements
     */
    @Contract(value = "null, _, _ -> fail", pure = true)
    public static void notEmpty(@Nullable Map<?, ?> map, final String message, final Object... args) {
        if (MapUtils.isEmpty(map)) {
            throw new BizException(CommonErrorCode.ILLEGAL_PARAM, FormatUtil.buildMessage(message, args));
        }
    }

    /**
     * Check that a map has elements; that is, it must not be {@code null} and must have at least one element.
     *
     * @param map       the map to check
     * @param errorCode 错误码
     * @throws BizException if the map is {@code null} or has no elements
     */
    @Contract(value = "null, _ -> fail", pure = true)
    public static void notEmpty(@Nullable Map<?, ?> map, final ErrorCode errorCode) {
        if (MapUtils.isEmpty(map)) {
            throw new BizException(errorCode);
        }
    }

    /**
     * Check that a map has elements; that is, it must not be {@code null} and must have at least one element.
     *
     * @param map       the map to check
     * @param errorCode 错误码（如果有占位符，请使用{}，和log4j2一样的风格）
     * @param message   错误信息（也可以作为占位符参数）
     * @throws BizException if the map is {@code null} or has no elements
     */
    @Contract(value = "null, _, _ -> fail", pure = true)
    public static void notEmpty(@Nullable Map<?, ?> map, final ErrorCode errorCode, final String message) {
        if (MapUtils.isEmpty(map)) {
            throw new BizException(errorCode, message);
        }
    }

    /**
     * Check that a map has elements; that is, it must not be {@code null} and must have at least one element.
     *
     * @param map       the map to check
     * @param errorCode 错误码
     * @param args      错误参数
     * @throws BizException if the map is {@code null} or has no elements
     */
    @Contract(value = "null, _, _ -> fail", pure = true)
    public static void notEmpty(@Nullable Map<?, ?> map, final ErrorCode errorCode, final Object... args) {
        if (MapUtils.isEmpty(map)) {
            throw new BizException(errorCode, args);
        }
    }

    /*-----------------------------------------notBlank-----------------------------------------*/

    /**
     * Check that the given String has valid text content; that is, it must not be {@code null} and must contain
     * at least one non-whitespace character.
     *
     * @param text the String to check
     * @throws BizException if the text does not contain valid text content
     */
    @Contract(value = "null -> fail", pure = true)
    public static void notBlank(@Nullable String text) {
        if (StringUtils.isBlank(text)) {
            throw new BizException(CommonErrorCode.ILLEGAL_PARAM);
        }
    }

    /**
     * Check that the given String has valid text content; that is, it must not be {@code null} and must contain
     * at least one non-whitespace character.
     *
     * @param text    the String to check
     * @param message the exception message to use if the validation fails
     * @throws BizException if the text does not contain valid text content
     */
    @Contract(value = "null, _ -> fail", pure = true)
    public static void notBlank(@Nullable String text, final String message) {
        if (StringUtils.isBlank(text)) {
            throw new BizException(CommonErrorCode.ILLEGAL_PARAM, message);
        }
    }

    /**
     * Check that the given String has valid text content; that is, it must not be {@code null} and must contain
     * at least one non-whitespace character.
     *
     * @param text    the String to check
     * @param message the exception message to use if the validation fails
     * @param args    错误参数
     * @throws BizException if the text does not contain valid text content
     */
    @Contract(value = "null, _, _ -> fail", pure = true)
    public static void notBlank(@Nullable String text, final String message, final Object... args) {
        if (StringUtils.isBlank(text)) {
            throw new BizException(CommonErrorCode.ILLEGAL_PARAM, FormatUtil.buildMessage(message, args));
        }
    }

    /**
     * Check that the given String has valid text content; that is, it must not be {@code null} and must contain
     * at least one non-whitespace character.
     *
     * @param text      the String to check
     * @param errorCode 错误码
     * @throws BizException if the text does not contain valid text content
     */
    @Contract(value = "null, _ -> fail", pure = true)
    public static void notBlank(@Nullable String text, final ErrorCode errorCode) {
        if (StringUtils.isBlank(text)) {
            throw new BizException(errorCode);
        }
    }

    /**
     * Check that the given String has valid text content; that is, it must not be {@code null} and must contain
     * at least one non-whitespace character.
     *
     * @param text      the String to check
     * @param errorCode 错误码（如果有占位符，请使用{}，和log4j2一样的风格）
     * @param message   错误信息（也可以作为占位符参数）
     * @throws BizException if the text does not contain valid text content
     */
    @Contract(value = "null, _, _ -> fail", pure = true)
    public static void notBlank(@Nullable String text, final ErrorCode errorCode, final String message) {
        if (StringUtils.isBlank(text)) {
            throw new BizException(errorCode, message);
        }
    }

    /**
     * Check that the given String has valid text content; that is, it must not be {@code null} and must contain
     * at least one non-whitespace character.
     *
     * @param text      the String to check
     * @param errorCode 错误码
     * @param args      错误参数
     * @throws BizException if the text does not contain valid text content
     */
    @Contract(value = "null, _, _ -> fail", pure = true)
    public static void notBlank(@Nullable String text, final ErrorCode errorCode, final Object... args) {
        if (StringUtils.isBlank(text)) {
            throw new BizException(errorCode, args);
        }
    }

    /*-----------------------------------------objectEquals-----------------------------------------*/

    /**
     * Check that two objects are equal.
     *
     * @param expected expected value
     * @param actual   actual value
     * @param message  the exception message to use if the validation fails
     * @throws BizException if the two objects are not equal.
     */
    public static void objectEquals(final Object expected, final Object actual, final String message) {
        if (!Objects.equals(expected, actual)) {
            throw new BizException(CommonErrorCode.ILLEGAL_PARAM, message);
        }
    }

    /**
     * Check that two objects are equal.
     *
     * @param expected expected value
     * @param actual   actual value
     * @param message  the exception message to use if the validation fails
     * @param args     错误参数
     * @throws BizException if the two objects are not equal.
     */
    public static void objectEquals(final Object expected, final Object actual, final String message,
                                    final Object... args) {
        if (!Objects.equals(expected, actual)) {
            throw new BizException(CommonErrorCode.ILLEGAL_PARAM, FormatUtil.buildMessage(message, args));
        }
    }

    /**
     * Check that two objects are equal.
     *
     * @param expected  expected value
     * @param actual    actual value
     * @param errorCode 错误码
     * @throws BizException if the two objects are not equal.
     */
    public static void objectEquals(final Object expected, final Object actual, final ErrorCode errorCode) {
        if (!Objects.equals(expected, actual)) {
            throw new BizException(errorCode);
        }
    }

    /**
     * Check that two objects are equal.
     *
     * @param expected  expected value
     * @param actual    actual value
     * @param errorCode 错误码（如果有占位符，请使用{}，和log4j2一样的风格）
     * @param message   错误信息（也可以作为占位符参数）
     * @throws BizException if the two objects are not equal.
     */
    public static void objectEquals(final Object expected, final Object actual, final ErrorCode errorCode,
                                    final String message) {
        if (!Objects.equals(expected, actual)) {
            throw new BizException(errorCode, message);
        }
    }

    /**
     * Check that two objects are equal.
     *
     * @param expected  expected value
     * @param actual    actual value
     * @param errorCode 错误码
     * @param args      错误参数
     * @throws BizException if the two objects are not equal.
     */
    public static void objectEquals(final Object expected, final Object actual, final ErrorCode errorCode,
                                    final Object... args) {
        if (!Objects.equals(expected, actual)) {
            throw new BizException(errorCode, args);
        }
    }
}
