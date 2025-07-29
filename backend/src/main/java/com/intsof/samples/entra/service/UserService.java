package com.intsof.samples.entra.service;

import com.intsof.samples.entra.model.User;
import com.intsof.samples.entra.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public boolean authenticate(String email, String password) {
        User user = userRepository.findByEmail(email);
        if (user == null) return false;
        // POC: compare plain text (password field already stores plain text or pre-known hash)
        return user.getPasswordHash().equals(password);
    }
}