package com.lofi.lofiapps.security.service;

import static org.junit.jupiter.api.Assertions.*;

import com.lofi.lofiapps.entity.Branch;
import com.lofi.lofiapps.entity.Permission;
import com.lofi.lofiapps.entity.Product;
import com.lofi.lofiapps.entity.Role;
import com.lofi.lofiapps.entity.User;
import com.lofi.lofiapps.enums.RoleName;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

class UserPrincipalTest {

  @Test
  @DisplayName("Create should build UserPrincipal from User entity")
  void create_ShouldBuildUserPrincipalFromUser() {
    // Arrange
    UUID userId = UUID.randomUUID();
    UUID branchId = UUID.randomUUID();

    Branch branch = Branch.builder().id(branchId).name("Test Branch").build();

    Product product = Product.builder().maxLoanAmount(BigDecimal.valueOf(50000000)).build();

    Permission permission1 = new Permission();
    permission1.setName("READ_LOAN");
    Permission permission2 = new Permission();
    permission2.setName("CREATE_LOAN");

    Role role = new Role();
    role.setName(RoleName.ROLE_CUSTOMER);
    role.setPermissions(Set.of(permission1, permission2));

    User user =
        User.builder()
            .id(userId)
            .email("test@example.com")
            .password("hashedPassword")
            .branch(branch)
            .product(product)
            .roles(Set.of(role))
            .build();

    // Act
    UserPrincipal principal = UserPrincipal.create(user);

    // Assert
    assertNotNull(principal);
    assertEquals(userId, principal.getId());
    assertEquals("test@example.com", principal.getEmail());
    assertEquals("hashedPassword", principal.getPassword());
    assertEquals(branchId, principal.getBranchId());
    assertEquals("Test Branch", principal.getBranchName());
    assertEquals(BigDecimal.valueOf(50000000), principal.getPlafond());
  }

  @Test
  @DisplayName("Create should handle user without branch")
  void create_ShouldHandleUserWithoutBranch() {
    // Arrange
    UUID userId = UUID.randomUUID();
    User user =
        User.builder()
            .id(userId)
            .email("test@example.com")
            .password("hashedPassword")
            .branch(null)
            .product(null)
            .roles(new HashSet<>())
            .build();

    // Act
    UserPrincipal principal = UserPrincipal.create(user);

    // Assert
    assertNotNull(principal);
    assertNull(principal.getBranchId());
    assertNull(principal.getBranchName());
    assertEquals(BigDecimal.ZERO, principal.getPlafond());
  }

  @Test
  @DisplayName("Create should handle user without product")
  void create_ShouldHandleUserWithoutProduct() {
    // Arrange
    UUID userId = UUID.randomUUID();
    User user =
        User.builder()
            .id(userId)
            .email("test@example.com")
            .password("hashedPassword")
            .branch(null)
            .product(null)
            .roles(new HashSet<>())
            .build();

    // Act
    UserPrincipal principal = UserPrincipal.create(user);

    // Assert
    assertEquals(BigDecimal.ZERO, principal.getPlafond());
  }

  @Test
  @DisplayName("GetAuthorities should include roles and permissions")
  void getAuthorities_ShouldIncludeRolesAndPermissions() {
    // Arrange
    Permission permission = new Permission();
    permission.setName("READ_LOAN");

    Role role = new Role();
    role.setName(RoleName.ROLE_CUSTOMER);
    role.setPermissions(Set.of(permission));

    User user =
        User.builder()
            .id(UUID.randomUUID())
            .email("test@example.com")
            .password("password")
            .roles(Set.of(role))
            .build();

    // Act
    UserPrincipal principal = UserPrincipal.create(user);

    // Assert
    assertNotNull(principal.getAuthorities());
    assertTrue(
        principal.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .anyMatch(a -> a.equals("ROLE_CUSTOMER")));
    assertTrue(
        principal.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .anyMatch(a -> a.equals("READ_LOAN")));
  }

  @Test
  @DisplayName("GetRoles should return only role authorities")
  void getRoles_ShouldReturnOnlyRoles() {
    // Arrange
    Permission permission = new Permission();
    permission.setName("READ_LOAN");

    Role role = new Role();
    role.setName(RoleName.ROLE_ADMIN);
    role.setPermissions(Set.of(permission));

    User user =
        User.builder()
            .id(UUID.randomUUID())
            .email("test@example.com")
            .password("password")
            .roles(Set.of(role))
            .build();

    UserPrincipal principal = UserPrincipal.create(user);

    // Act
    var roles = principal.getRoles();

    // Assert
    assertNotNull(roles);
    assertTrue(roles.stream().allMatch(r -> r.startsWith("ROLE_")));
    assertTrue(roles.contains("ROLE_ADMIN"));
    assertFalse(roles.contains("READ_LOAN"));
  }

  @Test
  @DisplayName("GetPermissions should return only permission authorities")
  void getPermissions_ShouldReturnOnlyPermissions() {
    // Arrange
    Permission permission1 = new Permission();
    permission1.setName("READ_LOAN");
    Permission permission2 = new Permission();
    permission2.setName("WRITE_LOAN");

    Role role = new Role();
    role.setName(RoleName.ROLE_MARKETING);
    role.setPermissions(Set.of(permission1, permission2));

    User user =
        User.builder()
            .id(UUID.randomUUID())
            .email("test@example.com")
            .password("password")
            .roles(Set.of(role))
            .build();

    UserPrincipal principal = UserPrincipal.create(user);

    // Act
    var permissions = principal.getPermissions();

    // Assert
    assertNotNull(permissions);
    assertTrue(permissions.stream().noneMatch(p -> p.startsWith("ROLE_")));
    assertTrue(permissions.contains("READ_LOAN"));
    assertTrue(permissions.contains("WRITE_LOAN"));
  }

  @Test
  @DisplayName("GetUsername should return email")
  void getUsername_ShouldReturnEmail() {
    // Arrange
    User user =
        User.builder()
            .id(UUID.randomUUID())
            .email("user@example.com")
            .password("password")
            .roles(new HashSet<>())
            .build();

    UserPrincipal principal = UserPrincipal.create(user);

    // Act & Assert
    assertEquals("user@example.com", principal.getUsername());
  }

  @Test
  @DisplayName("UserDetails methods should return true")
  void userDetailsMethods_ShouldReturnTrue() {
    // Arrange
    User user =
        User.builder()
            .id(UUID.randomUUID())
            .email("user@example.com")
            .password("password")
            .status(com.lofi.lofiapps.enums.UserStatus.ACTIVE)
            .roles(new HashSet<>())
            .build();

    UserPrincipal principal = UserPrincipal.create(user);

    // Act & Assert
    assertTrue(principal.isAccountNonExpired());
    assertTrue(principal.isAccountNonLocked());
    assertTrue(principal.isCredentialsNonExpired());
    assertTrue(principal.isEnabled());
  }

  @Test
  @DisplayName("Create should handle multiple roles")
  void create_ShouldHandleMultipleRoles() {
    // Arrange
    Role role1 = new Role();
    role1.setName(RoleName.ROLE_ADMIN);
    role1.setPermissions(new HashSet<>());

    Role role2 = new Role();
    role2.setName(RoleName.ROLE_MARKETING);
    role2.setPermissions(new HashSet<>());

    User user =
        User.builder()
            .id(UUID.randomUUID())
            .email("admin@example.com")
            .password("password")
            .roles(Set.of(role1, role2))
            .build();

    // Act
    UserPrincipal principal = UserPrincipal.create(user);

    // Assert
    var roles = principal.getRoles();
    assertEquals(2, roles.size());
    assertTrue(roles.contains("ROLE_ADMIN"));
    assertTrue(roles.contains("ROLE_MARKETING"));
  }

  @Test
  @DisplayName("Create should handle roles with null permissions")
  void create_ShouldHandleRolesWithNullPermissions() {
    // Arrange
    Role role = new Role();
    role.setName(RoleName.ROLE_CUSTOMER);
    role.setPermissions(null);

    User user =
        User.builder()
            .id(UUID.randomUUID())
            .email("customer@example.com")
            .password("password")
            .roles(Set.of(role))
            .build();

    // Act - should not throw exception
    UserPrincipal principal = UserPrincipal.create(user);

    // Assert
    assertNotNull(principal);
    assertTrue(principal.getRoles().contains("ROLE_CUSTOMER"));
    assertTrue(principal.getPermissions().isEmpty());
  }
}
