package com.lofi.lofiapps.service.impl.usecase.rbac;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.lofi.lofiapps.dto.response.BranchResponse;
import com.lofi.lofiapps.entity.Branch;
import com.lofi.lofiapps.exception.ResourceNotFoundException;
import com.lofi.lofiapps.repository.BranchRepository;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetBranchByIdUseCaseTest {

  @Mock private BranchRepository branchRepository;

  @InjectMocks private GetBranchByIdUseCase getBranchByIdUseCase;

  @Test
  void execute_ShouldReturnBranch_WhenBranchExists() {
    // Arrange
    UUID branchId = UUID.randomUUID();
    Branch branch =
        Branch.builder()
            .id(branchId)
            .name("Main Branch")
            .address("123 Main St")
            .city("Jakarta")
            .state("DKI Jakarta")
            .zipCode("12345")
            .phone("08123456789")
            .longitude(new BigDecimal("106.8456"))
            .latitude(new BigDecimal("-6.2088"))
            .build();

    when(branchRepository.findById(branchId)).thenReturn(Optional.of(branch));

    // Act
    BranchResponse result = getBranchByIdUseCase.execute(branchId);

    // Assert
    assertNotNull(result);
    assertEquals(branchId, result.getId());
    assertEquals("Main Branch", result.getName());
    assertEquals("Jakarta", result.getCity());
    verify(branchRepository).findById(branchId);
  }

  @Test
  void execute_ShouldThrowResourceNotFoundException_WhenBranchNotFound() {
    // Arrange
    UUID branchId = UUID.randomUUID();
    when(branchRepository.findById(branchId)).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(ResourceNotFoundException.class, () -> getBranchByIdUseCase.execute(branchId));
    verify(branchRepository).findById(branchId);
  }
}
