package ru.itmo.secureapi;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class AuthController {
    private final JdbcTemplate jdbc;
    private final PasswordEncoder passwords;
    private final JwtEncoder tokens;

    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public AuthController(JdbcTemplate jdbc, PasswordEncoder passwords, JwtEncoder tokens) {
        this.jdbc = jdbc;
        this.passwords = passwords;
        this.tokens = tokens;
    }

    @PostMapping("/auth/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        List<String> hashes = jdbc.query("SELECT password_hash FROM app_user WHERE username = ?",
            (rs, row) -> rs.getString(1), request.username());
        if (hashes.isEmpty() || !passwords.matches(request.password(), hashes.getFirst())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
            .subject(request.username())
            .issuedAt(now)
            .expiresAt(now.plus(1, ChronoUnit.HOURS))
            .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return new LoginResponse(tokens.encode(JwtEncoderParameters.from(header, claims)).getTokenValue());
    }

    public record LoginRequest(@NotBlank String username, @NotBlank String password) {}

    public record LoginResponse(String token) {}
}
