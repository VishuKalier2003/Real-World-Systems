package authentication.two.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {

        http
            .csrf(csrf -> csrf.disable())   // disable CSRF for APIs
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/users/**").permitAll()   // allow these APIs
                .anyRequest().authenticated()               // secure everything else
            );
        return http.build();
    }
}