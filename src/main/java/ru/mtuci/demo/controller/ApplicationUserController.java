package ru.mtuci.demo.controller;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.mtuci.demo.configuration.JwtTokenProvider;
import ru.mtuci.demo.model.ApplicationUser;
import ru.mtuci.demo.model.Requests;
import ru.mtuci.demo.model.Role;
import ru.mtuci.demo.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/users")
@AllArgsConstructor
public class ApplicationUserController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('modification')")
    public ResponseEntity<?> findUser(@RequestParam(value = "email", required = false) String email) {
        try {
            ApplicationUser reqApplicationUser = userService.findUserByEmail(email);

            if (reqApplicationUser == null) {
                List<ApplicationUser> applicationUsers = userService.findAllUsers();
                return ResponseEntity.ok(applicationUsers);
            }

            return ResponseEntity.ok(reqApplicationUser);
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Something went wrong");
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> findMe(@RequestHeader("Authorization") String authHeader) {
        try {
            authHeader = authHeader.replace("Bearer ", "");

            ApplicationUser user = userService.findUserByEmail(jwtTokenProvider.getUsername(authHeader));

            return ResponseEntity.ok(user);
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Something went wrong");
        }
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateUser(@RequestBody ApplicationUser applicationUser,
                                        @RequestHeader(value = "Authorization") String authHeader) {
        try {

            authHeader = authHeader.replace("Bearer ", "");
            ApplicationUser reqUser = userService.findUserByEmail(
                    jwtTokenProvider.getUsername(authHeader));

            if (reqUser == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("User not found");
            }

            String email = applicationUser.getEmail();

            if (!reqUser.getEmail().equals(email) && reqUser.getRole() != Role.ADMIN) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Email address does not match");
            }

            ApplicationUser updatedUser = userService.updateUser(applicationUser);

            return ResponseEntity.status(HttpStatus.OK)
                    .body(updatedUser.getId());
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Something went wrong");
        }
    }

    @DeleteMapping("/delete")
    @Transactional
    public ResponseEntity<?> deleteUser(@RequestBody Requests.DeleteUserRequest user,
                                        @RequestHeader("Authorization") String authHeader) {
        try {
            authHeader = authHeader.replace("Bearer ", "");
            ApplicationUser reqUser = userService.findUserByEmail(user.getEmail());

            if (reqUser == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("User not found");
            }

            ApplicationUser curUser = userService.findUserByEmail(jwtTokenProvider.getUsername(authHeader));

            if (curUser != reqUser && curUser.getRole() != Role.ADMIN) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Can't delete user");
            }

            userService.deleteUser(user.getEmail());

            return ResponseEntity.ok("User deleted successfully");
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Something went wrong");
        }
    }
}
