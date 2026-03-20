package com.drb.DrbMVP.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ReviewResponseDto {
    private Long locationId;
    private Long userId;
    private Double rating;
    private String comment;
    private LocalDateTime createdAt;
}
