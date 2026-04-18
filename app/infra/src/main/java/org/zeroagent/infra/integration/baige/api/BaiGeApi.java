package org.zeroagent.infra.integration.baige.api;

import com.fasterxml.jackson.databind.node.ObjectNode;
import feign.Param;
import feign.RequestLine;
import feign.Response;
import org.zeroagent.infra.core.card.model.BaiGeNodeResponse;
import org.zeroagent.infra.core.card.model.BaiGeQueryByIdResponse;

import java.util.Map;

/**
 * 百鸽API v0版本 <a href="https://ygocdb.com/api">...</a>
 * @author Nuk3m1
 * @version 2026年03月18日  19时36分
 */
public interface BaiGeApi {
    /**
     * 获取所有卡片的json文本
     */
    @RequestLine("GET /api/v0/cards.zip")
    Response getAllCards();

    /**
     *根据卡密查看卡牌信息
     */
    @RequestLine("GET /api/v0/card/{cardId}")
    BaiGeQueryByIdResponse getCardById(@Param("cardId") String cardId);

    /**
     *查看所有卡牌的MD5值，用于检验是否更新
     */
    @RequestLine("GET /api/v0/cards.zip.md5")
    String getAllCardsMd5();
}
