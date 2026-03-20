package com.drb.DrbMVP.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Location data")
public class LocationDto {

    @Schema(example = "Eyre Square")
    private String name;

    @Schema(example = "Central park in Galway city")
    private String description;

    @Schema(example = "53.2743")
    private Double latitude;

    @Schema(example = "-9.0488")
    private Double longitude;

    @Schema(example = "park")
    private String category;
}
