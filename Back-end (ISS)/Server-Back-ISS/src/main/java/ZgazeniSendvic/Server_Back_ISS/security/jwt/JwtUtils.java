package ZgazeniSendvic.Server_Back_ISS.security.jwt;

import ZgazeniSendvic.Server_Back_ISS.exception.InvalidRideTokenException;
import ZgazeniSendvic.Server_Back_ISS.exception.RideTokenExpiredException;
import ZgazeniSendvic.Server_Back_ISS.model.Account;
import ZgazeniSendvic.Server_Back_ISS.model.Ride;
import io.jsonwebtoken.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;

@Component
public class JwtUtils {

    // Token issuer
    @Value("DriveBy")
    private String APP_NAME;

    // Secret
    @Value("SendicSendicSendicSendicSendicSendicSendicSendicSendicSendicSendicSendicSendicSendicSendicSendicSendic")
    public String SECRET;

    // Validity time - 30 min
    @Value("1800000")
    private int EXPIRES_IN;

    // Header through which JWT is passed in client-server communication
    @Value("Authorization")
    private String AUTH_HEADER;

    private static final String AUDIENCE_WEB = "web";

    // Algorithm for JWT signing
    private SignatureAlgorithm SIGNATURE_ALGORITHM = SignatureAlgorithm.HS512;


    // ============= JWT generation =============

    //Gen of the token, contains the email for ID-ing
    public String generateToken(Account account) {
        return Jwts.builder()
                .setIssuer(APP_NAME)
                .setSubject(account.getEmail())
                .setAudience(generateAudience())
                .setIssuedAt(new Date())
                .claim("role", account.getRole())
                .setExpiration(generateExpirationDate())
                .signWith(SIGNATURE_ALGORITHM, SECRET).compact();


        // Roles are the only important info right?
    }

    //would determine weather pc/web etc., for now only web
    private String generateAudience() {


        return AUDIENCE_WEB;
    }

    //simply generates up to which point the token is valid
    private Date generateExpirationDate() {
        return new Date(new Date().getTime() + EXPIRES_IN);
    }

    // ============= Reading from JWT =============

    //gets the token from the header
    public String getToken(HttpServletRequest request) {
        String authHeader = getAuthHeaderFromHeader(request);
        // JWT se is passed through header 'Authorization' in format:
        // Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7); // only the token (value after "Bearer " prefix)
        }

        return null;
    }

    //func for obtaining the email from the Token
    //returns null if an exception, moves expiry exception up.
    public String getUsernameFromToken(String token) {
        String email;

        try {
            final Claims claims = this.getAllClaimsFromToken(token);
            email = claims.getSubject();
        } catch (ExpiredJwtException ex) {
            throw ex;
        } catch (Exception e) {
            email = null;
        }

        return email;
    }

    //returns date of creation
    public Date getIssuedAtDateFromToken(String token) {
        Date issueAt;
        try {
            final Claims claims = this.getAllClaimsFromToken(token);
            issueAt = claims.getIssuedAt();
        } catch (ExpiredJwtException ex) {
            throw ex;
        } catch (Exception e) {
            issueAt = null;
        }
        return issueAt;
    }

    //returns audience info
    public String getAudienceFromToken(String token) {
        String audience;
        try {
            final Claims claims = this.getAllClaimsFromToken(token);
            audience = claims.getAudience();
        } catch (ExpiredJwtException ex) {
            throw ex;
        } catch (Exception e) {
            audience = null;
        }
        return audience;
    }

    //returns expiry date
    public Date getExpirationDateFromToken(String token) {
        Date expiration;
        try {
            final Claims claims = this.getAllClaimsFromToken(token);
            expiration = claims.getExpiration();
        } catch (ExpiredJwtException ex) {
            throw ex;
        } catch (Exception e) {
            expiration = null;
        }

        return expiration;
    }

    // returns all claims
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET)
                .parseClaimsJws(token)
                .getBody();
    }


    // =================================================================

    // ============= Functions for validation of JWT =============

    /**
     * Validation of the JWT.
     *
     * @param token JWT token.
     * @param userDetails Info about the Owner of the token.
     * @return Information on validity of the Token.
     * It obtains email directly from the token, whilst also, the function that calls validateToken
     * based on the email obtained directly from the token, obtains a userDetails based on the email
     * so if the email was truly present in the database, they wall match, so:
     * if the token is false, the email gotten from it wont return anything from the database, so when compared to
     * that which is from the database they wont match
     */
    public Boolean validateToken(String token, UserDetails userDetails) {

        final String email = getUsernameFromToken(token);

        return (email != null
                && email.equals(userDetails.getUsername())
                && !isTokenExpired(token));
    }

    /**
     * Funkcija proverava da li je lozinka korisnika izmenjena nakon izdavanja tokena.
     *
     * @param created Date of token creation.
     * @param lastPasswordReset Date of last password reset.
     * @return Was token created before or after the last password reset.
     */
    private Boolean isCreatedBeforeLastPasswordReset(Date created, Date lastPasswordReset) {
        return (lastPasswordReset != null && created.before(lastPasswordReset));
    }

    public Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }


    // =================================================================

    // gets the time of expiry
    public int getExpiredIn() {
        return EXPIRES_IN;
    }

    /**
     * Function for obtaining Auth header
     *
     * @param request HTTP request.
     *
     * @return content of AUTH_HEADER.
     */
    public String getAuthHeaderFromHeader(HttpServletRequest request) {
        return request.getHeader(AUTH_HEADER);
    }


    // ===============================  Ride token  ==================================

    public Date generateRideExpirationDate(Ride ride) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = ride.getStartTime();

        long milliseconds = Duration.between(now, startTime).toMillis();

        // Safety: if start time is in the past, expire immediately
        if (milliseconds < 0) {
            milliseconds = 0;
        }

        return new Date(System.currentTimeMillis() + milliseconds);
    }
    public String generateRideToken(Ride ride){
        Long rideID = ride.getId();
        return Jwts.builder()
                .setSubject(rideID.toString())
                .setExpiration(generateRideExpirationDate(ride))
                .signWith(SIGNATURE_ALGORITHM, SECRET).compact();
    }

    public void validateRideToken(String token) {
        try {
            Jwts.parser()
                    .setSigningKey(SECRET)
                    .parseClaimsJws(token);
        } catch (ExpiredJwtException e) {
            throw new RideTokenExpiredException("Ride token has expired", e);
        } catch (JwtException e) {
            throw new InvalidRideTokenException("Invalid ride token", e);
        } catch (Exception e) {
            throw new InvalidRideTokenException("Invalid ride token", e);
        }
    }

    //extracts ride ID from the token, returns null if invalid
    public Long getRideIdFromToken(String token) {
        try {
            final Claims claims = this.getAllClaimsFromToken(token);
            String rideIdStr = claims.getSubject();
            return Long.parseLong(rideIdStr);
        } catch (Exception e) {
            throw new InvalidRideTokenException("Invalid ride token", e);
        }
    }

}

