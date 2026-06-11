package it.kimia;

import it.kimia.db.Database;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class KimiaWebApplication {
    public static void main(String[] args) throws Exception {
        Database.init();
        SpringApplication.run(KimiaWebApplication.class, args);
    }
}
