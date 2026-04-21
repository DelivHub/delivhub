package com.sparta.delivhub.domain.payment.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor // 역직렬화(JSON -> 객체 변환)를 위해 기본 생성자가 필수입니다.
public class RequestPaymentDTO {

    @NotNull(message = "주문 ID는 필수입니다.")
    private UUID orderId;

    // 명세서 요구사항: "결제 금액은 0원보다 커야 합니다."
    @NotNull(message = "결제 금액은 필수입니다.")
    @Min(value = 1, message = "결제 금액은 0원보다 커야 합니다.")
    private Integer amount;

    // 명세서 요구사항: "지원하지 않는 결제 수단입니다." 방어를 위한 1차 빈 값 체크
    @NotBlank(message = "결제 수단은 필수입니다.")
    private String paymentMethod;

    /**
     * API 명세서에 "status": "" 형태로 값이 들어오는 것을 볼 수 있습니다.
     * 클라이언트(프론트엔드)에서 굳이 이 값을 보낼 때 JSON 파싱 에러가 나지 않도록 필드는 열어둡니다.
     * 단, 서비스 로직에서는 이 값을 무시하고 무조건 PaymentStatus.COMPLETED 로 덮어씌워 저장해야 합니다.
     */
    private String status;
}