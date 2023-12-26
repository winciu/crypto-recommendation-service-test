package pl.rationalworks.cryptorecommendationservicetest.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "service.scheduling")
@Getter
@Setter
public class SchedulingProperties {

    private List<LocalDate> predefinedDates;
}
