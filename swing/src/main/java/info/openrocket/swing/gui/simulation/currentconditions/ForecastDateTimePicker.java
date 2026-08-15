package info.openrocket.swing.gui.simulation.currentconditions;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.text.DateFormatSymbols;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import info.openrocket.core.l10n.Translator;
import info.openrocket.core.startup.Application;

/** A compact calendar and hourly time picker for forecast selection. */
public final class ForecastDateTimePicker {
	private static final Translator TRANS = Application.getTranslator();

	private ForecastDateTimePicker() {
	}

	public static Selection show(Window owner, Instant initial, Instant minimum, Instant maximum, ZoneId zone) {
		ZonedDateTime initialLocal = (initial == null ? minimum : initial).atZone(zone);
		LocalDate minimumDate = minimum.atZone(zone).toLocalDate();
		LocalDate maximumDate = maximum.atZone(zone).toLocalDate();
		LocalDate[] selectedDate = { initialLocal.toLocalDate() };
		YearMonth[] displayedMonth = { YearMonth.from(selectedDate[0]) };
		Selection[] result = { null };

		JDialog dialog = new JDialog(owner, TRANS.get("simedtdlg.title.chooseForecastTime"),
				JDialog.ModalityType.APPLICATION_MODAL);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		JLabel monthLabel = new JLabel("", JLabel.CENTER);
		JButton previous = new JButton("‹");
		JButton next = new JButton("›");
		previous.getAccessibleContext().setAccessibleName("Previous Month");
		next.getAccessibleContext().setAccessibleName("Next Month");
		JPanel monthHeader = new JPanel(new BorderLayout(8, 0));
		monthHeader.add(previous, BorderLayout.WEST);
		monthHeader.add(monthLabel, BorderLayout.CENTER);
		monthHeader.add(next, BorderLayout.EAST);

		JPanel days = new JPanel(new GridLayout(0, 7, 4, 4));
		JComboBox<HourOption> time = new JComboBox<>();
		Runnable refreshTimes = () -> {
			Instant preferredInstant = time.getSelectedItem() instanceof HourOption option
					? option.instant() : initialLocal.toInstant();
			time.removeAllItems();
			HourOption preferred = null;
			for (Instant candidate : hourlyInstants(selectedDate[0], zone)) {
				if (candidate.isBefore(minimum) || candidate.isAfter(maximum)) {
					continue;
				}
				ZonedDateTime cursor = candidate.atZone(zone);
				HourOption option = new HourOption(candidate,
						cursor.format(DateTimeFormatter.ofPattern("h:00 a z", Locale.getDefault())));
				time.addItem(option);
				if (candidate.equals(preferredInstant)) {
					preferred = option;
				}
			}
			if (preferred != null) {
				time.setSelectedItem(preferred);
			}
		};

		Runnable[] refreshCalendar = new Runnable[1];
		refreshCalendar[0] = () -> {
			days.removeAll();
			Locale locale = Locale.getDefault();
			String[] weekdays = DateFormatSymbols.getInstance(locale).getShortWeekdays();
			for (int day = 1; day <= 7; day++) {
				int calendarDay = day == 7 ? 1 : day + 1;
				days.add(new JLabel(weekdays[calendarDay], JLabel.CENTER));
			}

			LocalDate first = displayedMonth[0].atDay(1);
			int leadingBlanks = first.getDayOfWeek().getValue() - 1;
			for (int i = 0; i < leadingBlanks; i++) {
				days.add(new JLabel());
			}
			for (int day = 1; day <= displayedMonth[0].lengthOfMonth(); day++) {
				LocalDate date = displayedMonth[0].atDay(day);
				JButton dayButton = new JButton(Integer.toString(day));
				dayButton.getAccessibleContext().setAccessibleName(
						date.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, uuuu", locale)));
				dayButton.setEnabled(!date.isBefore(minimumDate) && !date.isAfter(maximumDate));
				if (!dayButton.isEnabled()) {
					dayButton.putClientProperty("FlatLaf.style", "disabledTextColor: #777777");
				}
				if (date.equals(selectedDate[0])) {
					dayButton.setBorder(BorderFactory.createLineBorder(dayButton.getForeground(), 2));
				}
				dayButton.addActionListener(e -> {
					selectedDate[0] = date;
					refreshTimes.run();
					refreshCalendar[0].run();
				});
				days.add(dayButton);
			}
			monthLabel.setText(displayedMonth[0].getMonth().getDisplayName(TextStyle.FULL, locale)
					+ " " + displayedMonth[0].getYear());
			previous.setEnabled(displayedMonth[0].isAfter(YearMonth.from(minimumDate)));
			next.setEnabled(displayedMonth[0].isBefore(YearMonth.from(maximumDate)));
			days.revalidate();
			days.repaint();
		};

		previous.addActionListener(e -> {
			displayedMonth[0] = displayedMonth[0].minusMonths(1);
			refreshCalendar[0].run();
		});
		next.addActionListener(e -> {
			displayedMonth[0] = displayedMonth[0].plusMonths(1);
			refreshCalendar[0].run();
		});
		refreshTimes.run();
		refreshCalendar[0].run();

		JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JLabel timeLabel = new JLabel(TRANS.get("simedtdlg.lbl.forecastTime"));
		timeLabel.setLabelFor(time);
		timePanel.add(timeLabel);
		timePanel.add(time);
		timePanel.add(new JLabel(zone.getId()));

		JButton cancel = new JButton(TRANS.get("dlg.but.cancel"));
		cancel.addActionListener(e -> dialog.dispose());
		JButton now = new JButton(TRANS.get("simedtdlg.but.useCurrentTime"));
		now.addActionListener(e -> {
			result[0] = new Selection(true, null);
			dialog.dispose();
		});
		JButton use = new JButton(TRANS.get("simedtdlg.but.useForecastTime"));
		use.addActionListener(e -> {
			HourOption hour = (HourOption) time.getSelectedItem();
			if (hour != null) {
				result[0] = new Selection(false, hour.instant());
				dialog.dispose();
			}
		});
		JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
		rightActions.add(cancel);
		rightActions.add(use);
		JPanel actions = new JPanel(new BorderLayout());
		actions.add(now, BorderLayout.WEST);
		actions.add(rightActions, BorderLayout.EAST);

		JPanel content = new JPanel(new BorderLayout(0, 10));
		content.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
		content.add(monthHeader, BorderLayout.NORTH);
		content.add(days, BorderLayout.CENTER);
		JPanel bottom = new JPanel(new BorderLayout());
		bottom.add(timePanel, BorderLayout.NORTH);
		bottom.add(actions, BorderLayout.SOUTH);
		content.add(bottom, BorderLayout.SOUTH);
		dialog.setContentPane(content);
		dialog.getRootPane().setDefaultButton(use);
		dialog.pack();
		dialog.setLocationRelativeTo(owner);
		dialog.setVisible(true);
		return result[0];
	}

	static List<Instant> hourlyInstants(LocalDate date, ZoneId zone) {
		List<Instant> result = new ArrayList<>();
		ZonedDateTime cursor = date.atStartOfDay(zone);
		ZonedDateTime end = date.plusDays(1).atStartOfDay(zone);
		while (cursor.isBefore(end)) {
			result.add(cursor.toInstant());
			cursor = cursor.plusHours(1);
		}
		return result;
	}

	public record Selection(boolean now, Instant forecastAt) {
	}

	private record HourOption(Instant instant, String label) {
		@Override
		public String toString() {
			return label;
		}
	}
}
