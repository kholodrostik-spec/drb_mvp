package com.drb.DrbMVP.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NearestPointDto {
    private Long roadId;
    private String name;
    private String highway;
    private Double distance;
    private Double longitude;
    private Double latitude;
}
