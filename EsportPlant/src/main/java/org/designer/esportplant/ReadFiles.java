package org.designer.esportplant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Scanner;

public class ReadFiles {

    private static final Logger log = LoggerFactory.getLogger(ReadFiles.class);

    void readFunction(String path) throws IOException {
        Scanner scanner=new Scanner(System.in);
        path = scanner.next().trim();//String path="D:\\桌面\\NameCodeForE_Plant.txt";

        FileReader fileReader=new FileReader(path);
        BufferedReader bufferedReader=new BufferedReader(fileReader);

        String s;
        while ((s=bufferedReader.readLine())!=null) log.info("{}\n", s);
        scanner.close();
    }

}
