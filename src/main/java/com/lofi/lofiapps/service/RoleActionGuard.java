package com.lofi.lofiapps.service;

import com.lofi.lofiapps.entity.User;
import com.lofi.lofiapps.enums.RoleName;
import org.springframework.stereotype.Service;

@Service
public class RoleActionGuard {
  public void validate(User user, String action) {
    boolean hasRole =
        user.getRoles().stream()
            .anyMatch(
                role -> {
                  RoleName name = role.getName();
                  if (name == RoleName.ROLE_SUPER_ADMIN || name == RoleName.ROLE_ADMIN) {
                    return true;
                  }
                  switch (action.toLowerCase()) {
                    case "submit":
                      return name == RoleName.ROLE_CUSTOMER;
                    case "apply":
                      return name == RoleName.ROLE_CUSTOMER;
                    case "review":
                      return name == RoleName.ROLE_MARKETING;
                    case "approve":
                      return name == RoleName.ROLE_BRANCH_MANAGER;
                    case "reject":
                      return name == RoleName.ROLE_BRANCH_MANAGER
                          || name == RoleName.ROLE_MARKETING; // Marketing can

                    case "disburse":
                      return name == RoleName.ROLE_BACK_OFFICE;
                    default:
                      return false;
                  }
                });

    if (!hasRole) {
      throw new SecurityException(
          "ROLE_ACTION_NOT_ALLOWED: User role does not allow this action: " + action);
    }
  }
}
