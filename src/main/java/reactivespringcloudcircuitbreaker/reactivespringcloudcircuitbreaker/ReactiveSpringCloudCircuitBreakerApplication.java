package reactivespringcloudcircuitbreaker.reactivespringcloudcircuitbreaker;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.function.Function;

@SpringBootApplication
public class ReactiveSpringCloudCircuitBreakerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReactiveSpringCloudCircuitBreakerApplication.class, args);
    }

    @Bean
    WebClient webClient(WebClient.Builder builder) {
        return builder
                .baseUrl("http://localhost:8089")
                .build();
    }
}

@Log4j2
@Component
//@RequiredArgsConstructor
class Client {
    private final WebClient client;
    private final ReactiveCircuitBreaker reactiveCircuitBreaker;

    Client(WebClient webClient, ReactiveCircuitBreakerFactory reactiveCircuitBreakerFactory) {
        this.client = webClient;
        this.reactiveCircuitBreaker = reactiveCircuitBreakerFactory.create("greeting");
    }

    @EventListener(ApplicationReadyEvent.class)
    public void read() {
        var name = "Spring Developer";
        Mono<String> http = this.client
                .get()
                .uri("/greeting/{name}", name)
                .retrieve()
                .bodyToMono(GreetingResponse.class)
                .map(GreetingResponse::getMessage);

        this.reactiveCircuitBreaker.run(http, throwable -> Mono.just("Ooops"))
                .subscribe(greetingResponse -> log.info(" Mono :" + greetingResponse));
    }

}

@Data
@NoArgsConstructor
@AllArgsConstructor
class GreetingRequest {
    private String name;

}

@Data
@NoArgsConstructor
@AllArgsConstructor
class GreetingResponse {
    private String message;
}