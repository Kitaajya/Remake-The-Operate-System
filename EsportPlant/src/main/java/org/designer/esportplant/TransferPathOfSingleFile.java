package org.designer.esportplant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class TransferPathOfSingleFile {
    private static final Logger log = LoggerFactory.getLogger(TransferPathOfSingleFile.class);
    private final Scanner scanner = new Scanner(System.in);
    //参数：(原始路径，新路径)
    void transferFile(String originalPath, String newPath) throws IOException {
        // 验证源文件存在
        File sourceFile = new File(originalPath);
        if (!sourceFile.exists())
            throw new FileNotFoundException("源文件不存在: " + originalPath);
        // 显示文件内容
        log.info("当前文件内容：");
        try (BufferedReader reader = new BufferedReader(new FileReader(originalPath))) {
            String line;
            while ((line = reader.readLine()) != null) log.info(line);
        }

        // 确认操作
        log.info("是否要将文件从 {} 移动到 {}？(Y/N)", originalPath, newPath);
        String choice = scanner.nextLine().trim();

        if (choice.equalsIgnoreCase("N")) log.info("操作已取消");
        else if (choice.equalsIgnoreCase("Y")) {
            // 执行文件移动
            Path source = Paths.get(originalPath);
            Path target = Paths.get(newPath);

            // 确保目标目录存在
            Files.createDirectories(target.getParent());
            // 移动文件（如果目标已存在则覆盖）
            Files.move(source, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            log.info("文件已成功移动到: {}", newPath);
        }else throw new IllegalArgumentException("请输入 Y 或 N");
        scanner.close();
    }
}