package com.lofi.lofiapps.controller;

import com.lofi.lofiapps.model.dto.request.ResolveRiskRequest;
import com.lofi.lofiapps.model.dto.response.*;
import com.lofi.lofiapps.model.dto.response.LoanRiskResponse;
import com.lofi.lofiapps.model.dto.response.RiskItem;
import com.lofi.lofiapps.service.impl.risk.GetLoanRisksUseCase;
import com.lofi.lofiapps.service.impl.risk.ResolveRiskUseCase;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/loans")
@RequiredArgsConstructor
public class RiskController {
  private final GetLoanRisksUseCase getLoanRisksUseCase;
  private final ResolveRiskUseCase resolveRiskUseCase;

  @GetMapping("/{id}/risks")
  @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
  public ResponseEntity<ApiResponse<LoanRiskResponse>> getRisks(@PathVariable UUID id) {
    return ResponseEntity.ok(ApiResponse.success(getLoanRisksUseCase.execute(id)));
  }

  @PostMapping("/risks/{riskId}/resolve")
  @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
  public ResponseEntity<ApiResponse<RiskItem>> resolveRisk(
      @PathVariable UUID riskId, @Valid @RequestBody ResolveRiskRequest request) {
    return ResponseEntity.ok(
        ApiResponse.success(
            resolveRiskUseCase.execute(riskId, request), "Risk resolved successfully"));
  }
}
