package no.fintlabs;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "fint.kontroll.resource-catalog")
@Data
public class ServiceConfiguration {
    String source;

}
