package org.zeroagent.infra.core.card.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.zeroagent.common.id.IdHelper;
import org.zeroagent.common.problem.exception.BizException;
import org.zeroagent.common.utils.json.JSON;
import org.zeroagent.domain.core.card.error.CardErrorCode;
import org.zeroagent.domain.core.card.model.CardInformation;
import org.zeroagent.domain.core.card.model.CardInformationStatusEnum;
import org.zeroagent.domain.core.card.model.CardSubTypeEnum;
import org.zeroagent.domain.core.card.model.CardTypeEnum;
import org.zeroagent.domain.core.card.service.CardInformationFetchClient;
import org.zeroagent.domain.core.card.service.CardInformationRepository;
import org.zeroagent.domain.core.card.utils.CardTypeBitMaskUtil;
import org.zeroagent.infra.core.card.model.BaiGeNodeResponse;
import org.zeroagent.infra.integration.SetCodeDictionaryCache;
import org.zeroagent.infra.integration.baige.api.BaiGeApi;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * 封装百鸽API - 实现卡牌信息获取服务
 * @author Nuk3m1
 * @version 2026年03月20日  14时46分
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class BaiGeClientImpl implements CardInformationFetchClient {
    private final BaiGeApi baiGeApi;
    private final CardInformationRepository cardInformationRepository;
    private final ObjectMapper objectMapper;


    @Override
    public void getCards() {
        Response response = baiGeApi.getAllCards();
        // 检查响应状态
        if (response.status() != 200) {
            log.error("下载失败，HTTP 状态码: {}", response.status());
            return;
        }

        try (InputStream inputStream = response.body().asInputStream();
             ZipInputStream zipInputStream = new ZipInputStream(inputStream)) {
            ZipEntry entry = zipInputStream.getNextEntry();
            if (entry != null) {
                Map<String, BaiGeNodeResponse> resultMap = objectMapper.readValue(zipInputStream, new TypeReference<Map<String, BaiGeNodeResponse>>() {});
                List<CardInformation> cardInformationList = new ArrayList<>();
                for (Map.Entry<String, BaiGeNodeResponse> mapEntry : resultMap.entrySet()) {
                    BaiGeNodeResponse node = mapEntry.getValue();
                    if (node.getData() == null) {
                        log.info("data为空的节点node:{},名称为:{}", node.getCid(), node.getCnName());
                        continue;
                    }
                    cardInformationList.add(convertBaigeToCardInformation(node));
                }
                cardInformationRepository.batchCreate(cardInformationList);
            }
        } catch (Exception e) {
            log.error("获取所有卡牌信息时，发生异常", e);
            throw new BizException(CardErrorCode.HTTP_ERROR);
        }


    }
    // TODO 建立卡密索引 直接从pg库中查询
    @Override
    public CardInformation getCardInformationByCardId(String cardId) {
        return null;
    }

    private static CardInformation convertBaigeToCardInformation(BaiGeNodeResponse baiGeNodeResponse) {
        List<Long> setCodes = parseSetcodes(baiGeNodeResponse.getData().getSetcode());


        List<String> archetypes = setCodes.stream()
                .map(SetCodeDictionaryCache::getArchetypeNameBySetCode)
                .filter(Objects::nonNull)
                .toList();
        CardTypeEnum cardType = CardTypeBitMaskUtil.parseMainType(baiGeNodeResponse.getData().getType());
        List<CardSubTypeEnum> subTypes = CardTypeBitMaskUtil.parseSubTypes(baiGeNodeResponse.getData().getType(), cardType);
        // 装填 主键ID，卡密，卡名，属性，攻击力，防御力，灵摆效果，怪兽效果，图谱状态，原生响应体，字段列表,卡牌种类，卡牌分种类
        CardInformation cardInformation = new CardInformation()
                .setId(IdHelper.getId())
                .setPasscode(String.valueOf(baiGeNodeResponse.getId()))
                .setName(baiGeNodeResponse.getCnName())
                .setAttribution(CardTypeBitMaskUtil.parseAttribute(baiGeNodeResponse.getData().getAttribute()))
                .setAtk(baiGeNodeResponse.getData().getAtk())
                .setDef(baiGeNodeResponse.getData().getDef())
                .setRace(CardTypeBitMaskUtil.parseRace(baiGeNodeResponse.getData().getRace()))
                .setPendulumEffect(baiGeNodeResponse.getText().getPdesc() == null ? "" : baiGeNodeResponse.getText().getPdesc())
                .setEffect(baiGeNodeResponse.getText().getDesc() == null ? "" : baiGeNodeResponse.getText().getDesc())
                .setGraphSyncStatus(CardInformationStatusEnum.PENDING)
                .setBizResponse(JSON.toJSONObject(baiGeNodeResponse))
                .setArchetype(archetypes)
                .setCardType(cardType)
                .setCardSubtype(subTypes);
        // 装填 种族，星级，阶级，link值，灵摆刻度
        parseLevelAndScales(baiGeNodeResponse.getData().getLevel(), baiGeNodeResponse.getData().getType(), cardInformation);
        return cardInformation;
    }

    public static void parseLevelAndScales(long rawLevel, long rawType, CardInformation entity) {
        // 提取基础数值（掩码 0xFFL 截取最低的 8 位，即 0~255）
        long baseValue = rawLevel & 0xFFL;

        if ((rawType & 8388608L) != 0) {
            // 阶级
            entity.setMonsterRank((int) baseValue);
        } else if ((rawType & 67108864L) != 0) {
            // Link值
            entity.setLinkRating((int) baseValue);
        } else {
            // 星级
            entity.setMonsterLevel((int) baseValue);
        }

        // 灵摆刻度
        if ((rawType & 16777216L) != 0) {
            // 提取左刻度：向右移 24 位，然后截取 8 位
            long lScale = (rawLevel >> 24) & 0xFFL;

            // 提取右刻度：向右移 16 位，然后截取 8 位
            // long rScale = (rawLevel >> 16) & 0xFFL;

            // OCG 中左右刻度目前是绝对对称的，取左刻度存入实体即可
            entity.setPendulumScale((int) lScale);
        }
    }
    /**
     * Setcode 64位无损切分器
     *
     * @param setcode 百鸽 API 返回的 64 位整型字段集合
     * @return 拆分后的独立字段十六进制值列表 (不会包含空悬的 0)
     */
    public static List<Long> parseSetcodes(long setcode) {
        List<Long> setcodes = new ArrayList<>();

        // 极致压榨：64位最多只能切出 4 个 16位的独立字段
        for (int i = 0; i < 4; i++) {
            // 每次向右移位 (16 * i) 位，并用 0xFFFFL 截取最低 16 位
            long code = (setcode >> (i * 16)) & 0xFFFFL;

            // 如果切出来的片段不为 0，说明这个槽位装载了一个字段
            if (code != 0L) {
                setcodes.add(code);
            }
        }

        return setcodes;
    }
}
