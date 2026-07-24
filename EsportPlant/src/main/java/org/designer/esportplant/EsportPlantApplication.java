package org.designer.esportplant;

import ChooseProgrammer.ChooseProgrammerToUseMySystemOperation;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EsportPlantApplication {

    public static void main(String[] args) {
        SpringApplication.run(EsportPlantApplication.class, args);
        ChooseProgrammerToUseMySystemOperation.choice();
        /*
        * 前端主页	http://localhost:8082 (http://localhost:8082)
          后台管理	http://localhost:8082/admin.html (http://localhost:8082/admin.html)
        * */
    }

}