
package com.shopsphere.product.config;

import com.shopsphere.events.product.ProductEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.converter.JacksonJsonMessageConverter;
import org.springframework.kafka.support.converter.RecordMessageConverter;

@Configuration
public class KafkaConsumerConfig {

    @Bean
    public RecordMessageConverter productEventMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
