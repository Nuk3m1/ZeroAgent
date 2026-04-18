package org.zeroagent.domain.core.cardgraph.service;

import java.util.List;

/**
 *
 * @author Nuk3m1
 * @version 2026年03月17日  20时55分
 */
public interface CardGraphRepository {
    // -------------------- 搭建卡牌基底节点 -------------------------------------------
    void createCardNodeEntity(String passcode, String name, String effect, String type);

    // --------------------      查询语句       -------------------------------------------
    List<String> getRelationships(String passcode);

    // -------------------- 结构化关系（根据pg数据库） -------------------------------------------
    /**
     * 画出字段关系
     * @param passcode 卡密
     * @param archetype 字段
     */
    void drawArchetypeArrow(String passcode, String archetype);

    /**
     * 画出种族关系
     * @param passcode 卡密
     * @param race 种族
     */
    void drawRaceArrow(String passcode, String race);

    /**
     * 画出属性关系
     * @param passcode 卡密
     * @param attribute 属性
     */
    void drawAttributeArrow(String passcode, String attribute);
    // -------------------- 语义关系（根据Agent语义分析） -------------------------------------------
    /**
     * 画出检索关系
     * @param passcode 卡密
     * @param searchPasscode 目标卡密
     */
    void drawSearchArrow(String passcode, String searchPasscode);

    /**
     * 画出素材关系
     * @param passcode 卡密
     * @param materialPasscode 目标卡密
     */
    void drawMaterialArrow(String passcode, String materialPasscode);
}
