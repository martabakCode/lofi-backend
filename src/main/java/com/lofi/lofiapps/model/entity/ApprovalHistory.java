package com.lofi.lofiapps.model.entity;

import com.lofi.lofiapps.model.enums.LoanStatus;
import java.util.UUID;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalHistory extends BaseDomainEntity {
  private UUID loanId;
  private LoanStatus fromStatus;
  private LoanStatus toStatus;
  private String actionBy;
  private String notes;
}
