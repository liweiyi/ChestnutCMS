package com.chestnut.media.domain.dto;

import com.chestnut.system.validator.LongId;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * 视频截图DTO
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
public class VideoScreenshotDTO {

    /**
     * 视频资源路径
     */
    @NotEmpty
    private String path;

    /**
     * 截图时间点，单位：秒
     */
    @NotNull
    @Min(0)
    private Long timestamp;
}
