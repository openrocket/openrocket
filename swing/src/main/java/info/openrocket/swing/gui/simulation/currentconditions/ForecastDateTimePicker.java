package info.openrocket.swing.gui.simulation.currentconditions;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.Locale;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

import info.openrocket.core.l10n.Translator;
import info.openrocket.core.startup.Application;

/** A compact calendar and hourly time picker for forecast selection. */
public final class ForecastDateTimePicker {
	private static final Translator TRANS = Application.getTranslator();

	private ForecastDateTimePicker() {
	}

	public static Selection show(Window owner, Instant initial, Instant minimum, Instant maximum, ZoneId zone) {
		Instant openedAt = Instant.now();
		Instant initialInstant = initial == null ? openedAt : initial;
		if (initialInstant.isBefore(minimum)) {
			initialInstant = minimum;
		} else if (initialInstant.isAfter(maximum)) {
			initialInstant = maximum;
		}
		ZonedDateTime initialLocal = initialInstant.atZone(zone);
		LocalDate minimumDate = minimum.atZone(zone).toLocalDate();
		LocalDate maximumDate = maximum.atZone(zone).toLocalDate();
		LocalDate today = openedAt.atZone(zone).toLocalDate();
		Instant nextForecastHour = openedAt.truncatedTo(ChronoUnit.HOURS).plus(1, ChronoUnit.HOURS);
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
			if (selectedDate[0].equals(today)) {
				time.addItem(HourOption.current(TRANS.get("simedtdlg.but.useCurrentTime")));
			}
			for (Instant candidate : hourlyInstants(selectedDate[0], zone)) {
				if (candidate.isBefore(minimum) || candidate.isAfter(maximum)) {
					continue;
				}
				if (selectedDate[0].equals(today) && candidate.isBefore(nextForecastHour)) {
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
			DayOfWeek firstDayOfWeek = WeekFields.of(locale).getFirstDayOfWeek();
			for (DayOfWeek weekday : orderedWeekdays(locale)) {
				days.add(new JLabel(weekday.getDisplayName(TextStyle.SHORT, locale), JLabel.CENTER));
			}

			LocalDate first = displayedMonth[0].atDay(1);
			int leadingBlanks = Math.floorMod(first.getDayOfWeek().getValue() - firstDayOfWeek.getValue(), 7);
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
				if (date.equals(today)) {
					Color todayColor = UIManager.getColor("Actions.Red");
					if (todayColor == null) {
						todayColor = new Color(0xD32F2F);
					}
					dayButton.setBorder(BorderFactory.createLineBorder(todayColor, 2));
				} else if (date.equals(selectedDate[0])) {
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
				result[0] = hour.current() ? new Selection(true, null) : new Selection(false, hour.instant());
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

	static List<DayOfWeek> orderedWeekdays(Locale locale) {
		DayOfWeek first = WeekFields.of(locale).getFirstDayOfWeek();
		List<DayOfWeek> weekdays = new ArrayList<>(7);
		for (int offset = 0; offset < 7; offset++) {
			weekdays.add(DayOfWeek.of((first.getValue() - 1 + offset) % 7 + 1));
		}
		return weekdays;
	}

	public record Selection(boolean now, Instant forecastAt) {
	}

	private record HourOption(Instant instant, String label) {
		private static HourOption current(String label) {
			return new HourOption(null, label);
		}

		private boolean current() {
			return instant == null;
		}

		@Override
		public String toString() {
			return label;
		}
	}
}
