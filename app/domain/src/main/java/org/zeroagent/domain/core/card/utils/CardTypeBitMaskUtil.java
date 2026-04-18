package org.zeroagent.domain.core.card.utils;

import lombok.experimental.UtilityClass;
import org.zeroagent.domain.core.card.model.CardSubTypeEnum;
import org.zeroagent.domain.core.card.model.CardTypeEnum;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Map.entry;

/**
 *
 * @author Nuk3m1
 * @version 2026年03月18日  23时44分
 */
@UtilityClass
public class CardTypeBitMaskUtil {
    /**
     * 种族位表
     */
    private static final Map<Long, String> RACE_MASK_MAP = Map.ofEntries(
            entry(1L, "战士族"),         entry(2L, "魔法师族"),        entry(4L, "天使族"),        entry(8L, "恶魔族"),
            entry(16L, "不死族"),        entry(32L, "机械族"),        entry(64L, "水族"),         entry(128L, "炎族"),
            entry(256L, "岩石族"),       entry(512L, "鸟兽族"),       entry(1024L, "植物族"),     entry(2048L, "昆虫族"),
            entry(4096L, "雷族"),        entry(8192L, "龙族"),       entry(16384L, "兽族"),      entry(32768L, "兽战士族"),
            entry(65536L, "恐龙族"),     entry(131072L, "鱼族"),     entry(262144L, "海龙族"),    entry(524288L, "爬虫类族"),
            entry(1048576L, "念动力族"), entry(2097152L, "幻神兽族"), entry(4194304L, "创造神族"), entry(8388608L, "幻龙族"),
            entry(16777216L, "电子界族"),entry(33554432L, "幻想魔族")
    );
    /**
     * 属性位表
     */
    private static final Map<Long, String> ATTRIBUTE_MASK_MAP = Map.ofEntries(
            entry(1L, "地"), entry(2L, "水"), entry(4L, "炎"), entry(8L, "风"), entry(16L, "光"), entry(32L, "暗"), entry(64L, "神")
    );


    /**
     * 1. 解析主分类 (CardTypeEnum)
     * 规则：怪兽、魔法、陷阱是互斥的基石。
     */
    public static CardTypeEnum parseMainType(long typeBitmask) {
        if ((typeBitmask & 1L) != 0) {
            return CardTypeEnum.MONSTER;
        } else if ((typeBitmask & 2L) != 0) {
            return CardTypeEnum.MAGIC;
        } else if ((typeBitmask & 4L) != 0) {
            return CardTypeEnum.TRAP;
        }
        throw new IllegalArgumentException("未知的卡牌主分类，掩码: " + typeBitmask);
    }

    /**
     * 2. 解析子分类数组 (List<CardSubTypeEnum>)
     * 核心逻辑：根据主分类的不同，对共享掩码做不同方向的映射。
     */
    public static List<CardSubTypeEnum> parseSubTypes(long typeBitmask, CardTypeEnum mainType) {
        List<CardSubTypeEnum> subTypes = new ArrayList<>();

        switch (mainType) {
            case MONSTER:
                if ((typeBitmask & 16L) != 0) subTypes.add(CardSubTypeEnum.MONSTER_NORMAL); // 0x16 通常
                if ((typeBitmask & 32L) != 0) subTypes.add(CardSubTypeEnum.MONSTER_EFFECT); // 0x20 效果
                if ((typeBitmask & 64L) != 0) subTypes.add(CardSubTypeEnum.MONSTER_FUSION); // 0x40 融合
                if ((typeBitmask & 128L) != 0) subTypes.add(CardSubTypeEnum.MONSTER_RITUAL);// 0x80 仪式
                if ((typeBitmask & 256L) != 0) subTypes.add(CardSubTypeEnum.MONSTER_TRAP);  // 0x100 陷阱
                if ((typeBitmask & 512L) != 0) subTypes.add(CardSubTypeEnum.MONSTER_SPIRIT);// 0x200 灵魂
                if ((typeBitmask & 1024L) != 0) subTypes.add(CardSubTypeEnum.MONSTER_UNION);// 0x400 同盟
                if ((typeBitmask & 2048L) != 0) subTypes.add(CardSubTypeEnum.MONSTER_GEMINI);// 0x800 二重
                if ((typeBitmask & 4096L) != 0) subTypes.add(CardSubTypeEnum.MONSTER_TUNER);// 0x1000 调整
                if ((typeBitmask & 8192L) != 0) subTypes.add(CardSubTypeEnum.MONSTER_SYNCHRO);// 0x2000 同调
                if ((typeBitmask & 16384L) != 0) subTypes.add(CardSubTypeEnum.MONSTER_TOKEN);// 0x4000 衍生物
                if ((typeBitmask & 2097152L) != 0) subTypes.add(CardSubTypeEnum.MONSTER_FLIP);// 0x200000 反转
                if ((typeBitmask & 4194304L) != 0) subTypes.add(CardSubTypeEnum.MONSTER_TOON);// 0x400000 卡通
                if ((typeBitmask & 8388608L) != 0) subTypes.add(CardSubTypeEnum.MONSTER_XYZ);// 0x800000 超量
                if ((typeBitmask & 16777216L) != 0) subTypes.add(CardSubTypeEnum.MONSTER_PENDULUM);// 0x1000000 灵摆
                if ((typeBitmask & 33554432L) != 0) subTypes.add(CardSubTypeEnum.MONSTER_SPECIAL_SUMMON);// 0x2000000 特招限制
                if ((typeBitmask & 67108864L) != 0) subTypes.add(CardSubTypeEnum.MONSTER_LINK);// 0x4000000 连接
                break;

            case MAGIC:
                if ((typeBitmask & 128L) != 0) subTypes.add(CardSubTypeEnum.MAGIC_RITUAL); // 0x80 仪式
                if ((typeBitmask & 65536L) != 0) subTypes.add(CardSubTypeEnum.MAGIC_QUICKPLAY);// 0x10000 速攻
                if ((typeBitmask & 131072L) != 0) subTypes.add(CardSubTypeEnum.MAGIC_CONTINUOUS);// 0x20000 永续
                if ((typeBitmask & 262144L) != 0) subTypes.add(CardSubTypeEnum.MAGIC_EQUIP);// 0x40000 装备
                if ((typeBitmask & 524288L) != 0) subTypes.add(CardSubTypeEnum.MAGIC_FIELD);// 0x80000 场地

                // 无任何子类 则为通常魔法
                if (subTypes.isEmpty()) {
                    subTypes.add(CardSubTypeEnum.MAGIC_NORMAL);
                }
                break;

            case TRAP:
                if ((typeBitmask & 131072L) != 0) subTypes.add(CardSubTypeEnum.TRAP_CONTINUOUS);// 0x20000 永续
                if ((typeBitmask & 1048576L) != 0) subTypes.add(CardSubTypeEnum.TRAP_COUNTER);// 0x100000 反击

                // 无任何子类 则为通常陷阱
                if (subTypes.isEmpty()) {
                    subTypes.add(CardSubTypeEnum.TRAP_NORMAL);
                }
                break;
        }

        return subTypes;
    }
    public static String parseRace(long race) {
        if (race == 0) {
            return null;
        }
        return RACE_MASK_MAP.get(race);
    }
    public static String parseAttribute(long attribute) {
        if (attribute == 0) {
            return null;
        }
        return ATTRIBUTE_MASK_MAP.get(attribute);
    }

    public static void main(String[] args) {
        CardTypeEnum mainType = parseMainType(17);
        List<CardSubTypeEnum> subTypeEnums = parseSubTypes(17, mainType);
        System.out.println(subTypeEnums);

    }
}
