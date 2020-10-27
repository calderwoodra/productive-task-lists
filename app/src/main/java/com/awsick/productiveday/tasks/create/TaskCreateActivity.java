package com.awsick.productiveday.tasks.create;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.fragment.NavHostFragment;
import com.awsick.productiveday.R;
import com.awsick.productiveday.tasks.models.Task;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public final class TaskCreateActivity extends AppCompatActivity {

  static final String TASK_ID_KEY = "TASK_ID";

  public static Intent create(Context context) {
    return new Intent(context, TaskCreateActivity.class);
  }

  public static Intent create(Context context, Task task) {
    Intent intent = new Intent(context, TaskCreateActivity.class);
    intent.putExtra(TASK_ID_KEY, task.uid());
    return intent;
  }

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_create_task);
    Toolbar toolbar = findViewById(R.id.toolbar);
    NavHostFragment navHostFragment =
        (NavHostFragment)
            getSupportFragmentManager().findFragmentById(R.id.task_create_nav_container);
    navHostFragment
        .getNavController()
        .addOnDestinationChangedListener(
            (controller, destination, arguments) -> {
              if (destination.getId() == R.id.tasksCreateFragment) {
                toolbar.setNavigationIcon(R.drawable.ic_baseline_close_24);
                toolbar.setTitle(
                    getIntent().getIntExtra(TASK_ID_KEY, -1) == -1 ? "Create Task" : "Update Task");
                toolbar.setNavigationOnClickListener(view -> finish());
              } else if (destination.getId() == R.id.taskRepeatFragment) {
                toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24);
                toolbar.setTitle(destination.getLabel());
                toolbar.setNavigationOnClickListener(view -> controller.popBackStack());
              }
            });
  }
}
