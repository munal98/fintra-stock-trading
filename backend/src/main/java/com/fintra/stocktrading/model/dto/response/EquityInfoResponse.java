package com.fintra.stocktrading.model.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EquityInfoResponse {
    private Integer equityId;
    private String assetCode;
    private String ticker;
    private String equityName;
    private String market;
    private String country;
    private boolean participation;
}
