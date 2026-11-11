# 명언 앱 ⑧ — MySQL 전환 (15단계)

명언 앱 ⑦(`wiseSaying-9-10`, 파일 영속성·빌드)에서 이어서, 명언(WiseSaying)을 MySQL `wise_saying` 테이블에 저장합니다.
콘솔 명령과 출력은 ⑦과 같습니다.

## 1. MySQL 실행

Homebrew:

```bash
brew install mysql
brew services start mysql      # 기본 사용자 root, 비밀번호 없음
```

Docker (대안):

```bash
docker run -d --name wise-saying-mysql -p 3306:3306 -e MYSQL_ALLOW_EMPTY_PASSWORD=yes mysql:8
```

## 2. DB·테이블 생성 (setup script)

```bash
mysql -uroot < sql/schema.sql      # 비밀번호가 있으면 -p 추가
```

[`sql/schema.sql`](sql/schema.sql) 하나가 `wise_saying_dev`(앱 실행용)와 `wise_saying_test`(테스트용)를 utf8mb4 로 만들고,
각각에 `wise_saying` 테이블을 만듭니다. 앱은 DB나 테이블을 스스로 만들지 않습니다.

```sql
wise_saying(id INT AUTO_INCREMENT PRIMARY KEY, content TEXT NOT NULL, author VARCHAR(100) NOT NULL,
            create_date DATETIME DEFAULT CURRENT_TIMESTAMP,
            modify_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP)
```

## 3. 접속 정보 설정

소스에는 비밀번호도 접속 URL 도 없습니다. 다음 순서로 찾습니다.

1. 환경변수 `DB_URL`, `DB_USER`, `DB_PASSWORD`
2. 프로젝트 루트의 `db.properties` (gitignore 대상)

```bash
cp db.properties.example db.properties   # 필요하면 값 수정
```

`db.url` 은 서버 주소까지만 적습니다(`jdbc:mysql://localhost:3306`). DB 이름은 모드가 정합니다:
앱 실행은 `wise_saying_dev`, 테스트는 `wise_saying_test`.

## 4. 실행

```bash
./gradlew run --console=plain -q
```

## 명령

| 명령 | 설명 |
| --- | --- |
| `등록` | 명언·작가 입력 후 등록 |
| `목록` / `목록?page=2` / `목록?keywordType=content&keyword=과거` | 목록·페이징·검색 |
| `삭제?id=1` | 삭제 |
| `수정?id=1` | 수정 |
| `빌드` | 모든 명언을 번호 오름차순 JSON 배열로 `data.json`(프로젝트 루트)에 내보내기(Export) |
| `종료` | 종료 |

## 5. 테스트

```bash
./gradlew test
```

- 테스트는 실제 MySQL 의 `wise_saying_test` 를 씁니다(H2 아님, [ADR 0001](docs/adr/0001-mysql-behind-swappable-repository.md)).
- 매 테스트 전 `AppTestRunner.resetStorage()` 가 `TRUNCATE wise_saying` 을 실행해 테이블을 비우고 번호를 1부터 다시 시작합니다.
- MySQL 에 연결할 수 없으면 테스트는 건너뛰지 않고 접속 오류로 실패합니다.
- 테스트 모드의 빌드 결과는 `db_test/data.json` 입니다.

## 리포지터리 교체

- `WiseSayingRepository` 인터페이스는 그대로이고, MySQL 구현체 `WiseSayingDbRepository`(순수 JDBC, PreparedStatement, 작업마다 연결을 열고 닫음)를 추가했습니다.
- `AppContext` 에서 `new WiseSayingFileRepository()` 를 `new WiseSayingDbRepository()` 로 바꾼 한 줄이 교체의 전부입니다. 컨트롤러·서비스는 바뀌지 않았습니다.
- SQL·JDBC 는 `WiseSayingDbRepository` 와 접속 정보 도우미 `DbConfig` 에만 있습니다.
- 새 명언은 INSERT 후 AUTO_INCREMENT 로 생성된 키를 번호로 쓰고, 기존 명언은 번호로 UPDATE 합니다. AUTO_INCREMENT 는 삭제된 번호를 재사용하지 않습니다.
- 검색·페이징은 아직 인터페이스의 기본(메모리) 구현을 씁니다.
- 파일 구현체 `WiseSayingFileRepository` 와 메모리 구현체 `WiseSayingMemoryRepository` 는 대안으로 남아 있고, 파일 구현체는 자체 테스트(`WiseSayingFileRepositoryTest`)가 있습니다.
