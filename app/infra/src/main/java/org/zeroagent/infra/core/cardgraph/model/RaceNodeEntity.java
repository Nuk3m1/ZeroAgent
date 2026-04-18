package org.zeroagent.infra.core.cardgraph.model;

import lombok.Data;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

/**
 *
 * @author Nuk3m1
 * @version 2026年04月07日  14时32分
 */
@Data
@Node("Race")
public class RaceNodeEntity {
    /**
     * 种族 如(龙族，雷族)
     */
    @Id
    private String name;
}
