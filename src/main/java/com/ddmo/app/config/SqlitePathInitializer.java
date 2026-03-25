package com.ddmo.app.config;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class SqlitePathInitializer {

    @PostConstruct
    public void init() {
        try {
            Path dir = Path.of(System.getProperty("user.home"), ".show");
            Files.createDirectories(dir);
        } catch (Exception e) {
            throw new IllegalStateException("创建SQLite目录失败", e);
        }
    }
}

