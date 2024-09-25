package com.example.controller;

import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.PieChart;
import org.knowm.xchart.PieChartBuilder;
import org.knowm.xchart.CategoryChart;
import org.knowm.xchart.CategoryChartBuilder;
import org.knowm.xchart.style.Styler;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ChartController {

    @FXML
    private ImageView chartImageView;

    @FXML
    private ChoiceBox<String> chartTypeChoiceBox;

    @FXML
    private TextField xAxisInput;

    @FXML
    private TextField yAxisInput;

    @FXML
    public void initialize() {
        // Inicializar el ChoiceBox con los tipos de gráficos
        chartTypeChoiceBox.getItems().addAll("Línea", "Barras", "Circular");
        chartTypeChoiceBox.setValue("Línea");  // Valor por defecto
    }

    @FXML
    private void handleGenerateChart() {
        // Obtener valores de los campos de entrada
        String xAxisText = xAxisInput.getText();
        String yAxisText = yAxisInput.getText();

        // Convertir los valores en listas de números
        List<Double> xAxisValues = parseInputValues(xAxisText);
        List<Double> yAxisValues = parseInputValues(yAxisText);

        // Obtener el tipo de gráfico seleccionado
        String selectedChartType = chartTypeChoiceBox.getValue();

        // Generar el gráfico basado en el tipo seleccionado
        try {
            byte[] imageBytes;
            if (selectedChartType.equals("Línea")) {
                imageBytes = generateLineChart(xAxisValues, yAxisValues);
            } else if (selectedChartType.equals("Barras")) {
                imageBytes = generateBarChart(xAxisValues, yAxisValues);
            } else {
                imageBytes = generatePieChart(yAxisValues);
            }

            // Convertir los bytes en una imagen y mostrar en el ImageView
            Image chartImage = new Image(new ByteArrayInputStream(imageBytes));
            chartImageView.setImage(chartImage);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Método para generar gráfico de líneas
    private byte[] generateLineChart(List<Double> xValues, List<Double> yValues) throws IOException {
        XYChart chart = new XYChartBuilder().width(600).height(400).title("Gráfico de Línea").xAxisTitle("X").yAxisTitle("Y").build();
        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNW);
        chart.addSeries("Datos", xValues, yValues);
        return BitmapEncoder.getBitmapBytes(chart, BitmapEncoder.BitmapFormat.PNG);
    }

    // Método para generar gráfico de barras
    private byte[] generateBarChart(List<Double> xValues, List<Double> yValues) throws IOException {
        CategoryChart chart = new CategoryChartBuilder().width(600).height(400).title("Gráfico de Barras").xAxisTitle("X").yAxisTitle("Y").build();
        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNW);
        chart.addSeries("Datos", xValues, yValues);
        return BitmapEncoder.getBitmapBytes(chart, BitmapEncoder.BitmapFormat.PNG);
    }

    // Método para generar gráfico circular
    private byte[] generatePieChart(List<Double> values) throws IOException {
        PieChart chart = new PieChartBuilder().width(600).height(400).title("Gráfico Circular").build();
        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNW);
        for (int i = 0; i < values.size(); i++) {
            chart.addSeries("Segmento " + (i + 1), values.get(i));
        }
        return BitmapEncoder.getBitmapBytes(chart, BitmapEncoder.BitmapFormat.PNG);
    }

    // Método para convertir la cadena de entrada en una lista de números
    private List<Double> parseInputValues(String inputText) {
        return Arrays.stream(inputText.split(","))
                     .map(String::trim)
                     .map(Double::parseDouble)
                     .collect(Collectors.toList());
    }
}
