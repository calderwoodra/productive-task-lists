package com.awsick.productiveday.tasks.create;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.awsick.productiveday.R;
import com.awsick.productiveday.tasks.models.Task;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public final class TaskCreateActivity extends AppCompatActivity {

  private static final String TASK_ID_KEY = "TASK_ID";

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
    getSupportFragmentManager()
        .beginTransaction()
        .replace(
            R.id.main_content, TasksCreateFragment.create(getIntent().getIntExtra(TASK_ID_KEY, -1)))
        .commit();
  }
}
