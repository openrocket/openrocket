package info.openrocket.swing.gui.simulation.currentconditions;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import info.openrocket.core.arch.SystemInfo;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.startup.Application;
import info.openrocket.swing.gui.simulation.currentconditions.OpenMeteoClient.LocationSearchResult;
import info.openrocket.swing.gui.simulation.currentconditions.SavedPadRepository.SavedPad;
import info.openrocket.swing.gui.util.URLUtil;

/**
 * A small cross-platform location picker backed by OpenStreetMap tiles and Open-Meteo place search.
 */
public final class LocationPickerDialog {
	private static final Translator TRANS = Application.getTranslator();
	private LocationPickerDialog() {
	}

	public static DeviceLocation show(Window owner, DeviceLocation initial, DeviceLocation configured,
			boolean requestDeviceLocation) {
		JDialog dialog = new JDialog(owner, TRANS.get("simedtdlg.title.chooseWeatherLocation"),
				JDialog.ModalityType.APPLICATION_MODAL);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		MapPanel map = new MapPanel(initial.latitude(), initial.longitude());
		SavedPadRepository padRepository = new SavedPadRepository();
		Set<SwingWorker<?, ?>> dialogWorkers = ConcurrentHashMap.newKeySet();
		DeviceLocation[] selected = { initial };
		DeviceLocation[] result = { null };
		JLabel status = new JLabel();
		status.setVisible(false);
		JButton useLocation = new JButton(TRANS.get("simedtdlg.but.useWeatherLocation"));
		useLocation.setEnabled(!requestDeviceLocation);
		JSpinner latitudeSpinner = new JSpinner(new SpinnerNumberModel(initial.latitude(), -90.0, 90.0, 0.0001));
		JSpinner longitudeSpinner = new JSpinner(new SpinnerNumberModel(initial.longitude(), -180.0, 180.0, 0.0001));
		latitudeSpinner.setEditor(new JSpinner.NumberEditor(latitudeSpinner, "0.00000"));
		longitudeSpinner.setEditor(new JSpinner.NumberEditor(longitudeSpinner, "0.00000"));
		boolean[] updatingCoordinates = { false };

		map.setLocationListener((selectedLatitude, selectedLongitude) -> {
			selected[0] = new DeviceLocation(selectedLatitude, selectedLongitude, Double.NaN, Double.NaN,
					TRANS.get("simedtdlg.lbl.mapSelection"));
			status.setVisible(false);
			useLocation.setEnabled(true);
			updatingCoordinates[0] = true;
			latitudeSpinner.setValue(selectedLatitude);
			longitudeSpinner.setValue(selectedLongitude);
			updatingCoordinates[0] = false;
		});
		javax.swing.event.ChangeListener coordinateChange = e -> {
			if (updatingCoordinates[0]) {
				return;
			}
			double selectedLatitude = ((Number) latitudeSpinner.getValue()).doubleValue();
			double selectedLongitude = ((Number) longitudeSpinner.getValue()).doubleValue();
			selected[0] = new DeviceLocation(selectedLatitude, selectedLongitude, Double.NaN, Double.NaN,
					TRANS.get("simedtdlg.lbl.editedCoordinates"));
			map.setMarker(selectedLatitude, selectedLongitude, true, false);
			status.setVisible(false);
			useLocation.setEnabled(true);
		};
		latitudeSpinner.addChangeListener(coordinateChange);
		longitudeSpinner.addChangeListener(coordinateChange);

		JTextField search = new JTextField();
		JButton searchButton = new JButton(TRANS.get("simedtdlg.but.search"));
		Runnable searchAction = () -> searchLocations(dialog, map, search, searchButton, useLocation, status, selected,
				dialogWorkers);
		search.addActionListener(e -> searchAction.run());
		searchButton.addActionListener(e -> searchAction.run());
		JPanel searchBar = new JPanel(new BorderLayout(8, 0));
		JLabel searchLabel = new JLabel(TRANS.get("simedtdlg.lbl.searchPlace"));
		searchLabel.setLabelFor(search);
		searchBar.add(searchLabel, BorderLayout.WEST);
		searchBar.add(search, BorderLayout.CENTER);
		searchBar.add(searchButton, BorderLayout.EAST);

		JButton myLocation = new JButton(TRANS.get("simedtdlg.but.myLocation"));
		myLocation.addActionListener(e -> requestDeviceLocation(map, myLocation, useLocation, status, selected,
				dialogWorkers));
		JButton launchSite = new JButton(TRANS.get("simedtdlg.but.launchSite"));
		launchSite.addActionListener(e -> {
			map.setMarker(configured.latitude(), configured.longitude(), true, true);
			selected[0] = configured;
			status.setVisible(false);
			useLocation.setEnabled(true);
		});
		JPanel mapControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
		mapControls.add(myLocation);
		mapControls.add(launchSite);
		DefaultComboBoxModel<SavedPad> savedPadsModel = new DefaultComboBoxModel<>();
		JComboBox<SavedPad> savedPads = new JComboBox<>(savedPadsModel);
		savedPads.setPreferredSize(new Dimension(220, savedPads.getPreferredSize().height));
		Runnable refreshSavedPads = () -> {
			savedPadsModel.removeAllElements();
			for (SavedPad pad : padRepository.load()) {
				savedPadsModel.addElement(pad);
			}
			savedPads.setSelectedIndex(-1);
			savedPads.setEnabled(savedPadsModel.getSize() > 0);
		};
		refreshSavedPads.run();
		savedPads.addActionListener(e -> {
			if (!(savedPads.getSelectedItem() instanceof SavedPad pad)) {
				return;
			}
			DeviceLocation location = pad.location();
			selected[0] = location;
			map.setMarker(location.latitude(), location.longitude(), true, true);
			map.setZoom(12);
			updatingCoordinates[0] = true;
			latitudeSpinner.setValue(location.latitude());
			longitudeSpinner.setValue(location.longitude());
			updatingCoordinates[0] = false;
			status.setVisible(false);
			useLocation.setEnabled(true);
		});
		JButton savePad = new JButton(TRANS.get("simedtdlg.but.savePad"));
		savePad.addActionListener(e -> {
			String name = JOptionPane.showInputDialog(dialog, TRANS.get("simedtdlg.msg.padName"),
					TRANS.get("simedtdlg.title.savePad"), JOptionPane.PLAIN_MESSAGE);
			if (name == null || name.isBlank()) {
				return;
			}
			DeviceLocation location = selected[0];
			savePad.setEnabled(false);
			status.setText(TRANS.get("simedtdlg.lbl.resolvingTimezone"));
			status.setVisible(true);
			SwingWorker<DeviceLocation, Void> worker = new SwingWorker<>() {
				@Override
				protected DeviceLocation doInBackground() throws Exception {
					if (location.timezoneId() != null && !location.timezoneId().isBlank()) {
						return location;
					}
					ZoneId timezone = new OpenMeteoClient().resolveTimezone(location.latitude(), location.longitude());
					return location.withTimezone(timezone.getId());
				}

				@Override
				protected void done() {
					dialogWorkers.remove(this);
					savePad.setEnabled(true);
					try {
						DeviceLocation resolved = get();
						selected[0] = resolved;
						SavedPad saved = padRepository.save(name, resolved);
						refreshSavedPads.run();
						savedPads.setSelectedItem(saved);
						status.setText(MessageFormat.format(TRANS.get("simedtdlg.msg.padSaved"), saved.name()));
						status.setVisible(true);
					} catch (Exception exception) {
						status.setText(MessageFormat.format(TRANS.get("simedtdlg.msg.timezoneLookupFailedDetail"),
								rootMessage(exception)));
						status.setVisible(true);
					}
				}
			};
			dialogWorkers.add(worker);
			worker.execute();
		});
		JButton deletePad = new JButton(TRANS.get("simedtdlg.but.deletePad"));
		deletePad.addActionListener(e -> {
			if (!(savedPads.getSelectedItem() instanceof SavedPad pad)) {
				return;
			}
			int choice = JOptionPane.showConfirmDialog(dialog,
					MessageFormat.format(TRANS.get("simedtdlg.msg.deletePad"), pad.name()),
					TRANS.get("simedtdlg.title.deletePad"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if (choice == JOptionPane.YES_OPTION) {
				padRepository.delete(pad);
				refreshSavedPads.run();
			}
		});
		JPanel savedPadControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
		savedPadControls.add(new JLabel(TRANS.get("simedtdlg.lbl.savedPads")));
		savedPadControls.add(savedPads);
		savedPadControls.add(savePad);
		savedPadControls.add(deletePad);
		JPanel coordinateEditor = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
		JLabel latitudeLabel = new JLabel(TRANS.get("simedtdlg.lbl.Latitude"));
		latitudeLabel.setLabelFor(latitudeSpinner);
		coordinateEditor.add(latitudeLabel);
		coordinateEditor.add(latitudeSpinner);
		JLabel longitudeLabel = new JLabel(TRANS.get("simedtdlg.lbl.Longitude"));
		longitudeLabel.setLabelFor(longitudeSpinner);
		coordinateEditor.add(longitudeLabel);
		coordinateEditor.add(longitudeSpinner);

		JButton cancel = new JButton(TRANS.get("dlg.but.cancel"));
		cancel.addActionListener(e -> dialog.dispose());
		useLocation.addActionListener(e -> resolveTimezoneAndClose(dialog, selected, result, useLocation, status,
				dialogWorkers));
		JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		actions.add(cancel);
		actions.add(useLocation);

		JPanel footer = new JPanel(new BorderLayout(8, 6));
		JPanel locationControls = new JPanel(new GridLayout(0, 1, 0, 6));
		locationControls.add(mapControls);
		locationControls.add(savedPadControls);
		locationControls.add(coordinateEditor);
		footer.add(locationControls, BorderLayout.NORTH);
		JEditorPane attribution = new JEditorPane("text/html", TRANS.get("simedtdlg.lbl.mapAttribution"));
		attribution.setEditable(false);
		attribution.setFocusable(false);
		attribution.setHighlighter(null);
		attribution.setCursor(Cursor.getDefaultCursor());
		attribution.setOpaque(false);
		attribution.setBorder(null);
		attribution.addHyperlinkListener(event -> {
			if (event.getEventType() == javax.swing.event.HyperlinkEvent.EventType.ACTIVATED) {
				URLUtil.openWebpage(event.getURL().toString());
			}
		});
		footer.add(attribution, BorderLayout.SOUTH);

		JPanel actionRow = new JPanel(new BorderLayout());
		actionRow.add(status, BorderLayout.WEST);
		actionRow.add(actions, BorderLayout.EAST);
		JPanel bottom = new JPanel(new BorderLayout());
		bottom.add(footer, BorderLayout.CENTER);
		bottom.add(actionRow, BorderLayout.SOUTH);

		JPanel content = new JPanel(new BorderLayout(0, 8));
		content.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
		JPanel north = new JPanel(new BorderLayout(0, 6));
		JEditorPane privacy = new JEditorPane("text/html", TRANS.get("simedtdlg.msg.locationPrivacy"));
		privacy.setEditable(false);
		privacy.setFocusable(false);
		privacy.setHighlighter(null);
		privacy.setCursor(Cursor.getDefaultCursor());
		privacy.setOpaque(false);
		privacy.setBorder(null);
		privacy.addHyperlinkListener(event -> {
			if (event.getEventType() == javax.swing.event.HyperlinkEvent.EventType.ACTIVATED) {
				URLUtil.openWebpage(event.getURL().toString());
			}
		});
		north.add(privacy, BorderLayout.NORTH);
		north.add(searchBar, BorderLayout.SOUTH);
		content.add(north, BorderLayout.NORTH);
		content.add(map, BorderLayout.CENTER);
		content.add(bottom, BorderLayout.SOUTH);
		dialog.setContentPane(content);
		dialog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent event) {
				map.cancelLoads();
				dialogWorkers.forEach(worker -> worker.cancel(true));
				dialogWorkers.clear();
			}
		});
		dialog.getRootPane().setDefaultButton(useLocation);
		dialog.pack();
		dialog.setLocationRelativeTo(owner);
		if (requestDeviceLocation) {
			SwingUtilities.invokeLater(myLocation::doClick);
		}
		dialog.setVisible(true);
		return result[0];
	}

	private static void searchLocations(JDialog dialog, MapPanel map, JTextField search, JButton searchButton,
			JButton useLocation, JLabel status, DeviceLocation[] selected, Set<SwingWorker<?, ?>> workers) {
		String query = search.getText().trim();
		if (query.isEmpty()) {
			return;
		}
		searchButton.setEnabled(false);
		status.setText(MessageFormat.format(TRANS.get("simedtdlg.msg.searchingPlace"), query));
		status.setVisible(true);
		SwingWorker<List<LocationSearchResult>, Void> worker = new SwingWorker<>() {
			@Override
			protected List<LocationSearchResult> doInBackground() throws Exception {
				return new OpenMeteoClient().searchLocations(query);
			}

			@Override
			protected void done() {
				workers.remove(this);
				if (isCancelled() || !dialog.isDisplayable()) {
					return;
				}
				searchButton.setEnabled(true);
				try {
					List<LocationSearchResult> results = get();
					if (results.isEmpty()) {
						status.setText(TRANS.get("simedtdlg.msg.noLocationsFound"));
						status.setVisible(true);
						return;
					}
					LocationSearchResult choice = (LocationSearchResult) JOptionPane.showInputDialog(dialog,
							TRANS.get("simedtdlg.msg.chooseSearchResult"), TRANS.get("simedtdlg.title.searchResults"),
							JOptionPane.PLAIN_MESSAGE, null,
							results.toArray(), results.get(0));
					if (choice != null) {
						map.setMarker(choice.latitude(), choice.longitude(), true, true);
						selected[0] = new DeviceLocation(choice.latitude(), choice.longitude(), Double.NaN, Double.NaN,
								choice.toString(), choice.timezoneId());
						map.setZoom(10);
						status.setVisible(false);
						useLocation.setEnabled(true);
					}
				} catch (Exception e) {
					status.setText(MessageFormat.format(TRANS.get("simedtdlg.msg.locationSearchFailed"), rootMessage(e)));
					status.setVisible(true);
				}
			}
		};
		workers.add(worker);
		worker.execute();
	}

	private static void requestDeviceLocation(MapPanel map, JButton button, JButton useLocation, JLabel status,
			DeviceLocation[] selected, Set<SwingWorker<?, ?>> workers) {
		button.setEnabled(false);
		status.setText(TRANS.get("simedtdlg.msg.requestingDeviceLocation"));
		status.setVisible(true);
		SwingWorker<DeviceLocation, Void> worker = new SwingWorker<>() {
			@Override
			protected DeviceLocation doInBackground() throws Exception {
				return new SystemLocationProvider().locate();
			}

			@Override
			protected void done() {
				workers.remove(this);
				if (isCancelled() || !map.isDisplayable()) {
					return;
				}
				button.setEnabled(true);
				try {
					selected[0] = get();
					DeviceLocation deviceLocation = selected[0];
					map.setMarker(deviceLocation.latitude(), deviceLocation.longitude(), true, true);
					selected[0] = deviceLocation;
					map.setZoom(12);
					status.setVisible(false);
					useLocation.setEnabled(true);
				} catch (Exception e) {
					status.setText(MessageFormat.format(TRANS.get("simedtdlg.msg.deviceLocationUnavailable"),
							rootMessage(e)));
					status.setVisible(true);
				}
			}
		};
		workers.add(worker);
		worker.execute();
	}

	private static void resolveTimezoneAndClose(JDialog dialog, DeviceLocation[] selected, DeviceLocation[] result,
			JButton useLocation, JLabel status, Set<SwingWorker<?, ?>> workers) {
		DeviceLocation location = selected[0];
		if (location.timezoneId() != null && !location.timezoneId().isBlank()) {
			result[0] = location;
			dialog.dispose();
			return;
		}
		useLocation.setEnabled(false);
		status.setText(TRANS.get("simedtdlg.lbl.resolvingTimezone"));
		status.setVisible(true);
		SwingWorker<DeviceLocation, Void> worker = new SwingWorker<>() {
			@Override
			protected DeviceLocation doInBackground() throws Exception {
				ZoneId timezone = new OpenMeteoClient().resolveTimezone(location.latitude(), location.longitude());
				return location.withTimezone(timezone.getId());
			}

			@Override
			protected void done() {
				workers.remove(this);
				useLocation.setEnabled(true);
				try {
					result[0] = get();
					dialog.dispose();
				} catch (Exception exception) {
					status.setText(MessageFormat.format(TRANS.get("simedtdlg.msg.timezoneLookupFailedDetail"),
							rootMessage(exception)));
					status.setVisible(true);
				}
			}
		};
		workers.add(worker);
		worker.execute();
	}

	private static String rootMessage(Exception exception) {
		Throwable cause = exception;
		while (cause.getCause() != null) {
			cause = cause.getCause();
		}
		return cause.getMessage() == null ? cause.getClass().getSimpleName() : cause.getMessage();
	}

	private record Tile(int zoom, int x, int y) {
	}

	private static final class MapPanel extends JPanel {
		private static final int TILE_SIZE = 256;
		private static final int MIN_ZOOM = 2;
		private static final int MAX_ZOOM = 18;
		private static final Duration TILE_CACHE_MAX_AGE = Duration.ofDays(7);
		private static final Duration FAILED_TILE_RETRY = Duration.ofMinutes(1);
		private static final int MAX_MEMORY_TILES = 256;
		private static final int MAX_DISK_TILES = 5_000;
		private static final Path TILE_CACHE_DIRECTORY = SystemInfo.getUserApplicationDirectory().toPath()
				.resolve("cache").resolve("openstreetmap");
		private static final String TILE_URL_TEMPLATE = System.getProperty("openrocket.map.tileUrlTemplate",
				"https://tile.openstreetmap.org/%d/%d/%d.png");
		private static final Map<Tile, BufferedImage> TILE_CACHE = Collections.synchronizedMap(
				new LinkedHashMap<>(64, 0.75f, true) {
					@Override
					protected boolean removeEldestEntry(Map.Entry<Tile, BufferedImage> eldest) {
						return size() > MAX_MEMORY_TILES;
					}
				});
		private static final Set<Tile> LOADING_TILES = ConcurrentHashMap.newKeySet();
		private static final Map<Tile, Instant> FAILED_TILES = new ConcurrentHashMap<>();
		private static final AtomicInteger TILE_WRITES = new AtomicInteger();

		private double centerLatitude;
		private double centerLongitude;
		private double markerLatitude;
		private double markerLongitude;
		private int zoom = 5;
		private Point dragPoint;
		private Point pressPoint;
		private BiConsumer<Double, Double> locationListener;
		private final Set<SwingWorker<BufferedImage, Void>> workers = ConcurrentHashMap.newKeySet();

		private MapPanel(double latitude, double longitude) {
			setLayout(null);
			setFocusable(true);
			centerLatitude = clampLatitude(latitude);
			centerLongitude = normalizeLongitude(longitude);
			markerLatitude = centerLatitude;
			markerLongitude = centerLongitude;
			setPreferredSize(new Dimension(720, 400));
			setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
			setToolTipText(TRANS.get("simedtdlg.ttip.map"));
			getAccessibleContext().setAccessibleName(TRANS.get("simedtdlg.title.chooseWeatherLocation"));
			getAccessibleContext().setAccessibleDescription(TRANS.get("simedtdlg.ttip.map"));
			JButton zoomOut = new JButton("-");
			zoomOut.setCursor(Cursor.getDefaultCursor());
			zoomOut.setToolTipText(TRANS.get("simedtdlg.ttip.zoomOut"));
			zoomOut.addActionListener(e -> changeZoom(-1));
			add(zoomOut);
			JButton zoomIn = new JButton("+");
			zoomIn.setCursor(Cursor.getDefaultCursor());
			zoomIn.setToolTipText(TRANS.get("simedtdlg.ttip.zoomIn"));
			zoomIn.addActionListener(e -> changeZoom(1));
			add(zoomIn);

			MouseAdapter mouse = new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent e) {
					pressPoint = e.getPoint();
					dragPoint = e.getPoint();
				}

				@Override
				public void mouseDragged(MouseEvent e) {
					pan(e.getX() - dragPoint.x, e.getY() - dragPoint.y);
					dragPoint = e.getPoint();
				}

				@Override
				public void mouseReleased(MouseEvent e) {
					if (pressPoint != null && pressPoint.distance(e.getPoint()) < 5) {
						double[] location = screenToLocation(e.getX(), e.getY());
						setMarker(location[0], location[1], false, true);
					}
					pressPoint = null;
					dragPoint = null;
				}

				@Override
				public void mouseWheelMoved(MouseWheelEvent e) {
					changeZoom(e.getWheelRotation() < 0 ? 1 : -1);
				}
			};
			addMouseListener(mouse);
			addMouseMotionListener(mouse);
			addMouseWheelListener(mouse);
			bindKey("LEFT", "panLeft", () -> pan(32, 0));
			bindKey("RIGHT", "panRight", () -> pan(-32, 0));
			bindKey("UP", "panUp", () -> pan(0, 32));
			bindKey("DOWN", "panDown", () -> pan(0, -32));
			bindKey("PLUS", "zoomIn", () -> changeZoom(1));
			bindKey("EQUALS", "zoomInEquals", () -> changeZoom(1));
			bindKey("MINUS", "zoomOut", () -> changeZoom(-1));
			bindKey("ENTER", "selectCenter", () -> setMarker(centerLatitude, centerLongitude, false, true));
		}

		private void bindKey(String stroke, String name, Runnable action) {
			getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke(stroke), name);
			getActionMap().put(name, new AbstractAction() {
				@Override
				public void actionPerformed(java.awt.event.ActionEvent event) {
					action.run();
				}
			});
		}

		@Override
		public void doLayout() {
			int buttonWidth = 42;
			int buttonHeight = 34;
			int gap = 6;
			int margin = 10;
			int x = getWidth() - margin - buttonWidth * 2 - gap;
			int y = getHeight() - margin - buttonHeight;
			getComponent(0).setBounds(x, y, buttonWidth, buttonHeight);
			getComponent(1).setBounds(x + buttonWidth + gap, y, buttonWidth, buttonHeight);
		}

		private void setLocationListener(BiConsumer<Double, Double> listener) {
			locationListener = listener;
		}

		private void setMarker(double latitude, double longitude, boolean center, boolean notify) {
			markerLatitude = clampLatitude(latitude);
			markerLongitude = normalizeLongitude(longitude);
			if (center) {
				centerLatitude = markerLatitude;
				centerLongitude = markerLongitude;
			}
			if (notify && locationListener != null) {
				locationListener.accept(markerLatitude, markerLongitude);
			}
			repaint();
		}

		private void setZoom(int value) {
			zoom = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, value));
			repaint();
		}

		private void changeZoom(int amount) {
			setZoom(zoom + amount);
		}

		private void pan(double horizontal, double vertical) {
			double worldSize = worldSize();
			double x = longitudeToX(centerLongitude, worldSize) - horizontal;
			double y = latitudeToY(centerLatitude, worldSize) - vertical;
			centerLongitude = xToLongitude(x, worldSize);
			centerLatitude = yToLatitude(y, worldSize);
			repaint();
		}

		@Override
		protected void paintComponent(Graphics graphics) {
			super.paintComponent(graphics);
			Graphics2D g = (Graphics2D) graphics.create();
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			double worldSize = worldSize();
			double left = longitudeToX(centerLongitude, worldSize) - getWidth() / 2.0;
			double top = latitudeToY(centerLatitude, worldSize) - getHeight() / 2.0;
			int tileCount = 1 << zoom;
			int firstX = (int) Math.floor(left / TILE_SIZE);
			int lastX = (int) Math.floor((left + getWidth()) / TILE_SIZE);
			int firstY = Math.max(0, (int) Math.floor(top / TILE_SIZE));
			int lastY = Math.min(tileCount - 1, (int) Math.floor((top + getHeight()) / TILE_SIZE));
			for (int tileY = firstY; tileY <= lastY; tileY++) {
				for (int tileX = firstX; tileX <= lastX; tileX++) {
					int wrappedX = Math.floorMod(tileX, tileCount);
					Tile tile = new Tile(zoom, wrappedX, tileY);
					int x = (int) Math.round(tileX * TILE_SIZE - left);
					int y = (int) Math.round(tileY * TILE_SIZE - top);
					BufferedImage image = TILE_CACHE.get(tile);
					if (image == null) {
						g.setColor(new Color(225, 225, 225));
						g.fillRect(x, y, TILE_SIZE, TILE_SIZE);
						g.setColor(new Color(200, 200, 200));
						g.drawRect(x, y, TILE_SIZE, TILE_SIZE);
						loadTile(tile);
					} else {
						g.drawImage(image, x, y, null);
					}
				}
			}

			double markerX = longitudeToX(markerLongitude, worldSize);
			double centerX = longitudeToX(centerLongitude, worldSize);
			double deltaX = markerX - centerX;
			if (deltaX > worldSize / 2) deltaX -= worldSize;
			if (deltaX < -worldSize / 2) deltaX += worldSize;
			int x = (int) Math.round(getWidth() / 2.0 + deltaX);
			int y = (int) Math.round(latitudeToY(markerLatitude, worldSize) - top);
			g.setColor(new Color(220, 35, 45));
			g.fillOval(x - 9, y - 9, 18, 18);
			g.setColor(Color.WHITE);
			g.setStroke(new BasicStroke(3));
			g.drawOval(x - 9, y - 9, 18, 18);
			g.dispose();
		}

		private void loadTile(Tile tile) {
			Instant failedAt = FAILED_TILES.get(tile);
			if (failedAt != null && failedAt.isAfter(Instant.now().minus(FAILED_TILE_RETRY))) {
				return;
			}
			FAILED_TILES.remove(tile);
			if (!LOADING_TILES.add(tile)) {
				return;
			}
			SwingWorker<BufferedImage, Void> worker = new SwingWorker<>() {
				@Override
				protected BufferedImage doInBackground() throws Exception {
					Path cacheFile = TILE_CACHE_DIRECTORY.resolve(Integer.toString(tile.zoom()))
							.resolve(Integer.toString(tile.x())).resolve(tile.y() + ".png");
					Path etagFile = cacheFile.resolveSibling(cacheFile.getFileName() + ".etag");
					Path expiryFile = cacheFile.resolveSibling(cacheFile.getFileName() + ".expires");
					BufferedImage cached = readCachedTile(cacheFile, expiryFile, false);
					if (cached != null) {
						return cached;
					}
					URI uri = URI.create(String.format(Locale.ROOT, TILE_URL_TEMPLATE, tile.zoom(), tile.x(), tile.y()));
					HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
					connection.setConnectTimeout(8_000);
					connection.setReadTimeout(12_000);
					connection.setRequestProperty("User-Agent", "OpenRocket weather location picker (https://openrocket.info/)");
					if (Files.isRegularFile(cacheFile)) {
						connection.setIfModifiedSince(Files.getLastModifiedTime(cacheFile).toMillis());
					}
					if (Files.isRegularFile(etagFile)) {
						connection.setRequestProperty("If-None-Match", Files.readString(etagFile));
					}
					try {
						int status = connection.getResponseCode();
						if (status == HttpURLConnection.HTTP_NOT_MODIFIED) {
							Files.writeString(expiryFile, Long.toString(cacheExpiration(connection).toEpochMilli()));
							return readCachedTile(cacheFile, expiryFile, true);
						}
						if (status != HttpURLConnection.HTTP_OK) {
							return readCachedTile(cacheFile, expiryFile, true);
						}
						try (InputStream stream = connection.getInputStream()) {
							BufferedImage image = ImageIO.read(stream);
							if (image != null && !isNoStore(connection)) {
								Files.createDirectories(cacheFile.getParent());
								ImageIO.write(image, "png", cacheFile.toFile());
								Files.writeString(expiryFile,
										Long.toString(cacheExpiration(connection).toEpochMilli()));
									String etag = connection.getHeaderField("ETag");
									if (etag != null && !etag.isBlank()) {
										Files.writeString(etagFile, etag);
									}
									if (TILE_WRITES.incrementAndGet() % 100 == 0) {
										pruneDiskCache();
									}
							}
							return image;
						}
					} catch (Exception e) {
						BufferedImage stale = readCachedTile(cacheFile, expiryFile, true);
						if (stale != null) {
							return stale;
						}
						throw e;
					} finally {
						connection.disconnect();
					}
				}

				@Override
				protected void done() {
					workers.remove(this);
					LOADING_TILES.remove(tile);
					if (isCancelled()) {
						return;
					}
					try {
						BufferedImage image = get();
						if (image != null) {
							TILE_CACHE.put(tile, image);
						} else {
							FAILED_TILES.put(tile, Instant.now());
						}
					} catch (Exception ignored) {
						// Leave a neutral placeholder for unavailable tiles.
						FAILED_TILES.put(tile, Instant.now());
					}
					repaint();
				}
			};
			workers.add(worker);
			worker.execute();
		}

		private void cancelLoads() {
			for (SwingWorker<BufferedImage, Void> worker : workers) {
				worker.cancel(true);
			}
			workers.clear();
		}

		private static void pruneDiskCache() {
			try (var paths = Files.walk(TILE_CACHE_DIRECTORY)) {
				List<Path> files = paths.filter(path -> path.toString().endsWith(".png"))
						.sorted((left, right) -> {
							try {
								return Files.getLastModifiedTime(right).compareTo(Files.getLastModifiedTime(left));
							} catch (Exception ignored) {
								return 0;
							}
						}).toList();
				for (int index = MAX_DISK_TILES; index < files.size(); index++) {
					Path tile = files.get(index);
					Files.deleteIfExists(tile);
					Files.deleteIfExists(tile.resolveSibling(tile.getFileName() + ".etag"));
					Files.deleteIfExists(tile.resolveSibling(tile.getFileName() + ".expires"));
				}
			} catch (Exception ignored) {
				// Cache pruning is best effort and must never break the location picker.
			}
		}

		private static BufferedImage readCachedTile(Path cacheFile, Path expiryFile, boolean allowExpired) {
			try {
				if (!Files.isRegularFile(cacheFile)) {
					return null;
				}
				if (!allowExpired) {
					Instant expiration = Files.isRegularFile(expiryFile)
							? Instant.ofEpochMilli(Long.parseLong(Files.readString(expiryFile).trim()))
							: Files.getLastModifiedTime(cacheFile).toInstant().plus(TILE_CACHE_MAX_AGE);
					if (!Instant.now().isBefore(expiration)) {
						return null;
					}
				}
				return ImageIO.read(cacheFile.toFile());
			} catch (Exception ignored) {
				return null;
			}
		}

		private static boolean isNoStore(HttpURLConnection connection) {
			String cacheControl = connection.getHeaderField("Cache-Control");
			return cacheControl != null && cacheControl.toLowerCase(Locale.ROOT).contains("no-store");
		}

		private static Instant cacheExpiration(HttpURLConnection connection) {
			Instant now = Instant.now();
			String cacheControl = connection.getHeaderField("Cache-Control");
			if (cacheControl != null) {
				for (String directive : cacheControl.split(",")) {
					String value = directive.trim().toLowerCase(Locale.ROOT);
					if (value.startsWith("max-age=")) {
						try {
							return now.plusSeconds(Math.max(0, Long.parseLong(value.substring(8))));
						} catch (NumberFormatException ignored) {
							// Try the Expires header or the policy fallback below.
						}
					}
				}
			}
			long expires = connection.getExpiration();
			return expires > now.toEpochMilli() ? Instant.ofEpochMilli(expires) : now.plus(TILE_CACHE_MAX_AGE);
		}

		private double[] screenToLocation(double x, double y) {
			double worldSize = worldSize();
			double worldX = longitudeToX(centerLongitude, worldSize) - getWidth() / 2.0 + x;
			double worldY = latitudeToY(centerLatitude, worldSize) - getHeight() / 2.0 + y;
			return new double[] { yToLatitude(worldY, worldSize), xToLongitude(worldX, worldSize) };
		}

		private double worldSize() {
			return TILE_SIZE * (double) (1 << zoom);
		}

		private static double longitudeToX(double longitude, double worldSize) {
			return (normalizeLongitude(longitude) + 180.0) / 360.0 * worldSize;
		}

		private static double latitudeToY(double latitude, double worldSize) {
			double radians = Math.toRadians(clampLatitude(latitude));
			return (1.0 - Math.log(Math.tan(radians) + 1.0 / Math.cos(radians)) / Math.PI) / 2.0 * worldSize;
		}

		private static double xToLongitude(double x, double worldSize) {
			return normalizeLongitude(x / worldSize * 360.0 - 180.0);
		}

		private static double yToLatitude(double y, double worldSize) {
			double boundedY = Math.max(0, Math.min(worldSize, y));
			double n = Math.PI - 2.0 * Math.PI * boundedY / worldSize;
			return clampLatitude(Math.toDegrees(Math.atan(Math.sinh(n))));
		}

		private static double normalizeLongitude(double longitude) {
			double normalized = (longitude + 180.0) % 360.0;
			if (normalized < 0) normalized += 360.0;
			return normalized - 180.0;
		}

		private static double clampLatitude(double latitude) {
			return Math.max(-85.05112878, Math.min(85.05112878, latitude));
		}
	}
}
