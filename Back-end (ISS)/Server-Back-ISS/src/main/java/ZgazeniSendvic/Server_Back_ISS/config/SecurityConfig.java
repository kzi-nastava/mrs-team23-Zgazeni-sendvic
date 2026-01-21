package ZgazeniSendvic.Server_Back_ISS.config;

import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

public class SecurityConfig {

    @Bean
    //Skeleton, should use @Preautorize anyway, so most of this will be removed
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> {}) // Access from other sites (localhost:4200)
                .csrf(csrf -> csrf.disable()) // JWT does this, so shutdown for now
                //.authorizeHttpRequests(auth -> auth.anyRequest().permitAll()); //would permit all
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll() //for testing purposes
                        .requestMatchers("/*").permitAll() // Everyone can access Static
                        .requestMatchers("/*/*").permitAll() // Everyone can access Static
                        .requestMatchers("/api/auth/login").permitAll() // Everyone can access Login
                        .requestMatchers("/api/auth/register").permitAll()
                ).sessionManagement(session -> { // do not use cookies
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
                });
                // JWT before everything else, though not used as for now
                //.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration conf)
            throws Exception {
        return conf.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        return encoder;
    }

    //CORS config file needed, otherwise reqeusts from port 4200 wont owrk
    //also DaoAuthenticationProvider, though not for now


}
