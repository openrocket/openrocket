package net.sf.openrocket.gui.dialogs.optimization;

import java.awt.Color;
import java.awt.Paint;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.gui.components.StyledLabel;
import net.sf.openrocket.gui.util.GUIUtil;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.optimization.general.Point;
import net.sf.openrocket.optimization.rocketoptimization.OptimizableParameter;
import net.sf.openrocket.optimization.rocketoptimization.SimulationModifier;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.unit.Unit;
import net.sf.openrocket.unit.UnitGroup;
import net.sf.openrocket.unit.Value;
import net.sf.openrocket.util.LinearInterpolator;
import net.sf.openrocket.util.MathUtil;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYBoxAnnotation;
import org.jfree.chart.annotations.XYLineAnnotation;
import org.jfree.chart.annotations.XYPointerAnnotation;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.CustomXYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.PaintScale;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYShapeRenderer;
import org.jfree.chart.title.PaintScaleLegend;
import org.jfree.data.xy.DefaultXYZDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.TextAnchor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class that plots the path of an optimization.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class OptimizationPlotDialog extends JDialog {
	private static final Logger log = LoggerFactory.getLogger(OptimizationPlotDialog.class);
	private static final Translator trans = Application.getTranslator();
	

	private static final LinearInterpolator RED = new LinearInterpolator(
			new double[] { 0.0, 1.0 }, new double[] { 0.0, 1.0 }
			);
	private static final LinearInterpolator GREEN = new LinearInterpolator(
			new double[] { 0.0, 1.0 }, new double[] { 0.0, 0.0 }
			);
	private static final LinearInterpolator BLUE = new LinearInterpolator(
			new double[] { 0.0, 1.0 }, new double[] { 1.0, 0.0 }
			);
	
	private static final Color OUT_OF_DOMAIN_COLOR = Color.BLACK;
	
	private static final Color PATH_COLOR = new Color(220, 0, 0);
	
	
	public OptimizationPlotDialog(List<Point> path, Map<Point, FunctionEvaluationData> evaluations,
			List<SimulationModifier> modifiers, OptimizableParameter parameter, UnitGroup stabilityUnit, Window parent) {
		super(parent, trans.get("title"), ModalityType.APPLICATION_MODAL);
		

		JPanel panel = new JPanel(new MigLayout("fill"));
		
		ChartPanel chart;
		if (modifiers.size() == 1) {
			chart = create1DPlot(path, evaluations, modifiers, parameter, stabilityUnit);
		} else if (modifiers.size() == 2) {
			chart = create2DPlot(path, evaluations, modifiers, parameter, stabilityUnit);
		} else {
			throw new IllegalArgumentException("Invalid dimensionality, dim=" + modifiers.size());
		}
		chart.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		panel.add(chart, "span, grow, wrap para");
		

		JLabel label = new StyledLabel(trans.get("lbl.zoomInstructions"), -2);
		panel.add(label, "");
		

		JButton close = new JButton(trans.get("button.close"));
		close.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				OptimizationPlotDialog.this.setVisible(false);
			}
		});
		panel.add(close, "right");
		

		this.add(panel);
		
		GUIUtil.setDisposableDialogOptions(this, close);
		GUIUtil.rememberWindowSize(this);
	}
	
	

	/**
	 * Create a 1D plot of the optimization path.
	 */
	private ChartPanel create1DPlot(List<Point> path, Map<Point, FunctionEvaluationData> evaluations,
			List<SimulationModifier> modifiers, OptimizableParameter parameter, UnitGroup stabilityUnit) {
		
		SimulationModifier modX = modifiers.get(0);
		Unit xUnit = modX.getUnitGroup().getDefaultUnit();
		Unit yUnit = parameter.getUnitGroup().getDefaultUnit();
		
		// Create the optimization path (with autosort)
		XYSeries series = new XYSeries(trans.get("plot1d.series"), true, true);
		List<String> tooltips = new ArrayList<String>();
		for (Point p : evaluations.keySet()) {
			FunctionEvaluationData data = evaluations.get(p);
			if (data != null) {
				if (data.getParameterValue() != null) {
					Value[] state = data.getState();
					series.add(xUnit.toUnit(state[0].getValue()), yUnit.toUnit(data.getParameterValue().getValue()));
					tooltips.add(getTooltip(data, parameter));
				}
			} else {
				log.error("Could not find evaluation data for point " + p);
			}
		}
		

		String xLabel = modX.getRelatedObject().toString() + ": " + modX.getName() + " / " + xUnit.getUnit();
		String yLabel = parameter.getName() + " / " + yUnit.getUnit();
		
		JFreeChart chart = ChartFactory.createXYLineChart(
				trans.get("plot1d.title"),
				xLabel,
				yLabel,
				null,
				PlotOrientation.VERTICAL,
				false, // Legend
				true, // Tooltips
				false); // Urls
		

		// Set the scale of the plot to the limits
		double x1 = xUnit.toUnit(modX.getMinValue());
		double x2 = xUnit.toUnit(modX.getMaxValue());
		
		if (x1 < x2 - 0.0001) {
			log.debug("Setting 1D plot domain axis x1=" + x1 + " x2=" + x2);
			chart.getXYPlot().getDomainAxis().setRange(x1, x2);
		} else {
			log.warn("1D plot domain singular x1=" + x1 + " x2=" + x2 + ", not setting");
		}
		
		// Add lines to show optimization limits
		XYLineAnnotation line = new XYLineAnnotation(x1, -1e19, x1, 1e19);
		chart.getXYPlot().addAnnotation(line);
		line = new XYLineAnnotation(x2, -1e19, x2, 1e19);
		chart.getXYPlot().addAnnotation(line);
		
		// Mark the optimum point
		Point optimum = path.get(path.size() - 1);
		FunctionEvaluationData data = evaluations.get(optimum);
		if (data != null) {
			if (data.getParameterValue() != null) {
				Value[] state = data.getState();
				double x = xUnit.toUnit(state[0].getValue());
				double y = yUnit.toUnit(data.getParameterValue().getValue());
				
				XYPointerAnnotation text = new XYPointerAnnotation(trans.get("plot.label.optimum"),
						x, y, Math.PI / 2);
				text.setTextAnchor(TextAnchor.TOP_LEFT);
				chart.getXYPlot().addAnnotation(text);
			}
		} else {
			log.error("Could not find evaluation data for point " + optimum);
		}
		

		XYLineAndShapeRenderer lineRenderer = new XYLineAndShapeRenderer(true, true);
		lineRenderer.setBaseShapesVisible(true);
		lineRenderer.setSeriesShapesFilled(0, false);
		//lineRenderer.setSeriesShape(0, shapeRenderer.getBaseShape());
		lineRenderer.setSeriesOutlinePaint(0, PATH_COLOR);
		lineRenderer.setSeriesPaint(0, PATH_COLOR);
		lineRenderer.setUseOutlinePaint(true);
		CustomXYToolTipGenerator tooltipGenerator = new CustomXYToolTipGenerator();
		tooltipGenerator.addToolTipSeries(tooltips);
		lineRenderer.setBaseToolTipGenerator(tooltipGenerator);
		
		XYPlot plot = chart.getXYPlot();
		
		plot.setDataset(0, new XYSeriesCollection(series));
		plot.setRenderer(lineRenderer);
		


		return new ChartPanel(chart);
	}
	
	/**
	 * Create a 2D plot of the optimization path.
	 */
	private ChartPanel create2DPlot(List<Point> path, Map<Point, FunctionEvaluationData> evaluations,
			List<SimulationModifier> modifiers, OptimizableParameter parameter, UnitGroup stabilityUnit) {
		
		Unit parameterUnit = parameter.getUnitGroup().getDefaultUnit();
		
		SimulationModifier modX = modifiers.get(0);
		SimulationModifier modY = modifiers.get(1);
		
		Unit xUnit = modX.getUnitGroup().getDefaultUnit();
		Unit yUnit = modY.getUnitGroup().getDefaultUnit();
		
		// Create the optimization path dataset
		XYSeries pathSeries = new XYSeries(trans.get("plot2d.path"), false, true);
		List<String> pathTooltips = new ArrayList<String>();
		for (Point p : path) {
			FunctionEvaluationData data = evaluations.get(p);
			if (data != null) {
				Value[] state = data.getState();
				pathSeries.add(xUnit.toUnit(state[0].getValue()), yUnit.toUnit(state[1].getValue()));
				pathTooltips.add(getTooltip(data, parameter));
			} else {
				log.error("Could not find evaluation data for point " + p);
			}
		}
		

		// Create evaluations dataset
		double min = Double.POSITIVE_INFINITY;
		double max = Double.NEGATIVE_INFINITY;
		double[][] evals = new double[3][evaluations.size()];
		List<String> evalTooltips = new ArrayList<String>();
		
		Iterator<FunctionEvaluationData> iterator = evaluations.values().iterator();
		for (int i = 0; i < evaluations.size(); i++) {
			FunctionEvaluationData data = iterator.next();
			Value param = data.getParameterValue();
			double value;
			if (param != null) {
				value = parameterUnit.toUnit(data.getParameterValue().getValue());
			} else {
				value = Double.NaN;
			}
			
			Value[] state = data.getState();
			evals[0][i] = xUnit.toUnit(state[0].getValue());
			evals[1][i] = yUnit.toUnit(state[1].getValue());
			evals[2][i] = value;
			
			if (value < min) {
				min = value;
			}
			if (value > max) {
				max = value;
			}
			
			evalTooltips.add(getTooltip(data, parameter));
		}
		DefaultXYZDataset evalDataset = new DefaultXYZDataset();
		evalDataset.addSeries(trans.get("plot2d.evals"), evals);
		


		String xLabel = modX.getRelatedObject().toString() + ": " + modX.getName() + " / " + xUnit.getUnit();
		String yLabel = modY.getRelatedObject().toString() + ": " + modY.getName() + " / " + yUnit.getUnit();
		
		JFreeChart chart = ChartFactory.createXYLineChart(
				trans.get("plot2d.title"),
				xLabel,
				yLabel,
				null,
				//evalDataset,
				PlotOrientation.VERTICAL,
				true, // Legend
				true, // Tooltips
				false); // Urls
		

		// Set the scale of the plot to the limits
		double x1 = xUnit.toUnit(modX.getMinValue());
		double x2 = xUnit.toUnit(modX.getMaxValue());
		double y1 = yUnit.toUnit(modY.getMinValue());
		double y2 = yUnit.toUnit(modY.getMaxValue());
		
		if (x1 < x2 - 0.0001) {
			log.debug("Setting 2D plot domain axis to x1=" + x1 + " x2=" + x2);
			chart.getXYPlot().getDomainAxis().setRange(x1, x2);
		} else {
			log.warn("2D plot has singular domain axis: x1=" + x1 + " x2=" + x2);
		}
		
		if (y1 < y2 - 0.0001) {
			log.debug("Setting 2D plot range axis to y1=" + y1 + " y2=" + y2);
			chart.getXYPlot().getRangeAxis().setRange(y1, y2);
		} else {
			log.warn("2D plot has singular range axis: y1=" + y1 + " y2=" + y2);
		}
		
		XYBoxAnnotation box = new XYBoxAnnotation(x1, y1, x2, y2);
		chart.getXYPlot().addAnnotation(box);
		
		int n = pathSeries.getItemCount();
		XYPointerAnnotation text = new XYPointerAnnotation(trans.get("plot.label.optimum"),
				(Double) pathSeries.getX(n - 1), (Double) pathSeries.getY(n - 1), -Math.PI / 5);
		text.setTextAnchor(TextAnchor.BASELINE_LEFT);
		chart.getXYPlot().addAnnotation(text);
		

		if (min < max - 0.0001) {
			log.debug("Setting gradient scale range to min=" + min + " max=" + max);
		} else {
			log.warn("2D plot has singular gradient scale, resetting to (0,1): min=" + min + " max=" + max);
			min = 0;
			max = 1;
		}
		
		PaintScale paintScale = new GradientScale(min, max);
		
		XYShapeRenderer shapeRenderer = new XYShapeRenderer();
		shapeRenderer.setPaintScale(paintScale);
		shapeRenderer.setUseFillPaint(true);
		CustomXYToolTipGenerator tooltipGenerator = new CustomXYToolTipGenerator();
		tooltipGenerator.addToolTipSeries(evalTooltips);
		shapeRenderer.setBaseToolTipGenerator(tooltipGenerator);
		

		shapeRenderer.getLegendItem(0, 0);
		

		XYLineAndShapeRenderer lineRenderer = new XYLineAndShapeRenderer(true, true);
		lineRenderer.setBaseShapesVisible(true);
		lineRenderer.setSeriesShapesFilled(0, false);
		lineRenderer.setSeriesShape(0, shapeRenderer.getBaseShape());
		lineRenderer.setSeriesOutlinePaint(0, PATH_COLOR);
		lineRenderer.setSeriesPaint(0, PATH_COLOR);
		lineRenderer.setUseOutlinePaint(true);
		tooltipGenerator = new CustomXYToolTipGenerator();
		tooltipGenerator.addToolTipSeries(pathTooltips);
		lineRenderer.setBaseToolTipGenerator(tooltipGenerator);
		

		XYPlot plot = chart.getXYPlot();
		
		plot.setDataset(0, new XYSeriesCollection(pathSeries));
		plot.setRenderer(lineRenderer);
		
		plot.setDataset(1, evalDataset);
		plot.setRenderer(1, shapeRenderer);
		

		// Add value scale
		NumberAxis numberAxis = new NumberAxis(parameter.getName() + " / " + parameterUnit.getUnit());
		PaintScaleLegend scale = new PaintScaleLegend(paintScale, numberAxis);
		scale.setPosition(RectangleEdge.RIGHT);
		scale.setMargin(4.0D, 4.0D, 40.0D, 4.0D);
		scale.setAxisLocation(AxisLocation.BOTTOM_OR_RIGHT);
		chart.addSubtitle(scale);
		

		return new ChartPanel(chart);
	}
	
	

	private String getTooltip(FunctionEvaluationData data, OptimizableParameter parameter) {
		String ttip = "<html>";
		if (data.getParameterValue() != null) {
			ttip += parameter.getName() + ": " +
					parameter.getUnitGroup().getDefaultUnit().toStringUnit(data.getParameterValue().getValue());
			ttip += "<br>";
		}
		if (data.getDomainReference() != null) {
			ttip += trans.get("plot.ttip.stability") + " " + data.getDomainReference();
		}
		return ttip;
	}
	
	private class GradientScale implements PaintScale {
		
		private final double min;
		private final double max;
		
		public GradientScale(double min, double max) {
			this.min = min;
			this.max = max;
		}
		
		@Override
		public Paint getPaint(double value) {
			if (Double.isNaN(value)) {
				return OUT_OF_DOMAIN_COLOR;
			}
			
			value = MathUtil.map(value, min, max, 0.0, 1.0);
			value = MathUtil.clamp(value, 0.0, 1.0);
			
			float r = (float) RED.getValue(value);
			float g = (float) GREEN.getValue(value);
			float b = (float) BLUE.getValue(value);
			
			return new Color(r, g, b);
		}
		
		@Override
		public double getLowerBound() {
			return min;
		}
		
		@Override
		public double getUpperBound() {
			return max;
		}
	}
	
}
