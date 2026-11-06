package br.app.coeur;

import org.flywaydb.core.Flyway;

public class FlywayRepair {

    public static void main(String[] args) {
        Flyway flyway = Flyway.configure()
                .dataSource(
                        System.getenv("DB_URL"),
                        System.getenv("DB_USERNAME"),
                        System.getenv("DB_PASSWORD")
                )
                .load();

        flyway.repair();

        System.out.println("Flyway repair executado!");
    }
}