package com.python.ide;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ProjectAdapter extends RecyclerView.Adapter<ProjectAdapter.ViewHolder> {

    public interface OnProjectClickListener {
        void onClick(Project project);
        void onLongClick(Project project);
    }

    private final ArrayList<Project> projects;
    private final OnProjectClickListener listener;

    public ProjectAdapter(
            ArrayList<Project> projects,
            OnProjectClickListener listener
    ) {
        this.projects = projects;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(
                        R.layout.item_project,
                        parent,
                        false
                );

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position
    ) {

        Project project = projects.get(position);

        holder.projectName.setText(
                project.getName()
        );

        holder.itemView.setOnClickListener(v ->
                listener.onClick(project)
        );

        holder.itemView.setOnLongClickListener(v -> {
            listener.onLongClick(project);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return projects.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView projectName;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            projectName = itemView.findViewById(
                    R.id.txtProjectName
            );
        }
    }
}