package org.zeroagent.infra.integration.feishu.model;

import lombok.experimental.UtilityClass;

/**
 *
 * @author Nuk3m1
 * @version 2026年04月17日  22时49分
 */
@UtilityClass
public class FeiShuCardEntityPool {
    public final String OCG_CARD_ENTITY_JSON = """
            {
              "schema": "2.0",
              "config": {
                "streaming_mode": true,
                "update_multi": true
              },
              "header": {
                "padding": "12px 12px 12px 12px",
                "title": {
                  "tag": "plain_text",
                  "content": "🤖 ZeroAgent-游戏王OCG卡牌助手"
                },
                "template": "blue"
              },
              "body": {
                "elements": [
                  {
                    "tag": "column_set",
                    "element_id": "first_element",
                    "flex_mode": "stretch",
                    "horizontal_spacing": "12px",
                    "margin": "0px",
                    "columns": [
                      {
                        "tag": "column",
                        "width": "weighted",
                        "weight": 1,
                        "background_style": "blue-50",
                        "padding": "12px",
                        "vertical_spacing": "4px",
                        "elements": [
                          {
                            "element_id": "reasoning_title",
                            "tag": "markdown",
                            "content": "**<font color='blue'>🧠 **深度思考**</font>**"
                          },
                          {
                            "element_id": "reasoning_content",
                            "tag": "markdown",
                            "content": "思考中..."
                          }
                        ]
                      }
                    ]
                  },
                  {
                    "tag": "column_set",
                    "element_id": "second_element",
                    "flex_mode": "stretch",
                    "horizontal_spacing": "12px",
                    "margin": "12px 0px 0px 0px",
                    "columns": [
                      {
                        "tag": "column",
                        "width": "weighted",
                        "weight": 1,
                        "background_style": "violet-50",
                        "padding": "12px",
                        "vertical_spacing": "4px",
                        "elements": [
                          {
                            "element_id": "main_title",
                            "tag": "markdown",
                            "content": "**<font color='violet'>⭕️ **总结**</font>**"
                          },
                          {
                            "element_id": "main_content",
                            "tag": "markdown",
                            "content": "..."
                          }
                        ]
                      }
                    ]
                  }
                ]
              }
            }
            """;
}
