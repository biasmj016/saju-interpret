package com.saju.interpret.client;

import com.saju.interpret.client.request.LunarCalendarClientRequest;
import com.saju.interpret.client.response.LunarCalendarClientResponse;
import com.saju.interpret.client.response.LunarCalendarClientResponse.LunarCalendar;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilder;

@Slf4j
@Component
@RequiredArgsConstructor
public class PublicDataClient {

    private final RestClient publicDataRestClient;
    private final PublicDataClientProperties properties;

    public LunarCalendar getLunarCalendar(LunarCalendarClientRequest request) {
        String path = properties.endpoints().lunarCalendar();

        LunarCalendarClientResponse response = publicDataRestClient.get()
            .uri(uriBuilder -> buildUri(uriBuilder, path, request))
            .retrieve()
            .body(LunarCalendarClientResponse.class);

        return handleResponse(request, response);
    }

    private URI buildUri(UriBuilder uriBuilder, String path, LunarCalendarClientRequest request) {
        return uriBuilder
            .path(path)
            .queryParam("solYear", request.year())
            .queryParam("solMonth", request.month())
            .queryParam("solDay", request.day())
            .queryParam("ServiceKey", properties.serviceKey())
            .build();
    }

    private LunarCalendar handleResponse(LunarCalendarClientRequest request, LunarCalendarClientResponse response) {
        if (response == null || response.isFailed()) {
            log.error("공공데이터 API 호출 실패 - 요청정보: {}, 응답내용: {}", request, response);
            throw new IllegalStateException("사주 원천 데이터 조회에 실패했습니다.");
    }

        return response.getLunarCalendar();
    }
}