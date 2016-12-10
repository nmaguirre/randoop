package randoop.util.fieldbasedcontrol;

import java.io.IOException;

import org.jfree.data.xy.XYDataset;
import org.junit.Test;

public class JChartNewTests {

	@Test
	public void infiniteLoopHashCode() throws IOException {

	    FieldExtensions fe = new FieldExtensions();
	    HeapCanonizer canonizer = new HeapCanonizer(fe);
		
		XYDataset xYDataset0 = null;
		canonizer.canonizeAndEnlargeExtensions(xYDataset0);
		org.jfree.chart.axis.LogAxis logAxis2 = new org.jfree.chart.axis.LogAxis("hi!");
		canonizer.canonizeAndEnlargeExtensions(logAxis2);
		org.jfree.chart.axis.PeriodAxis periodAxis4 = new org.jfree.chart.axis.PeriodAxis("hi!");
		// BUG: PeriodAxis makes the heap change in different executions!
		// It is because it depends on the current time!
		canonizer.canonizeAndEnlargeExtensions(periodAxis4);
		periodAxis4.setTickLabelsVisible(false);
		//canonizer.canonizeAndEnlargeExtensions(periodAxis4);
		periodAxis4.resizeRange(100.0d, (double)(short)0);
		//canonizer.canonizeAndEnlargeExtensions(periodAxis4);
		org.jfree.chart.renderer.xy.XYItemRenderer xYItemRenderer10 = null;
		//canonizer.canonizeAndEnlargeExtensions(xYItemRenderer10);
		org.jfree.chart.plot.XYPlot xYPlot11 = new org.jfree.chart.plot.XYPlot(xYDataset0, (org.jfree.chart.axis.ValueAxis)logAxis2, (org.jfree.chart.axis.ValueAxis)periodAxis4, xYItemRenderer10);
		//canonizer.canonizeAndEnlargeExtensions(xYPlot11);
		java.lang.String str12 = logAxis2.getLabel();
		//canonizer.canonizeAndEnlargeExtensions(str12);

	    
	    //fe.toFile("src/test/java/randoop/util/fieldbasedcontrol/chartextnew.txt");
	    System.out.println("Extensions size: " + fe.size());
	    
	}
	
	  @Test
	  public void testChart() {
		  FieldExtensions fe = new FieldExtensions();
		  HeapCanonizer canonizer = new HeapCanonizer(fe);
		  
		  org.jfree.chart.axis.ValueAxis valueAxis0 = null;
		  canonizer.canonizeAndEnlargeExtensions(valueAxis0);
		  org.jfree.chart.plot.CombinedRangeXYPlot combinedRangeXYPlot1 = new org.jfree.chart.plot.CombinedRangeXYPlot(valueAxis0);
		  canonizer.canonizeAndEnlargeExtensions(combinedRangeXYPlot1);
		  org.jfree.chart.util.RectangleInsets rectangleInsets2 = org.jfree.chart.axis.CategoryAxis.DEFAULT_AXIS_LABEL_INSETS;
		  canonizer.canonizeAndEnlargeExtensions(rectangleInsets2);
		  combinedRangeXYPlot1.setAxisOffset(rectangleInsets2);
		  canonizer.canonizeAndEnlargeExtensions(combinedRangeXYPlot1);
		  combinedRangeXYPlot1.clearAnnotations();
		  canonizer.canonizeAndEnlargeExtensions(combinedRangeXYPlot1);
		  int i5 = 0;
		  org.jfree.data.xy.XYSeries xYSeries6 = null;
		  canonizer.canonizeAndEnlargeExtensions(xYSeries6);
		  org.jfree.data.xy.XYSeriesCollection xYSeriesCollection7 = new org.jfree.data.xy.XYSeriesCollection(xYSeries6);
		  canonizer.canonizeAndEnlargeExtensions(xYSeriesCollection7);
		  org.jfree.data.DomainOrder domainOrder8 = xYSeriesCollection7.getDomainOrder();
		  canonizer.canonizeAndEnlargeExtensions(domainOrder8);
		  combinedRangeXYPlot1.setDataset(0, (org.jfree.data.xy.XYDataset)xYSeriesCollection7);
		  canonizer.canonizeAndEnlargeExtensions(combinedRangeXYPlot1);
		  java.awt.Stroke stroke10 = org.jfree.chart.plot.XYPlot.DEFAULT_GRIDLINE_STROKE;
		  canonizer.canonizeAndEnlargeExtensions(stroke10);
		  combinedRangeXYPlot1.setRangeMinorGridlineStroke(stroke10);
		  canonizer.canonizeAndEnlargeExtensions(combinedRangeXYPlot1);
		  org.jfree.chart.JFreeChart jFreeChart12 = new org.jfree.chart.JFreeChart((org.jfree.chart.plot.Plot)combinedRangeXYPlot1);
		  canonizer.canonizeAndEnlargeExtensions(jFreeChart12);
		   
		  System.out.println("Extensions size: " + fe.size());

	  }
	  
	  
	  @Test
	  public void testPlot() throws IllegalArgumentException, IllegalAccessException, IOException {

		  FieldExtensions fe = new FieldExtensions();
		  HeapCanonizer canonizer = new HeapCanonizer(fe);
		  
		  org.jfree.chart.axis.ValueAxis valueAxis0 = null;
		  canonizer.canonizeAndEnlargeExtensions(valueAxis0);
		  
		  org.jfree.chart.plot.CombinedRangeXYPlot combinedRangeXYPlot1 = new org.jfree.chart.plot.CombinedRangeXYPlot(valueAxis0);
		  canonizer.canonizeAndEnlargeExtensions(combinedRangeXYPlot1);
		  
		  org.jfree.chart.util.RectangleInsets rectangleInsets2 = org.jfree.chart.axis.CategoryAxis.DEFAULT_AXIS_LABEL_INSETS;
		  canonizer.canonizeAndEnlargeExtensions(rectangleInsets2);
		  
		  combinedRangeXYPlot1.setAxisOffset(rectangleInsets2);
		  canonizer.canonizeAndEnlargeExtensions(combinedRangeXYPlot1);
		  
		  org.jfree.chart.util.RectangleEdge rectangleEdge5 = combinedRangeXYPlot1.getDomainAxisEdge(5);
		  canonizer.canonizeAndEnlargeExtensions(rectangleEdge5);
		  
		  org.jfree.chart.JFreeChart jFreeChart6 = new org.jfree.chart.JFreeChart((org.jfree.chart.plot.Plot)combinedRangeXYPlot1);
		  canonizer.canonizeAndEnlargeExtensions(jFreeChart6);
	  
		  System.out.println("Extensions size: " + fe.size());
	  }
	  
	  @Test
	  public void testBrokenHash() throws IllegalArgumentException, IllegalAccessException, IOException {

		  
		  FieldExtensions fe = new FieldExtensions();
		  HeapCanonizer canonizer = new HeapCanonizer(fe);
		  
		  org.jfree.chart.axis.ValueAxis valueAxis0 = null;
		  canonizer.canonizeAndEnlargeExtensions(valueAxis0);

		  org.jfree.chart.plot.CombinedRangeXYPlot combinedRangeXYPlot1 = new org.jfree.chart.plot.CombinedRangeXYPlot(valueAxis0);
		  canonizer.canonizeAndEnlargeExtensions(combinedRangeXYPlot1);
		  
		  org.jfree.chart.util.RectangleInsets rectangleInsets2 = org.jfree.chart.axis.CategoryAxis.DEFAULT_AXIS_LABEL_INSETS;
		  canonizer.canonizeAndEnlargeExtensions(rectangleInsets2);
		  
		  combinedRangeXYPlot1.setAxisOffset(rectangleInsets2);
		  canonizer.canonizeAndEnlargeExtensions(combinedRangeXYPlot1);
		  
		  java.lang.String str4 = combinedRangeXYPlot1.getPlotType();
		  canonizer.canonizeAndEnlargeExtensions(str4);
		  
		  org.jfree.data.xy.XYSeries xYSeries6 = null;
		  canonizer.canonizeAndEnlargeExtensions(xYSeries6);
		  
		  org.jfree.data.xy.XYSeriesCollection xYSeriesCollection7 = new org.jfree.data.xy.XYSeriesCollection(xYSeries6);
		  canonizer.canonizeAndEnlargeExtensions(xYSeriesCollection7);
		  
		  org.jfree.data.DomainOrder domainOrder8 = xYSeriesCollection7.getDomainOrder();
		  canonizer.canonizeAndEnlargeExtensions(domainOrder8);
		  
		  combinedRangeXYPlot1.setDataset(0, (org.jfree.data.xy.XYDataset)xYSeriesCollection7);
		  canonizer.canonizeAndEnlargeExtensions(combinedRangeXYPlot1);
		  
		  org.jfree.chart.event.AxisChangeEvent axisChangeEvent10 = null;
		  canonizer.canonizeAndEnlargeExtensions(axisChangeEvent10);
		  
		  combinedRangeXYPlot1.axisChanged(axisChangeEvent10);
		  canonizer.canonizeAndEnlargeExtensions(combinedRangeXYPlot1);
		  
		  java.awt.Paint paint12 = combinedRangeXYPlot1.getDomainZeroBaselinePaint();
		  canonizer.canonizeAndEnlargeExtensions(paint12);
		  
		  org.jfree.chart.title.LegendTitle legendTitle13 = new org.jfree.chart.title.LegendTitle((org.jfree.chart.LegendItemSource)combinedRangeXYPlot1);
		  canonizer.canonizeAndEnlargeExtensions(legendTitle13);
		  
		  org.jfree.chart.util.VerticalAlignment verticalAlignment14 = legendTitle13.getVerticalAlignment();
		  //canonizer.canonizeAndEnlargeExtensions(legendTitle13);
		  canonizer.canonizeAndEnlargeExtensions(verticalAlignment14);
		  
		  org.jfree.chart.event.TitleChangeListener titleChangeListener15 = null;
		  canonizer.canonizeAndEnlargeExtensions(titleChangeListener15);
		  
		  legendTitle13.addChangeListener(titleChangeListener15);
		  canonizer.canonizeAndEnlargeExtensions(legendTitle13);

		  System.out.println("Extensions size: " + fe.size());
	  }
}
