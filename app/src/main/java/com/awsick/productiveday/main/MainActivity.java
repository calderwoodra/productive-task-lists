package com.awsick.productiveday.main;

import android.os.Bundle;
import android.view.View.OnClickListener;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import com.awsick.productiveday.R;
import com.awsick.productiveday.common.uiutils.FragmentUtils.FragmentUtilListener;
import com.awsick.productiveday.tasks.repo.TasksRepo;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import dagger.hilt.android.AndroidEntryPoint;
import javax.inject.Inject;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity implements FragmentUtilListener {

  @Inject TasksRepo tasksRepo;

  private MainParent parent;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    setUpNavigation();
    parent = new MainParent(findViewById(R.id.toolbar), findViewById(R.id.fab));
  }

  public void setUpNavigation() {
    BottomNavigationView bottomNavigationView = findViewById(R.id.main_bottom_nav);
    NavHostFragment navHostFragment =
        (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.main_nav_host);
    NavigationUI.setupWithNavController(bottomNavigationView, navHostFragment.getNavController());
  }

  @Nullable
  @Override
  public <T> T getImpl(Class<T> callbackInterface) {
    if (callbackInterface.isInstance(parent)) {
      return (T) parent;
    }
    return null;
  }

  private static final class MainParent implements MainParentContainer {

    private final Toolbar toolbar;
    private final FloatingActionButton fab;

    private MainParent(Toolbar toolbar, FloatingActionButton fab) {
      this.toolbar = toolbar;
      this.fab = fab;
    }

    @Override
    public void setToolbarTitle(String title) {
      toolbar.setTitle(title);
    }

    @Override
    public void setFabOcl(OnClickListener ocl) {
      fab.setOnClickListener(ocl);
    }
  }
}
