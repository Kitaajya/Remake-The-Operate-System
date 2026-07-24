package ChooseProgrammer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.designer.esportplant.DeleteSingleFile;
import org.designer.esportplant.ReadFiles;
import org.designer.esportplant.TransferPathOfSingleFile;
import org.designer.esportplant.WriteFile;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ChooseProgrammerToUseMySystemOperation {
    private static final Logger log=LoggerFactory.getLogger(ChooseProgrammerToUseMySystemOperation.class);
    public static String modifyPath(String originalPath){
        List<Character> store=new ArrayList<>();
        for(int i=0;i<originalPath.length();i++){
            if (originalPath.charAt(i) == '"') continue;
            store.add(originalPath.charAt(i));
        }
        StringBuffer s=new StringBuffer();
        for(char c:store) s.append(c);
        return s.toString();
    }
    public static void choice() {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            log.info("\n========== 功能选择菜单 ==========");
            log.info("1. 写入文件并操作数据库 (WriteFile)");
            log.info("2. 读取文件内容 (ReadFiles)");
            log.info("3. 移动单个文件 (TransferPathOfSingleFile)");
            log.info("4. 删除单个文件到回收站 (DeleteSingleFile)");
            log.info("5. 绘制中文字符画 (drawChineseCharacter)");
            log.info("0. 退出程序");
            log.info("===================================");
            log.info("请选择功能 (0-5): ");

            String choice = scanner.nextLine().trim();

            try {
                switch (choice) {
                    case "1":
                        // 写入文件并操作数据库
                        WriteFile writeFile = new WriteFile();
                        writeFile.writeFunction();
                        log.info("写入文件操作完成！");
                        break;

                    case "2":
                        // 读取文件
                        ReadFiles readFiles = new ReadFiles();
                        System.out.print("请输入要读取的文件路径 (直接回车使用默认路径): ");
                        String readPath = scanner.nextLine().trim();

                        /**修复文件路径，去掉双引号**/
                        String modifiedFilePath;
                        if (readPath.isEmpty()) readPath = "D:\\桌面\\NameCodeForE_Plant.txt";
                        modifiedFilePath=modifyPath(readPath);
                        readFiles.readFunction(modifiedFilePath);
                        log.info("读取文件操作完成！");
                        break;

                    case "3":
                        // 移动文件
                        TransferPathOfSingleFile transfer = new TransferPathOfSingleFile();
                        log.info("请输入源文件路径: ");
                        String originalPath = scanner.nextLine().trim();
                        String modifiedOriginalPath=modifyPath(originalPath);
                        log.info("请输入目标文件路径: ");
                        String newPath = scanner.nextLine().trim();
                        String modifiedNewPath=modifyPath(newPath);
                        if (originalPath.isEmpty() || newPath.isEmpty()) {
                            log.error("源文件路径和目标文件路径不能为空！");
                            break;
                        }
                        transfer.transferFile(modifiedOriginalPath, modifiedNewPath);
                        log.info("文件移动操作完成！");
                        break;

                    case "4":
                        // 删除文件到回收站
                        DeleteSingleFile delete = new DeleteSingleFile();
                        System.out.print("请输入要删除的文件路径: ");
                        String deletePath = scanner.nextLine().trim();
                        String modifiedDeletePath=modifyPath(deletePath);
                        if (deletePath.isEmpty()) {
                            log.error("文件路径不能为空！");
                            break;
                        }
                        delete.delete(modifiedDeletePath);
                        //"D:\桌面\willBeDeleted.txt"
                        log.info("文件已成功移入回收站！");
                        break;

                    case "5":
                        // 绘制中文字符画
                        WriteFile writeFile2 = new WriteFile();
                        System.out.print("请输入要绘制的文字: ");
                        String text = scanner.nextLine().trim();
                        if (text.isEmpty()) {
                            text = "唐山神医";
                        }
                        String result = writeFile2.drawChineseCharacter(text);
                        System.out.println("\n字符画结果：");
                        System.out.println(result);
                        log.info("字符画绘制完成！");
                        break;

                    case "0":
                        log.info("程序已退出！");
                        running = false;
                        break;

                    default:
                        log.warn("无效的选择，请输入 0-5 之间的数字！");
                        break;
                }
            } catch (IOException e) {
                log.error("I/O 操作异常: {}", e.getMessage());
            } catch (SQLException e) {
                log.error("数据库操作异常: {}", e.getMessage());
            } catch (Exception e) {
                log.error("操作异常: {}", e.getMessage());
            }
        }

        scanner.close();
    }
}
