package info.openrocket.swing.gui.simulation.currentconditions;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GraphicsConfiguration;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import com.formdev.flatlaf.FlatClientProperties;

import info.openrocket.core.l10n.Translator;
import info.openrocket.core.models.atmosphere.ExtendedISAModel;
import info.openrocket.core.models.wind.MultiLevelPinkNoiseWindModel;
import info.openrocket.core.models.wind.PinkNoiseWindModel;
import info.openrocket.core.models.wind.WindModel.AltitudeReference;
import info.openrocket.core.models.wind.WindModelType;
import info.openrocket.core.simulation.SimulationOptions;
import info.openrocket.core.simulation.WeatherSource;
import info.openrocket.core.startup.Application;
import info.openrocket.core.unit.Unit;
import info.openrocket.core.unit.UnitGroup;
import info.openrocket.swing.gui.SpinnerEditor;
import info.openrocket.swing.gui.adaptors.DoubleModel;
import info.openrocket.swing.gui.components.UnitSelector;
import info.openrocket.swing.gui.simulation.MultiLevelWindEditDialog;
import info.openrocket.swing.gui.util.GUIUtil;
import info.openrocket.swing.gui.simulation.currentconditions.OpenMeteoClient.FetchResult;
import info.openrocket.swing.gui.simulation.currentconditions.OpenMeteoClient.RefreshRateLimitException;
import net.miginfocom.swing.MigLayout;

/** Coordinates weather selection, retrieval, preview, customization, and application for a simulation editor. */
public final class WeatherConditionsController {
	private static final Translator trans = Application.getTranslator();
	private static final int DEFAULT_CHOOSER_WIDTH = 520;
	private static final int MINIMUM_CHOOSER_WIDTH = 360;
	private static final int DIALOG_HORIZONTAL_OVERHEAD = 160;
	private final WeatherCustomizationPreferences customizationPreferences;
	private Instant selectedForecastTime;
	private final Map<UnitGroup, Unit> displayUnits = new HashMap<>();
	private DeviceLocation selectedWeatherLocation;
	private ApplySelection savedApplySelection;
	private Set<Integer> savedExcludedWindLevelIndices;
	private Double savedTurbulenceIntensity;
	private SwingWorker<ConditionsLookup, Void> weatherWorker;

	public WeatherConditionsController() {
		this(new WeatherCustomizationPreferences());
	}

	WeatherConditionsController(WeatherCustomizationPreferences customizationPreferences) {
		this.customizationPreferences = customizationPreferences;
		this.savedApplySelection = customizationPreferences.loadFieldSettings()
				.map(ApplySelection::from).orElseGet(ApplySelection::all);
		this.savedExcludedWindLevelIndices = customizationPreferences.loadExcludedWindLevelIndices();
		OptionalDouble savedTurbulence = customizationPreferences.loadTurbulenceIntensity();
		this.savedTurbulenceIntensity = savedTurbulence.isPresent() ? savedTurbulence.getAsDouble() : null;
	}

	public void cancel() {
		if (weatherWorker != null && !weatherWorker.isDone()) {
			weatherWorker.cancel(true);
		}
	}

	public void request(JButton button, SimulationOptions options, JPanel conditionsPanel) {
		displayUnits.clear();
		readDisplayUnits(conditionsPanel);
		if (options.getWeatherSource() != null) {
			var source = options.getWeatherSource();
			selectedForecastTime = source.kind().equals("forecast") && !source.isExpired(Instant.now())
					? source.validAt() : null;
		}
		WeatherRequest request = chooseWeatherRequest(panelOwner(button), options);
		if (request != null) {
			fetchWeatherConditions(button, options, request);
		}
	}

	private void readDisplayUnits(Container container) {
		for (Component component : container.getComponents()) {
			if (component instanceof UnitSelector selector) {
				displayUnits.putIfAbsent(selector.getUnitGroup(), selector.getSelectedUnit());
			} else if (component instanceof Container child) {
				readDisplayUnits(child);
			}
		}
	}

	private String formatValue(UnitGroup group, double value) {
		return displayUnits.getOrDefault(group, group.getDefaultUnit()).toStringUnit(value);
	}

	public static String weatherButtonText(SimulationOptions options) {
		var source = options.getWeatherSource();
		return trans.get(source != null && !source.isExpired(Instant.now())
				? "simedtdlg.but.updateWeatherConditions" : "simedtdlg.but.currentConditions");
	}

	public String sourceDetails(SimulationOptions options) {
		var source = options.getWeatherSource();
		if (source == null) return trans.get("simedtdlg.ttip.weatherSource.none");
		String details = String.format(trans.get("simedtdlg.lbl.weatherSource"),
				formatForecastTime(source.validAt()),
				formatForecastTime(source.fetchedAt()));
		if (source.isExpired(Instant.now())) {
			details += "<br>" + trans.get(source.kind().equals("current")
					? "simedtdlg.lbl.weatherSource.refreshDue" : "simedtdlg.lbl.weatherSource.past");
		}
		if (source.isEdited(options)) details += "<br>" + trans.get("simedtdlg.lbl.weatherSource.edited");
		if (source.isSiteMoved(options)) details += "<br>" + trans.get("simedtdlg.lbl.weatherSource.siteMoved");
		String fields = source.groups().stream().map(group -> group.equals("wind") ? trans.get("simedtdlg.lbl.Wind") : trans.get("simedtdlg.checkbox.weather"
				+ Character.toUpperCase(group.charAt(0)) + group.substring(1))).collect(Collectors.joining(", "));
		return "<html>" + details + "<br><br>"
				+ String.format(trans.get("simedtdlg.lbl.weatherSource.details"), fields,
						source.latitude(), source.longitude(), UnitGroup.UNITS_DISTANCE.toStringUnit(source.elevation()))
				+ "</html>";
	}

	private WeatherRequest chooseWeatherRequest(Window owner, SimulationOptions options) {
		Instant[] forecastTime = { selectedForecastTime };
		JLabel dateTime = new JLabel(forecastTime[0] == null
				? trans.get("simedtdlg.lbl.currentTime") : formatForecastTime(forecastTime[0]));
		JButton chooseDateTime = new JButton(trans.get("simedtdlg.but.chooseForecastTime"));

		DeviceLocation configuredLocation = new DeviceLocation(options.getLaunchLatitude(), options.getLaunchLongitude(),
				options.getLaunchAltitude(), Double.NaN, trans.get("simedtdlg.lbl.configuredCoordinates"));
		if (selectedWeatherLocation != null && sameCoordinates(selectedWeatherLocation, configuredLocation)) {
			configuredLocation = configuredLocation.withTimezone(selectedWeatherLocation.timezoneId());
		}
		DeviceLocation[] selectedLocation = { configuredLocation };
		ZoneId timezone = ZoneId.systemDefault();
		JLabel locationLabel = new JLabel(formatLocation(configuredLocation));
		JButton chooseLocation = new JButton(trans.get("simedtdlg.but.chooseWeatherLocation"));
		JLabel availability = new JLabel();
		Runnable refreshTimezoneLabels = () -> {
			availability.setText(String.format(Locale.ROOT, trans.get("simedtdlg.msg.forecastAvailability"),
							OpenMeteoClient.MAX_PAST_DAYS, OpenMeteoClient.MAX_FORECAST_DAYS, timezone.getId()));
			dateTime.setText(forecastTime[0] == null ? trans.get("simedtdlg.lbl.currentTime")
					: formatForecastTime(forecastTime[0]));
		};
		java.util.function.Consumer<DeviceLocation> updateLocation = chosen -> {
			selectedLocation[0] = chosen;
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
			Instant now = Instant.now();
			Instant firstHistoricalHour = LocalDate.ofInstant(now, timezone)
					.minusDays(OpenMeteoClient.MAX_PAST_DAYS).atStartOfDay(timezone).toInstant();
			Instant lastForecastHour = LocalDate.ofInstant(now, timezone)
					.plusDays(OpenMeteoClient.MAX_FORECAST_DAYS - 1L).atTime(23, 0).atZone(timezone).toInstant();
			Instant initial = forecastTime[0] != null && !forecastTime[0].isBefore(firstHistoricalHour)
					&& !forecastTime[0].isAfter(lastForecastHour) ? forecastTime[0] : null;
			Window pickerOwner = SwingUtilities.getWindowAncestor(chooseDateTime);
			ForecastDateTimePicker.Selection chosen = ForecastDateTimePicker.show(
					pickerOwner == null ? owner : pickerOwner, initial, firstHistoricalHour, lastForecastHour, timezone);
			if (chosen != null) {
				forecastTime[0] = chosen.now() ? null : chosen.forecastAt();
				refreshTimezoneLabels.run();
			}
		});
		refreshTimezoneLabels.run();

		JPanel chooser = new JPanel(new MigLayout("insets 0, fillx", "[grow]"));
		JPanel locationRow = new JPanel(new BorderLayout());
		locationRow.add(new JLabel(trans.get("simedtdlg.msg.chooseWeatherLocation")), BorderLayout.WEST);
		locationRow.add(chooseLocation, BorderLayout.EAST);
		chooser.add(locationRow, "growx, wrap");
		chooser.add(locationLabel, "gapbottom rel, wrap");
		chooser.add(new JSeparator(), "span, growx, gapbottom rel, wrap");
		JPanel timeRow = new JPanel(new BorderLayout());
		timeRow.add(new JLabel(trans.get("simedtdlg.lbl.conditionsFor")), BorderLayout.WEST);
		timeRow.add(chooseDateTime, BorderLayout.EAST);
		chooser.add(timeRow, "growx, wrap");
		chooser.add(dateTime, "wrap");
		chooser.add(availability, "span, wrap");
		Dimension chooserSize = chooser.getPreferredSize();
		int controlWidth = Math.max(locationRow.getPreferredSize().width, timeRow.getPreferredSize().width);
		chooser.setPreferredSize(new Dimension(chooserWidth(owner, controlWidth), chooserSize.height));

		int choice = JOptionPane.showConfirmDialog(owner, chooser, trans.get("simedtdlg.title.currentConditions"),
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
		selectedForecastTime = forecastTime[0];
		selectedWeatherLocation = selectedLocation[0];
		if (choice != JOptionPane.OK_OPTION) {
			return null;
		}
		return new WeatherRequest(forecastTime[0], LocationSource.SELECTED, selectedLocation[0], false);
	}

	private static int chooserWidth(Window owner, int controlWidth) {
		int preferredWidth = Math.max(DEFAULT_CHOOSER_WIDTH, controlWidth);
		if (owner == null) {
			return preferredWidth;
		}
		GraphicsConfiguration configuration = owner.getGraphicsConfiguration();
		if (configuration == null) {
			return preferredWidth;
		}
		Rectangle screen = configuration.getBounds();
		Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(configuration);
		int usableWidth = screen.width - insets.left - insets.right;
		int maximumWidth = Math.max(MINIMUM_CHOOSER_WIDTH, usableWidth - DIALOG_HORIZONTAL_OVERHEAD);
		return Math.min(preferredWidth, maximumWidth);
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

	private static String formatForecastTime(Instant time) {
		ZoneId zone = ZoneId.systemDefault();
		return DateTimeFormatter.ofPattern("MMM d, uuuu h:mm a z", Locale.getDefault()).withZone(zone).format(time);
	}

	private static String formatLocation(DeviceLocation location) {
		return String.format(Locale.ROOT, "<html>%s:<br>%.5f°, %.5f°</html>", escapeHtml(location.source()),
				location.latitude(), location.longitude());
	}

	private static String escapeHtml(String value) {
		return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
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
						ApplySelection selection = previewResult.selection();
						List<String> groups = new ArrayList<>();
						if (selection.latitude()) groups.add("latitude");
						if (selection.longitude()) groups.add("longitude");
						if (selection.elevation()) groups.add("elevation");
						if (selection.temperature()) groups.add("temperature");
						if (selection.pressure()) groups.add("pressure");
						if (selection.humidity()) groups.add("humidity");
						if (selection.wind()) groups.add("wind");
						if (selection.turbulence() && !selection.wind()
								&& previewResult.edits().turbulenceIntensity != null) groups.add("turbulence");
						if (!groups.isEmpty()) {
							String kind = !lookup.request().isForecast() ? "current"
									: lookup.conditions().validAt().isBefore(lookup.fetchResult().fetchedAt()) ? "historical" : "forecast";
							options.setWeatherSource(new WeatherSource("open-meteo", "forecast", kind,
									lookup.conditions().validAt(), lookup.fetchResult().fetchedAt(),
									kind.equals("current") ? lookup.fetchResult().refreshAvailableAt() : lookup.conditions().validAt(),
									timezoneOf(lookup.location()).getId(),
									lookup.conditions().latitude(), lookup.conditions().longitude(), lookup.conditions().elevation(),
									groups, WeatherSource.snapshot(options, groups)));
						}
					}
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					showCurrentConditionsError(button, trans.get("simedtdlg.error.currentConditionsInterrupted"));
				} catch (ExecutionException e) {
					Throwable cause = e.getCause();
					if (cause instanceof RefreshRateLimitException rateLimit) {
						String availableAt = formatWeatherTime(rateLimit.getAvailableAt());
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
						button.setText(weatherButtonText(options));
						button.setEnabled(true);
					}
				}
			}
		};
		weatherWorker = worker;
		worker.execute();
	}

	private static String formatWeatherTime(Instant time) {
		ZoneId zone = ZoneId.systemDefault();
		return DateTimeFormatter.ofPattern("MMM d, uuuu h:mm:ss a z", Locale.getDefault())
				.withZone(zone).format(time);
	}

	private WeatherPreviewResult confirmWeatherConditions(Window owner, ConditionsLookup lookup) {
		CurrentConditions conditions = lookup.conditions();
		WeatherEdits edits = editsFor(conditions);
		String preview = trans.get("simedtdlg.msg.currentConditionsPreview");
		ZoneId timezone = ZoneId.systemDefault();
		String validAt = DateTimeFormatter.ofPattern("MMM d, uuuu h:mm a z", Locale.getDefault())
				.withZone(timezone).format(conditions.validAt());
		String accuracy = Double.isFinite(lookup.location().horizontalAccuracy())
				? " (±" + formatValue(UnitGroup.UNITS_DISTANCE, lookup.location().horizontalAccuracy()) + ")" : "";
		ApplySelection selection = savedApplySelection;
		Set<Integer> excludedWindLevelIndices = savedExcludedWindLevelIndices;
		while (true) {
			List<CurrentConditions.WindLayer> selectedWindLayers =
					selectedWindLayers(edits.windLayers, excludedWindLevelIndices);
			List<CurrentConditions.WindLayer> displayedWindLayers = selectedWindLayers.isEmpty()
					? edits.windLayers : selectedWindLayers;
			CurrentConditions.WindLayer surfaceWind = displayedWindLayers.get(0);
			String heading = lookup.fetchResult().cached() ? "" : "<b>" + preview + "</b><br><br>";
			String windProfile = selectedWindLayers.size() == edits.windLayers.size()
					? String.format(Locale.ROOT, "%d layers to %s MSL", selectedWindLayers.size(),
							formatValue(UnitGroup.UNITS_DISTANCE, displayedWindLayers.get(displayedWindLayers.size() - 1).altitude()))
					: String.format(Locale.ROOT, "%d of %d layers to %s MSL", selectedWindLayers.size(),
							edits.windLayers.size(), formatValue(UnitGroup.UNITS_DISTANCE, displayedWindLayers.get(displayedWindLayers.size() - 1).altitude()));
			String summary = String.format(Locale.ROOT, trans.get("simedtdlg.msg.weatherSummary"),
					heading,
					formatPreviewField(selection.latitude() && selection.longitude(), String.format(Locale.ROOT,
							"Location: %.5f°, %.5f°%s", edits.latitude, edits.longitude, accuracy)),
					formatPreviewField(selection.elevation(),
							"Launch elevation: " + formatValue(UnitGroup.UNITS_DISTANCE, edits.elevation) + " MSL"), validAt,
					formatPreviewField(selection.temperature(),
							"Temperature: " + formatValue(UnitGroup.UNITS_TEMPERATURE, edits.temperature)),
					formatPreviewField(selection.pressure(),
							"Pressure: " + formatValue(UnitGroup.UNITS_PRESSURE, edits.pressure)),
					formatPreviewField(selection.humidity(),
							"Humidity: " + formatValue(UnitGroup.UNITS_RELATIVE, edits.relativeHumidity)),
					formatPreviewField(selection.wind(), String.format(Locale.ROOT,
							"Surface wind: %s from %s; gusts %s", formatValue(UnitGroup.UNITS_WINDSPEED, surfaceWind.speed()),
							formatValue(UnitGroup.UNITS_ANGLE, surfaceWind.direction()), formatValue(UnitGroup.UNITS_WINDSPEED, conditions.windGust()))),
					formatPreviewField(selection.wind(), "Vertical wind profile: " + windProfile),
					formatPreviewField(selection.turbulence(), edits.turbulenceIntensity == null
							? "Turbulence intensity: varies by level"
							: "Turbulence intensity: " + formatValue(UnitGroup.UNITS_RELATIVE, edits.turbulenceIntensity)),
					trans.get("simedtdlg.msg.weatherAttribution"));
			WeatherPreviewAction action = showWeatherPreviewDialog(owner, summary, lookup.fetchResult(), timezone);
			if (action == WeatherPreviewAction.OK) {
				return new WeatherPreviewResult(selection, withIncludedWindLayers(edits, excludedWindLevelIndices), false);
			}
			if (action == WeatherPreviewAction.FORCE_REFRESH) {
				return new WeatherPreviewResult(selection, edits, true);
			}
			if (action != WeatherPreviewAction.CUSTOMIZE) {
				return null;
			}
			WeatherCustomization customization = customizeApplySelection(owner, selection, edits,
					excludedWindLevelIndices);
			if (customization.action() == CustomizationAction.CANCEL) {
				continue;
			}
			selection = customization.selection();
			edits = customization.edits();
			excludedWindLevelIndices = customization.excludedWindLevelIndices();
			if (customization.action() == CustomizationAction.SAVE_AND_APPLY) {
				customizationPreferences.saveFieldSettings(selection.toFieldSettings());
				customizationPreferences.saveExcludedWindLevelIndices(excludedWindLevelIndices);
				if (edits.turbulenceIntensity == null) {
					customizationPreferences.clearTurbulenceIntensity();
				} else {
					customizationPreferences.saveTurbulenceIntensity(edits.turbulenceIntensity);
				}
				savedApplySelection = selection;
				savedExcludedWindLevelIndices = excludedWindLevelIndices;
				savedTurbulenceIntensity = edits.turbulenceIntensity;
			}
			continue;
		}
	}

	static String formatPreviewField(boolean included, String value) {
		return included ? value : "<font color='#808080'><strike>" + value + "</strike></font>";
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
			JPanel helpContent = new JPanel(new BorderLayout(0, 10));
			helpContent.setOpaque(false);
			helpContent.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
			helpContent.add(new JLabel(trans.get("simedtdlg.ttip.cachedWeather")), BorderLayout.CENTER);
			WeatherHelpButton help = new WeatherHelpButton(trans.get("simedtdlg.lbl.usingCachedWeather"), () -> helpContent);
			cacheHeader.add(help, "w 22lp!, h 22lp!");
			JButton forceRefresh = new JButton(trans.get("simedtdlg.but.forceRefreshWeather"));
			forceRefresh.addActionListener(e -> {
				Instant availableAt = fetchResult.forceRefreshAvailableAt();
				if (Instant.now().isBefore(availableAt)) {
					JOptionPane.showMessageDialog(dialog,
							String.format(Locale.ROOT, trans.get("simedtdlg.msg.forceRefreshRateLimited"),
									formatWeatherTime(availableAt)),
							trans.get("simedtdlg.title.currentConditions"), JOptionPane.INFORMATION_MESSAGE);
					return;
				}
				result[0] = WeatherPreviewAction.FORCE_REFRESH;
				dialog.dispose();
			});
			helpContent.add(forceRefresh, BorderLayout.SOUTH);
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
		JButton ok = new JButton(trans.get("dlg.but.ok"));
		ok.addActionListener(e -> {
			result[0] = WeatherPreviewAction.OK;
			dialog.dispose();
		});
		JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
		rightActions.add(cancel);
		rightActions.add(ok);
		JPanel actions = new JPanel(new BorderLayout());
		actions.add(customize, BorderLayout.WEST);
		actions.add(rightActions, BorderLayout.EAST);

		JPanel content = new JPanel(new BorderLayout(0, 16));
		content.setBorder(BorderFactory.createEmptyBorder(18, 18, 14, 18));
		content.add(body, BorderLayout.CENTER);
		content.add(actions, BorderLayout.SOUTH);
		dialog.setContentPane(content);
		dialog.getRootPane().setDefaultButton(ok);
		dialog.pack();
		dialog.setLocationRelativeTo(owner);
		dialog.setVisible(true);
		return result[0];
	}

	private WeatherCustomization customizeApplySelection(Window owner, ApplySelection current,
			WeatherEdits currentEdits, Set<Integer> currentExcludedWindLevelIndices) {
		JCheckBox latitudeEnabled = new JCheckBox(trans.get("simedtdlg.checkbox.weatherLatitude"), current.latitude());
		JCheckBox longitudeEnabled = new JCheckBox(trans.get("simedtdlg.checkbox.weatherLongitude"), current.longitude());
		JCheckBox elevation = new JCheckBox(trans.get("simedtdlg.checkbox.weatherElevation"), current.elevation());
		JCheckBox temperature = new JCheckBox(trans.get("simedtdlg.checkbox.weatherTemperature"), current.temperature());
		JCheckBox pressure = new JCheckBox(trans.get("simedtdlg.checkbox.weatherPressure"), current.pressure());
		JCheckBox humidity = new JCheckBox(trans.get("simedtdlg.checkbox.weatherHumidity"), current.humidity());
		JCheckBox wind = new JCheckBox(trans.get("simedtdlg.checkbox.weatherWind"), current.wind());
		JCheckBox turbulence = new JCheckBox(trans.get("simedtdlg.checkbox.weatherTurbulence"),
				current.turbulence());

		DoubleModel latitudeModel = new DoubleModel(currentEdits.latitude, UnitGroup.UNITS_LATITUDE, -90, 90);
		DoubleModel longitudeModel = new DoubleModel(currentEdits.longitude, UnitGroup.UNITS_LONGITUDE, -180, 180);
		DoubleModel elevationModel = new DoubleModel(currentEdits.elevation, UnitGroup.UNITS_DISTANCE, -500,
				ExtendedISAModel.getMaximumAllowedAltitude());
		DoubleModel temperatureModel = new DoubleModel(currentEdits.temperature, UnitGroup.UNITS_TEMPERATURE, 0);
		DoubleModel pressureModel = new DoubleModel(currentEdits.pressure, UnitGroup.UNITS_PRESSURE, 0);
		DoubleModel humidityModel = new DoubleModel(currentEdits.relativeHumidity, UnitGroup.UNITS_RELATIVE, 0, 1);
		boolean initialMixedTurbulence = currentEdits.turbulenceIntensity == null;
		DoubleModel turbulenceModel = new DoubleModel(initialMixedTurbulence ? 0 : currentEdits.turbulenceIntensity,
				UnitGroup.UNITS_RELATIVE, 0, 1);
		for (DoubleModel model : List.of(latitudeModel, longitudeModel, elevationModel, temperatureModel,
				pressureModel, humidityModel, turbulenceModel)) {
			model.setCurrentUnit(displayUnits.getOrDefault(model.getUnitGroup(), model.getUnitGroup().getDefaultUnit()));
		}
		JSpinner latitude = unitSpinner(latitudeModel);
		JSpinner longitude = unitSpinner(longitudeModel);
		JSpinner elevationValue = unitSpinner(elevationModel);
		JSpinner temperatureValue = unitSpinner(temperatureModel);
		JSpinner pressureValue = unitSpinner(pressureModel);
		JSpinner humidityValue = unitSpinner(humidityModel);
		JSpinner turbulenceValue = unitSpinner(turbulenceModel);
		boolean[] mixedTurbulence = { initialMixedTurbulence };
		boolean[] updatingTurbulenceModel = { false };

		MultiLevelPinkNoiseWindModel editableWind = new MultiLevelPinkNoiseWindModel();
		editableWind.clearLevels();
		editableWind.setAltitudeReference(AltitudeReference.MSL);
		for (CurrentConditions.WindLayer layer : currentEdits.windLayers) {
			editableWind.addWindLevel(layer.altitude(), layer.speed(), layer.direction(), layer.standardDeviation());
		}
		AtomicReference<List<Double>> turbulenceRestoreValues =
				new AtomicReference<>(standardDeviations(editableWind));
		boolean[] globalTurbulenceOverrideActive = { false };
		if (mixedTurbulence[0]) {
			showMixedValue(turbulenceValue);
		} else if (turbulence.isSelected()) {
			setTurbulenceIntensity(editableWind, turbulenceModel.getValue());
			globalTurbulenceOverrideActive[0] = true;
		}
		turbulenceModel.addChangeListener(e -> {
			if (!updatingTurbulenceModel[0]) {
				mixedTurbulence[0] = false;
			}
			if (!updatingTurbulenceModel[0] && turbulence.isSelected()) {
				if (!globalTurbulenceOverrideActive[0]) {
					turbulenceRestoreValues.set(standardDeviations(editableWind));
				}
				setTurbulenceIntensity(editableWind, turbulenceModel.getValue());
				globalTurbulenceOverrideActive[0] = true;
			}
		});
		turbulence.addActionListener(e -> {
			if (turbulence.isSelected() && !mixedTurbulence[0]) {
				if (!globalTurbulenceOverrideActive[0]) {
					turbulenceRestoreValues.set(standardDeviations(editableWind));
				}
				setTurbulenceIntensity(editableWind, turbulenceModel.getValue());
				globalTurbulenceOverrideActive[0] = true;
			} else if (!turbulence.isSelected() && globalTurbulenceOverrideActive[0]) {
				restoreStandardDeviations(editableWind, turbulenceRestoreValues.get());
				globalTurbulenceOverrideActive[0] = false;
				OptionalDouble restoredTurbulence = uniformTurbulenceIntensity(editableWind);
				if (restoredTurbulence.isPresent()) {
					updatingTurbulenceModel[0] = true;
					turbulenceModel.setValue(restoredTurbulence.getAsDouble());
					updatingTurbulenceModel[0] = false;
					mixedTurbulence[0] = false;
				} else {
					mixedTurbulence[0] = true;
					showMixedValue(turbulenceValue);
				}
			}
		});
		Set<Integer> initialExcludedWindLevelIndices = current.wind()
				? currentExcludedWindLevelIndices : allWindLevelIndices(editableWind.getLevels().size());
		AtomicReference<Set<Integer>> excludedWindLevelIndices =
				new AtomicReference<>(Set.copyOf(initialExcludedWindLevelIndices));
		updateWindImportCheckbox(wind, editableWind.getLevels().size(), excludedWindLevelIndices.get());
		wind.addActionListener(e -> {
			boolean wasIndeterminate = FlatClientProperties.SELECTED_STATE_INDETERMINATE.equals(
					wind.getClientProperty(FlatClientProperties.SELECTED_STATE));
			Set<Integer> excluded = wasIndeterminate || wind.isSelected()
					? Set.of() : allWindLevelIndices(editableWind.getLevels().size());
			excludedWindLevelIndices.set(excluded);
			updateWindImportCheckbox(wind, editableWind.getLevels().size(), excluded);
		});
		JButton editWind = new JButton(trans.get("simedtdlg.but.editWindLevels"));
		editWind.addActionListener(e -> {
			if (turbulence.isSelected() && !mixedTurbulence[0]) {
				if (!globalTurbulenceOverrideActive[0]) {
					turbulenceRestoreValues.set(standardDeviations(editableWind));
				}
				setTurbulenceIntensity(editableWind, turbulenceModel.getValue());
				globalTurbulenceOverrideActive[0] = true;
			}
			MultiLevelPinkNoiseWindModel workingWind = copyWindModel(editableWind);
			MultiLevelWindEditDialog dialog = new MultiLevelWindEditDialog(owner, workingWind,
					excludedWindLevelIndices.get());
			dialog.setVisible(true);
			MultiLevelWindEditDialog.WeatherImportResult result = dialog.getWeatherImportResult();
			if (result.action() == MultiLevelWindEditDialog.WeatherImportAction.CANCEL) {
				return;
			}
			replaceWindModel(editableWind, workingWind);
			globalTurbulenceOverrideActive[0] = false;
			turbulenceRestoreValues.set(standardDeviations(editableWind));
			OptionalDouble uniformTurbulence = uniformTurbulenceIntensity(editableWind);
			if (uniformTurbulence.isPresent()) {
				updatingTurbulenceModel[0] = true;
				turbulenceModel.setValue(uniformTurbulence.getAsDouble());
				updatingTurbulenceModel[0] = false;
				mixedTurbulence[0] = false;
			} else {
				mixedTurbulence[0] = true;
				showMixedValue(turbulenceValue);
			}
			excludedWindLevelIndices.set(result.excludedWindLevelIndices());
			updateWindImportCheckbox(wind, editableWind.getLevels().size(), result.excludedWindLevelIndices());
		});

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
		fields.add(new JSeparator(), "span 3, growx, gaptop 8, wrap");
		fields.add(turbulence);
		fields.add(turbulenceValue, "growx");
		fields.add(new UnitSelector(turbulenceModel), "growx, wrap");
		fields.add(new JLabel(trans.get("simedtdlg.msg.weatherTurbulenceDescription")), "span 3, growx, wrap");
		CustomizationAction action = showWeatherCustomizationDialog(owner, fields);
		if (action == CustomizationAction.CANCEL) {
			return new WeatherCustomization(current, currentEdits, currentExcludedWindLevelIndices,
					CustomizationAction.CANCEL);
		}
		for (DoubleModel model : List.of(latitudeModel, longitudeModel, elevationModel, temperatureModel,
				pressureModel, humidityModel, turbulenceModel)) {
			displayUnits.put(model.getUnitGroup(), model.getCurrentUnit());
		}
		List<CurrentConditions.WindLayer> windLayers = editableWind.getLevels().stream()
				.map(level -> new CurrentConditions.WindLayer(level.getAltitude(), level.getSpeed(), level.getDirection(),
						level.getStandardDeviation())).toList();
		WeatherEdits edits = new WeatherEdits(latitudeModel.getValue(), longitudeModel.getValue(),
				elevationModel.getValue(), temperatureModel.getValue(), pressureModel.getValue(),
				humidityModel.getValue(), windLayers,
				mixedTurbulence[0] ? null : turbulenceModel.getValue());
		ApplySelection selection = new ApplySelection(latitudeEnabled.isSelected(), longitudeEnabled.isSelected(),
				elevation.isSelected(),
				temperature.isSelected(), pressure.isSelected(), humidity.isSelected(),
				excludedWindLevelIndices.get().size() < editableWind.getLevels().size(), turbulence.isSelected());
		return new WeatherCustomization(selection, edits, excludedWindLevelIndices.get(), action);
	}

	static void updateWindImportCheckbox(JCheckBox checkbox, int levelCount, Set<Integer> excludedIndices) {
		long excludedCount = excludedIndices.stream().filter(index -> index >= 0 && index < levelCount).count();
		if (excludedCount == 0) {
			checkbox.putClientProperty(FlatClientProperties.SELECTED_STATE, null);
			checkbox.setSelected(true);
		} else if (excludedCount == levelCount) {
			checkbox.putClientProperty(FlatClientProperties.SELECTED_STATE, null);
			checkbox.setSelected(false);
		} else {
			checkbox.setSelected(true);
			checkbox.putClientProperty(FlatClientProperties.SELECTED_STATE,
					FlatClientProperties.SELECTED_STATE_INDETERMINATE);
		}
	}

	private static Set<Integer> allWindLevelIndices(int levelCount) {
		return IntStream.range(0, levelCount).boxed().collect(Collectors.toUnmodifiableSet());
	}

	private static CustomizationAction showWeatherCustomizationDialog(Window owner, JPanel fields) {
		JDialog dialog = new JDialog(owner, trans.get("simedtdlg.title.customizeWeather"),
				JDialog.ModalityType.APPLICATION_MODAL);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		CustomizationAction[] action = { CustomizationAction.CANCEL };

		JButton cancel = new JButton(trans.get("dlg.but.cancel"));
		cancel.addActionListener(e -> dialog.dispose());
		JButton apply = new JButton(trans.get("simedtdlg.but.applyWeather"));
		apply.addActionListener(e -> {
			action[0] = CustomizationAction.APPLY;
			dialog.dispose();
		});
		JButton saveAndApply = new JButton(trans.get("simedtdlg.but.saveAndApplyWeather"));
		saveAndApply.addActionListener(e -> {
			action[0] = CustomizationAction.SAVE_AND_APPLY;
			dialog.dispose();
		});

		JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
		buttons.add(cancel);
		buttons.add(apply);
		buttons.add(saveAndApply);
		JPanel content = new JPanel(new BorderLayout(0, 16));
		content.setBorder(BorderFactory.createEmptyBorder(18, 18, 14, 18));
		content.add(fields, BorderLayout.CENTER);
		content.add(buttons, BorderLayout.SOUTH);
		dialog.setContentPane(content);
		dialog.getRootPane().setDefaultButton(apply);
		GUIUtil.installEscapeCloseButtonOperation(dialog, cancel);
		dialog.pack();
		dialog.setLocationRelativeTo(owner);
		dialog.setVisible(true);
		return action[0];
	}

	private static MultiLevelPinkNoiseWindModel copyWindModel(MultiLevelPinkNoiseWindModel source) {
		MultiLevelPinkNoiseWindModel copy = new MultiLevelPinkNoiseWindModel();
		replaceWindModel(copy, source);
		return copy;
	}

	private static void replaceWindModel(MultiLevelPinkNoiseWindModel target, MultiLevelPinkNoiseWindModel source) {
		target.clearLevels();
		target.setAltitudeReference(source.getAltitudeReference());
		for (MultiLevelPinkNoiseWindModel.LevelWindModel level : source.getLevels()) {
			target.addWindLevel(level.getAltitude(), level.getSpeed(), level.getDirection(),
					level.getStandardDeviation());
		}
	}

	static void setTurbulenceIntensity(MultiLevelPinkNoiseWindModel windModel, double turbulenceIntensity) {
		for (MultiLevelPinkNoiseWindModel.LevelWindModel level : windModel.getLevels()) {
			level.setTurbulenceIntensity(turbulenceIntensity);
		}
	}

	static List<Double> standardDeviations(MultiLevelPinkNoiseWindModel windModel) {
		return windModel.getLevels().stream()
				.map(MultiLevelPinkNoiseWindModel.LevelWindModel::getStandardDeviation)
				.toList();
	}

	static void restoreStandardDeviations(MultiLevelPinkNoiseWindModel windModel,
			List<Double> standardDeviations) {
		int count = Math.min(windModel.getLevels().size(), standardDeviations.size());
		for (int index = 0; index < count; index++) {
			windModel.getLevels().get(index).setStandardDeviation(standardDeviations.get(index));
		}
	}

	static OptionalDouble uniformTurbulenceIntensity(MultiLevelPinkNoiseWindModel windModel) {
		if (windModel.getLevels().isEmpty()) {
			return OptionalDouble.empty();
		}
		double first = windModel.getLevels().get(0).getTurbulenceIntensity();
		boolean uniform = windModel.getLevels().stream()
				.allMatch(level -> Math.abs(level.getTurbulenceIntensity() - first) < 1.0e-9);
		return uniform ? OptionalDouble.of(first) : OptionalDouble.empty();
	}

	private static void showMixedValue(JSpinner spinner) {
		((SpinnerEditor) spinner.getEditor()).getTextField().setText("—");
	}

	private static JSpinner unitSpinner(DoubleModel model) {
		JSpinner spinner = new JSpinner(model.getSpinnerModel());
		spinner.setEditor(new SpinnerEditor(spinner));
		return spinner;
	}

	private WeatherEdits editsFor(CurrentConditions conditions) {
		double turbulenceIntensity = savedTurbulenceIntensity != null
				? savedTurbulenceIntensity : turbulenceIntensityFor(conditions.windLayers());
		return new WeatherEdits(conditions.latitude(), conditions.longitude(), conditions.elevation(),
				conditions.temperature(), conditions.pressure(), conditions.relativeHumidity(),
				conditions.windLayers(), turbulenceIntensity);
	}

	static double turbulenceIntensityFor(List<CurrentConditions.WindLayer> windLayers) {
		return windLayers.stream()
				.filter(layer -> layer.speed() > 0)
				.findFirst()
				.map(layer -> layer.standardDeviation() / layer.speed())
				.orElse(0.0);
	}

	private static List<CurrentConditions.WindLayer> includedWindLayers(
			List<CurrentConditions.WindLayer> windLayers, Set<Integer> excludedIndices) {
		List<CurrentConditions.WindLayer> included = selectedWindLayers(windLayers, excludedIndices);
		return included.isEmpty() ? windLayers : included;
	}

	private static List<CurrentConditions.WindLayer> selectedWindLayers(
			List<CurrentConditions.WindLayer> windLayers, Set<Integer> excludedIndices) {
		return IntStream.range(0, windLayers.size())
				.filter(index -> !excludedIndices.contains(index))
				.mapToObj(windLayers::get)
				.toList();
	}

	private static WeatherEdits withIncludedWindLayers(WeatherEdits edits, Set<Integer> excludedIndices) {
		return new WeatherEdits(edits.latitude, edits.longitude, edits.elevation, edits.temperature, edits.pressure,
				edits.relativeHumidity, includedWindLayers(edits.windLayers, excludedIndices), edits.turbulenceIntensity);
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

		PinkNoiseWindModel averageWind = options.getAverageWindModel();
		boolean hasUniformTurbulenceOverride = selection.turbulence() && edits.turbulenceIntensity != null;
		double appliedTurbulenceIntensity = hasUniformTurbulenceOverride
				? edits.turbulenceIntensity : averageWind.getTurbulenceIntensity();
		if (hasUniformTurbulenceOverride && !selection.wind()) {
			averageWind.setTurbulenceIntensity(appliedTurbulenceIntensity);
			for (MultiLevelPinkNoiseWindModel.LevelWindModel level : options.getMultiLevelWindModel().getLevels()) {
				level.setTurbulenceIntensity(appliedTurbulenceIntensity);
			}
		}

		if (selection.wind()) {
			CurrentConditions.WindLayer surface = edits.windLayers.get(0);
			averageWind.setAverage(surface.speed());
			averageWind.setDirection(surface.direction());
			if (selection.turbulence() && edits.turbulenceIntensity == null) {
				averageWind.setStandardDeviation(surface.standardDeviation());
			} else {
				averageWind.setTurbulenceIntensity(appliedTurbulenceIntensity);
			}

			MultiLevelPinkNoiseWindModel windModel = options.getMultiLevelWindModel();
			windModel.clearLevels();
			windModel.setAltitudeReference(AltitudeReference.MSL);
			for (CurrentConditions.WindLayer layer : edits.windLayers) {
				double standardDeviation = selection.turbulence() && edits.turbulenceIntensity == null
						? layer.standardDeviation() : layer.speed() * appliedTurbulenceIntensity;
				windModel.addWindLevel(layer.altitude(), layer.speed(), layer.direction(), standardDeviation);
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
		CUSTOMIZE, CANCEL, OK, FORCE_REFRESH
	}

	private record WeatherCustomization(ApplySelection selection, WeatherEdits edits,
			Set<Integer> excludedWindLevelIndices, CustomizationAction action) {
		private WeatherCustomization {
			excludedWindLevelIndices = Set.copyOf(excludedWindLevelIndices);
		}
	}

	private enum CustomizationAction {
		CANCEL, APPLY, SAVE_AND_APPLY
	}

	private record WeatherEdits(double latitude, double longitude, double elevation, double temperature,
			double pressure, double relativeHumidity, List<CurrentConditions.WindLayer> windLayers,
			Double turbulenceIntensity) {
		private WeatherEdits {
			windLayers = List.copyOf(windLayers);
		}
	}

	private enum LocationSource {
		DEVICE, CONFIGURED, SELECTED
	}

	private record ApplySelection(boolean latitude, boolean longitude, boolean elevation, boolean temperature, boolean pressure,
			boolean humidity, boolean wind, boolean turbulence) {
		private static ApplySelection all() {
			return new ApplySelection(true, true, true, true, true, true, true, true);
		}

		private static ApplySelection from(WeatherCustomizationPreferences.FieldSettings settings) {
			return new ApplySelection(settings.latitude(), settings.longitude(), settings.elevation(),
					settings.temperature(), settings.pressure(), settings.humidity(), settings.wind(),
					settings.turbulence());
		}

		private WeatherCustomizationPreferences.FieldSettings toFieldSettings() {
			return new WeatherCustomizationPreferences.FieldSettings(latitude, longitude, elevation, temperature,
					pressure, humidity, wind, turbulence);
		}
	}

}
