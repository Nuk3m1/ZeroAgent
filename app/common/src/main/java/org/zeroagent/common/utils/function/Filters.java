package org.zeroagent.common.utils.function;

import lombok.experimental.UtilityClass;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.lang.Boolean.TRUE;

/**
 * @author joton
 * @version Filters.java, v 0.1 2023-01-07 14:54 joton
 */
@UtilityClass
public class Filters {
    /**
     * 使用方法：
     * <pre>
     *     List<Order> orders = new ArrayList<>();
     *     orders.add(...);
     *     ...
     *     orders.stream().filter(distinctByKey(Order::getOrderNo)).collect(toList());
     * </pre>
     */
    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keySelector) {
        Map<Object, Boolean> seen = new HashMap<>();
        return t -> seen.putIfAbsent(keySelector.apply(t), TRUE) == null;
    }
}