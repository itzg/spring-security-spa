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

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.InsufficientAuthenticationException;

import javax.servlet.ServletException;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Geoff Bourne
 * @since Mar 2018
 */
public class SimpleAuthenticationFailureHandlerTest {

    @Test
    public void onAuthenticationFailure() throws IOException, ServletException {
        final SimpleAuthenticationFailureHandler handler = new SimpleAuthenticationFailureHandler();

        final MockHttpServletResponse resp = new MockHttpServletResponse();

        handler.onAuthenticationFailure(new MockHttpServletRequest("GET", "/"),
                resp,
                new InsufficientAuthenticationException("testing"));

        assertThat(resp.getStatus()).isEqualTo(SimpleAuthenticationFailureHandler.DEFAULT_STATUS_CODE);
    }

    @Test
    public void testSetStatus() throws IOException, ServletException {
        final SimpleAuthenticationFailureHandler handler = new SimpleAuthenticationFailureHandler();
        handler.setStatus(400);

        final MockHttpServletResponse resp = new MockHttpServletResponse();

        handler.onAuthenticationFailure(new MockHttpServletRequest("GET", "/"),
                resp,
                new InsufficientAuthenticationException("testing"));

        assertThat(resp.getStatus()).isEqualTo(400);
    }
}