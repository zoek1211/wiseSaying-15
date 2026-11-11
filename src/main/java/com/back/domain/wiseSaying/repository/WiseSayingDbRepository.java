package com.back.domain.wiseSaying.repository;

import com.back.AppConfig;
import com.back.domain.wiseSaying.entity.WiseSaying;
import com.back.global.db.DbConfig;
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
// 검색·페이징은 아직 인터페이스의 기본(메모리) 구현을 그대로 쓴다.
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
        String sql = "INSERT INTO wise_saying (content, author) VALUES (?, ?)";

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, wiseSaying.getContent());
            stmt.setString(2, wiseSaying.getAuthor());
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                keys.next();
                wiseSaying.setId(keys.getInt(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void update(WiseSaying wiseSaying) {
        String sql = "UPDATE wise_saying SET content = ?, author = ? WHERE id = ?";

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, wiseSaying.getContent());
            stmt.setString(2, wiseSaying.getAuthor());
            stmt.setInt(3, wiseSaying.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public WiseSaying findById(int id) {
        List<WiseSaying> rows = query("SELECT id, content, author FROM wise_saying WHERE id = ?", id);

        return rows.isEmpty() ? null : rows.getFirst();
    }

    @Override
    public void delete(WiseSaying wiseSaying) {
        String sql = "DELETE FROM wise_saying WHERE id = ?";

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, wiseSaying.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
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

    // 테스트 준비용: 테이블을 비우고 AUTO_INCREMENT 를 1로 되돌린다. 운영 코드에서는 부르지 않는다.
    public void truncate() {
        try (Connection conn = DbConfig.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("TRUNCATE wise_saying");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private List<WiseSaying> query(String sql, Object... params) {
        try (Connection conn = DbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                List<WiseSaying> rows = new ArrayList<>();

                while (rs.next()) {
                    WiseSaying wiseSaying = new WiseSaying(rs.getString("content"), rs.getString("author"));
                    wiseSaying.setId(rs.getInt("id"));
                    rows.add(wiseSaying);
                }

                return rows;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
