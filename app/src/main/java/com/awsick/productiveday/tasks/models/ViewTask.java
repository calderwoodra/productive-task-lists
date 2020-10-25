package com.awsick.productiveday.tasks.models;

import com.awsick.productiveday.directories.models.DirectoryReference;

public final class ViewTask {

  public final Task task;
  public final DirectoryReference directory;

  public ViewTask(Task task, DirectoryReference directory) {
    this.task = task;
    this.directory = directory;
  }
}
