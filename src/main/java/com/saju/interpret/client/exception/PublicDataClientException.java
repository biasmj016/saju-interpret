package com.saju.interpret.client.exception;

import static com.saju.interpret.common.exception.ErrorCode.PUBLIC_DATA_CLIENT_ERROR;
import com.saju.interpret.common.exception.CustomException;
import com.saju.interpret.common.exception.ErrorCode;
import lombok.Getter;

@Getter
public class PublicDataClientException extends CustomException {

    public PublicDataClientException() {
        super(PUBLIC_DATA_CLIENT_ERROR);
    }

    public PublicDataClientException(ErrorCode errorCode) {
        super(errorCode);
    }
}
