package com.saju.interpret.client;

import static java.util.Objects.nonNull;
import com.saju.interpret.client.response.LunarCalendarClientResponse;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilder;

@Component
@RequiredArgsConstructor
public class PublicDataClient {

    private final RestClient publicDataRestClient;
    private final PublicDataClientProperties properties;

    public LunarCalendarClientResponse.LunarCalendar getLunarCalendar(String year, String month, String day) {
        Map<String, String> queryParams = createQueryParams(year, month, day);

        LunarCalendarClientResponse response = publicDataRestClient.get()
            .uri(uriBuilder -> buildUri(uriBuilder, queryParams))
            .retrieve()
            .body(LunarCalendarClientResponse.class);

        return extractLunarCalendar(response);
    }

    private Map<String, String> createQueryParams(String year, String month, String day) {
        Map<String, String> params = new HashMap<>();
        params.put("solYear", year);
        params.put("solMonth", month);
        params.put("solDay", day);
        params.put("ServiceKey", properties.serviceKey());
        return params;
    }

    private URI buildUri(UriBuilder uriBuilder, Map<String, String> queryParams) {
        uriBuilder.path(properties.endpoints().lunarCalendar());
        queryParams.forEach((key, value) -> {
            if (nonNull(value)) {
                uriBuilder.queryParam(key, value);
            }
        });
        return uriBuilder.build();
    }

    private LunarCalendarClientResponse.LunarCalendar extractLunarCalendar(LunarCalendarClientResponse response) {
        if (response == null ||
            response.body() == null ||
            response.body().items() == null ||
            response.body().items().item() == null) {
            throw new IllegalStateException("공공데이터 API 응답에러.");
        }
        return response.body().items().item();
    }
}