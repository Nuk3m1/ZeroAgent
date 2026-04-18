package org.zeroagent.domain.core.cardgraph.model;

import lombok.Data;

import java.util.List;

/**
 * 卡片图节点 - 领域实体类
 * @author Nuk3m1
 * @version 2026年03月18日  16时03分
 */
@Data
public class CardTopology {
    /**
     * 卡密 - 主键ID
     */
    private String                passcode;
    /**
     * 卡名
     */
    private String                name;
    /**
     * 效果
     */
    private String                effect;
    /**
     * 种族
     */
    private String                race;
    /**
     * 属性
     */
    private String                attribute;
    /**
     * 所属字段
     */
    private List<String>          archetypes;
    /**
     * 检索目标
     */
    private List<String>          searchTargets;
    /**
     * 素材/被素材 目标
     */
    private List<String>          materialTargets;


}
