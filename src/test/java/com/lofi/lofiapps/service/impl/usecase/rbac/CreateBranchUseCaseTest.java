package com.lofi.lofiapps.service.impl.usecase.rbac;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.lofi.lofiapps.dto.request.CreateBranchRequest;
import com.lofi.lofiapps.dto.response.BranchResponse;
import com.lofi.lofiapps.entity.Branch;
import com.lofi.lofiapps.repository.BranchRepository;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateBranchUseCaseTest {

  @Mock private BranchRepository branchRepository;

  @InjectMocks private CreateBranchUseCase createBranchUseCase;

  private CreateBranchRequest request;
  private Branch savedBranch;
  private UUID branchId;

  @BeforeEach
  void setUp() {
    branchId = UUID.randomUUID();

    request =
        CreateBranchRequest.builder()
            .name("Main Branch")
            .address("123 Main Street")
            .city("Jakarta")
            .state("DKI Jakarta")
            .zipCode("12345")
            .phone("+6281234567890")
            .longitude(BigDecimal.valueOf(106.8456))
            .latitude(BigDecimal.valueOf(-6.2088))
            .build();

    savedBranch =
        Branch.builder()
            .id(branchId)
            .name("Main Branch")
            .address("123 Main Street")
            .city("Jakarta")
            .state("DKI Jakarta")
            .zipCode("12345")
            .phone("+6281234567890")
            .longitude(BigDecimal.valueOf(106.8456))
            .latitude(BigDecimal.valueOf(-6.2088))
            .build();
  }

  @Test
  @DisplayName("Execute should create branch successfully with all fields")
  void execute_ShouldCreateBranchSuccessfully() {
    // Arrange
    when(branchRepository.save(any(Branch.class))).thenReturn(savedBranch);

    // Act
    BranchResponse result = createBranchUseCase.execute(request);

    // Assert
    assertNotNull(result);
    assertEquals(branchId, result.getId());
    assertEquals("Main Branch", result.getName());
    assertEquals("123 Main Street", result.getAddress());
    assertEquals("Jakarta", result.getCity());
    assertEquals("DKI Jakarta", result.getState());
    assertEquals("12345", result.getZipCode());
    assertEquals("+6281234567890", result.getPhone());
    assertEquals(BigDecimal.valueOf(106.8456), result.getLongitude());
    assertEquals(BigDecimal.valueOf(-6.2088), result.getLatitude());
    verify(branchRepository).save(any(Branch.class));
  }

  @Test
  @DisplayName("Execute should create branch with minimal fields")
  void execute_ShouldCreateBranchWithMinimalFields() {
    // Arrange
    CreateBranchRequest minimalRequest =
        CreateBranchRequest.builder().name("Minimal Branch").city("Bandung").build();

    Branch minimalBranch =
        Branch.builder().id(UUID.randomUUID()).name("Minimal Branch").city("Bandung").build();

    when(branchRepository.save(any(Branch.class))).thenReturn(minimalBranch);

    // Act
    BranchResponse result = createBranchUseCase.execute(minimalRequest);

    // Assert
    assertNotNull(result);
    assertEquals("Minimal Branch", result.getName());
    assertEquals("Bandung", result.getCity());
    verify(branchRepository).save(any(Branch.class));
  }

  @Test
  @DisplayName("Execute should propagate repository exceptions")
  void execute_ShouldPropagateRepositoryExceptions() {
    // Arrange
    when(branchRepository.save(any(Branch.class)))
        .thenThrow(new RuntimeException("Database error"));

    // Act & Assert
    assertThrows(RuntimeException.class, () -> createBranchUseCase.execute(request));
    verify(branchRepository).save(any(Branch.class));
  }
}
