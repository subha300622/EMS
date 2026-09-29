package com.example.ems.increment.service;

import com.example.ems.common.exception.BadRequestException;
import com.example.ems.common.exception.ResourceNotFoundException;
import com.example.ems.employee.entity.Employee;
import com.example.ems.increment.dto.IncrementLetterResponse;
import com.example.ems.increment.entity.IncrementLetter;
import com.example.ems.increment.entity.IncrementRecommendation;
import com.example.ems.increment.entity.IncrementRecommendationStatus;
import com.example.ems.increment.repository.IncrementLetterRepository;
import com.example.ems.increment.repository.IncrementRecommendationRepository;
import com.example.ems.organization.entity.Organization;
import com.example.ems.security.context.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Increment Letter Service Unit Tests")
public class IncrementLetterServiceTest {

    @Mock
    private IncrementRecommendationRepository recommendationRepository;

    @Mock
    private IncrementLetterRepository letterRepository;

    @InjectMocks
    private IncrementLetterService letterService;

    private Organization organization;
    private Employee employee;
    private IncrementRecommendation recommendation;

    @BeforeEach
    void setUp() {
        TenantContext.setCurrentTenant(1L);

        organization = new Organization();
        organization.setId(1L);

        employee = new Employee();
        employee.setId(125L);
        employee.setEmployeeId("EMP125");
        employee.setFullName("John Doe");
        employee.setDepartment("Engineering");
        employee.setDesignation("Senior Software Engineer");

        recommendation = new IncrementRecommendation();
        recommendation.setId(800L);
        recommendation.setOrganization(organization);
        recommendation.setEmployee(employee);
        recommendation.setCurrentSalary(new BigDecimal("600000.00"));
        recommendation.setIncrementPercentage(new BigDecimal("10.0"));
        recommendation.setIncrementAmount(new BigDecimal("60000.00"));
        recommendation.setRecommendedSalary(new BigDecimal("660000.00"));
        recommendation.setEffectiveDate(LocalDate.of(2026, 10, 1));
        recommendation.setStatus(IncrementRecommendationStatus.IMPLEMENTED);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("Generate letter for IMPLEMENTED recommendation -> Success")
    void testGenerateLetter_ImplementedStatus_Success() {
        when(recommendationRepository.findByIdAndOrgId(800L, 1L)).thenReturn(Optional.of(recommendation));
        when(letterRepository.findByRecommendationIdAndOrgId(800L, 1L)).thenReturn(Optional.empty());
        when(letterRepository.save(any(IncrementLetter.class))).thenAnswer(inv -> {
            IncrementLetter l = inv.getArgument(0);
            l.setId(101L);
            return l;
        });

        IncrementLetterResponse response = letterService.generateLetter(800L);

        assertNotNull(response);
        assertEquals(101L, response.getId());
        assertEquals(800L, response.getRecommendationId());
        assertEquals(125L, response.getEmployeeId());
        assertEquals("John Doe", response.getEmployeeName());
        assertNotNull(response.getLetterReference());
        assertTrue(response.getContent().contains("OFFICIAL INCREMENT LETTER"));
        assertTrue(response.getContent().contains("600000.00"));
        assertTrue(response.getContent().contains("660000.00"));

        verify(letterRepository).save(any(IncrementLetter.class));
    }

    @Test
    @DisplayName("Generate letter is idempotent: returns existing letter if already generated")
    void testGenerateLetter_AlreadyGenerated_ReturnsExistingIdempotently() {
        IncrementLetter existingLetter = new IncrementLetter();
        existingLetter.setId(999L);
        existingLetter.setRecommendation(recommendation);
        existingLetter.setEmployee(employee);
        existingLetter.setLetterReference("INC-LET-800-EXISTING");
        existingLetter.setContent("Existing letter content");
        existingLetter.setGeneratedAt(LocalDateTime.now());

        when(recommendationRepository.findByIdAndOrgId(800L, 1L)).thenReturn(Optional.of(recommendation));
        when(letterRepository.findByRecommendationIdAndOrgId(800L, 1L)).thenReturn(Optional.of(existingLetter));

        IncrementLetterResponse response = letterService.generateLetter(800L);

        assertNotNull(response);
        assertEquals(999L, response.getId());
        assertEquals("INC-LET-800-EXISTING", response.getLetterReference());
        assertEquals("Existing letter content", response.getContent());

        verify(letterRepository, never()).save(any());
    }

    @Test
    @DisplayName("RECOMMENDED status cannot generate letter -> Throws BadRequestException")
    void testGenerateLetter_RecommendedStatus_ThrowsBadRequest() {
        recommendation.setStatus(IncrementRecommendationStatus.RECOMMENDED);
        when(recommendationRepository.findByIdAndOrgId(800L, 1L)).thenReturn(Optional.of(recommendation));

        BadRequestException ex = assertThrows(BadRequestException.class, () -> letterService.generateLetter(800L));
        assertTrue(ex.getMessage().contains("only be generated after salary implementation"));
        verify(letterRepository, never()).save(any());
    }

    @Test
    @DisplayName("UNDER_REVIEW status cannot generate letter -> Throws BadRequestException")
    void testGenerateLetter_UnderReviewStatus_ThrowsBadRequest() {
        recommendation.setStatus(IncrementRecommendationStatus.UNDER_REVIEW);
        when(recommendationRepository.findByIdAndOrgId(800L, 1L)).thenReturn(Optional.of(recommendation));

        BadRequestException ex = assertThrows(BadRequestException.class, () -> letterService.generateLetter(800L));
        assertTrue(ex.getMessage().contains("only be generated after salary implementation"));
        verify(letterRepository, never()).save(any());
    }

    @Test
    @DisplayName("APPROVED status cannot generate letter until implemented -> Throws BadRequestException")
    void testGenerateLetter_ApprovedStatus_ThrowsBadRequest() {
        recommendation.setStatus(IncrementRecommendationStatus.APPROVED);
        when(recommendationRepository.findByIdAndOrgId(800L, 1L)).thenReturn(Optional.of(recommendation));

        BadRequestException ex = assertThrows(BadRequestException.class, () -> letterService.generateLetter(800L));
        assertTrue(ex.getMessage().contains("only be generated after salary implementation"));
        verify(letterRepository, never()).save(any());
    }

    @Test
    @DisplayName("REJECTED status cannot generate letter -> Throws BadRequestException")
    void testGenerateLetter_RejectedStatus_ThrowsBadRequest() {
        recommendation.setStatus(IncrementRecommendationStatus.REJECTED);
        when(recommendationRepository.findByIdAndOrgId(800L, 1L)).thenReturn(Optional.of(recommendation));

        BadRequestException ex = assertThrows(BadRequestException.class, () -> letterService.generateLetter(800L));
        assertTrue(ex.getMessage().contains("only be generated after salary implementation"));
        verify(letterRepository, never()).save(any());
    }

    @Test
    @DisplayName("SENT_BACK status cannot generate letter -> Throws BadRequestException")
    void testGenerateLetter_SentBackStatus_ThrowsBadRequest() {
        recommendation.setStatus(IncrementRecommendationStatus.SENT_BACK);
        when(recommendationRepository.findByIdAndOrgId(800L, 1L)).thenReturn(Optional.of(recommendation));

        BadRequestException ex = assertThrows(BadRequestException.class, () -> letterService.generateLetter(800L));
        assertTrue(ex.getMessage().contains("only be generated after salary implementation"));
        verify(letterRepository, never()).save(any());
    }

    @Test
    @DisplayName("Wrong tenant cannot generate letter -> Throws ResourceNotFoundException")
    void testGenerateLetter_WrongTenant_ThrowsResourceNotFound() {
        when(recommendationRepository.findByIdAndOrgId(999L, 1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> letterService.generateLetter(999L));
        verify(letterRepository, never()).save(any());
    }
}
