package org.zeroagent.domain.core.card.model;


import lombok.Getter;

/**
 *  卡牌子种类
 *  List<CardSubTypeEnum>: 可以同时拥有多个
 */
@Getter
public enum CardSubTypeEnum {


    MAGIC_NORMAL("通常魔法"),
    MAGIC_CONTINUOUS("永续魔法"),
    MAGIC_FIELD("场地魔法"),
    MAGIC_EQUIP("装备魔法"),
    MAGIC_QUICKPLAY("速攻魔法"),
    MAGIC_RITUAL("仪式魔法"),


    TRAP_COUNTER("反击陷阱"),
    TRAP_NORMAL("通常陷阱"),
    TRAP_CONTINUOUS("永续陷阱"),


    MONSTER_NORMAL("通常怪兽"),
    MONSTER_EFFECT("效果怪兽"),
    MONSTER_TUNER("调整怪兽"),
    MONSTER_FUSION("融合怪兽"),
    MONSTER_SYNCHRO("同调怪兽"),
    MONSTER_XYZ("超量怪兽"),
    MONSTER_PENDULUM("灵摆怪兽"),
    MONSTER_LINK("链接怪兽"),
    MONSTER_TRAP("陷阱怪兽"),
    MONSTER_SPIRIT("灵魂怪兽"),
    MONSTER_UNION("同盟怪兽"),
    MONSTER_GEMINI("二重怪兽"),
    MONSTER_TOKEN("衍生物"),
    MONSTER_FLIP("反转怪兽"),
    MONSTER_TOON("卡通怪兽"),
    MONSTER_SPECIAL_SUMMON("特殊召唤限制"),
    MONSTER_RITUAL("仪式怪兽")


    ;


    private final String name;
    CardSubTypeEnum(String name) {
        this.name = name;
    }
}
