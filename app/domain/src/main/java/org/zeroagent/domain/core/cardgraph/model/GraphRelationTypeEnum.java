package org.zeroagent.domain.core.cardgraph.model;

import lombok.Getter;

/**
 *
 * @author Nuk3m1
 * @version 2026年04月23日  12时52分
 */
@Getter
public enum GraphRelationTypeEnum {
    SEARCH("从卡组加入手卡的检索关系")

    ;
    private final String description;

    GraphRelationTypeEnum(String description) {
        this.description = description;
    }
}
