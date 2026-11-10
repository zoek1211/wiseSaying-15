package com.back.domain.wiseSaying.repository;

import com.back.domain.wiseSaying.entity.WiseSaying;

import java.util.ArrayList;
import java.util.List;

// 8단계까지 쓰던 메모리 구현체. 파일 구현체로 갈아 끼운 뒤에도 비교용으로 남겨 둔다.
public class WiseSayingMemoryRepository implements WiseSayingRepository {
    private final List<WiseSaying> wiseSayings = new ArrayList<>();
    private int lastId = 0;

    @Override
    public WiseSaying save(WiseSaying wiseSaying) {
        if (wiseSaying.isNew()) {
            wiseSaying.setId(++lastId);
            wiseSayings.add(wiseSaying);
        }

        return wiseSaying;
    }

    @Override
    public WiseSaying findById(int id) {
        return wiseSayings
                .stream()
                .filter(w -> w.getId() == id)
                .findFirst()
                .orElse(null);
    }

    @Override
    public void delete(WiseSaying wiseSaying) {
        wiseSayings.remove(wiseSaying);
    }

    @Override
    public List<WiseSaying> findAll() {
        return wiseSayings.reversed();
    }

    @Override
    public String build() {
        throw new UnsupportedOperationException("메모리 리포지터리는 빌드를 지원하지 않습니다.");
    }
}
