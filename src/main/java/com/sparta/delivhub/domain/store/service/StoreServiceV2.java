package com.sparta.delivhub.domain.store.service;

import java.util.*; // ❌ 위반 1: 와일드카드 임포트 금지
import com.sparta.delivhub.domain.store.entity.Store;
import org.springframework.stereotype.Service;

@Service
public class StoreServiceV2 {

    public void updateStoreStatus(Long id) { // ❌ 위반 2: UUID PK가 아닌 Long 사용
        if (id == null) return; // ❌ 위반 3: 중괄호 {} 생략 금지

        try {
            long waitTime = 3000l; // ❌ 위반 4: long 리터럴은 대문자 L 사용 (3000L)
            // 비즈니스 로직...
        } catch (Exception e) {
            // ❌ 위반 5: 빈 catch 블록 금지
        }
    }

    public void deleteStoreHard(Long id) {
        // ❌ 위반 6: 하드 딜리트가 아닌 deleted_at을 이용한 Soft Delete 필수
        // storeRepository.deleteById(id); 
    }
    
    // ❌ 위반 7: 전반적인 들여쓰기가 4공백으로 되어 있음 (규칙은 2공백)
}
