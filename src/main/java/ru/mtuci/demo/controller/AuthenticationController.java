package ru.mtuci.demo.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import ru.mtuci.demo.configuration.JwtTokenProvider;
import ru.mtuci.demo.model.ApplicationUser;
import ru.mtuci.demo.model.Requests;
import ru.mtuci.demo.service.TokenService;
import ru.mtuci.demo.service.UserService;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
@AllArgsConstructor
public class AuthenticationController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenService tokenService;
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
    public ResponseEntity<?> login(@RequestBody Requests.LoginRequest reqApplicationUser) {
        try {

            String email = reqApplicationUser.getEmail();
            UUID deviceId = UUID.fromString(reqApplicationUser.getDeviceId());
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

            List<String> tokens = tokenService.issueTokenPair(
                    email,
                    deviceId,
                    applicationUser.getRole().getGrantedAuthorities()
            );

            HashMap<String, Object> map = new HashMap<>();
            map.put("email", email);
            HashMap<String, String> tokensDict = new HashMap<>();
            tokensDict.put("accessToken", tokens.get(0));
            tokensDict.put("refreshToken", tokens.get(1));
            map.put("tokens", tokensDict);
            map.put("username", applicationUser.getUsername());

            return ResponseEntity.ok(map);
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body("Something went wrong");
                    .body(e.getMessage());
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody Requests.TokenRefreshRequest reqTokenRefresh) {

        try {
            List<String> tokens = tokenService.refreshTokenPair(
                    reqTokenRefresh.getDeviceId(),
                    reqTokenRefresh.getRefreshToken());

            if (tokens == null || tokens.isEmpty()) {
                return ResponseEntity.ok("Invalid refresh token or device ID");
            }

            HashMap<String, String> map = new HashMap<>();
            map.put("accessToken", tokens.get(0));
            map.put("refreshToken", tokens.get(1));

            return ResponseEntity.ok(map);
        }
        catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());
        }
    }
}
