package com.awsick.productiveday.devoptions;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.awsick.productiveday.R;

public final class DevOptionsFragmentHostActivity extends AppCompatActivity {

  private static final String FRAG_KEY = "frag";

  enum Frag {
  }

  public static Intent create(Context context, Frag frag) {
    Intent intent = new Intent(context, DevOptionsFragmentHostActivity.class);
    intent.putExtra(FRAG_KEY, frag.toString());
    return intent;
  }

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_dev_options_fragment_host);
    Frag frag = Frag.valueOf(getIntent().getStringExtra(FRAG_KEY));
    switch (frag) {
    }
  }
}
