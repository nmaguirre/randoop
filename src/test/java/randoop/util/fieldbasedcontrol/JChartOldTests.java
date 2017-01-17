package randoop.util.fieldbasedcontrol;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.text.AttributedCharacterIterator;
import java.text.NumberFormat;

import org.junit.Test;

public class JChartOldTests {

	  @Test
	  public void testNumberFormat() throws IllegalArgumentException, IllegalAccessException, IOException {
		  NumberFormat numberFormat0 = NumberFormat.getCurrencyInstance();
		  AttributedCharacterIterator attributedCharacterIterator2 = numberFormat0.formatToCharacterIterator((java.lang.Object)10.0d);
		  
		  FieldExtensionsStrings fe1 = new FieldExtensionsStrings();
		  HeapDump objectDump = new HeapDump(attributedCharacterIterator2, fe1);
		  objectDump.extensionsToFile("src/test/java/randoop/util/fieldbasedcontrol/numberformatext.txt");
		  objectDump.heapToFile("src/test/java/randoop/util/fieldbasedcontrol/numberformat.dot");
		      
		  /*
		  FieldExtensions fe2 = new FieldExtensions();
		  HeapCanonizer canonizer = new HeapCanonizer(fe2);
		  canonizer.canonizeAndEnlargeExtensions(attributedCharacterIterator2);
		  fe2.toFile("src/test/java/randoop/util/fieldbasedcontrol/numberformatextnew.txt");
		  assertTrue(fe1.equals(fe2));
		  */
	  }
	  
	  @Test
	  public void testPlot() throws IllegalArgumentException, IllegalAccessException, IOException {

		  org.jfree.chart.axis.ValueAxis valueAxis0 = null;
		  org.jfree.chart.plot.CombinedRangeXYPlot combinedRangeXYPlot1 = new org.jfree.chart.plot.CombinedRangeXYPlot(valueAxis0);
		  org.jfree.chart.util.RectangleInsets rectangleInsets2 = org.jfree.chart.axis.CategoryAxis.DEFAULT_AXIS_LABEL_INSETS;
		  combinedRangeXYPlot1.setAxisOffset(rectangleInsets2);
		  int i4 = 5;
		  org.jfree.chart.util.RectangleEdge rectangleEdge5 = combinedRangeXYPlot1.getDomainAxisEdge(5);
		  org.jfree.chart.JFreeChart jFreeChart6 = new org.jfree.chart.JFreeChart((org.jfree.chart.plot.Plot)combinedRangeXYPlot1);
		  int maxDepth = 100000;
		  int maxArray = 100000;
		  String[] ignoredClasses = {};
		  String [] ignoredFields = {};
		  
		  FieldExtensionsStrings ext = new FieldExtensionsStrings();
		  
		  HeapDump objectDump = new HeapDump(valueAxis0, ext);
		  objectDump = new HeapDump(combinedRangeXYPlot1, ext);
		  objectDump = new HeapDump(rectangleInsets2, ext);
		  objectDump = new HeapDump(combinedRangeXYPlot1, ext);
		  objectDump = new HeapDump(rectangleInsets2, ext);
		  objectDump = new HeapDump(rectangleEdge5, ext);
		  objectDump = new HeapDump(jFreeChart6, ext);
		  objectDump = new HeapDump(combinedRangeXYPlot1, ext);
		  
		  objectDump.extensionsToFile("src/test/java/randoop/util/fieldbasedcontrol/plotext.txt");
		  objectDump.heapToFile("src/test/java/randoop/util/fieldbasedcontrol/plot.dot");
		  
	  }
	  
	  @Test
	  public void testPlot2() throws IllegalArgumentException, IllegalAccessException, IOException {

		  FieldExtensionsStrings ext = new FieldExtensionsStrings();
		  org.jfree.chart.axis.ValueAxis valueAxis0 = null;
		  HeapDump objectDump = new HeapDump(valueAxis0, ext);
		  
		  org.jfree.chart.plot.CombinedRangeXYPlot combinedRangeXYPlot1 = new org.jfree.chart.plot.CombinedRangeXYPlot(valueAxis0);
		  objectDump = new HeapDump(combinedRangeXYPlot1, ext);
		  objectDump = new HeapDump(valueAxis0, ext);
		  
		  org.jfree.chart.util.RectangleInsets rectangleInsets2 = org.jfree.chart.axis.CategoryAxis.DEFAULT_AXIS_LABEL_INSETS;
		  objectDump = new HeapDump(org.jfree.chart.axis.CategoryAxis.DEFAULT_AXIS_LABEL_INSETS, ext);
		  objectDump = new HeapDump(rectangleInsets2, ext);
		  
		  combinedRangeXYPlot1.setAxisOffset(rectangleInsets2);
		  objectDump = new HeapDump(combinedRangeXYPlot1, ext);
		  objectDump = new HeapDump(rectangleInsets2, ext);
		  
		  org.jfree.chart.util.RectangleEdge rectangleEdge5 = combinedRangeXYPlot1.getDomainAxisEdge(5);
		  objectDump = new HeapDump(5, ext);
		  objectDump = new HeapDump(rectangleEdge5, ext);
		  
		  org.jfree.chart.JFreeChart jFreeChart6 = new org.jfree.chart.JFreeChart((org.jfree.chart.plot.Plot)combinedRangeXYPlot1);
		  objectDump = new HeapDump(jFreeChart6, ext);
		  objectDump = new HeapDump(combinedRangeXYPlot1, ext);
		  
		  int maxDepth = 100000;
		  int maxArray = 100000;
		  String[] ignoredClasses = {};
		  String [] ignoredFields = {};
		  
		  

		  objectDump.extensionsToFile("src/test/java/randoop/util/fieldbasedcontrol/plot2ext.txt");
		  objectDump.heapToFile("src/test/java/randoop/util/fieldbasedcontrol/plot2.dot");
		  
	  }
	  
	  
	  @Test
	  public void testChart() {
		  FieldExtensionsStrings ext = new FieldExtensionsStrings();

		  org.jfree.chart.axis.ValueAxis valueAxis0 = null;
		  HeapDump objectDump = new HeapDump(valueAxis0, ext);
		  org.jfree.chart.plot.CombinedRangeXYPlot combinedRangeXYPlot1 = new org.jfree.chart.plot.CombinedRangeXYPlot(valueAxis0);
		  objectDump = new HeapDump(combinedRangeXYPlot1, ext);
		  org.jfree.chart.util.RectangleInsets rectangleInsets2 = org.jfree.chart.axis.CategoryAxis.DEFAULT_AXIS_LABEL_INSETS;
		  combinedRangeXYPlot1.setAxisOffset(rectangleInsets2);
		  objectDump = new HeapDump(combinedRangeXYPlot1, ext);
		  combinedRangeXYPlot1.clearAnnotations();
		  objectDump = new HeapDump(combinedRangeXYPlot1, ext);
		  int i5 = 0;
		  org.jfree.data.xy.XYSeries xYSeries6 = null;
		  org.jfree.data.xy.XYSeriesCollection xYSeriesCollection7 = new org.jfree.data.xy.XYSeriesCollection(xYSeries6);
		  objectDump = new HeapDump(xYSeriesCollection7, ext);
		  org.jfree.data.DomainOrder domainOrder8 = xYSeriesCollection7.getDomainOrder();
		  objectDump = new HeapDump(domainOrder8, ext);
		  combinedRangeXYPlot1.setDataset(0, (org.jfree.data.xy.XYDataset)xYSeriesCollection7);
		  objectDump = new HeapDump(xYSeriesCollection7, ext);
		  objectDump = new HeapDump(combinedRangeXYPlot1, ext);
		  java.awt.Stroke stroke10 = org.jfree.chart.plot.XYPlot.DEFAULT_GRIDLINE_STROKE;
		  objectDump = new HeapDump(stroke10, ext);
		  combinedRangeXYPlot1.setRangeMinorGridlineStroke(stroke10);
		  objectDump = new HeapDump(combinedRangeXYPlot1, ext);
		  org.jfree.chart.JFreeChart jFreeChart12 = new org.jfree.chart.JFreeChart((org.jfree.chart.plot.Plot)combinedRangeXYPlot1);
		  objectDump = new HeapDump(combinedRangeXYPlot1, ext);
		  objectDump = new HeapDump(jFreeChart12, ext);
		  
		  objectDump.extensionsToFile("src/test/java/randoop/util/fieldbasedcontrol/chartext.txt");
		  objectDump.heapToFile("src/test/java/randoop/util/fieldbasedcontrol/chart.dot");
		  
		  System.out.println("Extensions1 size: " + ext.size());
		  // System.out.println("Extensions2 size: " + fe2.size());
		  
	 }
	  
	
}
