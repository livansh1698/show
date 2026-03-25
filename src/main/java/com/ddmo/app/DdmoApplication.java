package com.ddmo.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

/**
 * SpringBoot 应用。
 * 在后台线程中启动，通过 CompletableFuture 传递实际端口号。
 */
@SpringBootApplication
public class DdmoApplication {

    private static final CompletableFuture<Integer> portFuture = new CompletableFuture<>();
    private static ConfigurableApplicationContext context;

    public static void startInBackground(String[] args) {
        Thread springThread = new Thread(() -> {
            try {
                ensureSqliteDir();
                context = SpringApplication.run(DdmoApplication.class, args);
                Integer port = context.getEnvironment().getProperty("local.server.port", Integer.class);
                portFuture.complete(port);
            } catch (Exception e) {
                portFuture.completeExceptionally(e);
            }
        }, "spring-boot-thread");
        springThread.setDaemon(true);
        springThread.start();
    }

    public static CompletableFuture<Integer> getPortFuture() {
        return portFuture;
    }

    public static void shutdown() {
        if (context != null) {
            context.close();
        }
    }

    private static void ensureSqliteDir() {
        try {
            Path dir = Path.of(System.getProperty("user.home"), ".show");
            Files.createDirectories(dir);
        } catch (Exception e) {
            throw new IllegalStateException("创建SQLite目录失败", e);
        }
    }
}
