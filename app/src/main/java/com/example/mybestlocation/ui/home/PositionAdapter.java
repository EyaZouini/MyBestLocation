package com.example.mybestlocation.ui.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mybestlocation.Position;
import java.util.ArrayList;

public class PositionAdapter extends RecyclerView.Adapter<PositionAdapter.PositionViewHolder> {

    private ArrayList<Position> positions;
    private final Context context;
    private final OnItemClickListener listener;

    public PositionAdapter(Context context, ArrayList<Position> positions, OnItemClickListener listener) {
        this.context = context;
        this.positions = positions;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PositionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the item layout and return the ViewHolder
        View view = LayoutInflater.from(context)
                .inflate(android.R.layout.simple_list_item_1, parent, false);
        return new PositionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PositionViewHolder holder, int position) {
        // Bind the position data to the ViewHolder
        Position currentPosition = positions.get(position);
        holder.textView.setText(currentPosition.toString());

        // Set item click listener
        holder.itemView.setOnClickListener(v -> listener.onItemClick(currentPosition));
    }

    @Override
    public int getItemCount() {
        return positions.size();
    }

    public void updateList(ArrayList<Position> newPositions) {
        // Update the positions and notify the adapter
        positions = new ArrayList<>(newPositions);
        notifyDataSetChanged();
    }

    // ViewHolder class to hold the item views
    static class PositionViewHolder extends RecyclerView.ViewHolder {
        final TextView textView;

        public PositionViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(android.R.id.text1);
        }
    }

    public Context getContext() {
        return context;
    }

    // Interface for handling item clicks
    public interface OnItemClickListener {
        void onItemClick(Position position);
    }
}
