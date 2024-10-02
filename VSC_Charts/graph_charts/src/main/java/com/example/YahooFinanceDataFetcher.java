package com.example;

import yahoofinance.Stock;
import yahoofinance.historical.HistoricalQuote;
import yahoofinance.YahooFinance;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;

public class YahooFinanceDataFetcher {

    public static void main(String[] args) {
        String symbol = "TSLA"; // Símbolo de Tesla
        try {
            // Configurar el rango de fechas
            Calendar from = Calendar.getInstance();
            from.add(Calendar.YEAR, -5); // Últimos 5 años
            Calendar to = Calendar.getInstance();

            // Obtener datos históricos
            Stock stock = YahooFinance.get(symbol);
            List<HistoricalQuote> historicalQuotes = stock.getHistory(from, to);

            // Crear el archivo CSV
            FileWriter writer = new FileWriter("src/main/resources/data/TSLA_HistoricalData.csv");
            // Escribir la cabecera
            writer.append("dates,open,high,low,close,volume\n");

            // Escribir los datos históricos
            for (HistoricalQuote quote : historicalQuotes) {
                writer.append(quote.getDate().getTimeInMillis() + "," + 
                              quote.getOpen() + "," + 
                              quote.getHigh() + "," + 
                              quote.getLow() + "," + 
                              quote.getClose() + "," + 
                              quote.getVolume() + "\n");
            }
            writer.flush();
            writer.close();

            System.out.println("Datos históricos de " + symbol + " descargados exitosamente.");
        } catch (IOException e) {
            System.err.println("Error al descargar los datos: " + e.getMessage());
        }
    }
}
