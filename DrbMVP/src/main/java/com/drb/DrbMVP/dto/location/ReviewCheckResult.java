package com.drb.DrbMVP.dto.location;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ReviewCheckResult {
     private Double rating;
     private String comment;
     private String photoUrl;
 }
