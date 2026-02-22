package it.unipi.MarketLens.controller;

import it.unipi.MarketLens.dto.LoginRequest;
import it.unipi.MarketLens.dto.ModifyEmailRequest;
import it.unipi.MarketLens.dto.ModifyPasswordRequest;
import it.unipi.MarketLens.dto.SignupRequest;
import it.unipi.MarketLens.model.User;
import it.unipi.MarketLens.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

//Questo Controller gestisce tutte le chiamate API relative all'autenticazione e agli utenti
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private AuthService authService;

    // Per la registrazione di un nuovo utente
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest request) {
        try {
            String result = authService.registerUser(
                    request.getFullName(),
                    request.getEmail(),
                    request.getPassword(),
                    request.getUsername()
            );

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Controlla se email e password sono corrette
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            User user = authService.loginUser(request.getEmail(), request.getPassword());
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.status(401).body("login failed" + e.getMessage());
        }
    }

    @PostMapping("/modify-email")
    public ResponseEntity<?> modifyEmail(@RequestBody ModifyEmailRequest request) {
        try {
            User updatedUser = authService.modifyEmail(request);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/modify-password")
    public ResponseEntity<?> modifyPassword(@RequestBody ModifyPasswordRequest request) {
        try {
            User updatedUser = authService.modifyPassword(request);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}