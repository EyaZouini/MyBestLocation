
package com.example.mybestlocation.ui.home;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mybestlocation.Position;
import com.example.mybestlocation.R;
import java.util.ArrayList;

public class PositionAdapter extends RecyclerView.Adapter<PositionAdapter.PositionViewHolder> {

    private ArrayList<Position> positions;
    private final Context context;
    private final OnItemClickListener listener;
    private int selectedPosition = -1;

    public PositionAdapter(Context context, ArrayList<Position> positions, OnItemClickListener listener) {
        this.context = context;
        this.positions = positions;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PositionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Utilise le layout personnalisé pour chaque item
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_position, parent, false);
        return new PositionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PositionViewHolder holder, int position) {
        Position currentPosition = positions.get(position);

        // Update the TextView with the data
        holder.textView.setText(currentPosition.toString());

        // Use getAdapterPosition() to ensure the correct position is used
        if (holder.getAdapterPosition() == selectedPosition) {
            // Set the selected color
            holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.selected_item_color));
        } else {
            // Reset to transparent background for non-selected items
            holder.itemView.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
        }

        // Handle the click on the item
        holder.itemView.setOnClickListener(v -> {
            // Update the selected position dynamically
            selectedPosition = holder.getAdapterPosition();  // Get the position of the clicked item

            // Notify the adapter to update the entire list
            notifyDataSetChanged();  // This will trigger a refresh and update the background color of the selected item

            Log.d("PositionAdapter", "Selected position: " + selectedPosition);  // Log selected position

            // Handle the position click (e.g., zoom into the map)
            listener.onItemClick(currentPosition);
        });

        // Handle the delete icon click
        holder.deleteIcon.setOnClickListener(v -> {
            new DeletePositionTask(context, new DeletePositionTask.DeleteCallback() {
                @Override
                public void onDeleteSuccess() {
                    Toast.makeText(context, "Position deleted successfully", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onDeleteFailure() {
                    // Handle failure
                }
            }).execute(String.valueOf(currentPosition.getIdposition()));  // Convert int to String

            // Remove from the local list and update RecyclerView immediately
            positions.remove(holder.getAdapterPosition()); // Use getAdapterPosition to get the correct position
            notifyItemRemoved(holder.getAdapterPosition());
            notifyItemRangeChanged(holder.getAdapterPosition(), positions.size());
        });
    }

        @Override
    public int getItemCount() {
        return positions.size();
    }

    public void updateList(ArrayList<Position> newPositions) {
        positions = new ArrayList<>(newPositions); // Met à jour la liste
        notifyDataSetChanged();
    }

    // ViewHolder pour les vues individuelles de l'item
    static class PositionViewHolder extends RecyclerView.ViewHolder {
        final TextView textView;
        final ImageView deleteIcon;

        public PositionViewHolder(@NonNull View itemView) {
            super(itemView);

            // Associe les vues du layout à des variables
            textView = itemView.findViewById(R.id.tv_position);
            deleteIcon = itemView.findViewById(R.id.iv_delete);
        }
    }

    public Context getContext() {
        return context;
    }

    // Interface pour les clics sur les éléments
    public interface OnItemClickListener {
        void onItemClick(Position position);
    }
}

