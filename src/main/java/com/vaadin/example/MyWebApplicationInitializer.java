package com.vaadin.example;

import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.filter.DelegatingFilterProxy;

import com.vaadin.spring.server.SpringVaadinServlet;

public class MyWebApplicationInitializer implements WebApplicationInitializer {
    @Override
    public void onStartup(ServletContext container) throws ServletException {
        AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
        context.register(SecurityConfig.class);
        context.refresh();

        container.addListener(new ContextLoaderListener(context));
        container.addListener(new RequestContextListener());

        ServletRegistration.Dynamic dispatcher = container.addServlet("dispatcher", new SpringVaadinServlet());
        dispatcher.setLoadOnStartup(1);
        dispatcher.setAsyncSupported(true);
        dispatcher.addMapping("/*");

        FilterRegistration.Dynamic securityFilter = container.addFilter("springSecurityFilterChain", DelegatingFilterProxy.class);
        securityFilter.addMappingForUrlPatterns(null, false, "/*");

        container.addFilter("customFilter", CustomFilter.class).addMappingForUrlPatterns(null, false, "/*");
    }
}
