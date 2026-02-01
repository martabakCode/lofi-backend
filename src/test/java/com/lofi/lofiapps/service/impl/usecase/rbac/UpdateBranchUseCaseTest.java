package com.lofi.lofiapps.service.impl.usecase.rbac;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.lofi.lofiapps.dto.request.CreateBranchRequest;
import com.lofi.lofiapps.dto.response.BranchResponse;
import com.lofi.lofiapps.entity.Branch;
import com.lofi.lofiapps.exception.ResourceNotFoundException;
import com.lofi.lofiapps.repository.BranchRepository;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdateBranchUseCaseTest {

  @Mock private BranchRepository branchRepository;

  @InjectMocks private UpdateBranchUseCase updateBranchUseCase;

  private UUID branchId;
  private CreateBranchRequest request;
  private Branch existingBranch;
  private Branch updatedBranch;

  @BeforeEach
  void setUp() {
    branchId = UUID.randomUUID();

    request =
        CreateBranchRequest.builder()
            .name("Updated Branch")
            .address("456 Updated Street")
            .city("Surabaya")
            .state("East Java")
            .zipCode("54321")
            .phone("+6289876543210")
            .longitude(BigDecimal.valueOf(112.7508))
            .latitude(BigDecimal.valueOf(-7.2575))
            .build();

    existingBranch =
        Branch.builder()
            .id(branchId)
            .name("Old Branch")
            .address("123 Old Street")
            .city("Jakarta")
            .state("DKI Jakarta")
            .zipCode("12345")
            .phone("+6281234567890")
            .longitude(BigDecimal.valueOf(106.8456))
            .latitude(BigDecimal.valueOf(-6.2088))
            .build();

    updatedBranch =
        Branch.builder()
            .id(branchId)
            .name("Updated Branch")
            .address("456 Updated Street")
            .city("Surabaya")
            .state("East Java")
            .zipCode("54321")
            .phone("+6289876543210")
            .longitude(BigDecimal.valueOf(112.7508))
            .latitude(BigDecimal.valueOf(-7.2575))
            .build();
  }

  @Test
  @DisplayName("Execute should update branch successfully")
  void execute_ShouldUpdateBranchSuccessfully() {
    // Arrange
    when(branchRepository.findById(branchId)).thenReturn(Optional.of(existingBranch));
    when(branchRepository.save(any(Branch.class))).thenReturn(updatedBranch);

    // Act
    BranchResponse result = updateBranchUseCase.execute(branchId, request);

    // Assert
    assertNotNull(result);
    assertEquals(branchId, result.getId());
    assertEquals("Updated Branch", result.getName());
    assertEquals("456 Updated Street", result.getAddress());
    assertEquals("Surabaya", result.getCity());
    assertEquals("East Java", result.getState());
    assertEquals("54321", result.getZipCode());
    assertEquals("+6289876543210", result.getPhone());
    assertEquals(BigDecimal.valueOf(112.7508), result.getLongitude());
    assertEquals(BigDecimal.valueOf(-7.2575), result.getLatitude());
    verify(branchRepository).findById(branchId);
    verify(branchRepository).save(any(Branch.class));
  }

  @Test
  @DisplayName("Execute should throw exception when branch not found")
  void execute_ShouldThrowException_WhenBranchNotFound() {
    // Arrange
    when(branchRepository.findById(branchId)).thenReturn(Optional.empty());

    // Act & Assert
    ResourceNotFoundException exception =
        assertThrows(
            ResourceNotFoundException.class, () -> updateBranchUseCase.execute(branchId, request));
    assertTrue(exception.getMessage().contains("Branch not found"));
    verify(branchRepository).findById(branchId);
    verify(branchRepository, never()).save(any(Branch.class));
  }

  @Test
  @DisplayName("Execute should update partial fields")
  void execute_ShouldUpdatePartialFields() {
    // Arrange
    CreateBranchRequest partialRequest =
        CreateBranchRequest.builder().name("Partially Updated Branch").city("Bandung").build();

    Branch partiallyUpdatedBranch =
        Branch.builder()
            .id(branchId)
            .name("Partially Updated Branch")
            .address("123 Old Street")
            .city("Bandung")
            .state("DKI Jakarta")
            .zipCode("12345")
            .phone("+6281234567890")
            .build();

    when(branchRepository.findById(branchId)).thenReturn(Optional.of(existingBranch));
    when(branchRepository.save(any(Branch.class))).thenReturn(partiallyUpdatedBranch);

    // Act
    BranchResponse result = updateBranchUseCase.execute(branchId, partialRequest);

    // Assert
    assertNotNull(result);
    assertEquals("Partially Updated Branch", result.getName());
    assertEquals("Bandung", result.getCity());
    assertEquals("123 Old Street", result.getAddress());
    verify(branchRepository).findById(branchId);
    verify(branchRepository).save(any(Branch.class));
  }
}
