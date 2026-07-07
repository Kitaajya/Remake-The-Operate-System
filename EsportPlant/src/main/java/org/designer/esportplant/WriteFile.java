package org.designer.esportplant;

import com.github.lalyos.jfiglet.FigletFont;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;

public class WriteFile {
    private static final String URL = "jdbc:mysql://localhost:3306/E_Plant?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String userName="root";
    private static final String password="123456";
    private static final Logger log = LoggerFactory.getLogger(WriteFile.class);
    String path="D:\\桌面\\NameCodeForE_Plant.txt";


    FileWriter fileWriter=new FileWriter(path,true);
    BufferedWriter bufferedWriter=new BufferedWriter(fileWriter);

    public WriteFile() throws IOException {}
    Connection connection;
    PreparedStatement preparedStatement;
    ResultSet resultSet;

    private String drawChineseCharacter(String text) {
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

    void writeFunction() throws IOException, SQLException {
        try{

            String tangshanShenYi= FigletFont.convertOneLine("TangShan Greater Doctor");
            bufferedWriter.write(tangshanShenYi);
            log.info(tangshanShenYi);

            String tangshanPY = FigletFont.convertOneLine("TangShan ShenYi");
            bufferedWriter.write("\n" + tangshanPY);
            log.info(tangshanPY);

            String cnArt = drawChineseCharacter("唐山神医");
            bufferedWriter.write("\n" + cnArt);
            log.info(cnArt);

            String selectSQL="select * from e_table";


            connection=DriverManager.getConnection(URL,userName,password);
            preparedStatement=connection.prepareStatement(selectSQL);
            resultSet=preparedStatement.executeQuery();
            while (resultSet.next()){
                String name;
                int position;
                String gameName;
                name= resultSet.getString("name");
                position=resultSet.getInt("position");
                gameName=resultSet.getString("game_name");
                log.info("电竞平台人物信息：\n");
                log.info("姓名：{}\n", name);
                log.info("段位：{}\n", position);
                log.info("游戏名：{}\n",gameName);
                log.info("--------------------");

                bufferedWriter.write("姓名："+name+"\n"+ "段位："+position+"\n"+ "游戏名："+gameName+"\n");
            }
            bufferedWriter.close();
            resultSet.close();
            preparedStatement.close();
            connection.close();
        }catch (SQLException e){
            log.error("数据库连接错误,位于方法->writeFunction(){}",e.getMessage());
        }catch (IOException e){
            log.error("写入文件发生I/O流错误,位于方法->writeFunction(){}", e.getMessage());
        }
    }
}
