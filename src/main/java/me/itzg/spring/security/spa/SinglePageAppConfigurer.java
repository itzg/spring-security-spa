package me.itzg.spring.security.spa;

import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.HttpSecurityBuilder;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * This configurer will install the registration filter and request-body login filter provided by this library.
 *
 * <h3>Example</h3>
 *
 * <pre>
 protected void configure(HttpSecurity http) throws Exception {
   http
   .apply(new SinglePageAppConfigurer<>()).registerUrl("/register/local").loginUrl("/login/local");
 }
 * </pre>
 *
 * Be sure to coordinate the use of the same beans for the authentication manager by declaring and configuring
 * those in the same configuration bean:
 *
 * <pre>
 &#64;Override
 protected void configure(AuthenticationManagerBuilder auth) throws Exception {
   auth
   .userDetailsService(userDetailsManager())
   .passwordEncoder(passwordEncoder())
   .and().inMemoryAuthentication();
 }

 &#64;Bean
 public PasswordEncoder passwordEncoder() {
   return PasswordEncoderFactories.createDelegatingPasswordEncoder();
 }

 &#64;Bean
 public UserDetailsManager userDetailsManager() {
   return new InMemoryUserDetailsManager();
 }
 * </pre>
 * @author Geoff Bourne
 * @since Jun 2018
 */
public class SinglePageAppConfigurer<B extends HttpSecurityBuilder<B>>
        extends AbstractHttpConfigurer<SinglePageAppConfigurer<B>, B> {

    private String registerUrl = RegistrationFilter.DEFAULT_PROCESSES_URL;
    private String loginUrl = RequestBodyLoginFilter.DEFAULT_PROCESSES_URL;

    @Override
    public void configure(B builder) throws Exception {

        final AuthenticationManager authenticationManager = builder.getSharedObject(AuthenticationManager.class);
        final ApplicationContext applicationContext = builder.getSharedObject(ApplicationContext.class);

        final RegistrationFilter registrationFilter = new RegistrationFilter(registerUrl);
        registrationFilter.setAuthenticationManager(authenticationManager);
        registrationFilter.setPasswordEncoder(applicationContext.getBean(PasswordEncoder.class));
        registrationFilter.setHttpMessageConverters(applicationContext.getBean(HttpMessageConverters.class));
        registrationFilter.setUserDetailsManager(applicationContext.getBean(UserDetailsManager.class));
        builder.addFilterBefore(postProcess(registrationFilter),
                UsernamePasswordAuthenticationFilter.class);

        final RequestBodyLoginFilter loginFilter = new RequestBodyLoginFilter(loginUrl);
        loginFilter.setAuthenticationManager(authenticationManager);
        loginFilter.setHttpMessageConverters(applicationContext.getBean(HttpMessageConverters.class));
        builder.addFilterBefore(postProcess(loginFilter),
                UsernamePasswordAuthenticationFilter.class);
    }

    public SinglePageAppConfigurer<B> registerUrl(String url) {
        this.registerUrl = url;
        return this;
    }

    public SinglePageAppConfigurer<B> loginUrl(String url) {
        this.loginUrl = url;
        return this;
    }
}
