package com.saju.interpret.client;

import static com.saju.interpret.common.exception.ErrorCode.LUNAR_CALENDAR_FETCH_FAILED;
import static com.saju.interpret.common.exception.ErrorCode.PUBLIC_DATA_CLIENT_SERVER_ERROR;
import com.saju.interpret.client.exception.PublicDataClientException;
import com.saju.interpret.client.request.LunarCalendarClientRequest;
import com.saju.interpret.client.response.LunarCalendarClientResponse;
import com.saju.interpret.client.response.LunarCalendarClientResponse.LunarCalendar;
import com.saju.interpret.common.exception.ErrorCode;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
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
            .onStatus(HttpStatusCode::is4xxClientError, this::handle4xxError)
            .onStatus(HttpStatusCode::is5xxServerError, this::handle5xxError)
            .body(LunarCalendarClientResponse.class);

        return handleResponse(request, response, LUNAR_CALENDAR_FETCH_FAILED);
    }

    private URI buildUri(UriBuilder uriBuilder, String path, LunarCalendarClientRequest request) {
        uriBuilder.path(path);

        request.toQueryParams().forEach(uriBuilder::queryParam);

        return uriBuilder.queryParam("ServiceKey", properties.serviceKey()).build();
    }

    private void handle4xxError(HttpRequest httpRequest, ClientHttpResponse clientHttpResponse) {
        handleError(httpRequest, clientHttpResponse, LUNAR_CALENDAR_FETCH_FAILED);
    }

    private void handle5xxError(HttpRequest httpRequest, ClientHttpResponse clientHttpResponse) {
        handleError(httpRequest, clientHttpResponse, PUBLIC_DATA_CLIENT_SERVER_ERROR);
    }

    private void handleError(HttpRequest request, ClientHttpResponse response, ErrorCode errorCode) {
        try {
            String body = new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);
            log.error("공공데이터 API 에러 - uri: {}, status: {}, body: {}", request.getURI(), response.getStatusCode(), body);
        } catch (IOException e) {
            log.error("공공데이터 API 에러 - uri: {}, 응답 본문 읽기 실패", request.getURI(), e);
        }
        throw new PublicDataClientException(errorCode);
    }

    private LunarCalendar handleResponse(LunarCalendarClientRequest request, LunarCalendarClientResponse response, ErrorCode errorCode) {
        if (response == null || response.isFailed()) {
            log.error("공공데이터 API 호출 실패 :: - 요청정보: {}, 응답내용: {}", request, response);
            throw new PublicDataClientException(errorCode);
        }

        return response.getLunarCalendar();
    }
}