package unical.enterpriceapplication.onlycards.application.data.service;

import unical.enterpriceapplication.onlycards.application.data.entities.accounts.Role;

import java.util.Optional;

public interface RoleService {
    void save(Role role);
    Optional<Role> findByName(String name);
}
