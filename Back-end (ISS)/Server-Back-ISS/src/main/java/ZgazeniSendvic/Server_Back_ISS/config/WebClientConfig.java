package ZgazeniSendvic.Server_Back_ISS.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    //For the ORS API usage,
    @Bean
    WebClient orsWebClient(
        @Value("${ors.api.url}") String orsApiUrl,
        @Value("${ors.api.key}") String orsApiKey) {

        return WebClient.builder()
                .baseUrl(orsApiUrl)
                .defaultHeader("Authorization", orsApiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();

    }


}
