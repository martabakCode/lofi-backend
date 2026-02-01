package com.lofi.lofiapps.service.impl.usecase.rbac;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.lofi.lofiapps.repository.BranchRepository;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DeleteBranchUseCaseTest {

  @Mock private BranchRepository branchRepository;

  @InjectMocks private DeleteBranchUseCase deleteBranchUseCase;

  private UUID branchId;

  @BeforeEach
  void setUp() {
    branchId = UUID.randomUUID();
  }

  @Test
  @DisplayName("Execute should delete branch successfully")
  void execute_ShouldDeleteBranchSuccessfully() {
    // Arrange
    doNothing().when(branchRepository).deleteById(branchId);

    // Act
    deleteBranchUseCase.execute(branchId);

    // Assert
    verify(branchRepository).deleteById(branchId);
  }

  @Test
  @DisplayName("Execute should propagate repository exceptions")
  void execute_ShouldPropagateRepositoryExceptions() {
    // Arrange
    doThrow(new RuntimeException("Database error")).when(branchRepository).deleteById(branchId);

    // Act & Assert
    assertThrows(RuntimeException.class, () -> deleteBranchUseCase.execute(branchId));
    verify(branchRepository).deleteById(branchId);
  }

  @Test
  @DisplayName("Execute should handle non-existent branch gracefully")
  void execute_ShouldHandleNonExistentBranch() {
    // Arrange - JPA deleteById doesn't throw exception for non-existent entity
    doNothing().when(branchRepository).deleteById(branchId);

    // Act - Should not throw exception
    assertDoesNotThrow(() -> deleteBranchUseCase.execute(branchId));

    // Assert
    verify(branchRepository).deleteById(branchId);
  }
}
