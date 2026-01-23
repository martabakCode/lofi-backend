package com.lofi.lofiapps.controller;

import com.lofi.lofiapps.dto.request.ResolveRiskRequest;
import com.lofi.lofiapps.dto.response.*;
import com.lofi.lofiapps.dto.response.LoanRiskResponse;
import com.lofi.lofiapps.dto.response.RiskItem;
import com.lofi.lofiapps.service.RiskService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/loans")
@RequiredArgsConstructor
@Tag(name = "Risk", description = "Risk Management")
public class RiskController {
  private final RiskService riskService;

  @GetMapping("/{id}/risks")
  @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
  @Operation(summary = "Get risks for a loan")
  public ResponseEntity<ApiResponse<LoanRiskResponse>> getRisks(@PathVariable UUID id) {
    return ResponseEntity.ok(ApiResponse.success(riskService.getRisks(id)));
  }

  @PostMapping("/risks/{riskId}/resolve")
  @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
  @Operation(summary = "Resolve a risk")
  public ResponseEntity<ApiResponse<RiskItem>> resolveRisk(
      @PathVariable UUID riskId, @Valid @RequestBody ResolveRiskRequest request) {
    return ResponseEntity.ok(
        ApiResponse.success(
            riskService.resolveRisk(riskId, request), "Risk resolved successfully"));
  }
}
