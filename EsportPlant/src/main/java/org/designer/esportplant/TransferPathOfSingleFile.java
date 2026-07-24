package org.designer.esportplant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TransferPathOfSingleFile {
    private static final Logger log = LoggerFactory.getLogger(TransferPathOfSingleFile.class);

    public void transferFile(String originalPath, String newPath) throws IOException {
        File sourceFile = new File(originalPath);
        if (!sourceFile.exists())
            throw new FileNotFoundException("源文件不存在: " + originalPath);
        log.info("当前文件内容：");
        try (BufferedReader reader = new BufferedReader(new FileReader(originalPath))) {
            String line;
            while ((line = reader.readLine()) != null) log.info(line);
        }
        Path source = Paths.get(originalPath);
        Path target = Paths.get(newPath);
        Files.createDirectories(target.getParent());
        Files.move(source, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        log.info("文件已成功移动到: {}", newPath);
    }
}