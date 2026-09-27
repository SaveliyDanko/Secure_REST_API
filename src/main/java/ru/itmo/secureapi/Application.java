package ru.itmo.secureapi;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    ApplicationRunner initialize(JdbcTemplate jdbc, PasswordEncoder encoder) {
        return args -> {
            String password = System.getenv("APP_PASSWORD");
            if (password == null || password.isBlank()) {
                throw new IllegalStateException("APP_PASSWORD must be set");
            }
            String username = System.getenv().getOrDefault("APP_USERNAME", "demo");
            jdbc.update("INSERT INTO app_user (username, password_hash) VALUES (?, ?)", username, encoder.encode(password));
            jdbc.update("INSERT INTO data_item (text) VALUES (?)", "Welcome to the secure API");
        };
    }
}
