This library provides Spring Security filters and supporting classes that streamline the use of
authentication and registration within Single Page web Applications (SPA).

## Installation

Add the jcenter repository to your build, such as

```xml
<repository>
  <snapshots>
    <enabled>false</enabled>
  </snapshots>
  <id>jcenter</id>
  <name>bintray</name>
  <url>https://jcenter.bintray.com</url>
</repository>
```

and the dependency to this library

xml


## Usage

This library provides a pair Spring Security filters that both accept a JSON payload via a POST:
* `me.itzg.spring.security.spa.RegistrationFilter`
* `me.itzg.spring.security.spa.RequestBodyLoginFilter`

The JSON payload must contain two fields:
* `username`
* `password`

The library also provides `SimpleLogoutSuccessHandler` in order to conclude the logout process with just a 200 OK
status code.

The registration manager needs a [`UserDetailsManager`][1] in order to add the newly registered user. 
It also needs a [`PasswordEncoder`][2] to encode the registration's new password. The following example
shows how to configure the filters in a way that consistently manages those beans between the filters
and the Spring security layer.

## Example

```java
import me.itzg.spring.security.spa.RegistrationFilter;
import me.itzg.spring.security.spa.RequestBodyLoginFilter;
import me.itzg.spring.security.spa.SimpleLogoutSuccessHandler;

@Configuration
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    private HttpMessageConverters httpMessageConverters;

    @Autowired
    public WebSecurityConfig(HttpMessageConverters httpMessageConverters) {
        this.httpMessageConverters = httpMessageConverters;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .anyRequest().fullyAuthenticated()

                .and().logout().logoutSuccessHandler(new SimpleLogoutSuccessHandler())
                .and().csrf().disable() // CSRF is less helpful (and a little annoying) with single page apps

                .addFilterBefore(new RegistrationFilter(userDetailsManager(), httpMessageConverters, passwordEncoder()),
                        UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(new RequestBodyLoginFilter(authenticationManager(), httpMessageConverters),
                        UsernamePasswordAuthenticationFilter.class);
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.apply(new SecurityConfigurer<InMemoryUserDetailsManagerConfigurer<AuthenticationManagerBuilder>>())
                .passwordEncoder(passwordEncoder());
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public UserDetailsManager userDetailsManager() {
        return new InMemoryUserDetailsManager();
    }

    private class SecurityConfigurer<C extends UserDetailsManagerConfigurer<AuthenticationManagerBuilder, C>>
            extends UserDetailsManagerConfigurer<AuthenticationManagerBuilder, C> {

        SecurityConfigurer() {
            super(userDetailsManager());
        }
    }
}
```

[1]: https://docs.spring.io/spring-security/site/docs/current/api/org/springframework/security/provisioning/UserDetailsManager.html
[2]: https://docs.spring.io/spring-security/site/docs/current/api/org/springframework/security/crypto/password/PasswordEncoder.html