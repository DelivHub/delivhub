package com.sparta.delivhub.common.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    
    // 1. Common (공통 검증 및 서버 에러)
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "C001", "잘못된 요청입니다."),
    INVALID_INPUT_DATA(HttpStatus.BAD_REQUEST, "C002", "입력 데이터가 올바르지 않습니다."),
    REQUIRED_FIELD_MISSING(HttpStatus.BAD_REQUEST, "C003", "필수 입력값이 누락되었습니다."),
    NO_CHANGES_DETECTED(HttpStatus.BAD_REQUEST, "C004", "변경할 내용이 없습니다."),
    INVALID_PAGE_SIZE(HttpStatus.BAD_REQUEST, "C005", "페이징 size는 10, 30, 50만 허용됩니다."),
    SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C500", "서버 내부 오류가 발생했습니다."),

    // 2. User (회원 및 검증 로직)
    DUPLICATE_USERNAME(HttpStatus.BAD_REQUEST, "U001", "이미 사용 중인 유저네임입니다."),
    INVALID_EMAIL_FORMAT(HttpStatus.BAD_REQUEST, "U002", "이메일 형식이 올바르지 않습니다."),
    INVALID_PASSWORD_FORMAT(HttpStatus.BAD_REQUEST, "U003", "비밀번호 형식이 올바르지 않습니다."),
    PASSWORD_SAME_AS_CURRENT(HttpStatus.BAD_REQUEST, "U004", "새 비밀번호가 현재 비밀번호와 동일합니다."),
    DUPLICATE_EMAIL(HttpStatus.BAD_REQUEST, "U005", "이미 사용 중인 이메일입니다."),
    DUPLICATE_NICKNAME(HttpStatus.BAD_REQUEST, "U006", "이미 사용 중인 닉네임입니다."),
    INVALID_ROLE(HttpStatus.BAD_REQUEST, "U007", "유효하지 않은 권한(Role) 값입니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U008", "사용자를 찾을 수 없습니다."),

    // 3. Auth & Permission (인증 및 인가)
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "A001", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "A002", "만료된 토큰입니다."),
    WRONG_CURRENT_PASSWORD(HttpStatus.UNAUTHORIZED, "A003", "현재 비밀번호가 일치하지 않습니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "A004", "해당 작업을 수행할 권한이 없습니다."),
    MASTER_ONLY(HttpStatus.FORBIDDEN, "A005", "MASTER 권한만 접근할 수 있습니다."),
    CANNOT_CHANGE_OWN_ROLE(HttpStatus.FORBIDDEN, "A006", "자신의 권한은 스스로 변경할 수 없습니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "A007", "아이디 또는 비밀번호가 일치하지 않습니다."),
    DEACTIVATED_ACCOUNT(HttpStatus.FORBIDDEN, "A008", "탈퇴한 계정입니다."),

    // 4. Address (주소 로직)
    INVALID_ADDRESSID(HttpStatus.BAD_REQUEST, "AD001", "유효하지 않은 주소 ID입니다."),
    ADDRESS_NOT_FOUND(HttpStatus.NOT_FOUND, "AD002", "해당 주소를 찾을 수 없습니다."),
    ALREADY_DELETED_ADDRESS(HttpStatus.NOT_FOUND, "AD003", "이미 삭제된 주소입니다."),

    // 5. Store (가게 로직)
    INVALID_STORE_ID(HttpStatus.BAD_REQUEST, "S001", "유효하지 않은 가게 ID(UUID) 형식입니다."),
    INVALID_PARAMETER(HttpStatus.BAD_REQUEST, "S002", "페이징 값 또는 파라미터가 올바르지 않습니다."),
    ALREADY_HIDDEN(HttpStatus.BAD_REQUEST, "S003", "이미 숨김 처리된 가게입니다."),
    NOT_STORE_OWNER(HttpStatus.FORBIDDEN, "S004", "본인의 가게만 수정 또는 관리할 수 있습니다."),
    FORBIDDEN_STORE_ACTION(HttpStatus.FORBIDDEN, "S005", "해당 가게에 대한 관리 권한이 없습니다."),
    STORE_NOT_FOUND(HttpStatus.NOT_FOUND, "S006", "존재하지 않거나 삭제된 가게입니다."),

    // 6. Category (카테고리 로직)
    CATEGORY_INVALID_INPUT(HttpStatus.BAD_REQUEST, "CT001", "카테고리 이름이 누락되었거나 너무 길 때 발생합니다."),
    CATEGORY_INVALID_PARAMETER(HttpStatus.BAD_REQUEST, "CT002", "페이징 파라미터가 잘못되었을 때 발생합니다."),
    INVALID_CATEGORY_ID(HttpStatus.BAD_REQUEST, "CT003", "UUID 형식이 아닌 잘못된 값이 들어왔을 때 발생합니다."),
    CATEGORY_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "CT004", "로그인 세션이 만료되었거나 인증 정보가 없을 때 발생합니다."),
    CATEGORY_FORBIDDEN_ACCESS(HttpStatus.FORBIDDEN, "CT005", "OWNER나 CUSTOMER 권한으로 생성을 시도하거나, 관리자(MANAGER, MASTER)가 아닌 권한으로 수정을 시도할 때 발생합니다."),
    CATEGORY_FORBIDDEN_DELETE(HttpStatus.FORBIDDEN, "CT006", "MASTER가 아닌데 삭제를 시도할 때 발생합니다."),
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "CT007", "존재하지 않거나 이미 삭제된 categoryId를 조회했거나, 수정하려는 카테고리 ID가 DB에 없을 때 발생합니다."),
    CATEGORY_STORE_NOT_FOUND(HttpStatus.NOT_FOUND, "CT008", "이미 삭제되었거나 존재하지 않는 카테고리일 때 발생합니다."),

    // 7. Area (지역 로직)
    INVALID_AREA_INPUT(HttpStatus.BAD_REQUEST, "AR001", "시/도, 군/구, 상세 주소 중 누락된 값이 있거나 형식이 잘못되었을 때."),
    INVALID_PAGING_PARAMETER(HttpStatus.BAD_REQUEST, "AR002", "page나 size 값이 음수이거나 숫자가 아닐 때."),
    INVALID_AREA_ID(HttpStatus.BAD_REQUEST, "AR003", "areaId가 유효한 UUID 형식이 아닐 때."),
    UNAUTHORIZED_ACCESS(HttpStatus.UNAUTHORIZED, "AR004", "인증되지 않은 사용자가 접근했을 때."),
    FORBIDDEN_AREA_MANAGEMENT_CREATE(HttpStatus.FORBIDDEN, "AR005", "관리자가 아닌 권한(OWNER, CUSTOMER)으로 지역을 생성하려 할 때."),
    FORBIDDEN_AREA_MANAGEMENT_UPDATE(HttpStatus.FORBIDDEN, "AR006", "관리자 권한이 없는 유저가 수정을 시도할 때."),
    FORBIDDEN_AREA_DELETE(HttpStatus.FORBIDDEN, "AR007", "MASTER 권한이 아닌 유저(MANAGER 포함)가 삭제를 시도할 때."),
    AREA_NOT_FOUND_ON_READ(HttpStatus.NOT_FOUND, "AR008", "존재하지 않거나 소프트 삭제된 areaId를 조회했을 때."),
    AREA_NOT_FOUND_ON_UPDATE(HttpStatus.NOT_FOUND, "AR009", "수정하려는 지역 ID가 DB에 없을 때."),
    AREA_NOT_FOUND_ON_DELETE(HttpStatus.NOT_FOUND, "AR010", "이미 삭제되었거나 존재하지 않는 지역을 삭제하려 할 때."),

    // 8. Menu (메뉴 로직)
    MENU_STORE_NOT_FOUND(HttpStatus.NOT_FOUND, "M001", "존재하지 않는 가게입니다."),
    MENU_FORBIDDEN_ON_READ(HttpStatus.FORBIDDEN, "M002", "숨김 또는 삭제된 메뉴에 접근할 권한이 없습니다."),
    MENU_NOT_FOUND_ON_READ(HttpStatus.NOT_FOUND, "M003", "존재하지 않는 메뉴입니다."),
    MENU_FORBIDDEN_ON_UPDATE(HttpStatus.FORBIDDEN, "M004", "본인 가게의 메뉴만 수정할 수 있습니다."),
    MENU_NOT_FOUND_ON_UPDATE(HttpStatus.NOT_FOUND, "M005", "존재하지 않는 메뉴입니다."),
    MENU_VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "M006", "누락된 값이 있습니다."),
    MENU_FORBIDDEN_ON_UPDATE_STATUS(HttpStatus.FORBIDDEN, "M007", "본인 가게의 메뉴만 수정할 수 있습니다."),
    MENU_NOT_FOUND_ON_UPDATE_STATUS(HttpStatus.NOT_FOUND, "M008", "존재하지 않는 메뉴입니다."),
    MENU_FORBIDDEN_ON_DELETE(HttpStatus.FORBIDDEN, "M009", "본인 가게의 메뉴만 삭제할 수 있습니다."),
    MENU_NOT_FOUND_ON_DELETE(HttpStatus.NOT_FOUND, "M010", "존재하지 않는 메뉴입니다."),

    // 9. Option (옵션 로직)
    OPTION_VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "OP001", "누락된 값이 있습니다."),
    OPTION_FORBIDDEN_ON_CREATE(HttpStatus.FORBIDDEN, "OP002", "본인 가게의 메뉴에만 옵션을 등록할 수 있습니다."),
    MENU_NOT_FOUND_ON_OPTION_CREATE(HttpStatus.NOT_FOUND, "OP003", "존재하지 않는 메뉴입니다."),
    MENU_NOT_FOUND_ON_OPTION_READ(HttpStatus.NOT_FOUND, "OP004", "존재하지 않는 메뉴입니다."),
    OPTION_FORBIDDEN_ON_UPDATE(HttpStatus.FORBIDDEN, "OP005", "본인 가게의 메뉴에 속한 옵션만 수정할 수 있습니다."),
    OPTION_NOT_FOUND(HttpStatus.NOT_FOUND, "OP006", "존재하지 않는 옵션입니다."),

    // 10. Order (주문 로직)
    ORDER_PARAM_MISSING(HttpStatus.BAD_REQUEST, "OD001", "필수 파라미터 누락"),
    ORDER_INVALID_QUANTITY(HttpStatus.BAD_REQUEST, "OD002", "quantity가 1 미만입니다."),
    ORDER_RELATION_NOT_FOUND(HttpStatus.NOT_FOUND, "OD003", "존재하지 않는 가게/배송지/메뉴입니다."),

    ORDER_INVALID_SIZE(HttpStatus.BAD_REQUEST, "OD004", "허용되지 않은 size 값입니다."),
    ORDER_READ_FORBIDDEN(HttpStatus.FORBIDDEN, "OD005", "조회 권한이 없습니다."),

    ORDER_ACCESS_DENIED_ON_READ(HttpStatus.FORBIDDEN, "OD006", "해당 주문에 대한 접근 권한이 없습니다."),
    ORDER_NOT_FOUND_ON_READ(HttpStatus.NOT_FOUND, "OD007", "존재하지 않는 주문입니다."),

    ORDER_UNMODIFIABLE(HttpStatus.BAD_REQUEST, "OD008", "접수된 주문은 요청사항을 수정할 수 없습니다."),
    ORDER_UPDATE_FORBIDDEN(HttpStatus.FORBIDDEN, "OD009", "본인 주문만 수정 가능합니다."),
    ORDER_NOT_FOUND_ON_UPDATE(HttpStatus.NOT_FOUND, "OD010", "존재하지 않는 주문입니다."),

    ORDER_STATUS_REVERSE_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "OD011", "역방향으로 상태를 변경할 수 없습니다."),
    ORDER_STATUS_UPDATE_FORBIDDEN(HttpStatus.FORBIDDEN, "OD012", "상태 변경 권한이 없습니다."),

    ORDER_CANCEL_TIMEOUT(HttpStatus.BAD_REQUEST, "OD013", "주문 생성 후 5분이 경과하여 취소할 수 없습니다."),
    ORDER_ALREADY_COOKING(HttpStatus.BAD_REQUEST, "OD014", "이미 조리중인 주문은 취소 불가."),
    ORDER_CANCEL_FORBIDDEN(HttpStatus.FORBIDDEN, "OD015", "권한이 없습니다."),

    ORDER_DELETE_FORBIDDEN(HttpStatus.FORBIDDEN, "OD016", "관리자(MASTER) 권한이 필요합니다."),
    ORDER_NOT_FOUND_ON_DELETE(HttpStatus.NOT_FOUND, "OD017", "주문이 존재하지 않습니다."),

    // 11. Payment (결제 로직)
    PAYMENT_VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "P001", "결제 금액은 0원보다 커야 하거나, 지원하지 않는 결제 수단입니다."),
    PAYMENT_BAD_REQUEST(HttpStatus.BAD_REQUEST, "P002", "이미 삭제 처리된 결제 내역입니다."),

    STORE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "P003", "해당 가게의 관리자(MANAGER) 또는 마스터(MASTER) 권한이 필요합니다."),
    PAYMENT_ACCESS_DENIED(HttpStatus.FORBIDDEN, "P004", "본인이 작성한 결제 내역만 상태를 변경할 수 있습니다."),

    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "P005","해당 주문을 찾을 수 없습니다."),
    PAYMENT_ALREADY_EXISTS(HttpStatus.BAD_REQUEST,"P006", "이미 결제가 완료된 주문입니다."),
    INVALID_PAYMENT_METHOD(HttpStatus.BAD_REQUEST,"P007", "지원하지 않는 결제 수단입니다."),
    CANNOT_PAY_OWN_STORE(HttpStatus.FORBIDDEN, "P008","본인이 운영하는 가게의 주문은 스스로 결제할 수 없습니다."),

    // 가게 식별자 에러 (기존 STORE_NOT_FOUND가 있지만 요청하신 메시지로 새로 생성)
    PAYMENT_STORE_NOT_FOUND(HttpStatus.NOT_FOUND, "P015", "해당 storeId가 존재하지 않거나, 이미 삭제된 가게입니다."),

    // 12. Review (리뷰 로직)
    REVIEW_VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "R001", "별점은 1점 이상 5점 이하로 입력해주세요."),
    REVIEW_BAD_REQUEST_STATUS(HttpStatus.BAD_REQUEST, "R002", "주문 상태가 COMPLETED가 아닌데 리뷰를 쓰려고 합니다. 배달 완료 후 작성해주세요."),
    REVIEW_PAGING_ERROR(HttpStatus.BAD_REQUEST, "R003", "페이지 번호는 0 이상이어야 합니다."),

    REVIEW_SECURITY_EXCEPTION(HttpStatus.FORBIDDEN, "R004", "권한이 없습니다. 로그인 세션이 만료되었는지 확인해주세요."),
    REVIEW_FORBIDDEN_UPDATE(HttpStatus.FORBIDDEN, "R005", "해당 리뷰를 수정할 권한이 없습니다."),
    REVIEW_FORBIDDEN_DELETE(HttpStatus.FORBIDDEN, "R006", "해당 리뷰를 수정할 권한이 없습니다."), // 요청하신 메시지 그대로 유지

    REVIEW_STORE_NOT_FOUND(HttpStatus.NOT_FOUND, "R007", "해당 가게 정보를 찾을 수 없습니다."),
    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "R008", "존재하지 않거나 이미 삭제 처리가 완료된 리뷰입니다."),

    REVIEW_ALREADY_EXISTS(HttpStatus.CONFLICT, "R009", "이미 해당 주문에 대해 작성된 리뷰가 존재합니다."),

    // 13. AI Log (AI 시스템 로직)
    AI_LOG_FORBIDDEN(HttpStatus.FORBIDDEN, "AI001", "AI 로그 접근은 관리자(MANAGER, MASTER)만 가능합니다."),
    AI_LOG_NOT_FOUND(HttpStatus.NOT_FOUND, "AI002", "존재하지 않는 AI 로그입니다."),
    AI_API_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "AI003", "AI 응답을 받지 못했습니다."),
    AI_PROMPT_REQUIRED(HttpStatus.BAD_REQUEST, "AI004", "AI 설명 생성을 위해 프롬프트를 입력해야 합니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
