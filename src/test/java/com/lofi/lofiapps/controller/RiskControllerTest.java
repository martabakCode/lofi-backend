package com.lofi.lofiapps.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lofi.lofiapps.dto.request.ResolveRiskRequest;
import com.lofi.lofiapps.dto.response.LoanRiskResponse;
import com.lofi.lofiapps.dto.response.RiskItem;
import com.lofi.lofiapps.service.RiskService;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RiskControllerTest {

  private MockMvc mockMvc;

  @Mock private RiskService riskService;

  @InjectMocks private RiskController riskController;

  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(riskController).build();
    objectMapper = new ObjectMapper();
  }

  @Test
  @DisplayName("Get risks should return risk response")
  void getRisks_ShouldReturnRiskResponse() throws Exception {
    UUID loanId = UUID.randomUUID();
    when(riskService.getRisks(loanId)).thenReturn(LoanRiskResponse.builder().build());

    mockMvc
        .perform(get("/loans/{id}/risks", loanId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));

    verify(riskService, times(1)).getRisks(loanId);
  }

  @Test
  @DisplayName("Resolve risk should return resolved risk item")
  void resolveRisk_ShouldReturnResolvedRiskItem() throws Exception {
    UUID riskId = UUID.randomUUID();
    ResolveRiskRequest request = new ResolveRiskRequest();
    request.setComments("Resolved");

    when(riskService.resolveRisk(eq(riskId), any(ResolveRiskRequest.class)))
        .thenReturn(RiskItem.builder().build());

    mockMvc
        .perform(
            post("/loans/risks/{riskId}/resolve", riskId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));

    verify(riskService, times(1)).resolveRisk(eq(riskId), any(ResolveRiskRequest.class));
  }
}
