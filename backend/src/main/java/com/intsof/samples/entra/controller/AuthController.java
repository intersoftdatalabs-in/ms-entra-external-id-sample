package com.intsof.samples.entra.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;



@RestController
public class AuthController {
    @Autowired


    @PostMapping("/login")
    public ResponseEntity<?> login() {
        // Authentication is handled by the filter. If we reach here, user is authenticated.
        return ResponseEntity.ok().build();
    }
}
// Controller for login endpoint