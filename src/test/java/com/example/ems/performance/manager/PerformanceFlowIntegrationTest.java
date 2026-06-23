package com.example.ems.performance.manager;

import com.example.ems.common.exception.BadRequestException;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.performance.manager.dto.*;
import com.example.ems.performance.manager.entity.PerformanceReview;
import com.example.ems.performance.manager.repository.PerformanceReviewRepository;
import com.example.ems.performance.entity.MyGoal;
import com.example.ems.performance.repository.MyGoalRepository;
import com.example.ems.appraisal.entity.AppraisalCycle;
import com.example.ems.appraisal.repository.AppraisalCycleRepository;
import com.example.ems.performance.manager.service.ManagerPerformanceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class PerformanceFlowIntegrationTest {

        @Autowired
        private EmployeeRepository employeeRepository;

        @Autowired
        private MyGoalRepository myGoalRepository;

        @Autowired
        private AppraisalCycleRepository appraisalCycleRepository;

        @Autowired
        private PerformanceReviewRepository reviewRepository;

        @Autowired
        private ManagerPerformanceService performanceService;

        @Test
        public void testFullPerformanceManagementFlow() {
                // Setup Manager and Employee relationship
                Employee managerEmp = employeeRepository.findByEmail("manager@company.com")
                                .orElseThrow(() -> new AssertionError("Manager employee not found"));
                Employee employeeEmp = employeeRepository.findByEmail("employee@company.com")
                                .orElseThrow(() -> new AssertionError("Employee employee not found"));

                employeeEmp.setManager(managerEmp);
                employeeRepository.save(employeeEmp);

                String cycle = "FY2024-25";

                // Fetch or create an AppraisalCycle for the database constraint
                AppraisalCycle appraisalCycle = appraisalCycleRepository.findByName("Annual Appraisal 2026")
                                .orElseGet(() -> {
                                        AppraisalCycle c = new AppraisalCycle();
                                        c.setName("Annual Appraisal 2026");
                                        c.setStartDate(LocalDate.now().minusMonths(6));
                                        c.setEndDate(LocalDate.now().plusMonths(6));
                                        c.setStatus("ACTIVE");
                                        return appraisalCycleRepository.save(c);
                                });

                // 1. Employee creates a goal in their active system
                MyGoal goal = new MyGoal();
                goal.setGoalCode("GOAL-FLOW-001");
                goal.setTitle("Complete React Migration");
                goal.setCategory("TECHNICAL");
                goal.setPriority("HIGH");
                goal.setWeightage(10);
                goal.setEmployee(employeeEmp);
                goal.setCycle(appraisalCycle);
                goal.setStartDate(LocalDate.now());
                goal.setDueDate(LocalDate.now().plusMonths(6));
                goal.setProgressPercentage(0.0);
                goal.setStatus("IN_PROGRESS");
                myGoalRepository.save(goal);

                // 2. Tracks progress: Employee updates progress to 100% (Completed)
                goal.setProgressPercentage(100.0);
                goal.setStatus("COMPLETED");
                myGoalRepository.save(goal);

                // 3. Submits self review (review gets lazily initialized & copies progress when
                // dashboard/detail is accessed)
                // 4. Manager reviews employee (calls scorecard review detail service)
                ReviewDetailResponse detailResp = performanceService.getEmployeeReview(
                                "manager@company.com", employeeEmp.getId(), cycle);

                assertNotNull(detailResp);
                assertEquals(employeeEmp.getFullName(), detailResp.getEmployee().getName());
                assertEquals("Complete React Migration", detailResp.getGoals().get(0).getTitle());
                assertEquals(100, detailResp.getGoals().get(0).getProgress());
                assertEquals("MET", detailResp.getGoals().get(0).getStatus());

                // Fetch the review ID from the detail response
                PerformanceReview review = reviewRepository.findByEmployeeIdAndReviewCycle(employeeEmp.getId(), cycle)
                                .orElseThrow(() -> new AssertionError("Performance review was not initialized"));

                // 5. Manager gives competency ratings and comments
                SaveManagerRatingRequest ratingReq = new SaveManagerRatingRequest();
                ratingReq.setManagerComment("Exceptional systems implementation.");
                ratingReq.setRecommendation("PROMOTION");

                List<SaveManagerRatingRequest.CompetencyRatingInput> competencyInputs = new ArrayList<>();

                SaveManagerRatingRequest.CompetencyRatingInput comp1 = new SaveManagerRatingRequest.CompetencyRatingInput();
                comp1.setCompetency("Technical Skills");
                comp1.setScore(5);
                comp1.setComment("Strong engineering and clean coding standards.");
                competencyInputs.add(comp1);

                SaveManagerRatingRequest.CompetencyRatingInput comp2 = new SaveManagerRatingRequest.CompetencyRatingInput();
                comp2.setCompetency("Communication");
                comp2.setScore(4);
                comp2.setComment("Clear and structured communicator.");
                competencyInputs.add(comp2);

                ratingReq.setCompetencyRatings(competencyInputs);

                // 6. System calculates final score
                // Goal score: 1 goal (100% progress, weight 10) -> 100/100 * 5.0 = 5.0
                // Competency Avg: (5 + 4) / 2 = 4.5
                // Final Score: 5.0 * 0.4 + 4.5 * 0.6 = 2.0 + 2.7 = 4.7
                SaveManagerRatingResponse saveResp = performanceService.saveManagerRating(
                                "manager@company.com", review.getId(), ratingReq);

                assertNotNull(saveResp);
                assertEquals(review.getId(), saveResp.getReviewId());
                assertEquals("IN_PROGRESS", saveResp.getStatus());
                assertEquals(4.5, saveResp.getManagerRating());
                assertEquals(4.7, saveResp.getFinalScore());

                // 7. Manager submits final review
                SubmitReviewResponse submitResp = performanceService.submitFinalReview(
                                "manager@company.com", review.getId());

                assertNotNull(submitResp);
                assertEquals("COMPLETED", submitResp.getStatus());
                assertEquals(4.7, submitResp.getFinalScore());
                assertNotNull(submitResp.getSubmittedAt());

                // 8. Review locked (COMPLETED): Check that further updates are blocked
                assertThrows(BadRequestException.class, () -> {
                        performanceService.saveManagerRating("manager@company.com", review.getId(), ratingReq);
                });
        }
}
