package org.zeroagent.common.repository;


import org.jetbrains.annotations.NotNull;
import org.zeroagent.common.page.Page;
import org.zeroagent.common.page.PageBuilder;
import org.zeroagent.common.page.Pageable;
import org.zeroagent.common.problem.error.CommonErrorCode;
import org.zeroagent.common.problem.exception.SysException;

import java.util.List;
import java.util.Optional;

public interface SingleRepository<ENTITY, QUERY> {
    /**
     * 新增记录
     * @param entity 领域对象模型
     * @return 查询条件模型
     */
    long create(ENTITY entity);

    /**
     *  按主键物理删除
     * @param id 主键ID
     */
    void deleteById(long id);

    /**
     *  按主键更新
     * @param entity 待更新数据
     */
    void updateById(ENTITY entity);

    /**
     * 按主键ID查询对应记录
     *
     * @param id 主键ID
     * @return 一条记录
     */
    @NotNull
    default ENTITY queryById(long id) {
        return this.queryOptionalById(id).orElseThrow(() -> new SysException(CommonErrorCode.RECORD_NOT_FOUND));
    }

    /**
     * 按主键ID查询对应记录
     *
     * @param id 主键ID
     * @return 一条记录
     */
    Optional<ENTITY> queryOptionalById(long id);

    /**
     * 按条件查询分页记录列表
     *
     * @param qo       查询条件
     * @param pageable 条件分页
     * @return 分页记录列表
     */
    default Page<ENTITY> queryPage(QUERY qo, Pageable pageable) {
        int count = this.count(qo);
        List<ENTITY> records = this.query(qo, pageable);
        return Page.of(records, pageable, count);
    }

    /**
     * 按条件查询记录列表
     *
     * @param qo       查询条件
     * @param pageable 条件分页
     * @return 列表记录
     */
    List<ENTITY> query(QUERY qo, Pageable pageable);

    /**
     * 按条件统计总数
     *
     * @param qo 查询条件
     * @return 总数量
     */
    int count(QUERY qo);

    /**
     * 按条件判断记录是否存在
     *
     * @param qo 查询条件
     * @return 是否存在
     */
    default boolean exists(QUERY qo) {
        Pageable pageable = PageBuilder.firstPage().size(1).build();
        return this.query(qo, pageable).size() == 1;
    }



}
