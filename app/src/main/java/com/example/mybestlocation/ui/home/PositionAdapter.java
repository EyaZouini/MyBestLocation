package com.example.mybestlocation.ui.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mybestlocation.Position;
import com.example.mybestlocation.R;

import java.util.ArrayList;

public class PositionAdapter extends RecyclerView.Adapter<PositionAdapter.PositionViewHolder> {

    private final Context context;
    private final ArrayList<Position> positions;
    private final OnPositionDeleteListener deleteListener;

    public interface OnPositionDeleteListener {
        void onDelete(Position position, int positionIndex);
    }

    public PositionAdapter(Context context, ArrayList<Position> positions, OnPositionDeleteListener deleteListener) {
        this.context = context;
        this.positions = positions;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public PositionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_position, parent, false);
        return new PositionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PositionViewHolder holder, int position) {
        Position currentPosition = positions.get(position);
        holder.positionText.setText(currentPosition.toString());

        holder.deleteButton.setOnClickListener(v -> deleteListener.onDelete(currentPosition, position));
    }

    @Override
    public int getItemCount() {
        return positions.size();
    }

    static class PositionViewHolder extends RecyclerView.ViewHolder {
        TextView positionText;
        ImageButton deleteButton;

        public PositionViewHolder(@NonNull View itemView) {
            super(itemView);
            positionText = itemView.findViewById(R.id.position_text);
            deleteButton = itemView.findViewById(R.id.delete_button);
        }
    }
}

