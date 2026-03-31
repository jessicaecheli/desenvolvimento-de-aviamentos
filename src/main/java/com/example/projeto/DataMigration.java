package com.example.projeto;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DataMigration implements ApplicationRunner {

    private final JdbcTemplate jdbc;

    public DataMigration(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void run(ApplicationArguments args) {
        int updated = jdbc.update(
            "UPDATE desenvolvimento SET status = 'ORCAMENTO' WHERE status = 'NOVO'");
        if (updated > 0) {
            System.out.println("[DataMigration] " + updated + " registro(s) com status NOVO convertidos para ORCAMENTO.");
        }
    }
}
