# 명언 앱 ⑦ — 파일 영속성 · 빌드 (9~10단계)

명언 앱 ⑥(`wiseSaying-tdd`)에서 이어서, 메모리에만 있던 명언을 파일에 저장합니다.

## 실행

```bash
./gradlew build
java -cp build/classes/java/main com.back.Main
```

## 명령

| 명령 | 설명 |
| --- | --- |
| `등록` | 명언·작가 입력 후 등록 |
| `목록` / `목록?page=2` / `목록?keywordType=content&keyword=과거` | 목록·페이징·검색 |
| `삭제?id=1` | 삭제 |
| `수정?id=1` | 수정 |
| `빌드` | 모든 명언을 `db/wiseSaying/data.json` 으로 내보내기 |
| `종료` | 종료 |

## 저장 구조

```
db/wiseSaying/
├── 1.json        # 명언 하나 = 파일 하나 { "id", "content", "author" }
├── 2.json
├── lastId.txt    # 마지막으로 만든 번호 (삭제한 번호는 재사용하지 않음)
└── data.json     # 빌드 결과 (배열)
```

JSON 라이브러리 없이 `Util.json` 으로 직접 문자열을 만들고 읽습니다.

## 구조

- `WiseSayingRepository` 를 인터페이스로 바꾸고, 파일 구현체 `WiseSayingFileRepository` 를 `AppContext` 에서 갈아 끼웠습니다.
- 컨트롤러·서비스는 파일을 직접 다루지 않습니다. 파일 처리는 리포지터리(와 `Util.file`)에만 있습니다.
- 기존 메모리 구현체는 `WiseSayingMemoryRepository` 로 남겨 두었습니다.

## 테스트

```bash
./gradlew test
```

- 테스트는 `AppConfig.setTestMode()` 로 실제 `db/` 대신 `db_test/` 폴더를 쓰고, 매 테스트 전에(`@BeforeEach`) 비웁니다.
- 기존 통합 테스트(등록·목록·삭제·수정·검색·페이징)에 더해, 재시작 후 목록 유지·다음 번호·삭제 번호 미재사용·수정 유지·파일 생성/삭제·빌드 테스트가 있습니다.
