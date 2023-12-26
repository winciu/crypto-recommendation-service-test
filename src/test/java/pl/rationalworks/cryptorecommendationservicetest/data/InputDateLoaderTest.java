package pl.rationalworks.cryptorecommendationservicetest.data;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class InputDateLoaderTest {

    @Test
    void shouldLoadTestDataFromCSVFileProperly() throws IOException {
        try (InputStream inputStream = getClass().getResourceAsStream("/ETH_values_test.csv")) {
            InputDataLoader inputDataLoader = new InputDataLoader();
            List<CsvDataRecord> records = inputDataLoader.loadFromFile(inputStream);
            List<String> symbols = records.stream().map(CsvDataRecord::symbol).distinct().toList();
            List<Instant> instants = records.stream().map(CsvDataRecord::timestamp).toList();
            assertThat(symbols, hasItem("ETH"));
            assertThat(instants, contains(
                    Instant.parse("2022-01-01T08:00:00Z"),
                    Instant.parse("2022-01-01T10:00:00Z"),
                    Instant.parse("2022-01-01T15:00:00Z"),
                    Instant.parse("2022-01-01T17:00:00Z"),
                    Instant.parse("2022-01-02T02:00:00Z")
            ));
            assertThat(records, hasSize(5));
        }
    }

}
