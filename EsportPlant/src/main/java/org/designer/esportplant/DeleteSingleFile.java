package org.designer.esportplant;

import java.io.File;
import java.io.IOException;

public class DeleteSingleFile {

    public void delete(String deletingFilePath) throws IOException {
        File file = new File(deletingFilePath);
        if (!file.exists()) throw new IOException("文件不存在: " + deletingFilePath);
        boolean success = java.awt.Desktop.getDesktop().moveToTrash(file);
        if (!success) throw new IOException("移入回收站失败，可能文件不在文件系统中或回收站不可用");
    }
}