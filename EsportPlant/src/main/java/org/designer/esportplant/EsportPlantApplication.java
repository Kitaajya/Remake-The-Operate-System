package org.designer.esportplant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

@SpringBootApplication
public class EsportPlantApplication {

    private static final Logger log = LoggerFactory.getLogger(EsportPlantApplication.class);

    public static void main(String[] args) throws IOException {


        try{
            SpringApplication.run(EsportPlantApplication.class, args);
            WriteFile writeFile = new WriteFile();
            writeFile.writeFunction();
        }catch (Exception e){
            log.error("方法有异常！");
        }
    }

}