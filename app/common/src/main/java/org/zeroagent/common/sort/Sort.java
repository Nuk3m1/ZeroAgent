package org.zeroagent.common.sort;

import jodd.util.StringPool;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.zeroagent.common.utils.function.Filters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Iterator;

import static java.util.stream.Collectors.toList;

/**
 *  排序参数
 * @author Nuk3m1
 * @version 2026年03月08日  15时40分
 */
@EqualsAndHashCode
@ToString
public class Sort implements Iterable<Order>{
    /**
     * 排序规则集合
     */
    private final List<Order> orders;
    /**
     * 构造方法
     */
    public Sort(Order... orders) {
        this (orders == null ? new ArrayList<>() : Arrays.asList(orders));
    }
    /**
     * 构造方法
     */
    public Sort(Direction direction, String... properties) {
        this(direction, properties == null ? new ArrayList<>() : Arrays.asList(properties));
    }
    /**
     * 构造方法
     */
    public Sort(Direction direction, List<String> properties) {
        this(buildOrders(direction, properties));
    }

    /**
     * Creates a new {@link Sort} instance.
     *
     * @param orders 多个排序规则
     */
    private Sort(List<Order> orders) {
        this.orders = purify(orders);
    }

    /**
     * Returns a new {@link Sort} appending new properties of one direction.
     */
    public Sort and(Direction direction, String... properties) {
        Sort sort = new Sort(direction, properties);
        return this.and(sort);
    }
    /**
     * Returns a new {@link Sort} consisting of the {@link Order}s of the current {@link Sort} combined with the given
     * ones.
     */
    public Sort and(@Nullable Sort sort) {
        if (sort == null) {
            return this;
        }
        ArrayList<Order> these = new ArrayList<>(this.orders);
        for (Order order : sort) {
            these.add(order);
        }
        return new Sort(these);
    }

    @Override
    @NotNull
    public Iterator<Order> iterator() {
        return this.orders.iterator();
    }
    public String toIdentity() {
        StringBuilder sb = new StringBuilder("[");
        this.orders.stream()
                .map(Order::toIdentity)
                .reduce((identity1, identity2) -> identity1 + StringPool.COMMA + identity2)
                .ifPresent(sb::append);
        sb.append("]");
        return sb.toString();
    }
    /*-------------------------------私有方法-------------------------------*/

    /**
     * 清理错误的排序规则，比如a,asc&&a,desc，只保留a,asc
     *
     * @param orders 排序规则集合
     * @return 清理后的排序规则集合
     */
    private List<Order> purify(List<Order> orders) {
        orders = orders.stream().filter(Filters.distinctByKey(Order::getProperty)).collect(toList());
        if (orders.isEmpty()) {
            throw new IllegalArgumentException("You have to provide at least one property to sort by!");
        }
        return orders;
    }

    /**
     * 构造排序规则集合
     *
     * @param direction  排序方向
     * @param properties 排序属性
     * @return 排序规则集合
     */
    private static List<Order> buildOrders(Direction direction, List<String> properties) {
        if (properties == null || properties.isEmpty()) {
            throw new IllegalArgumentException("You have to provide at least one property to sort by!");
        }
        List<Order> orders = new ArrayList<>(properties.size());
        for (String property : properties) {
            orders.add(new Order(direction, property));
        }
        return orders;
    }

}
