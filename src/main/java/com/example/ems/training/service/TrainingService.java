package com.example.ems.training.service;

import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.training.dto.TrainingAssessmentRequest;
import com.example.ems.training.dto.TrainingAttendanceRequest;
import com.example.ems.training.dto.TrainingCertificateResponse;
import com.example.ems.training.dto.TrainingCourseRequest;
import com.example.ems.training.dto.TrainingCourseResponse;
import com.example.ems.training.dto.TrainingDashboardResponse;
import com.example.ems.training.dto.TrainingEnrollmentRequest;
import com.example.ems.training.dto.TrainingEnrollmentResponse;
import com.example.ems.training.dto.TrainingSessionRequest;
import com.example.ems.training.dto.TrainingSessionResponse;
import com.example.ems.training.entity.TrainingAssessmentSubmission;
import com.example.ems.training.entity.TrainingAttendance;
import com.example.ems.training.entity.TrainingCertificate;
import com.example.ems.training.entity.TrainingCourse;
import com.example.ems.training.entity.TrainingEnrollment;
import com.example.ems.training.entity.TrainingSession;
import com.example.ems.training.repository.TrainingAssessmentSubmissionRepository;
import com.example.ems.training.repository.TrainingAttendanceRepository;
import com.example.ems.training.repository.TrainingCertificateRepository;
import com.example.ems.training.repository.TrainingCourseRepository;
import com.example.ems.training.repository.TrainingEnrollmentRepository;
import com.example.ems.training.repository.TrainingSessionRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TrainingService {

    @Autowired
    private TrainingCourseRepository courseRepository;
    @Autowired
    private TrainingSessionRepository sessionRepository;
    @Autowired
    private TrainingEnrollmentRepository enrollmentRepository;
    @Autowired
    private TrainingAttendanceRepository attendanceRepository;
    @Autowired
    private TrainingAssessmentSubmissionRepository submissionRepository;
    @Autowired
    private TrainingCertificateRepository certificateRepository;
    @Autowired
    private EmployeeRepository employeeRepository;

    // ── 1. DASHBOARD ────────────────────────────────────────────────────────
    @Cacheable(value = "trainingDashboard", key = "'stats'")
    public TrainingDashboardResponse getDashboardStats() {
        TrainingDashboardResponse stats = new TrainingDashboardResponse();

        long totalCourses = courseRepository.count();
        long activeCourses = courseRepository.findByStatus("ACTIVE").size();
        long totalSessions = sessionRepository.count();
        long totalEnrollments = enrollmentRepository.count();
        long completedEnrollments = enrollmentRepository.findByStatus("COMPLETED").size();

        double avgProgress = enrollmentRepository.findAll().stream()
                .mapToInt(TrainingEnrollment::getProgressPercent)
                .average()
                .orElse(0.0);

        long withdrawnCount = enrollmentRepository.findByStatus("WITHDRAWN").size();
        double withdrawalRate = totalEnrollments > 0 ? ((double) withdrawnCount / totalEnrollments) * 100.0 : 0.0;

        stats.setTotalCourses(totalCourses);
        stats.setActiveCourses(activeCourses);
        stats.setTotalSessions(totalSessions);
        stats.setTotalEnrollments(totalEnrollments);
        stats.setCompletedEnrollments(completedEnrollments);
        stats.setAverageProgress(Math.round(avgProgress * 100.0) / 100.0);
        stats.setWithdrawalRate(Math.round(withdrawalRate * 100.0) / 100.0);

        return stats;
    }

    // ── 2. COURSES ───────────────────────────────────────────────────────────
    @Transactional
    @CacheEvict(value = "trainingDashboard", allEntries = true)
    public TrainingCourseResponse createCourse(TrainingCourseRequest request) {
        TrainingCourse course = new TrainingCourse();
        course.setTitle(request.getTitle());
        course.setDescription(request.getDescription());
        course.setCategory(request.getCategory());
        course.setDurationHours(request.getDurationHours());
        course.setStatus("ACTIVE");

        return new TrainingCourseResponse(courseRepository.save(course));
    }

    public List<TrainingCourseResponse> getCourses() {
        return courseRepository.findAll().stream()
                .map(TrainingCourseResponse::new)
                .collect(Collectors.toList());
    }

    public Optional<TrainingCourseResponse> getCourseById(Long id) {
        return courseRepository.findById(id).map(TrainingCourseResponse::new);
    }

    @Transactional
    @CacheEvict(value = "trainingDashboard", allEntries = true)
    public Optional<TrainingCourseResponse> updateCourseStatus(Long id, String status) {
        return courseRepository.findById(id).map(course -> {
            course.setStatus(status.toUpperCase());
            course.setUpdatedAt(LocalDateTime.now());
            return new TrainingCourseResponse(courseRepository.save(course));
        });
    }

    // ── 3. SESSIONS ──────────────────────────────────────────────────────────
    @Transactional
    @CacheEvict(value = "trainingDashboard", allEntries = true)
    public TrainingSessionResponse createSession(TrainingSessionRequest request) {
        TrainingCourse course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new IllegalArgumentException("Course not found with ID: " + request.getCourseId()));

        TrainingSession session = new TrainingSession();
        session.setCourse(course);
        session.setTrainerName(request.getTrainerName());
        session.setScheduleDate(request.getScheduleDate());
        session.setStartTime(request.getStartTime());
        session.setEndTime(request.getEndTime());
        session.setLocation(request.getLocation());
        session.setCapacity(request.getCapacity());

        TrainingSession saved = sessionRepository.save(session);
        TrainingSessionResponse response = new TrainingSessionResponse(saved);
        response.setEnrolledCount(0);
        return response;
    }

    public List<TrainingSessionResponse> getSessions() {
        return sessionRepository.findAll().stream().map(session -> {
            TrainingSessionResponse resp = new TrainingSessionResponse(session);
            int enrolledCount = enrollmentRepository.findBySessionId(session.getId()).size();
            resp.setEnrolledCount(enrolledCount);
            return resp;
        }).collect(Collectors.toList());
    }

    // ── 4. ENROLLMENTS ───────────────────────────────────────────────────────
    @Transactional
    @CacheEvict(value = "trainingDashboard", allEntries = true)
    public TrainingEnrollmentResponse enrollEmployee(TrainingEnrollmentRequest request) {
        Employee emp = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(
                        () -> new IllegalArgumentException("Employee not found with ID: " + request.getEmployeeId()));
        TrainingSession session = sessionRepository.findById(request.getSessionId())
                .orElseThrow(
                        () -> new IllegalArgumentException("Session not found with ID: " + request.getSessionId()));

        // Enforce capacity check
        int currentEnrolled = enrollmentRepository.findBySessionId(session.getId()).size();
        if (session.getCapacity() != null && currentEnrolled >= session.getCapacity()) {
            throw new IllegalArgumentException("Session capacity has been reached.");
        }

        // Check if already enrolled in active status
        Optional<TrainingEnrollment> existing = enrollmentRepository.findByEmployeeIdAndSessionId(emp.getId(),
                session.getId());
        if (existing.isPresent() && !"WITHDRAWN".equals(existing.get().getStatus())) {
            throw new IllegalArgumentException("Employee is already enrolled in this session.");
        }

        TrainingEnrollment enrollment = existing.orElse(new TrainingEnrollment());
        enrollment.setEmployee(emp);
        enrollment.setSession(session);
        enrollment.setEnrollmentDate(LocalDateTime.now());
        enrollment.setStatus("ENROLLED");
        enrollment.setProgressPercent(0);
        enrollment.setGrade(null);
        enrollment.setUpdatedAt(LocalDateTime.now());

        return new TrainingEnrollmentResponse(enrollmentRepository.save(enrollment));
    }

    public List<TrainingEnrollmentResponse> getMyEnrollments(String employeeEmail) {
        Employee emp = employeeRepository.findByEmail(employeeEmail).orElse(null);
        if (emp == null)
            return List.of();

        return enrollmentRepository.findByEmployeeId(emp.getId()).stream().map(enrollment -> {
            TrainingEnrollmentResponse resp = new TrainingEnrollmentResponse(enrollment);
            certificateRepository.findByEnrollmentId(enrollment.getId())
                    .ifPresent(cert -> resp.setCertificateNumber(cert.getCertificateNumber()));
            return resp;
        }).collect(Collectors.toList());
    }

    @Transactional
    @CacheEvict(value = "trainingDashboard", allEntries = true)
    public Optional<TrainingEnrollmentResponse> withdrawEnrollment(Long enrollmentId) {
        return enrollmentRepository.findById(enrollmentId).map(enrollment -> {
            enrollment.setStatus("WITHDRAWN");
            enrollment.setProgressPercent(0);
            enrollment.setUpdatedAt(LocalDateTime.now());
            return new TrainingEnrollmentResponse(enrollmentRepository.save(enrollment));
        });
    }

    // ── 5. ATTENDANCE ────────────────────────────────────────────────────────
    @Transactional
    @CacheEvict(value = "trainingDashboard", allEntries = true)
    public Map<String, Object> submitAttendance(Long sessionId, TrainingAttendanceRequest request) {
        TrainingEnrollment enrollment = enrollmentRepository
                .findByEmployeeIdAndSessionId(request.getEmployeeId(), sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Enrollment not found for employee ID: "
                        + request.getEmployeeId() + " in session ID: " + sessionId));

        TrainingAttendance attendance = new TrainingAttendance();
        attendance.setEnrollment(enrollment);
        attendance.setAttendanceDate(request.getAttendanceDate());
        attendance.setStatus(request.getStatus().toUpperCase());
        attendanceRepository.save(attendance);

        // Update progress dynamically on attendance
        int totalDays = 1; // Simulated session length
        long attendedDays = attendanceRepository.findByEnrollmentId(enrollment.getId()).stream()
                .filter(a -> "PRESENT".equalsIgnoreCase(a.getStatus())).count();
        int progress = (int) Math.min(100, (attendedDays * 100) / totalDays);
        enrollment.setProgressPercent(progress);
        enrollmentRepository.save(enrollment);

        return Map.of(
                "attendanceId", attendance.getId(),
                "status", attendance.getStatus(),
                "progressPercent", progress);
    }

    // ── 6. ASSESSMENTS ───────────────────────────────────────────────────────
    @Transactional
    @CacheEvict(value = "trainingDashboard", allEntries = true)
    public Map<String, Object> submitAssessment(Long enrollmentId, TrainingAssessmentRequest request) {
        TrainingEnrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new IllegalArgumentException("Enrollment not found with ID: " + enrollmentId));

        TrainingAssessmentSubmission submission = new TrainingAssessmentSubmission();
        submission.setEnrollment(enrollment);
        submission.setScore(request.getScore());
        submission.setFeedback(request.getFeedback());
        submission.setStatus("GRADED");
        submissionRepository.save(submission);

        int score = request.getScore();
        String grade = score >= 90 ? "A" : score >= 80 ? "B" : score >= 70 ? "C" : "Fail";
        enrollment.setGrade(grade);
        enrollment.setProgressPercent(100);

        String certNum = null;
        if (score >= 70) {
            enrollment.setStatus("COMPLETED");

            // Auto-generate certificate
            Optional<TrainingCertificate> existingCert = certificateRepository.findByEnrollmentId(enrollment.getId());
            TrainingCertificate cert = existingCert.orElse(new TrainingCertificate());
            cert.setEnrollment(enrollment);
            cert.setIssueDate(LocalDate.now());
            if (cert.getCertificateNumber() == null) {
                cert.setCertificateNumber("CERT-" + System.currentTimeMillis() + "-" + enrollment.getId());
                cert.setFileUrl("/api/trainings/certificates/" + enrollment.getId() + "/download");
            }
            certificateRepository.save(cert);
            certNum = cert.getCertificateNumber();
        } else {
            enrollment.setStatus("FAILED");
        }
        enrollmentRepository.save(enrollment);

        return Map.of(
                "submissionId", submission.getId(),
                "score", score,
                "grade", grade,
                "status", enrollment.getStatus(),
                "certificateNumber", certNum != null ? certNum : "N/A");
    }

    // ── 7. CERTIFICATE ───────────────────────────────────────────────────────
    public Optional<TrainingCertificateResponse> getCertificate(Long enrollmentId) {
        return certificateRepository.findByEnrollmentId(enrollmentId).map(TrainingCertificateResponse::new);
    }

    // ── 8. REPORTS ───────────────────────────────────────────────────────────
    public Map<String, Object> getTrainingsReport(String reportType) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("reportType", reportType);
        data.put("generatedAt", LocalDateTime.now());

        long totalCourses = courseRepository.count();
        long totalEnrollments = enrollmentRepository.count();
        long completedEnrollments = enrollmentRepository.findByStatus("COMPLETED").size();

        data.put("totalCoursesCount", totalCourses);
        data.put("totalEnrollmentsCount", totalEnrollments);
        data.put("completedEnrollmentsCount", completedEnrollments);
        data.put("completionRate",
                totalEnrollments > 0
                        ? Math.round(((double) completedEnrollments / totalEnrollments) * 100.0 * 100.0) / 100.0
                        : 0.0);

        return data;
    }

    @Transactional
    @CacheEvict(value = "trainingDashboard", allEntries = true)
    public Optional<TrainingCourseResponse> updateCourse(Long id, TrainingCourseRequest request) {
        return courseRepository.findById(id).map(course -> {
            course.setTitle(request.getTitle());
            course.setDescription(request.getDescription());
            course.setCategory(request.getCategory());
            course.setDurationHours(request.getDurationHours());
            course.setUpdatedAt(LocalDateTime.now());
            return new TrainingCourseResponse(courseRepository.save(course));
        });
    }

    @Transactional
    @CacheEvict(value = "trainingDashboard", allEntries = true)
    public boolean deleteCourse(Long id) {
        if (courseRepository.existsById(id)) {
            // Delete dependent sessions first to prevent foreign key errors
            List<TrainingSession> sessions = sessionRepository.findAll().stream()
                    .filter(s -> s.getCourse().getId().equals(id))
                    .collect(Collectors.toList());
            for (TrainingSession session : sessions) {
                // Delete dependent enrollments, attendance, certificates for session
                List<TrainingEnrollment> enrolls = enrollmentRepository.findBySessionId(session.getId());
                for (TrainingEnrollment enroll : enrolls) {
                    certificateRepository.findByEnrollmentId(enroll.getId())
                            .ifPresent(c -> certificateRepository.delete(c));
                    submissionRepository.findAll().stream()
                            .filter(sub -> sub.getEnrollment().getId().equals(enroll.getId()))
                            .forEach(sub -> submissionRepository.delete(sub));
                    enrollmentRepository.delete(enroll);
                }
                attendanceRepository.findAll().stream()
                        .filter(att -> att.getEnrollment() != null
                                && att.getEnrollment().getSession().getId().equals(session.getId()))
                        .forEach(att -> attendanceRepository.delete(att));
                sessionRepository.delete(session);
            }
            courseRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
