package com.example.ems.organization.controller;

import com.example.ems.auth.entity.Role;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.organization.dto.PaymentDtos.*;
import com.example.ems.organization.service.PaymentService;
import com.example.ems.security.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class PaymentControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PaymentService paymentService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleService roleService;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private PaymentController controller;

    private static final String TOKEN = "admin-token";
    private static final String AUTH_HEADER = "Bearer " + TOKEN;
    private static final String EMAIL = "platform.admin@example.com";
    private final ObjectMapper mapper = new ObjectMapper().registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    private User adminUser;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        adminUser = new User();
        adminUser.setWorkEmail(EMAIL);
        Role role = new Role();
        role.setName("SUPER_ADMIN");
        adminUser.setRole(role);

        when(jwtService.validateAccessToken(TOKEN)).thenReturn(true);
        when(jwtService.getEmailFromToken(TOKEN)).thenReturn(EMAIL);
        when(userRepository.findByWorkEmail(EMAIL)).thenReturn(Optional.of(adminUser));
        when(roleService.hasPermission(eq(EMAIL), any())).thenReturn(true);
    }

    @Test
    public void testCreateOrder() throws Exception {
        CreateOrderRequest request = new CreateOrderRequest(1L, "RAZORPAY");
        PaymentOrderResponse response = new PaymentOrderResponse(
                new PaymentOrderResponse.PaymentDto(1L, "CREATED", "RAZORPAY", "order_123", "INR", new BigDecimal(5700000), new BigDecimal(57000), "EMS-2026-000001"),
                new PaymentOrderResponse.InvoiceDto(1L, "EMS-2026-000001", "PENDING"),
                new PaymentOrderResponse.GatewayDto("rzp_live_123", "2026-07-01T11:30:10Z")
        );

        when(paymentService.createPaymentOrder(any(), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/platform-admin/payments/orders")
                        .header("Authorization", AUTH_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.payment.gatewayOrderId").value("order_123"))
                .andExpect(jsonPath("$.data.invoice.invoiceNumber").value("EMS-2026-000001"));
    }

    @Test
    public void testVerifyPayment() throws Exception {
        VerifyPaymentRequest request = new VerifyPaymentRequest("order_123", "pay_ABC", "sig_XYZ", "UPI");
        VerifyPaymentResponse response = new VerifyPaymentResponse(
                new VerifyPaymentResponse.PaymentVerifyDto(1L, "RAZORPAY", "pay_ABC", "SUCCESS", "UPI", "2026-07-01T11:03:22Z"),
                new VerifyPaymentResponse.InvoiceVerifyDto(1L, "EMS-2026-000001", "PAID"),
                new VerifyPaymentResponse.SubscriptionVerifyDto(1L, "ACTIVE", "2026-07-01T11:03:23Z")
        );

        when(paymentService.verifyPayment(any(), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/platform-admin/payments/verify")
                        .header("Authorization", AUTH_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.payment.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.subscription.status").value("ACTIVE"));
    }

    @Test
    public void testGetPaymentDetails() throws Exception {
        PaymentDetailResponse response = new PaymentDetailResponse(
                1L, "RAZORPAY", "order_123", "pay_ABC", "EMS-2026-000001",
                new PaymentDetailResponse.OrganizationSummary(1L, "Acme Enterprise"),
                new PaymentDetailResponse.AmountDto("INR", new BigDecimal(57000)),
                "UPI", "SUCCESS", "2026-07-01T11:03:22Z"
        );

        when(paymentService.getPaymentDetails(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/platform-admin/payments/1")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.paymentId").value(1L))
                .andExpect(jsonPath("$.data.amount.value").value(57000));
    }

    @Test
    public void testGetPaymentHistory() throws Exception {
        PaymentHistoryResponse response = new PaymentHistoryResponse(
                Collections.singletonList(new PaymentHistoryItem(1L, "EMS-2026-000001", "Acme Enterprise", "RAZORPAY", new BigDecimal(57000), "INR", "SUCCESS", "2026-07-01T11:03:22Z")),
                new PaymentHistoryResponse.PaginationDto(1, 20, 1L, 1, false, false)
        );

        when(paymentService.getPaymentHistory(0, 20)).thenReturn(response);

        mockMvc.perform(get("/api/v1/platform-admin/payments")
                        .header("Authorization", AUTH_HEADER)
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].paymentId").value(1L));
    }

    @Test
    public void testRefundPayment() throws Exception {
        RefundPaymentRequest request = new RefundPaymentRequest(new BigDecimal(10000), "Subscription downgraded");
        RefundResponse response = new RefundResponse(
                301L, 1L, "rfnd_998", "EMS-2026-000001", new BigDecimal(10000), "INR", "PROCESSING", "2026-07-01T12:15:10Z", "Subscription downgraded"
        );

        when(paymentService.refundPayment(eq(1L), any(), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/platform-admin/payments/1/refund")
                        .header("Authorization", AUTH_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.refundId").value(301L))
                .andExpect(jsonPath("$.data.status").value("PROCESSING"));
    }

    @Test
    public void testHandleWebhook() throws Exception {
        String payload = "{\"event\":\"payment.captured\",\"payload\":{\"payment\":{\"entity\":{\"id\":\"pay_123\",\"order_id\":\"order_123\",\"method\":\"upi\"}}}}";
        String signature = "sig_mock_signature";

        mockMvc.perform(post("/api/v1/platform-admin/payments/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Razorpay-Signature", signature)
                        .content(payload))
                .andExpect(status().isOk());
    }
}
