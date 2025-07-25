package com.intsof.samples.entra.config;

import com.intsof.samples.entra.filter.AuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;

@Configuration
public class AuthenticationFilterConfig {

    @Autowired
    private AuthenticationFilter authenticationFilter;

    @Bean
    public FilterRegistrationBean<AuthenticationFilter> authenticationFilterRegistration() {
        FilterRegistrationBean<AuthenticationFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(authenticationFilter);
        registrationBean.setOrder(2); // Run after CORS filter
        registrationBean.addUrlPatterns("/*");
        return registrationBean;
    }
}
