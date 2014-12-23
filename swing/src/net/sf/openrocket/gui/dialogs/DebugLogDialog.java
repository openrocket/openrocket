package net.sf.openrocket.gui.dialogs;

import java.awt.Color;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.gui.adaptors.Column;
import net.sf.openrocket.gui.adaptors.ColumnTable;
import net.sf.openrocket.gui.adaptors.ColumnTableModel;
import net.sf.openrocket.gui.components.SelectableLabel;
import net.sf.openrocket.gui.util.GUIUtil;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.logging.DelegatorLogger;
import net.sf.openrocket.logging.LogHelper;
import net.sf.openrocket.logging.LogLevel;
import net.sf.openrocket.logging.LogLevelBufferLogger;
import net.sf.openrocket.logging.LogLine;
import net.sf.openrocket.logging.LoggingSystemSetup;
import net.sf.openrocket.logging.Markers;
import net.sf.openrocket.logging.StackTraceWriter;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.NumericComparator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DebugLogDialog extends JDialog {
	private static final Logger log = LoggerFactory.getLogger(DebugLogDialog.class);
	
	private static final int POLL_TIME = 250;
	private static final String STACK_TRACE_MARK = "\uFF01";
	private static final Translator trans = Application.getTranslator();
	
	private static final EnumMap<LogLevel, Color> backgroundColors = new EnumMap<LogLevel, Color>(LogLevel.class);
	static {
		for (LogLevel l : LogLevel.values()) {
			// Just to ensure every level has a bg color
			backgroundColors.put(l, Color.ORANGE);
		}
		final int hi = 255;
		final int lo = 150;
		backgroundColors.put(LogLevel.ERROR, new Color(hi, lo, lo));
		backgroundColors.put(LogLevel.WARN, new Color(hi, (hi + lo) / 2, lo));
		backgroundColors.put(LogLevel.USER, new Color(lo, lo, hi));
		backgroundColors.put(LogLevel.INFO, new Color(hi, hi, lo));
		backgroundColors.put(LogLevel.DEBUG, new Color(lo, hi, lo));
		backgroundColors.put(LogLevel.VBOSE, new Color(lo, hi, (hi + lo) / 2));
	}
	
	/** Buffer containing the log lines displayed */
	private final List<LogLine> buffer = new ArrayList<LogLine>();
	
	/** Queue of log lines to be added to the displayed buffer */
	private final Queue<LogLine> queue = new ConcurrentLinkedQueue<LogLine>();
	
	private final DelegatorLogger delegator;
	private final LogListener logListener;
	
	private final EnumMap<LogLevel, JCheckBox> filterButtons = new EnumMap<LogLevel, JCheckBox>(LogLevel.class);
	private final JCheckBox followBox;
	private final Timer timer;
	
	
	private final JTable table;
	private final ColumnTableModel model;
	private final TableRowSorter<TableModel> sorter;
	
	private final SelectableLabel numberLabel;
	private final SelectableLabel timeLabel;
	private final SelectableLabel levelLabel;
	private final SelectableLabel locationLabel;
	private final SelectableLabel messageLabel;
	private final JTextArea stackTraceLabel;
	
	public DebugLogDialog(Window parent) {
		//// OpenRocket debug log
		super(parent, trans.get("debuglogdlg.OpenRocketdebuglog"));
		
		LogHelper applicationLog = LoggingSystemSetup.getInstance();
		if (applicationLog instanceof DelegatorLogger) {
			log.info("Adding log listener");
			delegator = (DelegatorLogger) applicationLog;
			logListener = new LogListener();
			delegator.addLogger(logListener);
		} else {
			log.warn("Application log is not a DelegatorLogger");
			delegator = null;
			logListener = null;
		}
		
		// Fetch old log lines
		LogLevelBufferLogger bufferLogger = LoggingSystemSetup.getBufferLogger();
		if (bufferLogger != null) {
			buffer.addAll(bufferLogger.getLogs());
		} else {
			log.warn("Application does not have a log buffer");
		}
		
		
		// Create the UI
		JPanel mainPanel = new JPanel(new MigLayout("fill"));
		this.add(mainPanel);
		
		
		JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		split.setDividerLocation(0.7);
		mainPanel.add(split, "grow");
		
		// Top panel
		JPanel panel = new JPanel(new MigLayout("fill"));
		split.add(panel);
		
		//// Display log lines:
		panel.add(new JLabel(trans.get("debuglogdlg.Displayloglines")), "gapright para, split");
		for (LogLevel l : LogLevel.values()) {
			JCheckBox box = new JCheckBox(l.toString());
			// By default display DEBUG and above
			box.setSelected(l.atLeast(LogLevel.DEBUG));
			box.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					sorter.setRowFilter(new LogFilter());
				}
			});
			panel.add(box, "gapright unrel");
			filterButtons.put(l, box);
		}
		
		//// Follow
		followBox = new JCheckBox(trans.get("debuglogdlg.Follow"));
		followBox.setSelected(true);
		panel.add(followBox, "skip, gapright para, right");
		
		//// Clear button
		JButton clear = new JButton(trans.get("debuglogdlg.but.clear"));
		clear.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				log.info(Markers.USER_MARKER, "Clearing log buffer");
				buffer.clear();
				queue.clear();
				model.fireTableDataChanged();
			}
		});
		panel.add(clear, "right, wrap");
		
		
		
		// Create the table model
		model = new ColumnTableModel(
				
				new Column("#") {
					@Override
					public Object getValueAt(int row) {
						return buffer.get(row).getLogCount();
					}
					
					@Override
					public int getDefaultWidth() {
						return 60;
					}
				},
				//// Time
				new Column(trans.get("debuglogdlg.col.Time")) {
					@Override
					public Object getValueAt(int row) {
						return String.format("%.3f", buffer.get(row).getTimestamp() / 1000.0);
					}
					
					@Override
					public int getDefaultWidth() {
						return 60;
					}
				},
				//// Level
				new Column(trans.get("debuglogdlg.col.Level")) {
					@Override
					public Object getValueAt(int row) {
						return buffer.get(row).getLevel();
					}
					
					@Override
					public int getDefaultWidth() {
						return 60;
					}
				},
				new Column("") {
					@Override
					public Object getValueAt(int row) {
						if (buffer.get(row).getCause() != null) {
							return STACK_TRACE_MARK;
						} else {
							return "";
						}
					}
					
					@Override
					public int getExactWidth() {
						return 16;
					}
				},
				//// Location
				new Column(trans.get("debuglogdlg.col.Location")) {
					@Override
					public Object getValueAt(int row) {
						String e = buffer.get(row).getLocation();
						return e;
					}
					
					@Override
					public int getDefaultWidth() {
						return 200;
					}
				},
				//// Message
				new Column(trans.get("debuglogdlg.col.Message")) {
					@Override
					public Object getValueAt(int row) {
						return buffer.get(row).getMessage();
					}
					
					@Override
					public int getDefaultWidth() {
						return 580;
					}
				}
				
				) {
					@Override
					public int getRowCount() {
						return buffer.size();
					}
				};
		
		table = new ColumnTable(model);
		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		table.setSelectionBackground(Color.LIGHT_GRAY);
		table.setSelectionForeground(Color.BLACK);
		model.setColumnWidths(table.getColumnModel());
		table.setDefaultRenderer(Object.class, new Renderer());
		
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				int row = table.getSelectedRow();
				if (row >= 0) {
					row = sorter.convertRowIndexToModel(row);
				}
				updateSelected(row);
			}
		});
		
		sorter = new TableRowSorter<TableModel>(model);
		sorter.setComparator(0, NumericComparator.INSTANCE);
		sorter.setComparator(1, NumericComparator.INSTANCE);
		sorter.setComparator(4, new LocationComparator());
		table.setRowSorter(sorter);
		sorter.setRowFilter(new LogFilter());
		
		
		panel.add(new JScrollPane(table), "span, grow, width " +
				(Toolkit.getDefaultToolkit().getScreenSize().width * 8 / 10) +
				"px, height 400px");
		
		
		panel = new JPanel(new MigLayout("fill"));
		split.add(panel);
		
		//// Log line number:
		panel.add(new JLabel(trans.get("debuglogdlg.lbl.Loglinenbr")), "split, gapright rel");
		numberLabel = new SelectableLabel();
		panel.add(numberLabel, "width 70lp, gapright para");
		
		//// Time:
		panel.add(new JLabel(trans.get("debuglogdlg.lbl.Time")), "split, gapright rel");
		timeLabel = new SelectableLabel();
		panel.add(timeLabel, "width 70lp, gapright para");
		
		//// Level:
		panel.add(new JLabel(trans.get("debuglogdlg.lbl.Level")), "split, gapright rel");
		levelLabel = new SelectableLabel();
		panel.add(levelLabel, "width 70lp, gapright para");
		
		//// Location:
		panel.add(new JLabel(trans.get("debuglogdlg.lbl.Location")), "split, gapright rel");
		locationLabel = new SelectableLabel();
		panel.add(locationLabel, "growx, wrap unrel");
		
		//// Log message:
		panel.add(new JLabel(trans.get("debuglogdlg.lbl.Logmessage")), "split, gapright rel");
		messageLabel = new SelectableLabel();
		panel.add(messageLabel, "growx, wrap para");
		
		//// Stack trace:
		panel.add(new JLabel(trans.get("debuglogdlg.lbl.Stacktrace")), "wrap rel");
		stackTraceLabel = new JTextArea(8, 80);
		stackTraceLabel.setEditable(false);
		GUIUtil.changeFontSize(stackTraceLabel, -2);
		panel.add(new JScrollPane(stackTraceLabel), "grow");
		
		
		//Close button
		JButton close = new JButton(trans.get("dlg.but.close"));
		close.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				DebugLogDialog.this.dispose();
			}
		});
		mainPanel.add(close, "newline para, right, tag ok");
		
		
		// Use timer to purge the queue so as not to overwhelm the EDT with events
		timer = new Timer(POLL_TIME, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				purgeQueue();
			}
		});
		timer.setRepeats(true);
		timer.start();
		
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				log.info(Markers.USER_MARKER, "Closing debug log dialog");
				timer.stop();
				if (delegator != null) {
					log.info("Removing log listener");
					delegator.removeLogger(logListener);
				}
			}
		});
		
		GUIUtil.setDisposableDialogOptions(this, close);
		followBox.requestFocus();
	}
	
	private void updateSelected(int row) {
		if (row < 0) {
			
			numberLabel.setText("");
			timeLabel.setText("");
			levelLabel.setText("");
			locationLabel.setText("");
			messageLabel.setText("");
			stackTraceLabel.setText("");
			
		} else {
			
			LogLine line = buffer.get(row);
			numberLabel.setText("" + line.getLogCount());
			timeLabel.setText(String.format("%.3f s", line.getTimestamp() / 1000.0));
			levelLabel.setText(line.getLevel().toString());
			String e = line.getLocation();
			locationLabel.setText(e);
			messageLabel.setText(line.getMessage());
			Throwable t = line.getCause();
			if (t != null) {
				StackTraceWriter stw = new StackTraceWriter();
				PrintWriter pw = new PrintWriter(stw);
				t.printStackTrace(pw);
				pw.flush();
				stackTraceLabel.setText(stw.toString());
				stackTraceLabel.setCaretPosition(0);
			} else {
				stackTraceLabel.setText("");
			}
			
		}
	}
	
	
	/**
	 * Check whether a row signifies a number of missing rows.  This check is "heuristic"
	 * and checks whether the timestamp is zero and the message starts with "---".
	 */
	private boolean isExcludedRow(int row) {
		LogLine line = buffer.get(row);
		return (line.getTimestamp() == 0) && (line.getMessage().startsWith("---"));
	}
	
	
	/**
	 * Purge the queue of incoming log lines.  This is called periodically from the EDT, and
	 * it adds any lines in the queue to the buffer, and fires a table event.
	 */
	private void purgeQueue() {
		int start = buffer.size();
		
		LogLine line;
		while ((line = queue.poll()) != null) {
			buffer.add(line);
		}
		
		int end = buffer.size() - 1;
		if (end >= start) {
			model.fireTableRowsInserted(start, end);
			if (followBox.isSelected()) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						Rectangle rect = table.getCellRect(1000000000, 1, true);
						table.scrollRectToVisible(rect);
					}
				});
			}
		}
	}
	
	
	/**
	 * A logger that adds log lines to the queue.  This method may be called from any
	 * thread, and therefore must be thread-safe.
	 */
	private class LogListener extends LogHelper {
		@Override
		public void log(LogLine line) {
			queue.add(line);
		}
	}
	
	private class LogFilter extends RowFilter<TableModel, Integer> {
		
		@Override
		public boolean include(RowFilter.Entry<? extends TableModel, ? extends Integer> entry) {
			int index = entry.getIdentifier();
			LogLine line = buffer.get(index);
			return filterButtons.get(line.getLevel()).isSelected();
		}
		
	}
	
	
	private class Renderer extends JLabel implements TableCellRenderer {
		@Override
		public Component getTableCellRendererComponent(JTable table1, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
			Color fg, bg;
			
			row = sorter.convertRowIndexToModel(row);
			
			if (STACK_TRACE_MARK.equals(value)) {
				fg = Color.RED;
			} else {
				fg = table1.getForeground();
			}
			bg = backgroundColors.get(buffer.get(row).getLevel());
			
			if (isSelected) {
				bg = bg.darker();
			} else if (isExcludedRow(row)) {
				bg = bg.brighter();
			}
			
			this.setForeground(fg);
			this.setBackground(bg);
			
			this.setOpaque(true);
			this.setText(String.valueOf(value));
			
			return this;
		}
	}
	
	
	private class LocationComparator implements Comparator<Object> {
		private final Pattern splitPattern = Pattern.compile("^\\(([^:]*+):([0-9]++).*\\)$");
		
		@Override
		public int compare(Object o1, Object o2) {
			String s1 = o1.toString();
			String s2 = o2.toString();
			
			Matcher m1 = splitPattern.matcher(s1);
			Matcher m2 = splitPattern.matcher(s2);
			
			if (m1.matches() && m2.matches()) {
				String class1 = m1.group(1);
				String pos1 = m1.group(2);
				String class2 = m2.group(1);
				String pos2 = m2.group(2);
				
				if (class1.equals(class2)) {
					return NumericComparator.INSTANCE.compare(pos1, pos2);
				} else {
					return class1.compareTo(class2);
				}
			}
			
			return s1.compareTo(s2);
		}
		
	}
	
}
