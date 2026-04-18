package org.zeroagent.infra.integration.neo4j.configuration;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.core.Neo4jClient;

/**
 *
 * @author Nuk3m1
 * @version 2026年04月09日  10时55分
 */
@Configuration
@Slf4j
@RequiredArgsConstructor
public class Neo4jConstraintConfig {
    private final Neo4jClient neo4jClient;
    @PostConstruct
    public void InitNeo4jConstraints() {
        try {
            // 1. 卡密唯一约束
            neo4jClient.query("CREATE CONSTRAINT card_passcode_unique IF NOT EXISTS FOR (c:Card) REQUIRE c.passcode IS UNIQUE").run();

            // 2. 字段唯一约束
            neo4jClient.query("CREATE CONSTRAINT archetype_name_unique IF NOT EXISTS FOR (a:Archetype) REQUIRE a.name IS UNIQUE").run();

            // 3. 种族唯一约束
            neo4jClient.query("CREATE CONSTRAINT race_name_unique IF NOT EXISTS FOR (r:Race) REQUIRE r.name IS UNIQUE").run();

            // 4. 属性唯一约束
            neo4jClient.query("CREATE CONSTRAINT attribute_name_unique IF NOT EXISTS FOR (att:Attribute) REQUIRE att.name IS UNIQUE").run();

            log.info(" Neo4j 唯一约束初始化完成");
        } catch (Exception e) {
            log.error(" Neo4j 约束初始化失败！请检查数据库连接或权限。", e);
        }
    }
}
