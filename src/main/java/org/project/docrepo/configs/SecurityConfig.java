package org.project.docrepo.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        // Rule 1: Allow access to all /register/** URLs, plus CSS and JS files.
                        .requestMatchers("/register/**", "/css/**", "/js/**").permitAll()
                        // Rule 2: All other requests must be authenticated.
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login") // Specify our custom login page
                        .defaultSuccessUrl("/", true) // On success, go to the homepage
                        .permitAll() // Allow everyone to see the login page
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout") // On logout, redirect to login with a message
                        .permitAll()
                );
        return http.build();
    }
}