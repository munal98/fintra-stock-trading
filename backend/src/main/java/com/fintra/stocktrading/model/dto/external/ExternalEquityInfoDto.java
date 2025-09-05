package com.fintra.stocktrading.model.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class ExternalEquityInfoDto {

    @JsonProperty("code")
    private String code;

    @JsonProperty("issuer_name")
    private String equityName;

    @JsonProperty("market_desc")
    private String market;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("ticker")
    private String ticker;

    @JsonProperty("index")
    private String index;

    @JsonProperty("country")
    private String country;
}
