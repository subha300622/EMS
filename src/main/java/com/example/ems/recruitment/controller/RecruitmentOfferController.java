package com.example.ems.recruitment.controller;

import com.example.ems.common.dto.ApiResponse;
import com.example.ems.recruitment.dto.OfferGenerateRequest;
import com.example.ems.recruitment.dto.OfferResponse;
import com.example.ems.recruitment.service.OfferService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/recruitment")
public class RecruitmentOfferController {

    @Autowired
    private OfferService offerService;

    @PostMapping("/applications/{applicationId}/offers")
    public ResponseEntity<ApiResponse<OfferResponse>> generateOffer(
            @PathVariable Long applicationId,
            @Valid @RequestBody OfferGenerateRequest request) {
        OfferResponse response = offerService.generateOffer(applicationId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Offer generated successfully", response));
    }

    @PostMapping("/offers/{offerId}/send")
    public ResponseEntity<ApiResponse<OfferResponse>> sendOffer(@PathVariable Long offerId) {
        OfferResponse response = offerService.sendOffer(offerId);
        return ResponseEntity.ok(ApiResponse.success("Offer sent to candidate successfully", response));
    }

    @GetMapping("/applications/{applicationId}/offers")
    public ResponseEntity<ApiResponse<OfferResponse>> getOfferByApplicationId(@PathVariable Long applicationId) {
        OfferResponse response = offerService.getOfferByApplicationId(applicationId);
        return ResponseEntity.ok(ApiResponse.success("Fetched offer details successfully", response));
    }
}
