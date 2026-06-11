package com.example.ems.recruitment.service;

import com.example.ems.auth.entity.Role;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.RoleRepository;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.recruitment.dto.CandidateRequest;
import com.example.ems.recruitment.dto.CandidateResponse;
import com.example.ems.recruitment.dto.InterviewFeedbackRequest;
import com.example.ems.recruitment.dto.InterviewRequest;
import com.example.ems.recruitment.dto.InterviewResponse;
import com.example.ems.recruitment.dto.JobRequest;
import com.example.ems.recruitment.dto.JobResponse;
import com.example.ems.recruitment.dto.OfferRequest;
import com.example.ems.recruitment.dto.OfferResponse;
import com.example.ems.recruitment.dto.RecruitmentDashboardResponse;
import com.example.ems.recruitment.entity.Candidate;
import com.example.ems.recruitment.entity.Interview;
import com.example.ems.recruitment.entity.Job;
import com.example.ems.recruitment.entity.Offer;
import com.example.ems.recruitment.repository.CandidateRepository;
import com.example.ems.recruitment.repository.InterviewRepository;
import com.example.ems.recruitment.repository.JobRepository;
import com.example.ems.recruitment.repository.OfferRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecruitmentService {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private CandidateRepository candidateRepository;

    @Autowired
    private InterviewRepository interviewRepository;

    @Autowired
    private OfferRepository offerRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    // ── 1. DASHBOARD ────────────────────────────────────────────────────────
    @Cacheable(value = "recruitmentDashboard", key = "'stats'")
    public RecruitmentDashboardResponse getDashboardStats() {
        RecruitmentDashboardResponse stats = new RecruitmentDashboardResponse();

        long totalJobs = jobRepository.count();
        long activeJobs = jobRepository.findByStatus("ACTIVE").size();
        long closedJobs = jobRepository.findByStatus("CLOSED").size();
        long draftJobs = jobRepository.findByStatus("DRAFT").size();

        long totalCandidates = candidateRepository.count();
        long applied = candidateRepository.findByStatus("APPLIED").size();
        long screening = candidateRepository.findByStatus("SCREENING").size();
        long interviewing = candidateRepository.findByStatus("INTERVIEWING").size();
        long offered = candidateRepository.findByStatus("OFFERED").size();
        long hired = candidateRepository.findByStatus("HIRED").size();
        long rejected = candidateRepository.findByStatus("REJECTED").size();

        long scheduledInterviews = interviewRepository.findByStatus("SCHEDULED").size();

        double conversionRate = totalCandidates > 0 
                ? ((double) hired / totalCandidates) * 100.0 
                : 0.0;

        stats.setTotalJobs(totalJobs);
        stats.setActiveJobs(activeJobs);
        stats.setClosedJobs(closedJobs);
        stats.setDraftJobs(draftJobs);
        stats.setTotalCandidates(totalCandidates);
        stats.setCandidatesApplied(applied);
        stats.setCandidatesScreening(screening);
        stats.setCandidatesInterviewing(interviewing);
        stats.setCandidatesOffered(offered);
        stats.setCandidatesHired(hired);
        stats.setCandidatesRejected(rejected);
        stats.setScheduledInterviewsCount(scheduledInterviews);
        stats.setConversionRate(Math.round(conversionRate * 100.0) / 100.0);

        return stats;
    }

    // ── 2. JOBS ─────────────────────────────────────────────────────────────
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "recruitmentDashboard", allEntries = true),
        @CacheEvict(value = "jobsList", allEntries = true)
    })
    public JobResponse createJob(JobRequest request) {
        Job job = new Job();
        job.setTitle(request.getTitle());
        job.setDepartment(request.getDepartment());
        job.setLocation(request.getLocation());
        job.setDescription(request.getDescription());
        job.setRequirements(request.getRequirements());
        job.setSalaryRange(request.getSalaryRange());
        
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            job.setStatus(request.getStatus().toUpperCase());
        } else {
            job.setStatus("DRAFT");
        }
        
        Job saved = jobRepository.save(job);
        return new JobResponse(saved);
    }

    @Cacheable(value = "jobsList", key = "'all'")
    public List<JobResponse> getJobs() {
        return jobRepository.findAll().stream()
                .map(JobResponse::new)
                .collect(Collectors.toList());
    }

    public Optional<JobResponse> getJobById(Long id) {
        return jobRepository.findById(id).map(JobResponse::new);
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "recruitmentDashboard", allEntries = true),
        @CacheEvict(value = "jobsList", allEntries = true)
    })
    public Optional<JobResponse> updateJobStatus(Long id, String status) {
        return jobRepository.findById(id).map(job -> {
            job.setStatus(status.toUpperCase());
            job.setUpdatedAt(LocalDateTime.now());
            return new JobResponse(jobRepository.save(job));
        });
    }

    // ── 3. CANDIDATES ───────────────────────────────────────────────────────
    @Transactional
    @CacheEvict(value = "recruitmentDashboard", allEntries = true)
    public CandidateResponse createCandidate(CandidateRequest request) {
        Job job = jobRepository.findById(request.getJobId())
                .orElseThrow(() -> new IllegalArgumentException("Job not found with ID: " + request.getJobId()));

        Candidate candidate = new Candidate();
        candidate.setFullName(request.getFullName());
        candidate.setEmail(request.getEmail());
        candidate.setPhone(request.getPhone());
        candidate.setJob(job);
        candidate.setStatus("APPLIED");

        Candidate saved = candidateRepository.save(candidate);
        return new CandidateResponse(saved);
    }

    public List<CandidateResponse> getCandidates() {
        return candidateRepository.findAll().stream()
                .map(CandidateResponse::new)
                .collect(Collectors.toList());
    }

    public Optional<CandidateResponse> getCandidateById(Long id) {
        return candidateRepository.findById(id).map(CandidateResponse::new);
    }

    @Transactional
    @CacheEvict(value = "recruitmentDashboard", allEntries = true)
    public Optional<CandidateResponse> updateCandidateStatus(Long id, String status) {
        return candidateRepository.findById(id).map(candidate -> {
            candidate.setStatus(status.toUpperCase());
            candidate.setUpdatedAt(LocalDateTime.now());
            return new CandidateResponse(candidateRepository.save(candidate));
        });
    }

    // ── 4. INTERVIEWS ───────────────────────────────────────────────────────
    @Transactional
    @CacheEvict(value = "recruitmentDashboard", allEntries = true)
    public InterviewResponse scheduleInterview(InterviewRequest request) {
        Candidate candidate = candidateRepository.findById(request.getCandidateId())
                .orElseThrow(() -> new IllegalArgumentException("Candidate not found with ID: " + request.getCandidateId()));

        Interview interview = new Interview();
        interview.setCandidate(candidate);
        interview.setJob(candidate.getJob());
        interview.setInterviewerName(request.getInterviewerName());
        interview.setInterviewDate(request.getInterviewDate());
        interview.setType(request.getType());
        interview.setStatus("SCHEDULED");

        Interview saved = interviewRepository.save(interview);

        // Transition candidate status
        candidate.setStatus("INTERVIEWING");
        candidate.setUpdatedAt(LocalDateTime.now());
        candidateRepository.save(candidate);

        return new InterviewResponse(saved);
    }

    public List<InterviewResponse> getInterviews() {
        return interviewRepository.findAll().stream()
                .map(InterviewResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional
    @CacheEvict(value = "recruitmentDashboard", allEntries = true)
    public Optional<InterviewResponse> addInterviewFeedback(Long interviewId, InterviewFeedbackRequest request) {
        return interviewRepository.findById(interviewId).map(interview -> {
            interview.setFeedback(request.getFeedback());
            interview.setRating(request.getRating());
            interview.setStatus("COMPLETED");
            
            // Also update candidate status if needed (e.g. SCREENING -> INTERVIEWING etc, but we leave it as is or default status updates)
            return new InterviewResponse(interviewRepository.save(interview));
        });
    }

    // ── 5. OFFERS ───────────────────────────────────────────────────────────
    @Transactional
    @CacheEvict(value = "recruitmentDashboard", allEntries = true)
    public OfferResponse createOffer(OfferRequest request) {
        Candidate candidate = candidateRepository.findById(request.getCandidateId())
                .orElseThrow(() -> new IllegalArgumentException("Candidate not found with ID: " + request.getCandidateId()));

        Offer offer = new Offer();
        offer.setCandidate(candidate);
        offer.setJob(candidate.getJob());
        offer.setOfferedSalary(request.getOfferedSalary());
        offer.setStartDate(request.getStartDate());
        offer.setStatus("SENT");

        Offer saved = offerRepository.save(offer);

        // Update candidate status
        candidate.setStatus("OFFERED");
        candidate.setUpdatedAt(LocalDateTime.now());
        candidateRepository.save(candidate);

        return new OfferResponse(saved);
    }

    @Transactional
    @CacheEvict(value = "recruitmentDashboard", allEntries = true)
    public Optional<OfferResponse> updateOfferStatus(Long offerId, String status) {
        return offerRepository.findById(offerId).map(offer -> {
            offer.setStatus(status.toUpperCase());
            offer.setUpdatedAt(LocalDateTime.now());
            
            // If offer accepted/declined, update candidate status accordingly
            if ("ACCEPTED".equalsIgnoreCase(status)) {
                offer.getCandidate().setStatus("OFFERED_ACCEPTED");
            } else if ("DECLINED".equalsIgnoreCase(status)) {
                offer.getCandidate().setStatus("REJECTED");
            }
            candidateRepository.save(offer.getCandidate());

            return new OfferResponse(offerRepository.save(offer));
        });
    }

    // ── 6. HIRE CANDIDATE (ONBOARDING) ──────────────────────────────────────
    @Transactional
    @CacheEvict(value = "recruitmentDashboard", allEntries = true)
    public Employee hireCandidate(Long candidateId) {
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new IllegalArgumentException("Candidate not found with ID: " + candidateId));

        if ("HIRED".equalsIgnoreCase(candidate.getStatus())) {
            throw new IllegalStateException("Candidate is already hired");
        }

        // Update candidate status
        candidate.setStatus("HIRED");
        candidate.setUpdatedAt(LocalDateTime.now());
        candidateRepository.save(candidate);

        // Check if employee with same email already exists
        if (employeeRepository.existsByEmail(candidate.getEmail())) {
            throw new IllegalStateException("An employee with email '" + candidate.getEmail() + "' already exists in the system.");
        }

        // Find standard salary details from Offer if available
        BigDecimal salary = BigDecimal.ZERO;
        LocalDate startDate = LocalDate.now();
        List<Offer> offers = offerRepository.findByCandidateId(candidateId);
        if (!offers.isEmpty()) {
            // Pick the latest offer or accepted offer
            Offer acceptedOffer = offers.stream()
                    .filter(o -> "ACCEPTED".equalsIgnoreCase(o.getStatus()))
                    .findFirst()
                    .orElse(offers.get(offers.size() - 1));
            salary = acceptedOffer.getOfferedSalary();
            startDate = acceptedOffer.getStartDate();
        }

        // 1. Create Employee Profile
        Employee employee = new Employee();
        employee.setFullName(candidate.getFullName());
        employee.setEmail(candidate.getEmail());
        employee.setPhone(candidate.getPhone());
        employee.setDepartment(candidate.getJob().getDepartment());
        employee.setDesignation(candidate.getJob().getTitle());
        employee.setAnnualSalary(salary);
        employee.setJoiningDate(startDate);
        employee.setLocation(candidate.getJob().getLocation() != null ? candidate.getJob().getLocation() : "Remote");
        employee.setEmploymentType("FULL_TIME");
        employee.setStatus("ACTIVE");

        Employee savedEmployee = employeeRepository.save(employee);

        // Generate unique Employee ID
        String employeeId = "EMP" + String.format("%03d", savedEmployee.getId());
        savedEmployee.setEmployeeId(employeeId);
        employeeRepository.save(savedEmployee);

        // 2. Create User Account (if not already existing)
        if (!userRepository.existsByWorkEmail(candidate.getEmail())) {
            User user = new User();
            user.setFullName(candidate.getFullName());
            user.setWorkEmail(candidate.getEmail());
            user.setMobileNumber(candidate.getPhone());
            user.setEmployeeId(employeeId);
            user.setDepartment(candidate.getJob().getDepartment());
            user.setRequestedRole("EMPLOYEE");
            
            // Assign EMPLOYEE role
            Role employeeRole = roleRepository.findByName("EMPLOYEE")
                    .orElseGet(() -> {
                        Role r = new Role();
                        r.setName("EMPLOYEE");
                        r.setDescription("Role for EMPLOYEE");
                        return roleRepository.save(r);
                    });
            user.setRole(employeeRole);
            user.setLocation(savedEmployee.getLocation());
            user.setStatus("ACTIVE");
            // Set temp secure password
            user.setPassword(passwordEncoder.encode("Onboard@123"));
            
            userRepository.save(user);
            user.setUserId(employeeId);
            userRepository.save(user);
        }

        return savedEmployee;
    }

    // ── 7. REPORTS ──────────────────────────────────────────────────────────
    public Map<String, Object> getReportData(String reportType) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("reportType", reportType);
        data.put("generatedAt", LocalDateTime.now());

        if ("conversion".equalsIgnoreCase(reportType) || "conversion-rate".equalsIgnoreCase(reportType)) {
            long total = candidateRepository.count();
            long hired = candidateRepository.findByStatus("HIRED").size();
            long rejected = candidateRepository.findByStatus("REJECTED").size();
            double conversion = total > 0 ? ((double) hired / total) * 100.0 : 0.0;

            data.put("totalCandidates", total);
            data.put("hiredCandidates", hired);
            data.put("rejectedCandidates", rejected);
            data.put("conversionRatePercentage", Math.round(conversion * 100.0) / 100.0);
        } else if ("department".equalsIgnoreCase(reportType) || "department-stats".equalsIgnoreCase(reportType)) {
            List<Job> jobs = jobRepository.findAll();
            Map<String, Long> jobsByDept = jobs.stream()
                    .collect(Collectors.groupingBy(Job::getDepartment, Collectors.counting()));
            data.put("jobsCountByDepartment", jobsByDept);
        } else {
            // Default summary report
            data.put("totalActiveJobs", jobRepository.findByStatus("ACTIVE").size());
            data.put("totalCandidatesApplied", candidateRepository.count());
            data.put("interviewsScheduled", interviewRepository.findByStatus("SCHEDULED").size());
        }

        return data;
    }

    // ── 8. NOTIFICATIONS ────────────────────────────────────────────────────
    public Map<String, Object> triggerNotification(Map<String, Object> notificationRequest) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", "SENT");
        result.put("notifiedAt", LocalDateTime.now());
        result.put("channel", notificationRequest.getOrDefault("channel", "EMAIL"));
        result.put("recipient", notificationRequest.get("recipient"));
        result.put("subject", notificationRequest.get("subject"));
        result.put("body", notificationRequest.get("body"));
        return result;
    }

    // ── 9. RESUME UPLOAD / DOWNLOAD ──────────────────────────────────────────
    @Transactional
    @CacheEvict(value = "recruitmentDashboard", allEntries = true)
    public CandidateResponse uploadResume(Long candidateId, String fileName, String contentType, byte[] data) {
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new IllegalArgumentException("Candidate not found with ID: " + candidateId));

        candidate.setResumeFileName(fileName);
        candidate.setResumeFileType(contentType);
        candidate.setResumeData(data);
        candidate.setResumeUrl("http://localhost:8080/api/recruitments/candidates/" + candidateId + "/resume");
        candidate.setUpdatedAt(LocalDateTime.now());

        return new CandidateResponse(candidateRepository.save(candidate));
    }

    public Optional<Candidate> getResumeData(Long candidateId) {
        return candidateRepository.findById(candidateId);
    }

    public List<CandidateResponse> getCandidatesByJobId(Long jobId) {
        return candidateRepository.findByJobId(jobId).stream()
                .map(CandidateResponse::new)
                .collect(Collectors.toList());
    }
}
