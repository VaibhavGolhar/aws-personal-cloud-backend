package com.btech_major_project.Personal_Cloud.auth;

import com.btech_major_project.Personal_Cloud.user.UserRepository;
import com.btech_major_project.Personal_Cloud.user.User;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AppUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public AppUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Username is the username
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        com.btech_major_project.Personal_Cloud.user.User u = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return new org.springframework.security.core.userdetails.User(
                u.getUsername(),
                u.getPasswordHash(),
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }
}
