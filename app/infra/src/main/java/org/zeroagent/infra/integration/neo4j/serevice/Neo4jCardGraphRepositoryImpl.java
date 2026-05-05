package org.zeroagent.infra.integration.neo4j.serevice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.data.neo4j.core.Neo4jTemplate;
import org.springframework.stereotype.Component;
import org.zeroagent.domain.core.cardgraph.service.CardGraphRepository;
import org.zeroagent.infra.core.cardgraph.model.CardNodeEntity;

import java.util.List;

/**
 *
 * @author Nuk3m1
 * @version 2026年03月17日  14时28分
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class Neo4jCardGraphRepositoryImpl implements CardGraphRepository {
    private final Neo4jClient                   neo4jClient;
    private final Neo4jTemplate                 neo4jTemplate;

    // -------------------- 搭建卡牌基底节点 -------------------------------------------
    @Override
    public void createCardNodeEntity(String passcode, String name, String effect, String type) {
        CardNodeEntity cardNodeEntity = new CardNodeEntity();
        cardNodeEntity.setPasscode(passcode);
        cardNodeEntity.setName(name);
        cardNodeEntity.setEffect(effect);
        cardNodeEntity.setType(type);
        neo4jTemplate.save(cardNodeEntity);
    }

    // --------------------      查询语句       -------------------------------------------
    @Override
    public List<String> getRelationships(String passcode) {
        String cypher = """
                MATCH (c:Card {passcode: $passcode})-[r]->(target)
                RETURN type(r) AS relType, target.name AS targetName
                """;
        return neo4jClient.query(cypher)
                .bind(passcode).to("passcode")
                .fetchAs(String.class)
                .mappedBy((typeSystem, record) -> {
                    String relation = record.get("relType").asString();
                    String targetCard = record.get("targetName").asString();
                    return "【关系】： " + relation + " )-> 【目标节点】：" + targetCard;
                })
                .all()
                .stream().toList();
    }



    // -------------------- 结构化关系（根据pg数据库） -------------------------------------------
    @Override
    public void drawArchetypeArrow(String passcode, String archetype) {
        String cypher = """
                MATCH (c:Card {passcode: $passcode})
                MERGE (a:Archetype {name: $archetype})
                MERGE (c)-[:BELONGS_TO]->(a)
            """;

        neo4jClient.query(cypher)
                .bind(passcode).to("passcode")
                .bind(archetype).to("archetype")
                .run();
    }

    @Override
    public void drawRaceArrow(String passcode, String race) {
        String cypher = """
                MATCH (c:Card {passcode: $passcode})
                MERGE (a:Race {name: $race})
                MERGE (c)-[:HAS_RACE]->(a)
                """;
        neo4jClient.query(cypher)
                .bind(passcode).to("passcode")
                .bind(race).to("race")
                .run();
    }

    @Override
    public void drawAttributeArrow(String passcode, String attribute) {
        String cypher = """
                MATCH (c:Card {passcode: $passcode})
                MERGE (a:Attribute {name: $attribute})
                MERGE (c)-[:HAS_ATTRIBUTE]->(a)
                """;
        neo4jClient.query(cypher)
                .bind(passcode).to("passcode")
                .bind(attribute).to("attribute")
                .run();
    }
    // -------------------- 语义关系（根据Agent语义分析） -------------------------------------------
    @Override
    public void drawSearchArrow(String passcode, String searchPasscode) {
        String cypher = """
                MATCH (c:Card {passcode: $passcode})
                MATCH (a:Card {passcode: $searchPasscode})
                MERGE (c)-[:SEARCH]->(a)
                RETURN 1 AS ok
                """;
        Integer matched = neo4jClient.query(cypher)
                .bind(passcode).to("passcode")
                .bind(searchPasscode).to("searchPasscode")
                .fetchAs(Integer.class)
                .one()
                .orElse(null);
        if (matched == null) {
            throw new IllegalStateException("Neo4j未找到可建边节点, sourcePassCode=" + passcode + ", targetPassCode=" + searchPasscode);
        }
    }

    @Override
    public void drawMaterialArrow(String passcode, String materialPasscode) {
        String cypher = """
                MATCH (c:Card {passcode: $passcode})
                MATCH (a:Card {passcode: $materialPasscode})
                MERGE (c)-[:REQUIRE_MATERIAL]->(a)
                """;
        neo4jClient.query(cypher)
                .bind(passcode).to("passcode")
                .bind(materialPasscode).to("materialPasscode")
                .run();
    }
}
