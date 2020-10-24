package com.awsick.productiveday.directories.ui;

import static com.awsick.productiveday.common.utils.ImmutableUtils.toImmutableList;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.awsick.productiveday.R;
import com.awsick.productiveday.directories.models.Directory;
import com.awsick.productiveday.directories.models.DirectoryReference;
import com.awsick.productiveday.directories.ui.DirectoryListAdapter.DirectoryListItemViewHolder;
import com.awsick.productiveday.tasks.models.Task;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

public final class DirectoryListAdapter extends RecyclerView.Adapter<DirectoryListItemViewHolder> {

  private final DirectoryItemActionListener listener;
  private final boolean includeTasks;

  private ImmutableList<DirectoryListItemData> items = ImmutableList.of();

  DirectoryListAdapter(DirectoryItemActionListener listener, boolean includeTasks) {
    this.listener = listener;
    this.includeTasks = includeTasks;
  }

  public void setDirectory(Directory directory) {
    ImmutableList.Builder<DirectoryListItemData> items = ImmutableList.builder();
    if (directory.parent().isPresent()) {
      items.add(DirectoryListItemData.parent(directory.parent().get()));
    }

    items.addAll(
        directory.directories().stream()
            .map(DirectoryListItemData::directory)
            .collect(toImmutableList()));

    if (includeTasks) {
      items.addAll(
          directory.tasks().stream().map(DirectoryListItemData::task).collect(toImmutableList()));
    }

    this.items = items.build();
    notifyDataSetChanged();
  }

  @NonNull
  @Override
  public DirectoryListItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    switch (viewType) {
      case DirectoryListItemData.DIRECTORY_VIEW_TYPE:
        return new DirectoryViewHolder(
            listener,
            LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_directory, parent, false));
      case DirectoryListItemData.TASK_VIEW_TYPE:
        return new TaskViewHolder(
            listener,
            LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false));
      case DirectoryListItemData.PARENT_VIEW_TYPE:
        return new ParentDirectoryViewHolder(
            listener,
            LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_parent_directory, parent, false));
      case DirectoryListItemData.UNKNOWN_VIEW_TYPE:
      default:
        throw new IllegalArgumentException("Unknown view type: " + viewType);
    }
  }

  @Override
  public void onBindViewHolder(@NonNull DirectoryListItemViewHolder holder, int position) {
    holder.bind(items.get(position));
  }

  @Override
  public int getItemCount() {
    return items.size();
  }

  @Override
  public int getItemViewType(int position) {
    return items.get(position).viewType;
  }

  private static final class ParentDirectoryViewHolder extends DirectoryListItemViewHolder {

    private final TextView name;
    private final View root;

    public ParentDirectoryViewHolder(DirectoryItemActionListener listener, @NonNull View itemView) {
      super(listener, itemView);
      name = itemView.findViewById(R.id.directory_item_title);
      root = itemView.findViewById(R.id.directory_item_click_target);
    }

    @Override
    void bind(DirectoryListItemData data) {
      name.setText(data.directory.get().name());
      root.setOnClickListener(view -> listener.onNavigateToDirectory(data.directory.get()));
    }
  }

  private static final class DirectoryViewHolder extends DirectoryListItemViewHolder {

    private final TextView name;
    private final View root;

    public DirectoryViewHolder(DirectoryItemActionListener listener, @NonNull View itemView) {
      super(listener, itemView);
      name = itemView.findViewById(R.id.directory_item_title);
      root = itemView.findViewById(R.id.directory_item_click_target);
    }

    @Override
    void bind(DirectoryListItemData data) {
      name.setText(data.directory.get().name());
      root.setOnClickListener(view -> listener.onNavigateToDirectory(data.directory.get()));
      root.setOnLongClickListener(
          view -> {
            listener.onEditDirectoryRequested(data.directory.get());
            return true;
          });
    }
  }

  private static final class TaskViewHolder extends DirectoryListItemViewHolder {

    private final TextView title;
    private final TextView subtitle;
    private final View root;
    private final View done;

    public TaskViewHolder(DirectoryItemActionListener listener, @NonNull View itemView) {
      super(listener, itemView);
      title = itemView.findViewById(R.id.task_item_title);
      subtitle = itemView.findViewById(R.id.task_item_notes);
      root = itemView.findViewById(R.id.task_item_click_target);
      done = itemView.findViewById(R.id.task_item_done);
    }

    @Override
    void bind(DirectoryListItemData data) {
      title.setText(data.task.get().title());
      subtitle.setText(data.task.get().notes());
      root.setOnClickListener(
          view -> {
            listener.onEditTaskRequested(data.task.get());
          });
      done.setOnClickListener(
          view -> {
            listener.onCompleteTaskRequested(data.task.get());
          });
    }
  }

  abstract static class DirectoryListItemViewHolder extends RecyclerView.ViewHolder {

    protected final DirectoryItemActionListener listener;

    public DirectoryListItemViewHolder(
        DirectoryItemActionListener listener, @NonNull View itemView) {
      super(itemView);
      this.listener = listener;
    }

    abstract void bind(DirectoryListItemData data);
  }

  private static final class DirectoryListItemData {

    private static final int UNKNOWN_VIEW_TYPE = 0;
    private static final int DIRECTORY_VIEW_TYPE = 1;
    private static final int TASK_VIEW_TYPE = 2;
    private static final int PARENT_VIEW_TYPE = 3;

    private final int viewType;
    private final Optional<DirectoryReference> directory;
    private final Optional<Task> task;

    public static DirectoryListItemData task(Task task) {
      return new DirectoryListItemData(TASK_VIEW_TYPE, Optional.absent(), Optional.of(task));
    }

    public static DirectoryListItemData parent(DirectoryReference parent) {
      return new DirectoryListItemData(PARENT_VIEW_TYPE, Optional.of(parent), Optional.absent());
    }

    public static DirectoryListItemData directory(DirectoryReference directory) {
      return new DirectoryListItemData(
          DIRECTORY_VIEW_TYPE, Optional.of(directory), Optional.absent());
    }

    private DirectoryListItemData(
        int viewType, Optional<DirectoryReference> directory, Optional<Task> task) {
      this.viewType = viewType;
      this.directory = directory;
      this.task = task;
    }
  }

  public interface DirectoryItemActionListener {

    void onNavigateToDirectory(DirectoryReference directory);

    void onCompleteTaskRequested(Task task);

    void onEditTaskRequested(Task task);

    void onEditDirectoryRequested(DirectoryReference reference);
  }
}
