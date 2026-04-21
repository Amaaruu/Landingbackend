package Landing.Backend.repository;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import Landing.Backend.model.Transaction;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Integer> {

}
