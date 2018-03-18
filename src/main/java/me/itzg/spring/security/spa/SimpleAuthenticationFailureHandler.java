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

import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Handles authentication failures by simply setting the response status code to the given value or
 * {@value #DEFAULT_STATUS_CODE} by default.
 *
 * @author Geoff Bourne
 * @since Mar 2018
 */
public class SimpleAuthenticationFailureHandler implements AuthenticationFailureHandler {

    @SuppressWarnings("WeakerAccess")
    public static final int DEFAULT_STATUS_CODE = HttpServletResponse.SC_UNAUTHORIZED;
    private int status = DEFAULT_STATUS_CODE;

    /**
     * Sets the status code that this handler will use for the servlet response.
     * Default is {@value HttpServletResponse#SC_UNAUTHORIZED}.
     *
     * @param status the status code this handler should set in the response
     * @return this object for call chaining
     */
    @SuppressWarnings("unused")
    public SimpleAuthenticationFailureHandler setStatus(int status) {
        this.status = status;
        return this;
    }

    @SuppressWarnings("RedundantThrows")
    @Override
    public void onAuthenticationFailure(HttpServletRequest httpServletRequest, HttpServletResponse resp,
                                        AuthenticationException e) throws IOException, ServletException {
        resp.setStatus(status);
        resp.setContentType(MediaType.TEXT_PLAIN_VALUE);
        resp.getWriter().print(e.getMessage());
    }
}
