package com.example.controller;

import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.style.Styler;

import java.io.ByteArrayInputStream;
import java.io.IOException;


public class ChartController {

    @FXML
    private ImageView chartImageView;

    @FXML
    private void handleGenerateChart() {
        // Crear un gráfico con XChart
        XYChart chart = new XYChartBuilder().width(600).height(400).title("Gráfico de Ejemplo").xAxisTitle("X").yAxisTitle("Y").build();

        // Personaliza el gráfico (opcional)
        chart.getStyler().setChartTitleVisible(true);
        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNW);
        chart.getStyler().setMarkerSize(6);

        // Agregar datos de ejemplo
        chart.addSeries("Datos", new double[] { 0.0, 1.0, 2.0, 3.0 }, new double[] { 2.0, 1.0, 0.0, 3.0 });

        try {
            // Convertir el gráfico a una imagen
            byte[] imageBytes = BitmapEncoder.getBitmapBytes(chart, BitmapEncoder.BitmapFormat.PNG);
            Image chartImage = new Image(new ByteArrayInputStream(imageBytes));
            
            // Mostrar la imagen en el ImageView
            chartImageView.setImage(chartImage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
