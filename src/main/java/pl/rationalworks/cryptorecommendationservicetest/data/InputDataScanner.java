package pl.rationalworks.cryptorecommendationservicetest.data;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.springframework.stereotype.Component;

import java.io.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Component
public class InputDataScanner {

    public List<CsvDataRecord> loadFile(InputStream inputStream) throws IOException {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(inputStream))) {
            CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                    .setHeader(Headers.class)
                    .setSkipHeaderRecord(true)
                    .build();
            CSVParser csvParser = csvFormat.parse(in);
            return csvParser.stream().map(record -> {
                        String timestamp = record.get(Headers.timestamp);
                        String symbol = record.get(Headers.symbol);
                        String price = record.get(Headers.price);
                        return new CsvDataRecord(
                                Instant.ofEpochMilli(Long.parseLong(timestamp)),
                                symbol,
                                new BigDecimal(price));
                    }
            ).toList();
        }
    }

    enum Headers {
        timestamp, symbol, price
    }

}
