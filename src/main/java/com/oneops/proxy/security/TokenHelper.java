/*******************************************************************************
 *
 *   Copyright 2017 Walmart, Inc.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 *******************************************************************************/
package com.oneops.proxy.security;

import com.oneops.proxy.config.OneOpsConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;

import java.time.Instant;
import java.util.Date;

/**
 * JWT (JWS) token helper class.
 *
 * @author Suresh
 */
public class TokenHelper {

    /**
     * Base64 encoded HMAC SHA-512 key.
     */
    private final char[] secretKey;

    private final int expiresInSec;

    private static SignatureAlgorithm SIGNATURE_ALGORITHM = SignatureAlgorithm.HS512;

    public TokenHelper(OneOpsConfig.Auth authConfig) {
        this.secretKey = authConfig.getSigningKey();
        this.expiresInSec = authConfig.getExpiresInSec();
    }

    /**
     * Generate a JWT token for the given user.
     *
     * @param userName username (subject)
     * @param role     user role (Used for ACL)
     * @return compact JWS (JSON Web Signature)
     */
    public String generateToken(String userName, String role) {
        Instant now = Instant.now();
        Instant expiresIn = now.plusSeconds(expiresInSec);
        return Jwts.builder()
                .setSubject(userName)
                .setIssuer("OneOps-Proxy")
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiresIn))
                .claim("role", role)
                .signWith(SIGNATURE_ALGORITHM, String.valueOf(secretKey))
                .compact();
    }

    /**
     * Validates and returns the claims of given JWS
     *
     * @param token compact JWS (JSON Web Signature)
     * @return {@link Claims} . Returns <code>null</code> if it fails to verify/expires the JWT.
     */
    public Claims getClaims(String token) {
        Claims claims;
        try {
            claims = Jwts.parser()
                    .setSigningKey(String.valueOf(secretKey))
                    .parseClaimsJws(token)
                    .getBody();
        } catch (SignatureException e) {
            claims = null;
        }
        return claims;
    }
}
