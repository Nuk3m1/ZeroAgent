package org.zeroagent.common.utils;

import lombok.experimental.UtilityClass;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.ParameterizedMessageFactory;


@UtilityClass
public class FormatUtil {
    private static final char DELIM_START = '{';
    private static final char DELIM_STOP  = '}';
    private static final char ESCAPE_CHAR = '\\';
    /**
     * 格式化字符串
     *
     * @param message The message pattern.
     * @param args    The message parameters.
     * @return The message Object.
     */
    public Message buildMessage(String message, Object... args) {
        if (args == null || args.length == 0) {
            return ParameterizedMessageFactory.INSTANCE.newMessage(message);
        }
        return ParameterizedMessageFactory.INSTANCE.newMessage(message, args);
    }
    /**
     * 格式化字符串
     *
     * @param message The message pattern.
     * @param args    The message parameters.
     * @return The message String.
     */
    public String format(String message, Object... args) {
        if (args == null || args.length == 0) {
            return message;
        }
        return ParameterizedMessageFactory.INSTANCE.newMessage(message, args).getFormattedMessage();
    }

    public static int countArgumentPlaceholders(final String messagePattern) {
        if (messagePattern == null) {
            return 0;
        }
        final int length = messagePattern.length();
        int result = 0;
        boolean isEscaped = false;
        for (int i = 0; i < length - 1; i++) {
            final char curChar = messagePattern.charAt(i);
            if (curChar == ESCAPE_CHAR) {
                isEscaped = !isEscaped;
            } else if (curChar == DELIM_START) {
                if (!isEscaped && messagePattern.charAt(i + 1) == DELIM_STOP) {
                    result++;
                    i++;
                }
                isEscaped = false;
            } else {
                isEscaped = false;
            }
        }
        return result;
    }
}
