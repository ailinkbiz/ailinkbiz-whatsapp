package com.br.ailinkbiz.config;

import jakarta.annotation.PostConstruct;
import javax.sql.DataSource;
import org.springframework.stereotype.Component;

@Component
public class DatasourceHealthCheck {

    private final DataSource dataSource;

    public DatasourceHealthCheck(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @PostConstruct
    public void check() throws Exception {
        try (var conn = dataSource.getConnection()) {
            System.out.println("✅ Supabase conectado: " + conn.getMetaData().getURL());
        }
    }

}