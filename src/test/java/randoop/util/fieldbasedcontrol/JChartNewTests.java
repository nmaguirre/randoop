package randoop.util.fieldbasedcontrol;

import org.jfree.data.xy.XYDataset;
import org.junit.Test;

public class JChartNewTests {

	@Test
	public void infiniteLoopHashCode() {
		
		XYDataset xYDataset0 = null;
		org.jfree.chart.axis.LogAxis logAxis2 = new org.jfree.chart.axis.LogAxis("hi!");
		org.jfree.chart.axis.PeriodAxis periodAxis4 = new org.jfree.chart.axis.PeriodAxis("hi!");
		periodAxis4.setTickLabelsVisible(false);
		periodAxis4.resizeRange(100.0d, (double)(short)0);
		org.jfree.chart.renderer.xy.XYItemRenderer xYItemRenderer10 = null;
		org.jfree.chart.plot.XYPlot xYPlot11 = new org.jfree.chart.plot.XYPlot(xYDataset0, (org.jfree.chart.axis.ValueAxis)logAxis2, (org.jfree.chart.axis.ValueAxis)periodAxis4, xYItemRenderer10);
		java.lang.String str12 = logAxis2.getLabel();
	}
	
	
}
