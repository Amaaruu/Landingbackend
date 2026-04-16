package Landing.Backend.repository;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import Landing.Backend.model.Transaction;
import Landing.Backend.model.User;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Integer> {

}
