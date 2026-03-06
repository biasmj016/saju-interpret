package com.saju.interpret.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    PUBLIC_DATA_CLIENT_ERROR("공공데이터 API 호출에 실패했습니다."),
    PUBLIC_DATA_CLIENT_SERVER_ERROR("공공데이터 API 서버 에러가 발생했습니다."),
    LUNAR_CALENDAR_FETCH_FAILED("사주 원천 데이터 조회에 실패했습니다."),
    ;

    private final String message;
}
