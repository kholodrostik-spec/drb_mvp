package com.drb.DrbMVP.dto.apilog;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApiLogDto {
    private Long id;
    private String userEmail;
    private String method;
    private String path;
    private String queryParams;
    private String requestBody;
    private Integer status;
    private Long durationMs;
}
