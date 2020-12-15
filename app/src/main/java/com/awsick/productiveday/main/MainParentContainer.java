package com.awsick.productiveday.main;

import android.view.View.OnClickListener;

public interface MainParentContainer {

  void setFabVisibility(boolean visible);

  void setToolbarTitle(String title);

  void setFabOcl(OnClickListener ocl);
}
