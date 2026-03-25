package com.ddmo.app;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.scene.Scene;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import netscape.javascript.JSObject;

/**
 * JavaFX 无边框窗口，内嵌 WebView 加载 Vue SPA。
 */
public class MainWindow extends Application {

    private Stage primaryStage;
    private double xOffset = 0;
    private double yOffset = 0;

    /**
     * Java-JavaScript Bridge，供前端调用窗口操作。
     * 必须保持强引用，避免被 GC 回收。
     */
    private JavaBridge javaBridge;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        this.javaBridge = new JavaBridge();

        // 启动 SpringBoot
        DdmoApplication.startInBackground(getParameters().getRaw().toArray(new String[0]));

        // 创建无边框透明窗口
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setTitle("Show");

        // 创建 WebView 并设置透明背景
        WebView webView = new WebView();
        webView.setContextMenuEnabled(false);
        WebEngine webEngine = webView.getEngine();

        // 页面加载完成后注入 JavaScript Bridge 以及设置背景透明 (JavaFX 17 兼容方案)
        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                JSObject window = (JSObject) webEngine.executeScript("window");
                window.setMember("ddmo", javaBridge);
                // 通知前端 bridge 已就绪
                webEngine.executeScript(
                    "if(window.onDdmoBridgeReady) window.onDdmoBridgeReady();"
                );

                // 设置 WebView 背景透明 (尝试多种方案以确保兼容性)
                try {
                    // 方案 1: 反射调用 page.setBackgroundColor
                    java.lang.reflect.Field field = webEngine.getClass().getDeclaredField("page");
                    field.setAccessible(true);
                    Object page = field.get(webEngine);
                    java.lang.reflect.Method setBackgroundColor = page.getClass().getDeclaredMethod("setBackgroundColor", int.class);
                    setBackgroundColor.setAccessible(true);
                    setBackgroundColor.invoke(page, 0); // 0 = transparent
                    
                    // 方案 2: 尝试直接调用 setPageFill (虽然 17 可能没有，但某些定制版本可能有)
                    try {
                        java.lang.reflect.Method setPageFill = webView.getClass().getMethod("setPageFill", javafx.scene.paint.Paint.class);
                        setPageFill.invoke(webView, Color.TRANSPARENT);
                    } catch (Exception e2) {}
                } catch (Exception e) {
                    // ignore
                }
            }
        });

        String splashHtml = "<!DOCTYPE html><html><head>" +
            "<style>" +
            "html, body { background: transparent !important; margin: 0; padding: 0; width: 100%; height: 100%; overflow: hidden; font-family: 'Microsoft YaHei', sans-serif; }" +
            ".splash-container { background-color:#0f172a; border-radius: 12px; width: 100%; height: 100%; display:flex; flex-direction:column; justify-content:center; align-items:center; position:absolute; top:0; left:0; z-index:9999; }" +
            ".ddmo-spinner{position:relative;width:120px;height:120px;display:flex;justify-content:center;align-items:center;}" +
            ".ddmo-spinner::before,.ddmo-spinner::after{content:'';position:absolute;border-radius:50%;}" +
            ".ddmo-spinner::before{width:100%;height:100%;box-shadow:inset 0 0 20px #8b5cf6,0 0 20px #3b82f6;animation:ddmo-pulse 2s linear infinite;}" +
            ".ddmo-spinner::after{width:80%;height:80%;border:3px solid transparent;border-top-color:#3b82f6;border-bottom-color:#8b5cf6;animation:ddmo-spin 1.5s cubic-bezier(0.68,-0.55,0.265,1.55) infinite;}" +
            ".ddmo-logo{font-weight:700;font-size:32px;color:#ffffff;text-shadow:0 0 10px #8b5cf6,0 0 20px #8b5cf6;letter-spacing:4px;}" +
            ".ddmo-text{margin-top:40px;color:#94a3b8;font-size:16px;letter-spacing:4px;animation:ddmo-fade 1.5s infinite alternate;}" +
            "@keyframes ddmo-pulse{0%{transform:scale(0.9);opacity:0.7}50%{transform:scale(1.1);opacity:1}100%{transform:scale(0.9);opacity:0.7}}" +
            "@keyframes ddmo-spin{100%{transform:rotate(360deg)}}" +
            "@keyframes ddmo-glow{from{text-shadow:0 0 10px #8b5cf6,0 0 20px #8b5cf6}to{text-shadow:0 0 20px #3b82f6,0 0 30px #3b82f6}}" +
            "@keyframes ddmo-fade{from{opacity:0.3}to{opacity:1}}" +
            "</style></head><body>" +
            "<div class='splash-container'><div class='ddmo-spinner'><span class='ddmo-logo'>Show</span></div><div class='ddmo-text'>咻，咻咻...</div></div></body></html>";
        
        // 在 SpringBoot 加载完成前，先展示极速启动页（Splash Screen），与前端 loader 保持完全一致
        webEngine.loadContent(splashHtml);

        // 等待 SpringBoot 启动完成后加载真实页面
        DdmoApplication.getPortFuture().thenAccept(port -> {
            Platform.runLater(() -> {
                webEngine.load("http://localhost:" + port);
            });
        });

        // 布局
        StackPane root = new StackPane(webView);

        // 窗口圆角半径：12px - 与前端 global.css 中的 --radius-window 保持一致
        double windowRadius = 12.0;

        // 设置圆角背景
        Background roundedBackground = new Background(
            new BackgroundFill(Color.WHITE, new CornerRadii(windowRadius), null)
        );
        root.setBackground(roundedBackground);

        // 裁剪 WebView 内容为圆角
        Rectangle clip = new Rectangle();
        clip.setArcWidth(windowRadius * 2);  // 圆角宽度 = 半径 × 2
        clip.setArcHeight(windowRadius * 2); // 圆角高度 = 半径 × 2
        clip.widthProperty().bind(root.widthProperty());
        clip.heightProperty().bind(root.heightProperty());
        root.setClip(clip);

        Scene scene = new Scene(root, 1200, 800);
        scene.setFill(Color.TRANSPARENT);

        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
    }

    @Override
    public void stop() {
        DdmoApplication.shutdown();
        Platform.exit();
        System.exit(0);
    }

    /**
     * JavaScript Bridge — 暴露给前端的窗口操作接口。
     * 前端通过 window.ddmo.xxx() 调用。
     */
    public class JavaBridge {

        /** 最小化窗口 */
        public void minimize() {
            Platform.runLater(() -> primaryStage.setIconified(true));
        }

        /** 最大化 / 还原窗口 */
        public void maximize() {
            Platform.runLater(() -> primaryStage.setMaximized(!primaryStage.isMaximized()));
        }

        /** 关闭窗口 */
        public void close() {
            Platform.runLater(() -> {
                primaryStage.close();
                DdmoApplication.shutdown();
                Platform.exit();
                System.exit(0);
            });
        }

        /** 开始拖动 — 记录鼠标偏移 */
        public void startDrag(double screenX, double screenY) {
            Platform.runLater(() -> {
                // 如果窗口是最大化的，先还原
                if (primaryStage.isMaximized()) {
                    primaryStage.setMaximized(false);
                    // 将窗口中心移到鼠标位置
                    primaryStage.setX(screenX - primaryStage.getWidth() / 2);
                    primaryStage.setY(screenY - 20);
                }
                xOffset = screenX - primaryStage.getX();
                yOffset = screenY - primaryStage.getY();
            });
        }

        /** 拖动中 — 移动窗口 */
        public void drag(double screenX, double screenY) {
            Platform.runLater(() -> {
                primaryStage.setX(screenX - xOffset);
                primaryStage.setY(screenY - yOffset);
            });
        }

        /** 获取最大化状态 */
        public boolean isMaximized() {
            return primaryStage.isMaximized();
        }
    }
}
