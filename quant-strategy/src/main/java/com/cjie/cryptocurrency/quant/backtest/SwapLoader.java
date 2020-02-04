package com.cjie.cryptocurrency.quant.backtest;


import com.opencsv.CSVReader;
import org.ta4j.core.Bar;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseTimeSeries;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.num.DoubleNum;

import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Class for loading example/testing data from this repository
 */
public class SwapLoader{

    private static final DateTimeFormatter DATE_FORMAT_HOURLY_MINUTE = DateTimeFormatter.ofPattern("yyyy-M-d HH:mm:ss z");
    private static final DateTimeFormatter DATE_FORMAT_Daily = DateTimeFormatter.ofPattern("yyyy-M-d");


    public TimeSeries getHourlyTimeSeries(String pathToCsv, String name) throws Exception {

        List<Bar> ticks = new ArrayList<>();
        CSVReader reader = null;
        try {
            reader = new CSVReader(new FileReader(pathToCsv));
            String[] line;
            reader.readNext();
            while ((line = reader.readNext()) != null) {
                ZonedDateTime date = ZonedDateTime.parse(line[0] + " PST", DATE_FORMAT_HOURLY_MINUTE);
                double open = Double.parseDouble(line[1]);
                double high = Double.parseDouble(line[2]);
                double low = Double.parseDouble(line[3]);
                double close = Double.parseDouble(line[4]);
                double volume = Double.parseDouble(line[5]);

                ticks.add(new BaseBar(date, DoubleNum.valueOf(open), DoubleNum.valueOf(high), DoubleNum.valueOf(low), DoubleNum.valueOf(close), DoubleNum.valueOf(volume), DoubleNum.valueOf(0)));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new BaseTimeSeries(name, ticks);
    }

    public TimeSeries getMinuteTimeSeries(String pathToCsv, String name){
        try {
            return getHourlyTimeSeries(pathToCsv, name);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }


	
	
}