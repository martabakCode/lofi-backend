package com.lofi.lofiapps.security.idempotency;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark endpoints that require idempotency key. Per workflow rules, the following
 * operations MUST be idempotent:
 *
 * <ul>
 *   <li>Approve loan
 *   <li>Disburse loan
 *   <li>Reset password
 * </ul>
 *
 * <p>Usage: Apply this annotation to controller methods that require idempotency protection.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireIdempotency {
  /** TTL in hours for the cached response. Default is 24 hours. */
  long ttlHours() default 24;
}
