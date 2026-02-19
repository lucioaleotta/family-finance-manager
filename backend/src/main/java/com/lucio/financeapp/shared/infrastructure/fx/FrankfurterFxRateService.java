package com.lucio.financeapp.shared.infrastructure.fx;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lucio.financeapp.config.FinanceProperties;
import com.lucio.financeapp.shared.domain.Currency;

@Service
public class FrankfurterFxRateService implements FxRateService {

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper;
    private final FinanceProperties financeProperties;
    private final Map<String, BigDecimal> cache = new ConcurrentHashMap<>();

    public FrankfurterFxRateService(ObjectMapper objectMapper, FinanceProperties financeProperties) {
        this.objectMapper = objectMapper;
        this.financeProperties = financeProperties;
    }

    @Override
    public BigDecimal getRate(Currency from, Currency to) {
        if (from == to) {
            return BigDecimal.ONE;
        }
        String key = from.name() + "->" + to.name();
        return cache.computeIfAbsent(key, k -> fetchRate(from, to));
    }

    private BigDecimal fetchRate(Currency from, Currency to) {
        String url = financeProperties.getFxBaseUrl() + "/latest?from=" + from.name() + "&to=" + to.name();
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .GET()
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new IllegalStateException("FX rate request failed: " + response.statusCode());
            }
            JsonNode root = objectMapper.readTree(response.body());
            JsonNode rateNode = root.path("rates").path(to.name());
            if (rateNode.isMissingNode()) {
                throw new IllegalStateException("FX rate missing for " + from + "->" + to);
            }
            return rateNode.decimalValue();
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("FX rate request failed", e);
        }
    }
}
