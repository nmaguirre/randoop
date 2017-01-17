package randoop.util.fieldbasedcontrol;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import org.jfree.data.xy.XYDataset;
import org.junit.Test;

public class JChartNewTests {

	@Test
	public void arrayTooLarge() throws IOException {
		FieldExtensionsStrings fe2 = new FieldExtensionsStrings();
		HeapCanonizer canonizer2 = new HeapCanonizerListStore(fe2, false);	
		
		org.jfree.chart.plot.CombinedRangeXYPlot combinedRangeXYPlot0 = new org.jfree.chart.plot.CombinedRangeXYPlot();
		org.jfree.chart.axis.NumberAxis numberAxis3 = new org.jfree.chart.axis.NumberAxis("hi!");
		combinedRangeXYPlot0.setRangeAxis((int)(byte)100, (org.jfree.chart.axis.ValueAxis)numberAxis3);
		java.awt.Paint paint5 = combinedRangeXYPlot0.getDomainGridlinePaint();
		org.jfree.chart.plot.CombinedRangeXYPlot combinedRangeXYPlot7 = new org.jfree.chart.plot.CombinedRangeXYPlot();
		org.jfree.chart.axis.AxisLocation axisLocation9 = org.jfree.chart.axis.AxisLocation.TOP_OR_RIGHT;
		combinedRangeXYPlot7.setDomainAxisLocation(4, axisLocation9);
		combinedRangeXYPlot0.setRangeAxisLocation(2958465, axisLocation9);
	
		
		canonizer2.canonizeAndEnlargeExtensions(combinedRangeXYPlot0);
		//canonizer2.canonizeAndEnlargeExtensions(numberAxis12);

		// fe.toFile("/Users/pponzio/prueba-extensiones.txt");
		System.out.println("Extensions size: " + fe2.size());
	
	}
	
	
	@Test
	public void infiniteLoopHashCode() throws IOException {

		FieldExtensionsStrings fe = new FieldExtensionsStrings();
		HeapCanonizer canonizer = new HeapCanonizerMapStore(fe, false);

		XYDataset xYDataset0 = null;
		canonizer.canonizeAndEnlargeExtensions(xYDataset0);
		org.jfree.chart.axis.LogAxis logAxis2 = new org.jfree.chart.axis.LogAxis("hi!");
		canonizer.canonizeAndEnlargeExtensions(logAxis2);
		org.jfree.chart.axis.PeriodAxis periodAxis4 = new org.jfree.chart.axis.PeriodAxis("hi!");
		// BUG: PeriodAxis makes the heap change in different executions!
		// It is because it depends on the current time!
		canonizer.canonizeAndEnlargeExtensions(periodAxis4);
		periodAxis4.setTickLabelsVisible(false);
		// canonizer.canonizeAndEnlargeExtensions(periodAxis4);
		periodAxis4.resizeRange(100.0d, (double) (short) 0);
		// canonizer.canonizeAndEnlargeExtensions(periodAxis4);
		org.jfree.chart.renderer.xy.XYItemRenderer xYItemRenderer10 = null;
		// canonizer.canonizeAndEnlargeExtensions(xYItemRenderer10);
		org.jfree.chart.plot.XYPlot xYPlot11 = new org.jfree.chart.plot.XYPlot(xYDataset0,
				(org.jfree.chart.axis.ValueAxis) logAxis2, (org.jfree.chart.axis.ValueAxis) periodAxis4,
				xYItemRenderer10);
		// canonizer.canonizeAndEnlargeExtensions(xYPlot11);
		java.lang.String str12 = logAxis2.getLabel();
		// canonizer.canonizeAndEnlargeExtensions(str12);

		// fe.toFile("src/test/java/randoop/util/fieldbasedcontrol/chartextnew.txt");
		System.out.println("Extensions size: " + fe.size());

	}

	@Test
	public void extensionsVeryLarge() throws IOException {

		FieldExtensionsStrings fe1 = new FieldExtensionsStrings();
		FieldExtensionsStrings fe2 = new FieldExtensionsStrings();
		HeapCanonizer canonizer1 = new HeapCanonizerMapStore(fe1, false);
		HeapCanonizer canonizer2 = new HeapCanonizerListStore(fe2, false);
	    HeapCanonizerRuntimeEfficient canonizer3 = new HeapCanonizerRuntimeEfficient(false);
		org.jfree.chart.plot.CombinedRangeXYPlot combinedRangeXYPlot0 = new org.jfree.chart.plot.CombinedRangeXYPlot();
		org.jfree.chart.axis.NumberAxis numberAxis3 = new org.jfree.chart.axis.NumberAxis("hi!");
		java.awt.Font font4 = org.jfree.chart.JFreeChart.DEFAULT_TITLE_FONT;
		numberAxis3.setTickLabelFont(font4);
		combinedRangeXYPlot0.setDomainAxis(3, (org.jfree.chart.axis.ValueAxis) numberAxis3, true);
		org.jfree.chart.plot.CombinedRangeXYPlot combinedRangeXYPlot9 = new org.jfree.chart.plot.CombinedRangeXYPlot();
		org.jfree.chart.axis.NumberAxis numberAxis12 = new org.jfree.chart.axis.NumberAxis("hi!");
		java.awt.Font font13 = org.jfree.chart.JFreeChart.DEFAULT_TITLE_FONT;
		numberAxis12.setTickLabelFont(font13);
		combinedRangeXYPlot9.setDomainAxis(3, (org.jfree.chart.axis.ValueAxis) numberAxis12, true);
		combinedRangeXYPlot0.setDomainAxis(2958465, (org.jfree.chart.axis.ValueAxis) numberAxis12, true);
		//canonizer1.canonizeAndEnlargeExtensions(combinedRangeXYPlot0);
		//canonizer1.canonizeAndEnlargeExtensions(numberAxis12);

// 		canonizer2.canonizeAndEnlargeExtensions(combinedRangeXYPlot0);
//		canonizer2.canonizeAndEnlargeExtensions(numberAxis12);
	    canonizer3.traverseBreadthFirstAndEnlargeExtensions(combinedRangeXYPlot0);
	    canonizer3.traverseBreadthFirstAndEnlargeExtensions(numberAxis12);

		// fe.toFile("/Users/pponzio/prueba-extensiones.txt");
		System.out.println("Extensions size: " + fe1.size());
		System.out.println("Extensions size: " + fe2.size());
		System.out.println("Extensions size: " + canonizer3.getExtensions().size());

	}

	@Test
	public void testChart() {
		FieldExtensionsStrings fe = new FieldExtensionsStrings();
		HeapCanonizer canonizer = new HeapCanonizerMapStore(fe, true);

		org.jfree.chart.axis.ValueAxis valueAxis0 = null;
		canonizer.canonizeAndEnlargeExtensions(valueAxis0);
		org.jfree.chart.plot.CombinedRangeXYPlot combinedRangeXYPlot1 = new org.jfree.chart.plot.CombinedRangeXYPlot(
				valueAxis0);
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
		combinedRangeXYPlot1.setDataset(0, (org.jfree.data.xy.XYDataset) xYSeriesCollection7);
		canonizer.canonizeAndEnlargeExtensions(combinedRangeXYPlot1);
		java.awt.Stroke stroke10 = org.jfree.chart.plot.XYPlot.DEFAULT_GRIDLINE_STROKE;
		canonizer.canonizeAndEnlargeExtensions(stroke10);
		combinedRangeXYPlot1.setRangeMinorGridlineStroke(stroke10);
		canonizer.canonizeAndEnlargeExtensions(combinedRangeXYPlot1);
		org.jfree.chart.JFreeChart jFreeChart12 = new org.jfree.chart.JFreeChart(
				(org.jfree.chart.plot.Plot) combinedRangeXYPlot1);
		canonizer.canonizeAndEnlargeExtensions(jFreeChart12);

		System.out.println("Extensions size: " + fe.size());

	}

	@Test
	public void testPlot() throws IllegalArgumentException, IllegalAccessException, IOException {

		FieldExtensionsStrings fe = new FieldExtensionsStrings();
		HeapCanonizer canonizer = new HeapCanonizerMapStore(fe, true);

		org.jfree.chart.axis.ValueAxis valueAxis0 = null;
		canonizer.canonizeAndEnlargeExtensions(valueAxis0);

		org.jfree.chart.plot.CombinedRangeXYPlot combinedRangeXYPlot1 = new org.jfree.chart.plot.CombinedRangeXYPlot(
				valueAxis0);
		canonizer.canonizeAndEnlargeExtensions(combinedRangeXYPlot1);

		org.jfree.chart.util.RectangleInsets rectangleInsets2 = org.jfree.chart.axis.CategoryAxis.DEFAULT_AXIS_LABEL_INSETS;
		canonizer.canonizeAndEnlargeExtensions(rectangleInsets2);

		combinedRangeXYPlot1.setAxisOffset(rectangleInsets2);
		canonizer.canonizeAndEnlargeExtensions(combinedRangeXYPlot1);

		org.jfree.chart.util.RectangleEdge rectangleEdge5 = combinedRangeXYPlot1.getDomainAxisEdge(5);
		canonizer.canonizeAndEnlargeExtensions(rectangleEdge5);

		org.jfree.chart.JFreeChart jFreeChart6 = new org.jfree.chart.JFreeChart(
				(org.jfree.chart.plot.Plot) combinedRangeXYPlot1);
		canonizer.canonizeAndEnlargeExtensions(jFreeChart6);

		System.out.println("Extensions size: " + fe.size());
	}

	@Test
	public void testBrokenHash() throws IllegalArgumentException, IllegalAccessException, IOException {

		FieldExtensionsStrings fe = new FieldExtensionsStrings();
		HeapCanonizer canonizer = new HeapCanonizerMapStore(fe, true);

		org.jfree.chart.axis.ValueAxis valueAxis0 = null;
		canonizer.canonizeAndEnlargeExtensions(valueAxis0);

		org.jfree.chart.plot.CombinedRangeXYPlot combinedRangeXYPlot1 = new org.jfree.chart.plot.CombinedRangeXYPlot(
				valueAxis0);
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

		combinedRangeXYPlot1.setDataset(0, (org.jfree.data.xy.XYDataset) xYSeriesCollection7);
		canonizer.canonizeAndEnlargeExtensions(combinedRangeXYPlot1);

		org.jfree.chart.event.AxisChangeEvent axisChangeEvent10 = null;
		canonizer.canonizeAndEnlargeExtensions(axisChangeEvent10);

		combinedRangeXYPlot1.axisChanged(axisChangeEvent10);
		canonizer.canonizeAndEnlargeExtensions(combinedRangeXYPlot1);

		java.awt.Paint paint12 = combinedRangeXYPlot1.getDomainZeroBaselinePaint();
		canonizer.canonizeAndEnlargeExtensions(paint12);

		org.jfree.chart.title.LegendTitle legendTitle13 = new org.jfree.chart.title.LegendTitle(
				(org.jfree.chart.LegendItemSource) combinedRangeXYPlot1);
		canonizer.canonizeAndEnlargeExtensions(legendTitle13);

		org.jfree.chart.util.VerticalAlignment verticalAlignment14 = legendTitle13.getVerticalAlignment();
		// canonizer.canonizeAndEnlargeExtensions(legendTitle13);
		canonizer.canonizeAndEnlargeExtensions(verticalAlignment14);

		org.jfree.chart.event.TitleChangeListener titleChangeListener15 = null;
		canonizer.canonizeAndEnlargeExtensions(titleChangeListener15);

		legendTitle13.addChangeListener(titleChangeListener15);
		canonizer.canonizeAndEnlargeExtensions(legendTitle13);

		System.out.println("Extensions size: " + fe.size());
	}

	@Test
	public void testNullPointer() throws IllegalArgumentException, IllegalAccessException, IOException {
		FieldExtensionsStrings fe = new FieldExtensionsStrings();
		HeapCanonizer canonizer = new HeapCanonizerMapStore(fe, true);

		org.jfree.chart.axis.SegmentedTimeline segmentedTimeline3 = new org.jfree.chart.axis.SegmentedTimeline(
				(long) (byte) 1, 10, 500);
		canonizer.canonizeAndEnlargeExtensions(segmentedTimeline3);
		org.jfree.chart.axis.SegmentedTimeline segmentedTimeline4 = null;
		canonizer.canonizeAndEnlargeExtensions(segmentedTimeline4);
		segmentedTimeline3.setBaseTimeline(segmentedTimeline4);
		canonizer.canonizeAndEnlargeExtensions(segmentedTimeline3);
		canonizer.canonizeAndEnlargeExtensions(segmentedTimeline4);
		org.jfree.chart.axis.SegmentedTimeline segmentedTimeline9 = new org.jfree.chart.axis.SegmentedTimeline(
				(long) (byte) 1, 10, 500);
		canonizer.canonizeAndEnlargeExtensions(segmentedTimeline9);
		canonizer.canonizeAndEnlargeExtensions((long) (byte) 1);
		int i10 = segmentedTimeline9.getSegmentsExcluded();
		canonizer.canonizeAndEnlargeExtensions(i10);
		canonizer.canonizeAndEnlargeExtensions(segmentedTimeline9);
		long long12 = segmentedTimeline9.toMillisecond(0L);
		canonizer.canonizeAndEnlargeExtensions(segmentedTimeline9);
		canonizer.canonizeAndEnlargeExtensions(0L);
		segmentedTimeline3.setBaseTimeline(segmentedTimeline9);
		canonizer.canonizeAndEnlargeExtensions(segmentedTimeline3);
		long long15 = segmentedTimeline9.getTimeFromLong(900000L);
		canonizer.canonizeAndEnlargeExtensions(segmentedTimeline9);
		java.util.List list16 = null;
		canonizer.canonizeAndEnlargeExtensions(list16);
		segmentedTimeline9.setExceptionSegments(list16);
		canonizer.canonizeAndEnlargeExtensions(segmentedTimeline9);
		canonizer.canonizeAndEnlargeExtensions(list16);

		System.out.println("Extensions size: " + fe.size());
	}

	@Test
	public void testClassLoader() throws IllegalArgumentException, IllegalAccessException, IOException {

		FieldExtensionsStrings fe = new FieldExtensionsStrings();
		HeapCanonizer canonizer = new HeapCanonizerMapStore(fe, true);
		java.lang.Class class0 = Integer.class;
		canonizer.canonizeAndEnlargeExtensions(class0);
		java.lang.ClassLoader classLoader1 = org.jfree.chart.util.ObjectUtilities.getClassLoader(class0);
		canonizer.canonizeAndEnlargeExtensions(classLoader1);
		canonizer.canonizeAndEnlargeExtensions(class0);

		System.out.println("Extensions size: " + fe.size());
	}

	@Test
	public void testAnotherStackOverflow() throws IllegalArgumentException, IllegalAccessException, IOException {

		FieldExtensionsStrings fe = new FieldExtensionsStrings();
		HeapCanonizer canonizer = new HeapCanonizerMapStore(fe, true);

		org.jfree.chart.axis.PeriodAxis periodAxis1 = new org.jfree.chart.axis.PeriodAxis("Multiple Pie Plot");
		canonizer.canonizeAndEnlargeExtensions(periodAxis1);

		System.out.println("Extensions size: " + fe.size());
	}

	@Test(expected = NullPointerException.class)
	public void testAnotherNullPointer() throws IllegalArgumentException, IllegalAccessException, IOException {

		FieldExtensionsStrings fe = new FieldExtensionsStrings();
		HeapCanonizer canonizer = new HeapCanonizerMapStore(fe, true);

		org.jfree.chart.axis.NumberAxis numberAxis0 = null;
		canonizer.canonizeAndEnlargeExtensions(numberAxis0);
		java.awt.Font font5 = null;
		canonizer.canonizeAndEnlargeExtensions(font5);
		org.jfree.chart.axis.MarkerAxisBand markerAxisBand6 = new org.jfree.chart.axis.MarkerAxisBand(numberAxis0,
				(double) 12, Double.NEGATIVE_INFINITY, (double) 132, Double.NaN, font5);
		canonizer.canonizeAndEnlargeExtensions(markerAxisBand6);

		System.out.println("Extensions size: " + fe.size());
	}
	
	
	@Test
	public void testAnotherNullPointerFixedByListStore() throws IllegalArgumentException, IllegalAccessException, IOException {

		FieldExtensionsStrings fe = new FieldExtensionsStrings();
		HeapCanonizer canonizer = new HeapCanonizerListStore(fe, true);

		org.jfree.chart.axis.NumberAxis numberAxis0 = null;
		canonizer.canonizeAndEnlargeExtensions(numberAxis0);
		java.awt.Font font5 = null;
		canonizer.canonizeAndEnlargeExtensions(font5);
		org.jfree.chart.axis.MarkerAxisBand markerAxisBand6 = new org.jfree.chart.axis.MarkerAxisBand(numberAxis0,
				(double) 12, Double.NEGATIVE_INFINITY, (double) 132, Double.NaN, font5);
		canonizer.canonizeAndEnlargeExtensions(markerAxisBand6);

		System.out.println("Extensions size: " + fe.size());
	}
	
	

	@Test
	public void testYetAnotherStackOverflow() throws IllegalArgumentException, IllegalAccessException, IOException {

		FieldExtensionsStrings fe = new FieldExtensionsStrings();
		HeapCanonizer canonizer = new HeapCanonizerMapStore(fe, true);

		org.jfree.chart.renderer.xy.XYStepAreaRenderer xYStepAreaRenderer0 = new org.jfree.chart.renderer.xy.XYStepAreaRenderer();
		java.awt.Font font2 = null;
		xYStepAreaRenderer0.setLegendTextFont((int) '#', font2);
		java.awt.Graphics2D graphics2D4 = null;
		java.awt.geom.Rectangle2D rectangle2D5 = null;
		org.jfree.chart.plot.XYPlot xYPlot6 = null;
		org.jfree.data.xy.XYDataset xYDataset7 = null;
		org.jfree.chart.plot.PlotRenderingInfo plotRenderingInfo8 = null;
		org.jfree.chart.renderer.xy.XYItemRendererState xYItemRendererState9 = xYStepAreaRenderer0
				.initialise(graphics2D4, rectangle2D5, xYPlot6, xYDataset7, plotRenderingInfo8);
		java.awt.Font font11 = null;
		xYStepAreaRenderer0.setSeriesItemLabelFont((int) '4', font11, false);
		org.jfree.chart.labels.XYToolTipGenerator xYToolTipGenerator16 = null;
		org.jfree.chart.urls.XYURLGenerator xYURLGenerator17 = null;
		org.jfree.chart.renderer.xy.XYAreaRenderer xYAreaRenderer18 = new org.jfree.chart.renderer.xy.XYAreaRenderer(
				(int) (byte) 0, xYToolTipGenerator16, xYURLGenerator17);
		canonizer.canonizeAndEnlargeExtensions(xYAreaRenderer18);
		java.awt.Font font20 = null;
		xYAreaRenderer18.setSeriesItemLabelFont((int) 'a', font20);
		java.awt.Shape shape22 = xYAreaRenderer18.getLegendArea();
		java.awt.Graphics2D graphics2D23 = null;
		org.jfree.chart.plot.XYPlot xYPlot24 = null;
		org.jfree.chart.axis.NumberAxis numberAxis25 = new org.jfree.chart.axis.NumberAxis();
		boolean b26 = numberAxis25.isInverted();
		double d27 = numberAxis25.getUpperMargin();
		numberAxis25.setTickMarkInsideLength((float) 0);
		numberAxis25.setAutoTickUnitSelection(false, true);
		org.jfree.chart.util.LogFormat logFormat37 = new org.jfree.chart.util.LogFormat(10.0d, "hi!", true);
		org.jfree.chart.util.LogFormat logFormat41 = new org.jfree.chart.util.LogFormat(10.0d, "hi!", true);
		canonizer.canonizeAndEnlargeExtensions(logFormat41);
		org.jfree.chart.labels.StandardPieToolTipGenerator standardPieToolTipGenerator42 = new org.jfree.chart.labels.StandardPieToolTipGenerator(
				"hi!", (java.text.NumberFormat) logFormat37, (java.text.NumberFormat) logFormat41);
		canonizer.canonizeAndEnlargeExtensions(standardPieToolTipGenerator42);
		numberAxis25.setNumberFormatOverride((java.text.NumberFormat) logFormat41);
		canonizer.canonizeAndEnlargeExtensions(numberAxis25);
		java.awt.geom.Rectangle2D rectangle2D44 = null;
		java.awt.Color color46 = org.jfree.chart.ChartColor.GREEN;
		org.jfree.chart.axis.NumberAxis numberAxis47 = new org.jfree.chart.axis.NumberAxis();
		canonizer.canonizeAndEnlargeExtensions(numberAxis47);
		org.jfree.chart.axis.MarkerAxisBand markerAxisBand48 = numberAxis47.getMarkerBand();
		canonizer.canonizeAndEnlargeExtensions(markerAxisBand48);
		java.awt.Stroke stroke49 = numberAxis47.getAxisLineStroke();
		xYAreaRenderer18.drawDomainLine(graphics2D23, xYPlot24, (org.jfree.chart.axis.ValueAxis) numberAxis25,
				rectangle2D44, (double) (byte) -1, (java.awt.Paint) color46, stroke49);
		xYStepAreaRenderer0.setSeriesStroke((int) (short) 0, stroke49, true);
		canonizer.canonizeAndEnlargeExtensions(xYStepAreaRenderer0);
		canonizer.canonizeAndEnlargeExtensions(stroke49);

		System.out.println("Extensions size: " + fe.size());
	}

	@Test
	public void testRevealsBugInICC_Profile() throws IllegalArgumentException, IllegalAccessException, IOException {

		FieldExtensionsStrings fe = new FieldExtensionsStrings();
		HeapCanonizer canonizer = new HeapCanonizerMapStore(fe, true);

		java.awt.Color color0 = org.jfree.chart.ChartColor.VERY_LIGHT_MAGENTA;
		canonizer.canonizeAndEnlargeExtensions(color0);
		java.awt.color.ColorSpace colorSpace1 = color0.getColorSpace();
		canonizer.canonizeAndEnlargeExtensions(colorSpace1);
		canonizer.canonizeAndEnlargeExtensions(color0);

		System.out.println("Extensions size: " + fe.size());
	}

	@Test
	public void testNull() throws IllegalArgumentException, IllegalAccessException, IOException {

		FieldExtensionsStrings fe = new FieldExtensionsStrings();
		HeapCanonizer canonizer = new HeapCanonizerMapStore(fe, true);

		org.jfree.chart.plot.PolarPlot polarPlot1 = new org.jfree.chart.plot.PolarPlot();
		canonizer.canonizeAndEnlargeExtensions(polarPlot1);
		org.jfree.chart.JFreeChart jFreeChart2 = new org.jfree.chart.JFreeChart("null",
				(org.jfree.chart.plot.Plot) polarPlot1);
		canonizer.canonizeAndEnlargeExtensions(jFreeChart2);
		canonizer.canonizeAndEnlargeExtensions(polarPlot1);
		org.jfree.data.time.TimeSeries timeSeries3 = null;
		canonizer.canonizeAndEnlargeExtensions(timeSeries3);
		java.util.TimeZone timeZone4 = null;
		canonizer.canonizeAndEnlargeExtensions(timeZone4);
		org.jfree.data.time.TimeSeriesCollection timeSeriesCollection5 = new org.jfree.data.time.TimeSeriesCollection(
				timeSeries3, timeZone4);
		canonizer.canonizeAndEnlargeExtensions(timeZone4);
		canonizer.canonizeAndEnlargeExtensions(timeSeries3);
		canonizer.canonizeAndEnlargeExtensions(timeSeriesCollection5);
		java.util.List list6 = timeSeriesCollection5.getSeries();
		canonizer.canonizeAndEnlargeExtensions(timeSeriesCollection5);
		canonizer.canonizeAndEnlargeExtensions(list6);
		jFreeChart2.setSubtitles(list6);
		canonizer.canonizeAndEnlargeExtensions(list6);
		canonizer.canonizeAndEnlargeExtensions(jFreeChart2);

		System.out.println("Extensions size: " + fe.size());
	}

}
