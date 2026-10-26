package kmo.dev;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CsrfFilter;

/** One shared login for the whole lecture; username and bcrypt hash come from the environment. */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/style.css").permitAll()
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        // Always land on "/": otherwise Spring Security redirects to the last
                        // unauthenticated request, which a browser may make to something like
                        // /apple-touch-icon.png while loading the login page (404 after login).
                        .defaultSuccessUrl("/", true)
                        .permitAll())
                .logout(logout -> logout.logoutSuccessUrl("/login?logout").permitAll())
                // Inside the security chain (not a plain servlet filter), otherwise /login is
                // answered by Spring Security before the limiter ever sees it. Before CsrfFilter so rejected requests count too.
                .addFilterBefore(new RateLimitFilter(), CsrfFilter.class)
                .csrf(Customizer.withDefaults());
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService users(
            @Value("${chat.username}") String username,
            @Value("${chat.password-hash}") String passwordHash) {
        if (username.isBlank() || passwordHash.isBlank()) {
            throw new IllegalStateException(
                    "CHAT_USERNAME and CHAT_PASSWORD_HASH (bcrypt) must be set, see README.");
        }
        return new InMemoryUserDetailsManager(User.withUsername(username).password(passwordHash).roles("USER").build());
    }
}
