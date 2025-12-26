package com.example.effortlessflow.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.effortlessflow.R;
import com.example.effortlessflow.model.SubTask;
import java.util.List;

public class SubTaskAdapter extends RecyclerView.Adapter<SubTaskAdapter.SubTaskViewHolder> {
    private List<SubTask> subTasks;
    private OnSubTaskListener listener;

    public SubTaskAdapter(List<SubTask> subTasks, OnSubTaskListener listener) {
        this.subTasks = subTasks;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SubTaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_subtask, parent, false);
        return new SubTaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SubTaskViewHolder holder, int position) {
        SubTask subTask = subTasks.get(position);
        holder.bind(subTask);
    }

    @Override
    public int getItemCount() {
        return subTasks.size();
    }

    public void updateSubTasks(List<SubTask> newSubTasks) {
        this.subTasks = newSubTasks;
        notifyDataSetChanged();
    }

    public class SubTaskViewHolder extends RecyclerView.ViewHolder {
        private CheckBox subtaskCheckbox;
        private ImageView subtaskCompletedIcon;
        private TextView subtaskTitle;
        private TextView subtaskDescription;

        public SubTaskViewHolder(@NonNull View itemView) {
            super(itemView);
            subtaskCheckbox = itemView.findViewById(R.id.subtask_checkbox);
            subtaskCompletedIcon = itemView.findViewById(R.id.subtask_completed_icon);
            subtaskTitle = itemView.findViewById(R.id.subtask_title_tv);
            subtaskDescription = itemView.findViewById(R.id.subtask_description_tv);
        }

        public void bind(SubTask subTask) {
            subtaskTitle.setText(subTask.getTitle());

            if (subTask.getDescription() != null && !subTask.getDescription().isEmpty()) {
                subtaskDescription.setVisibility(View.VISIBLE);
                subtaskDescription.setText(subTask.getDescription());
            } else {
                subtaskDescription.setVisibility(View.GONE);
            }

            if (subTask.isCompleted()) {
                subtaskCheckbox.setVisibility(View.GONE);
                subtaskCompletedIcon.setVisibility(View.VISIBLE);
            } else {
                subtaskCheckbox.setVisibility(View.VISIBLE);
                subtaskCompletedIcon.setVisibility(View.GONE);
                subtaskCheckbox.setChecked(false);
            }

            subtaskCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (listener != null) {
                    listener.onSubTaskCheckChanged(subTask, isChecked);
                }
            });
        }
    }

    public interface OnSubTaskListener {
        void onSubTaskCheckChanged(SubTask subTask, boolean isChecked);
    }
}