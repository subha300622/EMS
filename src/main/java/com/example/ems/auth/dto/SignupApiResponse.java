package com.example.ems.auth.dto;

import com.example.ems.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "SaaS Registration API Response Wrapper")
public class SignupApiResponse extends ApiResponse<SignupResponse> {
    public SignupApiResponse() {
        super();
    }
}
