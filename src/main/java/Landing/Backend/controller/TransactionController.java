package Landing.Backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import Landing.Backend.dto.TransactionResponseDTO;
import Landing.Backend.dto.TransactionRequestDTO; // Asegúrate de tener este DTO
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
    @Operation(summary = "Crear nueva transacción", description = "Registra un intento de pago para un plan específico")
    public ResponseEntity<TransactionResponseDTO> createTransaction(@Valid @RequestBody TransactionRequestDTO requestDTO) {
        Transaction transaction = transactionService.createTransaction(requestDTO);
        return ResponseEntity.status(201).body(convertToResponseDTO(transaction));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Actualizar estado de pago")
    public ResponseEntity<TransactionResponseDTO> updateTransactionStatus(
            @PathVariable Integer id, 
            @RequestParam String status) {
        Transaction updatedTransaction = transactionService.updateTransactionStatus(id, status);
        return ResponseEntity.ok(convertToResponseDTO(updatedTransaction));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Listar transacciones por usuario")
    public ResponseEntity<List<TransactionResponseDTO>> getTransactionsByUser(@PathVariable Integer userId) {
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
        dto.setPlanId(transaction.getPlan().getPlanId());
        dto.setPaymentMethod(transaction.getPaymentMethod());
        dto.setStatus(transaction.getStatus());
        dto.setPaidAt(transaction.getPaidAt());
        return dto;
    }
}