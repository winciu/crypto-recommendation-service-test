package pl.rationalworks.cryptorecommendationservicetest.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "service.crypto")
@Getter
@Setter
public class CryptoProperties {

    private List<String> supportedCurrencies;
}
