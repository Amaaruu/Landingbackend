package Landing.Backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import Landing.Backend.dto.TransactionResponseDTO;
import Landing.Backend.dto.TransactionRequestDTO;
import Landing.Backend.model.Transaction;
import Landing.Backend.service.TransactionService;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@Tag(name = "Transacciones", description = "Gestión de pagos y planes de usuario")
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    @Operation(summary = "Crear nueva transacción", description = "Registra el pago de un plan. El usuario se deduce del JWT.")
    public ResponseEntity<TransactionResponseDTO> createTransaction(
            @Valid @RequestBody TransactionRequestDTO requestDTO) {
        Transaction transaction = transactionService.createTransaction(requestDTO);
        return ResponseEntity.status(201).body(convertToResponseDTO(transaction));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar estado de pago (solo admin)")
    public ResponseEntity<TransactionResponseDTO> updateTransactionStatus(
            @PathVariable Integer id,
            @RequestParam String status) {
        Transaction updatedTransaction = transactionService.updateTransactionStatus(id, status);
        return ResponseEntity.ok(convertToResponseDTO(updatedTransaction));
    }

    @GetMapping("/my")
    @Operation(summary = "Listar mis transacciones", description = "El usuario autenticado ve solo sus propias transacciones.")
    public ResponseEntity<List<TransactionResponseDTO>> getMyTransactions(Authentication authentication) {
        List<TransactionResponseDTO> transactions = transactionService
                .getTransactionsByAuthenticatedUser()
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar transacciones por usuario (solo admin)")
    public ResponseEntity<List<TransactionResponseDTO>> getTransactionsByUser(
            @PathVariable Integer userId) {
        List<TransactionResponseDTO> transactions = transactionService.getTransactionsByUserId(userId)
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(transactions);
    }

    private TransactionResponseDTO convertToResponseDTO(Transaction transaction) {
        TransactionResponseDTO dto = new TransactionResponseDTO();
        dto.setTransactionId(transaction.getTransactionId());
        dto.setUserId(transaction.getUser().getUserId());
        dto.setUserName(transaction.getUser().getName());
        dto.setPlanId(transaction.getPlan().getPlanId());
        dto.setPlanName(transaction.getPlan().getName());
        dto.setPaymentMethod(transaction.getPaymentMethod());
        dto.setStatus(transaction.getStatus());
        dto.setPaidAt(transaction.getPaidAt());
        return dto;
    }
}