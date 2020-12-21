package com.awsick.productiveday.main;

import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewPropertyAnimator;
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
    parent =
        new MainParent(
            findViewById(R.id.toolbar),
            findViewById(R.id.fab),
            findViewById(R.id.fab_option_1),
            findViewById(R.id.fab_option_2),
            findViewById(R.id.expandable_fab_background));
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
    private final FloatingActionButton fabOption1;
    private final FloatingActionButton fabOption2;
    private final View background;

    private final float fabElevation;
    private final float expandedFabElevation;
    private final float fab1y;
    private final float fab2y;

    private boolean isFabExpanded = false;

    private MainParent(
        Toolbar toolbar,
        FloatingActionButton fab,
        FloatingActionButton fabOption1,
        FloatingActionButton fabOption2,
        View background) {
      this.toolbar = toolbar;
      this.fab = fab;
      this.fabOption1 = fabOption1;
      this.fabOption2 = fabOption2;
      this.background = background;

      background.setOnClickListener(
          view -> {
            isFabExpanded = false;
            animateCollapseFab();
          });

      Resources resources = fab.getContext().getResources();
      fabElevation = resources.getDimension(R.dimen.fab_elevation);
      expandedFabElevation = resources.getDimension(R.dimen.fab_expanded_elevation);
      fab1y = -resources.getDimension(R.dimen.expanded_fab_1_y);
      fab2y = -resources.getDimension(R.dimen.expanded_fab_2_y);
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
    public void setupExpandableFab(FabOptions options1, FabOptions options2) {
      fab.setOnClickListener(
          view1 -> {
            if (isFabExpanded) {
              isFabExpanded = false;
              animateCollapseFab();
            } else {
              isFabExpanded = true;
              animateExpandFab();
            }
          });

      fabOption1.setImageResource(options1.icon);
      fabOption1.setOnClickListener(
          view3 -> {
            isFabExpanded = false;
            animateCollapseFab();
            options1.onClickListener.onClick(view3);
          });

      fabOption2.setImageResource(options2.icon);
      fabOption2.setOnClickListener(
          view4 -> {
            isFabExpanded = false;
            animateCollapseFab();
            options2.onClickListener.onClick(view4);
          });
    }

    @Override
    public void setFabVisibility(boolean visible) {
      if (visible) {
        fab.show();
      } else {
        fab.hide();
      }
    }

    private void animateExpandFab() {
      fab.setCompatElevation(expandedFabElevation);
      fab.animate().rotation(45);
      fabOption1.setVisibility(View.VISIBLE);
      fabOption1.animate().translationY(fab1y);
      fabOption2.setVisibility(View.VISIBLE);
      fabOption2.animate().translationY(fab2y);
      background.setAlpha(0);
      background.setVisibility(View.VISIBLE);
      background.animate().alpha(100);
    }

    private void animateCollapseFab() {
      fab.animate().rotation(0);
      cleanUpAnimation(fabOption1, fabOption1.animate().translationY(0));
      cleanUpAnimation(fabOption2, fabOption2.animate().translationY(0));
      cleanUpAnimation(background, background.animate().alpha(0));
    }

    private void cleanUpAnimation(View view, ViewPropertyAnimator animator) {
      AnimatorUpdateListener listener =
          animation -> {
            if (animation.getAnimatedFraction() >= 0.95) {
              view.setVisibility(View.GONE);
              animator.setUpdateListener(null);
              fab.setElevation(fabElevation);
            }
          };
      animator.setUpdateListener(listener);
    }
  }
}
