package com.intsof.samples.entra.service;

import com.intsof.samples.entra.model.User;
import com.intsof.samples.entra.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public boolean authenticate(String email, String password) {
        User user = userRepository.findByEmail(email);
        if (user == null) return false;
        return BCrypt.checkpw(password, user.getPasswordHash());
    }
}