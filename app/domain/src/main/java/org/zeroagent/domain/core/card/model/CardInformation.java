package org.zeroagent.domain.core.card.model;


import lombok.Data;
import lombok.experimental.Accessors;
import org.json.JSONObject;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * 卡牌信息 - 领域实体类
 * @author Nuk3m1
 * @version 2026年03月17日  19时54分
 */
@Data
@Accessors(chain = true)
public class CardInformation {
    /**
     * 主键ID
     */
    private Long                    id;
    /**
     * 创建时间
     */
    private ZonedDateTime           createdAt;
    /**
     * 更新时间
     */
    private ZonedDateTime           updatedAt;
    /**
     * 卡密
     */
    private String                  passcode;
    /**
     * 卡名
     */
    private String                  name;
    /**
     * 属性
     */
    private String                  attribution;
    /**
     * 种族
     */
    private String                  race;
    /**
     * 攻击力
     */
    private Integer                 atk;
    /**
     * 防御力
     */
    private Integer                 def;
    /**
     * 主种类
     */
    private CardTypeEnum            cardType;
    /**
     * 子种类数组
     */
    private List<CardSubTypeEnum>   cardSubtype;
    /**
     * 星级
     */
    private Integer                 monsterLevel;
    /**
     * 阶级
     */
    private Integer                 monsterRank;
    /**
     * 连接等级
     */
    private Integer                 linkRating;
    /**
     * 灵摆刻度
     */
    private Integer                 pendulumScale;
    /**
     * 灵摆效果
     */
    private String                  pendulumEffect;
    /**
     * 怪兽效果
     */
    private String                  effect;
    /**
     * 图谱状态
     */
    private CardInformationStatusEnum graphSyncStatus;
    /**
     * 原生API响应
     */
    private JSONObject               bizResponse;
    /**
     * 所属字段
     */
    private List<String>             archetype;
}
