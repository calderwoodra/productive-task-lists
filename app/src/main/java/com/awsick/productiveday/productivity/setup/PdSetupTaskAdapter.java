package com.awsick.productiveday.productivity.setup;

import static com.awsick.productiveday.common.utils.ImmutableUtils.toImmutableList;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.awsick.productiveday.R;
import com.awsick.productiveday.productivity.setup.PdSetupTaskAdapter.PdSetupTaskViewHolder;
import com.google.common.collect.ImmutableList;

public final class PdSetupTaskAdapter extends RecyclerView.Adapter<PdSetupTaskViewHolder> {

  private ImmutableList<PdSetupTaskItemData> data = ImmutableList.of();

  @NonNull
  @Override
  public PdSetupTaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    return new PdSetupTaskItem(
        LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_pd_setup_task, parent, false));
  }

  @Override
  public void onBindViewHolder(@NonNull PdSetupTaskViewHolder holder, int position) {
    holder.bind(data.get(position));
  }

  @Override
  public int getItemCount() {
    return data.size();
  }

  public void setData(ImmutableList<String> tasks) {
    data = tasks.stream().map(PdSetupTaskItemData::new).collect(toImmutableList());
    notifyDataSetChanged();
  }

  private static final class PdSetupTaskItem extends PdSetupTaskViewHolder {

    private final TextView title;
    private final View remove;

    public PdSetupTaskItem(@NonNull View itemView) {
      super(itemView);
      title = itemView.findViewById(R.id.pd_setup_task_item_title);
      remove = itemView.findViewById(R.id.pd_setup_task_item_remove);
    }

    @Override
    void bind(PdSetupTaskItemData data) {
      title.setText(data.title);
      remove.setOnClickListener(
          view -> {
            // TODO(allen): Implement remove
          });
    }
  }

  abstract static class PdSetupTaskViewHolder extends RecyclerView.ViewHolder {

    public PdSetupTaskViewHolder(@NonNull View itemView) {
      super(itemView);
    }

    abstract void bind(PdSetupTaskItemData data);
  }

  private static final class PdSetupTaskItemData {

    private final String title;

    PdSetupTaskItemData(String title) {
      this.title = title;
    }
  }
}
