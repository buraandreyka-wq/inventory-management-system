package ru.kurs.inventory.user;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kurs.inventory.common.EntityNotFoundException;
import ru.kurs.inventory.security.RoleName;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User create(String username, String rawPassword, Set<RoleName> roleNames, boolean enabled) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Имя пользователя уже занято: " + username);
        }
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("Пароль обязателен");
        }

        User user = new User(username, passwordEncoder.encode(rawPassword));
        user.setEnabled(enabled);
        user.setRoles(resolveRoles(roleNames));
        return userRepository.save(user);
    }

    public User update(Long id, String username, String rawPasswordOrNull, Set<RoleName> roleNames, boolean enabled) {
        User user = getById(id);

        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Имя пользователя обязательно");
        }
        if (!user.getUsername().equals(username) && userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Имя пользователя уже занято: " + username);
        }

        user.setUsername(username);
        user.setEnabled(enabled);
        user.setRoles(resolveRoles(roleNames));

        if (rawPasswordOrNull != null && !rawPasswordOrNull.isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(rawPasswordOrNull));
        }

        return userRepository.save(user);
    }

    public void setEnabled(Long id, boolean enabled) {
        User user = getById(id);
        user.setEnabled(enabled);
        userRepository.save(user);
    }

    public void delete(Long id) {
        User user = getById(id);
        // safety: do not allow deleting last admin
        boolean isAdmin = user.getRoles().stream().anyMatch(r -> r.getName() == RoleName.ROLE_ADMIN);
        if (isAdmin && userRepository.findAll().stream()
                .filter(u -> u.getRoles().stream().anyMatch(r -> r.getName() == RoleName.ROLE_ADMIN))
                .count() <= 1) {
            throw new IllegalStateException("Нельзя удалить последнего администратора");
        }
        userRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public User getByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден: " + username));
    }

    @Transactional(readOnly = true)
    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден: id=" + id));
    }

    private Set<Role> resolveRoles(Set<RoleName> roleNames) {
        Set<Role> roles = new HashSet<>();
        if (roleNames == null || roleNames.isEmpty()) {
            // default minimal role
            roleRepository.findByName(RoleName.ROLE_EMPLOYEE).ifPresent(roles::add);
            return roles;
        }
        for (RoleName rn : roleNames) {
            Role role = roleRepository.findByName(rn)
                    .orElseThrow(() -> new IllegalStateException("Роль не найдена: " + rn));
            roles.add(role);
        }
        return roles;
    }
}
