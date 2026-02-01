package com.lofi.lofiapps.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.lofi.lofiapps.dto.response.AuditLogResponse;
import com.lofi.lofiapps.dto.response.PagedResponse;
import com.lofi.lofiapps.service.impl.AuditServiceImpl;
import java.util.List;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuditControllerTest {

  private MockMvc mockMvc;

  @Mock private AuditServiceImpl auditService;

  @InjectMocks private AuditController auditController;

  @BeforeEach
  void setUp() {
    mockMvc =
        MockMvcBuilders.standaloneSetup(auditController)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .build();
  }

  @Test
  @DisplayName("Get audit logs should return paged response")
  void getAuditLogs_ShouldReturnPagedResponse() throws Exception {
    PagedResponse<AuditLogResponse> pagedResponse = new PagedResponse<>();
    pagedResponse.setItems(List.of(AuditLogResponse.builder().build()));
    pagedResponse.setMeta(new PagedResponse.Meta(1, 10, 1, 1));

    when(auditService.getAuditLogs(any(Pageable.class))).thenReturn(pagedResponse);

    mockMvc
        .perform(get("/admin/audit-logs").param("page", "0").param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));

    verify(auditService, times(1)).getAuditLogs(any(Pageable.class));
  }
}
