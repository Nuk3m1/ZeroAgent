package org.zeroagent.infra.core.cardgraph.model;

import lombok.Data;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

/**
 *
 * @author Nuk3m1
 * @version 2026年04月07日  14时26分
 */
@Data
@Node("Card")
public class CardNodeEntity {
    /**
     * 卡密 （唯一标识，主键定位）
     */
    @Id
    private String                           passcode;
    @Property("name")
    private String                           name;
    @Property("effect")
    private String                           effect;
    @Property("type")
    private String                           type;


}
