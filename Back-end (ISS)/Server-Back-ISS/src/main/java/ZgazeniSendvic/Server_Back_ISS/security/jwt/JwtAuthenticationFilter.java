package ZgazeniSendvic.Server_Back_ISS.security.jwt;

import ZgazeniSendvic.Server_Back_ISS.security.CustomUserDetailsService;
import ZgazeniSendvic.Server_Back_ISS.security.auth.RestAuthenticationEntryPoint;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    JwtUtils jwtUtil;
    @Autowired
    CustomUserDetailsService userDetailsService;
    @Autowired
    RestAuthenticationEntryPoint restAuthenticationEntryPoint;

    public JwtAuthenticationFilter(JwtUtils jwtUtil,
                                   CustomUserDetailsService userDetailsService,
                                   RestAuthenticationEntryPoint restAuthenticationEntryPoint) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.restAuthenticationEntryPoint = restAuthenticationEntryPoint;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        String email;
        try {
            email = jwtUtil.getUsernameFromToken(token);
        } catch (ExpiredJwtException ex) {
            restAuthenticationEntryPoint.commence(
                    request,
                    response,
                    new BadCredentialsException("JWT expired", ex)
            );
            return;
        } catch (Exception ex) {
            restAuthenticationEntryPoint.commence(
                    request,
                    response,
                    new BadCredentialsException("JWT invalid", ex)
            );
            return;
        }

        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

        filterChain.doFilter(request, response);
    }
}
