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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.util.StringUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Intercepts login attempts conveyed by a <code>username</code> and <code>password</code> in the request body.
 *
 * @author Geoff Bourne
 * @since Mar 2018
 */
public class RequestBodyLoginFilter extends AbstractAuthenticationProcessingFilter {

    private final ConverterHelper converterHelper;
    private AuthenticationManager authenticationManager;

    public RequestBodyLoginFilter(AuthenticationManager authenticationManager, HttpMessageConverters httpMessageConverters) {
        super(new AntPathRequestMatcher("/login", "POST"));
        this.authenticationManager = authenticationManager;
        converterHelper = new ConverterHelper(httpMessageConverters);

        setAuthenticationSuccessHandler(new SimpleAuthenticationSuccessHandler(HttpServletResponse.SC_OK));
        setAuthenticationFailureHandler(new SimpleAuthenticationFailureHandler());
    }

    @SuppressWarnings("RedundantThrows")
    @Override
    public Authentication attemptAuthentication(
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse) throws AuthenticationException, IOException, ServletException {
        final Credentials credentials = converterHelper.parseBody(httpServletRequest, Credentials.class);

        if (credentials != null) {
            if (!StringUtils.hasLength(credentials.getUsername())) {
                throw new BadCredentialsException("Missing username");
            }
            if (!StringUtils.hasLength(credentials.getPassword())) {
                throw new BadCredentialsException("Missing password");
            }

            final UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(credentials.getUsername(), credentials.getPassword());

            return authenticationManager.authenticate(token);
        } else {
            throw new BadCredentialsException("Request body was invalid");
        }
    }
}
