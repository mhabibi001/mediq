package com.quantumx.mediq.service;

import com.quantumx.mediq.model.User;
import com.quantumx.mediq.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
@Primary
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Autowired
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        // Try to find user by email first, if not found, try username
        User user = userRepository.findByEmail(identifier)
                .orElseGet(() -> userRepository.findByUsername(identifier)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found with email or username: " + identifier))
                );

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())  // Use username here
                .password(user.getPassword()) // Must be an encoded password
                .roles(user.getRole()) // Ensure this is properly configured
                .build();
    }
}
