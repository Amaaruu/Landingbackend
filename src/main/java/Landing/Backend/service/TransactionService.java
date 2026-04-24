package Landing.Backend.service;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import Landing.Backend.model.Transaction;
import Landing.Backend.repository.TransactionRepository;
import Landing.Backend.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class TransactionService {
    
    private final TransactionRepository transactionRepository;

    public Transaction saveTransaction(Transaction transaction) { 
        return transactionRepository.save(transaction); 
    }

    public List<Transaction> getAllTransactions() { 
        return transactionRepository.findAll(); 
    }

    public Optional<Transaction> getTransactionById(Integer id) { 
        return transactionRepository.findById(id); 
    }

    public Transaction updateTransactionStatus(Integer id, String newStatus) {
        return transactionRepository.findById(id).map(transaction -> {
            transaction.setStatus(newStatus);
            return transactionRepository.save(transaction);
        }).orElseThrow(() -> new ResourceNotFoundException("Transacción no encontrada con ID: " + id));
    }
}