package ru.kurs.inventory.bootstrap;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.kurs.inventory.security.RoleName;
import ru.kurs.inventory.user.Role;
import ru.kurs.inventory.user.RoleRepository;
import ru.kurs.inventory.user.User;
import ru.kurs.inventory.user.UserRepository;

import java.util.HashSet;
import java.util.Set;

@Component
public class AdminBootstrap implements ApplicationRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminBootstrap(UserRepository userRepository,
                          RoleRepository roleRepository,
                          PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        // Ensure base roles exist
        Role adminRole = ensureRole(RoleName.ROLE_ADMIN);
        Role managerRole = ensureRole(RoleName.ROLE_MANAGER);
        Role employeeRole = ensureRole(RoleName.ROLE_EMPLOYEE);

        // Demo users (created/updated on startup)
        // admin/admin
        ensureUser(
                "admin",
                "admin",
                new HashSet<>(Set.of(adminRole, managerRole, employeeRole))
        );

        // manager/manager
        ensureUser(
                "manager",
                "manager",
                new HashSet<>(Set.of(managerRole, employeeRole))
        );

        // employee/employee
        ensureUser(
                "employee",
                "employee",
                new HashSet<>(Set.of(employeeRole))
        );
    }

    private User ensureUser(String username, String rawPassword, Set<Role> roles) {
        User user = userRepository.findByUsername(username)
                .orElseGet(() -> new User(username, passwordEncoder.encode(rawPassword)));

        // NOTE: This intentionally resets the password on every start (demo defaults).
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setEnabled(true);
        user.setRoles(roles);

        return userRepository.save(user);
    }

    private Role ensureRole(RoleName roleName) {
        return roleRepository.findByName(roleName)
                .orElseGet(() -> roleRepository.save(new Role(roleName)));
    }
}
