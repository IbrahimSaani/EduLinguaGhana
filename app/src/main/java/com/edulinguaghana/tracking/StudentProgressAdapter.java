package com.edulinguaghana.tracking;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.edulinguaghana.R;

import com.edulinguaghana.roles.UserRole;
import java.util.List;

/**
 * Adapter for displaying student progress cards
 */
public class StudentProgressAdapter extends RecyclerView.Adapter<StudentProgressAdapter.ViewHolder> {

    private List<StudentProgressItem> students;
    private OnStudentClickListener listener;
    private UserRole viewRole = UserRole.STUDENT;

    public interface OnStudentClickListener {
        void onStudentClick(String studentId);
        void onRemoveStudent(StudentProgressItem student);
    }

    public StudentProgressAdapter(List<StudentProgressItem> students, OnStudentClickListener listener) {
        this.students = students;
        this.listener = listener;
    }

    public StudentProgressAdapter(List<StudentProgressItem> students, UserRole role, OnStudentClickListener listener) {
        this.students = students;
        this.viewRole = role;
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
        holder.bind(item, viewRole, listener);
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
        private TextView tvStudentClass;
        private View streakLayout;
        private View btnViewDetails;
        private View btnRemove;

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
            tvStudentClass = itemView.findViewById(R.id.tvStudentClass);
            streakLayout = itemView.findViewById(R.id.streakLayout);
            btnViewDetails = itemView.findViewById(R.id.btnViewDetails);
            btnRemove = itemView.findViewById(R.id.btnRemove);
        }

        public void bind(StudentProgressItem item, UserRole role, OnStudentClickListener listener) {
            ProgressAggregate progress = item.getProgress();

            tvStudentName.setText(item.getStudentName() != null ? item.getStudentName() : "Unknown Student");
            tvLevel.setText("Lvl " + progress.getCurrentLevel());
            tvXP.setText(String.valueOf(progress.getTotalXP()));
            tvQuizzes.setText(String.valueOf(progress.getTotalQuizzes()));
            tvAccuracy.setText(String.format("%.0f%%", progress.getAccuracy()));

            // Role-based customization
            if (role == UserRole.PARENT) {
                // Parents might care less about accuracy percentage on the summary card
                // Maybe we can show something else, but for now we'll just keep it consistent 
                // with what we decided for the detail screen if needed.
                // However, the detail screen hide Accuracy for parents.
                // Let's hide the accuracy column for parents to make it feel different.
                if (tvAccuracy != null && tvAccuracy.getParent() instanceof View) {
                    ((View) tvAccuracy.getParent()).setVisibility(View.GONE);
                }
            } else if (role == UserRole.TEACHER) {
                // Teachers focus on accuracy, maybe hide streak to keep it academic
                if (streakLayout != null) {
                    streakLayout.setVisibility(View.GONE);
                }
            }

            if (role != UserRole.TEACHER && progress.getCurrentStreak() > 0) {
                tvStreak.setText("🔥 " + progress.getCurrentStreak() + " day streak");
                if (streakLayout != null) {
                    streakLayout.setVisibility(View.VISIBLE);
                }
            } else if (role != UserRole.TEACHER) {
                if (streakLayout != null) {
                    streakLayout.setVisibility(View.GONE);
                }
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

            if (tvStudentClass != null) {
                String studentClass = item.getStudentClass();
                tvStudentClass.setText("Class: " + (studentClass != null && !studentClass.trim().isEmpty()
                        ? studentClass.trim()
                        : "Not set"));
            }

            // Click listeners
            cardView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onStudentClick(item.getStudentId());
                }
            });

            if (btnViewDetails != null) {
                btnViewDetails.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onStudentClick(item.getStudentId());
                    }
                });
            }

            if (btnRemove != null) {
                btnRemove.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onRemoveStudent(item);
                    }
                });
            }
        }
    }
}

