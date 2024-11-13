package unical.enterpriceapplication.onlycards.application.data.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import unical.enterpriceapplication.onlycards.application.data.entities.accounts.Role;
@Repository
public interface RoleRepository extends CrudRepository<Role, Long> {
    Optional<Role> findByName(String admin);

    List<Role> findByUsersId(UUID sub);
    List<Role> findByUsersEmail(String email);

}
