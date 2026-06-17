package com.example.lms.security.jwt;

import com.example.lms.service.impl.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class AuthTokenFilter extends OncePerRequestFilter {
    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        logger.debug("AuthTokenFilter: Processing request to {}", request.getRequestURI());
        try {
            String jwt = parseJwt(request);
            logger.debug("AuthTokenFilter: Parsed JWT: {}", jwt);

            if (jwt != null) {
                boolean isValidJwt = jwtUtils.validateJwtToken(jwt);
                logger.debug("AuthTokenFilter: JWT validation result: {}", isValidJwt);

                if (isValidJwt) {
                    String username = jwtUtils.getUserNameFromJwtToken(jwt);
                    logger.debug("AuthTokenFilter: Username from JWT: {}", username);

                    if (username != null) {
                        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                        if (userDetails != null) {
                            logger.debug("AuthTokenFilter: UserDetails loaded for username: {}. Authorities: {}", username, userDetails.getAuthorities());

                            UsernamePasswordAuthenticationToken authentication =
                                    new UsernamePasswordAuthenticationToken(userDetails,
                                                                          null,
                                                                          userDetails.getAuthorities());
                            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                            SecurityContextHolder.getContext().setAuthentication(authentication);
                            logger.debug("AuthTokenFilter: User {} authenticated and set in SecurityContextHolder.", username);
                        } else {
                            logger.warn("AuthTokenFilter: UserDetails not found for username: {}", username);
                        }
                    } else {
                        logger.warn("AuthTokenFilter: Username could not be extracted from JWT.");
                    }
                } else {
                    logger.warn("AuthTokenFilter: Invalid JWT token.");
                }
            } else {
                logger.debug("AuthTokenFilter: No JWT token found in request headers.");
            }
        } catch (Exception e) {
            logger.error("AuthTokenFilter: Cannot set user authentication: {}", e.getMessage(), e);
        }

        filterChain.doFilter(request, response);
        logger.debug("AuthTokenFilter: Finished processing request to {}", request.getRequestURI());
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        if (logger.isDebugEnabled()) {
            logger.debug("AuthTokenFilter: Authorization Header: {}", headerAuth);
        }

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }
        return null;
    }
} 