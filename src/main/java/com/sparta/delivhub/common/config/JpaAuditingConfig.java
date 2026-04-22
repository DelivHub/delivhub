package com.sparta.delivhub.common.config;

import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;

//@Configuration
//@EnableJpaAuditing
//public class JpaAuditingConfig implements AuditorAware<String> {
//
////    @Override
////    public Optional<String> getCurrentAuditor() {
////        Authentication authentication = SecurityContextHolder
////                .getContext()
////                .getAuthentication();
////
////        if (authentication == null || !authentication.isAuthenticated()) {
////            return Optional.empty();
////        }
////
////        UserCustomDetails userDetails = (UserCustomDetails) authentication.getPrincipal();
////        return Optional.of(userDetails.getUsername());
////    }
//}