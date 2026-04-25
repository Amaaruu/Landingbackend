package Landing.Backend.service;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import Landing.Backend.model.Transaction;
import Landing.Backend.model.User;
import Landing.Backend.model.DesignPlan; // Cambiado de Plan a DesignPlan
import Landing.Backend.dto.TransactionRequestDTO;
import Landing.Backend.repository.TransactionRepository;
import Landing.Backend.repository.UserRepository;
import Landing.Backend.repository.DesignPlanRepository; // Cambiado
import Landing.Backend.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class TransactionService {
    
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final DesignPlanRepository designPlanRepository; // Cambiado

    public Transaction createTransaction(TransactionRequestDTO request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        
        // Aquí usamos DesignPlan que es el nombre real de tu modelo
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
        // Asegúrate de tener este método en el TransactionRepository
        return transactionRepository.findByUser_UserId(userId);
    }

    public Optional<Transaction> getTransactionById(Integer id) { 
        return transactionRepository.findById(id); 
    }

    public Transaction updateTransactionStatus(Integer id, String newStatus) {
        return transactionRepository.findById(id).map(transaction -> {
            transaction.setStatus(newStatus);
            return transactionRepository.save(transaction);
        }).orElseThrow(() -> new ResourceNotFoundException("Transacción no encontrada"));
    }
}