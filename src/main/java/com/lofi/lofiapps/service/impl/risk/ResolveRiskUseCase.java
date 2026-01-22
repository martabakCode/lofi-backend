package com.lofi.lofiapps.service.impl.risk;

import com.lofi.lofiapps.model.dto.request.ResolveRiskRequest;
import com.lofi.lofiapps.model.dto.response.RiskItem;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ResolveRiskUseCase {

  public RiskItem execute(UUID riskId, ResolveRiskRequest request) {
    // In a real system, we'd fetch the risk record from DB.
    // For this demo, we'll simulate the resolution of a specific check.

    return RiskItem.builder()
        .id(riskId.toString())
        .name("Manual Override")
        .description("Manual resolution by backoffice")
        .status(RiskItem.RiskStatus.RESOLVED)
        .comments(request.getComments())
        .build();
  }
}
