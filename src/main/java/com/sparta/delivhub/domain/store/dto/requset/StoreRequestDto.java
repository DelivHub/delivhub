package com.sparta.delivhub.domain.store.dto.requset;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;



import java.util.UUID;

@NoArgsConstructor
@Getter
public class StoreRequestDto {

    @NotBlank(message = "가게 이름은 필수입니다.")
    private String name;

    @NotNull(message = "카테고리 ID는 필수입니다.")
    private UUID categoryId;

    @NotNull(message = "지역 ID는 필수입니다.")
    private UUID areaId;

    @NotBlank(message = "주소는 필수입니다.")
    private String address;

    @NotBlank(message = "전화번호는 필수입니다.")
    private String number;

    @NotNull(message = "공개 여부는 필수입니다.")
    private Boolean isHidden = false;
    public StoreRequestDto(String name, UUID categoryId, UUID areaId, String address, String number) {
        this.name = name;
        this.categoryId = categoryId;
        this.areaId = areaId;
        this.address = address;
        this.number = number;
    }
}
