package com.saju.interpret.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import com.saju.interpret.client.request.LunarCalendarClientRequest;
import com.saju.interpret.client.response.LunarCalendarClientResponse;
import com.saju.interpret.client.response.LunarCalendarClientResponse.LunarCalendar;
import com.saju.interpret.client.response.LunarCalendarClientResponse.ResponseBody;
import com.saju.interpret.client.response.LunarCalendarClientResponse.ResponseHeader;
import com.saju.interpret.client.response.LunarCalendarClientResponse.LunarCalendarItems;
import java.util.function.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

@ExtendWith(MockitoExtension.class)
class PublicDataClientTest {

    private PublicDataClient client;
    private RestClient restClient;
    private PublicDataClientProperties properties;

    @BeforeEach
    void setUp() {
        restClient = mock(RestClient.class);
        properties = new PublicDataClientProperties(
            "http://base.url",
            "test-key",
            new PublicDataClientProperties.Endpoints("/test-path")
        );
        client = new PublicDataClient(restClient, properties);
    }

    @Test
    @DisplayName("공공데이터로부터 사주 원천 데이터를 정상적으로 파싱한다")
    void getLunarCalendar_success() {
        // given
        var expectedItem = createSajuFixture();
        var response = new LunarCalendarClientResponse(
            new ResponseHeader("00", "OK"),
            new ResponseBody(new LunarCalendarItems(expectedItem), "1", "1", "1")
        );

        var requestSpec = mock(RestClient.RequestHeadersUriSpec.class);
        var responseSpec = mock(RestClient.ResponseSpec.class);

        when(restClient.get()).thenReturn(requestSpec);
        when(requestSpec.uri(any(Function.class))).thenReturn(requestSpec);
        when(requestSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(LunarCalendarClientResponse.class)).thenReturn(response);

        // when
        LunarCalendarClientRequest request = LunarCalendarClientRequest.of("1996", "10", "22");
        var result = client.getLunarCalendar(request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.dayPillar()).isEqualTo("임진(壬辰)");
        assertThat(result.yearPillar()).isEqualTo("병자(丙子)");
        assertThat(result.isLeapMonth()).isEqualTo("평");
    }

    @Test
    @DisplayName("API 응답이 비어있거나 데이터가 없으면 IllegalStateException을 던진다")
    void getLunarCalendar_emptyDataThrowsException() {
        // given
        var requestSpec = mock(RestClient.RequestHeadersUriSpec.class);
        var responseSpec = mock(RestClient.ResponseSpec.class);
        when(restClient.get()).thenReturn(requestSpec);
        when(requestSpec.uri(any(Function.class))).thenReturn(requestSpec);
        when(requestSpec.retrieve()).thenReturn(responseSpec);

        when(responseSpec.body(LunarCalendarClientResponse.class)).thenReturn(null);

        // when & then
        LunarCalendarClientRequest request = LunarCalendarClientRequest.of("1996", "10", "22");
        assertThatThrownBy(() -> client.getLunarCalendar(request))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("사주 원천 데이터 조회에 실패했습니다.");
    }

    private LunarCalendar createSajuFixture() {
        return new LunarCalendar(
            "병자(丙子)", // yearPillar
            "무술(戊戌)", // monthPillar
            "임진(壬辰)", // dayPillar
            "1996",     // lunarYear
            "09",       // lunarMonth
            "11",       // lunarDay
            "평",       // isLeapMonth
            "30",       // daysInMonth
            "1996",     // solarYear
            "10",       // solarMonth
            "22",       // solarDay
            "화",       // dayOfWeek
            "윤",       // isSolarLeap
            "2450379"   // julianDayNumber
        );
    }
}