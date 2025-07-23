package com.planup.planup.apiPayload.code.status;

import com.planup.planup.apiPayload.code.BaseErrorCode;
import com.planup.planup.apiPayload.code.ErrorReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorStatus implements BaseErrorCode {

    // 가장 일반적인 응답
    _INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON500", "서버 에러, 관리자에게 문의 바랍니다."),
    _BAD_REQUEST(HttpStatus.BAD_REQUEST,"COMMON400","잘못된 요청입니다."),
    _UNAUTHORIZED(HttpStatus.UNAUTHORIZED,"COMMON401","인증이 필요합니다."),
    _FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON403", "금지된 요청입니다."),


    //User 에러
    NOT_FOUND_USER(HttpStatus.NOT_FOUND, "USER4001", "존재하지 않는 유저입니다"),
    EXIST_NICKNAME(HttpStatus.CONFLICT, "USER4002", "이미 존재하는 닉네임입니다"),

    //Report 관련 에러
    NOT_FOUND_WEEKLY_REPORT(HttpStatus.NOT_FOUND, "WEEKLY_REPORT4001", "존재하지 않는 주간 리포트입니다")

    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public ErrorReasonDTO getReason() {
        return ErrorReasonDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(false)
                .build();
    }

    @Override
    public ErrorReasonDTO getReasonHttpStatus() {
        return ErrorReasonDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(false)
                .httpStatus(httpStatus)
                .build()
                ;
    }
}
