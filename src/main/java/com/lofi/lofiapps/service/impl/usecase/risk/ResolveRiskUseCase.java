package com.lofi.lofiapps.service.impl.usecase.risk;

import com.lofi.lofiapps.dto.request.ResolveRiskRequest;
import com.lofi.lofiapps.dto.response.RiskItem;
import com.lofi.lofiapps.entity.RiskCheck;
import com.lofi.lofiapps.service.RiskCheckService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for resolving risk checks with manual override. Persists resolution to database for
 * audit trail.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ResolveRiskUseCase {

  private final RiskCheckService riskCheckService;

  @Transactional
  public RiskItem execute(UUID riskId, ResolveRiskRequest request) {
    // Get current user ID from security context
    UUID resolvedBy = getCurrentUserId();

    log.info(
        "Resolving risk check {} by user {} with comments: {}",
        riskId,
        resolvedBy,
        request.getComments());

    RiskCheck resolvedCheck =
        riskCheckService.resolveRiskCheck(riskId, request.getComments(), resolvedBy);

    return riskCheckService.toRiskItem(resolvedCheck);
  }

  private UUID getCurrentUserId() {
    var authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null && authentication.getPrincipal() != null) {
      // Assuming UserPrincipal has getId() method
      var principal = authentication.getPrincipal();
      if (principal instanceof com.lofi.lofiapps.security.service.UserPrincipal) {
        return ((com.lofi.lofiapps.security.service.UserPrincipal) principal).getId();
      }
    }
    // Fallback - should not happen in properly authenticated requests
    log.warn("Could not determine current user ID for risk resolution");
    return null;
  }
}
