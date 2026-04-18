package org.zeroagent.infra.core.card.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 *
 * @author Nuk3m1
 * @version 2026年03月20日  11时55分
 */
@Data
@Accessors(chain = true)
public class BaiGeNodeResponse {
    /**
     * 请求中的节点ID
     */
    private long cid;
    /**
     * 卡密
     */
    private long id;
    /**
     * 中文名
     */
    @JsonProperty("cn_name")
    private String cnName;
    /**
     * 日文名
     */
    @JsonProperty("jp_name")
    private String jpName;

    private BaiGeNodeResponseText text;

    private BaiGeNodeResponseData data;
    @Data
    @Accessors(chain = true)
    public static class BaiGeNodeResponseText {
        /**
         * 小字 如"[怪兽|通常] 龙/光\n[★8] 3000/2500"
         */
        private String types;
        /**
         * 灵摆描述
         */
        private String pdesc;
        /**
         * 效果描述
         */
        private String desc;
    }
    @Data
    @Accessors(chain = true)
    public static class BaiGeNodeResponseData {
        private int ot;
        /**
         * 字段bit
         */
        private long setcode;
        /**
         * 卡牌类型 （怪兽 魔法 陷阱 及其细分子类）
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
         * 阶级/星级
         */
        private long level;
        /**
         * 种族
         */
        private long race;
        /**
         * 属性
         */
        private long attribute;
    }
}
