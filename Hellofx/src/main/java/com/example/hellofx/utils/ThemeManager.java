package com.example.hellofx.utils;

import javafx.scene.Scene;

public class ThemeManager {
    private static final String LIGHT_THEME = """
            .root {
                -fx-background-color: #f8f8f8;
                -fx-text-fill: #000000;
            }
            .button {
                -fx-background-color: #4CAF50;
                -fx-text-fill: white;
                -fx-background-radius: 5;
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 2);
            }
            .button:hover {
                -fx-background-color: #45a049;
            }
            .text-field {
                -fx-background-color: white;
                -fx-text-fill: black;
                -fx-background-radius: 5;
                -fx-border-color: #e0e0e0;
                -fx-border-radius: 5;
            }
            .label {
                -fx-text-fill: #333333;
            }
            .chart {
                -fx-background-color: white;
            }
            .chart-plot-background {
                -fx-background-color: #ffffff;
            }
            .table-view {
                -fx-background-color: white;
                -fx-text-fill: black;
            }
            .table-row-cell {
                -fx-background-color: white;
                -fx-text-fill: black;
            }
            .table-row-cell:odd {
                -fx-background-color: #f5f5f5;
            }
            .scroll-pane {
                -fx-background-color: white;
            }
            """;

    private static final String DARK_THEME = """
            .root {
                -fx-background-color: #2b2b2b;
                -fx-text-fill: #ffffff;
            }
            .button {
                -fx-background-color: #6b8e23;
                -fx-text-fill: white;
                -fx-background-radius: 5;
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 10, 0, 0, 2);
            }
            .button:hover {
                -fx-background-color: #5c7a1f;
            }
            .text-field {
                -fx-background-color: #3c3f41;
                -fx-text-fill: white;
                -fx-background-radius: 5;
                -fx-border-color: #555555;
                -fx-border-radius: 5;
            }
            .label {
                -fx-text-fill: #e0e0e0;
            }
            .chart {
                -fx-background-color: #2b2b2b;
            }
            .chart-plot-background {
                -fx-background-color: #3c3f41;
            }
            .chart-series-line {
                -fx-stroke: #6b8e23;
            }
            .table-view {
                -fx-background-color: #2b2b2b;
                -fx-text-fill: white;
            }
            .table-row-cell {
                -fx-background-color: #2b2b2b;
                -fx-text-fill: white;
            }
            .table-row-cell:odd {
                -fx-background-color: #353535;
            }
            .scroll-pane {
                -fx-background-color: #2b2b2b;
            }
            """;

    private static boolean isDarkMode = false;
    private static Scene currentScene = null;

    public static void toggleTheme(Scene scene) {
        isDarkMode = !isDarkMode;
        currentScene = scene;
        applyTheme(scene);
    }

    public static void applyTheme(Scene scene) {
        if (scene == null) return;
        currentScene = scene;
        scene.getStylesheets().clear();
        scene.getRoot().setStyle(isDarkMode ? DARK_THEME : LIGHT_THEME);
    }

    public static void setDarkMode(boolean darkMode, Scene scene) {
        isDarkMode = darkMode;
        applyTheme(scene);
    }

    public static boolean isDarkMode() {
        return isDarkMode;
    }

    public static void applyCurrentTheme(Scene scene) {
        applyTheme(scene);
    }
}
