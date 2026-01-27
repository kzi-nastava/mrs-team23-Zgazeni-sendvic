package ZgazeniSendvic.Server_Back_ISS.config;

import ZgazeniSendvic.Server_Back_ISS.security.jwt.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    JwtAuthenticationFilter jwtFilter;

    @Bean
    //Skeleton, should use @Preauthorize anyway, so most of this will be removed
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> {}) // Access from other sites (localhost:4200)
                .csrf(csrf -> csrf.disable()) // JWT does this, so shutdown for now
                //.authorizeHttpRequests(auth -> auth.anyRequest().permitAll()); //would permit all
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll() //for testing purposes
                ).sessionManagement(session -> { // do not use cookies
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
                })
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
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
