package org.zeroagent.infra.core.cardgraph.model;

import lombok.Data;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

/**
 *
 * @author Nuk3m1
 * @version 2026年04月07日  14时30分
 */
@Data
@Node("Archetype")
public class ArchetypeNodeEntity {
    /**
     * 字段名 如 (青眼，黑魔导)
     */
    @Id
    private String name;
}
