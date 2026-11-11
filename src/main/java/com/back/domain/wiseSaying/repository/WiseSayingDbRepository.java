package com.back.domain.wiseSaying.repository;

import com.back.AppConfig;
import com.back.domain.wiseSaying.entity.WiseSaying;
import com.back.global.db.DbConfig;
import com.back.standard.dto.Page;
import com.back.standard.dto.Pageable;
import com.back.standard.util.Util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// MySQL wise_saying 테이블에 저장하는 구현체. 순수 JDBC + PreparedStatement 를 쓰고,
// 연결은 작업마다 열고 닫는다(리포지터리가 연결을 붙잡고 있지 않는다).
// 검색·페이징은 인터페이스의 기본(메모리) 구현 대신 SQL(WHERE ... LIKE, ORDER BY id DESC LIMIT/OFFSET, COUNT(*))로 한다.
public class WiseSayingDbRepository implements WiseSayingRepository {
    @Override
    public WiseSaying save(WiseSaying wiseSaying) {
        if (wiseSaying.isNew()) {
            insert(wiseSaying);
        } else {
            update(wiseSaying);
        }

        return wiseSaying;
    }

    private void insert(WiseSaying wiseSaying) {
        int id = executeInsert(
                "INSERT INTO wise_saying (content, author) VALUES (?, ?)",
                wiseSaying.getContent(), wiseSaying.getAuthor()
        );

        wiseSaying.setId(id);
    }

    private void update(WiseSaying wiseSaying) {
        executeUpdate(
                "UPDATE wise_saying SET content = ?, author = ? WHERE id = ?",
                wiseSaying.getContent(), wiseSaying.getAuthor(), wiseSaying.getId()
        );
    }

    @Override
    public WiseSaying findById(int id) {
        List<WiseSaying> rows = query("SELECT id, content, author FROM wise_saying WHERE id = ?", id);

        return rows.isEmpty() ? null : rows.getFirst();
    }

    @Override
    public void delete(WiseSaying wiseSaying) {
        executeUpdate("DELETE FROM wise_saying WHERE id = ?", wiseSaying.getId());
    }

    @Override
    public List<WiseSaying> findAll() {
        return query("SELECT id, content, author FROM wise_saying ORDER BY id DESC");
    }

    @Override
    public String build() {
        List<Map<String, Object>> mapList = query("SELECT id, content, author FROM wise_saying ORDER BY id ASC")
                .stream()
                .map(WiseSaying::toMap)
                .toList();

        String exportFilePath = AppConfig.getExportFilePath();
        Util.file.set(exportFilePath, Util.json.toString(mapList));

        return exportFilePath;
    }

    @Override
    public Page<WiseSaying> findForList(Pageable pageable) {
        return findForListWhere("", List.of(), pageable);
    }

    @Override
    public Page<WiseSaying> findForListByContentContaining(String keyword, Pageable pageable) {
        return findForListWhere("WHERE content LIKE ? ESCAPE '!'", List.of(likePattern(keyword)), pageable);
    }

    @Override
    public Page<WiseSaying> findForListByAuthorContaining(String keyword, Pageable pageable) {
        return findForListWhere("WHERE author LIKE ? ESCAPE '!'", List.of(likePattern(keyword)), pageable);
    }

    @Override
    public Page<WiseSaying> findForListByContentContainingOrAuthorContaining(String keyword1, String keyword2, Pageable pageable) {
        return findForListWhere(
                "WHERE content LIKE ? ESCAPE '!' OR author LIKE ? ESCAPE '!'",
                List.of(likePattern(keyword1), likePattern(keyword2)),
                pageable
        );
    }

    // 같은 WHERE 절로 한 페이지(번호 내림차순)와 전체 개수를 각각 조회한다.
    private Page<WiseSaying> findForListWhere(String where, List<Object> whereParams, Pageable pageable) {
        List<Object> pageParams = new ArrayList<>(whereParams);
        pageParams.add(pageable.getPageSize());
        pageParams.add(pageable.getSkipCount());

        List<WiseSaying> content = query(
                "SELECT id, content, author FROM wise_saying " + where + " ORDER BY id DESC LIMIT ? OFFSET ?",
                pageParams.toArray()
        );

        int totalCount = count("SELECT COUNT(*) FROM wise_saying " + where, whereParams.toArray());

        return new Page<>(totalCount, pageable.getPageNo(), pageable.getPageSize(), content);
    }

    // 키워드를 글자 그대로 찾도록 LIKE 의 이스케이프 문자(!)와 와일드카드(%, _)를 이스케이프한 뒤 %…% 로 감싼다.
    // 이스케이프 문자는 MySQL 기본값(\, NO_BACKSLASH_ESCAPES 에서 사라짐)에 기대지 않고 SQL 에 ESCAPE '!' 로 명시한다.
    private static String likePattern(String keyword) {
        String escaped = keyword
                .replace("!", "!!")
                .replace("%", "!%")
                .replace("_", "!_");

        return "%" + escaped + "%";
    }

    // 테스트 준비용: 테이블을 비우고 AUTO_INCREMENT 를 1로 되돌린다. 운영 코드에서는 부르지 않는다.
    // 테스트 모드가 아니면(= wise_saying_dev) 거부한다. dev 의 번호가 재사용되면 안 되기 때문이다.
    public void truncate() {
        if (!AppConfig.isTestMode()) {
            throw new IllegalStateException("truncate 는 테스트 모드에서만 쓸 수 있습니다 (" + DbConfig.getDbName() + " 를 비우지 않습니다).");
        }

        executeUpdate("TRUNCATE wise_saying");
    }

    private List<WiseSaying> query(String sql, Object... params) {
        return execute(sql, Statement.NO_GENERATED_KEYS, params, stmt -> {
            try (ResultSet rs = stmt.executeQuery()) {
                List<WiseSaying> rows = new ArrayList<>();

                while (rs.next()) {
                    WiseSaying wiseSaying = new WiseSaying(rs.getString("content"), rs.getString("author"));
                    wiseSaying.setId(rs.getInt("id"));
                    rows.add(wiseSaying);
                }

                return rows;
            }
        });
    }

    private int count(String sql, Object... params) {
        return execute(sql, Statement.NO_GENERATED_KEYS, params, stmt -> {
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        });
    }

    private int executeUpdate(String sql, Object... params) {
        return execute(sql, Statement.NO_GENERATED_KEYS, params, PreparedStatement::executeUpdate);
    }

    // INSERT 를 실행하고 DB 가 만든 번호(AUTO_INCREMENT)를 돌려준다. 번호가 오지 않으면 조용히 0번으로 두지 않고 실패한다.
    private int executeInsert(String sql, Object... params) {
        return execute(sql, Statement.RETURN_GENERATED_KEYS, params, stmt -> {
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new IllegalStateException("등록한 명언의 번호를 DB 에서 받지 못했습니다.");
                }

                return keys.getInt(1);
            }
        });
    }

    @FunctionalInterface
    private interface StatementWork<T> {
        T run(PreparedStatement stmt) throws SQLException;
    }

    // 연결 열기 → 준비 → 바인딩 → 실행 → 닫기, SQLException 감싸기를 한 곳에서 한다.
    private <T> T execute(String sql, int autoGeneratedKeys, Object[] params, StatementWork<T> work) {
        try (Connection conn = DbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, autoGeneratedKeys)) {
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }

            return work.run(stmt);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
