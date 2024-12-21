package ru.mtuci.demo.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import ru.mtuci.demo.configuration.JwtTokenProvider;
import ru.mtuci.demo.model.ApplicationUser;
import ru.mtuci.demo.service.UserService;

@RestController
@RequestMapping("/auth")
@AllArgsConstructor
public class AuthenticationController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private AuthenticationManager authenticationManager;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody ApplicationUser applicationUser) {
        try {

            if (userService.findUserByEmail(applicationUser.getEmail()) != null) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("This email is already in use");
            }

            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            applicationUser.setPassword(encoder.encode(applicationUser.getPassword()));

            userService.saveUser(applicationUser);

            return ResponseEntity.status(HttpStatus.OK)
                    .body(applicationUser.getId());
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Something went wrong");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody ApplicationUser reqApplicationUser) {
        try {

            String email = reqApplicationUser.getEmail();
            ApplicationUser applicationUser = userService.findUserByEmail(email);

            if (applicationUser == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("User not found");
            }

            authenticationManager
                    .authenticate(
                            new UsernamePasswordAuthenticationToken(
                                    email, reqApplicationUser.getPassword())
                    );

            String token = jwtTokenProvider
                    .createToken(email, applicationUser.getRole().getGrantedAuthorities());

            return ResponseEntity.ok(token);
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Something went wrong");
        }
    }
}
