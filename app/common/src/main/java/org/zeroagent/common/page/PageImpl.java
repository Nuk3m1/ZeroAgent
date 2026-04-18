package org.zeroagent.common.page;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 *
 *  标准分页结果
 * @author Nuk3m1
 * @version 2026年03月08日  16时11分
 */
@Getter
@ToString
@EqualsAndHashCode
class PageImpl<T> implements Page<T> {

    /**
     * 结果集
     */
    private final List<T> content = new ArrayList<>();
    /**
     * 总记录数
     */
    private final Long total;
    /**
     * 分页条件（与该分页结果对应）
     */
    private final Pageable current;

    /**
     * 构造方法
     *
     * @param content  结果集
     * @param pageable 分页条件
     */
    public PageImpl(final List<T> content, final Pageable pageable) {
        this(content, pageable, null);
    }

    /**
     * 构造方法
     *
     * @param content  结果集
     * @param pageable 分页条件
     * @param total    总记录数
     */
    public PageImpl(final List<T> content, final Pageable pageable, final Long total) {
        if (content == null) {
            throw new IllegalArgumentException("content must not be null!");
        }
        if (pageable == null) {
            throw new IllegalArgumentException("pageable must not be null!");
        }
        if (pageable.needTotal()) {
            if (total == null) {
                throw new IllegalArgumentException("total must not be null!");
            }
            if (pageable.isFixEdge()) {
                this.current = PageUtil.fixEdge(pageable, total);
            } else {
                this.current = pageable;
            }
            this.total = total;
        } else {
            this.current = pageable;
            this.total = null;
        }
        this.content.addAll(content);
    }

    /**
     * @see Page#current()
     */
    @NotNull
    @Override
    public Pageable current() {
        return current;
    }

    /**
     * @see Page#map(Function)
     */
    @NotNull
    @Override
    public <S> Page<S> map(Function<? super T, ? extends S> mapper) {
        List<S> result = new ArrayList<>(content.size());
        for (T element : this) {
            result.add(mapper.apply(element));
        }
        return new PageImpl<>(result, current, total);
    }

    /**
     * @see Page#mapAll(Function)
     */
    @NotNull
    @Override
    public <S> Page<S> mapAll(Function<List<T>, List<S>> mapper) {
        List<S> result = mapper.apply(content);
        return new PageImpl<>(result, current, total);
    }
}

