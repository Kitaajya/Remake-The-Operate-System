package org.designer.esportplant;

import java.io.File;
import java.io.IOException;

public class DeleteSingleFile {

    public void delete(String deletingFilePath) throws IOException {
        File file = new File(deletingFilePath);
        if (!file.exists()) throw new IOException("文件不存在: " + deletingFilePath);

        try {
            boolean success = java.awt.Desktop.getDesktop().moveToTrash(file);
            if (!success) throw new IOException("移入回收站失败");
        } catch (Exception e) {
            String escapedPath = deletingFilePath.replace("'", "''");
            String ps = "Add-Type -AssemblyName Microsoft.VisualBasic; "
                    + "[Microsoft.VisualBasic.FileIO.FileSystem]::DeleteFile("
                    + "'" + escapedPath + "', 'OnlyErrorDialogs', 'SendToRecycleBin')";
            ProcessBuilder pb = new ProcessBuilder("powershell.exe", "-NoProfile", "-Command", ps);
            pb.redirectErrorStream(true);
            Process process = pb.start();
            String output = new String(process.getInputStream().readAllBytes());
            int exitCode = 0;
            try {
                exitCode = process.waitFor();
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                throw new IOException("操作被中断", ie);
            }
            if (exitCode != 0) {
                throw new IOException("移入回收站失败: " + output.trim());
            }
        }
    }
}