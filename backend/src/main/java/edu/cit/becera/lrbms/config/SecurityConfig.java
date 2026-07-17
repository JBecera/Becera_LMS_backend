package edu.cit.becera.lrbms.config;

import edu.cit.becera.lrbms.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:5173", frontendUrl));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source; 
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Preflight requests never carry credentials - must be allowed regardless of the target's rules
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                // Public
                .requestMatchers(HttpMethod.GET, "/").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/members").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/books", "/api/books/search").permitAll()

                // Members (staff-managed)
                .requestMatchers(HttpMethod.GET, "/api/members", "/api/members/role/**").hasAnyRole("ADMIN", "LIBRARIAN")
                .requestMatchers(HttpMethod.DELETE, "/api/members/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/members/**").authenticated()

                // Catalog CRUD
                .requestMatchers(HttpMethod.POST, "/api/books/**").hasAnyRole("ADMIN", "LIBRARIAN")
                .requestMatchers(HttpMethod.PUT, "/api/books/**").hasAnyRole("ADMIN", "LIBRARIAN")
                .requestMatchers(HttpMethod.DELETE, "/api/books/**").hasAnyRole("ADMIN", "LIBRARIAN")

                // Transactions (check-in/check-out)
                .requestMatchers(HttpMethod.GET, "/api/transactions").hasAnyRole("ADMIN", "LIBRARIAN")
                .requestMatchers(HttpMethod.GET, "/api/transactions/member/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/transactions/checkout").hasAnyRole("ADMIN", "LIBRARIAN")
                .requestMatchers(HttpMethod.POST, "/api/transactions/*/checkin").hasAnyRole("ADMIN", "LIBRARIAN")

                // Reservations
                .requestMatchers(HttpMethod.GET, "/api/reservations").hasAnyRole("ADMIN", "LIBRARIAN")
                .requestMatchers(HttpMethod.GET, "/api/reservations/member/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/reservations").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/reservations/**").hasAnyRole("ADMIN", "LIBRARIAN")
                .requestMatchers(HttpMethod.DELETE, "/api/reservations/**").authenticated()

                // Fines
                .requestMatchers(HttpMethod.GET, "/api/fines").hasAnyRole("ADMIN", "LIBRARIAN")
                .requestMatchers(HttpMethod.GET, "/api/fines/member/**").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/fines/**").hasAnyRole("ADMIN", "LIBRARIAN")

                .anyRequest().authenticated())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .httpBasic(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable);
        return http.build();
    }
}
