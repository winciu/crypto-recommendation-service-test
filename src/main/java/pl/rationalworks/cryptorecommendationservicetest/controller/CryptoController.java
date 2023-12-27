package pl.rationalworks.cryptorecommendationservicetest.controller;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.rationalworks.cryptorecommendationservicetest.model.dto.CryptoCurrencyFactorsDto;
import pl.rationalworks.cryptorecommendationservicetest.properties.CryptoProperties;
import pl.rationalworks.cryptorecommendationservicetest.service.CryptoCurrencyService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@RestController
@RequestMapping("/crypto")
@RequiredArgsConstructor
@Validated
public class CryptoController {

    private final CryptoCurrencyService cryptoCurrencyService;
    private final CryptoProperties cryptoProperties;

    @GetMapping("/{symbol}/monthly/{month}/factors")
    public ResponseEntity<CryptoCurrencyFactorsDto> obtainCryptoMonthlyFactors(
        @PathVariable("symbol") @NotBlank @Pattern(regexp = "[A-Z]{3}") String symbol,
        @PathVariable("month") @NotBlank @Pattern(regexp = "\\d{4}-\\d{2}") String month) {
        if (!cryptoProperties.getSupportedCurrencies().contains(symbol)) {
            return ResponseEntity.badRequest().build();
        }
        LocalDate localDate = LocalDate.parse(month, DateTimeFormatter.ofPattern("yyyy-MM"));
//        cryptoCurrencyService.fetchOldestAndNewsetPrices(symbol, month);
        return ResponseEntity.ok().build();
    }
}
