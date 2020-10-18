package com.awsick.productiveday;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import com.awsick.productiveday.tasks.repo.TasksRepo;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import dagger.hilt.android.AndroidEntryPoint;
import javax.inject.Inject;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

  @Inject TasksRepo tasksRepo;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    setUpNavigation();
  }

  public void setUpNavigation() {
    BottomNavigationView bottomNavigationView = findViewById(R.id.main_bottom_nav);
    NavHostFragment navHostFragment =
        (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.main_nav_host);
    NavigationUI.setupWithNavController(bottomNavigationView, navHostFragment.getNavController());
  }
}
