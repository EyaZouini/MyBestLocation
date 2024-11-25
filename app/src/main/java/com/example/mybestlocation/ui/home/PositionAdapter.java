
package com.example.mybestlocation.ui.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mybestlocation.Position;
import com.example.mybestlocation.R;
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
        // Utilise le layout personnalisé pour chaque item
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_position, parent, false);
        return new PositionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PositionViewHolder holder, int position) {
        // Récupère l'élément courant
        Position currentPosition = positions.get(position);

        // Met à jour le TextView avec les données
        holder.textView.setText(currentPosition.toString());

        // Gestion du clic sur l'élément entier
        holder.itemView.setOnClickListener(v -> listener.onItemClick(currentPosition));

        holder.deleteIcon.setOnClickListener(v -> {
            // Use the context passed in the adapter instead of requireContext
            new DeletePositionTask(context, new DeletePositionTask.DeleteCallback() {
                @Override
                public void onDeleteSuccess() {
                    Toast.makeText(context, "Position deleted successfully", Toast.LENGTH_SHORT).show();
                    // Optionally, you can remove the item from the list if it hasn't been removed yet
                }

                @Override
                public void onDeleteFailure() {
                }
            }).execute(String.valueOf(currentPosition.getIdposition()));  // Convert int to String


            // Remove from the local list and update RecyclerView immediately
            positions.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, positions.size());
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

