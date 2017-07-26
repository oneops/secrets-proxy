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

import com.oneops.proxy.auth.token.JwtAuthToken;
import com.oneops.proxy.auth.user.OneOpsUser;
import com.oneops.proxy.config.OneOpsConfig;
import io.jsonwebtoken.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static com.oneops.proxy.config.Constants.DEFAULT_DOMAIN;
import static org.springframework.util.StringUtils.isEmpty;

/**
 * Token (JWT) generation and validation services.
 *
 * @author Suresh
 */
@Service
public class JwtTokenService {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenService.class);

    private static final String ROLE_CLAIM = "roles";

    private static final String DOMAIN_CLAIM = "domain";

    private static SignatureAlgorithm SIGNATURE_ALGORITHM = SignatureAlgorithm.HS512;

    private final char[] secretKey;

    private final int expiresInSec;

    private final String issuer;

    private final String tokenHeader;

    private final String tokenType;

    public JwtTokenService(OneOpsConfig config) {
        final OneOpsConfig.Auth authConfig = config.getAuth();
        secretKey = authConfig.getSigningKey();
        expiresInSec = authConfig.getExpiresInSec();
        issuer = authConfig.getIssuer();
        tokenHeader = authConfig.getHeader();
        tokenType = authConfig.getTokenType();
    }


    /**
     * Generate a JWT token for the given user. The roles will be
     * stored as a claim in JWT token as a comma separated string.
     *
     * @param user authenticated user details object.
     * @return compact JWS (JSON Web Signature)
     */
    public @Nonnull
    String generateToken(OneOpsUser user) {
        Instant now = Instant.now();
        Instant expiresIn = now.plusSeconds(expiresInSec);

        JwtBuilder jwt = Jwts.builder()
                .setSubject(user.getUsername())
                .setIssuer(issuer)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiresIn))
                .signWith(SIGNATURE_ALGORITHM, String.valueOf(secretKey));
        if (user.getAuthorities() != null) {
            List<String> roles = user.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList());
            jwt.claim(ROLE_CLAIM, String.join(",", roles));
        }
        if (user.getDomain() != null) {
            jwt.claim(DOMAIN_CLAIM, user.getDomain());
        }
        return jwt.compact();
    }

    /**
     * Validates token and creates the user details object by extracting
     * identity and authorization claims.
     *
     * @param token jwt token
     * @return {@link OneOpsUser}
     */
    public OneOpsUser createUser(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(String.valueOf(secretKey))
                    .parseClaimsJws(token)
                    .getBody();

            String username = claims.getSubject();
            List<GrantedAuthority> authorities = getAuthorities(claims);
            String domain = claims.getOrDefault(DOMAIN_CLAIM, DEFAULT_DOMAIN).toString();
            return new OneOpsUser(username, "", authorities, username, domain);
        } catch (ExpiredJwtException ex) {
            throw new CredentialsExpiredException("Token has expired", ex);
        } catch (JwtException jex) {
            throw new BadCredentialsException("Invalid Authorization Token", jex);
        }
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
     * Retrieves the JWT authentication token from http request.
     *
     * @param req http request.
     * @return {@link JwtAuthToken} or <code>null</code> if the Bearer token is not present or empty.
     */
    public @Nullable
    JwtAuthToken getAccessToken(@Nonnull HttpServletRequest req) {
        log.debug("Getting the access token for " + req.getRequestURI());
        String bearerToken = req.getHeader(tokenHeader);
        if (bearerToken != null) {
            String jwtToken = bearerToken.replaceFirst(tokenType, "").trim();
            if (!isEmpty(jwtToken)) {
                return new JwtAuthToken("JwtToken", jwtToken, Collections.emptyList());
            }
        }
        log.debug("JWT Bearer token is null/empty for " + req.getRequestURI());
        return null;
    }

    public int getExpiresInSec() {
        return expiresInSec;
    }

    public String getIssuer() {
        return issuer;
    }

    public String getTokenHeader() {
        return tokenHeader;
    }

    public String getTokenType() {
        return tokenType;
    }
}

