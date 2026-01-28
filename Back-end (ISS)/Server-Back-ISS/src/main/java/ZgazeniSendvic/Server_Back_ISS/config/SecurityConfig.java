package ZgazeniSendvic.Server_Back_ISS.config;

import ZgazeniSendvic.Server_Back_ISS.repository.AccountRepository;
import ZgazeniSendvic.Server_Back_ISS.security.CustomUserDetailsService;
import ZgazeniSendvic.Server_Back_ISS.security.jwt.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import ZgazeniSendvic.Server_Back_ISS.security.auth.RestAuthenticationEntryPoint;
import ZgazeniSendvic.Server_Back_ISS.security.auth.TokenAuthenticationFilter;
import ZgazeniSendvic.Server_Back_ISS.service.AccountServiceImpl;
//import ZgazeniSendvic.Server_Back_ISS.util.TokenUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Arrays;

@Configuration // Marks as config, makes all @Beans auto execute
// @EnableWebSecurity(debug = true) // Enables web security
@EnableMethodSecurity // Enables @PreAuthorize, @Secured etc.
public class SecurityConfig {

    @Autowired
    JwtAuthenticationFilter jwtFilter;

    @Autowired
    AccountRepository accountRepository;


    // Service used for getting information about Accounts, ASIpml implements UserDetailsService
    @Bean
    public UserDetailsService userDetailsService() {
        //it implements USD, so this should be fine? it existing like this???
        //if the method were to return, not the interface, but the ASI, the @Autowire at AuthCOntroller wouldnt know
        //which to take
        return new CustomUserDetailsService(accountRepository);
    }



    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        // 1. Which service is to be used to pull information about the user that wants to be authenticated
        // During authentication, AuthenticationManager will call loadUserByUsername() method of this service
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService());

        //authProvider.setUserDetailsService(userDetailsService());
        // 2. What encoder is used for the obtained password
        // The obtained hash is compared with the one stored in the database
        authProvider.setPasswordEncoder(passwordEncoder());

        return authProvider;
    }

    @Bean
    //Skeleton, should use @Preauthorize anyway, so most of this will be removed
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> {}) // Access from other sites (localhost:4200)
                .csrf(csrf -> csrf.disable()) // JWT does this, so shutdown for now
                //.exceptionHandling(exceptionHandling -> exceptionHandling.authenticationEntryPoint(restAuthenticationEntryPoint))

                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll() //for testing purposes
                ).sessionManagement(session -> { // do not use cookies
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
                })
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .authenticationProvider(authenticationProvider());

                // JWT before everything else, though not used as for now
                //.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        // Authentication(filters ,security context etc) is completely ignored for the following:
        return (web) -> web.ignoring()
                .requestMatchers(HttpMethod.POST, "/auth/login","/api/auth/login", "/api/auth/register")


                // Allowing access to static resources
                .requestMatchers(HttpMethod.GET, "/", "/webjars/*", "/*.html", "/favicon.ico",
                        "/*/*.html", "/*/*.css", "/*/*.js");

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

    // Global CORS configuration so the Angular app can reach our API
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:4200"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    //CORS config file needed, otherwise reqeusts from port 4200 wont owrk

    //Without CorsConfig, all requests from the webBrowser would fail, preflight as well.
}
