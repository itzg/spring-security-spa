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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configurers.provisioning.InMemoryUserDetailsManagerConfigurer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Geoff Bourne
 * @since Mar 2018
 */
@RunWith(SpringRunner.class)
@TestPropertySource(properties = "logging.level.org.springframework.security=info")
@WebMvcTest
public class RegistrationFilterTest {
    @Autowired
    MockMvc mvc;

    @Qualifier("userDetailsServiceBean")
    @Autowired
    UserDetailsService userDetailsService;

    @Configuration
    public static class Config extends WebSecurityConfigurerAdapter {
        private HttpMessageConverters httpMessageConverters;
        private UserDetailsManager userDetailsManager;

        @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
        @Autowired
        public Config(HttpMessageConverters httpMessageConverters) {
            this.httpMessageConverters = httpMessageConverters;
        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            final UserDetailsService userDetailsService = userDetailsService();

            final RegistrationFilter filter = new RegistrationFilter(
                    userDetailsManager,
                    httpMessageConverters,
                    passwordEncoder());

            http
                    .authorizeRequests().anyRequest().fullyAuthenticated()
                    .and()
                    .csrf().disable()
                    .addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class);
        }

        @Override
        protected void configure(AuthenticationManagerBuilder auth) throws Exception {

            final InMemoryUserDetailsManagerConfigurer<AuthenticationManagerBuilder> configurer = auth.inMemoryAuthentication();
            // This can be saved off and used in configure(HttpSecurity) since auth is configured first.
            userDetailsManager = configurer.getUserDetailsService();

            configurer
                    .withUser("user").password(passwordEncoder().encode("password")).roles("USER")
                    .and().passwordEncoder(passwordEncoder());

        }

        @Bean
        @Override
        public UserDetailsService userDetailsServiceBean() throws Exception {
            return super.userDetailsServiceBean();
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder();
        }
    }

    @Test
    public void successfulRegistration() throws Exception {
        String body = "{\n" +
                "  \"username\": \"new-user\",\n" +
                "  \"password\": \"new-password\"\n" +
                "}";
        mvc.perform(post("/register")
                .contentType(MediaType.APPLICATION_JSON).characterEncoding(StandardCharsets.UTF_8.name()).content(body))
                .andExpect(status().isCreated());

        final UserDetails userDetails = userDetailsService.loadUserByUsername("new-user");
        assertThat(userDetails).isNotNull();
    }

    @Test
    public void failDuplicateUser() throws Exception {
        String body = "{\n" +
                "  \"username\": \"user\",\n" +
                "  \"password\": \"new-password\"\n" +
                "}";
        mvc.perform(post("/register")
                .contentType(MediaType.APPLICATION_JSON).characterEncoding(StandardCharsets.UTF_8.name()).content(body))
                .andExpect(status().isUnauthorized())
        .andExpect(content().string("Username is already in use"));
    }

    @Test
    public void failEmptyJson() throws Exception {
        String body = "{}";
        mvc.perform(post("/register")
                .contentType(MediaType.APPLICATION_JSON).characterEncoding(StandardCharsets.UTF_8.name()).content(body))
                .andExpect(status().isUnauthorized())
        .andExpect(content().string("Missing username"));
    }

    @Test
    public void failEmptyPassword() throws Exception {
        String body = "{\n" +
                "  \"username\": \"new-user\",\n" +
                "  \"password\": \"\"\n" +
                "}";
        mvc.perform(post("/register")
                .contentType(MediaType.APPLICATION_JSON).characterEncoding(StandardCharsets.UTF_8.name()).content(body))
                .andExpect(status().isUnauthorized())
        .andExpect(content().string("Missing password"));
    }

    @Test
    public void wrongContent() throws Exception {
        String body = "user=user2";
        mvc.perform(post("/register")
                .contentType(MediaType.TEXT_PLAIN).characterEncoding(StandardCharsets.UTF_8.name()).content(body))
                .andExpect(status().isUnauthorized())
        .andExpect(content().string("Invalid request content"));
    }
}