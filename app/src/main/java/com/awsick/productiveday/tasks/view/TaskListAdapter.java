package com.awsick.productiveday.tasks.view;

import static com.awsick.productiveday.common.utils.ImmutableUtils.toImmutableList;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.awsick.productiveday.R;
import com.awsick.productiveday.tasks.models.Task;
import com.awsick.productiveday.tasks.models.ViewTask;
import com.awsick.productiveday.tasks.view.TaskListAdapter.TaskListItem;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;

public final class TaskListAdapter extends RecyclerView.Adapter<TaskListItem> {

  private final TaskItemActionListener listener;

  private ImmutableList<TaskListItemData> tasks = ImmutableList.of();

  public TaskListAdapter(TaskItemActionListener listener) {
    this.listener = listener;
  }

  void setTasks(ImmutableList<ViewTask> tasks) {
    ImmutableList<TaskListItemData> newTasks =
        tasks.stream().map(TaskListItemData::task).collect(toImmutableList());
    TaskDiffUtil callback = new TaskDiffUtil(this.tasks, newTasks);
    this.tasks = newTasks;
    DiffUtil.calculateDiff(callback).dispatchUpdatesTo(this);
  }

  @Override
  public TaskListItem onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    switch (viewType) {
      case TaskListItemData.HEADER_VIEW_TYPE:
        return new HeaderViewHolder(
            LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task_header, parent, false));
      case TaskListItemData.TASK_VIEW_TYPE:
        return new TaskViewHolder(
            listener,
            LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false));
      case TaskListItemData.UNKNOWN_VIEW_TYPE:
      default:
        throw new IllegalArgumentException("Unknown viewtype: " + viewType);
    }
  }

  @Override
  public void onBindViewHolder(@NonNull TaskListItem holder, int position) {
    holder.bind(tasks.get(position));
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
    private final Optional<ViewTask> task;

    public static TaskListItemData task(ViewTask task) {
      return new TaskListItemData(TASK_VIEW_TYPE, Optional.absent(), Optional.of(task));
    }

    public static TaskListItemData header(String header) {
      return new TaskListItemData(HEADER_VIEW_TYPE, Optional.of(header), Optional.absent());
    }

    private TaskListItemData(int viewType, Optional<String> header, Optional<ViewTask> task) {
      this.viewType = viewType;
      this.header = header;
      this.task = task;
    }
  }

  private static final class HeaderViewHolder extends TaskListItem {

    private final TextView header;

    public HeaderViewHolder(@NonNull View itemView) {
      super(itemView);
      header = itemView.findViewById(R.id.task_header_header);
    }

    @Override
    public void bind(TaskListItemData data) {
      header.setText(data.header.get());
    }
  }

  private static final class TaskViewHolder extends TaskListItem {

    private final TaskItemActionListener listener;
    private final View clickTarget;
    private final TextView title;
    private final TextView notes;
    private final TextView directory;
    private final TextView nextDate;
    private final View done;

    public TaskViewHolder(TaskItemActionListener listener, @NonNull View itemView) {
      super(itemView);
      this.listener = listener;
      title = itemView.findViewById(R.id.task_item_title);
      notes = itemView.findViewById(R.id.task_item_notes);
      directory = itemView.findViewById(R.id.task_item_folder);
      nextDate = itemView.findViewById(R.id.task_item_deadline);
      done = itemView.findViewById(R.id.task_item_done);
      clickTarget = itemView.findViewById(R.id.task_item_click_target);
    }

    @Override
    public void bind(TaskListItemData data) {
      Task task = data.task.get().task;
      title.setText(task.title());
      if (task.completed()) {
        title.setPaintFlags(title.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        done.setOnClickListener(view -> listener.onUncompleteTaskRequested(task));
      } else {
        title.setPaintFlags(title.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
        done.setOnClickListener(view -> listener.onCompleteTaskRequested(task));
      }
      notes.setText(task.notes());
      notes.setVisibility(Strings.isNullOrEmpty(task.notes()) ? View.GONE : View.VISIBLE);
      clickTarget.setOnClickListener(view -> listener.onEditTaskRequested(task));
      directory.setText(data.task.get().directory.name());
      nextDate.setText(data.task.get().task.deadlineDistance());
    }
  }

  public static final class TaskDiffUtil extends DiffUtil.Callback {

    private final ImmutableList<TaskListItemData> oldTasks;
    private final ImmutableList<TaskListItemData> newTasks;

    public TaskDiffUtil(
        ImmutableList<TaskListItemData> oldTasks, ImmutableList<TaskListItemData> newTasks) {
      this.oldTasks = oldTasks;
      this.newTasks = newTasks;
    }

    @Override
    public int getOldListSize() {
      return oldTasks.size();
    }

    @Override
    public int getNewListSize() {
      return newTasks.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
      TaskListItemData oldTask = oldTasks.get(oldItemPosition);
      TaskListItemData newTask = newTasks.get(newItemPosition);

      if (oldTask.viewType != newTask.viewType) {
        return false;
      }

      switch (oldTask.viewType) {
        case TaskListItemData.HEADER_VIEW_TYPE:
          return oldTask.header.get().equals(newTask.header.get());
        case TaskListItemData.TASK_VIEW_TYPE:
          return oldTask.task.get().task.uid() == newTask.task.get().task.uid();
        case TaskListItemData.UNKNOWN_VIEW_TYPE:
        default:
          throw new IllegalArgumentException("Unhandled view type: " + oldTask.viewType);
      }
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
      TaskListItemData oldTask = oldTasks.get(oldItemPosition);
      TaskListItemData newTask = newTasks.get(newItemPosition);

      switch (oldTask.viewType) {
        case TaskListItemData.HEADER_VIEW_TYPE:
          return false;
        case TaskListItemData.TASK_VIEW_TYPE:
          return oldTask.task.get().task.equals(newTask.task.get().task)
              && oldTask.task.get().directory.equals(newTask.task.get().directory);
        case TaskListItemData.UNKNOWN_VIEW_TYPE:
        default:
          throw new IllegalArgumentException("Unhandled view type: " + oldTask.viewType);
      }
    }
  }

  abstract static class TaskListItem extends RecyclerView.ViewHolder {

    public TaskListItem(@NonNull View itemView) {
      super(itemView);
    }

    abstract void bind(TaskListItemData data);
  }

  public interface TaskItemActionListener {

    void onCompleteTaskRequested(Task task);

    void onEditTaskRequested(Task task);

    void onUncompleteTaskRequested(Task task);
  }
}
