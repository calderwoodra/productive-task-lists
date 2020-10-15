package com.awsick.productiveday.tasks.view;

import static java.util.stream.Collectors.toList;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import com.awsick.productiveday.R;
import com.awsick.productiveday.tasks.models.Task;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

public final class TaskListAdapter extends RecyclerView.Adapter<ViewHolder> {

  private ImmutableList<TaskListItemData> tasks = ImmutableList.of();

  void setTasks(ImmutableList<Task> tasks) {
    this.tasks = ImmutableList.copyOf(tasks.stream().map(TaskListItemData::task).collect(toList()));
    notifyDataSetChanged();
  }

  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    switch (viewType) {
      case TaskListItemData.HEADER_VIEW_TYPE:
        return new HeaderViewHolder(
            LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task_header, parent, false));
      case TaskListItemData.TASK_VIEW_TYPE:
        return new TaskViewHolder(
            LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false));
      case TaskListItemData.UNKNOWN_VIEW_TYPE:
      default:
        throw new IllegalArgumentException("Unknown viewtype: " + viewType);
    }
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    TaskListItemData data = tasks.get(position);
    if (holder instanceof HeaderViewHolder) {
      ((HeaderViewHolder) holder).bind(data);
    } else if (holder instanceof TaskViewHolder) {
      ((TaskViewHolder) holder).bind(data);
    } else {
      throw new IllegalArgumentException("Unknown view holder: " + holder.getClass());
    }
  }

  @Override
  public int getItemCount() {
    return tasks.size();
  }

  @Override
  public int getItemViewType(int position) {
    return tasks.get(position).viewType;
  }

  private static final class TaskListItemData {

    private static final int UNKNOWN_VIEW_TYPE = 0;
    private static final int HEADER_VIEW_TYPE = 1;
    private static final int TASK_VIEW_TYPE = 2;

    private final int viewType;
    private final Optional<String> header;
    private final Optional<Task> task;

    public static TaskListItemData task(Task task) {
      return new TaskListItemData(TASK_VIEW_TYPE, Optional.absent(), Optional.of(task));
    }

    public static TaskListItemData header(String header) {
      return new TaskListItemData(HEADER_VIEW_TYPE, Optional.of(header), Optional.absent());
    }

    private TaskListItemData(int viewType, Optional<String> header, Optional<Task> task) {
      this.viewType = viewType;
      this.header = header;
      this.task = task;
    }
  }

  private static final class HeaderViewHolder extends RecyclerView.ViewHolder {

    private final TextView header;

    public HeaderViewHolder(@NonNull View itemView) {
      super(itemView);
      header = itemView.findViewById(R.id.task_header_header);
    }

    public void bind(TaskListItemData data) {
      header.setText(data.header.get());
    }
  }

  private static final class TaskViewHolder extends RecyclerView.ViewHolder {

    private final View clickTarget;
    private final TextView title;
    private final TextView notes;
    private final View done;

    public TaskViewHolder(@NonNull View itemView) {
      super(itemView);
      title = itemView.findViewById(R.id.task_item_title);
      notes = itemView.findViewById(R.id.task_item_notes);
      done = itemView.findViewById(R.id.task_item_done);
      clickTarget = itemView.findViewById(R.id.task_item_click_target);
    }

    public void bind(TaskListItemData data) {
      Task task = data.task.get();
      title.setText(task.title());
      notes.setText(task.notes());
      done.setOnClickListener(
          view -> {
            // TODO(allen): Mark a task as completed
          });
      clickTarget.setOnClickListener(
          view -> {
            // TODO(allen): open edit/details screen
          });
    }
  }
}
