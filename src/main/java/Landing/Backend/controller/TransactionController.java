package Landing.Backend.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import Landing.Backend.dto.TransactionRequestDTO;
import Landing.Backend.dto.TransactionResponseDTO;
import Landing.Backend.model.DesignPlan;
import Landing.Backend.model.Transaction;
import Landing.Backend.model.User;
import Landing.Backend.service.DesignPlanService;
import Landing.Backend.service.TransactionService;
import Landing.Backend.service.UserService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    // Inyectamos estos servicios para validar que el usuario y el plan existan
    private final UserService userService;
    private final DesignPlanService designPlanService;

    @PostMapping
    public ResponseEntity<TransactionResponseDTO> createTransaction(@RequestBody TransactionRequestDTO requestDTO) {
        // 1. Validar que el Usuario y el Plan de Diseño realmente existan en la BD
        User user = userService.findById(requestDTO.getUserId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
                
        DesignPlan plan = designPlanService.getPlanById(requestDTO.getPlanId())
                .orElseThrow(() -> new RuntimeException("Plan no encontrado"));

        // 2. Construir la Entidad con sus relaciones reales
        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setPlan(plan);
        transaction.setPaymentMethod(requestDTO.getPaymentMethod());
        transaction.setStatus(requestDTO.getStatus());

        Transaction createdTransaction = transactionService.saveTransaction(transaction);
        return new ResponseEntity<>(convertToResponseDTO(createdTransaction), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<TransactionResponseDTO>> getAllTransactions() {
        List<TransactionResponseDTO> transactions = transactionService.getAllTransactions().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponseDTO> getTransactionById(@PathVariable Integer id) {
        return transactionService.getTransactionById(id)
                .map(t -> ResponseEntity.ok(convertToResponseDTO(t)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<TransactionResponseDTO> updateTransactionStatus(
            @PathVariable Integer id, 
            @RequestParam String status) {
        try {
            Transaction updatedTransaction = transactionService.updateTransactionStatus(id, status);
            return ResponseEntity.ok(convertToResponseDTO(updatedTransaction));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // --- MAPPER ---
    private TransactionResponseDTO convertToResponseDTO(Transaction transaction) {
        TransactionResponseDTO dto = new TransactionResponseDTO();
        dto.setTransactionId(transaction.getTransactionId());
        dto.setUserId(transaction.getUser().getUserId());
        // Extraemos nombres directamente para facilitarle la vida al Frontend
        dto.setUserName(transaction.getUser().getName() + " " + transaction.getUser().getLastName());
        dto.setPlanId(transaction.getPlan().getPlanId());
        dto.setPlanName(transaction.getPlan().getName());
        dto.setPaymentMethod(transaction.getPaymentMethod());
        dto.setStatus(transaction.getStatus());
        dto.setPaidAt(transaction.getPaidAt());
        return dto;
    }
}