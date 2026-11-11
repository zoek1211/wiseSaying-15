package com.back.domain.wiseSaying.repository;

import com.back.AppConfig;
import com.back.domain.wiseSaying.entity.WiseSaying;
import com.back.standard.util.Util;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class WiseSayingFileRepository implements WiseSayingRepository {
    public String getTableDirPath() {
        return AppConfig.getDbDirPath() + "/wiseSaying";
    }

    public String getFilePath(int id) {
        return getTableDirPath() + "/%d.json".formatted(id);
    }

    public String getLastIdPath() {
        return getTableDirPath() + "/lastId.txt";
    }

    public String getArchiveFilePath() {
        return AppConfig.getExportFilePath();
    }

    @Override
    public WiseSaying save(WiseSaying wiseSaying) {
        if (wiseSaying.isNew()) {
            int newId = getLastId() + 1;
            wiseSaying.setId(newId);
            setLastId(newId);
        }

        Util.file.set(getFilePath(wiseSaying.getId()), Util.json.toString(wiseSaying.toMap()));

        return wiseSaying;
    }

    @Override
    public WiseSaying findById(int id) {
        String jsonStr = Util.file.get(getFilePath(id), "");

        if (jsonStr.isBlank()) return null;

        return new WiseSaying(Util.json.toMap(jsonStr));
    }

    @Override
    public void delete(WiseSaying wiseSaying) {
        Util.file.delete(getFilePath(wiseSaying.getId()));
    }

    @Override
    public List<WiseSaying> findAll() {
        return Util.file.list(getTableDirPath())
                .stream()
                .filter(fileName -> fileName.matches("\\d+\\.json"))
                .map(fileName -> Integer.parseInt(fileName.replace(".json", "")))
                .map(this::findById)
                .filter(w -> w != null)
                .sorted(Comparator.comparingInt(WiseSaying::getId).reversed())
                .toList();
    }

    @Override
    public String build() {
        List<Map<String, Object>> mapList = findAll()
                .reversed()
                .stream()
                .map(WiseSaying::toMap)
                .toList();

        Util.file.set(getArchiveFilePath(), Util.json.toString(mapList));

        return getArchiveFilePath();
    }

    public int getLastId() {
        String lastIdStr = Util.file.get(getLastIdPath(), "0").trim();

        try {
            return Integer.parseInt(lastIdStr);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void setLastId(int lastId) {
        Util.file.set(getLastIdPath(), String.valueOf(lastId));
    }
}
