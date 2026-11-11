package com.back.domain.wiseSaying.controller;

import com.back.AppConfig;
import com.back.AppTestRunner;
import com.back.standard.util.Util;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

public class WiseSayingControllerTest {
    @BeforeEach
    void beforeEach() {
        AppTestRunner.resetStorage();
    }
    @Test
    @DisplayName("등록")
    void t1() {
        String rs = AppTestRunner.run("""
                등록
                현재를 사랑하라.
                작자미상
                """);

        assertThat(rs)
                .contains("명언 : ")
                .contains("작가 : ");
    }

    @Test
    @DisplayName("등록시 생성된 명언번호 노출")
    void t2() {
        String rs = AppTestRunner.run("""
                등록
                현재를 사랑하라.
                작자미상
                """);

        assertThat(rs)
                .contains("1번 명언이 등록되었습니다.");
    }

    @Test
    @DisplayName("등록할때 마다 생성되는 명언번호가 증가")
    void t3() {
        String rs = AppTestRunner.run("""
                등록
                현재를 사랑하라.
                작자미상
                등록
                현재를 사랑하라.
                작자미상
                """);

        assertThat(rs)
                .contains("1번 명언이 등록되었습니다.")
                .contains("2번 명언이 등록되었습니다.");
    }

    @Test
    @DisplayName("목록")
    void t4() {
        String rs = AppTestRunner.run("""
                등록
                현재를 사랑하라.
                작자미상
                등록
                과거에 집착하지 마라.
                작자미상
                목록
                """);

        assertThat(rs)
                .contains("번호 / 작가 / 명언")
                .contains("----------------------")
                .contains("2 / 작자미상 / 과거에 집착하지 마라.")
                .contains("1 / 작자미상 / 현재를 사랑하라.");
    }

    @Test
    @DisplayName("삭제?id=1")
    void t5() {
        String rs = AppTestRunner.run("""
                등록
                현재를 사랑하라.
                작자미상
                등록
                과거에 집착하지 마라.
                작자미상
                삭제?id=1
                목록
                """);

        assertThat(rs)
                .contains("1번 명언이 삭제되었습니다.")
                .contains("2 / 작자미상 / 과거에 집착하지 마라.")
                .doesNotContain("1 / 작자미상 / 현재를 사랑하라.");
    }

    @Test
    @DisplayName("`삭제?id=1`를 2번 시도했을 때의 예외처리")
    void t6() {
        String rs = AppTestRunner.run("""
                등록
                현재를 사랑하라.
                작자미상
                삭제?id=1
                삭제?id=1
                """);

        assertThat(rs)
                .contains("1번 명언이 삭제되었습니다.")
                .contains("1번 명언은 존재하지 않습니다.");
    }

    @Test
    @DisplayName("수정?id=1")
    void t7() {
        String rs = AppTestRunner.run("""
                등록
                현재를 사랑하라.
                작자미상
                수정?id=3
                수정?id=1
                현재를 사랑하세요.
                아리스토텔레스
                목록
                """);

        assertThat(rs)
                .contains("3번 명언은 존재하지 않습니다.")
                .contains("명언(기존) : 현재를 사랑하라.")
                .contains("작가(기존) : 작자미상")
                .contains("1 / 아리스토텔레스 / 현재를 사랑하세요.")
                .doesNotContain("1 / 작자미상 / 현재를 사랑하라.");
    }

    @Test
    @DisplayName("목록?keywordType=content&keyword=과거")
    void t8() {
        String rs = AppTestRunner.run("""
                등록
                현재를 사랑하라.
                작자미상
                등록
                과거에 집착하지 마라.
                작자미상
                목록?keywordType=content&keyword=과거
                """);

        assertThat(rs)
                .contains("2 / 작자미상 / 과거에 집착하지 마라.")
                .doesNotContain("1 / 작자미상 / 현재를 사랑하라.");
    }

    @Test
    @DisplayName("목록?keywordType=author&keyword=작자")
    void t9() {
        String rs = AppTestRunner.run("""
                등록
                현재를 사랑하라.
                작자미상
                등록
                과거에 집착하지 마라.
                작자미상
                목록?keywordType=author&keyword=작자
                """);

        assertThat(rs)
                .contains("2 / 작자미상 / 과거에 집착하지 마라.")
                .contains("1 / 작자미상 / 현재를 사랑하라.");
    }

    @Test
    @DisplayName("목록?keyword=이순신")
    void t10() {
        String rs = AppTestRunner.run("""
                등록
                나의 죽음을 적들에게 알리지 말라.
                이순신
                등록
                진정한 해전의 왕은 이순신 뿐이다.
                넬슨제독
                목록?keyword=이순신
                """);

        assertThat(rs)
                .contains("1 / 이순신 / 나의 죽음을 적들에게 알리지 말라.")
                .contains("2 / 넬슨제독 / 진정한 해전의 왕은 이순신 뿐이다.");
    }

    private String makeSampleDataCommand() {
        return IntStream
                .rangeClosed(1, 10)
                .mapToObj(id -> "등록\n명언 %d\n작자미상 %d".formatted(id, id))
                .collect(Collectors.joining("\n"));
    }

    @Test
    @DisplayName("목록 : 한 페이지에 최대 5개만 노출된다.")
    void t11() {
        String rs = AppTestRunner.run(
                makeSampleDataCommand() + "\n목록"
        );

        assertThat(rs)
                .contains("10 / 작자미상 10 / 명언 10")
                .contains("9 / 작자미상 9 / 명언 9")
                .contains("8 / 작자미상 8 / 명언 8")
                .contains("7 / 작자미상 7 / 명언 7")
                .contains("6 / 작자미상 6 / 명언 6")
                .doesNotContain("5 / 작자미상 5 / 명언 5")
                .doesNotContain("4 / 작자미상 4 / 명언 4")
                .doesNotContain("3 / 작자미상 3 / 명언 3")
                .doesNotContain("2 / 작자미상 2 / 명언 2")
                .doesNotContain("1 / 작자미상 1 / 명언 1")
                .contains("페이지 : [1] / 2");
    }

    @Test
    @DisplayName("목록?page=2")
    void t12() {
        String rs = AppTestRunner.run(
                makeSampleDataCommand() + "\n목록?page=2"
        );

        assertThat(rs)
                .doesNotContain("10 / 작자미상 10 / 명언 10")
                .doesNotContain("9 / 작자미상 9 / 명언 9")
                .doesNotContain("8 / 작자미상 8 / 명언 8")
                .doesNotContain("7 / 작자미상 7 / 명언 7")
                .doesNotContain("6 / 작자미상 6 / 명언 6")
                .contains("5 / 작자미상 5 / 명언 5")
                .contains("4 / 작자미상 4 / 명언 4")
                .contains("3 / 작자미상 3 / 명언 3")
                .contains("2 / 작자미상 2 / 명언 2")
                .contains("1 / 작자미상 1 / 명언 1")
                .contains("페이지 : 1 / [2]");
    }

    @Test
    @DisplayName("목록?keyword=명언")
    void t13() {
        String rs = AppTestRunner.run(
                makeSampleDataCommand() + "\n목록?keyword=명언"
        );

        assertThat(rs)
                .contains("10 / 작자미상 10 / 명언 10")
                .contains("9 / 작자미상 9 / 명언 9")
                .contains("8 / 작자미상 8 / 명언 8")
                .contains("7 / 작자미상 7 / 명언 7")
                .contains("6 / 작자미상 6 / 명언 6")
                .doesNotContain("5 / 작자미상 5 / 명언 5")
                .doesNotContain("4 / 작자미상 4 / 명언 4")
                .doesNotContain("3 / 작자미상 3 / 명언 3")
                .doesNotContain("2 / 작자미상 2 / 명언 2")
                .doesNotContain("1 / 작자미상 1 / 명언 1")
                .contains("페이지 : [1] / 2");
    }

    @Test
    @DisplayName("목록?keyword=명언&page=2")
    void t14() {
        String rs = AppTestRunner.run(
                makeSampleDataCommand() + "\n목록?keyword=명언&page=2"
        );

        assertThat(rs)
                .doesNotContain("10 / 작자미상 10 / 명언 10")
                .doesNotContain("9 / 작자미상 9 / 명언 9")
                .doesNotContain("8 / 작자미상 8 / 명언 8")
                .doesNotContain("7 / 작자미상 7 / 명언 7")
                .doesNotContain("6 / 작자미상 6 / 명언 6")
                .contains("5 / 작자미상 5 / 명언 5")
                .contains("4 / 작자미상 4 / 명언 4")
                .contains("3 / 작자미상 3 / 명언 3")
                .contains("2 / 작자미상 2 / 명언 2")
                .contains("1 / 작자미상 1 / 명언 1")
                .contains("페이지 : 1 / [2]");
    }

    @Test
    @DisplayName("목록?keyword=명언 1")
    void t15() {
        String rs = AppTestRunner.run(
                makeSampleDataCommand() + "\n목록?keyword=명언 1"
        );

        assertThat(rs)
                .contains("10 / 작자미상 10 / 명언 10")
                .doesNotContain("9 / 작자미상 9 / 명언 9")
                .doesNotContain("8 / 작자미상 8 / 명언 8")
                .doesNotContain("7 / 작자미상 7 / 명언 7")
                .doesNotContain("6 / 작자미상 6 / 명언 6")
                .doesNotContain("5 / 작자미상 5 / 명언 5")
                .doesNotContain("4 / 작자미상 4 / 명언 4")
                .doesNotContain("3 / 작자미상 3 / 명언 3")
                .doesNotContain("2 / 작자미상 2 / 명언 2")
                .contains("1 / 작자미상 1 / 명언 1")
                .contains("페이지 : [1]");
    }

    @Test
    @DisplayName("등록 후 재시작해도 목록에 남아 있다.")
    void t16() {
        AppTestRunner.run("""
                등록
                현재를 사랑하라.
                작자미상
                등록
                과거에 집착하지 마라.
                작자미상
                """);

        String rs = AppTestRunner.run("목록");

        assertThat(rs)
                .contains("2 / 작자미상 / 과거에 집착하지 마라.")
                .contains("1 / 작자미상 / 현재를 사랑하라.");
    }

    @Test
    @DisplayName("재시작 후 등록하면 마지막 번호 다음 번호를 쓴다.")
    void t17() {
        AppTestRunner.run("""
                등록
                현재를 사랑하라.
                작자미상
                """);

        String rs = AppTestRunner.run("""
                등록
                과거에 집착하지 마라.
                작자미상
                """);

        assertThat(rs).contains("2번 명언이 등록되었습니다.");
    }

    @Test
    @DisplayName("삭제한 번호는 재시작 후에도 재사용하지 않는다.")
    void t18() {
        AppTestRunner.run("""
                등록
                현재를 사랑하라.
                작자미상
                등록
                과거에 집착하지 마라.
                작자미상
                삭제?id=2
                """);

        String rs = AppTestRunner.run("""
                등록
                미래를 꿈꿔라.
                작자미상
                목록
                """);

        assertThat(rs)
                .contains("3번 명언이 등록되었습니다.")
                .contains("3 / 작자미상 / 미래를 꿈꿔라.")
                .contains("1 / 작자미상 / 현재를 사랑하라.")
                .doesNotContain("과거에 집착하지 마라.");
    }

    @Test
    @DisplayName("수정 내용이 재시작 후에도 유지된다.")
    void t19() {
        AppTestRunner.run("""
                등록
                현재를 사랑하라.
                작자미상
                수정?id=1
                현재를 사랑하세요.
                아리스토텔레스
                """);

        String rs = AppTestRunner.run("목록");

        assertThat(rs)
                .contains("1 / 아리스토텔레스 / 현재를 사랑하세요.")
                .doesNotContain("1 / 작자미상 / 현재를 사랑하라.");
    }

    @Test
    @DisplayName("빌드")
    void t20() {
        String rs = AppTestRunner.run("""
                등록
                명언 1
                작가 1
                등록
                명언 2
                작가 2
                빌드
                """);

        assertThat(rs).contains("data.json 파일의 내용이 갱신되었습니다.");

        assertThat(Util.file.get(AppConfig.getExportFilePath(), "")).isEqualTo("""
                [
                    {
                        "id": 1,
                        "content": "명언 1",
                        "author": "작가 1"
                    },
                    {
                        "id": 2,
                        "content": "명언 2",
                        "author": "작가 2"
                    }
                ]
                """.stripIndent().trim());
    }

    @Test
    @DisplayName("빌드 : 테스트 모드에서는 db_test/data.json 에 Export 한다.")
    void t21() {
        AppTestRunner.run("""
                등록
                현재를 사랑하라.
                작자미상
                빌드
                """);

        assertThat(Util.file.get("db_test/data.json", "")).isEqualTo("""
                [
                    {
                        "id": 1,
                        "content": "현재를 사랑하라.",
                        "author": "작자미상"
                    }
                ]
                """.stripIndent().trim());
    }

    @Test
    @DisplayName("빌드 : 명언이 없으면 빈 배열을 Export 한다.")
    void t22() {
        String rs = AppTestRunner.run("빌드");

        assertThat(rs).contains("data.json 파일의 내용이 갱신되었습니다.");
        assertThat(Util.file.get(AppConfig.getExportFilePath(), "").replaceAll("\\s", "")).isEqualTo("[]");
    }

    @Test
    @DisplayName("따옴표·역슬래시가 들어간 명언 내용이 재시작 후에도 그대로 나온다.")
    void t23() {
        AppTestRunner.run("""
                등록
                그는 "안녕"이라 말했다. C:\\temp\\new 'ok'
                작자"미상
                """);

        String rs = AppTestRunner.run("목록");

        assertThat(rs).contains("1 / 작자\"미상 / 그는 \"안녕\"이라 말했다. C:\\temp\\new 'ok'");
    }
}
