package com.example.cws_gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import java.util.*;
import java.util.concurrent.*;

// === MAIN APPLICATION ===
public class ServiceStationGUI extends Application {
    private HBox pumpBox;
    private FlowPane waitingCarsBox;
    private TextArea logArea;
    private TextField waitingAreaField, pumpCountField, carCountField;
    private Button startButton, stopButton, resetButton;
    private Label statusText, pumpsStatusLabel, queueStatusLabel, statsLabel;
    private ServiceStation station;
    private Map<Integer, VBox> pumpVisuals = new HashMap<>();
    private int configSlots = 5;
    private int configPumps = 3;
    private int configCars = 12;

    @Override
    public void start(Stage stage) {
        // Top Section - Header
        Label title = new Label("Service Station Simulator");
        title.setFont(Font.font("System", FontWeight.BOLD, 24));
        title.setTextFill(Color.WHITE);
        title.setGraphic(createCarIcon(20, Color.WHITE));
        title.setGraphicTextGap(10);

        HBox configBox = new HBox(15);
        configBox.setAlignment(Pos.CENTER_LEFT);

        Label slotsLabel = new Label("Waiting Slots:");
        slotsLabel.setTextFill(Color.WHITE);
        slotsLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        waitingAreaField = new TextField("5");
        waitingAreaField.setPrefWidth(60);

        Label pumpsLabel = new Label("Pumps:");
        pumpsLabel.setTextFill(Color.WHITE);
        pumpsLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        pumpCountField = new TextField("3");
        pumpCountField.setPrefWidth(60);

        Label carsLabel = new Label("Cars:");
        carsLabel.setTextFill(Color.WHITE);
        carsLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        carCountField = new TextField("12");
        carCountField.setPrefWidth(60);

        startButton = new Button("Start Simulation");
        startButton.setGraphic(createPlayIcon(12, Color.WHITE));
        startButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20;");
        startButton.setOnAction(e -> startSimulation());

        stopButton = new Button("Stop");
        stopButton.setGraphic(createStopIcon(12, Color.WHITE));
        stopButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20;");
        stopButton.setDisable(true);
        stopButton.setOnAction(e -> stopSimulation());

        resetButton = new Button("Reset");
        resetButton.setGraphic(createResetIcon(12, Color.WHITE));
        resetButton.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20;");
        resetButton.setOnAction(e -> resetSimulation());

        configBox.getChildren().addAll(slotsLabel, waitingAreaField, pumpsLabel, pumpCountField,
                carsLabel, carCountField, startButton, stopButton, resetButton);

        statusText = new Label("Ready to start simulation");
        statusText.setTextFill(Color.web("#ecf0f1"));
        statusText.setFont(Font.font(14));
        statusText.setGraphic(createInfoIcon(14, Color.web("#ecf0f1")));
        statusText.setGraphicTextGap(8);

        VBox topBox = new VBox(15, title, configBox, statusText);
        topBox.setStyle("-fx-background-color: #2c3e50; -fx-padding: 20;");

        // Center Section - Pumps and Waiting Area
        Label pumpsTitle = new Label("Fuel Pumps");
        pumpsTitle.setFont(Font.font("System", FontWeight.BOLD, 18));
        pumpsTitle.setTextFill(Color.web("#2c3e50"));
        pumpsTitle.setGraphic(createPumpIconLarge(18, Color.web("#2c3e50")));
        pumpsTitle.setGraphicTextGap(10);

        pumpsStatusLabel = new Label("(3 available)");
        pumpsStatusLabel.setFont(Font.font(14));
        pumpsStatusLabel.setTextFill(Color.web("#7f8c8d"));

        HBox pumpsTitleBox = new HBox(10, pumpsTitle, pumpsStatusLabel);
        pumpsTitleBox.setAlignment(Pos.CENTER_LEFT);

        pumpBox = new HBox(15);
        pumpBox.setAlignment(Pos.CENTER);
        pumpBox.setStyle("-fx-padding: 20; -fx-background-color: white; -fx-background-radius: 10;");

        Label queueTitle = new Label("Waiting Area");
        queueTitle.setFont(Font.font("System", FontWeight.BOLD, 18));
        queueTitle.setTextFill(Color.web("#2c3e50"));
        queueTitle.setGraphic(createCarIcon(18, Color.web("#2c3e50")));
        queueTitle.setGraphicTextGap(10);

        queueStatusLabel = new Label("(0 / 5)");
        queueStatusLabel.setFont(Font.font(14));
        queueStatusLabel.setTextFill(Color.web("#7f8c8d"));

        HBox queueTitleBox = new HBox(10, queueTitle, queueStatusLabel);
        queueTitleBox.setAlignment(Pos.CENTER_LEFT);

        waitingCarsBox = new FlowPane(10, 10);
        waitingCarsBox.setAlignment(Pos.TOP_LEFT);
        waitingCarsBox.setStyle("-fx-padding: 20; -fx-background-color: white;");

        ScrollPane queueScroll = new ScrollPane(waitingCarsBox);
        queueScroll.setFitToWidth(true);
        queueScroll.setPrefHeight(150);
        queueScroll.setStyle("-fx-background: white; -fx-background-color: white;");

        Label logTitle = new Label("Activity Log");
        logTitle.setFont(Font.font("System", FontWeight.BOLD, 18));
        logTitle.setTextFill(Color.web("#2c3e50"));
        logTitle.setGraphic(createLogIcon(18, Color.web("#2c3e50")));
        logTitle.setGraphicTextGap(10);

        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setWrapText(true);
        logArea.setPrefHeight(180);
        logArea.setStyle("-fx-font-family: 'Consolas', 'Monaco', monospace; -fx-font-size: 12px; " +
                "-fx-control-inner-background: #2c3e50; -fx-text-fill: #ecf0f1;");

        VBox centerContent = new VBox(10, pumpsTitleBox, pumpBox, queueTitleBox, queueScroll, logTitle, logArea);
        centerContent.setStyle("-fx-padding: 15; -fx-background-color: #ecf0f1;");
        VBox.setVgrow(queueScroll, Priority.NEVER);
        VBox.setVgrow(logArea, Priority.NEVER);

        queueScroll.setPrefHeight(120);
        queueScroll.setMinHeight(120);
        logArea.setPrefHeight(150);
        logArea.setMinHeight(150);

        ScrollPane centerScroll = new ScrollPane(centerContent);
        centerScroll.setFitToWidth(true);
        centerScroll.setStyle("-fx-background: #ecf0f1; -fx-background-color: #ecf0f1;");

        // Bottom Section - Statistics
        Label statsTitle = new Label("Statistics:");
        statsTitle.setTextFill(Color.WHITE);
        statsTitle.setFont(Font.font("System", FontWeight.BOLD, 12));
        statsTitle.setGraphic(createChartIcon(12, Color.WHITE));
        statsTitle.setGraphicTextGap(8);

        statsLabel = new Label("Served: 0 | Waiting: 0 | Total: 0");
        statsLabel.setTextFill(Color.web("#ecf0f1"));
        statsLabel.setFont(Font.font(14));

        HBox bottomBox = new HBox(20, statsTitle, statsLabel);
        bottomBox.setAlignment(Pos.CENTER);
        bottomBox.setStyle("-fx-padding: 15; -fx-background-color: #34495e;");

        // Main Layout
        BorderPane root = new BorderPane();
        root.setTop(topBox);
        root.setCenter(centerScroll);
        root.setBottom(bottomBox);

        initializePumps(3);

        Scene scene = new Scene(root, 900, 700);
        stage.setMinWidth(700);
        stage.setMinHeight(600);
        stage.setTitle("Service Station Simulator");
        stage.setScene(scene);
        stage.show();
    }

    // === ICON CREATION METHODS ===

    private StackPane createCarIcon(double size, Color color) {
        StackPane icon = new StackPane();

        // Car body
        Rectangle body = new Rectangle(size * 1.2, size * 0.6);
        body.setFill(color);
        body.setArcWidth(size * 0.2);
        body.setArcHeight(size * 0.2);

        // Car top
        Rectangle top = new Rectangle(size * 0.7, size * 0.4);
        top.setFill(color);
        top.setTranslateY(-size * 0.3);
        top.setArcWidth(size * 0.15);
        top.setArcHeight(size * 0.15);

        // Wheels
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

        // Pump body
        Rectangle body = new Rectangle(size * 0.6, size);
        body.setFill(color);
        body.setArcWidth(size * 0.1);
        body.setArcHeight(size * 0.1);

        // Display screen
        Rectangle screen = new Rectangle(size * 0.4, size * 0.25);
        screen.setFill(Color.TRANSPARENT);
        screen.setStroke(Color.WHITE);
        screen.setStrokeWidth(1.5);
        screen.setTranslateY(-size * 0.2);

        // Nozzle holder
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

    private SVGPath createChartIcon(double size, Color color) {
        SVGPath chart = new SVGPath();
        chart.setContent("M 5 9.2 L 5 19 L 9.8 19 L 9.8 9.2 Z M 10.8 5 L 10.8 19 L 15.6 19 L 15.6 5 Z M 16.6 13 L 16.6 19 L 21.4 19 L 21.4 13 Z");
        chart.setFill(color);
        chart.setScaleX(size / 24);
        chart.setScaleY(size / 24);
        return chart;
    }

    private StackPane createCheckIcon(double size, Color color) {
        SVGPath check = new SVGPath();
        check.setContent("M 9 16.17 L 4.83 12 L 3.41 13.41 L 9 19 L 21 7 L 19.59 5.59 Z");
        check.setFill(color);
        check.setScaleX(size / 24);
        check.setScaleY(size / 24);
        StackPane icon = new StackPane(check);
        return icon;
    }

    private void initializePumps(int count) {
        pumpBox.getChildren().clear();
        pumpVisuals.clear();

        for (int i = 0; i < count; i++) {
            VBox pumpVisual = createPumpVisual(i);
            pumpVisuals.put(i, pumpVisual);
            pumpBox.getChildren().add(pumpVisual);
        }
    }

    private VBox createPumpVisual(int pumpId) {
        VBox container = new VBox(8);
        container.setAlignment(Pos.CENTER);
        container.setStyle("-fx-border-color: #bdc3c7; -fx-border-width: 2; -fx-border-radius: 8; " +
                "-fx-background-color: #ecf0f1; -fx-background-radius: 8; -fx-padding: 15;");
        container.setPrefWidth(120);

        Label pumpLabel = new Label("Pump " + (pumpId + 1));
        pumpLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

        Rectangle pumpShape = new Rectangle(40, 60);
        pumpShape.setFill(Color.web("#27ae60"));
        pumpShape.setArcWidth(10);
        pumpShape.setArcHeight(10);

        Circle nozzle = new Circle(8);
        nozzle.setFill(Color.web("#2ecc71"));

        Label statusLabel = new Label("FREE");
        statusLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        statusLabel.setTextFill(Color.web("#27ae60"));

        Label carLabel = new Label("");
        carLabel.setFont(Font.font(10));
        carLabel.setTextFill(Color.web("#7f8c8d"));

        VBox pumpGraphic = new VBox(5, pumpShape, nozzle);
        pumpGraphic.setAlignment(Pos.CENTER);

        container.getChildren().addAll(pumpLabel, pumpGraphic, statusLabel, carLabel);
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

        pumpsStatusLabel.setText("(" + configPumps + " available)");
        queueStatusLabel.setText("(0 / " + configSlots + ")");
        statsLabel.setText("Served: 0 | Waiting: 0 | Total: " + configCars);
        statusText.setText("Simulation running...");
        statusText.setTextFill(Color.web("#2ecc71"));
        statusText.setGraphic(createPlayIcon(14, Color.web("#2ecc71")));

        station = new ServiceStation(configSlots, configPumps, this);
        new Thread(() -> station.runSimulation(configCars)).start();
        log("Simulation started: " + configCars + " cars, " + configPumps + " pumps, " + configSlots + " slots");
    }

    private void stopSimulation() {
        if (station != null) {
            station.stop();
            log("Simulation stopped manually.");
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
        pumpsStatusLabel.setText("(3 available)");
        queueStatusLabel.setText("(0 / 5)");
        statsLabel.setText("Served: 0 | Waiting: 0 | Total: 0");
        statusText.setText("Ready to start simulation");
        statusText.setTextFill(Color.web("#ecf0f1"));
        statusText.setGraphic(createInfoIcon(14, Color.web("#ecf0f1")));
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
                Rectangle pumpShape = (Rectangle) ((VBox) pumpVisual.getChildren().get(1)).getChildren().get(0);
                Circle nozzle = (Circle) ((VBox) pumpVisual.getChildren().get(1)).getChildren().get(1);
                Label statusLabel = (Label) pumpVisual.getChildren().get(2);
                Label carLabel = (Label) pumpVisual.getChildren().get(3);

                if (occupied) {
                    pumpShape.setFill(Color.web("#e74c3c"));
                    nozzle.setFill(Color.web("#c0392b"));
                    statusLabel.setText("BUSY");
                    statusLabel.setTextFill(Color.web("#e74c3c"));
                    carLabel.setText(carName);
                } else {
                    pumpShape.setFill(Color.web("#27ae60"));
                    nozzle.setFill(Color.web("#2ecc71"));
                    statusLabel.setText("FREE");
                    statusLabel.setTextFill(Color.web("#27ae60"));
                    carLabel.setText("");
                }
            }
        });
    }

    public void updateQueue(List<String> cars) {
        Platform.runLater(() -> {
            waitingCarsBox.getChildren().clear();
            for (String car : cars) {
                VBox carBox = new VBox(5);
                carBox.setAlignment(Pos.CENTER);
                carBox.setStyle("-fx-border-color: #3498db; -fx-border-width: 2; -fx-border-radius: 5; " +
                        "-fx-background-color: #ecf0f1; -fx-background-radius: 5; -fx-padding: 10;");

                StackPane carIconGraphic = createCarIcon(24, Color.web("#3498db"));

                Label carNameLabel = new Label(car);
                carNameLabel.setFont(Font.font("System", FontWeight.BOLD, 11));
                carNameLabel.setTextFill(Color.web("#2c3e50"));

                carBox.getChildren().addAll(carIconGraphic, carNameLabel);
                waitingCarsBox.getChildren().add(carBox);
            }

            queueStatusLabel.setText("(" + cars.size() + " / " + configSlots + ")");
        });
    }

    public void updateStats(int served, int waiting) {
        Platform.runLater(() -> {
            statsLabel.setText("Served: " + served + " | Waiting: " + waiting + " | Total: " + configCars);
        });
    }

    public void log(String msg) {
        Platform.runLater(() -> logArea.appendText(msg + "\n"));
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

        gui.log("Station initialized with " + slotSize + " waiting slots and " + numPumps + " pumps.");
    }

    public void enterQueue(Car car) throws InterruptedException {
        emptySlots.acquire();
        carMutex.acquire();
        carQueue.add(car);
        gui.updateQueue(carQueue.stream().map(Car::getName).toList());
        gui.updateStats(carsServed, carQueue.size());
        gui.log(car.getName() + " enters the queue (" + carQueue.size() + " waiting)");
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
                            gui.log("Simulation complete! All " + totalCars + " cars serviced.");
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