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
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Geoff Bourne
 * @since Mar 2018
 */
@RunWith(SpringRunner.class)
@TestPropertySource(properties = "logging.level.org.springframework.security=info")
@WebMvcTest
public class RequestBodyLoginFilterTest {

    @Autowired
    MockMvc mvc;

    @Configuration
    public static class Config extends WebSecurityConfigurerAdapter {
        private HttpMessageConverters httpMessageConverters;

        @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
        @Autowired
        public Config(HttpMessageConverters httpMessageConverters) {
            this.httpMessageConverters = httpMessageConverters;
        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            final RequestBodyLoginFilter filter = new RequestBodyLoginFilter(authenticationManager(), httpMessageConverters);

            http
                    .authorizeRequests().anyRequest().fullyAuthenticated()
                    .and()
                    .csrf().disable()
                    .addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class);
        }

        @Override
        protected void configure(AuthenticationManagerBuilder auth) throws Exception {
            final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

            auth.inMemoryAuthentication()
                    .withUser("user").password(passwordEncoder.encode("password")).roles("USER")
                    .and().passwordEncoder(passwordEncoder);
        }
    }

    @Test
    public void successfulAuth() throws Exception {
        String body = "{\n" +
                "  \"username\": \"user\",\n" +
                "  \"password\": \"password\"\n" +
                "}";
        mvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk());
    }

    @Test
    public void badPassword() throws Exception {
        String body = "{\n" +
                "  \"username\": \"user\",\n" +
                "  \"password\": \"wrong\"\n" +
                "}";
        mvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void emptyJson() throws Exception {
        String body = "{}";
        mvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void wrongMediaType() throws Exception {
        String body = "user=user";
        mvc.perform(post("/login").contentType(MediaType.TEXT_PLAIN).content(body))
                .andExpect(status().isUnauthorized());
    }
}