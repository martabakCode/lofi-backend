package com.lofi.lofiapps.service;

import com.lofi.lofiapps.entity.Loan;
import com.lofi.lofiapps.entity.Product;
import com.lofi.lofiapps.enums.LoanStatus;
import com.lofi.lofiapps.exception.ResourceNotFoundException;
import com.lofi.lofiapps.repository.LoanRepository;
import com.lofi.lofiapps.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.integration.redis.util.RedisLockRegistry;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductCalculationService {

  private final LoanRepository loanRepository;
  private final ProductRepository productRepository;
  private final CacheManager cacheManager;
  private final RedisLockRegistry redisLockRegistry;

  private static final String AVAILABLE_PRODUCT_CACHE = "availableProduct";
  private static final String USER_LOANS_CACHE = "userLoans";
  private static final long LOCK_TIMEOUT = 5_000; // 5 seconds

  /**
   * Calculate the available amount for a user's product availableAmount = productLimit -
   * approvedLoanAmount
   *
   * <p>Active loan statuses: APPROVED, DISBURSED
   */
  public BigDecimal calculateAvailableAmount(UUID userId, UUID productId) {
    String cacheKey = userId + ":" + productId;

    // Try to get from cache first (fast path)
    try {
      Cache cache = cacheManager.getCache(AVAILABLE_PRODUCT_CACHE);
      if (cache != null) {
        BigDecimal cachedValue = cache.get(cacheKey, BigDecimal.class);
        if (cachedValue != null) {
          return cachedValue;
        }
      }
    } catch (Exception e) {
      log.warn("Cache read failed, proceeding with calculation", e);
    }

    // Acquire distributed lock to prevent race conditions
    String lockKey = "lock:availableProduct:" + cacheKey;
    Lock lock = redisLockRegistry.obtain(lockKey);
    boolean locked = false;
    try {
      locked = lock.tryLock(LOCK_TIMEOUT, TimeUnit.MILLISECONDS);
      if (!locked) {
        log.warn("Could not acquire lock for {}, proceeding without cache", cacheKey);
        return calculateWithoutCache(userId, productId);
      }

      // Double-check cache after acquiring lock
      try {
        Cache cache = cacheManager.getCache(AVAILABLE_PRODUCT_CACHE);
        if (cache != null) {
          BigDecimal cachedValue = cache.get(cacheKey, BigDecimal.class);
          if (cachedValue != null) {
            return cachedValue;
          }
        }
      } catch (Exception e) {
        log.warn("Cache read failed after lock acquisition", e);
      }

      // Calculate and cache
      BigDecimal result = calculateWithoutCache(userId, productId);
      cacheResult(cacheKey, result);
      return result;
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      log.warn("Interrupted while waiting for lock", e);
      return calculateWithoutCache(userId, productId);
    } finally {
      if (locked) {
        try {
          lock.unlock();
        } catch (Exception e) {
          log.warn("Failed to unlock {}", lockKey, e);
        }
      }
    }
  }

  public BigDecimal calculateTotalApprovedLoanAmount(UUID userId) {
    List<LoanStatus> activeStatuses = List.of(LoanStatus.APPROVED, LoanStatus.DISBURSED);
    BigDecimal amount = loanRepository.sumLoanAmountByCustomerIdAndStatusIn(userId, activeStatuses);
    return amount != null ? amount : BigDecimal.ZERO;
  }

  private BigDecimal calculateWithoutCache(UUID userId, UUID productId) {
    BigDecimal approvedAmount = calculateTotalApprovedLoanAmount(userId);

    Product product =
        productRepository
            .findById(productId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Product", "id", productId.toString()));

    return product.getMaxLoanAmount().subtract(approvedAmount).max(BigDecimal.ZERO);
  }

  private void cacheResult(String cacheKey, BigDecimal result) {
    try {
      Cache cache = cacheManager.getCache(AVAILABLE_PRODUCT_CACHE);
      if (cache != null) {
        cache.put(cacheKey, result);
      }
    } catch (Exception e) {
      log.warn("Failed to cache result for key: {}", cacheKey, e);
    }
  }

  /** Get all active loan information for a user with caching Cache key: userLoans:{userId} */
  public List<Loan> getActiveLoans(UUID userId) {
    // Try cache first
    try {
      Cache cache = cacheManager.getCache(USER_LOANS_CACHE);
      if (cache != null) {
        List<Loan> cached = cache.get(userId.toString(), List.class);
        if (cached != null) {
          return cached;
        }
      }
    } catch (Exception e) {
      log.warn("Cache read failed for user loans", e);
    }

    // Calculate without cache (read-heavy operation)
    List<LoanStatus> activeStatuses = List.of(LoanStatus.APPROVED, LoanStatus.DISBURSED);
    List<Loan> loans = loanRepository.findByCustomerIdAndLoanStatusIn(userId, activeStatuses);

    // Cache result
    try {
      Cache cache = cacheManager.getCache(USER_LOANS_CACHE);
      if (cache != null) {
        cache.put(userId.toString(), loans);
      }
    } catch (Exception e) {
      log.warn("Failed to cache user loans", e);
    }

    return loans;
  }

  /** Check if user has any submitted/active loan */
  public boolean hasActiveLoan(UUID userId) {
    List<LoanStatus> activeStatuses =
        List.of(
            LoanStatus.SUBMITTED, LoanStatus.REVIEWED, LoanStatus.APPROVED, LoanStatus.DISBURSED);
    return !loanRepository.findByCustomerIdAndLoanStatusIn(userId, activeStatuses).isEmpty();
  }

  /** Invalidate cache when loan status changes */
  @CacheEvict(
      value = {AVAILABLE_PRODUCT_CACHE, USER_LOANS_CACHE},
      allEntries = true)
  public void invalidateCache(UUID userId, UUID productId) {
    log.info("Cache invalidated for user: {}, product: {}", userId, productId);
  }
}
