package org.jeecg;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


/**
 * @author zhoujf
 *
 */
@SpringBootApplication
@MapperScan("org.jeecg.modules.cas.db.mapper")
public class JeecgBootCasDbApplication {

    public static void main(String[] args) {
        SpringApplication.run(JeecgBootCasDbApplication.class, args);
    }

}
