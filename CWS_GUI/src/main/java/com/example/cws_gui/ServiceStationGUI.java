package com.example.cws_gui;

import javafx.animation.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.util.*;
import java.util.concurrent.*;

public class ServiceStationGUI extends Application {
    // Used for GUI
    private HBox pumpBox;
    private FlowPane waitingCarsBox;
    private TextArea logArea;
    private TextField waitingAreaField, pumpCountField, carCountField;
    private Button startButton, stopButton, resetButton;
    private Label statusText, pumpsStatusLabel, queueStatusLabel, statsLabel;
    private ServiceStation station;
    private Map<Integer, VBox> pumpVisuals = new HashMap<>();
    private Label servedLabel, waitingLabel, totalLabel;

    // Used for backend
    private int configSlots = 5;
    private int configPumps = 3;
    private int configCars = 12;


    @Override
    public void start(Stage stage) {
        Label title = new Label("Service Station Simulator");
        title.setFont(Font.font("System", FontWeight.BOLD, 28));
        title.setTextFill(Color.WHITE);
        title.setGraphic(createCarIcon(24, Color.WHITE));
        title.setGraphicTextGap(12);
        title.setEffect(new DropShadow(5, Color.rgb(0, 0, 0, 0.3)));

        HBox configBox = new HBox(20);
        configBox.setAlignment(Pos.CENTER_LEFT);
        configBox.setPadding(new Insets(10, 0, 0, 0));

        // Styled input groups
        VBox slotsGroup = createInputGroup("Waiting Slots", waitingAreaField = new TextField("5"));
        VBox pumpsGroup = createInputGroup("Pumps", pumpCountField = new TextField("5"));
        VBox carsGroup = createInputGroup("Cars", carCountField = new TextField("12"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        startButton = new Button("Start");
        startButton.setGraphic(createPlayIcon(14, Color.WHITE));
        startButton.setStyle("-fx-background-color: linear-gradient(to bottom, #27ae60, #229954); " +
                "-fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 25; " +
                "-fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 13px;");
        addHoverEffect(startButton, "#27ae60", "#1e8449");

        stopButton = new Button("Stop");
        stopButton.setGraphic(createStopIcon(12, Color.WHITE));
        stopButton.setStyle("-fx-background-color: linear-gradient(to bottom, #e74c3c, #c0392b); " +
                "-fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 25; " +
                "-fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 13px;");
        stopButton.setDisable(true);
        addHoverEffect(stopButton, "#e74c3c", "#a93226");

        resetButton = new Button("Reset");
        resetButton.setGraphic(createResetIcon(12, Color.WHITE));
        resetButton.setStyle("-fx-background-color: linear-gradient(to bottom, #95a5a6, #7f8c8d); " +
                "-fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 25; " +
                "-fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 13px;");
        addHoverEffect(resetButton, "#95a5a6", "#5d6d7e");

        startButton.setOnAction(e -> startSimulation());
        stopButton.setOnAction(e -> stopSimulation());
        resetButton.setOnAction(e -> resetSimulation());

        HBox buttonBox = new HBox(12, startButton, stopButton, resetButton);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        configBox.getChildren().addAll(slotsGroup, pumpsGroup, carsGroup, spacer, buttonBox);

        statusText = new Label("Ready to start simulation");
        statusText.setTextFill(Color.web("#ecf0f1"));
        statusText.setFont(Font.font("System", FontWeight.NORMAL, 14));
        statusText.setGraphic(createInfoIcon(16, Color.web("#ecf0f1")));
        statusText.setGraphicTextGap(10);

        VBox topBox = new VBox(18, title, configBox, statusText);
        topBox.setStyle("-fx-background-color: linear-gradient(to bottom, #2c3e50, #34495e); -fx-padding: 25;");

        // Center Section - Pumps
        Label pumpsTitle = new Label("Fuel Pumps");
        pumpsTitle.setFont(Font.font("System", FontWeight.BOLD, 20));
        pumpsTitle.setTextFill(Color.web("#2c3e50"));
        pumpsTitle.setGraphic(createPumpIconLarge(20, Color.web("#2c3e50")));
        pumpsTitle.setGraphicTextGap(12);

        pumpsStatusLabel = new Label("3 available");
        pumpsStatusLabel.setFont(Font.font("System", FontWeight.NORMAL, 13));
        pumpsStatusLabel.setTextFill(Color.web("#7f8c8d"));

        HBox pumpsTitleBox = new HBox(10, pumpsTitle, pumpsStatusLabel);
        pumpsTitleBox.setAlignment(Pos.CENTER_LEFT);
        pumpsTitleBox.setPadding(new Insets(0, 0, 10, 0));

        pumpBox = new HBox(20);
        pumpBox.setAlignment(Pos.CENTER);
        pumpBox.setStyle("-fx-padding: 25; -fx-background-color: white; " +
                "-fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");
        pumpBox.setPrefHeight(180);

        // Waiting Area
        Label queueTitle = new Label("Waiting Area");
        queueTitle.setFont(Font.font("System", FontWeight.BOLD, 20));
        queueTitle.setTextFill(Color.web("#2c3e50"));
        queueTitle.setGraphic(createCarIcon(20, Color.web("#2c3e50")));
        queueTitle.setGraphicTextGap(12);

        queueStatusLabel = new Label("0 / 5");
        queueStatusLabel.setFont(Font.font("System", FontWeight.NORMAL, 13));
        queueStatusLabel.setTextFill(Color.web("#7f8c8d"));

        HBox queueTitleBox = new HBox(10, queueTitle, queueStatusLabel);
        queueTitleBox.setAlignment(Pos.CENTER_LEFT);
        queueTitleBox.setPadding(new Insets(15, 0, 10, 0));

        waitingCarsBox = new FlowPane(12, 12);
        waitingCarsBox.setAlignment(Pos.TOP_LEFT);
        waitingCarsBox.setStyle("-fx-padding: 20; -fx-background-color: white;");
        waitingCarsBox.setMinHeight(100);

        ScrollPane queueScroll = new ScrollPane(waitingCarsBox);
        queueScroll.setFitToWidth(true);
        queueScroll.setPrefHeight(140);
        queueScroll.setStyle("-fx-background: white; -fx-background-color: transparent; " +
                "-fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");

        // Activity Log
        Label logTitle = new Label("Activity Log");
        logTitle.setFont(Font.font("System", FontWeight.BOLD, 20));
        logTitle.setTextFill(Color.web("#2c3e50"));
        logTitle.setGraphic(createLogIcon(20, Color.web("#2c3e50")));
        logTitle.setGraphicTextGap(12);

        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setWrapText(true);
        logArea.setPrefHeight(200);
        logArea.setStyle("-fx-font-family: 'Consolas', 'Monaco', monospace; -fx-font-size: 12px; " +
                "-fx-control-inner-background: #1e272e; -fx-text-fill: #00d2d3; " +
                "-fx-background-radius: 12; -fx-border-radius: 12; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");

        VBox centerContent = new VBox(15, pumpsTitleBox, pumpBox, queueTitleBox, queueScroll,
                new VBox(10, logTitle, logArea));
        centerContent.setStyle("-fx-padding: 25; -fx-background-color: #f5f6fa;");
        VBox.setVgrow(logArea, Priority.ALWAYS);

        ScrollPane centerScroll = new ScrollPane(centerContent);
        centerScroll.setFitToWidth(true);
        centerScroll.setStyle("-fx-background: #f5f6fa; -fx-background-color: #f5f6fa;");

        // Bottom Section
        HBox statsBox = createStatsBox();

        // Main Layout
        BorderPane root = new BorderPane();
        root.setTop(topBox);
        root.setCenter(centerScroll);
        root.setBottom(statsBox);

        initializePumps(3);

        Scene scene = new Scene(root, 1000, 750);
        stage.setMinWidth(800);
        stage.setMinHeight(650);
        stage.setTitle("Service Station Simulator");
        stage.setScene(scene);
        stage.show();
    }

    private VBox createInputGroup(String label, TextField field) {
        Label lbl = new Label(label);
        lbl.setTextFill(Color.web("#ecf0f1"));
        lbl.setFont(Font.font("System", FontWeight.BOLD, 11));

        field.setPrefWidth(70);
        field.setStyle("-fx-background-color: rgba(255, 255, 255, 0.9); " +
                "-fx-background-radius: 5; -fx-padding: 6 10; " +
                "-fx-font-size: 13px; -fx-font-weight: bold;");

        VBox group = new VBox(5, lbl, field);
        group.setAlignment(Pos.CENTER_LEFT);
        return group;
    }

    private HBox createStatsBox() {
        HBox statsBox = new HBox(30);
        statsBox.setAlignment(Pos.CENTER);
        statsBox.setStyle("-fx-background-color: linear-gradient(to bottom, #34495e, #2c3e50); -fx-padding: 20;");

        servedLabel = createStatCard("Served", "0", "#27ae60");
        waitingLabel = createStatCard("Waiting", "0", "#3498db");
        totalLabel = createStatCard("Total", "0", "#95a5a6");

        statsBox.getChildren().addAll(servedLabel, waitingLabel, totalLabel);
        return statsBox;
    }

    private Label createStatCard(String title, String value, String color) {
        VBox card = new VBox(5);
        card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-background-color: rgba(255, 255, 255, 0.1); " +
                "-fx-background-radius: 10; -fx-padding: 15 30; " +
                "-fx-border-color: " + color + "; -fx-border-width: 2; -fx-border-radius: 10;");

        Label titleLabel = new Label(title);
        titleLabel.setTextFill(Color.web("#bdc3c7"));
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 11));

        Label valueLabel = new Label(value);
        valueLabel.setTextFill(Color.web(color));
        valueLabel.setFont(Font.font("System", FontWeight.BOLD, 28));
        valueLabel.setEffect(new DropShadow(3, Color.rgb(0, 0, 0, 0.3)));

        card.getChildren().addAll(titleLabel, valueLabel);

        Label container = new Label();
        container.setGraphic(card);
        container.setUserData(valueLabel);
        return container;
    }

    private void addHoverEffect(Button button, String normalColor, String hoverColor) {
        button.setOnMouseEntered(e -> {
            button.setStyle(button.getStyle().replace(normalColor, hoverColor));
            ScaleTransition st = new ScaleTransition(Duration.millis(100), button);
            st.setToX(1.05);
            st.setToY(1.05);
            st.play();
        });
        button.setOnMouseExited(e -> {
            button.setStyle(button.getStyle().replace(hoverColor, normalColor));
            ScaleTransition st = new ScaleTransition(Duration.millis(100), button);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
        });
    }

    private void initializePumps(int count) {
        pumpBox.getChildren().clear();
        pumpVisuals.clear();

        for (int i = 0; i < count; i++) {
            VBox pumpVisual = createPumpVisual(i);
            pumpVisuals.put(i, pumpVisual);
            pumpBox.getChildren().add(pumpVisual);

            // Entrance animation
            pumpVisual.setOpacity(0);
            pumpVisual.setTranslateY(-20);
            FadeTransition ft = new FadeTransition(Duration.millis(400), pumpVisual);
            ft.setToValue(1.0);
            ft.setDelay(Duration.millis(i * 100));
            TranslateTransition tt = new TranslateTransition(Duration.millis(400), pumpVisual);
            tt.setToY(0);
            tt.setDelay(Duration.millis(i * 100));
            ft.play();
            tt.play();
        }
    }

    private VBox createPumpVisual(int pumpId) {
        VBox container = new VBox(10);
        container.setAlignment(Pos.CENTER);
        container.setStyle("-fx-border-color: #27ae60; -fx-border-width: 3; -fx-border-radius: 10; " +
                "-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 20; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 8, 0, 0, 2);");
        container.setPrefWidth(140);
        container.setMinHeight(150);

        Label pumpLabel = new Label("Pump " + (pumpId + 1));
        pumpLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
        pumpLabel.setTextFill(Color.web("#2c3e50"));

        Rectangle pumpShape = new Rectangle(50, 70);
        pumpShape.setFill(Color.web("#27ae60"));
        pumpShape.setArcWidth(12);
        pumpShape.setArcHeight(12);
        pumpShape.setEffect(new DropShadow(5, Color.rgb(0, 0, 0, 0.2)));

        Rectangle screen = new Rectangle(30, 15);
        screen.setFill(Color.web("#2ecc71"));
        screen.setArcWidth(3);
        screen.setArcHeight(3);
        screen.setTranslateY(-15);

        Circle nozzle = new Circle(10);
        nozzle.setFill(Color.web("#2ecc71"));
        nozzle.setEffect(new InnerShadow(5, Color.rgb(0, 0, 0, 0.3)));

        StackPane pumpGraphic = new StackPane(pumpShape, screen);
        VBox fullPump = new VBox(8, pumpGraphic, nozzle);
        fullPump.setAlignment(Pos.CENTER);

        Label statusLabel = new Label("AVAILABLE");
        statusLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        statusLabel.setTextFill(Color.web("#27ae60"));
        statusLabel.setStyle("-fx-background-color: rgba(39, 174, 96, 0.1); " +
                "-fx-background-radius: 5; -fx-padding: 4 12;");

        Label carLabel = new Label("");
        carLabel.setFont(Font.font("System", FontWeight.NORMAL, 10));
        carLabel.setTextFill(Color.web("#7f8c8d"));

        container.getChildren().addAll(pumpLabel, fullPump, statusLabel, carLabel);
        return container;
    }

    private void startSimulation() {
        try {
            configSlots = Integer.parseInt(waitingAreaField.getText());
            configPumps = Integer.parseInt(pumpCountField.getText());
            configCars = Integer.parseInt(carCountField.getText());

            if (configSlots < 1 || configPumps < 1 || configCars < 1) {
                showAlert("Invalid Input", "All values must be at least 1");
                return;
            }
            if (configSlots > 20 || configPumps > 10 || configCars > 100) {
                showAlert("Invalid Input", "Maximum limits: Slots=20, Pumps=10, Cars=100");
                return;
            }
        } catch (NumberFormatException e) {
            showAlert("Invalid Input", "Please enter valid numbers");
            return;
        }

        logArea.clear();
        waitingCarsBox.getChildren().clear();
        initializePumps(configPumps);

        startButton.setDisable(true);
        stopButton.setDisable(false);
        waitingAreaField.setDisable(true);
        pumpCountField.setDisable(true);
        carCountField.setDisable(true);

        pumpsStatusLabel.setText(configPumps + " available");
        queueStatusLabel.setText("0 / " + configSlots);
        updateStatsDisplay(0, 0, configCars);

        statusText.setText("Simulation running...");
        statusText.setTextFill(Color.web("#2ecc71"));
        statusText.setGraphic(createPlayIcon(16, Color.web("#2ecc71")));

        station = new ServiceStation(configSlots, configPumps, this);
        new Thread(() -> station.runSimulation(configCars)).start();
        log("Simulation started: " + configCars + " cars, " + configPumps + " pumps, " + configSlots + " slots");
    }

    private void stopSimulation() {
        if (station != null) {
            station.stop();
            log("Simulation stopped manually");
        }
        startButton.setDisable(false);
        stopButton.setDisable(true);
        waitingAreaField.setDisable(false);
        pumpCountField.setDisable(false);
        carCountField.setDisable(false);
        statusText.setText("Simulation stopped");
        statusText.setTextFill(Color.web("#e74c3c"));
        statusText.setGraphic(createStopIcon(14, Color.web("#e74c3c")));
    }

    private void resetSimulation() {
        stopSimulation();
        logArea.clear();
        waitingCarsBox.getChildren().clear();
        initializePumps(3);
        waitingAreaField.setText("5");
        pumpCountField.setText("3");
        carCountField.setText("12");
        pumpsStatusLabel.setText("3 available");
        queueStatusLabel.setText("0 / 5");
        updateStatsDisplay(0, 0, 0);
        statusText.setText("Ready to start simulation");
        statusText.setTextFill(Color.web("#ecf0f1"));
        statusText.setGraphic(createInfoIcon(16, Color.web("#ecf0f1")));
        log("Simulation reset");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void updatePumpStatus(int index, String carName, boolean occupied) {
        Platform.runLater(() -> {
            VBox pumpVisual = pumpVisuals.get(index);
            if (pumpVisual != null) {
                VBox fullPump = (VBox) pumpVisual.getChildren().get(1);
                StackPane pumpGraphic = (StackPane) fullPump.getChildren().get(0);
                Rectangle pumpShape = (Rectangle) pumpGraphic.getChildren().get(0);
                Rectangle screen = (Rectangle) pumpGraphic.getChildren().get(1);
                Circle nozzle = (Circle) fullPump.getChildren().get(1);
                Label statusLabel = (Label) pumpVisual.getChildren().get(2);
                Label carLabel = (Label) pumpVisual.getChildren().get(3);

                FillTransition ft = new FillTransition(Duration.millis(300), pumpShape);
                FillTransition nft = new FillTransition(Duration.millis(300), nozzle);
                FillTransition sft = new FillTransition(Duration.millis(300), screen);

                if (occupied) {
                    ft.setToValue(Color.web("#e74c3c"));
                    nft.setToValue(Color.web("#c0392b"));
                    sft.setToValue(Color.web("#e74c3c"));
                    statusLabel.setText("BUSY");
                    statusLabel.setTextFill(Color.web("#e74c3c"));
                    statusLabel.setStyle("-fx-background-color: rgba(231, 76, 60, 0.1); " +
                            "-fx-background-radius: 5; -fx-padding: 4 12;");
                    carLabel.setText(carName);
                    pumpVisual.setStyle(pumpVisual.getStyle().replace("#27ae60", "#e74c3c"));
                } else {
                    ft.setToValue(Color.web("#27ae60"));
                    nft.setToValue(Color.web("#2ecc71"));
                    sft.setToValue(Color.web("#2ecc71"));
                    statusLabel.setText("AVAILABLE");
                    statusLabel.setTextFill(Color.web("#27ae60"));
                    statusLabel.setStyle("-fx-background-color: rgba(39, 174, 96, 0.1); " +
                            "-fx-background-radius: 5; -fx-padding: 4 12;");
                    carLabel.setText("");
                    pumpVisual.setStyle(pumpVisual.getStyle().replace("#e74c3c", "#27ae60"));
                }
                ft.play();
                nft.play();
                sft.play();
            }
        });
    }

    public void updateQueue(List<String> cars) {
        Platform.runLater(() -> {
            waitingCarsBox.getChildren().clear();
            for (int i = 0; i < cars.size(); i++) {
                String car = cars.get(i);
                VBox carBox = new VBox(8);
                carBox.setAlignment(Pos.CENTER);
                carBox.setStyle("-fx-border-color: #3498db; -fx-border-width: 2.5; -fx-border-radius: 8; " +
                        "-fx-background-color: white; -fx-background-radius: 8; -fx-padding: 12; " +
                        "-fx-effect: dropshadow(gaussian, rgba(52, 152, 219, 0.3), 8, 0, 0, 2);");

                StackPane carIconGraphic = createCarIcon(28, Color.web("#3498db"));
                Label carNameLabel = new Label(car);
                carNameLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
                carNameLabel.setTextFill(Color.web("#2c3e50"));

                carBox.getChildren().addAll(carIconGraphic, carNameLabel);

                carBox.setOpacity(0);
                carBox.setScaleX(0.8);
                carBox.setScaleY(0.8);
                waitingCarsBox.getChildren().add(carBox);

                FadeTransition ft = new FadeTransition(Duration.millis(300), carBox);
                ft.setToValue(1.0);
                ft.setDelay(Duration.millis(i * 50));
                ScaleTransition st = new ScaleTransition(Duration.millis(300), carBox);
                st.setToX(1.0);
                st.setToY(1.0);
                st.setDelay(Duration.millis(i * 50));
                ft.play();
                st.play();
            }
            queueStatusLabel.setText(cars.size() + " / " + configSlots);
        });
    }

    public void updateStats(int served, int waiting) {
        Platform.runLater(() -> updateStatsDisplay(served, waiting, configCars));
    }

    private void updateStatsDisplay(int served, int waiting, int total) {
        animateStatValue(servedLabel, served);
        animateStatValue(waitingLabel, waiting);
        animateStatValue(totalLabel, total);
    }

    private void animateStatValue(Label statCard, int newValue) {
        Label valueLabel = (Label) statCard.getUserData();
        if (valueLabel != null) {
            valueLabel.setText(String.valueOf(newValue));
            ScaleTransition st = new ScaleTransition(Duration.millis(200), valueLabel);
            st.setToX(1.15);
            st.setToY(1.15);
            st.setAutoReverse(true);
            st.setCycleCount(2);
            st.play();
        }
    }

    public void log(String msg) {
        Platform.runLater(() -> {
            String timestamp = String.format("[%tT] ", System.currentTimeMillis());
            logArea.appendText(timestamp + msg + "\n");
        });
    }

    // === ICON CREATION METHODS ===
    private StackPane createCarIcon(double size, Color color) {
        StackPane icon = new StackPane();
        Rectangle body = new Rectangle(size * 1.2, size * 0.6);
        body.setFill(color);
        body.setArcWidth(size * 0.2);
        body.setArcHeight(size * 0.2);
        Rectangle top = new Rectangle(size * 0.7, size * 0.4);
        top.setFill(color);
        top.setTranslateY(-size * 0.3);
        top.setArcWidth(size * 0.15);
        top.setArcHeight(size * 0.15);
        Circle wheel1 = new Circle(size * 0.15);
        wheel1.setFill(color);
        wheel1.setTranslateX(-size * 0.35);
        wheel1.setTranslateY(size * 0.3);
        Circle wheel2 = new Circle(size * 0.15);
        wheel2.setFill(color);
        wheel2.setTranslateX(size * 0.35);
        wheel2.setTranslateY(size * 0.3);
        icon.getChildren().addAll(body, top, wheel1, wheel2);
        return icon;
    }

    private StackPane createPumpIconLarge(double size, Color color) {
        StackPane icon = new StackPane();
        Rectangle body = new Rectangle(size * 0.6, size);
        body.setFill(color);
        body.setArcWidth(size * 0.1);
        body.setArcHeight(size * 0.1);
        Rectangle screen = new Rectangle(size * 0.4, size * 0.25);
        screen.setFill(Color.TRANSPARENT);
        screen.setStroke(color);
        screen.setStrokeWidth(2);
        screen.setTranslateY(-size * 0.2);
        Circle nozzle = new Circle(size * 0.15);
        nozzle.setFill(color);
        nozzle.setTranslateX(size * 0.4);
        icon.getChildren().addAll(body, screen, nozzle);
        return icon;
    }

    private SVGPath createPlayIcon(double size, Color color) {
        SVGPath play = new SVGPath();
        play.setContent("M 0 0 L " + size + " " + (size/2) + " L 0 " + size + " Z");
        play.setFill(color);
        return play;
    }

    private Rectangle createStopIcon(double size, Color color) {
        Rectangle stop = new Rectangle(size, size);
        stop.setFill(color);
        stop.setArcWidth(2);
        stop.setArcHeight(2);
        return stop;
    }

    private SVGPath createResetIcon(double size, Color color) {
        SVGPath reset = new SVGPath();
        reset.setContent("M 12 4 C 9.79 4 7.7 4.85 6.17 6.39 L 4 4.18 L 4 10 L 10 10 L 7.82 7.82 C 9.05 6.59 10.5 6 12 6 C 15.31 6 18 8.69 18 12 C 18 15.31 15.31 18 12 18 C 9.94 18 8.08 16.95 7 15.39 L 5.27 16.73 C 6.73 18.78 9.21 20 12 20 C 16.42 20 20 16.42 20 12 C 20 7.58 16.42 4 12 4 Z");
        reset.setFill(color);
        reset.setScaleX(size / 24);
        reset.setScaleY(size / 24);
        return reset;
    }

    private SVGPath createInfoIcon(double size, Color color) {
        SVGPath info = new SVGPath();
        info.setContent("M 12 2 C 6.48 2 2 6.48 2 12 C 2 17.52 6.48 22 12 22 C 17.52 22 22 17.52 22 12 C 22 6.48 17.52 2 12 2 Z M 13 17 L 11 17 L 11 11 L 13 11 Z M 13 9 L 11 9 L 11 7 L 13 7 Z");
        info.setFill(color);
        info.setScaleX(size / 24);
        info.setScaleY(size / 24);
        return info;
    }

    private SVGPath createLogIcon(double size, Color color) {
        SVGPath log = new SVGPath();
        log.setContent("M 14 2 L 6 2 C 4.9 2 4 2.9 4 4 L 4 20 C 4 21.1 4.89 22 5.99 22 L 18 22 C 19.1 22 20 21.1 20 20 L 20 8 Z M 16 18 L 8 18 L 8 16 L 16 16 Z M 16 14 L 8 14 L 8 12 L 16 12 Z M 13 9 L 13 3.5 L 18.5 9 Z");
        log.setFill(color);
        log.setScaleX(size / 24);
        log.setScaleY(size / 24);
        return log;
    }

    public static void main(String[] args) {
        launch();
    }
}

// === SIMULATION LOGIC ===
class ServiceStation {
    private final List<Car> carQueue = new LinkedList<>();
    private final Mutex carMutex = new Mutex();
    private final Semaphore emptySlots;
    private final Semaphore fullSlots;
    private final Semaphore availablePumps;
    private final ServiceStationGUI gui;
    private final int slotSize;
    private final int numPumps;
    private volatile boolean running = true;
    private int carsServed = 0;

    public ServiceStation(int slotSize, int numPumps, ServiceStationGUI gui) {
        this.slotSize = slotSize;
        this.numPumps = numPumps;
        this.gui = gui;

        this.emptySlots = new Semaphore(slotSize);
        this.fullSlots = new Semaphore(0);
        this.availablePumps = new Semaphore(numPumps);

        gui.log("Station initialized with " + slotSize + " waiting slots and " + numPumps + " pumps");
    }

    public void enterQueue(Car car) throws InterruptedException {
        emptySlots.acquire();
        carMutex.acquire();
        carQueue.add(car);
        gui.updateQueue(carQueue.stream().map(Car::getName).toList());
        gui.updateStats(carsServed, carQueue.size());
        gui.log( car.getName() + " enters the queue (" + carQueue.size() + " waiting)");
        carMutex.release();
        fullSlots.release();
    }

    public Car takeCar() throws InterruptedException {
        fullSlots.acquire();
        carMutex.acquire();
        Car car = carQueue.remove(0);
        gui.updateQueue(carQueue.stream().map(Car::getName).toList());
        gui.updateStats(carsServed, carQueue.size());
        carMutex.release();
        emptySlots.release();
        return car;
    }

    public void runSimulation(int totalCars) {
        ExecutorService pumpPool = Executors.newFixedThreadPool(numPumps);

        for (int i = 0; i < numPumps; i++) {
            int pumpIndex = i;
            pumpPool.submit(() -> {
                try {
                    while (running) {
                        Car car = takeCar();
                        availablePumps.acquire();
                        gui.updatePumpStatus(pumpIndex, car.getName(), true);
                        gui.log("Pump-" + (pumpIndex + 1) + " starts servicing " + car.getName());
                        Thread.sleep((int) (Math.random() * 2000 + 1000));
                        gui.updatePumpStatus(pumpIndex, car.getName(), false);
                        synchronized (this) {
                            carsServed++;
                            gui.updateStats(carsServed, carQueue.size());
                        }
                        gui.log("Pump-" + (pumpIndex + 1) + " finished servicing " + car.getName());
                        availablePumps.release();

                        if (carsServed >= totalCars) {
                            gui.log("Simulation complete! All " + totalCars + " cars serviced");
                            running = false;
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        for (int i = 1; i <= totalCars; i++) {
            new Car("Car-" + i, this).start();
            try {
                Thread.sleep((int) (Math.random() * 800));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public void stop() {
        running = false;
    }
}

class Car extends Thread {
    private final ServiceStation station;
    public Car(String name, ServiceStation station) {
        super(name);
        this.station = station;
    }
    @Override
    public void run() {
        try {
            station.enterQueue(this);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

class Semaphore {
    private int permits;
    public Semaphore(int initialPermits) {
        this.permits = initialPermits;
    }
    public synchronized void acquire() throws InterruptedException {
        while (permits == 0) wait();
        permits--;
    }
    public synchronized void release() {
        permits++;
        notify();
    }
}

class Mutex {
    private boolean locked = false;
    public synchronized void acquire() throws InterruptedException {
        while (locked) wait();
        locked = true;
    }
    public synchronized void release() {
        locked = false;
        notify();
    }
}