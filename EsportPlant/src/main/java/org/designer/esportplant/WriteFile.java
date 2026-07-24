package org.designer.esportplant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;

public class WriteFile {
    private static final String URL = "jdbc:mysql://localhost:3306/E_Plant?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&useUnicode=true&characterEncoding=UTF-8";
    private static final String userName="root";
    private static final String password="123456";
    private static final Logger log = LoggerFactory.getLogger(WriteFile.class);
    String path="D:\\桌面\\NameCodeForE_Plant.txt";


    FileWriter fileWriter=new FileWriter(path);
    BufferedWriter bufferedWriter=new BufferedWriter(fileWriter);

    public WriteFile() throws IOException {}
    Connection connection;
    PreparedStatement preparedStatement;
    ResultSet resultSet;

    public String drawChineseCharacter(String text) {
        Font font = findChineseFont(150);
        BufferedImage tmp = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = tmp.createGraphics();
        g.setFont(font);
        FontMetrics fm = g.getFontMetrics();
        int imgW = fm.stringWidth(text);
        int imgH = fm.getHeight();
        int ascent = fm.getAscent();
        g.dispose();

        BufferedImage img = new BufferedImage(imgW, imgH, BufferedImage.TYPE_INT_RGB);
        g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, imgW, imgH);
        g.setColor(Color.BLACK);
        g.setFont(font);
        g.drawString(text, 0, ascent);
        g.dispose();

        StringBuilder sb = new StringBuilder();
        for (int y = 3; y < imgH - 3; y += 8) {
            for (int x = 3; x < imgW - 3; x += 6) {
                int darkPixels = 0;
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dx = -1; dx <= 1; dx++) {
                        Color c = new Color(img.getRGB(x + dx, y + dy));
                        if (c.getRed() < 128) darkPixels++;
                    }
                }
                sb.append(darkPixels >= 5 ? "█" : " ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    private Font findChineseFont(int size) {
        String[] chineseFonts = {"Microsoft YaHei", "SimSun", "SimHei", "KaiTi", "FangSong", "STSong", "Dialog"};
        for (String name : chineseFonts) {
            Font f = new Font(name, Font.PLAIN, size);
            if (f.canDisplay('唐')) return f;
        }
        return new Font("Dialog", Font.PLAIN, size);
    }

    public void writeFunction() throws IOException, SQLException {
        try{

            String selectSQL = "select * from e_table";
            String insertSQL = "insert into e_table(name,position,game_name) values (?,?,?)";

            connection = DriverManager.getConnection(URL, userName, password);

            //查询数据
            preparedStatement = connection.prepareStatement(selectSQL);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String name = resultSet.getString("name");
                int position = resultSet.getInt("position");
                String gameName = resultSet.getString("game_name");
                log.info("电竞平台人物信息：\n");
                log.info("姓名：{}\n", name);
                log.info("段位：{}\n", position);
                log.info("游戏名：{}\n", gameName);
                log.info("--------------------");

                bufferedWriter.write("姓名：" + name + "\n" + "段位：" + position + "\n" + "游戏名：" + gameName + "\n");

                //插入数据
                PreparedStatement insertStmt = connection.prepareStatement(insertSQL);

                String insertName="端木生治";
                insertStmt.setString(1, insertName);

                int insertPosition =1;
                insertStmt.setInt(2, insertPosition);

                String insertGameName="潮州神医";
                insertStmt.setString(3, insertGameName);
                insertStmt.executeUpdate();
                insertStmt.close();
            }
            resultSet.close();


        }catch (SQLException e){
            log.error("数据库连接错误,位于方法->writeFunction(){}",e.getMessage());
        }catch (IOException e){
            log.error("写入文件发生I/O流错误,位于方法->writeFunction(){}", e.getMessage());
        }finally {
            bufferedWriter.close();
            if (resultSet != null) resultSet.close();
            if (preparedStatement != null) preparedStatement.close();
            if (connection != null) connection.close();
            fileWriter.close();
        }
    }
}
