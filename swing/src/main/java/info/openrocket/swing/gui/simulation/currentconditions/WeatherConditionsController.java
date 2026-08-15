package info.openrocket.swing.gui.simulation.currentconditions;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AWTEventListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;

import info.openrocket.core.l10n.Translator;
import info.openrocket.core.models.atmosphere.ExtendedISAModel;
import info.openrocket.core.models.wind.MultiLevelPinkNoiseWindModel;
import info.openrocket.core.models.wind.PinkNoiseWindModel;
import info.openrocket.core.models.wind.WindModel.AltitudeReference;
import info.openrocket.core.models.wind.WindModelType;
import info.openrocket.core.simulation.SimulationOptions;
import info.openrocket.core.startup.Application;
import info.openrocket.core.unit.UnitGroup;
import info.openrocket.swing.gui.SpinnerEditor;
import info.openrocket.swing.gui.adaptors.DoubleModel;
import info.openrocket.swing.gui.components.UnitSelector;
import info.openrocket.swing.gui.simulation.MultiLevelWindEditDialog;
import info.openrocket.swing.gui.simulation.currentconditions.OpenMeteoClient.FetchResult;
import info.openrocket.swing.gui.simulation.currentconditions.OpenMeteoClient.RefreshRateLimitException;
import net.miginfocom.swing.MigLayout;

/** Coordinates weather selection, retrieval, preview, customization, and application for a simulation editor. */
public final class WeatherConditionsController {
	private static final Translator trans = Application.getTranslator();
	private Instant selectedForecastTime;
	private DeviceLocation selectedWeatherLocation;
	private ApplySelection weatherApplySelection = ApplySelection.all();
	private WeatherEdits weatherEdits;
	private WeatherEditKey weatherEditKey;
	private SwingWorker<ConditionsLookup, Void> weatherWorker;

	public void cancel() {
		if (weatherWorker != null && !weatherWorker.isDone()) {
			weatherWorker.cancel(true);
		}
	}

	public void request(JButton button, SimulationOptions options) {
		WeatherRequest request = chooseWeatherRequest(panelOwner(button), options);
		if (request != null) {
			fetchWeatherConditions(button, options, request);
		}
	}

	private WeatherRequest chooseWeatherRequest(Window owner, SimulationOptions options) {
		Instant[] forecastTime = { selectedForecastTime };
		JLabel dateTime = new JLabel();
		JButton chooseDateTime = new JButton(trans.get("simedtdlg.but.chooseForecastTime"));

		DeviceLocation configuredLocation = new DeviceLocation(options.getLaunchLatitude(), options.getLaunchLongitude(),
				options.getLaunchAltitude(), Double.NaN, trans.get("simedtdlg.lbl.configuredCoordinates"));
		if (selectedWeatherLocation != null && sameCoordinates(selectedWeatherLocation, configuredLocation)) {
			configuredLocation = configuredLocation.withTimezone(selectedWeatherLocation.timezoneId());
		}
		DeviceLocation[] selectedLocation = { configuredLocation };
		ZoneId[] selectedTimezone = { timezoneOf(configuredLocation) };
		JLabel locationLabel = new JLabel(formatLocation(configuredLocation));
		JButton chooseLocation = new JButton(trans.get("simedtdlg.but.chooseWeatherLocation"));
		JLabel availability = new JLabel();
		Runnable refreshTimezoneLabels = () -> {
			ZoneId timezone = selectedTimezone[0];
			availability.setText(timezone == null
					? String.format(Locale.ROOT, trans.get("simedtdlg.msg.forecastAvailabilityPendingTimezone"),
							OpenMeteoClient.MAX_FORECAST_DAYS)
					: String.format(Locale.ROOT, trans.get("simedtdlg.msg.forecastAvailability"),
							OpenMeteoClient.MAX_FORECAST_DAYS, timezone.getId()));
			String conditionsFor = trans.get("simedtdlg.lbl.conditionsFor");
			dateTime.setText(forecastTime[0] == null
					? "<html>" + conditionsFor + " " + trans.get("simedtdlg.lbl.currentTime") + "<br>&nbsp;</html>"
					: "<html>" + conditionsFor + "<br>" + formatForecastTime(forecastTime[0], timezone)
							+ "</html>");
		};
		java.util.function.Consumer<DeviceLocation> updateLocation = chosen -> {
			selectedLocation[0] = chosen;
			selectedTimezone[0] = timezoneOf(chosen);
			options.setLaunchLatitude(chosen.latitude());
			options.setLaunchLongitude(chosen.longitude());
			locationLabel.setText(formatLocation(chosen));
			refreshTimezoneLabels.run();
		};
		chooseLocation.addActionListener(e -> {
			DeviceLocation chosen = LocationPickerDialog.show(owner, selectedLocation[0], false);
			if (chosen != null) {
				updateLocation.accept(chosen);
			}
		});
		chooseDateTime.addActionListener(e -> {
			Runnable showPicker = () -> {
				ZoneId timezone = selectedTimezone[0] == null ? ZoneId.systemDefault() : selectedTimezone[0];
				Instant now = Instant.now();
				Instant firstForecastHour = now.truncatedTo(ChronoUnit.HOURS).plus(1, ChronoUnit.HOURS);
				Instant lastForecastHour = LocalDate.ofInstant(now, timezone)
						.plusDays(OpenMeteoClient.MAX_FORECAST_DAYS - 1L).atTime(23, 0).atZone(timezone).toInstant();
				Instant initial = forecastTime[0] != null && !forecastTime[0].isBefore(firstForecastHour)
						&& !forecastTime[0].isAfter(lastForecastHour) ? forecastTime[0] : null;
				ForecastDateTimePicker.Selection chosen = ForecastDateTimePicker.show(owner, initial, firstForecastHour,
						lastForecastHour, timezone);
				if (chosen != null) {
					forecastTime[0] = chosen.now() ? null : chosen.forecastAt();
					refreshTimezoneLabels.run();
				}
			};
			if (selectedTimezone[0] != null) {
				showPicker.run();
				return;
			}
			chooseDateTime.setEnabled(false);
			chooseDateTime.setText(trans.get("simedtdlg.lbl.resolvingTimezone"));
			DeviceLocation location = selectedLocation[0];
			new SwingWorker<ZoneId, Void>() {
				@Override
				protected ZoneId doInBackground() throws Exception {
					return new OpenMeteoClient().resolveTimezone(location.latitude(), location.longitude());
				}

				@Override
				protected void done() {
					chooseDateTime.setEnabled(true);
					chooseDateTime.setText(trans.get("simedtdlg.but.chooseForecastTime"));
					try {
						ZoneId timezone = get();
						updateLocation.accept(location.withTimezone(timezone.getId()));
						showPicker.run();
					} catch (Exception exception) {
						showCurrentConditionsError(chooseDateTime, trans.get("simedtdlg.msg.timezoneLookupFailed"));
					}
				}
			}.execute();
		});
		refreshTimezoneLabels.run();

		JPanel chooser = new JPanel(new MigLayout("insets 0, fillx", "[grow]"));
		JPanel locationRow = new JPanel(new MigLayout("insets 0, fillx", "[grow][]"));
		locationRow.add(new JLabel(trans.get("simedtdlg.msg.chooseWeatherLocation")));
		locationRow.add(chooseLocation);
		chooser.add(locationRow, "growx, wrap");
		chooser.add(locationLabel, "gapbottom rel, wrap");
		chooser.add(new JSeparator(), "span, growx, gapbottom rel, wrap");
		JPanel timeRow = new JPanel(new MigLayout("insets 0, fillx", "[grow][]"));
		timeRow.add(dateTime);
		timeRow.add(chooseDateTime);
		chooser.add(timeRow, "growx, wrap");
		chooser.add(availability, "span, wrap");

		int choice = JOptionPane.showConfirmDialog(owner, chooser, trans.get("simedtdlg.title.currentConditions"),
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
		selectedForecastTime = forecastTime[0];
		selectedWeatherLocation = selectedLocation[0];
		if (choice != JOptionPane.OK_OPTION) {
			return null;
		}
		return new WeatherRequest(forecastTime[0], LocationSource.SELECTED, selectedLocation[0], false);
	}

	private static boolean sameCoordinates(DeviceLocation first, DeviceLocation second) {
		return Math.abs(first.latitude() - second.latitude()) < 0.00001
				&& Math.abs(first.longitude() - second.longitude()) < 0.00001;
	}

	private static ZoneId timezoneOf(DeviceLocation location) {
		if (location == null || location.timezoneId() == null || location.timezoneId().isBlank()) {
			return null;
		}
		try {
			return ZoneId.of(location.timezoneId());
		} catch (RuntimeException ignored) {
			return null;
		}
	}

	private static String formatForecastTime(Instant time, ZoneId timezone) {
		ZoneId zone = timezone == null ? ZoneId.systemDefault() : timezone;
		return DateTimeFormatter.ofPattern("MMM d, uuuu h:mm a z", Locale.getDefault()).withZone(zone).format(time);
	}

	private static String formatLocation(DeviceLocation location) {
		String timezone = location.timezoneId() == null || location.timezoneId().isBlank()
				? "" : " (" + location.timezoneId() + ")";
		return String.format(Locale.ROOT, "%s: %.5f°, %.5f°%s", location.source(), location.latitude(),
				location.longitude(), timezone);
	}

	private void fetchWeatherConditions(JButton button, SimulationOptions options, WeatherRequest request) {
		button.setEnabled(false);
		button.setText(trans.get(request.usesDeviceLocation()
				? "simedtdlg.lbl.locating" : "simedtdlg.lbl.fetchingWeather"));

		if (weatherWorker != null && !weatherWorker.isDone()) {
			weatherWorker.cancel(true);
		}
		SwingWorker<ConditionsLookup, Void> worker = new SwingWorker<>() {
			@Override
			protected ConditionsLookup doInBackground() throws Exception {
				DeviceLocation location = switch (request.locationSource()) {
					case DEVICE -> new SystemLocationProvider().locate();
					case CONFIGURED -> new DeviceLocation(options.getLaunchLatitude(), options.getLaunchLongitude(),
							options.getLaunchAltitude(), Double.NaN,
							trans.get("simedtdlg.lbl.configuredCoordinates"));
					case SELECTED -> request.selectedLocation();
				};
				SwingUtilities.invokeLater(() -> button.setText(trans.get("simedtdlg.lbl.fetchingWeather")));
				OpenMeteoClient client = new OpenMeteoClient();
				if (timezoneOf(location) == null) {
					location = location.withTimezone(client.resolveTimezone(location.latitude(), location.longitude()).getId());
				}
				FetchResult fetchResult;
				if (request.isForecast()) {
					fetchResult = request.forceRefresh()
							? client.forceFetchForecast(location.latitude(), location.longitude(), request.forecastAt())
							: client.fetchForecastWithCacheInfo(location.latitude(), location.longitude(), request.forecastAt());
				} else {
					fetchResult = request.forceRefresh()
							? client.forceFetch(location.latitude(), location.longitude())
							: client.fetchWithCacheInfo(location.latitude(), location.longitude());
				}
				return new ConditionsLookup(location, fetchResult.conditions(), request, fetchResult);
			}

			@Override
			protected void done() {
				if (isCancelled() || !button.isDisplayable()) {
					return;
				}
				boolean restarted = false;
				try {
					ConditionsLookup lookup = get();
					selectedWeatherLocation = lookup.location();
					WeatherPreviewResult previewResult = confirmWeatherConditions(panelOwner(button), lookup);
					if (previewResult != null && previewResult.forceRefresh()) {
						restarted = true;
						fetchWeatherConditions(button, options, request.withForceRefresh());
						return;
					}
					if (previewResult != null) {
						applyWeatherConditions(options, previewResult.edits(), previewResult.selection());
					}
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					showCurrentConditionsError(button, trans.get("simedtdlg.error.currentConditionsInterrupted"));
				} catch (ExecutionException e) {
					Throwable cause = e.getCause();
					if (cause instanceof RefreshRateLimitException rateLimit) {
						String availableAt = formatWeatherTime(rateLimit.getAvailableAt(), timezoneOf(request.selectedLocation()));
						showCurrentConditionsError(button, String.format(Locale.ROOT,
								trans.get("simedtdlg.msg.forceRefreshRateLimited"), availableAt));
					} else if (request.usesDeviceLocation() && cause instanceof LocationException) {
						int choice = JOptionPane.showConfirmDialog(panelOwner(button),
								cause.getMessage() + "\n\n" + trans.get("simedtdlg.msg.useConfiguredCoordinates"),
								trans.get("simedtdlg.title.currentConditions"), JOptionPane.YES_NO_OPTION,
								JOptionPane.WARNING_MESSAGE);
						if (choice == JOptionPane.YES_OPTION) {
							restarted = true;
							fetchWeatherConditions(button, options, request.withConfiguredLocation());
						}
					} else {
						showCurrentConditionsError(button, cause == null ? e.getMessage() : cause.getMessage());
					}
				} finally {
					if (!restarted) {
						button.setText(trans.get("simedtdlg.but.currentConditions"));
						button.setEnabled(true);
					}
				}
			}
		};
		weatherWorker = worker;
		worker.execute();
	}

	private static String formatWeatherTime(Instant time, ZoneId timezone) {
		ZoneId zone = timezone == null ? ZoneId.systemDefault() : timezone;
		return DateTimeFormatter.ofPattern("MMM d, uuuu h:mm:ss a z", Locale.getDefault())
				.withZone(zone).format(time);
	}

	private WeatherPreviewResult confirmWeatherConditions(Window owner, ConditionsLookup lookup) {
		CurrentConditions conditions = lookup.conditions();
		WeatherEdits edits = editsFor(conditions);
		String preview = trans.get(lookup.request().isForecast()
				? "simedtdlg.msg.forecastConditionsPreview" : "simedtdlg.msg.currentConditionsPreview");
		ZoneId timezone = timezoneOf(lookup.location());
		String validAt = DateTimeFormatter.ofPattern("MMM d, uuuu h:mm a z", Locale.getDefault())
				.withZone(timezone == null ? ZoneId.systemDefault() : timezone).format(conditions.validAt());
		String accuracy = Double.isFinite(lookup.location().horizontalAccuracy())
				? String.format(Locale.ROOT, " (±%.0f m)", lookup.location().horizontalAccuracy()) : "";
		ApplySelection selection = weatherApplySelection;
		while (true) {
			CurrentConditions.WindLayer surfaceWind = edits.windLayers.get(0);
			String heading = lookup.fetchResult().cached() ? "" : "<b>" + preview + "</b><br><br>";
			String summary = String.format(Locale.ROOT, trans.get("simedtdlg.msg.weatherSummary"),
					heading, edits.latitude, edits.longitude,
					accuracy, edits.elevation, validAt,
					edits.temperature - 273.15,
					edits.pressure / 100.0, edits.relativeHumidity * 100.0, surfaceWind.speed(),
					Math.toDegrees(surfaceWind.direction()), conditions.windGust(), edits.windLayers.size(),
					edits.windLayers.get(edits.windLayers.size() - 1).altitude(),
					trans.get("simedtdlg.msg.weatherAttribution"));
			WeatherPreviewAction action = showWeatherPreviewDialog(owner, summary, lookup.fetchResult(), timezone);
			if (action == WeatherPreviewAction.APPLY) {
				weatherApplySelection = selection;
				return new WeatherPreviewResult(selection, edits, false);
			}
			if (action == WeatherPreviewAction.FORCE_REFRESH) {
				return new WeatherPreviewResult(selection, edits, true);
			}
			if (action != WeatherPreviewAction.CUSTOMIZE) {
				weatherApplySelection = selection;
				return null;
			}
			WeatherCustomization customization = customizeApplySelection(owner, selection, edits);
			selection = customization.selection();
			edits = customization.edits();
			weatherApplySelection = selection;
			weatherEdits = edits;
		}
	}

	private static WeatherPreviewAction showWeatherPreviewDialog(Window owner, String summary, FetchResult fetchResult,
			ZoneId timezone) {
		JDialog dialog = new JDialog(owner, trans.get("simedtdlg.title.currentConditions"),
				JDialog.ModalityType.APPLICATION_MODAL);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		WeatherPreviewAction[] result = { WeatherPreviewAction.CANCEL };

		JPanel body = new JPanel(new BorderLayout(0, 12));
		if (fetchResult.cached()) {
			JPanel cacheHeader = new JPanel(new MigLayout("insets 0", "[][]"));
			cacheHeader.add(new JLabel("<html><b>" + trans.get("simedtdlg.lbl.usingCachedWeather") + "</b></html>"));
			JButton help = new JButton() {
				@Override
				protected void paintComponent(Graphics graphics) {
					super.paintComponent(graphics);
					Graphics copy = graphics.create();
					FontMetrics metrics = copy.getFontMetrics(getFont());
					int x = (getWidth() - metrics.stringWidth("?")) / 2;
					int y = (getHeight() - metrics.getHeight()) / 2 + metrics.getAscent();
					copy.setColor(getForeground());
					copy.setFont(getFont());
					copy.drawString("?", x, y);
					copy.dispose();
				}
			};
			help.getAccessibleContext().setAccessibleName(trans.get("simedtdlg.lbl.usingCachedWeather"));
			help.setMargin(new java.awt.Insets(0, 0, 0, 0));
			help.setFocusable(false);
			help.setFocusPainted(false);
			help.putClientProperty("JButton.buttonType", "roundRect");
			help.putClientProperty("FlatLaf.style", "arc: 999");
			JPanel helpContent = new JPanel(new BorderLayout(0, 10));
			Color calloutBorder = UIManager.getColor("Component.borderColor");
			if (calloutBorder == null) {
				calloutBorder = Color.GRAY;
			}
			helpContent.setOpaque(false);
			helpContent.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
			helpContent.add(new JLabel(trans.get("simedtdlg.ttip.cachedWeather")), BorderLayout.CENTER);
			cacheHeader.add(help, "w 22lp!, h 22lp!");
			JWindow helpPopup = new JWindow(dialog);
			JButton forceRefresh = new JButton(trans.get("simedtdlg.but.forceRefreshWeather"));
			forceRefresh.addActionListener(e -> {
				Instant availableAt = fetchResult.forceRefreshAvailableAt();
				if (Instant.now().isBefore(availableAt)) {
					JOptionPane.showMessageDialog(dialog,
							String.format(Locale.ROOT, trans.get("simedtdlg.msg.forceRefreshRateLimited"),
									formatWeatherTime(availableAt, timezone)),
							trans.get("simedtdlg.title.currentConditions"), JOptionPane.INFORMATION_MESSAGE);
					return;
				}
				result[0] = WeatherPreviewAction.FORCE_REFRESH;
				helpPopup.dispose();
				dialog.dispose();
			});
			helpContent.add(forceRefresh, BorderLayout.SOUTH);
			Color calloutFill = UIManager.getColor("Panel.background");
			Color calloutOutline = calloutBorder;
			JPanel popupContent = new JPanel(new BorderLayout()) {
				@Override
				protected void paintComponent(Graphics graphics) {
					Graphics2D graphics2D = (Graphics2D) graphics.create();
					graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
							RenderingHints.VALUE_ANTIALIAS_ON);
					double bodyTop = 9;
					Area callout = new Area(new RoundRectangle2D.Double(0.5, bodyTop, getWidth() - 1.0,
							getHeight() - bodyTop - 0.5, 14, 14));
					Path2D tail = new Path2D.Double();
					tail.moveTo(1.5, bodyTop + 2);
					tail.quadTo(7, 7, 11, 0.8);
					tail.quadTo(15, 7, 22, bodyTop + 2);
					tail.closePath();
					callout.add(new Area(tail));
					graphics2D.setColor(calloutFill);
					graphics2D.fill(callout);
					graphics2D.setColor(calloutOutline);
					graphics2D.draw(callout);
					graphics2D.dispose();
				}
			};
			popupContent.setOpaque(false);
			popupContent.setBorder(BorderFactory.createEmptyBorder(10, 1, 1, 1));
			popupContent.add(helpContent, BorderLayout.CENTER);
			helpPopup.setBackground(new Color(0, 0, 0, 0));
			helpPopup.setContentPane(popupContent);
			Point[] predictionOrigin = { null };
			Runnable showHelpPopup = () -> {
				if (!helpPopup.isVisible()) {
					predictionOrigin[0] = topCenterOnScreen(help);
					helpPopup.pack();
					Point anchor = help.getLocationOnScreen();
					helpPopup.setLocation(anchor.x + help.getWidth() / 2 - 11,
							anchor.y + help.getHeight() - 1);
					helpPopup.setVisible(true);
				}
			};
			help.addActionListener(event -> {
				showHelpPopup.run();
				forceRefresh.requestFocusInWindow();
			});
			AWTEventListener dismissHelpPopup = event -> {
				if (!(event instanceof MouseEvent mouseEvent) || !helpPopup.isVisible()
						|| !(mouseEvent.getSource() instanceof Component source)) {
					return;
				}
				boolean insideHelp = source == help || SwingUtilities.isDescendingFrom(source, help);
				boolean insidePopup = source == helpPopup || SwingUtilities.isDescendingFrom(source, helpPopup);
				if (insideHelp) {
					predictionOrigin[0] = topCenterOnScreen(help);
					return;
				}
				if (insidePopup) {
					return;
				}
				if (mouseEvent.getID() != MouseEvent.MOUSE_PRESSED && predictionOrigin[0] != null
						&& isInPredictionCone(mouseEvent.getLocationOnScreen(), predictionOrigin[0],
							helpPopup.getBounds())) {
					return;
				}
				helpPopup.setVisible(false);
				predictionOrigin[0] = null;
			};
			Toolkit.getDefaultToolkit().addAWTEventListener(dismissHelpPopup,
					AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK);
			dialog.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosed(WindowEvent event) {
					Toolkit.getDefaultToolkit().removeAWTEventListener(dismissHelpPopup);
					helpPopup.dispose();
				}
			});
			help.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseEntered(MouseEvent event) {
					showHelpPopup.run();
				}
			});
			long secondsUntilRefresh = Math.max(0,
					fetchResult.refreshAvailableAt().getEpochSecond() - Instant.now().getEpochSecond());
			long minutesUntilRefresh = Math.max(1, (secondsUntilRefresh + 59) / 60);
			cacheHeader.add(new JLabel(String.format(Locale.ROOT, trans.get("simedtdlg.lbl.nextNormalRefresh"),
					minutesUntilRefresh)), "newline, span 2");
			body.add(cacheHeader, BorderLayout.NORTH);
		}
		JEditorPane summaryPane = new JEditorPane("text/html", summary);
		summaryPane.setEditable(false);
		summaryPane.setFocusable(false);
		summaryPane.setHighlighter(null);
		summaryPane.setCursor(Cursor.getDefaultCursor());
		summaryPane.setOpaque(false);
		summaryPane.setBorder(null);
		summaryPane.addHyperlinkListener(event -> {
			if (event.getEventType() == javax.swing.event.HyperlinkEvent.EventType.ACTIVATED) {
				info.openrocket.swing.gui.util.URLUtil.openWebpage(event.getURL().toString());
			}
		});
		body.add(summaryPane, BorderLayout.CENTER);

		JButton customize = new JButton(trans.get("simedtdlg.but.customizeWeather"));
		customize.addActionListener(e -> {
			result[0] = WeatherPreviewAction.CUSTOMIZE;
			dialog.dispose();
		});
		JButton cancel = new JButton(trans.get("dlg.but.cancel"));
		cancel.addActionListener(e -> dialog.dispose());
		JButton apply = new JButton(trans.get("simedtdlg.but.applyWeather"));
		apply.addActionListener(e -> {
			result[0] = WeatherPreviewAction.APPLY;
			dialog.dispose();
		});
		JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
		rightActions.add(cancel);
		rightActions.add(apply);
		JPanel actions = new JPanel(new BorderLayout());
		actions.add(customize, BorderLayout.WEST);
		actions.add(rightActions, BorderLayout.EAST);

		JPanel content = new JPanel(new BorderLayout(0, 16));
		content.setBorder(BorderFactory.createEmptyBorder(18, 18, 14, 18));
		content.add(body, BorderLayout.CENTER);
		content.add(actions, BorderLayout.SOUTH);
		dialog.setContentPane(content);
		dialog.getRootPane().setDefaultButton(apply);
		dialog.pack();
		dialog.setLocationRelativeTo(owner);
		dialog.setVisible(true);
		return result[0];
	}

	private static Point topCenterOnScreen(Component component) {
		Point location = component.getLocationOnScreen();
		return new Point(location.x + component.getWidth() / 2, location.y);
	}

	private static boolean isInPredictionCone(Point pointer, Point origin, Rectangle popupBounds) {
		int popupBottom = popupBounds.y + popupBounds.height;
		if (pointer.y < origin.y || pointer.y > popupBottom) {
			return false;
		}
		Polygon cone = new Polygon(
				new int[] { origin.x, popupBounds.x, popupBounds.x + popupBounds.width },
				new int[] { origin.y, popupBottom, popupBottom }, 3);
		return cone.contains(pointer);
	}

	private static WeatherCustomization customizeApplySelection(Window owner, ApplySelection current,
			WeatherEdits currentEdits) {
		JCheckBox latitudeEnabled = new JCheckBox(trans.get("simedtdlg.checkbox.weatherLatitude"), current.latitude());
		JCheckBox longitudeEnabled = new JCheckBox(trans.get("simedtdlg.checkbox.weatherLongitude"), current.longitude());
		JCheckBox elevation = new JCheckBox(trans.get("simedtdlg.checkbox.weatherElevation"), current.elevation());
		JCheckBox temperature = new JCheckBox(trans.get("simedtdlg.checkbox.weatherTemperature"), current.temperature());
		JCheckBox pressure = new JCheckBox(trans.get("simedtdlg.checkbox.weatherPressure"), current.pressure());
		JCheckBox humidity = new JCheckBox(trans.get("simedtdlg.checkbox.weatherHumidity"), current.humidity());
		JCheckBox wind = new JCheckBox(trans.get("simedtdlg.checkbox.weatherWind"), current.wind());

		DoubleModel latitudeModel = new DoubleModel(currentEdits.latitude, UnitGroup.UNITS_LATITUDE, -90, 90);
		DoubleModel longitudeModel = new DoubleModel(currentEdits.longitude, UnitGroup.UNITS_LONGITUDE, -180, 180);
		DoubleModel elevationModel = new DoubleModel(currentEdits.elevation, UnitGroup.UNITS_DISTANCE, -500,
				ExtendedISAModel.getMaximumAllowedAltitude());
		DoubleModel temperatureModel = new DoubleModel(currentEdits.temperature, UnitGroup.UNITS_TEMPERATURE, 0);
		DoubleModel pressureModel = new DoubleModel(currentEdits.pressure, UnitGroup.UNITS_PRESSURE, 0);
		DoubleModel humidityModel = new DoubleModel(currentEdits.relativeHumidity, UnitGroup.UNITS_RELATIVE, 0, 1);
		JSpinner latitude = unitSpinner(latitudeModel);
		JSpinner longitude = unitSpinner(longitudeModel);
		JSpinner elevationValue = unitSpinner(elevationModel);
		JSpinner temperatureValue = unitSpinner(temperatureModel);
		JSpinner pressureValue = unitSpinner(pressureModel);
		JSpinner humidityValue = unitSpinner(humidityModel);

		MultiLevelPinkNoiseWindModel editableWind = new MultiLevelPinkNoiseWindModel();
		editableWind.clearLevels();
		editableWind.setAltitudeReference(AltitudeReference.MSL);
		for (CurrentConditions.WindLayer layer : currentEdits.windLayers) {
			editableWind.addWindLevel(layer.altitude(), layer.speed(), layer.direction(), layer.standardDeviation());
		}
		JButton editWind = new JButton(trans.get("simedtdlg.but.editWindLevels"));
		editWind.addActionListener(e -> new MultiLevelWindEditDialog(owner, editableWind).setVisible(true));

		JPanel fields = new JPanel(new MigLayout("insets 0, fillx", "[][220lp!][]"));
		fields.add(new JLabel(trans.get("simedtdlg.msg.chooseFieldsToUpdate")), "span 3, wrap");
		fields.add(latitudeEnabled);
		fields.add(latitude, "growx");
		fields.add(new UnitSelector(latitudeModel), "growx, wrap");
		fields.add(longitudeEnabled);
		fields.add(longitude, "growx");
		fields.add(new UnitSelector(longitudeModel), "growx, wrap");
		fields.add(elevation);
		fields.add(elevationValue, "growx");
		fields.add(new UnitSelector(elevationModel), "growx, wrap");
		fields.add(temperature);
		fields.add(temperatureValue, "growx");
		fields.add(new UnitSelector(temperatureModel), "growx, wrap");
		fields.add(pressure);
		fields.add(pressureValue, "growx");
		fields.add(new UnitSelector(pressureModel), "growx, wrap");
		fields.add(humidity);
		fields.add(humidityValue, "growx");
		fields.add(new UnitSelector(humidityModel), "growx, wrap");
		fields.add(wind);
		fields.add(editWind, "span 2, growx");
		int result = JOptionPane.showConfirmDialog(owner, fields, trans.get("simedtdlg.title.customizeWeather"),
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (result != JOptionPane.OK_OPTION) {
			return new WeatherCustomization(current, currentEdits);
		}
		List<CurrentConditions.WindLayer> windLayers = editableWind.getLevels().stream()
				.map(level -> new CurrentConditions.WindLayer(level.getAltitude(), level.getSpeed(), level.getDirection(),
						level.getStandardDeviation())).toList();
		WeatherEdits edits = new WeatherEdits(latitudeModel.getValue(), longitudeModel.getValue(),
				elevationModel.getValue(), temperatureModel.getValue(), pressureModel.getValue(),
				humidityModel.getValue(), windLayers);
		ApplySelection selection = new ApplySelection(latitudeEnabled.isSelected(), longitudeEnabled.isSelected(),
				elevation.isSelected(),
				temperature.isSelected(), pressure.isSelected(), humidity.isSelected(), wind.isSelected());
		return new WeatherCustomization(selection, edits);
	}

	private static JSpinner unitSpinner(DoubleModel model) {
		JSpinner spinner = new JSpinner(model.getSpinnerModel());
		spinner.setEditor(new SpinnerEditor(spinner));
		return spinner;
	}

	private WeatherEdits editsFor(CurrentConditions conditions) {
		WeatherEditKey key = new WeatherEditKey(conditions.latitude(), conditions.longitude(), conditions.validAt());
		if (!key.equals(weatherEditKey) || weatherEdits == null) {
			weatherEditKey = key;
			weatherEdits = new WeatherEdits(conditions.latitude(), conditions.longitude(), conditions.elevation(),
					conditions.temperature(), conditions.pressure(), conditions.relativeHumidity(),
					conditions.windLayers());
		}
		return weatherEdits;
	}

	private static void applyWeatherConditions(SimulationOptions options, WeatherEdits edits,
			ApplySelection selection) {
		if (selection.latitude()) options.setLaunchLatitude(edits.latitude);
		if (selection.longitude()) options.setLaunchLongitude(edits.longitude);
		if (selection.elevation()) options.setLaunchAltitude(edits.elevation);
		if (selection.temperature() || selection.pressure() || selection.humidity()) {
			options.setISAAtmosphere(false);
		}
		if (selection.temperature()) options.setLaunchTemperature(edits.temperature);
		if (selection.pressure()) options.setLaunchPressure(edits.pressure);
		if (selection.humidity()) options.setLaunchRelativeHumidity(edits.relativeHumidity);

		if (selection.wind()) {
			CurrentConditions.WindLayer surface = edits.windLayers.get(0);
			PinkNoiseWindModel averageWind = options.getAverageWindModel();
			averageWind.setAverage(surface.speed());
			averageWind.setDirection(surface.direction());
			averageWind.setStandardDeviation(surface.standardDeviation());

			MultiLevelPinkNoiseWindModel windModel = options.getMultiLevelWindModel();
			windModel.clearLevels();
			windModel.setAltitudeReference(AltitudeReference.MSL);
			for (CurrentConditions.WindLayer layer : edits.windLayers) {
				windModel.addWindLevel(layer.altitude(), layer.speed(), layer.direction(), layer.standardDeviation());
			}
			options.setWindModelType(WindModelType.MULTI_LEVEL);
		}
	}

	private static Window panelOwner(Component component) {
		return SwingUtilities.getWindowAncestor(component);
	}

	private static void showCurrentConditionsError(Component parent, String message) {
		JOptionPane.showMessageDialog(panelOwner(parent), message, trans.get("simedtdlg.title.currentConditions"),
				JOptionPane.ERROR_MESSAGE);
	}

	private record ConditionsLookup(DeviceLocation location, CurrentConditions conditions, WeatherRequest request,
			FetchResult fetchResult) {
	}

	private record WeatherRequest(Instant forecastAt, LocationSource locationSource, DeviceLocation selectedLocation,
			boolean forceRefresh) {
		private boolean isForecast() {
			return forecastAt != null;
		}

		private boolean usesDeviceLocation() {
			return locationSource == LocationSource.DEVICE;
		}

		private WeatherRequest withConfiguredLocation() {
			return new WeatherRequest(forecastAt, LocationSource.CONFIGURED, selectedLocation, forceRefresh);
		}

		private WeatherRequest withForceRefresh() {
			return new WeatherRequest(forecastAt, locationSource, selectedLocation, true);
		}
	}

	private record WeatherPreviewResult(ApplySelection selection, WeatherEdits edits, boolean forceRefresh) {
	}

	private enum WeatherPreviewAction {
		CUSTOMIZE, CANCEL, APPLY, FORCE_REFRESH
	}

	private record WeatherCustomization(ApplySelection selection, WeatherEdits edits) {
	}

	private record WeatherEditKey(double latitude, double longitude, Instant validAt) {
	}

	private record WeatherEdits(double latitude, double longitude, double elevation, double temperature,
			double pressure, double relativeHumidity, List<CurrentConditions.WindLayer> windLayers) {
		private WeatherEdits {
			windLayers = List.copyOf(windLayers);
		}
	}

	private enum LocationSource {
		DEVICE, CONFIGURED, SELECTED
	}

	private record ApplySelection(boolean latitude, boolean longitude, boolean elevation, boolean temperature, boolean pressure,
			boolean humidity, boolean wind) {
		private static ApplySelection all() {
			return new ApplySelection(true, true, true, true, true, true, true);
		}
	}

}
