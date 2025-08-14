package org.elmorshedy.security;

import org.elmorshedy.security.jwt.AuthEntryPointJwtImp;
import org.elmorshedy.security.jwt.AuthTokenFilter;
import org.elmorshedy.security.jwt.JwtUtils;
import org.elmorshedy.security.service.UserDetailsServiceImp;
import org.elmorshedy.user.model.Role;
import org.elmorshedy.user.model.User;
import org.elmorshedy.user.repo.RoleRepo;
import org.elmorshedy.user.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.elmorshedy.user.model.AppRole;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

import java.time.LocalDate;


@Configuration
public class securityConfig {
    @Autowired
    private AuthEntryPointJwtImp unauthorizedHandler;
    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailsServiceImp userDetailsService;
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
    @Bean
    public AuthTokenFilter authtokenfilter() {
        return new AuthTokenFilter(jwtUtils,userDetailsService);
    }

    @Bean
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests((requests) ->
                requests
//                        .requestMatchers("/api/admin/**").hasAnyRole("ADMIN")
//                        .requestMatchers("/api/csrf-token").permitAll()
                        .requestMatchers("/api/auth/public/**").permitAll()
                        .anyRequest().authenticated());
//        http.csrf(csrf ->
//                csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
//                        .ignoringRequestMatchers("/api/auth/public/**")
//        );
        http.csrf(AbstractHttpConfigurer::disable);
        //todo مش فاهم
        //        http.addFilterBefore(new CustomLoggingFilter(),
        //                UsernamePasswordAuthenticationFilter.class);
        //        http.addFilterAfter(new RequestValidationFilter(),
        //                CustomLoggingFilter.class);
        http.exceptionHandling(exception ->
                exception.authenticationEntryPoint(unauthorizedHandler));
        http.addFilterBefore(authtokenfilter(),
                UsernamePasswordAuthenticationFilter.class);
//        http.formLogin(Customizer.withDefaults());
//        http.httpBasic(Customizer.withDefaults());
        return (SecurityFilterChain) http.build();
    }

    @Bean
    public CommandLineRunner initData (RoleRepo roleRepo, UserRepo userRepo,PasswordEncoder passwordEncoder){
        return args -> {
            Role userRole = roleRepo.findByRolename(AppRole.SALES_REP)
                    .orElseGet(()->roleRepo.save(new Role (AppRole.SALES_REP)));
            Role adminRole = roleRepo.findByRolename(AppRole.ROLE_ADMIN)
                    .orElseGet(()->roleRepo.save(new Role(AppRole.ROLE_ADMIN)));
            if (!userRepo.existsByUsername("user1")){
                User user1 =new User("user1","user1@gmail.com",passwordEncoder.encode("password1"));
                local_signup(userRepo, userRole, user1);
            }
            if (!userRepo.existsByUsername("admin")){
                User admin =new User("admin","admin@gmail.com",passwordEncoder.encode("adminpass"));
                local_signup(userRepo, adminRole, admin);
            }
        };}

    private void local_signup(UserRepo userRepo, Role userRole, User admin) {
//        admin.setAccountNonLocked(false);
//        admin.setAccountNonExpired(true);
//        admin.setCredentialsNonExpired(true);
//        admin.setEnabled(true);
//        admin.setCredentialsExpiredData(LocalDate.now().plusYears(1));
//        admin.setAccountExpiryData(LocalDate.now().plusYears(1));
//        admin.setTwofactorEnabled(false);
//        admin.setSignUpMethod("email");
        admin.setRole(userRole);
        userRepo.save(admin);
    }

}
