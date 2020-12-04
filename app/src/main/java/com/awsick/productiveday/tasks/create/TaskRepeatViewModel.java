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
import com.awsick.productiveday.tasks.models.TaskRepeatability;
import com.awsick.productiveday.tasks.models.TaskRepeatability.EndType;
import com.awsick.productiveday.tasks.models.TaskRepeatability.PeriodType;
import com.awsick.productiveday.tasks.models.TaskRepeatability.Weekly;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.Calendar;

public final class TaskRepeatViewModel extends ViewModel {

  private final long startTimeMillis;
  private final MutableLiveData<Integer> frequency = new MutableLiveData<>(1);
  private final MutableLiveData<PeriodType> periodType = new MutableLiveData<>(PeriodType.WEEKLY);
  private final FrequencyStringLiveData frequencyString =
      new FrequencyStringLiveData(frequency, periodType);

  private final MutableLiveData<WeeklyFrequency> weeklyFrequency;
  private final MutableLiveData<MonthlyFrequency> monthlyFrequency;

  private final MutableLiveData<EndType> endType = new MutableLiveData<>(EndType.NEVER);
  private final MutableLiveData<Integer> endAfterN = new MutableLiveData<>(1);
  private final MutableLiveData<Calendar> endDate;

  @ViewModelInject
  TaskRepeatViewModel(@Assisted SavedStateHandle savedState) {
    startTimeMillis = savedState.get("start_time");
    Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(startTimeMillis);

    weeklyFrequency = new MutableLiveData<>(new WeeklyFrequency(calendar));
    monthlyFrequency = new MutableLiveData<>(new MonthlyFrequency(calendar, Type.DAY_OF_THE_MONTH));

    Calendar endDateCalendar = (Calendar) calendar.clone();
    endDateCalendar.add(Calendar.MONTH, 1);
    endDate = new MutableLiveData<>(endDateCalendar);
  }

  public void initializeRepeatability(Optional<TaskRepeatability> taskRepeatability) {
    if (taskRepeatability == null || !taskRepeatability.isPresent()) {
      return;
    }

    TaskRepeatability repeatability = taskRepeatability.get();
    frequency.setValue(repeatability.frequency());
    periodType.setValue(repeatability.periodType());
    switch (repeatability.periodType()) {
      case WEEKLY:
        weeklyFrequency.setValue(new WeeklyFrequency(repeatability.weekly().get()));
        break;
      case MONTHLY:
        monthlyFrequency.setValue(new MonthlyFrequency(repeatability.monthly().get()));
        break;
      case DAILY:
      case YEARLY:
        break;
    }

    endType.setValue(repeatability.endType());
    endAfterN.setValue(repeatability.endAfterNTimes().or(1));
    if (repeatability.endOnTimeMillis().isPresent()) {
      endDate.getValue().setTimeInMillis(repeatability.endOnTimeMillis().get());
    }
  }

  public LiveData<Integer> getFrequency() {
    return frequency;
  }

  public void setFrequency(int frequency) {
    this.frequency.setValue(frequency);
  }

  public LiveData<PeriodType> getPeriodType() {
    return periodType;
  }

  public void setPeriodType(PeriodType type) {
    periodType.setValue(type);
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

  public TaskRepeatability getTaskRepeatability() {
    TaskRepeatability.Builder builder = TaskRepeatability.builder();
    builder.setFrequency(frequency.getValue());
    builder.setFirstReminder(startTimeMillis);
    builder.setPeriodType(periodType.getValue());
    switch (periodType.getValue()) {
      case WEEKLY:
        builder.setWeekly(Optional.of(weeklyFrequency.getValue().toPojo()));
        break;
      case MONTHLY:
        builder.setMonthly(Optional.of(monthlyFrequency.getValue().getDayOfMonth()));
        break;
      case DAILY:
      case YEARLY:
        // No-op
        break;
    }

    builder.setEndType(endType.getValue());
    switch (endType.getValue()) {
      case NEVER:
        // No-op
        break;
      case ON:
        builder.setEndOnTimeMillis(Optional.of(endDate.getValue().getTimeInMillis()));
        break;
      case AFTER:
        builder.setEndAfterNTimes(Optional.of(endAfterN.getValue()));
        break;
    }
    return builder.build();
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

    public WeeklyFrequency(TaskRepeatability.Weekly weekly) {
      Arrays.fill(dow, -1);
      if (weekly.monday()) {
        flip(Dow.M);
      }
      if (weekly.tuesday()) {
        flip(Dow.T);
      }
      if (weekly.wednesday()) {
        flip(Dow.W);
      }
      if (weekly.thursday()) {
        flip(Dow.R);
      }
      if (weekly.friday()) {
        flip(Dow.F);
      }
      if (weekly.saturday()) {
        flip(Dow.Sa);
      }
      if (weekly.sunday()) {
        flip(Dow.Su);
      }
    }

    private void flip(Dow dow) {
      int position = dow.ordinal();
      this.dow[position] = -this.dow[position];
    }

    public boolean get(Dow dow) {
      return this.dow[dow.ordinal()] == 1;
    }

    public Weekly toPojo() {
      return TaskRepeatability.Weekly.builder()
          .monday(get(Dow.M))
          .tuesday(get(Dow.T))
          .wednesday(get(Dow.W))
          .thursday(get(Dow.R))
          .friday(get(Dow.F))
          .saturday(get(Dow.Sa))
          .sunday(get(Dow.Su))
          .build();
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

    public MonthlyFrequency(Integer integer) {
      Calendar calendar = Calendar.getInstance();
      calendar.setTimeInMillis(System.currentTimeMillis());
      calendar.set(Calendar.DAY_OF_MONTH, integer);
      startDate = calendar;
      type = Type.DAY_OF_THE_MONTH;
    }

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

    public Integer getDayOfMonth() {
      return startDate.get(Calendar.DAY_OF_MONTH);
    }
  }

  private static final class FrequencyStringLiveData extends MediatorLiveData<String> {

    private int frequency;
    private PeriodType type;

    FrequencyStringLiveData(
        MutableLiveData<Integer> frequency, MutableLiveData<PeriodType> frequencyType) {
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
        case DAILY:
          setValue("day" + suffix);
          break;
        case WEEKLY:
          setValue("week" + suffix);
          break;
        case MONTHLY:
          setValue("month" + suffix);
          break;
        case YEARLY:
          setValue("year" + suffix);
          break;
      }
    }
  }
}
