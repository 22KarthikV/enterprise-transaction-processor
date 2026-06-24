package com.transactionprocessor.transaction.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;

/** Generates JWTs matching the secret/issuer in the default application.yml, for tests. */
public final class JwtTestHelper {

  public static final String TEST_SECRET =
      "local-dev-jwt-secret-change-in-prod-please-use-a-long-random-value";
  public static final String TEST_ISSUER = "enterprise-transaction-processor";

  private JwtTestHelper() {}

  public static String validToken(String subject) {
    return token(subject, Date.from(Instant.now().plusSeconds(3600)));
  }

  public static String expiredToken(String subject) {
    return token(subject, Date.from(Instant.now().minusSeconds(10)));
  }

  private static String token(String subject, Date expiry) {
    Key key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
    return Jwts.builder()
        .subject(subject)
        .issuer(TEST_ISSUER)
        .issuedAt(new Date())
        .expiration(expiry)
        .signWith(key)
        .compact();
  }
}
