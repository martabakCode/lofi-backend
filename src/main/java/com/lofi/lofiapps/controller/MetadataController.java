package com.lofi.lofiapps.controller;

import com.lofi.lofiapps.dto.response.*;
import com.lofi.lofiapps.enums.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/metadata")
@Tag(name = "Metadata", description = "Metadata Management")
public class MetadataController {

  @GetMapping("/enums")
  @Operation(summary = "Get enums")
  public ResponseEntity<ApiResponse<Map<String, Object>>> getEnums() {
    Map<String, Object> enums = new HashMap<>();

    enums.put(
        "loanStatus",
        Arrays.stream(LoanStatus.values()).map(Enum::name).collect(Collectors.toList()));
    enums.put(
        "approvalStage",
        Arrays.stream(ApprovalStage.values()).map(Enum::name).collect(Collectors.toList()));
    enums.put(
        "roleName", Arrays.stream(RoleName.values()).map(Enum::name).collect(Collectors.toList()));
    enums.put(
        "userStatus",
        Arrays.stream(UserStatus.values()).map(Enum::name).collect(Collectors.toList()));
    enums.put(
        "gender", Arrays.stream(Gender.values()).map(Enum::name).collect(Collectors.toList()));
    enums.put(
        "maritalStatus",
        Arrays.stream(MaritalStatus.values()).map(Enum::name).collect(Collectors.toList()));

    // Placeholders for risk levels until implemented in domain
    enums.put("riskLevel", Arrays.asList("LOW", "MEDIUM", "HIGH", "CRITICAL"));

    return ResponseEntity.ok(ApiResponse.success(enums, "Metadata fetched successfully"));
  }
}
