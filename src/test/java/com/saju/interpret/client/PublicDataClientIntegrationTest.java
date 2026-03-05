package com.saju.interpret.client;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class PublicDataClientIntegrationTest {

    @Autowired
    private PublicDataClient publicDataClient;

    @Test
    @DisplayName("공공데이터 API를 호출하여 음력 정보를 실 조회한다")
    void getLunarCalendar_RealApiCall() {
        // given
        String year = "2024";
        String month = "03";
        String day = "05";

        // when
        var result = publicDataClient.getLunarCalendar(year, month, day);

        // then
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(result).as("응답 데이터 존재 여부").isNotNull();
            softly.assertThat(result.solarYear()).isEqualTo(year);
            softly.assertThat(result.dayPillar()).as("일주(일진)").isNotBlank();
            softly.assertThat(result.yearPillar()).as("년주(세차)").contains("갑진"); // 2024년은 갑진년
            softly.assertThat(result.isLeapMonth()).isIn("평", "윤");
        });
    }
}