package com.lofi.lofiapps.service.impl.usecase.rbac;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.lofi.lofiapps.dto.response.RoleResponse;
import com.lofi.lofiapps.entity.Role;
import com.lofi.lofiapps.enums.RoleName;
import com.lofi.lofiapps.exception.ResourceNotFoundException;
import com.lofi.lofiapps.repository.RoleRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetRoleByIdUseCaseTest {

  @Mock private RoleRepository roleRepository;

  @InjectMocks private GetRoleByIdUseCase getRoleByIdUseCase;

  @Test
  void execute_ShouldReturnRole_WhenRoleExists() {
    // Arrange
    UUID roleId = UUID.randomUUID();
    Role role =
        Role.builder().id(roleId).name(RoleName.ROLE_ADMIN).description("Admin Role").build();

    when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));

    // Act
    RoleResponse result = getRoleByIdUseCase.execute(roleId);

    // Assert
    assertNotNull(result);
    assertEquals(roleId, result.getId());
    assertEquals(RoleName.ROLE_ADMIN, result.getName());
    assertEquals("Admin Role", result.getDescription());
    verify(roleRepository).findById(roleId);
  }

  @Test
  void execute_ShouldThrowResourceNotFoundException_WhenRoleNotFound() {
    // Arrange
    UUID roleId = UUID.randomUUID();
    when(roleRepository.findById(roleId)).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(ResourceNotFoundException.class, () -> getRoleByIdUseCase.execute(roleId));
    verify(roleRepository).findById(roleId);
  }
}
