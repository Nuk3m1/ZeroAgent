package org.zeroagent.infra.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.UtilityClass;

/**
 * @author Nuk3m1
 * @version 2026年03月07日  22时04分
 * @Description:
 */
@UtilityClass
public class DebugUtil {
    public static void printJsonObject(ObjectMapper objectMapper, Object object) {
        try {
            System.out.println(objectMapper.writeValueAsString(object));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
