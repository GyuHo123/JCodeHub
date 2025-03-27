package org.jbnu.jdevops.jcodeportallogin.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class WebClientConfig {

    @Value("\${watcher.url}")
    private lateinit var watcherUrl: String

    @Value("\${generator.url}")
    private lateinit var generatorUrl: String

    @Bean
    fun watcherWebClient(): WebClient {
        return WebClient.builder()
            .baseUrl(watcherUrl)
            .build()
    }

    @Bean
    fun generatorWebClient(): WebClient {
        return WebClient.builder()
            .baseUrl(generatorUrl)
            .build()
    }
}


