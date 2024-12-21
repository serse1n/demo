package ru.mtuci.demo.service.impl;

import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import ru.mtuci.demo.model.ApplicationUser;
import ru.mtuci.demo.model.Role;
import ru.mtuci.demo.service.UserService;
import ru.mtuci.demo.repository.UserRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public List<ApplicationUser> findAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public ApplicationUser findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public ApplicationUser findUserById(UUID id) {
        return userRepository.findById(id)
                .orElse(null);
    }

    @Override
    public ApplicationUser saveUser(ApplicationUser applicationUser) {
        applicationUser.setRole(Role.USER);
        return userRepository.save(applicationUser);
    }

    @Override
    public ApplicationUser updateUser(ApplicationUser applicationUser) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        ApplicationUser reqApplicationUser = userRepository.findByEmail(applicationUser.getEmail());
        reqApplicationUser.setUsername(applicationUser.getUsername());
        reqApplicationUser.setPassword(encoder.encode(applicationUser.getPassword()));

        return userRepository.save(reqApplicationUser);
    }

    @Override
    public void deleteUser(String email) {
        userRepository.deleteByEmail(email);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        ApplicationUser applicationUser = userRepository.findByEmail(email);
        if (applicationUser == null) {
            throw new UsernameNotFoundException(email);
        }
        return UserDetailsImpl.fromApplicationUser(applicationUser);
    }
}
