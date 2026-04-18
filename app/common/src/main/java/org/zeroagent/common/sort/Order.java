package org.zeroagent.common.sort;

import jodd.util.StringPool;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

/**
 *
 *
 * @author Nuk3m1
 * @version 2026年03月08日  15时41分
 */
@Data
public class Order {
    /**
     * 默认排序方向
     */
    public static final Direction DEFAULT_DIRECTION = Direction.ASC;
    /**
     *  排序方向
     */
    private final Direction direction;
    /**
     *  排序属性
     */
    private final String property;
    public Order(String property) {
        this(null, property);
    }
    public Order(@Nullable Direction direction, String property) {
        if (StringUtils.isBlank(property)) {
            throw new IllegalArgumentException("property must not null or empty");
        }
        this.direction = direction;
        this.property = property;
    }
    public String toIdentity() {
        return property + StringPool.COMMA + direction;
    }
}
