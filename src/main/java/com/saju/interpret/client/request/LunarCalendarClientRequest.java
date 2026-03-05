package com.saju.interpret.client.request;

public record LunarCalendarClientRequest(
    String year,
    String month,
    String day
) {

    public static LunarCalendarClientRequest of(String year, String month, String day) {
        return new LunarCalendarClientRequest(year, month, day);
    }
}