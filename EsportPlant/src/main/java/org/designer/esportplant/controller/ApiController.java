package org.designer.esportplant.controller;

import org.designer.esportplant.DeleteSingleFile;
import org.designer.esportplant.ReadFiles;
import org.designer.esportplant.TransferPathOfSingleFile;
import org.designer.esportplant.WriteFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.nio.file.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/api")
public class ApiController {

    private static final Logger log = LoggerFactory.getLogger(ApiController.class);
    private static final String DB_URL = "jdbc:mysql://localhost:3306/E_Plant?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&useUnicode=true&characterEncoding=UTF-8";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "123456";

    // ======================== SYSTEM ========================

    @GetMapping("/system/info")
    public Map<String, Object> systemInfo() {
        Map<String, Object> info = new LinkedHashMap<>();
        Runtime rt = Runtime.getRuntime();
        long usedMem = rt.totalMemory() - rt.freeMemory();
        info.put("javaVersion", System.getProperty("java.version"));
        info.put("osName", System.getProperty("os.name"));
        info.put("osArch", System.getProperty("os.arch"));
        info.put("cores", rt.availableProcessors());
        info.put("maxMemory", formatMem(rt.maxMemory()));
        info.put("usedMemory", formatMem(usedMem));
        info.put("freeMemory", formatMem(rt.freeMemory()));
        info.put("totalMemory", formatMem(rt.totalMemory()));
        info.put("jvmMemUsed", formatMem(usedMem));
        RuntimeMXBean mx = ManagementFactory.getRuntimeMXBean();
        long ms = mx.getUptime();
        long h = ms / 3600000, m = (ms % 3600000) / 60000, s = (ms % 60000) / 1000;
        info.put("uptime", String.format("%dh %dm %ds", h, m, s));
        info.put("serverPort", "8082");
        info.put("dbUrl", "localhost:3306/E_Plant");
        info.put("projectDir", System.getProperty("user.dir"));

        boolean dbOk = false;
        int recordCount = 0;
        try (Connection c = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            dbOk = true;
            try (PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM e_table");
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) recordCount = rs.getInt(1);
            }
        } catch (Exception e) {
            log.warn("DB check failed: {}", e.getMessage());
        }
        info.put("dbConnected", dbOk);
        info.put("dbRecords", recordCount);

        int fileCount = 0;
        try {
            Path dir = Path.of("D:\\桌面");
            if (Files.isDirectory(dir)) {
                try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir, "*.txt")) {
                    for (Path ignored : ds) fileCount++;
                }
            }
        } catch (Exception ignored) {}
        info.put("fileCount", fileCount);
        return info;
    }

    // ======================== DATABASE CRUD ========================

    @GetMapping("/db/records")
    public Map<String, Object> dbRecords() {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> records = new ArrayList<>();
        try (Connection c = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT id, name, position, game_name FROM e_table")) {
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("id", rs.getInt("id"));
                row.put("name", rs.getString("name"));
                row.put("position", rs.getInt("position"));
                row.put("gameName", rs.getString("game_name"));
                records.add(row);
            }
            result.put("success", true);
            result.put("records", records);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "查询失败: " + e.getMessage());
        }
        return result;
    }

    @PostMapping("/db/add")
    public Map<String, Object> dbAdd(@RequestBody Map<String, Object> body) {
        Map<String, Object> result = new HashMap<>();
        String name = (String) body.get("name");
        int position = body.get("position") != null ? ((Number) body.get("position")).intValue() : 0;
        String gameName = (String) body.get("gameName");
        if (name == null || name.isEmpty() || gameName == null || gameName.isEmpty()) {
            result.put("success", false);
            result.put("message", "Name and Game Name are required");
            return result;
        }
        try (Connection c = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement ps = c.prepareStatement("INSERT INTO e_table(name, position, game_name) VALUES (?, ?, ?)")) {
            ps.setString(1, name);
            ps.setInt(2, position);
            ps.setString(3, gameName);
            ps.executeUpdate();
            result.put("success", true);
            result.put("message", "Record added successfully");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Add failed: " + e.getMessage());
        }
        return result;
    }

    @PostMapping("/db/delete")
    public Map<String, Object> dbDelete(@RequestBody Map<String, Object> body) {
        Map<String, Object> result = new HashMap<>();
        Object idObj = body.get("id");
        if (idObj == null) {
            result.put("success", false);
            result.put("message", "ID is required");
            return result;
        }
        int id = ((Number) idObj).intValue();
        try (Connection c = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement ps = c.prepareStatement("DELETE FROM e_table WHERE id = ?")) {
            ps.setInt(1, id);
            int affected = ps.executeUpdate();
            result.put("success", affected > 0);
            result.put("message", affected > 0 ? "Record #" + id + " deleted" : "Record not found");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Delete failed: " + e.getMessage());
        }
        return result;
    }

    // ======================== FILE OPS ========================

    @PostMapping("/files/list")
    public Map<String, Object> fileList(@RequestBody Map<String, String> body) {
        Map<String, Object> result = new HashMap<>();
        String path = body.getOrDefault("path", "D:\\");
        try {
            Path dir = Path.of(path);
            if (!Files.isDirectory(dir)) {
                result.put("success", false);
                result.put("message", "Not a directory: " + path);
                return result;
            }
            List<Map<String, Object>> files = new ArrayList<>();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir)) {
                for (Path p : ds) {
                    Map<String, Object> f = new LinkedHashMap<>();
                    f.put("name", p.getFileName().toString());
                    f.put("path", p.toAbsolutePath().toString());
                    f.put("isDir", Files.isDirectory(p));
                    if (Files.isRegularFile(p)) {
                        long size = Files.size(p);
                        f.put("size", size);
                        f.put("sizeStr", formatSize(size));
                    }
                    try {
                        f.put("dateStr", sdf.format(new java.util.Date(Files.getLastModifiedTime(p).toMillis())));
                    } catch (Exception e) {
                        f.put("dateStr", "--");
                    }
                    files.add(f);
                }
            }
            files.sort((a, b) -> {
                boolean ad = (Boolean) a.get("isDir"), bd = (Boolean) b.get("isDir");
                if (ad != bd) return ad ? -1 : 1;
                return ((String) a.get("name")).compareToIgnoreCase((String) b.get("name"));
            });
            result.put("success", true);
            result.put("files", files);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Browse failed: " + e.getMessage());
        }
        return result;
    }

    // ======================== EXISTING OPS ========================

    @PostMapping("/write")
    public Map<String, Object> write() {
        Map<String, Object> result = new HashMap<>();
        try {
            WriteFile writeFile = new WriteFile();
            writeFile.writeFunction();
            result.put("success", true);
            result.put("message", "数据库查询与文件写入完成");
        } catch (IOException e) {
            result.put("success", false);
            result.put("message", "I/O 异常: " + e.getMessage());
        } catch (SQLException e) {
            result.put("success", false);
            result.put("message", "数据库异常: " + e.getMessage());
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "操作异常: " + e.getMessage());
        }
        return result;
    }

    @PostMapping("/read")
    public Map<String, Object> read(@RequestBody Map<String, String> body) {
        Map<String, Object> result = new HashMap<>();
        String path = body.getOrDefault("path", "D:\\桌面\\NameCodeForE_Plant.txt");
        try {
            String content = Files.readString(Path.of(path));
            result.put("success", true);
            result.put("content", content);
            result.put("message", "文件读取完成");
        } catch (IOException e) {
            result.put("success", false);
            result.put("message", "读取失败: " + e.getMessage());
        }
        return result;
    }

    @PostMapping("/transfer")
    public Map<String, Object> transfer(@RequestBody Map<String, String> body) {
        Map<String, Object> result = new HashMap<>();
        String source = body.get("source");
        String target = body.get("target");
        if (source == null || source.isEmpty() || target == null || target.isEmpty()) {
            result.put("success", false);
            result.put("message", "源路径和目标路径不能为空");
            return result;
        }
        try {
            TransferPathOfSingleFile t = new TransferPathOfSingleFile();
            t.transferFile(source, target);
            result.put("success", true);
            result.put("message", "文件已移动到: " + target);
        } catch (IOException e) {
            result.put("success", false);
            result.put("message", "移动失败: " + e.getMessage());
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "操作异常: " + e.getMessage());
        }
        return result;
    }

    @PostMapping("/delete")
    public Map<String, Object> delete(@RequestBody Map<String, String> body) {
        Map<String, Object> result = new HashMap<>();
        String path = body.get("path");
        if (path == null || path.isEmpty()) {
            result.put("success", false);
            result.put("message", "文件路径不能为空");
            return result;
        }
        try {
            DeleteSingleFile d = new DeleteSingleFile();
            d.delete(path);
            result.put("success", true);
            result.put("message", "文件已移入回收站");
        } catch (IOException e) {
            result.put("success", false);
            result.put("message", "删除失败: " + e.getMessage());
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "操作异常: " + e.getMessage());
        }
        return result;
    }

    @PostMapping("/draw")
    public Map<String, Object> draw(@RequestBody Map<String, String> body) {
        Map<String, Object> result = new HashMap<>();
        String text = body.getOrDefault("text", "唐山神医");
        try {
            WriteFile wf = new WriteFile();
            String art = wf.drawChineseCharacter(text);
            result.put("success", true);
            result.put("art", art);
            result.put("text", text);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "绘制失败: " + e.getMessage());
        }
        return result;
    }

    // ======================== HELPERS ========================

    private String formatMem(long bytes) {
        double mb = bytes / 1024.0 / 1024.0;
        return String.format("%.0f MB", mb);
    }

    private String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1048576) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1073741824) return String.format("%.1f MB", bytes / 1048576.0);
        return String.format("%.2f GB", bytes / 1073741824.0);
    }
}
