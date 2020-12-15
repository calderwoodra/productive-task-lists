package com.awsick.productiveday.main;

import android.os.Bundle;
import android.view.View.OnClickListener;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;
import com.awsick.productiveday.R;
import com.awsick.productiveday.common.uiutils.FragmentUtils.FragmentUtilListener;
import com.awsick.productiveday.directories.DirectoriesHostFragment;
import com.awsick.productiveday.productivity.ProductivityHostFragment;
import com.awsick.productiveday.tasks.TasksHostFragment;
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
    bottomNavigationView.setOnNavigationItemSelectedListener(
        item -> {

          // list of all fragments
          ProductivityHostFragment productivityFragment =
              (ProductivityHostFragment)
                  getSupportFragmentManager().findFragmentByTag("PRODUCTIVITY");
          TasksHostFragment taskFragment =
              (TasksHostFragment) getSupportFragmentManager().findFragmentByTag("TASKS");
          DirectoriesHostFragment directoryFragment =
              (DirectoriesHostFragment) getSupportFragmentManager().findFragmentByTag("DIRECTORY");
          FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

          // handle tasks fragment
          if (item.getItemId() == R.id.tasksHostFragment) {
            if (taskFragment == null) {
              transaction.add(R.id.main_content, new TasksHostFragment(), "TASKS");
            } else {
              transaction.attach(taskFragment);
            }

            if (directoryFragment != null) {
              transaction.detach(directoryFragment);
            }

            if (productivityFragment != null) {
              transaction.detach(productivityFragment);
            }

            // handle directory fragment
          } else if (item.getItemId() == R.id.directoriesHostFragment) {
            if (directoryFragment == null) {
              transaction.add(R.id.main_content, new DirectoriesHostFragment(), "DIRECTORY");
            } else {
              transaction.attach(directoryFragment);
            }

            if (taskFragment != null) {
              transaction.detach(taskFragment);
            }

            if (productivityFragment != null) {
              transaction.detach(productivityFragment);
            }

          } else if (item.getItemId() == R.id.productivityHostFragment) {
            if (productivityFragment == null) {
              transaction.add(R.id.main_content, new ProductivityHostFragment(), "PRODUCTIVITY");
            } else {
              transaction.attach(productivityFragment);
            }

            if (taskFragment != null) {
              transaction.detach(taskFragment);
            }

            if (directoryFragment != null) {
              transaction.detach(directoryFragment);
            }
          }

          transaction.commit();
          return true;
        });
    bottomNavigationView.setSelectedItemId(R.id.productivityHostFragment);
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

    @Override
    public void setFabVisibility(boolean visible) {
      if (visible) {
        fab.show();
      } else {
        fab.hide();
      }
    }
  }
}
