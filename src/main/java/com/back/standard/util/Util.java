package com.back.standard.util;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class Util {
    public static class file {
        // 유틸리티 메서드들
        private static Path getPath(String filePath) {
            return Paths.get(filePath);
        }

        public static void touch(String filePath) {
            set(filePath, "");
        }

        public static boolean exists(String filePath) {
            return Files.exists(getPath(filePath));
        }

        public static boolean notExists(String filePath) {
            return !exists(filePath);
        }

        public static void set(String filePath, String content) {
            Path path = getPath(filePath);
            try {
                writeFile(path, content);
            } catch (IOException e) {
                handleFileWriteError(path, content, e);
            }
        }

        private static void writeFile(Path path, String content) throws IOException {
            Files.writeString(path, content,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
        }

        private static void handleFileWriteError(Path path, String content, IOException e) {
            Path parentDir = path.getParent();
            if (parentDir != null && Files.notExists(parentDir)) {
                try {
                    Files.createDirectories(parentDir);
                    writeFile(path, content);
                } catch (IOException ex) {
                    throw new RuntimeException("파일 쓰기 실패: " + path, ex);
                }
            } else {
                throw new RuntimeException("파일 접근 실패: " + path, e);
            }
        }

        private static class FileDeleteVisitor extends SimpleFileVisitor<Path> {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        }

        public static boolean delete(String filePath) {
            try {
                Files.walkFileTree(getPath(filePath), new FileDeleteVisitor());
                return true;
            } catch (IOException e) {
                return false;
            }
        }

        public static String get(String filePath, String defaultValue) {
            try {
                return Files.readString(getPath(filePath));
            } catch (IOException e) {
                return defaultValue;
            }
        }

        public static void mkdir(String dirPath) {
            try {
                Files.createDirectories(getPath(dirPath));
            } catch (IOException e) {
                throw new RuntimeException("디렉토리 생성 실패: " + dirPath, e);
            }
        }

        public static boolean rmdir(String dirPath) {
            return delete(dirPath);
        }

        // 폴더 안의 파일 이름 목록 (폴더가 없으면 빈 목록)
        public static List<String> list(String dirPath) {
            Path path = getPath(dirPath);

            if (Files.notExists(path)) return List.of();

            try (Stream<Path> stream = Files.list(path)) {
                return stream
                        .filter(Files::isRegularFile)
                        .map(p -> p.getFileName().toString())
                        .toList();
            } catch (IOException e) {
                throw new RuntimeException("디렉토리 목록 조회 실패: " + dirPath, e);
            }
        }
    }

    public static class json {
        public static String toString(List<Map<String, Object>> mapList) {
            StringBuilder sb = new StringBuilder();

            sb.append("[");
            sb.append("\n");

            String indent = "    ";

            mapList.forEach(map -> {
                sb.append(indent);
                sb.append(toString(map).replaceAll("\n", "\n" + indent));
                sb.append(",\n");
            });

            if (!mapList.isEmpty()) {
                sb.delete(sb.length() - 2, sb.length());
            }

            sb.append("\n");
            sb.append("]");

            return sb.toString();
        }

        public static String toString(Map<String, Object> map) {
            StringBuilder sb = new StringBuilder();

            sb.append("{");
            sb.append("\n");

            map.forEach((key, value) -> {
                sb.append("    ");
                key = "\"" + key + "\"";

                if (value instanceof String) {
                    value = "\"" + escape((String) value) + "\"";
                }

                sb.append("%s: %s,\n".formatted(key, value));
            });

            if (!map.isEmpty()) {
                sb.delete(sb.length() - 2, sb.length());
            }

            sb.append("\n");
            sb.append("}");

            return sb.toString();
        }

        public static Map<String, Object> toMap(String jsonStr) {
            Map<String, Object> map = new LinkedHashMap<>();

            jsonStr = jsonStr.trim();
            jsonStr = jsonStr.substring(1, jsonStr.length() - 1);

            String[] jsonStrBits = jsonStr.split(",\n    \"");

            for (String jsonStrBit : jsonStrBits) {
                jsonStrBit = jsonStrBit.trim();

                if (jsonStrBit.endsWith(",")) jsonStrBit = jsonStrBit.substring(0, jsonStrBit.length() - 1);

                String[] jsonField = jsonStrBit.split("\": ", 2);

                String key = jsonField[0];
                if (key.startsWith("\"")) key = key.substring(1);

                boolean valueIsString = jsonField[1].startsWith("\"") && jsonField[1].endsWith("\"");
                String value = jsonField[1];

                if (valueIsString) value = value.substring(1, value.length() - 1);

                if (valueIsString) {
                    map.put(key, unescape(value));
                } else if (value.equals("true") || value.equals("false")) {
                    map.put(key, value.equals("true"));
                } else if (value.contains(".")) {
                    map.put(key, Double.parseDouble(value));
                } else {
                    map.put(key, Integer.parseInt(value));
                }
            }

            return map;
        }

        private static String escape(String str) {
            return str
                    .replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n");
        }

        private static String unescape(String str) {
            StringBuilder sb = new StringBuilder();

            for (int i = 0; i < str.length(); i++) {
                char c = str.charAt(i);

                if (c == '\\' && i + 1 < str.length()) {
                    char next = str.charAt(++i);
                    sb.append(next == 'n' ? '\n' : next);
                } else {
                    sb.append(c);
                }
            }

            return sb.toString();
        }
    }
}