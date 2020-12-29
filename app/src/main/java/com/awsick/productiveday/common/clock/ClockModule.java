package com.awsick.productiveday.common.clock;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.components.ApplicationComponent;
import java.time.Clock;
import javax.inject.Singleton;

@Module
@InstallIn(ApplicationComponent.class)
public final class ClockModule {

  @Provides
  @Singleton
  public static Clock provideClock() {
    return Clock.systemDefaultZone();
  }
}
