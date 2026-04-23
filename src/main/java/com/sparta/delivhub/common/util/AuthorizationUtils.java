package com.sparta.delivhub.common.util;

import com.sparta.delivhub.common.dto.BusinessException;
import com.sparta.delivhub.common.dto.ErrorCode;
import com.sparta.delivhub.domain.user.entity.User;
import com.sparta.delivhub.domain.user.entity.UserRole;

public class AuthorizationUtils {
    // OWNER/MANAGER/MASTER 권한 체크
    public static void checkOwnerOrAdminPermission(User user, String ownerUsername) {
        if (user.getUserRole() == UserRole.CUSTOMER) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
        if (user.getUserRole() == UserRole.OWNER
                && !ownerUsername.equals(user.getUsername())) {
            throw new BusinessException(ErrorCode.NOT_STORE_OWNER);
        }
    }

    // MANAGER/MASTER 권한 체크
    public static void checkAdminPermission(User user) {
        if (user.getUserRole() != UserRole.MANAGER && user.getUserRole() != UserRole.MASTER) {
            throw new BusinessException(ErrorCode.AI_LOG_FORBIDDEN);
        }
    }
}
