package ru.mtuci.demo.service;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import ru.mtuci.demo.model.ApplicationUser;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Service
public interface UserService extends UserDetailsService {
    List<ApplicationUser> findAllUsers();

    ApplicationUser findUserByEmail(String email);
    ApplicationUser findUserById(UUID id);

    ApplicationUser saveUser(ApplicationUser applicationUser);

    ApplicationUser updateUser(ApplicationUser applicationUser);

    void deleteUser(String email);

    Collection<? extends GrantedAuthority> getAuthorities();
}
