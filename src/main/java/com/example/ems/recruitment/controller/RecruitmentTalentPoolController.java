package com.example.ems.recruitment.controller;

import com.example.ems.common.dto.ApiResponse;
import com.example.ems.recruitment.dto.TalentPoolCandidateResponse;
import com.example.ems.recruitment.dto.TalentPoolInviteRequest;
import com.example.ems.recruitment.service.TalentPoolService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/recruitment/talent-pool")
public class RecruitmentTalentPoolController {

    @Autowired
    private TalentPoolService talentPoolService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<TalentPoolCandidateResponse>>> searchTalentPool(
            @RequestParam(required = false) String skill,
            @RequestParam(required = false) Double experienceMin,
            @RequestParam(required = false) Double experienceMax,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction) {

        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        Page<TalentPoolCandidateResponse> candidates = talentPoolService.searchTalentPool(
                skill, experienceMin, experienceMax, location, search, PageRequest.of(page, size, sort));

        return ResponseEntity.ok(ApiResponse.success("Fetched talent pool candidates successfully", candidates));
    }

    @PostMapping("/{candidateId}/invite")
    public ResponseEntity<ApiResponse<String>> inviteCandidate(
            @PathVariable Long candidateId,
            @Valid @RequestBody TalentPoolInviteRequest request) {
        talentPoolService.inviteCandidate(candidateId, request);
        return ResponseEntity.ok(ApiResponse.success("Invitation sent to candidate successfully", "INVITED"));
    }
}
