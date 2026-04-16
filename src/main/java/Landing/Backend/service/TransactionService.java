package Landing.Backend.service;

import org.springframework.stereotype.Service;
import Landing.Backend.model.Transaction;
import Landing.Backend.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
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

    //unico método de actualización permitido (Cambiar estado del pago)
    public Transaction updateTransactionStatus(Integer id, String newStatus) {
        return transactionRepository.findById(id).map(transaction -> {
            transaction.setStatus(newStatus);
            return transactionRepository.save(transaction);
        }).orElseThrow(() -> new RuntimeException("Transacción no encontrada con ID: " + id));
    }
}