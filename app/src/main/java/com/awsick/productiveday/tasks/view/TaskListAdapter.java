package com.awsick.productiveday.tasks.view;

import static com.awsick.productiveday.common.utils.ImmutableUtils.toImmutableList;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import com.awsick.productiveday.R;
import com.awsick.productiveday.tasks.models.Task;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

public final class TaskListAdapter extends RecyclerView.Adapter<ViewHolder> {

  private final TaskItemActionListener listener;

  private ImmutableList<TaskListItemData> tasks = ImmutableList.of();

  public TaskListAdapter(TaskItemActionListener listener) {
    this.listener = listener;
  }

  void setTasks(ImmutableList<Task> tasks) {
    ImmutableList<TaskListItemData> newTasks =
        tasks.stream().map(TaskListItemData::task).collect(toImmutableList());
    TaskDiffUtil callback = new TaskDiffUtil(this.tasks, newTasks);
    this.tasks = newTasks;
    DiffUtil.calculateDiff(callback).dispatchUpdatesTo(this);
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
            listener,
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

    private final TaskItemActionListener listener;
    private final View clickTarget;
    private final TextView title;
    private final TextView notes;
    private final View done;

    public TaskViewHolder(TaskItemActionListener listener, @NonNull View itemView) {
      super(itemView);
      this.listener = listener;
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
            listener.onCompleteTaskRequested(task);
          });
      clickTarget.setOnClickListener(
          view -> {
            listener.onEditTaskRequested(task);
          });
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
          return oldTask.task.get().uid() == newTask.task.get().uid();
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
          return oldTask.task.get().equals(newTask.task.get());
        case TaskListItemData.UNKNOWN_VIEW_TYPE:
        default:
          throw new IllegalArgumentException("Unhandled view type: " + oldTask.viewType);
      }
    }
  }

  public interface TaskItemActionListener {

    void onCompleteTaskRequested(Task task);

    void onEditTaskRequested(Task task);
  }
}
