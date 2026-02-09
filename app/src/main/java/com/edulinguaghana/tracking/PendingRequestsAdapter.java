package com.edulinguaghana.tracking;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.edulinguaghana.R;
import com.edulinguaghana.roles.UserRelationship;

import java.util.List;

/**
 * Adapter for displaying pending relationship requests
 */
public class PendingRequestsAdapter extends RecyclerView.Adapter<PendingRequestsAdapter.ViewHolder> {

    private List<UserRelationship> requests;
    private OnRequestActionListener acceptListener;
    private OnRequestActionListener rejectListener;

    public interface OnRequestActionListener {
        void onAction(UserRelationship relationship);
    }

    public PendingRequestsAdapter(List<UserRelationship> requests,
                                 OnRequestActionListener acceptListener,
                                 OnRequestActionListener rejectListener) {
        this.requests = requests;
        this.acceptListener = acceptListener;
        this.rejectListener = rejectListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pending_request, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserRelationship request = requests.get(position);
        holder.bind(request, acceptListener, rejectListener);
    }

    @Override
    public int getItemCount() {
        return requests.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvRequestFrom;
        private TextView tvRequestType;
        private Button btnAccept;
        private Button btnReject;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRequestFrom = itemView.findViewById(R.id.tvRequestFrom);
            tvRequestType = itemView.findViewById(R.id.tvRequestType);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            btnReject = itemView.findViewById(R.id.btnReject);
        }

        public void bind(UserRelationship request,
                        OnRequestActionListener acceptListener,
                        OnRequestActionListener rejectListener) {
            String fromName = request.getSupervisorName() != null ?
                            request.getSupervisorName() : "Unknown";
            tvRequestFrom.setText(fromName);

            String type = request.getType() == UserRelationship.RelationType.TEACHER_STUDENT ?
                        "Teacher" : "Parent";
            tvRequestType.setText(type + " wants to track your progress");

            btnAccept.setOnClickListener(v -> {
                if (acceptListener != null) {
                    acceptListener.onAction(request);
                }
            });

            btnReject.setOnClickListener(v -> {
                if (rejectListener != null) {
                    rejectListener.onAction(request);
                }
            });
        }
    }
}

