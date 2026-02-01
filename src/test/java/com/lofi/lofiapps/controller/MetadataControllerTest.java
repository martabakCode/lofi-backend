package com.lofi.lofiapps.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class MetadataControllerTest {

  private MockMvc mockMvc;

  @InjectMocks private MetadataController metadataController;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(metadataController).build();
  }

  @Test
  @DisplayName("Get enums should return map of enums")
  void getEnums_ShouldReturnMapOfEnums() throws Exception {
    mockMvc
        .perform(get("/metadata/enums"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.loanStatus").isArray())
        .andExpect(jsonPath("$.data.approvalStage").isArray())
        .andExpect(jsonPath("$.data.roleName").isArray())
        .andExpect(jsonPath("$.data.userStatus").isArray())
        .andExpect(jsonPath("$.data.gender").isArray())
        .andExpect(jsonPath("$.data.maritalStatus").isArray())
        .andExpect(jsonPath("$.data.riskLevel").isArray());
  }
}
