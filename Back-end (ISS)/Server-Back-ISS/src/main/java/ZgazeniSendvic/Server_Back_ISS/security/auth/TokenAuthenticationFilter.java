package ZgazeniSendvic.Server_Back_ISS.security.auth;

import ZgazeniSendvic.Server_Back_ISS.util.TokenUtils;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

//every single request from the client goes through here, except certain thigns in WebSecurityCustomizer
//checks wether everything in regard to the JWT is correct. If so, sets the security context, so that the info
//is available everywhere necessary
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private TokenUtils tokenUtils;

    private UserDetailsService userDetailsService;

    protected final Log LOGGER = LogFactory.getLog(getClass());

    public TokenAuthenticationFilter(TokenUtils tokenHelper, UserDetailsService userDetailsService) {
        this.tokenUtils = tokenHelper;
        this.userDetailsService = userDetailsService;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String email;

        // Obtaining the token
        String authToken = tokenUtils.getToken(request);

        try{
            if(authToken != null && !authToken.isEmpty()) {

                email = tokenUtils.getUsernameFromToken(authToken);

                if(email != null) {
                    // Loading of userDetails, implemented in AccountService, based on email
                    // LoadUser returns User in practice example, here not. thats why it cracks
                    // perhaps I should change that, as I had planned to anyway.
                    UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                    // checks wether token is valid
                    if(tokenUtils.validateToken(authToken, userDetails)) {
                        // Create the authentication, populare security context
                        // I find using a custom class to be unnecessary, at least for now
                        // Token has been validated, and Identity established, so credentials are null
                        // no need to keep the Token anymore, though it could go where credentials are.
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails,
                                        null,
                                        userDetails.getAuthorities()

                                ); //System.out.println(userDetails.getAuthorities());

                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                }


            }
        }
        catch (ExpiredJwtException ex) {
            LOGGER.debug("Token expired!");
        }
        // Send request further, to next filter
        chain.doFilter(request, response);

    }
}
