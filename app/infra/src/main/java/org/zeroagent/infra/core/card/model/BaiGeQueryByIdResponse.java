package org.zeroagent.infra.core.card.model;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 *
 * @author Nuk3m1
 * @version 2026年03月20日  14时33分
 */
@Data
@Accessors(chain = true)
public class BaiGeQueryByIdResponse {
    private long id;
    private BaiGeQueryByIdResponseData data;
    private BaiGeQueryByIdResponseText text;
    @Data
    @Accessors(chain = true)
    public static class BaiGeQueryByIdResponseData {
        /**
         * 发行环境
         */
        private int ot;
        /**
         * 所属字段
         */
        private long setcode;
        /**
         * 卡牌类型
         */
        private long type;
        /**
         * 攻击力
         */
        private int atk;
        /**
         * 防御力
         */
        private int def;
        /**
         * 星级/阶级/link值/灵摆刻度
         */
        private long level;
        /**
         * 种族
         */
        private long race;
        /**
         * 种族
         */
        private long attribute;
    }
    @Data
    @Accessors(chain = true)
    public static class BaiGeQueryByIdResponseText {
        /**
         * 卡名
         */
        private String name;
        /**
         * 卡面小字 如“[怪兽|效果|灵摆] 龙/暗\n[★7] 2500/2000  4/4”
         */
        private String types;
        /**
         * 效果
         */
        private String desc;
    }
}
