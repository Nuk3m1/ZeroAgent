package org.zeroagent.api.core.file;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.zeroagent.biz.file.TosFileManager;
import org.zeroagent.common.result.ApiResult;

/**
 *
 * @author Nuk3m1
 * @version 2026年03月16日  12时31分
 */
@RestController
@RequestMapping(value = "/api/file/tos" , produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Slf4j
public class TosFileResource {
    private final TosFileManager tosFileManager;


    @PostMapping(path = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResult<String> TosFileUpload(@RequestPart("file") MultipartFile file) {
        return ApiResult.success(tosFileManager.upload(file));
    }

}
