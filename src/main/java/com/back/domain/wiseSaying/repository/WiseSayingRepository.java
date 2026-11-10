package com.back.domain.wiseSaying.repository;

import com.back.domain.wiseSaying.entity.WiseSaying;
import com.back.standard.dto.Page;
import com.back.standard.dto.Pageable;

import java.util.List;
import java.util.function.Predicate;

public interface WiseSayingRepository {
    WiseSaying save(WiseSaying wiseSaying);

    WiseSaying findById(int id);

    void delete(WiseSaying wiseSaying);

    // 번호 내림차순(최신순)
    List<WiseSaying> findAll();

    // 모든 명언을 하나의 파일로 내보낸다. 내보낸 파일 경로를 반환한다.
    String build();

    default Page<WiseSaying> findForList(Pageable pageable) {
        return findForListBy(w -> true, pageable);
    }

    default Page<WiseSaying> findForListByContentContaining(String keyword, Pageable pageable) {
        return findForListBy(w -> w.getContent().contains(keyword), pageable);
    }

    default Page<WiseSaying> findForListByAuthorContaining(String keyword, Pageable pageable) {
        return findForListBy(w -> w.getAuthor().contains(keyword), pageable);
    }

    default Page<WiseSaying> findForListByContentContainingOrAuthorContaining(String keyword1, String keyword2, Pageable pageable) {
        return findForListBy(w -> w.getContent().contains(keyword1) || w.getAuthor().contains(keyword2), pageable);
    }

    private Page<WiseSaying> findForListBy(Predicate<WiseSaying> predicate, Pageable pageable) {
        List<WiseSaying> filtered = findAll()
                .stream()
                .filter(predicate)
                .toList();

        List<WiseSaying> content = filtered
                .stream()
                .skip(pageable.getSkipCount())
                .limit(pageable.getPageSize())
                .toList();

        return new Page<>(filtered.size(), pageable.getPageNo(), pageable.getPageSize(), content);
    }
}
