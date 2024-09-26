package com.example.controller;

import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
    private void handleGenerateChart() {
        // Ruta del archivo CSV
        String filePath = "src/main/resources/daily_AAPL.csv";

        try {
            // Generar gráfico de velas desde el archivo CSV
            JFreeChart chart = createCandlestickChartFromCSV(filePath);

            // Convertir el gráfico a una imagen PNG
            byte[] imageBytes = EncoderUtil.encode(chart.createBufferedImage(800, 600), ImageFormat.PNG);

            // Mostrar la imagen en el ImageView
            Image chartImage = new Image(new ByteArrayInputStream(imageBytes));
            chartImageView.setImage(chartImage);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Método para generar gráfico de velas a partir de un archivo CSV
    private JFreeChart createCandlestickChartFromCSV(String filePath) throws IOException {
        // Leer el archivo CSV y convertir los datos
        OHLCDataset dataset = readCSVToOHLCDataset(filePath);

        // Crear el gráfico de velas
        JFreeChart chart = ChartFactory.createCandlestickChart(
                "Gráfico de Velas",
                "Fecha",
                "Precio",
                dataset,
                false
        );

        XYPlot plot = (XYPlot) chart.getPlot();
        CandlestickRenderer renderer = new CandlestickRenderer();
        
        renderer.setCandleWidth(5.0); // Ancho de las velas
        // Usar colores para velas alcistas y bajistas
        renderer.setUpPaint(new java.awt.Color(0, 255, 0)); // Verde para velas alcistas
        renderer.setDownPaint(new java.awt.Color(255, 0, 0)); // Rojo para velas bajistas

        plot.setRenderer(renderer);
        
        return chart;
    }

    // Método para convertir un archivo CSV en un OHLCDataset
    private OHLCDataset readCSVToOHLCDataset(String filePath) throws IOException {
        // Usamos listas dinámicas para almacenar los datos
        List<Date> dates = new ArrayList<>();
        List<Double> open = new ArrayList<>();
        List<Double> high = new ArrayList<>();
        List<Double> low = new ArrayList<>();
        List<Double> close = new ArrayList<>();
        List<Double> volume = new ArrayList<>();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        // Abrir el archivo CSV
        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            String[] nextLine;

            reader.readNext(); // Saltar la cabecera del CSV

            // Leer las líneas del CSV
            while ((nextLine = reader.readNext()) != null) {
                Date date = sdf.parse(nextLine[0]);  // Columna "dates"
                double openPrice = Double.parseDouble(nextLine[1]);  // Columna "open"
                double highPrice = Double.parseDouble(nextLine[2]);  // Columna "high"
                double lowPrice = Double.parseDouble(nextLine[3]);   // Columna "low"
                double closePrice = Double.parseDouble(nextLine[4]); // Columna "close"
                double volumeValue = Double.parseDouble(nextLine[5]); // Columna "volume"

                // Agregar los datos a las listas
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

        // Convertir las listas a arrays
        Date[] dateArray = dates.toArray(new Date[0]);
        double[] openArray = open.stream().mapToDouble(Double::doubleValue).toArray();
        double[] highArray = high.stream().mapToDouble(Double::doubleValue).toArray();
        double[] lowArray = low.stream().mapToDouble(Double::doubleValue).toArray();
        double[] closeArray = close.stream().mapToDouble(Double::doubleValue).toArray();
        double[] volumeArray = volume.stream().mapToDouble(Double::doubleValue).toArray();

        // Aquí está el orden correcto para DefaultHighLowDataset: fecha, alto, bajo, apertura, cierre, volumen
        return new DefaultHighLowDataset("Datos", dateArray, highArray, lowArray, openArray, closeArray, volumeArray);
    }
}
