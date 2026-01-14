package ru.kurs.inventory.user;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.kurs.inventory.security.RoleName;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleName name);
}
