package com.example.ems.expense.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.expense.dto.*;
import com.example.ems.expense.entity.*;
import com.example.ems.expense.repository.ExpenseRepository;
import com.example.ems.expense.repository.MyExpenseReceiptRepository;
import com.example.ems.expense.service.MyExpenseService;
import com.example.ems.security.service.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/my-expenses")
@CrossOrigin("*")
@Tag(name = "Employee Self Service - Expenses")
public class MyExpenseController {

    @Autowired
    private MyExpenseService expenseService;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private MyExpenseReceiptRepository receiptRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private RoleService roleService;

    private User resolveUser(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtService.validateAccessToken(token)) {
                String email = jwtService.getEmailFromToken(token);
                return userRepository.findByWorkEmail(email).orElse(null);
            }
        }
        return null;
    }

    private Employee resolveEmployee(User user) {
        if (user == null) return null;
        return employeeRepository.findByEmail(user.getWorkEmail()).orElse(null);
    }

    private boolean checkPermission(User user, String permission) {
        if (user == null) return false;
        if (roleService.hasPermission(user.getWorkEmail(), permission)
                || roleService.hasPermission(user.getWorkEmail(), "expense.manage")
                || roleService.isSuperAdmin(user.getWorkEmail())) {
            return true;
        }
        if (permission.startsWith("expense.self.read") || permission.equals("expense.self.timeline.read")) {
            return roleService.hasPermission(user.getWorkEmail(), "employee.expense.read")
                    || roleService.hasPermission(user.getWorkEmail(), "expense.self.read");
        }
        if (permission.equals("expense.self.create") || permission.equals("expense.self.receipt.upload")) {
            return roleService.hasPermission(user.getWorkEmail(), "employee.expense.create");
        }
        if (permission.equals("expense.self.update") || permission.equals("expense.self.withdraw")) {
            return roleService.hasPermission(user.getWorkEmail(), "employee.expense.update");
        }
        return false;
    }

    private boolean isExpenseOwnerOrAdmin(User currentUser, Employee employee, Expense expense) {
        if (expense.getEmployee() != null && expense.getEmployee().getId().equals(employee.getId())) {
            return true;
        }
        return roleService.hasPermission(currentUser.getWorkEmail(), "expense.manage")
                || roleService.isSuperAdmin(currentUser.getWorkEmail());
    }

    private boolean isReceiptOwnerOrAdmin(User currentUser, Employee employee, MyExpenseReceipt receipt) {
        if (receipt.getEmployee() != null && receipt.getEmployee().getId().equals(employee.getId())) {
            return true;
        }
        return roleService.hasPermission(currentUser.getWorkEmail(), "expense.manage")
                || roleService.isSuperAdmin(currentUser.getWorkEmail());
    }

    private ResponseEntity<ErrorResponse> unauthorizedResponse() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
    }

    private ResponseEntity<ErrorResponse> forbiddenResponse(String permission) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ErrorResponse.error("Access Denied: Requires '" + permission + "' permission.", "AUTH_002"));
    }



    // 2. Get My Expense List
    @Operation(summary = "Get My Expenses", description = "Retrieves a paginated list of expense claims for the logged-in employee, with optional filters for status, category, and date range.")
    @GetMapping
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getMyExpenses(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "expenseDate,desc") String sort){

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) return (ResponseEntity) unauthorizedResponse();
        if (!checkPermission(currentUser, "expense.self.read")) return (ResponseEntity) forbiddenResponse("expense.self.read");

        Employee employee = resolveEmployee(currentUser);
        if (employee == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Employee profile not found.", "EMP_404"));
        }

        String statusParam = (status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status)) ? status.trim().toUpperCase() : null;
        String categoryParam = (category != null && !category.isBlank() && !"ALL".equalsIgnoreCase(category)) ? category.trim().toUpperCase() : null;
        LocalDate fromDateParam = (fromDate != null && !fromDate.isBlank()) ? LocalDate.parse(fromDate.trim()) : null;
        LocalDate toDateParam = (toDate != null && !toDate.isBlank()) ? LocalDate.parse(toDate.trim()) : null;

        Sort sortObj = Sort.unsorted();
        if (sort != null && !sort.isBlank()) {
            String[] parts = sort.split(",");
            String property = parts[0];
            Sort.Direction direction = Sort.Direction.DESC;
            if (parts.length > 1 && "asc".equalsIgnoreCase(parts[1])) {
                direction = Sort.Direction.ASC;
            }
            sortObj = Sort.by(direction, property);
        }

        Pageable pageable = PageRequest.of(page, size, sortObj);
        MyExpenseListResponse response = expenseService.getMyExpenses(employee, statusParam, categoryParam, fromDateParam, toDateParam, pageable);
        return ResponseEntity.ok(ApiResponse.success("Expense claims retrieved successfully", response));
    }

    // 3. Create Expense Claim
    @Operation(summary = "Create Expense Claim", description = "Submits a new expense reimbursement claim.")
    @PostMapping
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> createExpense(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @Valid @RequestBody CreateExpenseRequest request){

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) return (ResponseEntity) unauthorizedResponse();
        if (!checkPermission(currentUser, "expense.self.create")) return (ResponseEntity) forbiddenResponse("expense.self.create");

        Employee employee = resolveEmployee(currentUser);
        if (employee == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Employee profile not found.", "EMP_404"));
        }

        try {
            CreateExpenseResponse response = expenseService.createExpense(request, employee);
            return ResponseEntity.ok(ApiResponse.success("Expense claim submitted successfully", response));
        } catch (Exception e) {
            return (ResponseEntity) ResponseEntity.badRequest()
                    .body(ErrorResponse.error(e.getMessage(), "EXP_500"));
        }
    }

    // 4. Get Expense Details
    @Operation(summary = "Get Expense Details", description = "Retrieves details of a specific expense claim by ID.")
    @GetMapping("/{expenseId}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getExpenseDetails(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable("expenseId") Long expenseId){

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) return (ResponseEntity) unauthorizedResponse();
        if (!checkPermission(currentUser, "expense.self.read")) return (ResponseEntity) forbiddenResponse("expense.self.read");

        Employee employee = resolveEmployee(currentUser);
        if (employee == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Employee profile not found.", "EMP_404"));
        }

        Optional<Expense> expOpt = expenseRepository.findById(expenseId);
        if (expOpt.isEmpty()) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Expense claim not found with ID: " + expenseId, "EXP_404"));
        }

        if (!isExpenseOwnerOrAdmin(currentUser, employee, expOpt.get())) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: You do not own this claim.", "EXP_403"));
        }

        try {
            ExpenseDetailsResponse response = expenseService.getExpenseDetails(expenseId, employee);
            return ResponseEntity.ok(ApiResponse.success("Expense claim details retrieved successfully", response));
        } catch (Exception e) {
            return (ResponseEntity) ResponseEntity.badRequest()
                    .body(ErrorResponse.error(e.getMessage(), "EXP_500"));
        }
    }

    // 5. Update Expense Claim
    @Operation(summary = "Update Expense Claim", description = "Updates the details of a pending expense claim.")
    @PutMapping("/{expenseId}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> updateExpense(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable("expenseId") Long expenseId,
            @Valid @RequestBody UpdateExpenseRequest request){

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) return (ResponseEntity) unauthorizedResponse();
        if (!checkPermission(currentUser, "expense.self.update")) return (ResponseEntity) forbiddenResponse("expense.self.update");

        Employee employee = resolveEmployee(currentUser);
        if (employee == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Employee profile not found.", "EMP_404"));
        }

        Optional<Expense> expOpt = expenseRepository.findById(expenseId);
        if (expOpt.isEmpty()) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Expense claim not found with ID: " + expenseId, "EXP_404"));
        }

        if (!isExpenseOwnerOrAdmin(currentUser, employee, expOpt.get())) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: You do not own this claim.", "EXP_403"));
        }

        try {
            UpdateExpenseResponse response = expenseService.updateExpense(expenseId, request, employee);
            return ResponseEntity.ok(ApiResponse.success("Expense updated successfully", response));
        } catch (Exception e) {
            return (ResponseEntity) ResponseEntity.badRequest()
                    .body(ErrorResponse.error(e.getMessage(), "EXP_500"));
        }
    }

    // 6. Withdraw Expense Claim
    @Operation(summary = "Withdraw Expense Claim", description = "Withdraws a submitted expense claim from approval workflow.")
    @PatchMapping("/{expenseId}/withdraw")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> withdrawExpense(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable("expenseId") Long expenseId,
            @Valid @RequestBody WithdrawExpenseRequest request){

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) return (ResponseEntity) unauthorizedResponse();
        if (!checkPermission(currentUser, "expense.self.withdraw")) return (ResponseEntity) forbiddenResponse("expense.self.withdraw");

        Employee employee = resolveEmployee(currentUser);
        if (employee == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Employee profile not found.", "EMP_404"));
        }

        Optional<Expense> expOpt = expenseRepository.findById(expenseId);
        if (expOpt.isEmpty()) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Expense claim not found with ID: " + expenseId, "EXP_404"));
        }

        if (!isExpenseOwnerOrAdmin(currentUser, employee, expOpt.get())) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: You do not own this claim.", "EXP_403"));
        }

        try {
            WithdrawExpenseResponse response = expenseService.withdrawExpense(expenseId, request, employee);
            return ResponseEntity.ok(ApiResponse.success("Expense claim withdrawn successfully", response));
        } catch (Exception e) {
            return (ResponseEntity) ResponseEntity.badRequest()
                    .body(ErrorResponse.error(e.getMessage(), "EXP_500"));
        }
    }

    // 7. Upload Expense Receipt
    @Operation(summary = "Upload Receipt", description = "Uploads a receipt document for expense verification.")
    @PostMapping(value = "/receipts", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> uploadReceipt(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "receiptType", required = false) String receiptType){

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) return (ResponseEntity) unauthorizedResponse();
        if (!checkPermission(currentUser, "expense.self.receipt.upload")) return (ResponseEntity) forbiddenResponse("expense.self.receipt.upload");

        Employee employee = resolveEmployee(currentUser);
        if (employee == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Employee profile not found.", "EMP_404"));
        }

        try {
            UploadReceiptResponse response = expenseService.uploadReceipt(file, receiptType, employee);
            return ResponseEntity.ok(ApiResponse.success("Receipt uploaded successfully", response));
        } catch (Exception e) {
            return (ResponseEntity) ResponseEntity.badRequest()
                    .body(ErrorResponse.error(e.getMessage(), "EXP_500"));
        }
    }

    // 8. Download Receipt
    @Operation(summary = "Download Receipt", description = "Downloads the file data of an uploaded expense receipt.")
    @GetMapping("/receipts/{receiptId}/download")
    public ResponseEntity<?> downloadReceipt(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable("receiptId") Long receiptId){

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) return unauthorizedResponse();
        if (!checkPermission(currentUser, "expense.self.read")) return forbiddenResponse("expense.self.read");

        Employee employee = resolveEmployee(currentUser);
        if (employee == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Employee profile not found.", "EMP_404"));
        }

        Optional<MyExpenseReceipt> receiptOpt = receiptRepository.findById(receiptId);
        if (receiptOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Receipt not found with ID: " + receiptId, "EXP_404"));
        }

        if (!isReceiptOwnerOrAdmin(currentUser, employee, receiptOpt.get())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: You do not own this receipt.", "EXP_403"));
        }

        try {
            MyExpenseReceipt doc = expenseService.downloadReceipt(receiptId);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(doc.getFileType()));
            headers.setContentDispositionFormData("attachment", doc.getFileName());
            headers.setContentLength(doc.getFileData().length);
            return new ResponseEntity<>(doc.getFileData(), headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.error(e.getMessage(), "EXP_500"));
        }
    }

    // 9. Get Expense Timeline
    @Operation(summary = "Get Expense Timeline", description = "Retrieves the timeline/audit log of events for a specific expense claim.")
    @GetMapping("/{expenseId}/timeline")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getExpenseTimeline(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable("expenseId") Long expenseId){

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) return (ResponseEntity) unauthorizedResponse();
        if (!checkPermission(currentUser, "expense.self.timeline.read")) return (ResponseEntity) forbiddenResponse("expense.self.timeline.read");

        Employee employee = resolveEmployee(currentUser);
        if (employee == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Employee profile not found.", "EMP_404"));
        }

        Optional<Expense> expOpt = expenseRepository.findById(expenseId);
        if (expOpt.isEmpty()) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Expense claim not found with ID: " + expenseId, "EXP_404"));
        }

        if (!isExpenseOwnerOrAdmin(currentUser, employee, expOpt.get())) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: You do not own this claim.", "EXP_403"));
        }

        try {
            ExpenseTimelineResponse response = expenseService.getExpenseTimeline(expenseId, employee);
            return ResponseEntity.ok(ApiResponse.success("Timeline events retrieved successfully", response));
        } catch (Exception e) {
            return (ResponseEntity) ResponseEntity.badRequest()
                    .body(ErrorResponse.error(e.getMessage(), "EXP_500"));
        }
    }

    // 10. Get Expense Categories
    @Operation(summary = "Get Expense Categories", description = "Retrieves all active expense categories.")
    @GetMapping("/categories")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getCategories(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) return (ResponseEntity) unauthorizedResponse();
        if (!checkPermission(currentUser, "expense.self.read")) return (ResponseEntity) forbiddenResponse("expense.self.read");

        ExpenseCategoriesResponse response = expenseService.getCategories();
        return ResponseEntity.ok(ApiResponse.success("Categories retrieved successfully", response));
    }

    // 11. Get Expense Policy
    @Operation(summary = "Get Expense Policies", description = "Retrieves active company expense policies.")
    @GetMapping("/policies")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getPolicies(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) return (ResponseEntity) unauthorizedResponse();
        if (!checkPermission(currentUser, "expense.self.read")) return (ResponseEntity) forbiddenResponse("expense.self.read");

        ExpensePoliciesResponse response = expenseService.getPolicies();
        return ResponseEntity.ok(ApiResponse.success("Expense policies retrieved successfully", response));
    }
}
