package com.example.controller;

import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.scene.input.ScrollEvent;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.CandlestickRenderer;
import org.jfree.data.xy.DefaultHighLowDataset;
import org.jfree.data.xy.OHLCDataset;
import com.opencsv.CSVReader;
import org.jfree.chart.encoders.EncoderUtil;
import org.jfree.chart.encoders.ImageFormat;

import java.io.ByteArrayInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;



public class ChartController {

    @FXML
    private ImageView chartImageView;

    @FXML
    private ScrollPane scrollPane;

    private double zoomFactor = 1.0;

    @FXML
    private void initialize() {
        if (chartImageView != null) {
            // No vincular el tamaño del ImageView al tamaño del ScrollPane para permitir zoom
            chartImageView.setPreserveRatio(true);

            // Añadir el control del zoom utilizando la rueda del ratón
            scrollPane.addEventFilter(ScrollEvent.SCROLL, event -> {
                if (event.getDeltaY() != 0) {
                    zoom(event);
                }
                event.consume();
            });

            // Configurar las barras de desplazamiento para que aparezcan cuando sea necesario
            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        } else {
            System.out.println("El chartImageView no ha sido cargado correctamente desde el archivo FXML.");
        }
    }

    private void zoom(ScrollEvent event) {
        // Coordenadas actuales del ratón relativas al ScrollPane
        double mouseX = event.getX();
        double mouseY = event.getY();

        // Coordenadas actuales del ratón relativas al ImageView
        double imageViewX = (mouseX + scrollPane.getHvalue() * (chartImageView.getBoundsInParent().getWidth() - scrollPane.getViewportBounds().getWidth())) / zoomFactor;
        double imageViewY = (mouseY + scrollPane.getVvalue() * (chartImageView.getBoundsInParent().getHeight() - scrollPane.getViewportBounds().getHeight())) / zoomFactor;

        // Ajustar el factor de zoom
        if (event.getDeltaY() > 0) {
            zoomFactor *= 1.1;
        } else {
            zoomFactor /= 1.1;
        }

        // Aplicar el nuevo zoom
        applyZoom();

        // Ajustar las barras de desplazamiento para mantener el punto bajo el ratón en la misma posición visual
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

            JFreeChart chart = createCandlestickChartFromCSV(filePath);

            byte[] imageBytes = EncoderUtil.encode(chart.createBufferedImage((int) screenWidth, (int) screenHeight), ImageFormat.PNG);

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
}
