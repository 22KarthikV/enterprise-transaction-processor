package com.transactionprocessor.transaction.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenValidator {

  private final SecretKey signingKey;
  private final String expectedIssuer;

  public JwtTokenValidator(
      @Value("${app.jwt.secret}") String secret, @Value("${app.jwt.issuer}") String issuer) {
    this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    this.expectedIssuer = issuer;
  }

  /** Validates signature and expiry (both enforced by the JJWT parser) and the issuer claim. */
  public Claims validate(String token) {
    Claims claims = Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token).getPayload();
    if (!expectedIssuer.equals(claims.getIssuer())) {
      throw new JwtException("Unexpected issuer: " + claims.getIssuer());
    }
    return claims;
  }
}
