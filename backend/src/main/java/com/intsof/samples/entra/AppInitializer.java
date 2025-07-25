package com.intsof.samples.entra;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.filter.DelegatingFilterProxy;

import com.intsof.samples.entra.filter.AuthenticationFilter;

import jakarta.servlet.FilterRegistration;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;

public class AppInitializer implements WebApplicationInitializer {

    @Autowired
    private AuthenticationFilter authenticationFilter;

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
        context.setConfigLocation("com.intsof.samples.entra.config");
        servletContext.addListener(new ContextLoaderListener(context));

        FilterRegistrationBean<AuthenticationFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter( authenticationFilter);
        registrationBean.setOrder(2); // Run after CORS filter (default order is 0)
        registrationBean.addUrlPatterns("/*");
      
    }
}