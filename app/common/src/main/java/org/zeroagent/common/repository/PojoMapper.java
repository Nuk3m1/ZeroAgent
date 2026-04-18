package org.zeroagent.common.repository;

import java.util.ArrayList;
import java.util.List;

public interface PojoMapper<MODEL, ENTITY> {
    /**
     *  领域模型对象 转换位 数据库模型对象
     * @param model 领域模型对象
     * @return 数据库模型对象
     */
    ENTITY toEntity(MODEL model);

    /**
     * 领域模型对象转换为数据库模型对象
     *
     * @param models 领域模型对象
     * @return 数据库模型对象
     */
    default List<ENTITY> toEntities(List<MODEL> models) {
        if (models == null) {
            return null;
        }

        List<ENTITY> list = new ArrayList<>(models.size());
        for (MODEL model : models) {
            list.add(toEntity(model));
        }

        return list;
    }

    /**
     * 数据库模型对象转换为领域模型对象
     *
     * @param entity 数据库模型对象
     * @return 领域模型对象
     */
    MODEL toModel(ENTITY entity);

    /**
     * 数据库模型对象转换为领域模型对象
     *
     * @param entities 数据库模型对象
     * @return 领域模型对象
     */
    default List<MODEL> toModels(List<ENTITY> entities) {
        if (entities == null) {
            return null;
        }

        List<MODEL> list = new ArrayList<>(entities.size());
        for (ENTITY entity : entities) {
            list.add(toModel(entity));
        }

        return list;
    }



}
