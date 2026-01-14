package ru.kurs.inventory.web;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import ru.kurs.inventory.security.RecaptchaProperties;
import ru.kurs.inventory.security.RoleName;
import ru.kurs.inventory.user.UserService;
import ru.kurs.inventory.user.dto.RegisterForm;

import java.util.EnumSet;

@Controller
public class AuthController {

    private final UserService userService;
    private final RecaptchaProperties recaptchaProperties;

    public AuthController(UserService userService, RecaptchaProperties recaptchaProperties) {
        this.userService = userService;
        this.recaptchaProperties = recaptchaProperties;
    }

    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("recaptchaSiteKey", recaptchaProperties.getSiteKey());
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("form", new RegisterForm());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("form") RegisterForm form,
                           BindingResult bindingResult,
                           Model model) {
        if (bindingResult.hasErrors()) {
            return "auth/register";
        }
        if (!form.getPassword().equals(form.getPasswordConfirm())) {
            bindingResult.rejectValue("passwordConfirm", "passwordConfirm.mismatch", "Пароли не совпадают");
            return "auth/register";
        }

        try {
            userService.create(
                    form.getUsername(),
                    form.getPassword(),
                    EnumSet.of(RoleName.ROLE_EMPLOYEE),
                    true
            );
            // после регистрации направляем на страницу входа
            return "redirect:/login?registered";
        } catch (Exception ex) {
            ViewUtils.putError(model, ex);
            return "auth/register";
        }
    }
}
