package org.zeroagent.infra.integration;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.zeroagent.common.problem.exception.BizException;
import org.zeroagent.domain.core.card.error.CardErrorCode;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 解析 resources/card_config/strings.conf 文件为 Map 字段表
 * @author Nuk3m1
 * @version 2026年03月20日  14时01分
 */
@Slf4j
@Component
public class SetCodeDictionaryCache {
    private static Map<Long, String> SETCODE_MAP = new HashMap<>();
    @PostConstruct
    public void initDictionary() {
        Map<Long, String> tempMap = new HashMap<>();
        ClassPathResource resource = new ClassPathResource("card_config/strings.conf");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("!setname")){
                    // 按空白字符（空格或Tab）分割。例如："!setname", "0x307b", "银河眼时空龙", "ギャラクシー..."
                    String[] parts = line.split("\\s+");

                    if (parts.length > 2) {
                        try {
                            Long code = Long.decode(parts[1]);
                            String cnName = parts[2];
                            tempMap.put(code, cnName);
                        } catch (NumberFormatException e) {
                            log.warn("解析 strings.conf 时遇到非法十六机制码，已跳过:{}", parts[1]);
                        }
                    }
                }
            }
            // 将最终Map设置为不可变对象 防止修改
            SETCODE_MAP = Collections.unmodifiableMap(tempMap);
            log.info("Archetype Dictionary has been initial, with {} archetypes totally", SETCODE_MAP.size() );
        } catch (Exception e) {
            log.error("ERROR : initialize Archetype Dictionary failed!", e);
        }
    }
    /**
     * 通过setcode查找字段Archetype
     */
    public static String getArchetypeNameBySetCode(Long setCode) {
        return SETCODE_MAP.getOrDefault(setCode, null);
    }

}
