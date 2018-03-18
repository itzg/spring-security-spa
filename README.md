This library provides Spring Security filters and supporting classes that streamline the use of
authentication and registration within Single Page web Applications (SPA).

## Usage

```java
import me.itzg.spring.security.spa.RegistrationFilter;
import me.itzg.spring.security.spa.RequestBodyLoginFilter;
import me.itzg.spring.security.spa.SimpleLogoutSuccessHandler;

@Configuration
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .anyRequest().fullyAuthenticated()

                .and().logout().logoutSuccessHandler(new SimpleLogoutSuccessHandler())

                .and()
                .addFilterBefore(new RegistrationFilter(userDetailsManager(), httpMessageConverters, passwordEncoder()),
                        UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(new RequestBodyLoginFilter(authenticationManager(), httpMessageConverters),
                        UsernamePasswordAuthenticationFilter.class);
    }
    
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```