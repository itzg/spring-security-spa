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
import org.springframework.security.authentication.TestingAuthenticationToken;

import javax.servlet.ServletException;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * @author Geoff Bourne
 * @since Mar 2018
 */
public class SimpleAuthenticationSuccessHandlerTest {
    @Test
    public void testHandler() throws IOException, ServletException {
        final SimpleAuthenticationSuccessHandler handler = new SimpleAuthenticationSuccessHandler(200);

        MockHttpServletResponse resp = new MockHttpServletResponse();
        handler.onAuthenticationSuccess(new MockHttpServletRequest("GET", "/"),
                resp,
                new TestingAuthenticationToken("user", null));

        assertThat(resp.getStatus()).isEqualTo(200);
    }
}