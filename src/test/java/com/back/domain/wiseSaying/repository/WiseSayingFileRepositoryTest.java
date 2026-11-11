package com.back.domain.wiseSaying.repository;

import com.back.AppConfig;
import com.back.domain.wiseSaying.entity.WiseSaying;
import com.back.standard.dto.Page;
import com.back.standard.dto.Pageable;
import com.back.standard.util.Util;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class WiseSayingFileRepositoryTest {
    private WiseSayingFileRepository wiseSayingFileRepository;

    @BeforeEach
    void beforeEach() {
        // 파일 리포지터리 자체의 테스트이므로 저장 폴더를 직접 비운다.
        AppConfig.setTestMode();
        Util.file.rmdir(AppConfig.getDbDirPath());
        wiseSayingFileRepository = new WiseSayingFileRepository();
    }

    @Test
    @DisplayName("저장 후 번호로 조회")
    void t1() {
        WiseSaying wiseSaying = new WiseSaying("꿈을 지녀라. 그러면 어려운 현실을 이길 수 있다.", "괴테");
        wiseSayingFileRepository.save(wiseSaying);

        assertThat(wiseSaying.getId()).isEqualTo(1);
        assertThat(wiseSayingFileRepository.findById(1)).isEqualTo(wiseSaying);
    }

    @Test
    @DisplayName("저장할 때마다 번호가 증가하고 lastId 가 갱신된다.")
    void t2() {
        wiseSayingFileRepository.save(new WiseSaying("명언 1", "작가 1"));
        wiseSayingFileRepository.save(new WiseSaying("명언 2", "작가 2"));

        assertThat(wiseSayingFileRepository.getLastId()).isEqualTo(2);
        assertThat(new WiseSayingFileRepository().getLastId()).isEqualTo(2);
    }

    @Test
    @DisplayName("삭제")
    void t3() {
        WiseSaying wiseSaying = wiseSayingFileRepository.save(new WiseSaying("명언 1", "작가 1"));

        wiseSayingFileRepository.delete(wiseSaying);

        assertThat(wiseSayingFileRepository.findById(1)).isNull();
    }

    @Test
    @DisplayName("전체 조회는 번호 내림차순이고, lastId.txt·data.json 은 포함하지 않는다.")
    void t4() {
        wiseSayingFileRepository.save(new WiseSaying("명언 1", "작가 1"));
        wiseSayingFileRepository.save(new WiseSaying("명언 2", "작가 2"));
        wiseSayingFileRepository.build();

        assertThat(wiseSayingFileRepository.findAll())
                .extracting(WiseSaying::getId)
                .containsExactly(2, 1);
    }

    @Test
    @DisplayName("따옴표·역슬래시가 들어간 명언도 그대로 저장·조회된다.")
    void t5() {
        WiseSaying wiseSaying = new WiseSaying("그는 \"안녕\"이라 말했다. C:\\temp", "작자\"미상");
        wiseSayingFileRepository.save(wiseSaying);

        assertThat(new WiseSayingFileRepository().findById(1)).isEqualTo(wiseSaying);
    }

    @Test
    @DisplayName("페이징")
    void t6() {
        for (int i = 1; i <= 7; i++) {
            wiseSayingFileRepository.save(new WiseSaying("명언 " + i, "작가 " + i));
        }

        Page<WiseSaying> page = wiseSayingFileRepository.findForList(new Pageable(2, 5));

        assertThat(page.getTotalCount()).isEqualTo(7);
        assertThat(page.getContent())
                .extracting(WiseSaying::getId)
                .containsExactly(2, 1);
    }

    @Test
    @DisplayName("저장하면 WiseSaying 하나당 {번호}.json 파일이 생기고 lastId.txt 가 갱신된다.")
    void t7() {
        wiseSayingFileRepository.save(new WiseSaying("현재를 사랑하라.", "작자미상"));

        String dir = AppConfig.getDbDirPath() + "/wiseSaying";

        assertThat(Util.file.get(dir + "/1.json", "")).isEqualTo("""
                {
                    "id": 1,
                    "content": "현재를 사랑하라.",
                    "author": "작자미상"
                }
                """.stripIndent().trim());
        assertThat(Util.file.get(dir + "/lastId.txt", "")).isEqualTo("1");
    }

    @Test
    @DisplayName("삭제하면 {번호}.json 파일은 지워지고 lastId.txt 는 값을 유지한다.")
    void t8() {
        WiseSaying wiseSaying = wiseSayingFileRepository.save(new WiseSaying("현재를 사랑하라.", "작자미상"));

        wiseSayingFileRepository.delete(wiseSaying);

        String dir = AppConfig.getDbDirPath() + "/wiseSaying";

        assertThat(Util.file.notExists(dir + "/1.json")).isTrue();
        assertThat(Util.file.get(dir + "/lastId.txt", "")).isEqualTo("1");
    }
}
