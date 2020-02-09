package com.cjie.cryptocurrency.quant.backtest;


import com.opencsv.CSVReader;
import org.ta4j.core.Bar;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseBarSeries;;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.num.PrecisionNum;

import java.io.FileReader;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Class for loading example/testing data from this repository
 */
public class SwapLoader{

    private static final DateTimeFormatter DATE_FORMAT_HOURLY_MINUTE = DateTimeFormatter.ofPattern("yyyy-M-d HH:mm:ss z");
    private static final DateTimeFormatter DATE_FORMAT_Daily = DateTimeFormatter.ofPattern("yyyy-M-d");


    public BaseBarSeries getHourlyTimeSeries(int type, String pathToCsv, String name) throws Exception {

        List<Bar> ticks = new ArrayList<>();
        CSVReader reader = null;
        try {
            reader = new CSVReader(new FileReader(pathToCsv));
            String[] line;
            reader.readNext();
            while ((line = reader.readNext()) != null) {
                ZonedDateTime date = ZonedDateTime.parse(line[0] + " GMT", DATE_FORMAT_HOURLY_MINUTE);
                double open = Double.parseDouble(line[1]);
                double high = Double.parseDouble(line[2]);
                double low = Double.parseDouble(line[3]);
                double close = Double.parseDouble(line[4]);
                double volume = Double.parseDouble(line[5]);

                ticks.add(new BaseBar(Duration.ofMinutes(type), date, PrecisionNum.valueOf(open), PrecisionNum.valueOf(high), PrecisionNum.valueOf(low), PrecisionNum.valueOf(close), PrecisionNum.valueOf(volume), PrecisionNum.valueOf(0)));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new BaseBarSeries(name, ticks);
    }

    public BaseBarSeries getMinuteTimeSeries(int type, String pathToCsv, String name){
        try {
            return getHourlyTimeSeries(type, pathToCsv, name);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }


	
	
}