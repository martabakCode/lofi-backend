package com.lofi.lofiapps.security.service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.lofi.lofiapps.model.entity.User;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
@AllArgsConstructor
public class UserPrincipal implements UserDetails {
  private UUID id;
  private String email;
  @JsonIgnore private String password;
  private UUID branchId;
  private String branchName;
  private java.math.BigDecimal plafond;
  private Collection<? extends GrantedAuthority> authorities;

  public static UserPrincipal create(User user) {
    List<GrantedAuthority> authorities =
        user.getRoles().stream()
            .map(role -> new SimpleGrantedAuthority(role.getName().name()))
            .collect(Collectors.toList());

    // Add permissions as authorities
    user.getRoles()
        .forEach(
            role -> {
              if (role.getPermissions() != null) {
                role.getPermissions()
                    .forEach(
                        permission ->
                            authorities.add(new SimpleGrantedAuthority(permission.getName())));
              }
            });

    return new UserPrincipal(
        user.getId(),
        user.getEmail(),
        user.getPassword(),
        user.getBranch() != null ? user.getBranch().getId() : null,
        user.getBranch() != null ? user.getBranch().getName() : null,
        user.getProduct() != null
            ? user.getProduct().getMaxLoanAmount()
            : java.math.BigDecimal.ZERO,
        authorities);
  }

  public List<String> getPermissions() {
    return authorities.stream()
        .map(GrantedAuthority::getAuthority)
        .filter(auth -> !auth.startsWith("ROLE_"))
        .collect(Collectors.toList());
  }

  public List<String> getRoles() {
    return authorities.stream()
        .map(GrantedAuthority::getAuthority)
        .filter(auth -> auth.startsWith("ROLE_"))
        .collect(Collectors.toList());
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return authorities;
  }

  @Override
  public String getPassword() {
    return password;
  }

  @Override
  public String getUsername() {
    return email; // Using email as username for spring security context usually
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }
}
