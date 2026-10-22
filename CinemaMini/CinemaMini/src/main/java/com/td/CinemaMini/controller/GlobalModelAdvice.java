package com.td.CinemaMini.controller;

import com.td.CinemaMini.exception.BusinessException;
import com.td.CinemaMini.model.*;
import com.td.CinemaMini.model.enums.Role;
import com.td.CinemaMini.service.account.AccountService;
import java.security.Principal;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@ControllerAdvice
public class GlobalModelAdvice {
    private final AccountService accounts;

    public GlobalModelAdvice(AccountService accounts) {
        this.accounts = accounts;
    }

    @ModelAttribute
    public void navigation(Principal principal, Model model) {
        model.addAttribute("loggedIn", principal != null);
        model.addAttribute("admin", false);
        if (principal != null) {
            try {
                UserAccount account = accounts.account(principal.getName());
                model.addAttribute("currentAccount", account);
                model.addAttribute("admin", account.getRole() == Role.ADMIN);
                if (account.getRole() == Role.USER) {
                    model.addAttribute("currentCustomer", accounts.customer(account));
                }
            } catch (BusinessException inactive) {
                model.addAttribute("loggedIn", false);
                model.addAttribute("admin", false);
            }
        }
    }
}
