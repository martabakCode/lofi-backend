package com.lofi.lofiapps.service;

import com.lofi.lofiapps.entity.Loan;
import com.lofi.lofiapps.entity.User;
import com.lofi.lofiapps.enums.RoleName;
import org.springframework.stereotype.Service;

@Service
public class BranchAccessGuard {
  public void validate(User user, Loan loan) {
    // Skip for Super Admin and Admin (Global access with audit)
    boolean isGlobalAdmin =
        user.getRoles().stream()
            .anyMatch(
                r ->
                    r.getName() == RoleName.ROLE_SUPER_ADMIN || r.getName() == RoleName.ROLE_ADMIN);
    if (isGlobalAdmin) return;

    // Skip for Back Office (Global access but should be logged)
    boolean isBackOffice =
        user.getRoles().stream().anyMatch(r -> r.getName() == RoleName.ROLE_BACK_OFFICE);
    if (isBackOffice) return;

    // Check branch
    if (user.getBranch() == null) {
      // If user has no branch (except BackOffice/Admin), deny
      throw new SecurityException("USER_NOT_IN_BRANCH");
    }

    if (loan.getBranch() == null) {
      // If loan not assigned branch yet, only allow if user is the loan owner
      // (customer)
      // or if user has explicit permission to work with unassigned loans
      if (loan.getCustomer() != null && loan.getCustomer().getId().equals(user.getId())) {
        // Customer accessing their own loan - allow
        return;
      }
      // For other users, deny access to unassigned loans
      throw new SecurityException(
          "BRANCH_ACCESS_DENIED: Cannot access loan without branch assignment.");
    }

    if (!user.getBranch().getId().equals(loan.getBranch().getId())) {
      throw new SecurityException(
          "BRANCH_NOT_FOUND: User does not have access to this branch data.");
    }
  }
}
