package reactivespringcloudcircuitbreaker.reactivespringcloudcircuitbreaker;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
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

//   explore service hedging
//    Flux<String> host1=null;
//    Flux<String> host2=null;
//    Flux<String> host3=null;
//      Flux<String> firstNodeResponded=FLux.first(host1,host2,host3);
//
    Client(WebClient client, ReactiveCircuitBreakerFactory reactiveCircuitBreakerFactory) {
        this.client = client;
        this.reactiveCircuitBreaker = reactiveCircuitBreakerFactory.create("greeting");
    }


    @EventListener(ApplicationReadyEvent.class)
    public void ready() {
        var name = "Spring Developer";
        Mono<String> http = this.client
                .get()
                .uri("/greeting/{name}", name)
                .retrieve()
                .bodyToMono(GreetingResponse.class)
                .map(GreetingResponse::getMessage);
        reactiveCircuitBreaker.run(
                        http,
                        throwable -> Mono.just("drenched in error")
                )
                .subscribe(gr -> log.info("Mono" + gr));


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