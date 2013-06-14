package com.example.projectbat;

import java.util.Arrays;

import com.androidplot.series.XYSeries;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.PointLabelFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;

import android.os.Bundle;
import android.app.Activity;
import android.graphics.Color;
import android.view.Menu;

public class HistogramActivity extends Activity
{
	private XYPlot plot;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_histogram);
		
		plot = (XYPlot)findViewById(R.id.plot);
		
		// Create a couple arrays of y-values to plot:
        Number[] serieNumbers = {1, 8, 5, 2, 7, 4};
        
        XYSeries serie = new SimpleXYSeries(Arrays.asList(serieNumbers), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Serie");
        plot.addSeries(serie,
                new LineAndPointFormatter(Color.rgb(0, 0, 200), Color.rgb(0, 0, 100), null, new PointLabelFormatter(Color.WHITE)));
        
        plot.setTicksPerRangeLabel(3);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.histogram, menu);
		return true;
	}

}
