package unical.enterpriceapplication.onlycards.application.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import unical.enterpriceapplication.onlycards.application.data.entities.InvalidatedToken;

public interface InvalidatedTokenRepository extends JpaRepository<InvalidatedToken, Long>{
    boolean existsByToken(String token);

    
}
