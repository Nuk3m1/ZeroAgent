package org.zeroagent.infra.core.cardgraph.model;

import lombok.Data;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

/**
 *
 * @author Nuk3m1
 * @version 2026年04月07日  14时44分
 */
@Data
@Node("Attribute")
public class AttributeNodeEntity {
    /**
     * 属性 如(地，火，水，风)
     */
    @Id
    private String name;
}
