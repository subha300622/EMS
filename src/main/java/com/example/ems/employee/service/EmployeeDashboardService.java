package com.example.ems.employee.service;

import com.example.ems.attendance.dto.AttendanceCoreResponse;
import com.example.ems.auth.entity.User;
import com.example.ems.employee.dto.MyDocumentDetailsResponse;
import com.example.ems.employee.dto.MyDocumentUploadResponse;
import com.example.ems.employee.dto.dashboard.*;
import com.example.ems.employee.entity.Employee;
import com.example.ems.leave.dto.LeaveRequest;
import com.example.ems.leave.entity.Leave;
import com.example.ems.performance.dto.EnterprisePerformanceReviewResponse;
import com.example.ems.performance.dto.EnterpriseSelfReviewRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface EmployeeDashboardService {

    EmployeeDashboardResponse getDashboard();

    EmployeeAttendanceDetailSummaryDto getAttendanceSummary();

    List<AttendanceCoreResponse> getAttendanceHistory(String month);

    EmployeeLeaveBalanceDetailDto getLeaveBalance();

    EmployeeCompensationSummaryDto getCurrentCompensation();

    EmployeePerformanceSummaryDto getPerformanceSummary();

    EmployeeActionCenterResponseDto getActionCenter();

    // Leave Drill-downs
    List<Leave> getMyLeaves();

    Leave applyMyLeave(LeaveRequest request);

    Leave getMyLeaveById(Long id);

    // Performance Drill-downs
    List<EnterprisePerformanceReviewResponse> getMyPerformanceReviews();

    EnterprisePerformanceReviewResponse getMyPerformanceReviewById(Long id);

    EnterprisePerformanceReviewResponse submitMySelfReview(Long id, EnterpriseSelfReviewRequest request);

    // Document Drill-downs
    Object getMyDocuments();

    MyDocumentUploadResponse uploadMyDocument(MultipartFile file, Long categoryId, String documentType,
                                             String documentNumber, String issuedDate, String expiryDate, String remarks);

    MyDocumentDetailsResponse getMyDocumentDetails(Long id);

    // Training Drill-downs
    List<Map<String, Object>> getMyTrainings();

    Map<String, Object> getMyTrainingDetails(Long id);

    Map<String, Object> completeMyTraining(Long id);

    // Context identity helpers
    Employee resolveCurrentEmployee();

    User resolveCurrentUser();
}
