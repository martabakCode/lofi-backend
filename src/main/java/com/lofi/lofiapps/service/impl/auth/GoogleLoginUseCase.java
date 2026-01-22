package com.lofi.lofiapps.service.impl.auth;

import com.lofi.lofiapps.model.dto.request.GoogleLoginRequest;
import com.lofi.lofiapps.model.dto.response.LoginResponse;
import com.lofi.lofiapps.model.entity.Branch;
import com.lofi.lofiapps.model.entity.Role;
import com.lofi.lofiapps.model.entity.User;
import com.lofi.lofiapps.model.enums.RoleName;
import com.lofi.lofiapps.model.enums.UserStatus;
import com.lofi.lofiapps.repository.BranchRepository;
import com.lofi.lofiapps.repository.RoleRepository;
import com.lofi.lofiapps.repository.UserRepository;
import com.lofi.lofiapps.security.jwt.JwtUtils;
import com.lofi.lofiapps.security.service.GoogleAuthService;
import com.lofi.lofiapps.security.service.UserPrincipal;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GoogleLoginUseCase {
  private final GoogleAuthService googleAuthService;
  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final BranchRepository branchRepository;
  private final JwtUtils jwtUtils;

  @Transactional
  public LoginResponse execute(GoogleLoginRequest request) {
    String email = googleAuthService.verifyGoogleToken(request.getIdToken());
    if (email == null) {
      throw new IllegalArgumentException("Invalid Google Token");
    }

    User user = userRepository.findByEmail(email).orElse(null);

    if (user == null) {
      // Auto-register
      Role customerRole =
          roleRepository
              .findByName(RoleName.ROLE_CUSTOMER)
              .orElseThrow(() -> new RuntimeException("Role Customer not found"));

      Branch nearestBranch = null;
      if (request.getLatitude() != null && request.getLongitude() != null) {
        nearestBranch = findNearestBranch(request.getLatitude(), request.getLongitude());
      }

      user =
          User.builder()
              .email(email)
              .username(email)
              .fullName("Google User")
              .password("") // No password for Google users
              .status(UserStatus.ACTIVE)
              .roles(Collections.singleton(customerRole))
              .branch(nearestBranch)
              .profileCompleted(false)
              .build();

      user = userRepository.save(user);
    }

    UserPrincipal userPrincipal = UserPrincipal.create(user);
    Authentication authentication =
        new UsernamePasswordAuthenticationToken(
            userPrincipal, null, userPrincipal.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(authentication);

    String jwt = jwtUtils.generateJwtToken(authentication);
    long expiration = jwtUtils.getExpirationFromJwtToken(jwt);

    return LoginResponse.builder()
        .accessToken(jwt)
        .expiresIn(expiration / 1000)
        .tokenType("Bearer")
        .build();
  }

  private Branch findNearestBranch(double lat, double lon) {
    List<Branch> branches = branchRepository.findAll();
    if (branches.isEmpty()) return null;

    Branch nearest = null;
    double minDistance = Double.MAX_VALUE;

    for (Branch branch : branches) {
      if (branch.getLatitude() == null || branch.getLongitude() == null) continue;

      try {
        double branchLat = Double.parseDouble(branch.getLatitude());
        double branchLon = Double.parseDouble(branch.getLongitude());
        double distance = calculateDistance(lat, lon, branchLat, branchLon);

        if (distance < minDistance) {
          minDistance = distance;
          nearest = branch;
        }
      } catch (NumberFormatException e) {
        // Skip branches with invalid coordinates
      }
    }
    return nearest;
  }

  private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
    double earthRadius = 6371; // km
    double dLat = Math.toRadians(lat2 - lat1);
    double dLon = Math.toRadians(lon2 - lon1);
    double a =
        Math.sin(dLat / 2) * Math.sin(dLat / 2)
            + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);
    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return earthRadius * c;
  }
}
