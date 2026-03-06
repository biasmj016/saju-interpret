package com.saju.interpret.client.request;

import java.util.Map;

public record LunarCalendarClientRequest(
    String year,
    String month,
    String day
) {

    public static LunarCalendarClientRequest of(String year, String month, String day) {
        return new LunarCalendarClientRequest(year, month, day);
    }

    public Map<String, String> toQueryParams() {
        return Map.of(
            "solYear", year,
            "solMonth", month,
            "solDay", day
        );
    }
}