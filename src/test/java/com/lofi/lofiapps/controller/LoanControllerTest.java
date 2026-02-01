package com.lofi.lofiapps.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.lofi.lofiapps.dto.request.*;
import com.lofi.lofiapps.dto.response.*;
import com.lofi.lofiapps.enums.LoanStatus;
import com.lofi.lofiapps.security.service.UserPrincipal;
import com.lofi.lofiapps.service.LoanService;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LoanControllerTest {

  private MockMvc mockMvc;

  @Mock private LoanService loanService;

  @InjectMocks private LoanController loanController;

  private ObjectMapper objectMapper;

  private UUID userId;

  @BeforeEach
  void setUp() {
    mockMvc =
        MockMvcBuilders.standaloneSetup(loanController)
            .setCustomArgumentResolvers(
                new AuthenticationPrincipalArgumentResolver(),
                new PageableHandlerMethodArgumentResolver())
            .build();
    objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    userId = UUID.randomUUID();
    setupSecurityContext(userId);
  }

  private void setupSecurityContext(UUID userId) {
    UserPrincipal userPrincipal =
        new UserPrincipal(
            userId,
            "test@example.com",
            "password",
            UUID.randomUUID(),
            "Test Branch",
            BigDecimal.valueOf(10000000),
            com.lofi.lofiapps.enums.UserStatus.ACTIVE,
            Collections.emptyList());

    Authentication authentication = mock(Authentication.class);
    SecurityContext securityContext = mock(SecurityContext.class);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(userPrincipal);
    SecurityContextHolder.setContext(securityContext);
  }

  @Test
  @DisplayName("Get loans should return paged responses")
  void getLoans_ShouldReturnPagedResponse() throws Exception {
    PagedResponse<LoanResponse> pagedResponse = new PagedResponse<>();
    pagedResponse.setItems(List.of(LoanResponse.builder().build()));
    pagedResponse.setMeta(new PagedResponse.Meta(1, 10, 1, 1));

    when(loanService.getLoans(any(LoanCriteria.class), any(Pageable.class)))
        .thenReturn(pagedResponse);

    mockMvc
        .perform(get("/loans").param("page", "0").param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));

    verify(loanService, times(1)).getLoans(any(LoanCriteria.class), any(Pageable.class));
  }

  @Test
  @DisplayName("Get my loans should return paged responses")
  void getMyLoans_ShouldReturnPagedResponse() throws Exception {
    PagedResponse<LoanResponse> pagedResponse = new PagedResponse<>();
    pagedResponse.setItems(List.of(LoanResponse.builder().build()));
    pagedResponse.setMeta(new PagedResponse.Meta(1, 10, 1, 1));

    when(loanService.getMyLoans(eq(userId), any(Pageable.class))).thenReturn(pagedResponse);

    mockMvc
        .perform(get("/loans/me").param("page", "0").param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));

    verify(loanService, times(1)).getMyLoans(eq(userId), any(Pageable.class));
  }

  @Test
  @DisplayName("Submit loan should return success")
  void submitLoan_ShouldReturnSuccess() throws Exception {
    UUID loanId = UUID.randomUUID();
    LoanResponse response =
        LoanResponse.builder().id(loanId).loanStatus(LoanStatus.SUBMITTED).build();

    when(loanService.submitLoan(eq(loanId), anyString())).thenReturn(response);

    mockMvc
        .perform(post("/loans/{id}/submit", loanId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));

    verify(loanService, times(1)).submitLoan(eq(loanId), anyString());
  }

  @Test
  @DisplayName("Review loan should return success")
  void reviewLoan_ShouldReturnSuccess() throws Exception {
    UUID loanId = UUID.randomUUID();
    ReviewLoanRequest request = new ReviewLoanRequest();
    request.setNotes("Reviewed");

    LoanResponse response =
        LoanResponse.builder().id(loanId).loanStatus(LoanStatus.REVIEWED).build();

    when(loanService.reviewLoan(eq(loanId), anyString(), eq("Reviewed"))).thenReturn(response);

    mockMvc
        .perform(
            post("/loans/{id}/review", loanId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));

    verify(loanService, times(1)).reviewLoan(eq(loanId), anyString(), eq("Reviewed"));
  }

  @Test
  @DisplayName("Approve loan should return success")
  void approveLoan_ShouldReturnSuccess() throws Exception {
    UUID loanId = UUID.randomUUID();
    ReviewLoanRequest request = new ReviewLoanRequest();
    request.setNotes("Approved");

    LoanResponse response =
        LoanResponse.builder().id(loanId).loanStatus(LoanStatus.APPROVED).build();

    when(loanService.approveLoan(eq(loanId), anyString(), eq("Approved"))).thenReturn(response);

    mockMvc
        .perform(
            post("/loans/{id}/approve", loanId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));

    verify(loanService, times(1)).approveLoan(eq(loanId), anyString(), eq("Approved"));
  }

  @Test
  @DisplayName("Reject loan should return success")
  void rejectLoan_ShouldReturnSuccess() throws Exception {
    UUID loanId = UUID.randomUUID();
    RejectLoanRequest request = new RejectLoanRequest();
    request.setReason("Rejected");

    LoanResponse response =
        LoanResponse.builder().id(loanId).loanStatus(LoanStatus.REJECTED).build();

    when(loanService.rejectLoan(eq(loanId), anyString(), eq("Rejected"))).thenReturn(response);

    mockMvc
        .perform(
            post("/loans/{id}/reject", loanId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));

    verify(loanService, times(1)).rejectLoan(eq(loanId), anyString(), eq("Rejected"));
  }

  @Test
  @DisplayName("Cancel loan should return success")
  void cancelLoan_ShouldReturnSuccess() throws Exception {
    UUID loanId = UUID.randomUUID();
    RejectLoanRequest request = new RejectLoanRequest();
    request.setReason("Cancelled");

    LoanResponse response =
        LoanResponse.builder().id(loanId).loanStatus(LoanStatus.CANCELLED).build();

    when(loanService.cancelLoan(eq(loanId), anyString(), eq("Cancelled"))).thenReturn(response);

    mockMvc
        .perform(
            post("/loans/{id}/cancel", loanId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));

    verify(loanService, times(1)).cancelLoan(eq(loanId), anyString(), eq("Cancelled"));
  }

  @Test
  @DisplayName("Rollback loan should return success")
  void rollbackLoan_ShouldReturnSuccess() throws Exception {
    UUID loanId = UUID.randomUUID();
    ReviewLoanRequest request = new ReviewLoanRequest();
    request.setNotes("Rollback");

    LoanResponse response = LoanResponse.builder().id(loanId).build();

    when(loanService.rollbackLoan(eq(loanId), anyString(), eq("Rollback"))).thenReturn(response);

    mockMvc
        .perform(
            post("/loans/{id}/rollback", loanId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));

    verify(loanService, times(1)).rollbackLoan(eq(loanId), anyString(), eq("Rollback"));
  }

  @Test
  @DisplayName("Disburse loan should return success")
  void disburseLoan_ShouldReturnSuccess() throws Exception {
    UUID loanId = UUID.randomUUID();
    DisbursementRequest request = new DisbursementRequest();
    request.setReferenceNumber("REF123");

    LoanResponse response =
        LoanResponse.builder().id(loanId).loanStatus(LoanStatus.DISBURSED).build();

    when(loanService.disburseLoan(eq(loanId), anyString(), eq("REF123"))).thenReturn(response);

    mockMvc
        .perform(
            post("/loans/{id}/disburse", loanId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));

    verify(loanService, times(1)).disburseLoan(eq(loanId), anyString(), eq("REF123"));
  }

  @Test
  @DisplayName("Complete loan should return success")
  void completeLoan_ShouldReturnSuccess() throws Exception {
    UUID loanId = UUID.randomUUID();
    LoanResponse response =
        LoanResponse.builder().id(loanId).loanStatus(LoanStatus.COMPLETED).build();

    when(loanService.completeLoan(eq(loanId), anyString())).thenReturn(response);

    mockMvc
        .perform(post("/loans/{id}/complete", loanId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));

    verify(loanService, times(1)).completeLoan(eq(loanId), anyString());
  }

  @Test
  @DisplayName("Apply loan should return success")
  void applyLoan_ShouldReturnSuccess() throws Exception {
    LoanRequest request = new LoanRequest();
    request.setLoanAmount(BigDecimal.valueOf(1000000));
    request.setTenor(12);

    LoanResponse response = LoanResponse.builder().id(UUID.randomUUID()).build();

    when(loanService.applyLoan(any(LoanRequest.class), eq(userId), anyString()))
        .thenReturn(response);

    mockMvc
        .perform(
            post("/loans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));

    verify(loanService, times(1)).applyLoan(any(LoanRequest.class), eq(userId), anyString());
  }

  @Test
  @DisplayName("Marketing apply loan should return success")
  void marketingApplyLoan_ShouldReturnSuccess() throws Exception {
    MarketingApplyLoanRequest request = new MarketingApplyLoanRequest();
    // Populate required fields
    request.setFullName("Test User");
    request.setEmail("test@email.com");
    request.setUsername("testuser");
    request.setPhoneNumber("123");
    request.setBranchId(UUID.randomUUID());
    request.setIncomeSource("Work");
    request.setIncomeType("Salary");
    request.setMonthlyIncome(BigDecimal.TEN);

    request.setNik("12345");
    request.setDateOfBirth(java.time.LocalDate.now());
    request.setPlaceOfBirth("City");
    request.setCity("City");
    request.setAddress("Addr");
    request.setProvince("Prov");
    request.setDistrict("Dist");
    request.setSubDistrict("Sub");
    request.setPostalCode("123");
    request.setGender(com.lofi.lofiapps.enums.Gender.MALE);
    request.setMaritalStatus(com.lofi.lofiapps.enums.MaritalStatus.SINGLE);

    request.setOccupation("Job");
    request.setProductId(UUID.randomUUID());
    request.setLoanAmount(BigDecimal.valueOf(1000000));
    request.setTenor(12);

    LoanResponse response = LoanResponse.builder().id(UUID.randomUUID()).build();

    when(loanService.marketingApplyLoan(any(MarketingApplyLoanRequest.class), anyString()))
        .thenReturn(response);

    mockMvc
        .perform(
            post("/loans/marketing/apply-on-behalf")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));

    verify(loanService, times(1))
        .marketingApplyLoan(any(MarketingApplyLoanRequest.class), anyString());
  }

  @Test
  @DisplayName("Get loan detail should return success")
  void getLoanDetail_ShouldReturnSuccess() throws Exception {
    UUID loanId = UUID.randomUUID();
    LoanResponse response = LoanResponse.builder().id(loanId).build();

    when(loanService.getLoanDetail(loanId)).thenReturn(response);

    mockMvc
        .perform(get("/loans/{id}", loanId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));

    verify(loanService, times(1)).getLoanDetail(loanId);
  }

  @Test
  @DisplayName("Analyze loan should return success")
  void analyzeLoan_ShouldReturnSuccess() throws Exception {
    UUID loanId = UUID.randomUUID();
    LoanAnalysisResponse response = new LoanAnalysisResponse();

    when(loanService.analyzeLoan(loanId)).thenReturn(response);

    mockMvc
        .perform(get("/loans/{id}/analysis", loanId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));

    verify(loanService, times(1)).analyzeLoan(loanId);
  }
}
