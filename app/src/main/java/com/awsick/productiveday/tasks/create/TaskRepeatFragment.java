package com.awsick.productiveday.tasks.create;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.awsick.productiveday.R;
import com.awsick.productiveday.common.textwatchers.TwoDigitNonZeroTextWatcher;
import com.awsick.productiveday.common.uiutils.KeyboardUtils;
import com.awsick.productiveday.common.uiutils.NoopTextWatcher;
import com.awsick.productiveday.common.utils.StringUtils;
import com.awsick.productiveday.tasks.create.TaskRepeatViewModel.WeeklyFrequency.Dow;
import com.awsick.productiveday.tasks.models.TaskRepeatability.EndType;
import com.awsick.productiveday.tasks.models.TaskRepeatability.PeriodType;
import com.google.common.base.Strings;
import dagger.hilt.android.AndroidEntryPoint;
import java.util.Calendar;

@AndroidEntryPoint
public final class TaskRepeatFragment extends Fragment {

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_set_repeat, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View root, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(root, savedInstanceState);
    TaskRepeatViewModel viewModel = new ViewModelProvider(this).get(TaskRepeatViewModel.class);
    setupFrequency(root, viewModel);
    setupWeeklyFrequency(root, viewModel);
    setupMonthlyFrequency(root, viewModel);
    setupEnds(root, viewModel);

    // Save repeatability
    TasksCreateViewModel createViewModel =
        new ViewModelProvider(requireActivity()).get(TasksCreateViewModel.class);
    root.findViewById(R.id.repeat_save_cta)
        .setOnClickListener(
            view -> {
              createViewModel.setRepeatability(viewModel.getTaskRepeatability());
              Navigation.findNavController(requireView()).popBackStack();
            });
  }

  private void setupFrequency(View root, TaskRepeatViewModel viewModel) {
    // TODO(allen): Add a drop down to switch types
    EditText frequency = root.findViewById(R.id.repeat_frequency_count);
    TextWatcher update =
        new NoopTextWatcher() {
          @Override
          public void afterTextChanged(Editable s) {
            super.afterTextChanged(s);
            if (Strings.isNullOrEmpty(s.toString())) {
              return;
            }
            viewModel.setFrequency(Integer.parseInt(StringUtils.numbersOnly(s.toString())));
          }
        };

    frequency.addTextChangedListener(new TwoDigitNonZeroTextWatcher(frequency));
    frequency.addTextChangedListener(update);
    frequency.setText(Integer.toString(viewModel.getFrequency().getValue()));

    TextView type = root.findViewById(R.id.repeat_frequency_period);
    viewModel.getFrequencyTypeString().observe(getViewLifecycleOwner(), type::setText);
    setupTypeSelectorMenu(type, viewModel);
  }

  private void setupTypeSelectorMenu(View typeView, TaskRepeatViewModel viewModel) {
    PopupMenu popup = new PopupMenu(requireContext(), typeView, Gravity.START);
    popup.inflate(R.menu.menu_frequency_types);
    popup.setOnMenuItemClickListener(
        item -> {
          if (item.getItemId() == R.id.menu_frequency_daily) {
            viewModel.setPeriodType(PeriodType.DAILY);
          } else if (item.getItemId() == R.id.menu_frequency_weekly) {
            viewModel.setPeriodType(PeriodType.WEEKLY);
          } else if (item.getItemId() == R.id.menu_frequency_monthly) {
            viewModel.setPeriodType(PeriodType.MONTHLY);
          } else if (item.getItemId() == R.id.menu_frequency_yearly) {
            viewModel.setPeriodType(PeriodType.YEARLY);
          } else {
            throw new IllegalStateException("Unhandled frequency");
          }
          return true;
        });
    typeView.setOnClickListener(view -> popup.show());
  }

  private void setupWeeklyFrequency(View root, TaskRepeatViewModel viewModel) {
    View container = root.findViewById(R.id.repeat_weekly_container);
    viewModel
        .getPeriodType()
        .observe(
            getViewLifecycleOwner(),
            type -> container.setVisibility(type == PeriodType.WEEKLY ? View.VISIBLE : View.GONE));

    View sun = getDow(root, R.id.repeat_sunday, viewModel, Dow.Su);
    View m = getDow(root, R.id.repeat_monday, viewModel, Dow.M);
    View t = getDow(root, R.id.repeat_tuesday, viewModel, Dow.T);
    View w = getDow(root, R.id.repeat_wednesday, viewModel, Dow.W);
    View r = getDow(root, R.id.repeat_thursday, viewModel, Dow.R);
    View f = getDow(root, R.id.repeat_friday, viewModel, Dow.F);
    View sat = getDow(root, R.id.repeat_saturday, viewModel, Dow.Sa);
    viewModel
        .getWeeklyFrequency()
        .observe(
            getViewLifecycleOwner(),
            weekly -> {
              sun.setSelected(weekly.get(Dow.Su));
              m.setSelected(weekly.get(Dow.M));
              t.setSelected(weekly.get(Dow.T));
              w.setSelected(weekly.get(Dow.W));
              r.setSelected(weekly.get(Dow.R));
              f.setSelected(weekly.get(Dow.F));
              sat.setSelected(weekly.get(Dow.Sa));
            });
  }

  private static View getDow(View root, @IdRes int id, TaskRepeatViewModel viewModel, Dow dow) {
    View view = root.findViewById(id);
    view.setOnClickListener(v -> viewModel.flipDow(dow));
    return view;
  }

  private void setupMonthlyFrequency(View root, TaskRepeatViewModel viewModel) {
    View container = root.findViewById(R.id.repeat_monthly_container);
    viewModel
        .getPeriodType()
        .observe(
            getViewLifecycleOwner(),
            type -> container.setVisibility(type == PeriodType.MONTHLY ? View.VISIBLE : View.GONE));
    TextView selection = root.findViewById(R.id.repeat_monthly_selector);
    viewModel
        .getMonthlyFrequency()
        .observe(getViewLifecycleOwner(), monthly -> selection.setText(monthly.getText()));
  }

  private void setupEnds(View root, TaskRepeatViewModel viewModel) {
    // ends after
    View endsAfterContainer = root.findViewById(R.id.repeat_ends_after_times);
    RadioButton endsAfter = endsAfterContainer.findViewById(R.id.repeat_ends_after_times_rb);
    EditText nTimes = endsAfterContainer.findViewById(R.id.repeat_ends_after_times_input);
    nTimes.setText(Integer.toString(viewModel.getEndAfterN().getValue()));
    endsAfterContainer.setOnClickListener(
        view -> {
          viewModel.setEnds(EndType.AFTER);
          nTimes.requestFocus();
          nTimes.setSelection(nTimes.getText().length());
          KeyboardUtils.openKeyboardFrom(nTimes);
        });
    TextWatcher update =
        new NoopTextWatcher() {
          @Override
          public void afterTextChanged(Editable s) {
            super.afterTextChanged(s);
            if (Strings.isNullOrEmpty(s.toString())) {
              return;
            }
            viewModel.setEndsAfter(Integer.parseInt(StringUtils.numbersOnly(s.toString())));
          }
        };
    nTimes.addTextChangedListener(new TwoDigitNonZeroTextWatcher(nTimes));
    nTimes.addTextChangedListener(update);

    // never
    View neverContainer = root.findViewById(R.id.repeat_ends_never);
    RadioButton never = neverContainer.findViewById(R.id.repeat_ends_never_rb);
    neverContainer.setOnClickListener(
        view -> {
          nTimes.clearFocus();
          KeyboardUtils.hideKeyboardFrom(root);
          viewModel.setEnds(EndType.NEVER);
        });

    // ends on
    View endsOnContainer = root.findViewById(R.id.repeat_ends_on_date);
    RadioButton endsOn = endsOnContainer.findViewById(R.id.repeat_ends_on_date_rb);
    endsOnContainer.setOnClickListener(
        view -> {
          nTimes.clearFocus();
          KeyboardUtils.hideKeyboardFrom(root);
          viewModel.setEnds(EndType.ON);
          Calendar calendar = viewModel.getEndDateCalendar();
          new DatePickerDialog(
                  requireContext(),
                  (picker, year, month, dayOfMonth) ->
                      viewModel.setEndsOnDate(year, month, dayOfMonth),
                  calendar.get(Calendar.YEAR),
                  calendar.get(Calendar.MONTH),
                  calendar.get(Calendar.DAY_OF_MONTH))
              .show();
        });
    TextView date = endsOnContainer.findViewById(R.id.repeat_ends_on_date_selector);
    viewModel.getEndDate().observe(getViewLifecycleOwner(), date::setText);

    viewModel
        .getEndType()
        .observe(
            getViewLifecycleOwner(),
            type -> {
              never.setChecked(type == EndType.NEVER);
              endsOn.setChecked(type == EndType.ON);
              endsAfter.setChecked(type == EndType.AFTER);
            });
  }
}
