package com.intsof.samples.entra;


import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;

public class AppInitializer implements WebApplicationInitializer {


    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
        context.setConfigLocation("com.intsof.samples.entra.config");
        servletContext.addListener(new ContextLoaderListener(context));

        // AuthenticationFilter registration moved to AuthenticationFilterConfig
    }
}