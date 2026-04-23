package com.sparta.delivhub.common.util;

import com.sparta.delivhub.common.dto.BusinessException;
import com.sparta.delivhub.common.dto.ErrorCode;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

public class PageableUtils {
    private PageableUtils() {
        throw new IllegalStateException("Utility class");
    }

    private static final List<Integer> ALLOWED_SIZES = List.of(10, 30, 50);

    public static Pageable of(int page, int size, String sort) {
        if (!ALLOWED_SIZES.contains(size)) {
            throw new BusinessException(ErrorCode.INVALID_PAGE_SIZE);
        }
        String[] sortParams = sort.split(",");
        Sort.Direction direction = sortParams.length > 1
                ? Sort.Direction.fromString(sortParams[1])
                : Sort.Direction.DESC;
        return PageRequest.of(page, size, Sort.by(direction, sortParams[0]));
    }
}
