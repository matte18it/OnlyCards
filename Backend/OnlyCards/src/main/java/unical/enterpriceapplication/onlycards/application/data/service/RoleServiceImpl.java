package unical.enterpriceapplication.onlycards.application.data.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import unical.enterpriceapplication.onlycards.application.data.entities.accounts.Role;
import unical.enterpriceapplication.onlycards.application.data.repository.RoleRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService{
    private final RoleRepository roleRepository;

    @Override
    public void save(Role role) {
        roleRepository.save(role);
    }

    @Override
    public Optional<Role> findByName(String name) {
        return roleRepository.findByName(name);
    }

}
