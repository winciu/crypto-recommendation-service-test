package pl.rationalworks.cryptorecommendationservicetest.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pl.rationalworks.cryptorecommendationservicetest.model.FactorPeriod;
import pl.rationalworks.cryptorecommendationservicetest.model.dto.CryptoCurrencyDto;
import pl.rationalworks.cryptorecommendationservicetest.model.dto.CryptoCurrencyFactorsDto;
import pl.rationalworks.cryptorecommendationservicetest.properties.CryptoProperties;
import pl.rationalworks.cryptorecommendationservicetest.repository.CryptoDailyPriceFactors;
import pl.rationalworks.cryptorecommendationservicetest.service.CryptoCurrencyService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/cryptos")
@RequiredArgsConstructor
@Validated
public class CryptoController {

    private final CryptoCurrencyService cryptoCurrencyService;
    private final CryptoProperties cryptoProperties;

    @GetMapping(value = {"/ranking", "/ranking/{date}", "/ranking/{date}/{period}"})
    @Operation(summary = "Returns a descending sorted list of all cryptos using a normalized factor as a comparator.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Ranking was found",
            content = {@Content(mediaType = "application/json", examples = {
                @ExampleObject(value = "[{\"symbol\":\"XRP\"},{\"symbol\":\"DOGE\"},{\"symbol\":\"ETH\"},{\"symbol\":\"LTC\"},{\"symbol\":\"BTC\"}]")
            }, schema = @Schema(implementation = CryptoCurrencyDto.class))}),
        @ApiResponse(responseCode = "400", description = "Invalid parameter supplied",
            content = @Content),
        @ApiResponse(responseCode = "404", description = "There is no data to calculate the requested ranking",
            content = @Content)})
    public ResponseEntity<List<CryptoCurrencyDto>> cryptoRanking(@PathVariable("date")
                                                                 @DateTimeFormat(pattern = "yyyy-MM-dd") Optional<LocalDate> date,
                                                                 @PathVariable("period") Optional<FactorPeriod> period) {
        LocalDate referenceDate = date.orElse(LocalDate.now());
        List<CryptoCurrencyDto> rankingList = cryptoCurrencyService.cryptoRanking(referenceDate, period.orElse(FactorPeriod.DAY));
        return ResponseEntity.ok(rankingList);
    }

    @GetMapping(value = {"/{symbol}/factors", "/{symbol}/factors/{date}", "/{symbol}/factors/{date}/{period}"})
    @Operation(summary = "Returns price factors related to the requested crypto")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Factors for the requested cryptocurrency exist",
            content = {@Content(mediaType = "application/json", examples = {
                @ExampleObject(value = "{\"symbol\":\"DOGE\",\"referenceDate\":\"2022-01-05\",\"minPrice\":0.16900,\"minPriceDate\":\"2022-01-04T22:00:00Z\",\"maxPrice\":0.17310,\"maxPriceDate\":\"2022-01-03T01:00:00Z\",\"oldestPrice\":0.17020,\"oldestPriceDate\":\"2022-01-01T05:00:00Z\",\"newestPrice\":0.16900,\"newestPriceDate\":\"2022-01-04T22:00:00Z\",\"period\":\"WEEK\"}")
            }, schema = @Schema(implementation = CryptoCurrencyFactorsDto.class))}),
        @ApiResponse(responseCode = "400", description = "Invalid parameter supplied",
            content = @Content),
        @ApiResponse(responseCode = "404", description = "Factors for the requested cryptocurrency are not available (yet)",
            content = @Content)})
    public ResponseEntity<CryptoCurrencyFactorsDto> obtainCryptoPriceFactors(
        @PathVariable("symbol")
        @Pattern(regexp = "[A-Z]{3,6}", message = "Cryptocurrency symbol must match '[A-Z]{3,6}'") String symbol,
        @PathVariable("date") @DateTimeFormat(pattern = "yyyy-MM-dd") Optional<LocalDate> date,
        @PathVariable("period") Optional<FactorPeriod> period) {
        if (!cryptoProperties.getSupportedCurrencies().contains(symbol)) {
            return ResponseEntity.badRequest().build();
        }
        LocalDate referenceDate = date.orElse(LocalDate.now());
        FactorPeriod factorPeriod = period.orElse(FactorPeriod.DAY);
        Optional<CryptoDailyPriceFactors> factors = cryptoCurrencyService.getCryptoPriceFactors(symbol, referenceDate,
            factorPeriod);
        return factors.
            map(f -> {
                CryptoCurrencyFactorsDto dto = new CryptoCurrencyFactorsDto(f.symbol(), referenceDate,
                    f.minPrice(), f.minPriceDate(), f.maxPrice(), f.maxPriceDate(), f.oldestPrice(), f.oldestPriceDate(),
                    f.newestPrice(), f.newestPriceDate(), factorPeriod);
                return ResponseEntity.ok(dto);
            })
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Returns a cryptocurrency with the highest normalized factor for the specified period.")
    @GetMapping(value = {"/best", "/best/{date}", "/best/{date}/{period}"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "The cryptocurrency with highest normalized factor has been calculated successfully",
            content = {@Content(mediaType = "application/json", examples = {
                @ExampleObject(value = "{\"symbol\":\"ETH\"}")
            }, schema = @Schema(implementation = CryptoCurrencyDto.class))}),
        @ApiResponse(responseCode = "400", description = "Invalid parameter supplied",
            content = @Content),
        @ApiResponse(responseCode = "404", description = "There are no data for the specified period to calculate the best cryptocurrency",
            content = @Content)})
    public ResponseEntity<CryptoCurrencyDto> obtainBestCrypto(@PathVariable("date")
                                                              @DateTimeFormat(pattern = "yyyy-MM-dd") Optional<LocalDate> date,
                                                              @PathVariable("period") Optional<FactorPeriod> period) {
        LocalDate referenceDate = date.orElse(LocalDate.now());
        FactorPeriod factorPeriod = period.orElse(FactorPeriod.DAY);
        Optional<CryptoCurrencyDto> cryptoCurrency = cryptoCurrencyService.getBestCrypto(referenceDate, factorPeriod);
        return cryptoCurrency.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ResponseEntity<?> handleConstraintViolationException(ConstraintViolationException e) {
        return new ResponseEntity<>("Validation Error: " + e.getMessage(), HttpStatus.BAD_REQUEST);
    }
}
