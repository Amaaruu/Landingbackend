// src/main/java/Landing/Backend/service/TransactionService.java
package Landing.Backend.service;

import Landing.Backend.dto.TransactionRequestDTO;
import Landing.Backend.exception.BusinessLogicException;
import Landing.Backend.exception.ResourceNotFoundException;
import Landing.Backend.model.DesignPlan;
import Landing.Backend.model.Transaction;
import Landing.Backend.model.User;
import Landing.Backend.repository.DesignPlanRepository;
import Landing.Backend.repository.TransactionRepository;
import Landing.Backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final DesignPlanRepository  designPlanRepository;

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new BusinessLogicException("No autenticado", HttpStatus.UNAUTHORIZED);
        }
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new BusinessLogicException("Usuario no encontrado", HttpStatus.UNAUTHORIZED));
    }

    public Transaction createTransaction(TransactionRequestDTO request) {
        // El usuario viene del JWT, nunca del body del cliente
        User user = getAuthenticatedUser();

        DesignPlan plan = designPlanRepository.findById(request.getPlanId())
                .orElseThrow(() -> new ResourceNotFoundException("Plan no encontrado"));

        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setPlan(plan);
        transaction.setPaymentMethod(request.getPaymentMethod());
        transaction.setStatus(request.getStatus());

        return transactionRepository.save(transaction);
    }

    public List<Transaction> getTransactionsByUserId(Integer userId) {
        return transactionRepository.findByUser_UserId(userId);
    }

    public Optional<Transaction> getTransactionById(Integer id) {
        return transactionRepository.findById(id);
    }

    public Transaction updateTransactionStatus(Integer id, String newStatus) {
        return transactionRepository.findById(id).map(t -> {
            t.setStatus(newStatus);
            return transactionRepository.save(t);
        }).orElseThrow(() -> new ResourceNotFoundException("Transacción no encontrada"));
    }
}