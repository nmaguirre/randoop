package randoop.util.fieldbasedcontrol;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import org.jfree.data.xy.XYDataset;
import org.junit.Test;

public class JChartNewTests {

	  @Test
	  public void test00112() throws Throwable {
	
		  /*
	org.jfree.chart.renderer.RenderAttributes renderAttributes1 = new org.jfree.chart.renderer.RenderAttributes(true);
	// during test generation this statement threw an exception of type java.lang.NullPointerException in error
	java.lang.Boolean b4 = renderAttributes1.isLabelVisible(0, 0);
	  	*/
	  }
	  
	  
	  
	     @Test
	     public void test11137() throws Throwable {
	   
	   
	  	     org.jfree.chart.renderer.category.BarRenderer barRenderer0 = new org.jfree.chart.renderer.category.BarRenderer();
	  	     barRenderer0.setSeriesCreateEntities(0, (java.lang.Boolean)false);
	  	     boolean b4 = barRenderer0.getShadowsVisible();
	  	     
	  	     // Regression assertion (captures the current behavior of the code)
	  	     org.junit.Assert.assertTrue(b4 == false);
	  	     
	     }
	  
	  
	  
	
	     @Test
	     public void test137() throws Throwable {
	   
	   
	  	     org.jfree.chart.renderer.category.BarRenderer barRenderer0 = new org.jfree.chart.renderer.category.BarRenderer();
	  	     barRenderer0.setSeriesCreateEntities(0, (java.lang.Boolean)false);
	  	     boolean b4 = barRenderer0.getShadowsVisible();
	  	     org.jfree.chart.urls.CategoryURLGenerator categoryURLGenerator6 = null;
	  	     barRenderer0.setSeriesURLGenerator(1, categoryURLGenerator6, false);
	  	     double d9 = barRenderer0.getShadowYOffset();
	  	     
	  	     // Regression assertion (captures the current behavior of the code)
	  	     org.junit.Assert.assertTrue(b4 == false);
	  	     
	  	     // Regression assertion (captures the current behavior of the code)
	  	     org.junit.Assert.assertTrue(d9 == 4.0d);
	   
	     }

	  @Test
	  public void test002() throws Throwable {

	    org.jfree.chart.renderer.category.LineAndShapeRenderer lineAndShapeRenderer2 = new org.jfree.chart.renderer.category.LineAndShapeRenderer(false, false);
	    java.awt.Stroke stroke3 = lineAndShapeRenderer2.getBaseStroke();
	    java.awt.Font font7 = lineAndShapeRenderer2.getItemLabelFont((int)(byte)1, (int)'#', false);
	    lineAndShapeRenderer2.setDataBoundsIncludesVisibleSeriesOnly(false);
	    org.jfree.chart.labels.CategoryToolTipGenerator categoryToolTipGenerator10 = null;
	    lineAndShapeRenderer2.setBaseToolTipGenerator(categoryToolTipGenerator10);
	    org.jfree.chart.labels.ItemLabelPosition itemLabelPosition15 = lineAndShapeRenderer2.getNegativeItemLabelPosition(10, 0, false);
	    java.awt.Font font16 = lineAndShapeRenderer2.getBaseLegendTextFont();
	    java.awt.Graphics2D graphics2D17 = null;
	    org.jfree.chart.renderer.category.CategoryItemRendererState categoryItemRendererState18 = null;
	    java.awt.geom.Rectangle2D rectangle2D19 = null;
	    org.jfree.data.category.CategoryDataset categoryDataset20 = null;
	    org.jfree.chart.axis.CategoryAxis categoryAxis21 = new org.jfree.chart.axis.CategoryAxis();
	    org.jfree.chart.plot.Plot plot22 = null;
	    categoryAxis21.setPlot(plot22);
	    java.awt.Graphics2D graphics2D24 = null;
	    java.awt.geom.Rectangle2D rectangle2D26 = null;
	    org.jfree.chart.util.RectangleEdge rectangleEdge27 = null;
	    org.jfree.chart.axis.AxisState axisState28 = null;
	    categoryAxis21.drawTickMarks(graphics2D24, (double)(short)10, rectangle2D26, rectangleEdge27, axisState28);
	    java.awt.Font font30 = categoryAxis21.getLabelFont();
	    org.jfree.chart.axis.ValueAxis valueAxis31 = null;
	    org.jfree.chart.renderer.category.LineAndShapeRenderer lineAndShapeRenderer34 = new org.jfree.chart.renderer.category.LineAndShapeRenderer(false, false);
	    java.awt.Stroke stroke35 = lineAndShapeRenderer34.getBaseStroke();
	    boolean b36 = lineAndShapeRenderer34.getAutoPopulateSeriesShape();
	    org.jfree.chart.labels.ItemLabelPosition itemLabelPosition37 = lineAndShapeRenderer34.getBasePositiveItemLabelPosition();
	    org.jfree.chart.plot.CategoryPlot categoryPlot38 = new org.jfree.chart.plot.CategoryPlot(categoryDataset20, categoryAxis21, valueAxis31, (org.jfree.chart.renderer.category.CategoryItemRenderer)lineAndShapeRenderer34);
	    java.awt.Graphics2D graphics2D39 = null;
	    java.awt.geom.Rectangle2D rectangle2D40 = null;
	    org.jfree.chart.plot.PlotRenderingInfo plotRenderingInfo42 = null;
	    org.jfree.chart.plot.CategoryCrosshairState categoryCrosshairState43 = null;
	    boolean b44 = categoryPlot38.render(graphics2D39, rectangle2D40, 128, plotRenderingInfo42, categoryCrosshairState43);
	    boolean b45 = categoryPlot38.isRangeZeroBaselineVisible();
	    org.jfree.chart.renderer.category.CategoryItemRenderer categoryItemRenderer47 = categoryPlot38.getRenderer((int)(byte)100);
	    java.awt.Font font48 = org.jfree.chart.plot.CategoryPlot.DEFAULT_VALUE_LABEL_FONT;
	    categoryPlot38.setNoDataMessageFont(font48);
	    org.jfree.chart.axis.CategoryAxis categoryAxis50 = null;
	    org.jfree.chart.axis.ValueAxis valueAxis51 = null;
	    org.jfree.data.category.DefaultCategoryDataset defaultCategoryDataset52 = new org.jfree.data.category.DefaultCategoryDataset();
	    defaultCategoryDataset52.setValue((double)'#', (java.lang.Comparable)"-3,-3,3,3", (java.lang.Comparable)(short)0);
	    int i57 = defaultCategoryDataset52.getRowCount();
	    lineAndShapeRenderer2.drawItem(graphics2D17, categoryItemRendererState18, rectangle2D19, categoryPlot38, categoryAxis50, valueAxis51, (org.jfree.data.category.CategoryDataset)defaultCategoryDataset52, (-4194112), (int)'a', false, 8);
	    org.jfree.chart.event.PlotChangeListener plotChangeListener63 = null;
	    categoryPlot38.addChangeListener(plotChangeListener63);
	    org.jfree.chart.plot.Marker marker65 = null;
	    org.jfree.chart.util.Layer layer66 = null;
	    // The following exception was thrown during execution in test generation
	    try {
	    categoryPlot38.addRangeMarker(marker65, layer66);
	      org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException");
	    } catch (java.lang.NullPointerException e) {
	      // Expected exception.
	      if (! e.getClass().getCanonicalName().equals("java.lang.NullPointerException")) {
	        org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException, got " + e.getClass().getCanonicalName());
	      }
	    }
	    
	    
	    // Regression assertion (captures the current behavior of the code)
	    org.junit.Assert.assertNotNull(stroke3);
	    
	    // Regression assertion (captures the current behavior of the code)
	    org.junit.Assert.assertNotNull(font7);
	    
	    // Regression assertion (captures the current behavior of the code)
	    org.junit.Assert.assertNotNull(itemLabelPosition15);
	    
	    // Regression assertion (captures the current behavior of the code)
	    org.junit.Assert.assertNull(font16);
	    
	    // Regression assertion (captures the current behavior of the code)
	    org.junit.Assert.assertNotNull(font30);
	    
	    // Regression assertion (captures the current behavior of the code)
	    org.junit.Assert.assertNotNull(stroke35);
	    
	    // Regression assertion (captures the current behavior of the code)
	    org.junit.Assert.assertTrue(b36 == true);
	    
	    // Regression assertion (captures the current behavior of the code)
	    org.junit.Assert.assertNotNull(itemLabelPosition37);
	    
	    // Regression assertion (captures the current behavior of the code)
	    org.junit.Assert.assertTrue(b44 == false);
	    
	    // Regression assertion (captures the current behavior of the code)
	    org.junit.Assert.assertTrue(b45 == false);
	    
	    // Regression assertion (captures the current behavior of the code)
	    org.junit.Assert.assertNull(categoryItemRenderer47);
	    
	    // Regression assertion (captures the current behavior of the code)
	    org.junit.Assert.assertNotNull(font48);
	    
	    // Regression assertion (captures the current behavior of the code)
	    org.junit.Assert.assertTrue(i57 == 1);

	  }

	

  @Test
  public void test181() throws Throwable {



    org.jfree.chart.ui.BasicProjectInfo basicProjectInfo0 = new org.jfree.chart.ui.BasicProjectInfo();
    basicProjectInfo0.addOptionalLibrary("");
    org.jfree.chart.ui.BasicProjectInfo basicProjectInfo7 = new org.jfree.chart.ui.BasicProjectInfo("item", "PieLabelLinkStyle.CUBIC_CURVE", "[100.0, 1.0]", "hi!");
    org.jfree.chart.axis.AxisLocation axisLocation8 = org.jfree.chart.axis.AxisLocation.BOTTOM_OR_RIGHT;
    boolean b9 = basicProjectInfo7.equals((java.lang.Object)axisLocation8);
    basicProjectInfo0.addOptionalLibrary((org.jfree.chart.ui.Library)basicProjectInfo7);
    java.lang.String str11 = basicProjectInfo7.getInfo();
    java.lang.String str12 = basicProjectInfo7.getVersion();
    org.jfree.chart.ui.ProjectInfo projectInfo13 = org.jfree.chart.JFreeChart.INFO;
    basicProjectInfo7.addOptionalLibrary((org.jfree.chart.ui.Library)projectInfo13);
    projectInfo13.addOptionalLibrary("Size2D[width=0.0, height=0.0]");
    // The following exception was thrown during execution in test generation
    try {
    java.lang.String str17 = projectInfo13.toString();
      org.junit.Assert.fail("Expected exception of type java.lang.ClassCastException");
    } catch (java.lang.ClassCastException e) {
      // Expected exception.
      if (! e.getClass().getCanonicalName().equals("java.lang.ClassCastException")) {
        org.junit.Assert.fail("Expected exception of type java.lang.ClassCastException, got " + e.getClass().getCanonicalName());
      }
    }
    
    
    // Regression assertion (captures the current behavior of the code)
    org.junit.Assert.assertNotNull(axisLocation8);
    
    // Regression assertion (captures the current behavior of the code)
    org.junit.Assert.assertTrue(b9 == false);
    
    // Regression assertion (captures the current behavior of the code)
    org.junit.Assert.assertTrue("'" + str11 + "' != '" + "hi!"+ "'", str11.equals("hi!"));
    
    // Regression assertion (captures the current behavior of the code)
    org.junit.Assert.assertTrue("'" + str12 + "' != '" + "PieLabelLinkStyle.CUBIC_CURVE"+ "'", str12.equals("PieLabelLinkStyle.CUBIC_CURVE"));
    
    // Regression assertion (captures the current behavior of the code)
    org.junit.Assert.assertNotNull(projectInfo13);

  }	
	
	
	@Test
	public void flakyTest() throws Throwable {
		org.jfree.chart.axis.SegmentedTimeline segmentedTimeline3 = new org.jfree.chart.axis.SegmentedTimeline(0L, (-1), (int)(short)100);
		long long5 = segmentedTimeline3.getTimeFromLong((long)10);
		// during test generation this statement threw an exception of type java.lang.NullPointerException in error
		segmentedTimeline3.addBaseTimelineException((long)(short)1);	
	}
	
	
	@Test
	public void flakyTest2() throws Throwable {
		org.jfree.chart.axis.SegmentedTimeline segmentedTimeline3 = new org.jfree.chart.axis.SegmentedTimeline(0L, (-1), (int)(short)100);
		long long5 = segmentedTimeline3.getTimeFromLong((long)10);
		long long6 = segmentedTimeline3.getSegmentSize();
		// during test generation this statement threw an exception of type java.lang.NullPointerException in error
		segmentedTimeline3.addBaseTimelineExclusions((long)4, 1484578799999L);
	}
	

	@Test
	public void notSoLargeNumberOfObjects() throws Throwable {

		java.util.TimeZone timeZone1 = org.jfree.chart.axis.SegmentedTimeline.NO_DST_TIME_ZONE;
		org.jfree.chart.axis.DateAxis dateAxis2 = new org.jfree.chart.axis.DateAxis("{0}: ({1}, {2})", timeZone1);
	    HeapCanonizerRuntimeEfficient canonizer3 = new HeapCanonizerRuntimeEfficient(false, 10000, 1000, 1000, 1000, true);
//	    canonizer3.activateReadableExtensions();
	    canonizer3.traverseBreadthFirstAndEnlargeExtensions(dateAxis2);
	    System.out.println(canonizer3.store.extensions.size());
		canonizer3.traverseBreadthFirstAndEnlargeExtensions(timeZone1); 
	    System.out.println(canonizer3.store.extensions.size());
    
	}


	@Test
	public void notNullPointer() throws Throwable {
		java.util.TimeZone timeZone105 = org.jfree.chart.axis.SegmentedTimeline.DEFAULT_TIME_ZONE;
	     org.jfree.chart.axis.DateAxis dateAxis106 = new org.jfree.chart.axis.DateAxis("0", timeZone105);
	     java.util.TimeZone timeZone108 = org.jfree.chart.axis.SegmentedTimeline.DEFAULT_TIME_ZONE;
	     org.jfree.chart.axis.DateAxis dateAxis109 = new org.jfree.chart.axis.DateAxis("hi!", timeZone108);
	}


	@Test
	public void test328() throws Throwable {
		java.lang.Number[][] number_array_array2 = new java.lang.Number[][] {  };
		org.jfree.data.category.CategoryDataset categoryDataset3 = org.jfree.data.general.DatasetUtilities.createCategoryDataset("TimePeriodAnchor.MIDDLE", "RectangleAnchor.BOTTOM", number_array_array2);
		org.jfree.data.Range range5 = org.jfree.data.general.DatasetUtilities.iterateRangeBounds(categoryDataset3, true);
		org.jfree.chart.plot.MultiplePiePlot multiplePiePlot6 = new org.jfree.chart.plot.MultiplePiePlot(categoryDataset3);
		org.jfree.data.Range range7 = org.jfree.data.general.DatasetUtilities.iterateRangeBounds(categoryDataset3);
		org.jfree.data.general.PieDataset pieDataset9 = org.jfree.data.general.DatasetUtilities.createPieDatasetForRow(categoryDataset3, 4);
		org.jfree.chart.ui.ProjectInfo projectInfo10 = org.jfree.chart.JFreeChart.INFO;
		java.lang.String str11 = projectInfo10.toString();
		//System.out.println(str11);
		java.util.List list12 = projectInfo10.getContributors();
		//System.out.println(str11);
		java.util.List list13 = projectInfo10.getContributors();
		// The following exception was thrown during execution in test generation
		try {
			org.jfree.data.Range range15 = org.jfree.data.general.DatasetUtilities.findRangeBounds(categoryDataset3, list13, true);
			org.junit.Assert.fail("Expected exception of type java.lang.ClassCastException");
		} catch (java.lang.ClassCastException e) {
			// Expected exception.
			if (! e.getClass().getCanonicalName().equals("java.lang.ClassCastException")) {
				org.junit.Assert.fail("Expected exception of type java.lang.ClassCastException, got " + e.getClass().getCanonicalName());
			}
		}
		// Regression assertion (captures the current behavior of the code)
		org.junit.Assert.assertNotNull(number_array_array2);
		// Regression assertion (captures the current behavior of the code)
		org.junit.Assert.assertNotNull(categoryDataset3);
		// Regression assertion (captures the current behavior of the code)
		org.junit.Assert.assertNull(range5);
		// Regression assertion (captures the current behavior of the code)
		org.junit.Assert.assertNull(range7);
		// Regression assertion (captures the current behavior of the code)
		org.junit.Assert.assertNotNull(pieDataset9);
		// Regression assertion (captures the current behavior of the code)
		org.junit.Assert.assertNotNull(projectInfo10);
		// Regression assertion (captures the current behavior of the code)
		org.junit.Assert.assertTrue("'" + str11 + "' != '" + "JFreeChart version 1.2.0-pre.\n(C)opyright 2000-2008, by Object Refinery Limited and Contributors.\n\nFor terms of use, see the licence below.\n\nFURTHER INFORMATION:\nCONTRIBUTORS:Eric Alexander (-).Richard Atkinson (richard_c_atkinson@ntlworld.com).David Basten (-).David Berry (-).Chris Boek (-).Zoheb Borbora (-).Anthony Boulestreau (-).Jeremy Bowman (-).Nicolas Brodu (-).Jody Brownell (-).David Browning (-).Soren Caspersen (-).Chuanhao Chiu (-).Brian Cole (-).Pascal Collet (-).Martin Cordova (-).Paolo Cova (-).Greg Darke (-).Mike Duffy (-).Don Elliott (-).David Forslund (-).Jonathan Gabbai (-).David Gilbert (david.gilbert@object-refinery.com).Serge V. Grachov (-).Daniel Gredler (-).Hans-Jurgen Greiner (-).Joao Guilherme Del Valle (-).Aiman Han (-).Cameron Hayne (-).Martin Hoeller (-).Jon Iles (-).Wolfgang Irler (-).Sergei Ivanov (-).Adriaan Joubert (-).Darren Jung (-).Xun Kang (-).Bill Kelemen (-).Norbert Kiesel (-).Peter Kolb (-).Gideon Krause (-).Pierre-Marie Le Biot (-).Arnaud Lelievre (-).Wolfgang Lenhard (-).David Li (-).Yan Liu (-).Tin Luu (-).Craig MacFarlane (-).Achilleus Mantzios (-).Thomas Meier (-).Jim Moore (-).Jonathan Nash (-).Barak Naveh (-).David M. O'Donnell (-).Krzysztof Paz (-).Eric Penfold (-).Tomer Peretz (-).Diego Pierangeli (-).Xavier Poinsard (-).Andrzej Porebski (-).Viktor Rajewski (-).Eduardo Ramalho (-).Michael Rauch (-).Cameron Riley (-).Klaus Rheinwald (-).Dan Rivett (d.rivett@ukonline.co.uk).Scott Sams (-).Michel Santos (-).Thierry Saura (-).Andreas Schneider (-).Jean-Luc SCHWAB (-).Bryan Scott (-).Tobias Selb (-).Darshan Shah (-).Mofeed Shahin (-).Michael Siemer (-).Pady Srinivasan (-).Greg Steckman (-).Gerald Struck (-).Roger Studner (-).Irv Thomae (-).Eric Thomas (-).Jess Thrysoee (-).Rich Unger (-).Daniel van Enckevort (-).Laurence Vanhelsuwe (-).Sylvain Vieujot (-).Ulrich Voigt (-).Jelai Wang (-).Mark Watson (www.markwatson.com).Alex Weber (-).Matthew Wright (-).Benoit Xhenseval (-).Christian W. Zuckschwerdt (Christian.Zuckschwerdt@Informatik.Uni-Oldenburg.de).Hari (-).Sam (oldman) (-).\nOTHER LIBRARIES USED BY JFreeChart:None\nJFreeChart LICENCE TERMS:\nLengthConstraintType.FIXED"+ "'", str11.equals("JFreeChart version 1.2.0-pre.\n(C)opyright 2000-2008, by Object Refinery Limited and Contributors.\n\nFor terms of use, see the licence below.\n\nFURTHER INFORMATION:\nCONTRIBUTORS:Eric Alexander (-).Richard Atkinson (richard_c_atkinson@ntlworld.com).David Basten (-).David Berry (-).Chris Boek (-).Zoheb Borbora (-).Anthony Boulestreau (-).Jeremy Bowman (-).Nicolas Brodu (-).Jody Brownell (-).David Browning (-).Soren Caspersen (-).Chuanhao Chiu (-).Brian Cole (-).Pascal Collet (-).Martin Cordova (-).Paolo Cova (-).Greg Darke (-).Mike Duffy (-).Don Elliott (-).David Forslund (-).Jonathan Gabbai (-).David Gilbert (david.gilbert@object-refinery.com).Serge V. Grachov (-).Daniel Gredler (-).Hans-Jurgen Greiner (-).Joao Guilherme Del Valle (-).Aiman Han (-).Cameron Hayne (-).Martin Hoeller (-).Jon Iles (-).Wolfgang Irler (-).Sergei Ivanov (-).Adriaan Joubert (-).Darren Jung (-).Xun Kang (-).Bill Kelemen (-).Norbert Kiesel (-).Peter Kolb (-).Gideon Krause (-).Pierre-Marie Le Biot (-).Arnaud Lelievre (-).Wolfgang Lenhard (-).David Li (-).Yan Liu (-).Tin Luu (-).Craig MacFarlane (-).Achilleus Mantzios (-).Thomas Meier (-).Jim Moore (-).Jonathan Nash (-).Barak Naveh (-).David M. O'Donnell (-).Krzysztof Paz (-).Eric Penfold (-).Tomer Peretz (-).Diego Pierangeli (-).Xavier Poinsard (-).Andrzej Porebski (-).Viktor Rajewski (-).Eduardo Ramalho (-).Michael Rauch (-).Cameron Riley (-).Klaus Rheinwald (-).Dan Rivett (d.rivett@ukonline.co.uk).Scott Sams (-).Michel Santos (-).Thierry Saura (-).Andreas Schneider (-).Jean-Luc SCHWAB (-).Bryan Scott (-).Tobias Selb (-).Darshan Shah (-).Mofeed Shahin (-).Michael Siemer (-).Pady Srinivasan (-).Greg Steckman (-).Gerald Struck (-).Roger Studner (-).Irv Thomae (-).Eric Thomas (-).Jess Thrysoee (-).Rich Unger (-).Daniel van Enckevort (-).Laurence Vanhelsuwe (-).Sylvain Vieujot (-).Ulrich Voigt (-).Jelai Wang (-).Mark Watson (www.markwatson.com).Alex Weber (-).Matthew Wright (-).Benoit Xhenseval (-).Christian W. Zuckschwerdt (Christian.Zuckschwerdt@Informatik.Uni-Oldenburg.de).Hari (-).Sam (oldman) (-).\nOTHER LIBRARIES USED BY JFreeChart:None\nJFreeChart LICENCE TERMS:\nLengthConstraintType.FIXED"));
		// Regression assertion (captures the current behavior of the code)
		org.junit.Assert.assertNotNull(list12);
		// Regression assertion (captures the current behavior of the code)
		org.junit.Assert.assertNotNull(list13);
	}


	@Test
	public void test150() throws Throwable {
	  org.jfree.chart.ui.ProjectInfo projectInfo0 = org.jfree.chart.JFreeChart.INFO;
	  java.awt.Image image1 = projectInfo0.getLogo();
	  java.util.List list2 = projectInfo0.getContributors();
	  java.lang.String str3 = projectInfo0.getLicenceName();
	  projectInfo0.addOptionalLibrary("RectangleEdge.TOP");
	  // Regression assertion (captures the current behavior of the code)
	  org.junit.Assert.assertNotNull(projectInfo0);
	  // Regression assertion (captures the current behavior of the code)
	  org.junit.Assert.assertNotNull(image1);
	  // Regression assertion (captures the current behavior of the code)
	  org.junit.Assert.assertNotNull(list2);
	//System.out.println(str3);
	  // Regression assertion (captures the current behavior of the code)
	  org.junit.Assert.assertTrue("'" + str3 + "' != '" + "Rotation.ANTICLOCKWISE"+ "'", str3.equals("Rotation.ANTICLOCKWISE"));
	}
	
	
	
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
