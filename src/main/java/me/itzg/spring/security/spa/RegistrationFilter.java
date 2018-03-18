/*
 * Copyright 2018 Geoff Bourne
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package me.itzg.spring.security.spa;

import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.util.StringUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Handles registration of new users given by a request payload containing a <code>username</code> and <code>password</code>.
 * The actual mechanics of storing the new user is abstracted via the given {@link UserDetailsManager}.
 * Upon success, the response contains a {@value #STATUS_CODE} and the user will be authenticated into the current
 * session.
 *
 * @author Geoff Bourne
 * @since Mar 2018
 */
public class RegistrationFilter extends AbstractAuthenticationProcessingFilter {

    @SuppressWarnings("WeakerAccess")
    public static final int STATUS_CODE = HttpServletResponse.SC_CREATED;

    private final UserDetailsManager userDetailsManager;
    private final PasswordEncoder passwordEncoder;
    private final ConverterHelper converterHelper;
    private String[] initialRoles = new String[]{"USER"};

    /**
     * Creates a filter instance that processes registrations at the path /register.
     *
     * @param userDetailsManager    the new user will be added via this details manager
     * @param httpMessageConverters used for processing the credentials from the request body
     * @param passwordEncoder       used for encoding the new user's password
     */
    public RegistrationFilter(UserDetailsManager userDetailsManager, HttpMessageConverters httpMessageConverters,
                              PasswordEncoder passwordEncoder) {
        this("/register", userDetailsManager, httpMessageConverters, passwordEncoder);
    }

    /**
     * Creates a filter instance that processes registrations at the given path.
     *
     * @param path                  the path where this filter will process registrations
     * @param userDetailsManager    the new user will be added via this details manager
     * @param httpMessageConverters used for processing the credentials from the request body
     * @param passwordEncoder       used for encoding the new user's password
     */
    @SuppressWarnings("WeakerAccess")
    public RegistrationFilter(String path, UserDetailsManager userDetailsManager, HttpMessageConverters httpMessageConverters,
                              PasswordEncoder passwordEncoder) {
        super(new AntPathRequestMatcher(path, "POST"));
        this.userDetailsManager = userDetailsManager;
        this.passwordEncoder = passwordEncoder;
        this.converterHelper = new ConverterHelper(httpMessageConverters);

        setAuthenticationSuccessHandler(new SimpleAuthenticationSuccessHandler(STATUS_CODE));

        setAuthenticationFailureHandler(new SimpleAuthenticationFailureHandler());
    }

    /**
     * Sets the initial role(s) of registered users.
     * The default is <code>USER</code>
     *
     * @param initialRoles the roles (without authority prefix)
     * @return this object for call chaining
     */
    @SuppressWarnings("unused")
    public RegistrationFilter setInitialRoles(String... initialRoles) {
        this.initialRoles = initialRoles;
        return this;
    }

    @SuppressWarnings("RedundantThrows")
    @Override
    public Authentication attemptAuthentication(HttpServletRequest req,
                                                HttpServletResponse resp) throws AuthenticationException, IOException, ServletException {

        final Credentials registration = converterHelper.parseBody(req, Credentials.class);

        if (registration != null) {
            if (!StringUtils.hasLength(registration.getUsername())) {
                throw new BadCredentialsException("Missing username");
            }
            if (!StringUtils.hasLength(registration.getPassword())) {
                throw new BadCredentialsException("Missing password");
            }

            final UserDetails user = User.builder()
                    .username(registration.getUsername())
                    .password(registration.getPassword())
                    .passwordEncoder(passwordEncoder::encode)
                    .roles(initialRoles)
                    .build();

            try {
                if (userDetailsManager.userExists(user.getUsername())) {
                    throw new RegistrationFailedException("Username is already in use");
                }
                userDetailsManager.createUser(user);
            } catch (Exception e) {
                if (e instanceof RegistrationFailedException) {
                    throw e;
                }
                throw new RegistrationFailedException("Unexpected failure", e);
            }

            return new UsernamePasswordAuthenticationToken(user.getUsername(), null, user.getAuthorities());
        }
        else {
            throw new RegistrationFailedException("Invalid request content");
        }
    }
}
