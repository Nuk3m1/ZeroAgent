package org.zeroagent.biz.file;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.zeroagent.common.problem.error.CommonErrorCode;
import org.zeroagent.common.problem.exception.BizException;
import org.zeroagent.domain.common.integration.tos.FileAcl;
import org.zeroagent.domain.common.integration.tos.TosTemplate;

import java.io.IOException;

/**
 *
 * @author Nuk3m1
 * @version 2026年03月16日  12时34分
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TosFileManager {
    private final TosTemplate                   tosTemplate;



    public String upload(MultipartFile file) {
        String bucket = "zeroagent";
        String key = "/test";
        final String result;
        try {
            result = tosTemplate.upload(bucket, key, FileAcl.PRIVATE, file.getInputStream());
        } catch (IOException e) {
            log.error("[upload-error] bucket = {}, key = {}", bucket, key, e);
            throw new BizException(CommonErrorCode.UNSPECIFIED, e);
        }
        return result;
    }

}
