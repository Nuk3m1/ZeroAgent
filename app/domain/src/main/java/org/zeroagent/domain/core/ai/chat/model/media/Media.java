package org.zeroagent.domain.core.ai.chat.model.media;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.zeroagent.common.problem.error.CommonErrorCode;
import org.zeroagent.common.problem.exception.BizException;

/**
 * @author Nuk3m1
 * @version 2026年03月07日  15时32分
 * @Description:
 */
@Data
@Accessors(chain = true)
public class Media {
    @NotNull
    private String type;
    @Nullable
    private String text;
    @JsonProperty("video_url")
    @Nullable
    private VideoMedia videoUrl;
    @JsonProperty("image_url")
    @Nullable
    private ImageMedia imageUrl;
    @Data
    @Accessors(chain = true)
    public static class VideoMedia {
        private String url;
    }
    @Data
    @Accessors(chain = true)
    public static class ImageMedia {
        private String url;
    }


    public Media(MediaType mediaType, String content) {

        if (mediaType == MediaType.TEXT) {
            this.text = content;
            this.type = mediaType.getValue();
        } else if (mediaType == MediaType.VIDEO) {
            this.type = MediaType.VIDEO.getValue();
            this.videoUrl = new VideoMedia().setUrl(content);
        } else if (mediaType == MediaType.IMAGE) {
            this.type = MediaType.IMAGE.getValue();
            this.imageUrl = new ImageMedia().setUrl(content);
        } else {
            throw new BizException(CommonErrorCode.ILLEGAL_PARAM, "MediaType is wrong");
        }
    }
}
