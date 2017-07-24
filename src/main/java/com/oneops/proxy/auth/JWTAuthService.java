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
package com.oneops.proxy.auth;

import com.oneops.proxy.config.OneOpsConfig;
import io.jsonwebtoken.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * JWT (JWS) authentication service and other helper methods.
 *
 * @author Suresh
 */
@Service
public class JWTAuthService {

    private static final Logger log = LoggerFactory.getLogger(JWTAuthService.class);

    private static final String ROLE_CLAIM = "roles";

    private static SignatureAlgorithm SIGNATURE_ALGORITHM = SignatureAlgorithm.HS512;

    private final char[] secretKey;

    private final int expiresInSec;

    private final String issuer;

    private final String tokenHeader;

    private final String tokenType;

    public JWTAuthService(OneOpsConfig config) {
        final OneOpsConfig.Auth authConfig = config.getAuth();
        secretKey = authConfig.getSigningKey();
        expiresInSec = authConfig.getExpiresInSec();
        issuer = authConfig.getIssuer();
        tokenHeader = authConfig.getHeader();
        tokenType = authConfig.getTokenType();
    }

    /**
     * Generate a JWT token for the given user.
     *
     * @param userName username (subject)
     * @return compact JWS (JSON Web Signature)
     */
    public @Nonnull
    String generateToken(@Nonnull String userName) {
        return generateToken(userName, null);
    }

    /**
     * Generate a JWT token for the given user. The roles will be
     * stored as a claim in JWT token as a comma separated string.
     *
     * @param userName username (subject)
     * @param roles    user roles (Used for ACL)
     * @return compact JWS (JSON Web Signature)
     */
    public @Nonnull
    String generateToken(@Nonnull String userName,
                         @Nullable List<String> roles) {
        Instant now = Instant.now();
        Instant expiresIn = now.plusSeconds(expiresInSec);

        JwtBuilder jwt = Jwts.builder()
                .setSubject(userName)
                .setIssuer(issuer)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiresIn))
                .signWith(SIGNATURE_ALGORITHM, String.valueOf(secretKey));
        if (roles != null) {
            jwt.claim(ROLE_CLAIM, String.join(",", roles));
        }
        return jwt.compact();
    }

    /**
     * Validates and returns the claims of given JWS
     *
     * @param token compact JWS (JSON Web Signature)
     * @return {@link Claims} . Returns <code>null</code> if it fails to verify/expires the JWT.
     */
    public @Nullable
    Claims getClaims(@Nonnull String token) {
        Claims claims;
        try {
            claims = Jwts.parser()
                    .setSigningKey(String.valueOf(secretKey))
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException e) {
            log.debug("JWT token parser error.", e);
            claims = null;
        }
        return claims;
    }


    /**
     * A helper method to returns authority from the role
     * claim. The role is a string of comma separated values.
     *
     * @param claims JWT claims.
     * @return list of {@link SimpleGrantedAuthority}
     */
    private @Nullable
    List<GrantedAuthority> getAuthorities(@Nonnull Claims claims) {
        String rolesStr = claims.getOrDefault(ROLE_CLAIM, "").toString();
        return Arrays.stream(rolesStr.split(","))
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    /**
     * Returns the list of roles from {@link Authentication}
     *
     * @param auth authentication object
     * @return list of strings.
     */
    private @Nullable
    List<String> getRoles(@Nonnull Authentication auth) {
        Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();
        if (authorities != null) {
            return authorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList());
        }
        return null;
    }

    /**
     * Get the authentication object from http request.
     *
     * @param req http request.
     * @return {@link Authentication}
     */
    public Authentication getAuthenticationFromReq(HttpServletRequest req) {
        log.debug("Getting the authentication object for " + req.getRequestURI());
        String bearerToken = req.getHeader(tokenHeader);
        if (bearerToken != null) {
            String jwtToken = bearerToken.replaceFirst(tokenType, "").trim();
            Claims claims = getClaims(jwtToken);
            if (claims != null) {
                return new UsernamePasswordAuthenticationToken(claims.getSubject(), null, getAuthorities(claims));
            }
        }
        log.debug("Bearer token/Claim is null for " + req.getRequestURI());
        return null;
    }

    /**
     * Add authenticated JWT token to response header.
     *
     * @param res  http response
     * @param auth authentication object.
     */
    public void setAuthenticationToRes(HttpServletResponse res, Authentication auth) {
        String jwtToken = generateToken(auth.getName(), getRoles(auth));
        res.addHeader(tokenHeader, tokenType + " " + jwtToken);
    }
}

