package com.drb.DrbMVP.dto.review;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Review data")
public class ReviewDto {
    private Long locationId;
    private Long userId;
    private Double rating;
    private String comment;
}
