package com.drb.DrbMVP.dto.review;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
@Schema(description = "Review with optional photo")
public class ReviewWithPhotoRequest {

    @Schema(required = true, example = "1")
    private Long locationId;

    @Schema(required = true, example = "1")
    private Long userId;

    @Schema(required = true, example = "4.5")
    private Double rating;

    @Schema(example = "Great place!")
    private String comment;

    @Schema(type = "string", format = "binary", description = "Photo (JPEG/PNG/WEBP, max 10MB)")
    private MultipartFile photo;
}
