package com.saju.interpret.client.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LunarCalendarClientResponse(
    ResponseHeader header,
    ResponseBody body
) {

    public record ResponseHeader(
        String resultCode,
        String resultMsg
    ) {

    }

    public record ResponseBody(
        LunarCalendarItems items,
        String numOfRows,
        String pageNo,
        String totalCount
    ) {

    }

    public record LunarCalendarItems(
        LunarCalendar item
    ) {

    }

    public record LunarCalendar(
        // --- [사주 간지 (Pillars)] ---
        @JsonProperty("lunSecha")
        String yearPillar,        // 년주 (세차)

        @JsonProperty("lunWolgeon")
        String monthPillar,       // 월주 (월건)

        @JsonProperty("lunIljin")
        String dayPillar,         // 일주 (일진)

        // --- [음력 정보 (Lunar)] ---
        @JsonProperty("lunYear")
        String lunarYear,

        @JsonProperty("lunMonth")
        String lunarMonth,

        @JsonProperty("lunDay")
        String lunarDay,

        @JsonProperty("lunLeapmonth")
        String isLeapMonth,       // 윤달 여부 (평달: 평, 윤달: 윤)

        @JsonProperty("lunNday")
        String daysInMonth,       // 해당 월의 총 일수 (29 or 30)

        // --- [양력 정보 (Solar)] ---
        @JsonProperty("solYear")
        String solarYear,

        @JsonProperty("solMonth")
        String solarMonth,

        @JsonProperty("solDay")
        String solarDay,

        @JsonProperty("solWeek")
        String dayOfWeek,         // 요일 (월~일)

        @JsonProperty("solLeapyear")
        String isSolarLeap,       // 양력 윤년 여부

        @JsonProperty("solJd")
        String julianDayNumber    // 유일리우스 적일
    ) {

    }
}