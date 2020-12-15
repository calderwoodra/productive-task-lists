package com.awsick.productiveday.productivity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.awsick.productiveday.R;
import com.awsick.productiveday.common.uiutils.FragmentUtils;
import com.awsick.productiveday.main.MainParentContainer;

public final class ProductivitySetupFragment extends Fragment {

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_productivity_setup, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    MainParentContainer parent = FragmentUtils.getParentUnsafe(this, MainParentContainer.class);
    parent.setToolbarTitle("Productive Day");
    parent.setFabVisibility(false);
  }
}
