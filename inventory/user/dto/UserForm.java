package ru.kurs.inventory.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UserForm {

    @NotBlank
    @Size(max = 50)
    private String username;

    @Size(max = 200)
    private String password;

    private boolean enabled = true;

    private boolean roleEmployee;
    private boolean roleManager;
    private boolean roleAdmin;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isRoleEmployee() {
        return roleEmployee;
    }

    public void setRoleEmployee(boolean roleEmployee) {
        this.roleEmployee = roleEmployee;
    }

    public boolean isRoleManager() {
        return roleManager;
    }

    public void setRoleManager(boolean roleManager) {
        this.roleManager = roleManager;
    }

    public boolean isRoleAdmin() {
        return roleAdmin;
    }

    public void setRoleAdmin(boolean roleAdmin) {
        this.roleAdmin = roleAdmin;
    }
}
