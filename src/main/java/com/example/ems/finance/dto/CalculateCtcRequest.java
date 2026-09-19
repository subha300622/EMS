package com.example.ems.finance.dto;

import com.example.ems.common.validation.AtLeastOne;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "Request body for calculating CTC breakup")
@AtLeastOne(fields = {"ctc", "monthlyCtc"}, message = "Either ctc (annual) or monthlyCtc must be provided")
public record CalculateCtcRequest(
    @Schema(description = "Annual CTC amount", example = "1200000.00")
    BigDecimal ctc,

    @Schema(description = "Monthly CTC amount", example = "100000.00")
    BigDecimal monthlyCtc
) {}
