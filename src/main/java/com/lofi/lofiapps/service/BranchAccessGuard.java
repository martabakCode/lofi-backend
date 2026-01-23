package com.lofi.lofiapps.service;

import com.lofi.lofiapps.entity.Loan;
import com.lofi.lofiapps.entity.User;
import com.lofi.lofiapps.enums.RoleName;
import org.springframework.stereotype.Service;

@Service
public class BranchAccessGuard {
  public void validate(User user, Loan loan) {
    // Skip for Back Office (Global access?)
    boolean isBackOffice =
        user.getRoles().stream().anyMatch(r -> r.getName() == RoleName.ROLE_BACK_OFFICE);
    if (isBackOffice) return;

    // Check branch
    if (user.getBranch() == null) {
      // If user has no branch (except BackOffice), deny
      throw new SecurityException("USER_NOT_IN_BRANCH");
    }

    if (loan.getBranch() == null) {
      // If loan not assigned branch yet (e.g. Draft), check if user matches user
      // branch?
      // Or if loan belongs to customer, check customer branch
      // For now, allow? Or strict?
      return;
    }

    if (!user.getBranch().getId().equals(loan.getBranch().getId())) {
      throw new SecurityException(
          "BRANCH_NOT_FOUND: User does not have access to this branch data.");
    }
  }
}
