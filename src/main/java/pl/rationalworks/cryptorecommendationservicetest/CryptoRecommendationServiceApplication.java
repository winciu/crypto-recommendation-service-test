package pl.rationalworks.cryptorecommendationservicetest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.EnableScheduling;
import pl.rationalworks.cryptorecommendationservicetest.data.CsvDataRecord;
import pl.rationalworks.cryptorecommendationservicetest.data.InputDataLoader;
import pl.rationalworks.cryptorecommendationservicetest.service.CryptoCurrencyService;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SpringBootApplication
@EnableScheduling
@Slf4j
public class CryptoRecommendationServiceApplication {


	/**
	 * We need to load input data during application startup.
	 * @param dataLoader {@link InputDataLoader} instance
	 * @param service
	 * @return lambda expression for loading available data files in a given directory
	 * @throws IOException
	 */
	@Bean
	InitializingBean loadInputData(InputDataLoader dataLoader, CryptoCurrencyService service) throws IOException {
		ClassPathResource classPathResource = new ClassPathResource("static/Prices");
		Set<String> filePaths;
		try (Stream<Path> stream = Files.walk(Paths.get(classPathResource.getURI()), 1)) {
			filePaths = stream
					.filter(file -> !Files.isDirectory(file))
					.map(Path::toAbsolutePath)
					.map(Path::toString)
					.collect(Collectors.toSet());
		}
		return () -> {
			filePaths.forEach(path -> {
				try {
					List<CsvDataRecord> records = dataLoader.loadFromFile(new FileInputStream(path));
					service.saveCryptos(records);
				} catch (IOException e) {
					log.error("Error while loading data file {}", path, e);
				}
			});
		};
	}

	public static void main(String[] args) {
		SpringApplication.run(CryptoRecommendationServiceApplication.class, args);
	}

}
