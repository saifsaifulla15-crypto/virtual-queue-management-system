package com.virtual_queue.apiGateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;
import static org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions.removeRequestHeader;

import static org.springframework.cloud.gateway.server.mvc.filter.LoadBalancerFilterFunctions.lb;
import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;
import static org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http;
import static org.springframework.cloud.gateway.server.mvc.predicate.GatewayRequestPredicates.path;
@Configuration
public class GateWayConfig {
	@Bean
    public RouterFunction<ServerResponse> userRoute() {
        return route("user-service")
                .route(path("/user/**"), http())
                .before(removeRequestHeader("Accept-Encoding"))
                .filter(lb("USER"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> queueRoute() {
        return route("queue-service")
                .route(path("/queue/**"), http())
                .before(removeRequestHeader("Accept-Encoding"))
                .filter(lb("QUEUE"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> businessRoute() {
        return route("business-service")
                .route(path("/business/**"), http())
                .before(removeRequestHeader("Accept-Encoding"))
                .filter(lb("BUSINESS"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> staffRoute() {
        return route("staff-service")
                .route(path("/staff/**"), http())
                .before(removeRequestHeader("Accept-Encoding"))
                .filter(lb("BUSINESS"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> tokenRoute() {
        return route("token-service")
                .route(path("/token/**"), http())
                .before(removeRequestHeader("Accept-Encoding"))
                .filter(lb("QUEUE"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> serviceRecordRoute() {
        return route("service-record-service")
                .route(path("/serviceRecords/**"), http())
                .before(removeRequestHeader("Accept-Encoding"))
                .filter(lb("QUEUE"))
                .build();
    }
}
