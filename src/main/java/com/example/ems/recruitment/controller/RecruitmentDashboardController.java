package com.example.ems.recruitment.controller;

import com.example.ems.common.dto.ApiResponse;
import com.example.ems.recruitment.dto.RecruitmentDashboardResponse;
import com.example.ems.recruitment.service.RecruitmentDashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/recruitment/dashboard")
public class RecruitmentDashboardController {

    @Autowired
    private RecruitmentDashboardService recruitmentDashboardService;

    @GetMapping
    public ResponseEntity<ApiResponse<RecruitmentDashboardResponse>> getDashboardStats() {
        RecruitmentDashboardResponse stats = recruitmentDashboardService.getDashboardStats();
        return ResponseEntity.ok(ApiResponse.success("Fetched recruitment dashboard stats successfully", stats));
    }
}
