package org.designer.esportplant.gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import org.designer.esportplant.DeleteSingleFile;
import org.designer.esportplant.TransferPathOfSingleFile;
import org.designer.esportplant.WriteFile;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.nio.file.*;
import java.sql.*;
import java.util.*;

public class EsportPlantGui extends Application {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/E_Plant?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&useUnicode=true&characterEncoding=UTF-8";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "123456";

    private final Font bigNum = Font.font("Consolas", FontWeight.BOLD, 28);

    private VBox sidebar;
    private StackPane contentArea;
    private final List<Button> navButtons = new ArrayList<>();

    @Override
    public void start(Stage stage) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #0a0a12;");

        root.setLeft(buildSidebar());
        contentArea = new StackPane();
        contentArea.setStyle("-fx-background-color: #0a0a12;");
        root.setCenter(contentArea);

        showPage("dashboard");

        Scene scene = new Scene(root, 1100, 700);
        var cssUrl = getClass().getResource("/gui/style.css");
        if (cssUrl != null) scene.getStylesheets().add(cssUrl.toExternalForm());
        stage.setTitle("EsportPlant - Admin Console");
        stage.setScene(scene);
        stage.setMinWidth(900);
        stage.setMinHeight(550);
        stage.show();
    }

    // ==================== SIDEBAR ====================

    private VBox buildSidebar() {
        sidebar = new VBox();
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPrefWidth(220);

        Label logo = new Label("ESPORT PLANT");
        logo.getStyleClass().add("sidebar-title");
        logo.setMaxWidth(Double.MAX_VALUE);
        logo.setAlignment(Pos.CENTER);

        Label ver = new Label("ADMIN CONSOLE v1.0");
        ver.getStyleClass().add("sidebar-ver");
        ver.setMaxWidth(Double.MAX_VALUE);
        ver.setAlignment(Pos.CENTER);

        Region spacer1 = new Region();
        spacer1.setPrefHeight(20);

        sidebar.getChildren().addAll(logo, ver, spacer1);

        addNavSection("OVERVIEW");
        addNavItem("dashboard", "\u25C9  Dashboard");
        addNavSection("DATA");
        addNavItem("database", "\u2630  Database Manager");
        addNavItem("files", "\uD83D\uDCC2  File Manager");
        addNavSection("TOOLS");
        addNavItem("art", "\uD83C\uDFA8  Character Art");
        addNavSection("SYSTEM");
        addNavItem("sysinfo", "\u2699  System Info");
        addNavItem("logs", "\uD83D\uDCC4  Logs");

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        sidebar.getChildren().add(spacer);

        Label status = new Label("  ●  SYSTEM ONLINE");
        status.setStyle("-fx-text-fill: #00ff88; -fx-font-size: 10px; -fx-padding: 12 0;");
        sidebar.getChildren().add(status);

        return sidebar;
    }

    private void addNavSection(String text) {
        Label s = new Label(text);
        s.getStyleClass().add("nav-section");
        sidebar.getChildren().add(s);
    }

    private void addNavItem(String id, String text) {
        Button btn = new Button(text);
        btn.getStyleClass().add("nav-btn");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setOnAction(e -> showPage(id));
        navButtons.add(btn);
        sidebar.getChildren().add(btn);
    }

    private void setActiveNav(String id) {
        int idx = switch (id) {
            case "dashboard" -> 0;
            case "database" -> 1;
            case "files" -> 2;
            case "art" -> 3;
            case "sysinfo" -> 4;
            case "logs" -> 5;
            default -> -1;
        };
        for (int i = 0; i < navButtons.size(); i++) {
            Button b = navButtons.get(i);
            b.getStyleClass().remove("nav-btn-active");
            if (i == idx) b.getStyleClass().add("nav-btn-active");
        }
    }

    // ==================== PAGE ROUTER ====================

    private void showPage(String id) {
        setActiveNav(id);
        contentArea.getChildren().clear();
        ScrollPane sp = new ScrollPane();
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background: #0a0a12; -fx-background-color: #0a0a12;");
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        VBox page = switch (id) {
            case "dashboard" -> buildDashboardPage();
            case "database" -> buildDatabasePage();
            case "files" -> buildFilesPage();
            case "art" -> buildArtPage();
            case "sysinfo" -> buildSysInfoPage();
            case "logs" -> buildLogsPage();
            default -> new VBox();
        };
        page.setPadding(new Insets(20));
        sp.setContent(page);
        contentArea.getChildren().add(sp);
    }

    // ==================== DASHBOARD ====================

    private final Label dashDbStatus = new Label("--");
    private final Label dashDbRecords = new Label("--");
    private final Label dashFileCount = new Label("--");
    private final Label dashJvmMem = new Label("--");
    private final Label dashUptime = new Label("--");
    private final TextArea dashLogArea = new TextArea();

    private VBox buildDashboardPage() {
        dashLogArea.setEditable(false);
        dashLogArea.setPrefHeight(180);
        dashLogArea.setStyle("-fx-control-inner-background: #060610; -fx-text-fill: #6a6a8a;");

        Label pageTitle = new Label("Dashboard");
        pageTitle.setStyle("-fx-text-fill: #00ffff; -fx-font-size: 20px; -fx-font-weight: bold;");
        pageTitle.setPadding(new Insets(0, 0, 16, 0));

        FlowPane stats = new FlowPane(Orientation.HORIZONTAL, 12, 12);
        stats.getChildren().addAll(
                statCard("● DB STATUS", dashDbStatus, "MySQL Connection", "#00ffff"),
                statCard("◉ RECORDS", dashDbRecords, "e_table rows", "#00ff88"),
                statCard("\uD83D\uDCC1 FILES", dashFileCount, "D:\\桌面\\*.txt", "#ff00ff"),
                statCard("⚡ JVM MEM", dashJvmMem, "Heap Used", "#ffcc00"),
                statCard("⏱ UPTIME", dashUptime, "Process Running", "#7b2fff")
        );

        HBox buttons = new HBox(8);
        buttons.setPadding(new Insets(16, 0, 0, 0));
        Button refreshBtn = makeBtn("Refresh", "btn btn-yellow");
        refreshBtn.setOnAction(e -> loadDashboard());
        buttons.getChildren().add(refreshBtn);

        VBox.setMargin(stats, new Insets(0, 0, 16, 0));
        VBox.setMargin(buttons, new Insets(0, 0, 16, 0));

        Label logTitle = new Label("Recent Activity");
        logTitle.setStyle("-fx-text-fill: #00ffff; -fx-font-size: 13px; -fx-font-weight: bold;");
        VBox.setMargin(logTitle, new Insets(0, 0, 8, 0));

        loadDashboard();
        return new VBox(0, pageTitle, stats, buttons, logTitle, dashLogArea);
    }

    private VBox statCard(String label, Label value, String sub, String color) {
        value.setStyle("-fx-text-fill: " + color + ";");
        value.setFont(bigNum);
        Label l = new Label(label);
        l.getStyleClass().add("stat-label");
        Label s = new Label(sub);
        s.getStyleClass().add("stat-sub");
        VBox card = new VBox(4, l, value, s);
        card.getStyleClass().add("stat-card");
        card.setPrefWidth(180);
        return card;
    }

    private void loadDashboard() {
        new Thread(() -> {
            final String[] dbStatusText = {"OFFLINE"};
            final String[] dbStatusColor = {"#ff3366"};
            final int[] recordCount = {0};
            try (Connection c = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
                dbStatusText[0] = "ONLINE";
                dbStatusColor[0] = "#00ff88";
                try (PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM e_table");
                     ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) recordCount[0] = rs.getInt(1);
                }
            } catch (Exception ignored) {}

            final int[] fileCount = {0};
            try {
                Path dir = Path.of("D:\\桌面");
                if (Files.isDirectory(dir)) {
                    try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir, "*.txt")) {
                        for (Path ignored : ds) fileCount[0]++;
                    }
                }
            } catch (Exception ignored) {}

            Runtime rt = Runtime.getRuntime();
            long usedMem = rt.totalMemory() - rt.freeMemory();
            RuntimeMXBean mx = ManagementFactory.getRuntimeMXBean();
            long ms = mx.getUptime();

            Platform.runLater(() -> {
                dashDbStatus.setText(dbStatusText[0]);
                dashDbStatus.setStyle("-fx-text-fill: " + dbStatusColor[0] + ";");
                dashDbRecords.setText(String.valueOf(recordCount[0]));
                dashFileCount.setText(String.valueOf(fileCount[0]));
                dashJvmMem.setText(String.format("%.0f MB", usedMem / 1048576.0));
                dashUptime.setText(String.format("%dh %dm", ms / 3600000, (ms % 3600000) / 60000));
            });
        }).start();
    }

    // ==================== DATABASE ====================

    private TableView<Map<String, Object>> dbTable;
    private TextArea dbLog;

    private VBox buildDatabasePage() {
        Label pageTitle = new Label("Database Manager");
        pageTitle.setStyle("-fx-text-fill: #00ffff; -fx-font-size: 20px; -fx-font-weight: bold;");

        dbTable = new TableView<>();
        dbTable.setPrefHeight(280);
        dbTable.setStyle("-fx-background-color: #0c0c1d;");

        TableColumn<Map<String, Object>, String> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(String.valueOf(d.getValue().get("id"))));
        colId.setPrefWidth(60);

        TableColumn<Map<String, Object>, String> colName = new TableColumn<>("Name");
        colName.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(String.valueOf(d.getValue().get("name"))));
        colName.setPrefWidth(150);

        TableColumn<Map<String, Object>, String> colPos = new TableColumn<>("Position");
        colPos.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(String.valueOf(d.getValue().get("position"))));
        colPos.setPrefWidth(100);

        TableColumn<Map<String, Object>, String> colGame = new TableColumn<>("Game Name");
        colGame.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(String.valueOf(d.getValue().get("gameName"))));
        colGame.setPrefWidth(150);

        dbTable.getColumns().addAll(colId, colName, colPos, colGame);

        Button refreshBtn = makeBtn("Refresh", "btn btn-sm");
        refreshBtn.setOnAction(e -> loadDbRecords());
        Button addBtn = makeBtn("+ Add Record", "btn btn-sm btn-green");
        addBtn.setOnAction(e -> showAddRecordDialog());
        Button delBtn = makeBtn("Delete Selected", "btn btn-sm btn-red");
        delBtn.setOnAction(e -> deleteSelectedRecord());
        HBox tableBtns = new HBox(8, refreshBtn, addBtn, delBtn);
        tableBtns.setPadding(new Insets(8, 0, 0, 0));

        VBox.setMargin(dbTable, new Insets(8, 0, 0, 0));

        Label writeTitle = new Label("Write to File");
        writeTitle.setStyle("-fx-text-fill: #00ffff; -fx-font-size: 13px; -fx-font-weight: bold;");
        writeTitle.setPadding(new Insets(16, 0, 4, 0));
        Label writeDesc = new Label("Query all records and write to D:\\桌面\\NameCodeForE_Plant.txt");
        writeDesc.setStyle("-fx-text-fill: #5a5a8a; -fx-font-size: 11px;");
        Button writeBtn = makeBtn("Execute WriteOperation", "btn btn-yellow");
        writeBtn.setOnAction(e -> doWriteOp());

        dbLog = new TextArea();
        dbLog.setEditable(false);
        dbLog.setPrefHeight(120);
        dbLog.setStyle("-fx-control-inner-background: #060610; -fx-text-fill: #6a6a8a;");

        loadDbRecords();
        return new VBox(4, pageTitle, dbTable, tableBtns, writeTitle, writeDesc, writeBtn, dbLog);
    }

    private void loadDbRecords() {
        new Thread(() -> {
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
            } catch (Exception e) {
                Platform.runLater(() -> dbLog.appendText("Query error: " + e.getMessage() + "\n"));
            }
            Platform.runLater(() -> dbTable.getItems().setAll(records));
        }).start();
    }

    private void showAddRecordDialog() {
        Dialog<String[]> dialog = new Dialog<>();
        dialog.setTitle("Add Record");
        dialog.setHeaderText("Insert into e_table");

        TextField nameField = new TextField();
        nameField.setPromptText("Name");
        TextField posField = new TextField();
        posField.setPromptText("Position (number)");
        TextField gameField = new TextField();
        gameField.setPromptText("Game Name");

        VBox content = new VBox(8, nameField, posField, gameField);
        content.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(bt -> bt == ButtonType.OK ? new String[]{nameField.getText(), posField.getText(), gameField.getText()} : null);

        dialog.showAndWait().ifPresent(result -> {
            if (result[0].isEmpty() || result[2].isEmpty()) return;
            new Thread(() -> {
                try (Connection c = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                     PreparedStatement ps = c.prepareStatement("INSERT INTO e_table(name, position, game_name) VALUES (?, ?, ?)")) {
                    ps.setString(1, result[0]);
                    ps.setInt(2, result[1].isEmpty() ? 0 : Integer.parseInt(result[1]));
                    ps.setString(3, result[2]);
                    ps.executeUpdate();
                    Platform.runLater(() -> {
                        dbLog.appendText("Record added: " + result[0] + "\n");
                        loadDbRecords();
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> dbLog.appendText("Add error: " + e.getMessage() + "\n"));
                }
            }).start();
        });
    }

    private void deleteSelectedRecord() {
        Map<String, Object> selected = dbTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        int id = (int) selected.get("id");

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Delete record #" + id + "?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.YES) {
                new Thread(() -> {
                    try (Connection c = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                         PreparedStatement ps = c.prepareStatement("DELETE FROM e_table WHERE id = ?")) {
                        ps.setInt(1, id);
                        ps.executeUpdate();
                        Platform.runLater(() -> {
                            dbLog.appendText("Deleted record #" + id + "\n");
                            loadDbRecords();
                        });
                    } catch (Exception e) {
                        Platform.runLater(() -> dbLog.appendText("Delete error: " + e.getMessage() + "\n"));
                    }
                }).start();
            }
        });
    }

    private void doWriteOp() {
        new Thread(() -> {
            try {
                WriteFile wf = new WriteFile();
                wf.writeFunction();
                Platform.runLater(() -> dbLog.appendText("WriteOperation completed successfully\n"));
            } catch (Exception e) {
                Platform.runLater(() -> dbLog.appendText("WriteOperation error: " + e.getMessage() + "\n"));
            }
        }).start();
    }

    // ==================== FILES ====================

    private TextArea fileReadOutput;
    private TextArea fileMoveOutput;
    private TextArea fileDeleteOutput;

    private VBox buildFilesPage() {
        Label pageTitle = new Label("File Manager");
        pageTitle.setStyle("-fx-text-fill: #00ffff; -fx-font-size: 20px; -fx-font-weight: bold;");

        // --- Read File ---
        Label readTitle = new Label("Read File");
        readTitle.setStyle("-fx-text-fill: #00ffff; -fx-font-size: 13px; -fx-font-weight: bold;");
        TextField readPath = new TextField();
        readPath.setPromptText("D:\\桌面\\NameCodeForE_Plant.txt");
        HBox.setHgrow(readPath, Priority.ALWAYS);
        Button readBtn = makeBtn("Read", "btn btn-sm");
        readBtn.setOnAction(e -> doReadFile(readPath.getText()));
        HBox readRow = new HBox(8, readPath, readBtn);
        fileReadOutput = createOutputArea(120);

        // --- Move File ---
        Label moveTitle = new Label("Move File");
        moveTitle.setStyle("-fx-text-fill: #00ffff; -fx-font-size: 13px; -fx-font-weight: bold;");
        moveTitle.setPadding(new Insets(12, 0, 0, 0));
        TextField moveSrc = new TextField();
        moveSrc.setPromptText("Source path");
        TextField moveDst = new TextField();
        moveDst.setPromptText("Target path");
        HBox.setHgrow(moveSrc, Priority.ALWAYS);
        HBox.setHgrow(moveDst, Priority.ALWAYS);
        Button moveBtn = makeBtn("Move", "btn btn-sm btn-purple");
        moveBtn.setOnAction(e -> doMoveFile(moveSrc.getText(), moveDst.getText()));
        HBox moveBtnRow = new HBox(8, moveBtn);
        moveBtnRow.setPadding(new Insets(6, 0, 0, 0));
        fileMoveOutput = createOutputArea(60);

        // --- Delete File ---
        Label delTitle = new Label("Delete File (Recycle Bin)");
        delTitle.setStyle("-fx-text-fill: #ff3366; -fx-font-size: 13px; -fx-font-weight: bold;");
        delTitle.setPadding(new Insets(12, 0, 0, 0));
        TextField delPath = new TextField();
        delPath.setPromptText("File path to delete");
        HBox.setHgrow(delPath, Priority.ALWAYS);
        Button delBtn = makeBtn("Delete", "btn btn-sm btn-red");
        delBtn.setOnAction(e -> doDeleteFile(delPath.getText()));
        HBox delRow = new HBox(8, delPath, delBtn);
        fileDeleteOutput = createOutputArea(60);

        return new VBox(4, pageTitle,
                readTitle, readRow, fileReadOutput,
                moveTitle, new HBox(8, moveSrc, moveDst), moveBtnRow, fileMoveOutput,
                delTitle, delRow, fileDeleteOutput);
    }

    private TextArea createOutputArea(int prefHeight) {
        TextArea area = new TextArea();
        area.setEditable(false);
        area.setPrefHeight(prefHeight);
        area.setStyle("-fx-control-inner-background: #060610; -fx-text-fill: #6a6a8a;");
        return area;
    }

    private void doReadFile(String path) {
        if (path == null || path.isBlank()) { fileReadOutput.setText("Enter a file path"); return; }
        new Thread(() -> {
            try {
                String content = Files.readString(Path.of(path));
                Platform.runLater(() -> fileReadOutput.setText(content));
            } catch (Exception e) {
                Platform.runLater(() -> fileReadOutput.setText("Error: " + e.getMessage()));
            }
        }).start();
    }

    private void doMoveFile(String src, String dst) {
        if (src == null || src.isBlank() || dst == null || dst.isBlank()) {
            fileMoveOutput.setText("Fill both paths"); return;
        }
        new Thread(() -> {
            try {
                TransferPathOfSingleFile t = new TransferPathOfSingleFile();
                t.transferFile(src, dst);
                Platform.runLater(() -> fileMoveOutput.setText("Moved to: " + dst));
            } catch (Exception e) {
                Platform.runLater(() -> fileMoveOutput.setText("Error: " + e.getMessage()));
            }
        }).start();
    }

    private void doDeleteFile(String path) {
        if (path == null || path.isBlank()) { fileDeleteOutput.setText("Enter a file path"); return; }
        new Thread(() -> {
            try {
                DeleteSingleFile d = new DeleteSingleFile();
                d.delete(path);
                Platform.runLater(() -> fileDeleteOutput.setText("Sent to recycle bin: " + path));
            } catch (Exception e) {
                Platform.runLater(() -> fileDeleteOutput.setText("Error: " + e.getMessage()));
            }
        }).start();
    }

    // ==================== CHARACTER ART ====================

    private TextArea artOutput;

    private VBox buildArtPage() {
        Label pageTitle = new Label("Character Art Generator");
        pageTitle.setStyle("-fx-text-fill: #00ffff; -fx-font-size: 20px; -fx-font-weight: bold;");

        Label desc = new Label("Convert Chinese text to ASCII art");
        desc.setStyle("-fx-text-fill: #5a5a8a; -fx-font-size: 11px;");
        desc.setPadding(new Insets(0, 0, 12, 0));

        TextField input = new TextField();
        input.setText("电竞之王");
        input.setPromptText("Enter text");
        HBox.setHgrow(input, Priority.ALWAYS);

        Button genBtn = makeBtn("Generate", "btn btn-purple");
        genBtn.setOnAction(e -> doGenArt(input.getText()));
        HBox row = new HBox(8, input, genBtn);

        artOutput = new TextArea();
        artOutput.setEditable(false);
        artOutput.setPrefHeight(400);
        artOutput.setStyle("-fx-control-inner-background: #060610; -fx-text-fill: #00ffff; -fx-font-family: 'Consolas'; -fx-font-size: 8px;");
        VBox.setMargin(artOutput, new Insets(12, 0, 0, 0));

        return new VBox(4, pageTitle, desc, row, artOutput);
    }

    private void doGenArt(String text) {
        if (text == null || text.isBlank()) { artOutput.setText("Enter text"); return; }
        new Thread(() -> {
            try {
                WriteFile wf = new WriteFile();
                String art = wf.drawChineseCharacter(text);
                Platform.runLater(() -> artOutput.setText(art));
            } catch (Exception e) {
                Platform.runLater(() -> artOutput.setText("Error: " + e.getMessage()));
            }
        }).start();
    }

    // ==================== SYSTEM INFO ====================

    private VBox buildSysInfoPage() {
        Label pageTitle = new Label("System Info");
        pageTitle.setStyle("-fx-text-fill: #00ffff; -fx-font-size: 20px; -fx-font-weight: bold;");

        Runtime rt = Runtime.getRuntime();
        long usedMem = rt.totalMemory() - rt.freeMemory();
        RuntimeMXBean mx = ManagementFactory.getRuntimeMXBean();
        long ms = mx.getUptime();

        boolean dbOk = false;
        int recordCount = 0;
        try (Connection c = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            dbOk = true;
            try (PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM e_table");
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) recordCount = rs.getInt(1);
            }
        } catch (Exception ignored) {}

        String[][] items = {
            {"Java Version", System.getProperty("java.version")},
            {"OS Name", System.getProperty("os.name")},
            {"OS Arch", System.getProperty("os.arch")},
            {"CPU Cores", String.valueOf(rt.availableProcessors())},
            {"Max Memory", String.format("%.0f MB", rt.maxMemory() / 1048576.0)},
            {"Used Memory", String.format("%.0f MB", usedMem / 1048576.0)},
            {"Free Memory", String.format("%.0f MB", rt.freeMemory() / 1048576.0)},
            {"Total Memory", String.format("%.0f MB", rt.totalMemory() / 1048576.0)},
            {"Uptime", String.format("%dh %dm %ds", ms / 3600000, (ms % 3600000) / 60000, (ms % 60000) / 1000)},
            {"Server Port", "8082"},
            {"DB Status", dbOk ? "Connected" : "Disconnected"},
            {"DB Records", String.valueOf(recordCount)},
            {"DB URL", "localhost:3306/E_Plant"},
            {"Project Dir", System.getProperty("user.dir")},
        };

        return new VBox(4, pageTitle, buildInfoGrid(items));
    }

    private GridPane buildInfoGrid(String[][] items) {
        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(2);
        grid.setPadding(new Insets(8, 0, 0, 0));
        for (int i = 0; i < items.length; i++) {
            Label k = new Label(items[i][0]);
            k.setStyle("-fx-text-fill: #5a5a8a; -fx-font-size: 12px;");
            k.setMinWidth(140);
            Label v = new Label(items[i][1]);
            v.setStyle("-fx-text-fill: #e0e0f0; -fx-font-size: 12px;");
            grid.add(k, 0, i);
            grid.add(v, 1, i);
        }
        return grid;
    }

    // ==================== LOGS ====================

    private TextArea logArea;

    private VBox buildLogsPage() {
        Label pageTitle = new Label("Logs");
        pageTitle.setStyle("-fx-text-fill: #00ffff; -fx-font-size: 20px; -fx-font-weight: bold;");

        Button refreshBtn = makeBtn("Refresh", "btn btn-sm");
        refreshBtn.setOnAction(e -> loadLogs());
        Button clearBtn = makeBtn("Clear", "btn btn-sm btn-red");
        clearBtn.setOnAction(e -> logArea.clear());
        HBox btns = new HBox(8, refreshBtn, clearBtn);

        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setStyle("-fx-control-inner-background: #060610; -fx-text-fill: #6a6a8a;");
        VBox.setVgrow(logArea, Priority.ALWAYS);

        loadLogs();
        return new VBox(8, pageTitle, btns, logArea);
    }

    private void loadLogs() {
        new Thread(() -> {
            try {
                String logFile = "logs/spring.log";
                Path logPath = Path.of(logFile);
                if (Files.exists(logPath)) {
                    List<String> lines = Files.readAllLines(logPath);
                    int start = Math.max(0, lines.size() - 200);
                    String content = String.join("\n", lines.subList(start, lines.size()));
                    Platform.runLater(() -> logArea.setText(content));
                } else {
                    Platform.runLater(() -> logArea.setText("No log file found at " + logFile));
                }
            } catch (Exception e) {
                Platform.runLater(() -> logArea.setText("Error loading logs: " + e.getMessage()));
            }
        }).start();
    }

    // ==================== HELPERS ====================

    private Button makeBtn(String text, String styleClass) {
        Button btn = new Button(text);
        for (String s : styleClass.split(" ")) btn.getStyleClass().add(s);
        return btn;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
