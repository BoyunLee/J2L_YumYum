package com.ssafy.yumyum.domain.refrigerator.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

public record ManualInventoryCreateBatchRequest(
        @NotEmpty(message = "등록할 상품을 한 개 이상 입력해 주세요.")
        List<@Valid ManualInventoryCreateRequest> items
) {
}
