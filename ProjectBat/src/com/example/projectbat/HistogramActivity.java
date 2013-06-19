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

import edu.emory.mathcs.jtransforms.fft.FloatFFT_1D;

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
		
		float fft[] = new float[data.length];
		for(int i = 0; i < fft.length; ++i)
		{
			fft[i] = data[i] / (float) Short.MAX_VALUE;
		}
		
		FloatFFT_1D floatFFT = new FloatFFT_1D(fft.length);
		floatFFT.realForward(fft);
		
		// Create a couple arrays of y-values to plot:
        Number[] numbers = new Number[fft.length / 10];
        final int targetFrequency = 4410;
        for(int i = 0; i < numbers.length; ++i)
        {
        	numbers[i] = fft[fft.length * targetFrequency / 44100 - numbers.length / 2 + i];
        }
        
        XYSeries serie = new SimpleXYSeries(Arrays.asList(numbers), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "FFT");
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
