package com.example.controller;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYShapeAnnotation;
import org.jfree.chart.encoders.EncoderUtil;
import org.jfree.chart.encoders.ImageFormat;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.CandlestickRenderer;
import org.jfree.data.xy.DefaultHighLowDataset;
import org.jfree.data.xy.OHLCDataset;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ScrollEvent;
import javafx.stage.Screen;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Ellipse2D;
import java.io.ByteArrayInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.TimeZone;

public class ChartController {

    @FXML
    private ImageView chartImageView;

    @FXML
    private ScrollPane scrollPane;

    private JFreeChart currentChart; // Almacena el gráfico actual
    private double zoomFactor = 1.0; // Factor de zoom inicial
    private List<CandlestickPattern> detectedPatterns; // Almacenar los patrones detectados

    @FXML
    private void initialize() {
        if (chartImageView != null) {
            chartImageView.setPreserveRatio(true);
            scrollPane.addEventFilter(ScrollEvent.SCROLL, this::zoom);
        } else {
            System.out.println("El ImageView no fue cargado correctamente desde FXML.");
        }
    }

    @FXML
    private void handleGenerateChart() {
        String filePath = "src/main/resources/daily_AAPL.csv";
        try {
            double screenWidth = Screen.getPrimary().getBounds().getWidth();
            double screenHeight = Screen.getPrimary().getBounds().getHeight();

            currentChart = createCandlestickChartFromCSV(filePath);

            byte[] imageBytes = EncoderUtil.encode(currentChart.createBufferedImage((int) screenWidth, (int) screenHeight), ImageFormat.PNG);
            Image chartImage = new Image(new ByteArrayInputStream(imageBytes));
            chartImageView.setImage(chartImage);

            zoomFactor = 1.0; // Restablecer el zoom después de generar un gráfico
            applyZoom();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void zoom(ScrollEvent event) {
        double mouseX = event.getX();
        double mouseY = event.getY();
        double imageViewX = (mouseX + scrollPane.getHvalue() * (chartImageView.getBoundsInParent().getWidth() - scrollPane.getViewportBounds().getWidth())) / zoomFactor;
        double imageViewY = (mouseY + scrollPane.getVvalue() * (chartImageView.getBoundsInParent().getHeight() - scrollPane.getViewportBounds().getHeight())) / zoomFactor;

        zoomFactor *= (event.getDeltaY() > 0) ? 1.1 : 0.9;
        applyZoom();

        scrollPane.setHvalue((imageViewX * zoomFactor - mouseX) / (chartImageView.getBoundsInParent().getWidth() - scrollPane.getViewportBounds().getWidth()));
        scrollPane.setVvalue((imageViewY * zoomFactor - mouseY) / (chartImageView.getBoundsInParent().getHeight() - scrollPane.getViewportBounds().getHeight()));
    }

    private void applyZoom() {
        chartImageView.setFitWidth(chartImageView.getImage().getWidth() * zoomFactor);
        chartImageView.setFitHeight(chartImageView.getImage().getHeight() * zoomFactor);
    }

    private JFreeChart createCandlestickChartFromCSV(String filePath) throws IOException {
        OHLCDataset dataset = readCSVToOHLCDataset(filePath);
        JFreeChart chart = ChartFactory.createCandlestickChart("Gráfico de Velas", "Fecha", "Precio", dataset, false);

        XYPlot plot = (XYPlot) chart.getPlot();
        CandlestickRenderer renderer = new CandlestickRenderer();
        renderer.setUpPaint(new java.awt.Color(0, 255, 0));  // Verde para velas alcistas
        renderer.setDownPaint(new java.awt.Color(255, 0, 0)); // Rojo para velas bajistas

        plot.setRenderer(renderer);
        return chart;
    }

    private OHLCDataset readCSVToOHLCDataset(String filePath) throws IOException {
        List<Date> dates = new ArrayList<>();
        List<Double> open = new ArrayList<>();
        List<Double> high = new ArrayList<>();
        List<Double> low = new ArrayList<>();
        List<Double> close = new ArrayList<>();
        List<Double> volume = new ArrayList<>();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            reader.readNext(); // Saltar la cabecera
            String[] nextLine;
            while ((nextLine = reader.readNext()) != null) {
                try {
                    dates.add(sdf.parse(nextLine[0]));
                } catch (ParseException e) {
                    e.printStackTrace(); // Maneja o registra el error de análisis de fecha
                }
                open.add(Double.parseDouble(nextLine[1]));
                high.add(Double.parseDouble(nextLine[2]));
                low.add(Double.parseDouble(nextLine[3]));
                close.add(Double.parseDouble(nextLine[4]));
                volume.add(Double.parseDouble(nextLine[5]));
            }
        } catch (CsvValidationException e) {
            e.printStackTrace(); // Maneja o registra el error de validación del CSV
        }

        Date[] dateArray = dates.toArray(new Date[0]);
        double[] openArray = open.stream().mapToDouble(Double::doubleValue).toArray();
        double[] highArray = high.stream().mapToDouble(Double::doubleValue).toArray();
        double[] lowArray = low.stream().mapToDouble(Double::doubleValue).toArray();
        double[] closeArray = close.stream().mapToDouble(Double::doubleValue).toArray();
        double[] volumeArray = volume.stream().mapToDouble(Double::doubleValue).toArray();

        return new DefaultHighLowDataset("Datos", dateArray, highArray, lowArray, openArray, closeArray, volumeArray);
    }

    @FXML
    private void handleDetectPatterns() {
        String filePath = "src/main/resources/daily_AAPL.csv";
        try {
            detectedPatterns = detectCandlestickPatterns(filePath);
            if (detectedPatterns.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("No se detectaron patrones");
                alert.setHeaderText(null);
                alert.setContentText("No se detectaron patrones de velas.");
                alert.showAndWait();
            } else {
                showPatternSelectionDialog(detectedPatterns);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showPatternSelectionDialog(List<CandlestickPattern> patterns) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Seleccionar tipo de patrón");

        ButtonType okButton = new ButtonType("Aceptar", ButtonType.OK.getButtonData());
        dialog.getDialogPane().getButtonTypes().addAll(okButton, ButtonType.CANCEL);

        Set<String> patternTypes = patterns.stream()
                .map(CandlestickPattern::getName)
                .collect(Collectors.toSet());

        ComboBox<String> comboBox = new ComboBox<>(javafx.collections.FXCollections.observableArrayList(patternTypes));
        comboBox.setPromptText("Selecciona un patrón");

        VBox vbox = new VBox(comboBox);
        vbox.setSpacing(10);
        dialog.getDialogPane().setContent(vbox);

        dialog.setResultConverter(button -> {
            if (button == okButton) {
                return comboBox.getValue();
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(this::highlightSelectedPattern);
    }

    private void highlightSelectedPattern(String selectedPattern) {
        if (currentChart == null) {
            System.out.println("El gráfico no está generado aún.");
            return;
        }

        try {
            XYPlot plot = currentChart.getXYPlot();
            plot.clearAnnotations(); // Limpiar anotaciones anteriores

            // Crear los puntos amarillos en las posiciones detectadas
            for (CandlestickPattern pattern : detectedPatterns) {
                if (pattern.getName().equals(selectedPattern)) {
                    // Crear un círculo amarillo en las coordenadas del patrón
                    Ellipse2D.Double circle = new Ellipse2D.Double(pattern.getX() - 0.5, pattern.getY() - 0.5, 1, 1);
                    XYShapeAnnotation annotation = new XYShapeAnnotation(circle, new BasicStroke(5.0f), Color.YELLOW, Color.YELLOW);
                    plot.addAnnotation(annotation);
                }
            }

            double screenWidth = Screen.getPrimary().getBounds().getWidth();
            double screenHeight = Screen.getPrimary().getBounds().getHeight();
            byte[] imageBytes = EncoderUtil.encode(currentChart.createBufferedImage((int) screenWidth, (int) screenHeight), ImageFormat.PNG);
            Image chartImage = new Image(new ByteArrayInputStream(imageBytes));
            chartImageView.setImage(chartImage);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Lógica de detección de patrones de velas usando tus métodos
    private List<CandlestickPattern> detectCandlestickPatterns(String filePath) throws IOException {
        List<CandlestickPattern> patterns = new ArrayList<>();
        OHLCDataset dataset = readCSVToOHLCDataset(filePath);

        for (int i = 1; i < dataset.getItemCount(0); i++) {
            double prevOpen = dataset.getOpenValue(0, i - 1);
            double prevClose = dataset.getCloseValue(0, i - 1);
            double open = dataset.getOpenValue(0, i);
            double close = dataset.getCloseValue(0, i);
            double high = dataset.getHighValue(0, i);
            double low = dataset.getLowValue(0, i);

            if (isHammer(open, close, high, low)) {
                patterns.add(new CandlestickPattern("Hammer", dataset.getXValue(0, i), high));
            }

            if (isShootingStar(open, close, high, low)) {
                patterns.add(new CandlestickPattern("Shooting Star", dataset.getXValue(0, i), high));
            }

            if (isBullishEngulfing(prevOpen, prevClose, open, close)) {
                patterns.add(new CandlestickPattern("Bullish Engulfing", dataset.getXValue(0, i), high));
            }

            if (isBearishEngulfing(prevOpen, prevClose, open, close)) {
                patterns.add(new CandlestickPattern("Bearish Engulfing", dataset.getXValue(0, i), low));
            }
        }

        return patterns;
    }

    // Métodos de detección de patrones
    private boolean isHammer(double openPrice, double closePrice, double highPrice, double lowPrice) {
        double bodySize = Math.abs(openPrice - closePrice);
        double upperShadow = highPrice - Math.max(openPrice, closePrice);
        double lowerShadow = Math.min(openPrice, closePrice) - lowPrice;

        return bodySize <= (highPrice - lowPrice) * 0.2 && lowerShadow > 2 * bodySize && upperShadow <= bodySize;
    }

    private boolean isShootingStar(double openPrice, double closePrice, double highPrice, double lowPrice) {
        double bodySize = Math.abs(openPrice - closePrice);
        double upperShadow = highPrice - Math.max(openPrice, closePrice);
        double lowerShadow = Math.min(openPrice, closePrice) - lowPrice;

        return bodySize <= (highPrice - lowPrice) * 0.2 && upperShadow > 2 * bodySize && lowerShadow <= bodySize;
    }

    private boolean isBullishEngulfing(double prevOpen, double prevClose, double open, double close) {
        return prevClose < prevOpen && close > open && open < prevClose && close > prevOpen;
    }

    private boolean isBearishEngulfing(double prevOpen, double prevClose, double open, double close) {
        return prevClose > prevOpen && close < open && open > prevClose && close < prevOpen;
    }

    // Clase interna para almacenar la información de los patrones
    private static class CandlestickPattern {
        private final String name;
        private final double x;
        private final double y;

        public CandlestickPattern(String name, double x, double y) {
            this.name = name;
            this.x = x;
            this.y = y;
        }

        public String getName() {
            return name;
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }
    }
}
