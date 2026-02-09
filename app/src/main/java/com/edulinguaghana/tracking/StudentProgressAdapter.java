package com.edulinguaghana.tracking;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.edulinguaghana.R;

import java.util.List;

/**
 * Adapter for displaying student progress cards
 */
public class StudentProgressAdapter extends RecyclerView.Adapter<StudentProgressAdapter.ViewHolder> {

    private List<StudentProgressItem> students;
    private OnStudentClickListener listener;

    public interface OnStudentClickListener {
        void onStudentClick(String studentId);
    }

    public StudentProgressAdapter(List<StudentProgressItem> students, OnStudentClickListener listener) {
        this.students = students;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_student_progress, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StudentProgressItem item = students.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return students.size();
    }

    public void updateStudents(List<StudentProgressItem> newStudents) {
        this.students = newStudents;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private CardView cardView;
        private TextView tvStudentName;
        private TextView tvLevel;
        private TextView tvXP;
        private TextView tvQuizzes;
        private TextView tvAccuracy;
        private TextView tvStreak;
        private TextView tvLastActive;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            tvStudentName = itemView.findViewById(R.id.tvStudentName);
            tvLevel = itemView.findViewById(R.id.tvLevel);
            tvXP = itemView.findViewById(R.id.tvXP);
            tvQuizzes = itemView.findViewById(R.id.tvQuizzes);
            tvAccuracy = itemView.findViewById(R.id.tvAccuracy);
            tvStreak = itemView.findViewById(R.id.tvStreak);
            tvLastActive = itemView.findViewById(R.id.tvLastActive);
        }

        public void bind(StudentProgressItem item, OnStudentClickListener listener) {
            ProgressAggregate progress = item.getProgress();

            tvStudentName.setText(item.getStudentName() != null ? item.getStudentName() : "Unknown Student");
            tvLevel.setText("Level " + progress.getCurrentLevel());
            tvXP.setText(progress.getTotalXP() + " XP");
            tvQuizzes.setText(progress.getTotalQuizzes() + " quizzes");
            tvAccuracy.setText(String.format("%.1f%%", progress.getAccuracy()));

            if (progress.getCurrentStreak() > 0) {
                tvStreak.setText("🔥 " + progress.getCurrentStreak() + " day streak");
                tvStreak.setVisibility(View.VISIBLE);
            } else {
                tvStreak.setVisibility(View.GONE);
            }

            // Format last active time
            if (progress.getLastUpdated() > 0) {
                long hoursSince = (System.currentTimeMillis() - progress.getLastUpdated()) / (1000 * 60 * 60);
                if (hoursSince < 1) {
                    tvLastActive.setText("Active just now");
                } else if (hoursSince < 24) {
                    tvLastActive.setText("Active " + hoursSince + "h ago");
                } else {
                    long daysSince = hoursSince / 24;
                    tvLastActive.setText("Active " + daysSince + "d ago");
                }
            } else {
                tvLastActive.setText("Not active yet");
            }

            cardView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onStudentClick(item.getStudentId());
                }
            });
        }
    }
}

