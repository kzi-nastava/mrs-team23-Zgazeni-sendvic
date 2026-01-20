package ZgazeniSendvic.Server_Back_ISS;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())                 // disable CSRF for Postman/testing
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll()); // allow all endpoints

        return http.build();
    }

}
