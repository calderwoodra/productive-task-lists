package com.awsick.productiveday.tasks.create;

import androidx.hilt.Assisted;
import androidx.hilt.lifecycle.ViewModelInject;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import com.awsick.productiveday.common.utils.DateUtils;
import com.awsick.productiveday.tasks.create.TaskRepeatViewModel.MonthlyFrequency.Type;
import com.awsick.productiveday.tasks.create.TaskRepeatViewModel.WeeklyFrequency.Dow;
import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.Calendar;

public final class TaskRepeatViewModel extends ViewModel {

  public enum FrequencyType {
    DAY,
    WEEK,
    MONTH,
    YEAR,
  }

  public enum EndType {
    NEVER,
    ON_DATE,
    AFTER_N_TIMES,
  }

  private final MutableLiveData<Integer> frequency = new MutableLiveData<>(1);
  private final MutableLiveData<FrequencyType> frequencyType =
      new MutableLiveData<>(FrequencyType.WEEK);
  private final FrequencyStringLiveData frequencyString =
      new FrequencyStringLiveData(frequency, frequencyType);

  private final MutableLiveData<WeeklyFrequency> weeklyFrequency;
  private final MutableLiveData<MonthlyFrequency> monthlyFrequency;

  private final MutableLiveData<EndType> endType = new MutableLiveData<>(EndType.NEVER);
  private final MutableLiveData<Integer> endAfterN = new MutableLiveData<>(1);
  private final MutableLiveData<Calendar> endDate;

  @ViewModelInject
  TaskRepeatViewModel(@Assisted SavedStateHandle savedState) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(savedState.get("start_time"));
    weeklyFrequency = new MutableLiveData<>(new WeeklyFrequency(calendar));
    monthlyFrequency = new MutableLiveData<>(new MonthlyFrequency(calendar, Type.DAY_OF_THE_MONTH));

    Calendar endDateCalendar = (Calendar) calendar.clone();
    endDateCalendar.add(Calendar.MONTH, 1);
    endDate = new MutableLiveData<>(endDateCalendar);
  }

  public LiveData<Integer> getFrequency() {
    return frequency;
  }

  public void setFrequency(int frequency) {
    this.frequency.setValue(frequency);
  }

  public LiveData<FrequencyType> getFrequencyType() {
    return frequencyType;
  }

  public void setFrequencyType(FrequencyType type) {
    frequencyType.setValue(type);
  }

  public LiveData<String> getFrequencyTypeString() {
    return frequencyString;
  }

  public LiveData<WeeklyFrequency> getWeeklyFrequency() {
    return weeklyFrequency;
  }

  public void flipDow(Dow dow) {
    weeklyFrequency.getValue().flip(dow);
    weeklyFrequency.setValue(weeklyFrequency.getValue());
  }

  public LiveData<MonthlyFrequency> getMonthlyFrequency() {
    return monthlyFrequency;
  }

  public LiveData<EndType> getEndType() {
    return endType;
  }

  public void setEnds(EndType type) {
    endType.setValue(type);
  }

  public LiveData<String> getEndDate() {
    return Transformations.map(endDate, c -> DateUtils.humanReadableDate(c.getTimeInMillis()));
  }

  public Calendar getEndDateCalendar() {
    return (Calendar) endDate.getValue().clone();
  }

  public void setEndsOnDate(int year, int month, int dayOfMonth) {
    Calendar calendar = endDate.getValue();
    calendar.set(year, month, dayOfMonth);
    endDate.setValue(calendar);
  }

  public LiveData<Integer> getEndAfterN() {
    return endAfterN;
  }

  public void setEndsAfter(int times) {
    endAfterN.setValue(times);
  }

  public void save() {
    // TODO(allen): save data to room db
  }

  public static final class WeeklyFrequency {

    public enum Dow {
      Su,
      M,
      T,
      W,
      R,
      F,
      Sa,
    }

    // S,M,T,W,R,F,S
    private final int[] dow = new int[7];

    public WeeklyFrequency(Calendar startDate) {
      Arrays.fill(dow, -1);
      flip(Dow.values()[startDate.get(Calendar.DAY_OF_WEEK) - 1]);
    }

    private void flip(Dow dow) {
      int position = dow.ordinal();
      this.dow[position] = -this.dow[position];
    }

    public boolean get(Dow dow) {
      return this.dow[dow.ordinal()] == 1;
    }
  }

  public static final class MonthlyFrequency {

    public enum Type {
      /** Ex. Monthly on day 25. */
      DAY_OF_THE_MONTH,
      /** Ex. Last Saturday of the month. */
      LAST_DOW_OF_THE_MONTH,
      /** Ex. 4th Saturday of the month. */
      NTH_DOW_OF_THE_MONTH,
    }

    private final Calendar startDate;
    private final Type type;

    public MonthlyFrequency(Calendar startDate, Type type) {
      this.startDate = (Calendar) startDate.clone();
      this.type = type;
    }

    public static ImmutableList<Type> getTypes(Calendar startDate) {
      // TODO(allen): Add support for more types
      return ImmutableList.of(Type.DAY_OF_THE_MONTH);
    }

    public String getText() {
      int day = startDate.get(Calendar.DAY_OF_MONTH);
      String suffix =
          day == 11 || day == 12 || day == 13
              ? "th"
              : day % 10 == 1 ? "st" : day % 10 == 2 ? "nd" : day % 10 == 3 ? "rd" : "th";

      switch (type) {
        case DAY_OF_THE_MONTH:
          return "Monthly on the " + day + suffix;
        case LAST_DOW_OF_THE_MONTH:
        case NTH_DOW_OF_THE_MONTH:
        default:
          throw new IllegalStateException("Unhandled: " + type);
      }
    }
  }

  private static final class FrequencyStringLiveData extends MediatorLiveData<String> {

    private int frequency;
    private FrequencyType type;

    FrequencyStringLiveData(
        MutableLiveData<Integer> frequency, MutableLiveData<FrequencyType> frequencyType) {
      this.frequency = frequency.getValue();
      type = frequencyType.getValue();
      onChanged();

      addSource(
          frequency,
          f -> {
            this.frequency = f;
            onChanged();
          });

      addSource(
          frequencyType,
          t -> {
            type = t;
            onChanged();
          });
    }

    private void onChanged() {
      String suffix = frequency == 1 ? "" : "s";
      switch (type) {
        case DAY:
          setValue("day" + suffix);
          break;
        case WEEK:
          setValue("week" + suffix);
          break;
        case MONTH:
          setValue("month" + suffix);
          break;
        case YEAR:
          setValue("year" + suffix);
          break;
      }
    }
  }
}
