package org.zeroagent.common.utils.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsonorg.JsonOrgModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.zeroagent.common.utils.Asserts;

import java.io.IOException;
import java.util.*;

import static org.apache.commons.collections4.MapUtils.isEmpty;

/**
 * JSON常用操作工具类
 */
@UtilityClass
public class JSON {
    private final Object lock = new Object();

    private static volatile ObjectMapper objectMapper;

    static {
        objectMapper = new ObjectMapper();
        init(objectMapper);
    }
    private static void init(ObjectMapper objectMapper) {
        List<Module> modules = new ArrayList<>();

        // Java8, see: https://github.com/FasterXML/jackson-modules-java8
        // Jackson module that adds support for accessing parameter names; a feature added in JDK 8.
        modules.add(new ParameterNamesModule(JsonCreator.Mode.DEFAULT));
        modules.add(new JavaTimeModule());
        modules.add(new Jdk8Module());

        // Third part
        modules.add(new JsonOrgModule());

        objectMapper.registerModules(modules);

        // configure DeserializationFeature and SerializationFeature
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_ABSENT);

        objectMapper.setTimeZone(TimeZone.getDefault());

        // Default Features
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        objectMapper.disable(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS);
        objectMapper.disable(DeserializationFeature.ACCEPT_FLOAT_AS_INT);

        objectMapper.disable(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS);
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.disable(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS);
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    }

    /**
     * 重设objectMapper对象
     *
     * @param objectMapper 序列化核心对象
     */
    public static void reloadObjectMapper(ObjectMapper objectMapper) {
        if (objectMapper == null) {
            return;
        }
        synchronized (lock) {
            JSON.objectMapper = objectMapper;
        }
    }

    /**
     * Accessor for getting currently configured {@link TypeFactory} instance.
     */
    public TypeFactory getTypeFactory() {
        return objectMapper.getTypeFactory();
    }

    /*----------------------- deserialization -----------------------*/

    /**
     * 反序列化为JSONObject对象
     *
     * @param json JSON字符串
     * @return JSONObject对象
     */
    @NotNull
    public JSONObject parseJSONObject(String json) {
        if (json == null || json.isEmpty()) {
            return new JSONObject();
        }
        return parseObject(json, JSONObject.class);
    }

    @NotNull
    public static <T> T parseObject(String json, Class<T> clazz) {
        Asserts.notEmpty(json, "json string cannot be empty");
        try {
            return objectMapper.readValue(json, clazz);
        } catch (IOException e) {
            throw new JSONException(e.getMessage(), e);
        }
    }

    @NotNull
    public static <T> T parseObject(byte[] jsonBytes, Class<T> clazz) {
        Asserts.notNull(jsonBytes, "json bytes cannot be empty");
        try {
            return objectMapper.readValue(jsonBytes, clazz);
        } catch (IOException e) {
            throw new JSONException(e.getMessage(), e);
        }
    }

    @NotNull
    public static <T> T parseObject(String json, JavaType type) {
        Asserts.notEmpty(json, "json string cannot be empty");
        try {
            return objectMapper.readValue(json, type);
        } catch (IOException e) {
            throw new JSONException(e.getMessage(), e);
        }
    }

    @NotNull
    public static <T> T parseObject(String json, TypeReference<T> type) {
        Asserts.notEmpty(json, "json string cannot be empty");
        try {
            return objectMapper.readValue(json, type);
        } catch (IOException e) {
            throw new JSONException(e.getMessage(), e);
        }
    }

    public static <T> Optional<T> parseOptional(String json, Class<T> clazz) {
        if (json == null || json.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(parseObject(json, clazz));
    }

    public static <T> Optional<T> parseOptional(String json, JavaType type) {
        if (json == null || json.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(parseObject(json, type));
    }

    public static <T> Optional<T> parseOptional(String json, TypeReference<T> type) {
        if (json == null || json.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(parseObject(json, type));
    }

    /**
     * 反序列化为集合
     *
     * @param json        JSON字符串
     * @param elementType 元素类型
     * @param <E>         元素类型
     * @return 集合
     */
    public static <E> List<E> parseList(String json, Class<E> elementType) {
        if (json == null || json.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, getTypeFactory().constructCollectionType(List.class, elementType));
        } catch (IOException e) {
            throw new JSONException("[parseList]error", e);
        }
    }

    /**
     * 反序列化为Map对象（无序）
     *
     * @param json JSON字符串
     * @return Map对象
     */
    public static Map<String, Object> parseMap(String json) {
        return parseMap(json, Object.class);
    }

    /**
     * 反序列化为Map对象（无序）
     *
     * @param json      JSON字符串
     * @param valueType value类型
     * @param <V>       value类型
     * @return Map对象
     */
    public static <V> Map<String, V> parseMap(String json, Class<V> valueType) {
        if (json == null || json.isEmpty()) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(json, getTypeFactory().constructMapType(HashMap.class, String.class, valueType));
        } catch (IOException e) {
            throw new JSONException("[parseMap]error", e);
        }
    }

    /**
     * 反序列化为Map对象（有序）
     *
     * @param json JSON字符串
     * @return Map对象
     */
    public static Map<String, Object> parseLinkedHashMap(String json) {
        return parseMap(json, Object.class);
    }

    /**
     * 反序列化为Map对象（有序）
     *
     * @param json      JSON字符串
     * @param valueType value类型
     * @param <V>       value类型
     * @return Map对象
     */
    public static <V> LinkedHashMap<String, V> parseLinkedHashMap(String json, Class<V> valueType) {
        if (json == null || json.isEmpty()) {
            return new LinkedHashMap<>();
        }
        try {
            return objectMapper.readValue(json, getTypeFactory().constructMapType(LinkedHashMap.class, String.class, valueType));
        } catch (IOException e) {
            throw new JSONException("[parseMap]error", e);
        }
    }

    @NotNull
    public static JsonNode parseJsonNode(String json) {
        Asserts.notEmpty(json, "json string cannot be empty");
        try {
            return objectMapper.readTree(json);
        } catch (IOException e) {
            throw new JSONException(e.getMessage(), e);
        }
    }

    /*----------------------- serialization -----------------------*/

    /**
     * 序列化为JSON字符串
     *
     * @param object 对象
     * @return JSON字符串
     */
    public static String toJSONString(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (IOException e) {
            throw new JSONException(e.getMessage(), e);
        }
    }

    public static String toPrettyJSONString(Object object) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
        } catch (IOException e) {
            throw new JSONException(e.getMessage(), e);
        }
    }

    /**
     * 序列化为字节数组
     *
     * @param object 对象
     * @return 字节数组
     */
    public static byte[] toJSONBytes(Object object) {
        try {
            return objectMapper.writeValueAsBytes(object);
        } catch (IOException e) {
            throw new JSONException(e.getMessage(), e);
        }
    }

    /*-----------------------convert-----------------------*/

    public static Map<String, Object> toMap(Object object) {
        Asserts.notNull(object, "object cannot be null");
        Map<String, Object> params = objectMapper.convertValue(object, getTypeFactory().constructMapType(HashMap.class, String.class, Object.class));
        return isEmpty(params) ? new HashMap<>() : params;
    }

    public static JSONObject toJSONObject(Object object) {
        return objectMapper.convertValue(object, JSONObject.class);
    }

    public static JsonNode toJsonNode(Object object) {
        Asserts.notNull(object, "object cannot be null");
        return objectMapper.valueToTree(object);
    }

    public static <T> T toObject(Map<String, ?> map, Class<T> type) {
        Asserts.notNull(map, "map cannot be null");
        return objectMapper.convertValue(map, type);
    }
}
