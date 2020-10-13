package com.awsick.productiveday.devoptions;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.awsick.productiveday.BuildConfig;
import com.awsick.productiveday.R;
import com.awsick.productiveday.common.uiutils.FragmentUtils.FragmentUtilListener;
import com.awsick.productiveday.common.utils.Assert;

public final class DevOptionsActivity extends AppCompatActivity implements FragmentUtilListener {

  public static Intent create(Context context) {
    return new Intent(context, DevOptionsActivity.class);
  }

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Assert.checkState(BuildConfig.DEBUG);
    setContentView(R.layout.activity_dev_options);
  }

  @Nullable
  @Override
  public <T> T getImpl(Class<T> callbackInterface) {
    return null;
  }
}
