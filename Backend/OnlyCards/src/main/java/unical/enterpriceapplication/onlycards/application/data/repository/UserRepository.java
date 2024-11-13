package unical.enterpriceapplication.onlycards.application.data.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import unical.enterpriceapplication.onlycards.application.data.entities.accounts.Role;
import unical.enterpriceapplication.onlycards.application.data.entities.accounts.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    boolean existsByEmail(String email);
    // Metodo aggiuntivo per trovare un utente per email
    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    List<User> findAllByRolesNotContaining(Role role);

    List<User> findAllByRolesContaining(Role role);

    @NonNull
    Page<User> findAll(@NonNull Pageable pageable);

    Page<User> findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(String username, String email, Pageable pageable);
    Page<User> findByUsernameContainingIgnoreCase(String username, Pageable pageable);
    Page<User> findByEmailContainingIgnoreCase(String email, Pageable pageable);

    Page<User> findByUsernameContainingIgnoreCaseAndEmailContainingIgnoreCase(String username, String email, Pageable pageable);
    boolean existsByUsername(String uniqueUsername);
}
