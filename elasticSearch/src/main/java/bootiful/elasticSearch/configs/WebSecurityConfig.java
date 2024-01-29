package bootiful.elasticSearch.configs;

import bootiful.elasticSearch.user.AuthUser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * @author pari on 17/01/24
 */
@Configuration
@EnableWebSecurity
class WebSecurityConfig {

    @Bean
    SecurityFilterChain configure(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
//                .csrf(AbstractHttpConfigurer::disable)
//                .sessionManagement(session-> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(requests-> {
                    requests.anyRequest().authenticated();
                })
                .formLogin(formLoginCustomizer-> formLoginCustomizer
                        .permitAll()
                        .failureUrl("/login?error=true")
//                        .loginPage("/login")
                        .defaultSuccessUrl("/"))
                .logout(logoutConfigurer-> {
                    logoutConfigurer.clearAuthentication(true)
                            .logoutSuccessUrl("/login?logout=true")
                            .logoutUrl("/logout");
                })
                .build();
    }

    @Bean
    InMemoryUserDetailsManager userDetailsService() {
        UserDetails user1 = AuthUser.withUsername("user1")
                .password(passwordEncoder().encode("user1Pass"))
                .roles("USER")
                .build();
        UserDetails user2 = AuthUser.withUsername("user2")
                .password(passwordEncoder().encode("user2Pass"))
                .roles("USER")
                .build();
        UserDetails admin = AuthUser.withUsername("admin")
                .password(passwordEncoder().encode("adminPass"))
                .roles("ADMIN")
                .build();
        return new InMemoryUserDetailsManager(user1, user2, admin);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
