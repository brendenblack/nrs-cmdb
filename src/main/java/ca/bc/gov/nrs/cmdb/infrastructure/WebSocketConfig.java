package ca.bc.gov.nrs.cmdb.infrastructure;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;

// https://spring.io/guides/gs/messaging-stomp-websocket/#_configure_spring_for_stomp_messaging

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig extends AbstractWebSocketMessageBrokerConfigurer
{
    public static final String BROKER_ROOT_PATH = "/topic";

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry)
    {
        registry.setApplicationDestinationPrefixes("/app")
                .enableSimpleBroker(BROKER_ROOT_PATH);
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry stompEndpointRegistry)
    {
        // gs-guide-websocket
        stompEndpointRegistry.addEndpoint("/socket")
                .setAllowedOrigins("*")
                .withSockJS();
    }


}
