package org.designer.esportplant;

import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public class ReadFiles {

    private static final Logger log = LoggerFactory.getLogger(ReadFiles.class);

    public void readFunction(String path) throws IOException {
        if (path == null || path.trim().isEmpty()) {
            throw new IOException("文件路径不能为空");
        }

        File file = new File(path);
        if (!file.exists()) {
            log.error("文件不存在: {}", path);
            return;
        }

        String lowerPath = path.toLowerCase();

        // 根据文件扩展名选择不同的读取方式
        if (lowerPath.endsWith(".docx")) {
            readWordDocument(path);
        } else if (lowerPath.endsWith(".doc")) {
            log.warn(".doc 格式需要使用 POI-HSDF 库读取，建议转换为 .docx 格式");
            // 简单处理，尝试以文本方式读取
            readTextFile(path);
        } else {
            // 默认作为文本文件读取
            readTextFile(path);
        }
    }

    /**
     * 读取文本文件
     */
    private void readTextFile(String path) throws IOException {
        log.info("开始读取文本文件: {}", path);
        int lineNum = 0;
        int charCount = 0;

        try (FileReader fileReader = new FileReader(path);
             BufferedReader bufferedReader = new BufferedReader(fileReader)) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                lineNum++;
                charCount += line.length();
                // 只显示前50行，避免输出过多
                if (lineNum <= 50) {
                    log.info("第{}行: {}", lineNum, line);
                }
            }
            log.info("文件读取完成！总行数: {}, 总字符数: {}", lineNum, charCount);
            if (lineNum > 50) {
                log.info("(仅显示前50行，共{}行)", lineNum);
            }
        }
    }

    /**
     * 读取 Word 文档 (.docx)
     */
    private void readWordDocument(String path) throws IOException {
        log.info("开始读取 Word 文档: {}", path);

        try (FileInputStream fis = new FileInputStream(path);
             XWPFDocument document = new XWPFDocument(fis);
             XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {

            String text = extractor.getText();
            if (text == null || text.trim().isEmpty()) {
                log.warn("文档内容为空");
                return;
            }

            String[] lines = text.split("\\r?\\n");
            int lineNum = 0;
            int charCount = 0;

            log.info("Word 文档内容：");
            log.info("========================================");

            for (String line : lines) {
                if (line.trim().isEmpty()) continue; // 跳过空行
                lineNum++;
                charCount += line.length();
                if (lineNum <= 100) { // 限制显示行数
                    log.info("{}", line);
                }
            }

            log.info("========================================");
            log.info("文档读取完成！有效行数: {}, 总字符数: {}", lineNum, charCount);
            if (lineNum > 100) {
                log.info("(仅显示前100行，共{}行)", lineNum);
            }

        } catch (Exception e) {
            log.error("读取 Word 文档失败: {}", e.getMessage());
            log.error("请检查文件是否损坏或格式是否正确");
            throw new IOException("无法读取 Word 文档", e);
        }
    }
}