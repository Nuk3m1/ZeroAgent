package org.zeroagent.domain.core.card.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 *
 * @author Nuk3m1
 * @version 2026年04月22日  14时03分
 */
@Data
@Accessors(chain = true)
public class CardInformationQO {
    /**
     * 字段
     */
    private List<String> archetypes;


    /**
     * 主类型
     */
    private String mainType;
    /**
     * 子类型
     */
    private List<String> subTypes;


    /**
     * 属性
     */
    private String attribute;
    /**
     * 种族
     */
    private String race;

    /**
     * 星级
     */
    private Integer level;
    /**
     * 星级操作符
     */
    private String levelOperator;

    /**
     * 攻击力
     */
    private Integer atk;
    /**
     * 攻击力操作符
     */
    private String atkOperator;

    /**
     * 防御力
     */
    private Integer def;
    /**
     * 防御力操作符
     */
    private String defOperator;

}
