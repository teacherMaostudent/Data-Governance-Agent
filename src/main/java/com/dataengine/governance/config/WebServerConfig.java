package com.dataengine.governance.config;

import org.apache.coyote.http11.Http11Nio2Protocol;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Configuration;

/**
 * NIO2 avoids the JDK 25 Windows selector loopback issue while retaining
 * non-blocking request handling. Java 21 deployments behave identically.
 */
@Configuration
public class WebServerConfig implements WebServerFactoryCustomizer<TomcatServletWebServerFactory> {
    @Override
    public void customize(TomcatServletWebServerFactory factory) {
        factory.setProtocol(Http11Nio2Protocol.class.getName());
    }
}
