package com.example.account.controller;

import com.example.account.dto.*;
import com.example.account.service.CustomerService;
import com.example.account.util.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/")
public class AccountController {

    private final CustomerService customerService;
    private final JwtUtil jwtUtil;

    public AccountController(CustomerService customerService, JwtUtil jwtUtil) {
        this.customerService = customerService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping
    public ResponseEntity<String> root() {
        return ResponseEntity.ok("Account service is up and running");
    }

    @PostMapping("token")
    public ResponseEntity<?> token(@RequestBody LoginRequest login) {
        if (login == null || !StringUtils.hasText(login.getUsername()) || !StringUtils.hasText(login.getPassword())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("username and password required");
        }

        CustomerDto customer = customerService.findByEmail(login.getUsername());
        if (customer == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("invalid credentials");
        }

        // Simple password compare - assumes stored password is plain text; in production use hashing
        if (!login.getPassword().equals(customer.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("invalid credentials");
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("email", customer.getEmail());
        claims.put("name", customer.getName());

        String token = jwtUtil.generateToken(customer.getEmail(), claims);

        return ResponseEntity.ok(new TokenResponse(token));
    }

    @PostMapping("register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        if (req == null || !StringUtils.hasText(req.getEmail()) || !StringUtils.hasText(req.getPassword()) || !StringUtils.hasText(req.getName())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("name, email and password required");
        }

        // create via Data Service
        CustomerDto created = customerService.createCustomer(req);
        if (created == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("could not create customer");
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
