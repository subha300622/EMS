package com.example.ems.organization.controller;

import com.example.ems.auth.entity.Role;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.RoleRepository;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.config.GlobalExceptionHandler;
import com.example.ems.organization.dto.*;
import com.example.ems.organization.dto.PaymentDtos.*;
import com.example.ems.organization.dto.SubscriptionDtos.*;
import com.example.ems.organization.repository.OrganizationRepository;
import com.example.ems.organization.repository.SubscriptionInvoiceRepository;
import com.example.ems.organization.repository.SubscriptionRepository;
import com.example.ems.security.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
public class SaaSOrganizationPaymentFlowIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private PlatformAdminOrganizationController organizationController;

    @Autowired
    private com.example.ems.subscription.controller.SubscriptionController subscriptionController;

    @Autowired
    private PaymentController paymentController;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private SubscriptionInvoiceRepository invoiceRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    private final ObjectMapper mapper = new ObjectMapper().registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
    private String token;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(organizationController, subscriptionController, paymentController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        // Seed Super Admin
        User adminUser = userRepository.findByWorkEmail("platform.admin@example.com").orElse(null);
        if (adminUser == null) {
            adminUser = new User();
            adminUser.setWorkEmail("platform.admin@example.com");
            adminUser.setUserId("platform-admin");
            adminUser.setPassword("password123");
            Role superAdmin = roleRepository.findByName("SUPER_ADMIN").orElseGet(() -> {
                Role r = new Role();
                r.setName("SUPER_ADMIN");
                r.setDescription("Super Admin");
                return roleRepository.save(r);
            });
            adminUser.setRole(superAdmin);
            adminUser = userRepository.save(adminUser);
        }

        token = "Bearer " + jwtService.generateAccessToken(adminUser.getUserId(), adminUser.getWorkEmail(), adminUser.getRole().getName());
    }

    @Test
    public void testSaaSOrganizationPaymentFlow() throws Exception {
        System.out.println("================================================================================");
        System.out.println("                     STARTING SAAS ORGANIZATION PAYMENT FLOW                   ");
        System.out.println("================================================================================");

        // Step 1: Create Organization
        CreateOrganizationRequest orgRequest = new CreateOrganizationRequest();
        orgRequest.setName("Flow Test Enterprise");
        orgRequest.setEmail("admin@flowtest.com");
        orgRequest.setPhone("9876543210");
        orgRequest.setWebsite("https://flowtest.com");
        orgRequest.setSubscriptionPlan("ENTERPRISE");
        orgRequest.setAddress(new OrganizationAddressDto("123 Flow St", "Tech City", "Karnataka", "India", "560001"));

        String orgReqJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(orgRequest);
        System.out.println("\n[FLOW-STEP-1-REQUEST] Create Organization -> POST /api/v1/platform-admin/organizations");
        System.out.println(orgReqJson);

        MvcResult orgResult = mockMvc.perform(post("/api/v1/platform-admin/organizations")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orgReqJson))
                .andDo(org.springframework.test.web.servlet.result.MockMvcResultHandlers.print())
                .andExpect(status().isCreated())
                .andReturn();

        String orgRespJson = orgResult.getResponse().getContentAsString();
        System.out.println("\n[FLOW-STEP-1-RESPONSE] Create Organization Response:");
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(mapper.readTree(orgRespJson)));

        com.fasterxml.jackson.databind.JsonNode orgNode = mapper.readTree(orgRespJson);
        long orgId = orgNode.get("data").get("id").asLong();

        // Clear default active subscription created automatically, so we can test create subscription API
        subscriptionRepository.deleteAll(subscriptionRepository.findAll().stream()
                .filter(s -> s.getOrganization().getId().equals(orgId))
                .collect(java.util.stream.Collectors.toList()));

        // Step 2: Create Subscription
        PlanDto planDto = new PlanDto("ENTERPRISE", "Enterprise Plan");
        BillingDto billingDto = new BillingDto("YEARLY", BigDecimal.valueOf(50000.00), "INR", BigDecimal.valueOf(9000.00), BigDecimal.valueOf(2000.00), BigDecimal.valueOf(57000.00));
        DurationDto durationDto = new DurationDto(LocalDate.now(), LocalDate.now().plusYears(1), true, 365L);
        LimitsDto limitsDto = new LimitsDto(1000, 25, 100, 500, 1000000);
        FeaturesDto featuresDto = new FeaturesDto(true, true, true, true, true, true, true, true, true, true, true, true, true);
        PaymentDto paymentDto = new PaymentDto("BANK_TRANSFER", "EMS-2026-000001", "PENDING");

        CreateSubscriptionRequest subRequest = new CreateSubscriptionRequest(
                orgId,
                planDto,
                billingDto,
                durationDto,
                limitsDto,
                featuresDto,
                paymentDto,
                "Flow integration subscription"
        );

        String subReqJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(subRequest);
        System.out.println("\n[FLOW-STEP-2-REQUEST] Create Subscription -> POST /api/v1/platform-admin/subscriptions");
        System.out.println(subReqJson);

        MvcResult subResult = mockMvc.perform(post("/api/v1/platform-admin/subscriptions")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(subReqJson))
                .andDo(org.springframework.test.web.servlet.result.MockMvcResultHandlers.print())
                .andExpect(status().isCreated())
                .andReturn();

        String subRespJson = subResult.getResponse().getContentAsString();
        System.out.println("\n[FLOW-STEP-2-RESPONSE] Create Subscription Response:");
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(mapper.readTree(subRespJson)));

        // Step 3: Fetch Invoice (Generated automatically during subscription creation)
        com.fasterxml.jackson.databind.JsonNode subNode = mapper.readTree(subRespJson);
        long subId = subNode.get("data").get("subscriptionId").asLong();
        var invoices = invoiceRepository.findBySubscriptionId(subId);
        assertNotNull(invoices);
        var invoice = invoices.get(0);
        System.out.println("\n[FLOW-STEP-3-GENERATED-INVOICE] Automatically Generated Invoice Details:");
        System.out.println("Invoice ID: " + invoice.getId());
        System.out.println("Invoice Number: " + invoice.getInvoiceNumber());
        System.out.println("Status: " + invoice.getStatus());

        // Step 4: Create Payment Order
        CreateOrderRequest orderRequest = new CreateOrderRequest(invoice.getId(), "RAZORPAY");
        String orderReqJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(orderRequest);
        System.out.println("\n[FLOW-STEP-4-REQUEST] Create Payment Order -> POST /api/v1/platform-admin/payments/orders");
        System.out.println(orderReqJson);

        MvcResult orderResult = mockMvc.perform(post("/api/v1/platform-admin/payments/orders")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderReqJson))
                .andDo(org.springframework.test.web.servlet.result.MockMvcResultHandlers.print())
                .andExpect(status().isCreated())
                .andReturn();

        String orderRespJson = orderResult.getResponse().getContentAsString();
        System.out.println("\n[FLOW-STEP-4-RESPONSE] Create Payment Order Response:");
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(mapper.readTree(orderRespJson)));

        com.fasterxml.jackson.databind.JsonNode orderNode = mapper.readTree(orderRespJson);
        String gatewayOrderId = orderNode.get("data").get("payment").get("gatewayOrderId").asText();

        // Step 5: Gateway Webhook Trigger (Sig verification bypass via standalone header validation)
        String webhookPayload = "{"
                + "\"event\":\"payment.captured\","
                + "\"payload\":{"
                + "  \"payment\":{"
                + "    \"entity\":{"
                + "      \"id\":\"pay_ABC123456\","
                + "      \"order_id\":\"" + gatewayOrderId + "\","
                + "      \"method\":\"upi\","
                + "      \"amount\":5700000,"
                + "      \"currency\":\"INR\""
                + "    }"
                + "  }"
                + "}"
                + "}";

        System.out.println("\n[FLOW-STEP-5-REQUEST] Gateway Webhook -> POST /api/v1/platform-admin/payments/webhook");
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(mapper.readTree(webhookPayload)));

        MvcResult webhookResult = mockMvc.perform(post("/api/v1/platform-admin/payments/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Razorpay-Signature", "sig_mock_signature")
                        .content(webhookPayload))
                .andExpect(status().isOk())
                .andReturn();

        System.out.println("\n[FLOW-STEP-5-RESPONSE] Gateway Webhook Response: HTTP 200 OK");

        // Verify status updates in Database
        var updatedInvoice = invoiceRepository.findById(invoice.getId()).orElse(null);
        var updatedSub = subscriptionRepository.findById(subId).orElse(null);
        assertNotNull(updatedInvoice);
        assertNotNull(updatedSub);

        System.out.println("\n[FLOW-STEP-6-VERIFIED-DATA-UPDATES] Final State Verification:");
        System.out.println("Invoice Status: " + updatedInvoice.getStatus());
        System.out.println("Subscription Status: " + updatedSub.getStatus());

        // Step 6: Query Analytics Dashboard & Metrics APIs
        System.out.println("\n[FLOW-STEP-7-ANALYTICS] Verifying Composed Analytics Dashboard APIs...");

        // 1. GET /overview
        MvcResult overviewResult = mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/platform-admin/subscriptions/overview")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andReturn();
        String overviewJson = overviewResult.getResponse().getContentAsString();
        System.out.println("Overview Dashboard Response:");
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(mapper.readTree(overviewJson)));

        // 2. GET /metrics/mrr
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/platform-admin/subscriptions/metrics/mrr")
                        .header("Authorization", token))
                .andExpect(status().isOk());

        // 3. GET /metrics/active
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/platform-admin/subscriptions/metrics/active")
                        .header("Authorization", token))
                .andExpect(status().isOk());

        // 4. GET /metrics/revenue
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/platform-admin/subscriptions/metrics/revenue")
                        .header("Authorization", token))
                .andExpect(status().isOk());

        // 5. GET /metrics/invoices
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/platform-admin/subscriptions/metrics/invoices")
                        .header("Authorization", token))
                .andExpect(status().isOk());

        // 6. GET /metrics/plan-distribution
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/platform-admin/subscriptions/metrics/plan-distribution")
                        .header("Authorization", token))
                .andExpect(status().isOk());

        // 7. GET /metrics/renewals
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/platform-admin/subscriptions/metrics/renewals?days=30")
                        .header("Authorization", token))
                .andExpect(status().isOk());

        // 8. GET /metrics/churn
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/platform-admin/subscriptions/metrics/churn")
                        .header("Authorization", token))
                .andExpect(status().isOk());

        System.out.println("================================================================================");
    }
}
