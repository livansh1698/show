package com.ddmo.app;

import javafx.application.Application;

/**
 * JVM 入口点。
 * 不继承 Application，绕过 JavaFX 模块系统检查。
 */
public class DdmoLauncher {

    public static void main(String[] args) {
        Application.launch(MainWindow.class, args);
    }
}
