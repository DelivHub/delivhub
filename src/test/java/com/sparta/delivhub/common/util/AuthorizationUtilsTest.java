package com.sparta.delivhub.common.util;

import com.sparta.delivhub.common.dto.BusinessException;
import com.sparta.delivhub.common.dto.ErrorCode;
import com.sparta.delivhub.domain.user.entity.User;
import com.sparta.delivhub.domain.user.entity.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthorizationUtilsTest {

    @Test
    @DisplayName("유틸리티 클래스 생성자 호출 시 예외 발생")
    void constructor_Test() throws NoSuchMethodException {
        Constructor<AuthorizationUtils> constructor = AuthorizationUtils.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        assertThatThrownBy(constructor::newInstance)
                .isInstanceOf(InvocationTargetException.class)
                .hasCauseInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("OWNER/ADMIN 권한 체크 - CUSTOMER는 거부됨")
    void checkOwnerOrAdminPermission_Customer_Fail() {
        User user = mock(User.class);
        when(user.getUserRole()).thenReturn(UserRole.CUSTOMER);

        assertThatThrownBy(() -> AuthorizationUtils.checkOwnerOrAdminPermission(user, "owner"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.ACCESS_DENIED.getMessage());
    }

    @Test
    @DisplayName("OWNER/ADMIN 권한 체크 - 타인 소유 OWNER는 거부됨")
    void checkOwnerOrAdminPermission_OtherOwner_Fail() {
        User user = mock(User.class);
        when(user.getUserRole()).thenReturn(UserRole.OWNER);
        when(user.getUsername()).thenReturn("other");

        assertThatThrownBy(() -> AuthorizationUtils.checkOwnerOrAdminPermission(user, "owner"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.NOT_STORE_OWNER.getMessage());
    }

    @Test
    @DisplayName("OWNER/ADMIN 권한 체크 - 본인 소유 OWNER는 허용")
    void checkOwnerOrAdminPermission_MyOwner_Success() {
        User user = mock(User.class);
        when(user.getUserRole()).thenReturn(UserRole.OWNER);
        when(user.getUsername()).thenReturn("owner");

        AuthorizationUtils.checkOwnerOrAdminPermission(user, "owner");
    }

    @Test
    @DisplayName("OWNER/ADMIN 권한 체크 - MANAGER/MASTER는 허용")
    void checkOwnerOrAdminPermission_Admin_Success() {
        User manager = mock(User.class);
        when(manager.getUserRole()).thenReturn(UserRole.MANAGER);
        AuthorizationUtils.checkOwnerOrAdminPermission(manager, "any");

        User master = mock(User.class);
        when(master.getUserRole()).thenReturn(UserRole.MASTER);
        AuthorizationUtils.checkOwnerOrAdminPermission(master, "any");
    }

    @Test
    @DisplayName("ADMIN 권한 체크 - MANAGER/MASTER 성공")
    void checkAdminPermission_Success() {
        User manager = mock(User.class);
        when(manager.getUserRole()).thenReturn(UserRole.MANAGER);
        AuthorizationUtils.checkAdminPermission(manager);

        User master = mock(User.class);
        when(master.getUserRole()).thenReturn(UserRole.MASTER);
        AuthorizationUtils.checkAdminPermission(master);
    }

    @Test
    @DisplayName("ADMIN 권한 체크 - 일반 유저는 실패")
    void checkAdminPermission_Fail() {
        User customer = mock(User.class);
        when(customer.getUserRole()).thenReturn(UserRole.CUSTOMER);
        assertThatThrownBy(() -> AuthorizationUtils.checkAdminPermission(customer))
                .isInstanceOf(BusinessException.class);

        User owner = mock(User.class);
        when(owner.getUserRole()).thenReturn(UserRole.OWNER);
        assertThatThrownBy(() -> AuthorizationUtils.checkAdminPermission(owner))
                .isInstanceOf(BusinessException.class);
    }
}
