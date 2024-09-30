package com.example.controller;

import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.ScrollPane;
import javafx.stage.Screen;
import javafx.scene.input.ScrollEvent;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextArea;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.CandlestickRenderer;
import org.jfree.data.xy.DefaultHighLowDataset;
import org.jfree.data.xy.OHLCDataset;
import org.jfree.chart.encoders.EncoderUtil;
import org.jfree.chart.encoders.ImageFormat;
import org.jfree.chart.annotations.XYPointerAnnotation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.ui.TextAnchor;
import java.awt.Font;
import org.jfree.chart.annotations.XYTextAnnotation;

import java.io.ByteArrayInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import com.opencsv.CSVReader;
import javax.swing.JOptionPane;

public class ChartController {

    private JFreeChart currentChart; // Añadir esto como un atributo de clase

    @FXML
    private ImageView chartImageView;

    @FXML
    private ScrollPane scrollPane;

    private double zoomFactor = 1.0;

    private List<CandlestickPattern> patternsDetected;  // Lista para almacenar los patrones detectados

    @FXML
    private void initialize() {
        if (chartImageView != null) {
            chartImageView.setPreserveRatio(true);

            scrollPane.addEventFilter(ScrollEvent.SCROLL, event -> {
                if (event.getDeltaY() != 0) {
                    zoom(event);
                }
                event.consume();
            });

            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        } else {
            System.out.println("El chartImageView no ha sido cargado correctamente desde el archivo FXML.");
        }
    }

    private void zoom(ScrollEvent event) {
        double mouseX = event.getX();
        double mouseY = event.getY();

        double imageViewX = (mouseX + scrollPane.getHvalue() * (chartImageView.getBoundsInParent().getWidth() - scrollPane.getViewportBounds().getWidth())) / zoomFactor;
        double imageViewY = (mouseY + scrollPane.getVvalue() * (chartImageView.getBoundsInParent().getHeight() - scrollPane.getViewportBounds().getHeight())) / zoomFactor;

        if (event.getDeltaY() > 0) {
            zoomFactor *= 1.1;
        } else {
            zoomFactor /= 1.1;
        }

        applyZoom();

        scrollPane.setHvalue((imageViewX * zoomFactor - mouseX) / (chartImageView.getBoundsInParent().getWidth() - scrollPane.getViewportBounds().getWidth()));
        scrollPane.setVvalue((imageViewY * zoomFactor - mouseY) / (chartImageView.getBoundsInParent().getHeight() - scrollPane.getViewportBounds().getHeight()));
    }

    private void applyZoom() {
        chartImageView.setFitWidth(chartImageView.getImage().getWidth() * zoomFactor);
        chartImageView.setFitHeight(chartImageView.getImage().getHeight() * zoomFactor);
    }

    @FXML
    private void handleGenerateChart() {
        String filePath = "src/main/resources/daily_AAPL.csv";
        try {
            double screenWidth = Screen.getPrimary().getBounds().getWidth();
            double screenHeight = Screen.getPrimary().getBounds().getHeight();
    
            // Generar el gráfico y guardarlo en currentChart
            currentChart = createCandlestickChartFromCSV(filePath);
    
            // Convertir el gráfico en imagen y mostrarlo en el ImageView
            byte[] imageBytes = EncoderUtil.encode(currentChart.createBufferedImage((int) screenWidth, (int) screenHeight), ImageFormat.PNG);
            Image chartImage = new Image(new ByteArrayInputStream(imageBytes));
            chartImageView.setImage(chartImage);
    
            // Restablecer el factor de zoom al generar una nueva imagen
            zoomFactor = 1.0;
            applyZoom();
    
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private JFreeChart createCandlestickChartFromCSV(String filePath) throws IOException {
        OHLCDataset dataset = readCSVToOHLCDataset(filePath);

        JFreeChart chart = ChartFactory.createCandlestickChart(
                "Gráfico de Velas",
                "Fecha",
                "Precio",
                dataset,
                false
        );

        XYPlot plot = (XYPlot) chart.getPlot();
        CandlestickRenderer renderer = new CandlestickRenderer();

        renderer.setCandleWidth(5.0); 
        renderer.setUpPaint(new java.awt.Color(0, 255, 0)); 
        renderer.setDownPaint(new java.awt.Color(255, 0, 0)); 

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
            String[] nextLine;

            reader.readNext(); // Saltar la cabecera del CSV

            while ((nextLine = reader.readNext()) != null) {
                Date date = sdf.parse(nextLine[0]);  
                double openPrice = Double.parseDouble(nextLine[1]);  
                double highPrice = Double.parseDouble(nextLine[2]);  
                double lowPrice = Double.parseDouble(nextLine[3]);   
                double closePrice = Double.parseDouble(nextLine[4]); 
                double volumeValue = Double.parseDouble(nextLine[5]); 

                dates.add(date);
                open.add(openPrice);
                high.add(highPrice);
                low.add(lowPrice);
                close.add(closePrice);
                volume.add(volumeValue);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Date[] dateArray = dates.toArray(new Date[0]);
        double[] openArray = open.stream().mapToDouble(Double::doubleValue).toArray();
        double[] highArray = high.stream().mapToDouble(Double::doubleValue).toArray();
        double[] lowArray = low.stream().mapToDouble(Double::doubleValue).toArray();
        double[] closeArray = close.stream().mapToDouble(Double::doubleValue).toArray();
        double[] volumeArray = volume.stream().mapToDouble(Double::doubleValue).toArray();

        return new DefaultHighLowDataset("Datos", dateArray, highArray, lowArray, openArray, closeArray, volumeArray);
    }

    // Nuevo método para manejar la detección de patrones
    @FXML
    private void handleDetectPatterns() {
        String filePath = "src/main/resources/daily_AAPL.csv";
        try {
            List<CandlestickPattern> patterns = detectCandlestickPatterns(filePath);
            if (patterns.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Patrones no detectados");
                alert.setHeaderText(null);
                alert.setContentText("No se detectaron patrones de velas.");
                alert.showAndWait();
            } else {
                Dialog<ButtonType> dialog = new Dialog<>();
                dialog.setTitle("Patrones Detectados");
    
                VBox vbox = new VBox();
                vbox.setSpacing(10);
    
                for (CandlestickPattern pattern : patterns) {
                    Button patternButton = new Button(pattern.getName() + " - Fecha: " + pattern.getDate());
                    patternButton.setOnAction(event -> highlightPatternOnChart(pattern));
                    vbox.getChildren().add(patternButton);
                }
    
                ScrollPane scrollPane = new ScrollPane(vbox);
                scrollPane.setFitToWidth(true);
                scrollPane.setPrefHeight(400);
    
                dialog.getDialogPane().setContent(scrollPane);
                dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
                dialog.showAndWait();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Método para detectar patrones de velas
    private List<CandlestickPattern> detectCandlestickPatterns(String filePath) throws IOException {
        OHLCDataset dataset = readCSVToOHLCDataset(filePath);
        List<CandlestickPattern> patterns = new ArrayList<>();

        for (int i = 1; i < dataset.getItemCount(0); i++) {
            double open = dataset.getOpenValue(0, i);
            double close = dataset.getCloseValue(0, i);
            double high = dataset.getHighValue(0, i);
            double low = dataset.getLowValue(0, i);
            double prevOpen = dataset.getOpenValue(0, i - 1);
            double prevClose = dataset.getCloseValue(0, i - 1);
            Date date = new Date((long) dataset.getXValue(0, i));

            if (isHammer(open, close, high, low)) {
                patterns.add(new CandlestickPattern("Hammer", date));
            }
            if (isShootingStar(open, close, high, low)) {
                patterns.add(new CandlestickPattern("Shooting Star", date));
            }
            if (isBullishEngulfing(prevOpen, prevClose, open, close)) {
                patterns.add(new CandlestickPattern("Bullish Engulfing", date));
            }
            if (isBearishEngulfing(prevOpen, prevClose, open, close)) {
                patterns.add(new CandlestickPattern("Bearish Engulfing", date));
            }
        }

        return patterns;
    }

    private void highlightPatternOnChart(CandlestickPattern pattern) {
        if (currentChart == null) {
            System.out.println("El gráfico no está generado aún.");
            return;
        }
    
        // Obtener el plot del gráfico actual
        XYPlot plot = currentChart.getXYPlot();
    
        // Encontrar la fecha del patrón
        Date patternDate = pattern.getDate();
        double xValue = patternDate.getTime(); // Convertir la fecha a milisegundos para el eje X
    
        // Crear una anotación en la posición del patrón detectado
        XYTextAnnotation annotation = new XYTextAnnotation(pattern.getName(), xValue, plot.getRangeAxis().getUpperBound());
        annotation.setFont(new Font("SansSerif", Font.BOLD, 12));
        annotation.setPaint(java.awt.Color.RED);
        annotation.setTextAnchor(TextAnchor.TOP_LEFT); // Utiliza TextAnchor en lugar de RectangleAnchor
    
        // Agregar la anotación al gráfico
        plot.addAnnotation(annotation);
    
        // Redibujar el gráfico para incluir la nueva anotación
        try {
            double screenWidth = Screen.getPrimary().getBounds().getWidth();
            double screenHeight = Screen.getPrimary().getBounds().getHeight();
            byte[] imageBytes = EncoderUtil.encode(currentChart.createBufferedImage((int) screenWidth, (int) screenHeight), ImageFormat.PNG);
            Image chartImage = new Image(new ByteArrayInputStream(imageBytes));
            chartImageView.setImage(chartImage);
        } catch (IOException e) {
            e.printStackTrace();
            // Opcionalmente, puedes mostrar un mensaje de error al usuario
            JOptionPane.showMessageDialog(null, "Error al generar la imagen del gráfico: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
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
}
