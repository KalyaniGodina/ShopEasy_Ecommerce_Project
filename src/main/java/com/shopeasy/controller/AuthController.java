package com.shopeasy.controller;

import com.shopeasy.model.User;
import com.shopeasy.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    public record SignupRequest(String name, String email, String password) {}
    public record LoginRequest(String email, String password) {}

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authManager;
    private final SecurityContextRepository contextRepo = new HttpSessionSecurityContextRepository();

    public AuthController(UserRepository users, PasswordEncoder encoder, AuthenticationManager authManager) {
        this.users = users;
        this.encoder = encoder;
        this.authManager = authManager;
    }

    @PostMapping("/signup")
    public ResponseEntity<Map<String, Object>> signup(@RequestBody SignupRequest req,
                                                      HttpServletRequest request, HttpServletResponse response) {
        if (req == null || blank(req.name()) || blank(req.email()) || blank(req.password()))
            throw bad("Name, email and password are required");
        String email = req.email().trim().toLowerCase();
        if (!EMAIL.matcher(email).matches()) throw bad("Please enter a valid email address");
        if (req.password().length() < 8) throw bad("Password must be at least 8 characters");
        if (users.existsByEmail(email))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An account with this email already exists");

        User user = users.save(new User(req.name().trim(), email, encoder.encode(req.password()), "USER"));
        signIn(email, req.password(), request, response);   // log the new user in straight away
        return ResponseEntity.status(HttpStatus.CREATED).body(describe(user));
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody LoginRequest req,
                                     HttpServletRequest request, HttpServletResponse response) {
        if (req == null || blank(req.email()) || blank(req.password()))
            throw bad("Email and password are required");
        String email = req.email().trim().toLowerCase();
        try {
            signIn(email, req.password(), request, response);
        } catch (AuthenticationException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }
        return describe(users.findByEmail(email).orElseThrow());
    }

    @PostMapping("/logout")
    public Map<String, Object> logout(HttpServletRequest request, HttpServletResponse response, Authentication auth) {
        new SecurityContextLogoutHandler().logout(request, response, auth);
        return Map.of("authenticated", false);
    }

    @GetMapping("/me")
    public Map<String, Object> me(Authentication auth) {
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal()))
            return Map.of("authenticated", false);
        return users.findByEmail(auth.getName()).map(AuthController::describe)
                .orElse(Map.of("authenticated", false));
    }

    private void signIn(String email, String rawPassword, HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = authManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(email, rawPassword));
        if (request.getSession(false) != null) request.changeSessionId();   // prevent session fixation
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
        contextRepo.saveContext(context, request, response);                // stores it in the HTTP session
    }

    private static Map<String, Object> describe(User u) {
        return Map.of("authenticated", true, "name", u.getName(), "email", u.getEmail(), "role", u.getRole());
    }

    private static boolean blank(String s) { return s == null || s.isBlank(); }
    private static ResponseStatusException bad(String m) { return new ResponseStatusException(HttpStatus.BAD_REQUEST, m); }
}
