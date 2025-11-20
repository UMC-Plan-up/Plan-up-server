package com.planup.planup.apiPayload.code.status;

import com.planup.planup.apiPayload.code.BaseErrorCode;
import com.planup.planup.apiPayload.code.ErrorReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Getter
@AllArgsConstructor
public enum ErrorStatus implements BaseErrorCode {

    // 가장 일반적인 응답
    _INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON500", "서버 에러, 관리자에게 문의 바랍니다."),
    _BAD_REQUEST(HttpStatus.BAD_REQUEST,"COMMON400","잘못된 요청입니다."),
    _UNAUTHORIZED(HttpStatus.UNAUTHORIZED,"COMMON401","인증이 필요합니다."),
    _FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON403", "금지된 요청입니다."),
    _NOT_ALLOWED(HttpStatus.FORBIDDEN, "COMMON404", "권한이 없는 요청입니다"),
    _NON_TRANSACTION(BAD_REQUEST, "COMMON405", "트랜잭션이 적용되지 않는 요청입니다."),

    //Notification 관련 에러
    UNAUTHORIZED_NOTIFICATION_ACCESS(HttpStatus.FORBIDDEN, "NOTIFICATION4001", "알림을 읽을 권한이 없습니다."),


    // User 에러
    NOT_FOUND_USER(HttpStatus.NOT_FOUND, "USER4001", "존재하지 않는 유저입니다"),
    EXIST_NICKNAME(HttpStatus.CONFLICT, "USER4002", "이미 존재하는 닉네임입니다"),


    //Friend
    ALREADY_FRIEND(BAD_REQUEST, "FRIEND4001", "이미 친구 관계입니다."),
    ALREADY_REQUESTED(BAD_REQUEST, "FRIEND4002", "이미 친구 신청을 하였습니다."),
    SAME_USER(BAD_REQUEST, "FRIEND4003", "친구 신청자와 대상자가 동일 인물입니다."),
    FRIEND_BLOCKED(BAD_REQUEST, "FRIEND4004", "차단된 친구입니다."),
    NOT_EXIST_USERBLOCK(BAD_REQUEST, "FRIEND4005", "이미 친구 차단 상태가 아닙니다."),
    NOT_FRIEND(BAD_REQUEST, "FRIEND4006", "친구 관계가 아닙니다."),

    // 로그인, 회원가입 관련 에러
    USER_EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "USER4003", "이미 존재하는 이메일입니다"),
    PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, "USER4004", "비밀번호가 일치하지 않습니다"),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "USER4005", "이메일 또는 비밀번호가 잘못되었습니다"),
    USER_INACTIVE(HttpStatus.FORBIDDEN, "USER4006", "비활성화된 계정입니다"),

    //Challenge 에러
    MISSING_TIME_CHALLENGE_INFO(HttpStatus.BAD_REQUEST, "CHALLENGE4001", "시간 챌린지 정보가 필요합니다."),
    MISSING_PHOTO_CHALLENGE_INFO(HttpStatus.BAD_REQUEST, "CHALLENGE4002", "사진 챌린지 정보가 필요합니다."),
    INVALID_HTTP_CHALLENGE_METHOD(HttpStatus.METHOD_NOT_ALLOWED, "CHALLENGE4007", "챌린지 생성 요청이 아닙니다."),
    INVALID_CHALLENGE_TYPE(HttpStatus.BAD_REQUEST, "CHALLENGE4003", "올바르지 않는 챌리지 타입입니다"),
    INVALID_CHALLENGE_STATUS(BAD_REQUEST, "CHALLENGE4004", "요청을 처리할 수 없는 챌린지 상태입니다."),


    //Report 관련 에러
    NOT_FOUND_WEEKLY_REPORT(HttpStatus.NOT_FOUND, "WEEKLY_REPORT4001", "존재하지 않는 주간 리포트입니다"),
    NOT_FOUND_GOAL_REPORT(HttpStatus.NOT_FOUND, "GOAL_REPORT4001", "존재하지 않는 목표 리포트입니다"),
    NOT_FOUND_CHALLENGE(HttpStatus.NOT_FOUND, "CHALLENGE4004", "존재하지 않는 챌린지 입니다."),
    INVALID_CHALLENGE_DATA(INTERNAL_SERVER_ERROR, "CHALLENGE5001", "손상된 챌린지 데이터입니다."),

    // 약관 관련 에러
    NOT_FOUND_TERMS(HttpStatus.NOT_FOUND, "4004", "존재하지 않는 약관입니다."),
    REQUIRED_TERMS_NOT_AGREED(HttpStatus.BAD_REQUEST, "4005", "필수 약관에 동의해야 합니다."),
    TERMS_AGREEMENT_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "5001", "약관 동의 처리 중 오류가 발생했습니다."),
    NOT_FOUND_USERGOAL(HttpStatus.NOT_FOUND, "CHALLENGE4009", "찾지 못함"),

    // 초대코드 관련 에러
    INVALID_INVITE_CODE(BAD_REQUEST, "INVITE001", "유효하지 않은 초대코드입니다."),
    EXPIRED_INVITE_CODE(BAD_REQUEST, "INVITE002", "만료된 초대코드입니다."),
    INVITE_CODE_ALREADY_USED(BAD_REQUEST, "INVITE003", "이미 사용된 초대코드입니다."),

    // 이메일 인증 관련 에러
    INVALID_EMAIL_TOKEN(BAD_REQUEST, "EMAIL4001", "유효하지 않거나 만료된 인증 토큰입니다."),
    EMAIL_ALREADY_VERIFIED(BAD_REQUEST, "EMAIL4002", "이미 인증된 이메일입니다."),
    EMAIL_VERIFICATION_FAILED(INTERNAL_SERVER_ERROR, "EMAIL5001", "이메일 인증 처리 중 오류가 발생했습니다."),
    EMAIL_VERIFICATION_REQUIRED(HttpStatus.BAD_REQUEST, "VERIFICATION_001", "이메일 인증이 필요합니다"),

    // 카카오 소셜 로그인 관련 에러
    KAKAO_AUTH_FAILED(BAD_REQUEST, "KAKAO4001", "카카오 인증에 실패했습니다."),
    KAKAO_TOKEN_INVALID(BAD_REQUEST, "KAKAO4002", "유효하지 않은 카카오 토큰입니다."),
    KAKAO_USER_INFO_FAILED(INTERNAL_SERVER_ERROR, "KAKAO5001", "카카오 사용자 정보 조회에 실패했습니다."),
    KAKAO_ACCOUNT_ALREADY_LINKED(BAD_REQUEST, "KAKAO4003", "이미 카카오 계정이 연동되어 있습니다."),
    KAKAO_ACCOUNT_ALREADY_USED(BAD_REQUEST, "KAKAO4004", "이미 다른 사용자에게 연동된 카카오 계정입니다."),

    // Comment 관련 에러
    NOT_FOUND_COMMENT(HttpStatus.NOT_FOUND, "COMMENT4001", "존재하지 않는 댓글입니다."),
    UNAUTHORIZED_COMMENT_ACCESS(HttpStatus.FORBIDDEN, "COMMENT4002", "댓글에 대한 권한이 없습니다."),
    INACTIVE_COMMENT(HttpStatus.BAD_REQUEST, "COMMENT4003", "삭제된 댓글입니다."),
    INVALID_PARENT_COMMENT(HttpStatus.BAD_REQUEST, "COMMENT4004", "유효하지 않은 부모 댓글입니다."),
    UNAUTHORIZED_GOAL_ACCESS(HttpStatus.FORBIDDEN, "COMMENT4005", "해당 목표에 접근할 권한이 없습니다."),

    // Goal 관련 에러
    NOT_FOUND_GOAL(HttpStatus.NOT_FOUND, "GOAL4001", "존재하지 않는 목표입니다."),
    NOT_FOUND_GOAL_TITLE(HttpStatus.FORBIDDEN, "GOAL4002", "목표 제목은 필수입니다."),
    GOAL_CREATION_LIMIT_EXCEEDED(HttpStatus.FORBIDDEN, "GOAL4003", "목표 생성 제한을 초과했습니다"),
    INVALID_GOAL_END_DATE(HttpStatus.FORBIDDEN, "GOAL4004", "종료일이 생성일보다 빠릅니다."),
    DUPLICATE_GOAL_NAME(HttpStatus.FORBIDDEN, "GOAL4005", "중복되는 목표 입니다."),
    ALREADY_REACTED(HttpStatus.FORBIDDEN,"GOAL4006", "이미 응원하였습니다."),
    REACTION_ADD_FAILED(HttpStatus.FORBIDDEN,"GOAL4007", "응원 등록 중 오류가 발생했습니다."),

    // 랜덤 닉네임 관련 에러
    NICKNAME_DATA_NOT_FOUND(HttpStatus.INTERNAL_SERVER_ERROR, "NICKNAME5001", "닉네임 생성에 필요한 데이터가 없습니다.");



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
