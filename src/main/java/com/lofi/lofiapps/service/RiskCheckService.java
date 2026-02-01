package com.lofi.lofiapps.service;

import com.lofi.lofiapps.dto.response.RiskItem;
import com.lofi.lofiapps.entity.Loan;
import com.lofi.lofiapps.entity.RiskCheck;
import java.util.List;
import java.util.UUID;

/**
 * Service interface for performing risk checks on loans. Implementations should integrate with
 * external credit bureaus and blacklist databases.
 */
public interface RiskCheckService {

  /**
   * Performs all required risk checks for a loan. Results are persisted to the database.
   *
   * @param loan the loan to check
   * @param performedBy the user performing the check
   * @return list of risk check results
   */
  List<RiskCheck> performRiskChecks(Loan loan, UUID performedBy);

  /**
   * Performs a specific risk check type.
   *
   * @param loan the loan to check
   * @param checkType the type of check to perform
   * @param performedBy the user performing the check
   * @return the risk check result
   */
  RiskCheck performCheck(Loan loan, String checkType, UUID performedBy);

  /**
   * Gets all risk checks for a loan.
   *
   * @param loanId the loan ID
   * @return list of risk checks
   */
  List<RiskCheck> getRiskChecks(UUID loanId);

  /**
   * Resolves a risk check with manual override.
   *
   * @param riskCheckId the risk check ID
   * @param comments resolution comments
   * @param resolvedBy the user resolving the check
   * @return the updated risk check
   */
  RiskCheck resolveRiskCheck(UUID riskCheckId, String comments, UUID resolvedBy);

  /**
   * Determines if a loan can be disbursed based on risk checks.
   *
   * @param loanId the loan ID
   * @return true if no FAIL status checks exist
   */
  boolean canDisburse(UUID loanId);

  /**
   * Converts a RiskCheck entity to a RiskItem DTO.
   *
   * @param riskCheck the entity
   * @return the DTO
   */
  RiskItem toRiskItem(RiskCheck riskCheck);
}
