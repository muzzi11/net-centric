package com.example.projectbat;

import java.util.Arrays;

import com.androidplot.series.XYSeries;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.StepFormatter;
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
		
		Bundle extra = getIntent().getExtras();
		short data[] = extra.getShortArray("data");
		int size = extra.getInt("size");
		// Create a couple arrays of y-values to plot:
        Number[] numbers = new Number[size/10];
        
        for(int i = 0; i < numbers.length; ++i)
        {
        	float avg = 0.0f;
        	int k;
        	for(k = i*10; k < i*10 + 10 && k < size; ++k)
        		avg += data[k];
        	avg /= (k - i*10);
        	numbers[i] = avg / (float)Short.MAX_VALUE;
        }
        
        XYSeries serie = new SimpleXYSeries(Arrays.asList(numbers), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Serie");
        plot.addSeries(serie, new StepFormatter(Color.rgb(0, 0, 200), Color.rgb(0, 0, 100)));
        
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
