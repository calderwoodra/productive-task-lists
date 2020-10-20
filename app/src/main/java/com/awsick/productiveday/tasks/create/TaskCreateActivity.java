package com.awsick.productiveday.tasks.create;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.awsick.productiveday.R;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public final class TaskCreateActivity extends AppCompatActivity {

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_create_task);
    getSupportFragmentManager()
        .beginTransaction()
        .replace(R.id.main_content, new TasksCreateFragment())
        .commit();
  }
}
