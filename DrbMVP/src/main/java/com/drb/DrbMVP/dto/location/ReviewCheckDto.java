package com.drb.DrbMVP.dto.location;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ReviewCheckDto {
    private boolean hasReview;
    private Long locationId;
    private String locationName;
    private Double existingRating;
    private String existingComment;
}
