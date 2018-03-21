package ca.bc.gov.nrs.cmdb;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.WebMvcRegistrationsAdapter;
import org.springframework.boot.builder.SpringApplicationBuilder;

import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.support.XmlWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;

// This application shell is based on the reference OpenShift application available at
//  https://blog.openshift.com/using-spring-boot-on-openshift/


@SpringBootApplication

public class Application extends SpringBootServletInitializer
{
    @Autowired
    private ApplicationContext context;

    public static void main(String[] args) {
        // run the application.
        SpringApplication.run(Application.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(Application.class);
    }


    // https://mhdevelopment.wordpress.com/2016/10/03/spring-restcontroller-specific-basepath/
    @Bean
    public WebMvcRegistrationsAdapter webMvcRegistrationsHandlerMapping()
    {
        return new WebMvcRegistrationsAdapter()
        {
            @Override
            public RequestMappingHandlerMapping getRequestMappingHandlerMapping()
            {
                return new RequestMappingHandlerMapping()
                {
                    private final static String API_BASE_PATH = "api";

                    @Override
                    protected void registerHandlerMethod(Object handler, Method method, RequestMappingInfo mapping)
                    {
                        Class<?> beanType = method.getDeclaringClass();
                        if (AnnotationUtils.findAnnotation(beanType, RestController.class) != null) {
                            PatternsRequestCondition apiPattern = new PatternsRequestCondition(API_BASE_PATH)
                                    .combine(mapping.getPatternsCondition());

                            mapping = new RequestMappingInfo(mapping.getName(), apiPattern,
                                                             mapping.getMethodsCondition(), mapping.getParamsCondition(),
                                                             mapping.getHeadersCondition(), mapping.getConsumesCondition(),
                                                             mapping.getProducesCondition(), mapping.getCustomCondition());
                        }

                        super.registerHandlerMethod(handler, method, mapping);
                    }
                };
            }
        };
    }


}