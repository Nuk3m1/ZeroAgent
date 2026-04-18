package org.zeroagent.domain.core.card.model;


import lombok.Getter;

/**
 *  卡牌主分类
 *  三种类型互斥
 */
@Getter
public enum CardTypeEnum {
    MONSTER("怪兽卡"),
    MAGIC("魔法卡"),
    TRAP("陷阱卡")
    ;
    private final String desc;

    CardTypeEnum(String desc) {
        this.desc = desc;
    }


}
