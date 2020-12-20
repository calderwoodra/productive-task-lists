package com.awsick.productiveday.productivity.setup;

import static com.awsick.productiveday.common.utils.ImmutableUtils.toImmutableList;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.awsick.productiveday.R;
import com.awsick.productiveday.productivity.setup.PdSetupTaskAdapter.PdSetupTaskViewHolder;
import com.awsick.productiveday.tasks.models.Task;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

public final class PdSetupTaskAdapter extends RecyclerView.Adapter<PdSetupTaskViewHolder> {

  private final TaskActions taskActions;
  private ImmutableList<PdSetupTaskItemData> data = ImmutableList.of();

  public PdSetupTaskAdapter(TaskActions taskActions) {
    this.taskActions = taskActions;
  }

  @NonNull
  @Override
  public PdSetupTaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    switch (viewType) {
      case PdSetupTaskItemData.TYPE_HEADER:
        return new PdSetupHeaderItem(
            LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task_header, parent, false));
      case PdSetupTaskItemData.TYPE_EXISTING_TASK:
        return new PdSetupExistingTaskItem(
            taskActions,
            LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false));
      case PdSetupTaskItemData.TYPE_TEMP_TASK:
        return new PdSetupTaskItem(
            taskActions,
            LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false));
      default:
        throw new IllegalArgumentException("Unknown view type: " + viewType);
    }
  }

  @Override
  public void onBindViewHolder(@NonNull PdSetupTaskViewHolder holder, int position) {
    holder.bind(data.get(position));
  }

  @Override
  public int getItemCount() {
    return data.size();
  }

  @Override
  public int getItemViewType(int position) {
    return data.get(position).getType();
  }

  public void setData(ImmutableList<Task> tasks) {
    ImmutableList<PdSetupTaskItemData> tempTasks =
        tasks.stream()
            .filter(task -> task.uid() == 0)
            .map(PdSetupTaskItemData::tempTask)
            .collect(toImmutableList());
    ImmutableList<PdSetupTaskItemData> existingTasks =
        tasks.stream()
            .filter(task -> task.uid() != 0)
            .map(PdSetupTaskItemData::existingTask)
            .collect(toImmutableList());

    ImmutableList.Builder<PdSetupTaskItemData> dataBuilder = ImmutableList.builder();
    if (!tempTasks.isEmpty()) {
      dataBuilder.add(PdSetupTaskItemData.header("New Tasks"));
      dataBuilder.addAll(tempTasks);
    }

    if (!existingTasks.isEmpty()) {
      dataBuilder.add(PdSetupTaskItemData.header("Existing Tasks"));
      dataBuilder.addAll(existingTasks);
    }
    ImmutableList<PdSetupTaskItemData> newData = dataBuilder.build();
    TaskItemDiffCallback diffCallback = new TaskItemDiffCallback(data, newData);
    DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);
    data = newData;
    diffResult.dispatchUpdatesTo(this);
  }

  private static final class PdSetupHeaderItem extends PdSetupTaskViewHolder {

    private final TextView title;

    public PdSetupHeaderItem(@NonNull View itemView) {
      super(itemView);
      title = itemView.findViewById(R.id.task_header_header);
    }

    @Override
    void bind(PdSetupTaskItemData data) {
      title.setText(data.getHeader());
    }
  }

  private static final class PdSetupExistingTaskItem extends PdSetupTaskViewHolder {

    private final TaskActions taskActions;
    private final TextView title;
    private final ImageView done;

    public PdSetupExistingTaskItem(TaskActions taskActions, @NonNull View itemView) {
      super(itemView);
      this.taskActions = taskActions;
      title = itemView.findViewById(R.id.task_item_title);
      done = itemView.findViewById(R.id.task_item_done);
    }

    @Override
    void bind(PdSetupTaskItemData data) {
      title.setText(data.getTask().title());
      done.setOnClickListener(view -> taskActions.markTaskCompleted(data.getTask()));
      itemView.setOnClickListener(view -> taskActions.editTask(data.getTask()));
    }
  }

  private static final class PdSetupTaskItem extends PdSetupTaskViewHolder {

    private final TaskActions taskActions;
    private final TextView title;
    private final View remove;

    public PdSetupTaskItem(TaskActions taskActions, @NonNull View itemView) {
      super(itemView);
      this.taskActions = taskActions;
      title = itemView.findViewById(R.id.task_item_title);
      remove = itemView.findViewById(R.id.pd_setup_task_item_remove);
      itemView.findViewById(R.id.task_item_done).setVisibility(View.INVISIBLE);
      itemView.findViewById(R.id.pd_setup_task_item_remove).setVisibility(View.VISIBLE);
    }

    @Override
    void bind(PdSetupTaskItemData data) {
      title.setText(data.getTask().title());
      remove.setOnClickListener(view -> taskActions.removeTask(data.getTask()));
    }
  }

  abstract static class PdSetupTaskViewHolder extends RecyclerView.ViewHolder {

    public PdSetupTaskViewHolder(@NonNull View itemView) {
      super(itemView);
    }

    abstract void bind(PdSetupTaskItemData data);
  }

  private static final class PdSetupTaskItemData {

    private static final int TYPE_HEADER = 1;
    private static final int TYPE_TEMP_TASK = 2;
    private static final int TYPE_EXISTING_TASK = 3;

    private final int type;
    private final Optional<String> header;
    private final Optional<Task> task;

    PdSetupTaskItemData(int type, Optional<Task> task, Optional<String> header) {
      this.type = type;
      this.task = task;
      this.header = header;
    }

    public static PdSetupTaskItemData tempTask(Task task) {
      return new PdSetupTaskItemData(TYPE_TEMP_TASK, Optional.of(task), Optional.absent());
    }

    public static PdSetupTaskItemData existingTask(Task task) {
      return new PdSetupTaskItemData(TYPE_EXISTING_TASK, Optional.of(task), Optional.absent());
    }

    public static PdSetupTaskItemData header(String header) {
      return new PdSetupTaskItemData(TYPE_HEADER, Optional.absent(), Optional.of(header));
    }

    public int getType() {
      return type;
    }

    public String getHeader() {
      return header.get();
    }

    public Task getTask() {
      return task.get();
    }
  }

  public interface TaskActions {

    void markTaskCompleted(Task task);

    void editTask(Task task);

    /** Remove a task from the list. */
    void removeTask(Task task);
  }

  private static final class TaskItemDiffCallback extends DiffUtil.Callback {

    private final ImmutableList<PdSetupTaskItemData> oldData;
    private final ImmutableList<PdSetupTaskItemData> newData;

    private TaskItemDiffCallback(
        ImmutableList<PdSetupTaskItemData> oldData, ImmutableList<PdSetupTaskItemData> newData) {
      this.oldData = oldData;
      this.newData = newData;
    }

    @Override
    public int getOldListSize() {
      return oldData.size();
    }

    @Override
    public int getNewListSize() {
      return newData.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
      PdSetupTaskItemData oldItem = oldData.get(oldItemPosition);
      PdSetupTaskItemData newItem = newData.get(newItemPosition);
      if (oldItem.getType() != newItem.getType()) {
        return false;
      }

      switch (oldItem.getType()) {
        case PdSetupTaskItemData.TYPE_HEADER:
          return oldItem.getHeader().equals(newItem.getHeader());
        case PdSetupTaskItemData.TYPE_EXISTING_TASK:
          return oldItem.getTask().uid() == newItem.getTask().uid();
        case PdSetupTaskItemData.TYPE_TEMP_TASK:
          return oldItem.getTask().title().equals(oldItem.getTask().title());
        default:
          throw new IllegalArgumentException("Unhandled type: " + oldItem.getType());
      }
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
      return true;
    }
  }
}
