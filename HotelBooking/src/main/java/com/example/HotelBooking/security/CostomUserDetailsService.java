package com.example.HotelBooking.security;

import com.example.HotelBooking.entities.User;
import com.example.HotelBooking.exceptions.NotFoundException;
import com.example.HotelBooking.respositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CostomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user=userRepository.findByEmail(username).orElseThrow(()->new NotFoundException("User not found"));
        return AuthUser.builder()
                .user(user)
                .build();
    }
}
