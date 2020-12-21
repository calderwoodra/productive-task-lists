package com.awsick.productiveday.main;

import android.view.View.OnClickListener;
import androidx.annotation.DrawableRes;

public interface MainParentContainer {

  void setFabVisibility(boolean visible);

  void setToolbarTitle(String title);

  void setFabOcl(OnClickListener ocl);

  void setupExpandableFab(FabOptions options1, FabOptions options2);

  final class FabOptions {

    // TODO(allen): Consider adding text
    @DrawableRes public final int icon;
    public final OnClickListener onClickListener;

    public FabOptions(int icon, OnClickListener onClickListener) {
      this.icon = icon;
      this.onClickListener = onClickListener;
    }
  }
}
