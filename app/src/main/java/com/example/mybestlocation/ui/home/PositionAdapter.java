
package com.example.mybestlocation.ui.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
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
        View view = LayoutInflater.from(context).inflate(R.layout.item_position, parent, false);
        return new PositionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PositionViewHolder holder, int position) {
        Position currentPosition = positions.get(position);

        // Afficher le pseudo et le type
        holder.pseudoTextView.setText(currentPosition.getPseudo());
        holder.typeTextView.setText(currentPosition.getType()); // Afficher le type

        // Changer la couleur de fond si sélectionné
        holder.itemView.setBackgroundColor(holder.getAdapterPosition() == selectedPosition
                ? ContextCompat.getColor(context, R.color.selected_item_color)
                : ContextCompat.getColor(context, android.R.color.transparent));

        // Gérer le clic sur l'élément
        holder.itemView.setOnClickListener(v -> {
            selectedPosition = holder.getAdapterPosition();
            notifyDataSetChanged();
            listener.onItemClick(currentPosition);
        });

        // Gérer l'icône de suppression
        holder.deleteIcon.setOnClickListener(v -> {
            new DeletePositionTask(context, new DeletePositionTask.DeleteCallback() {
                @Override
                public void onDeleteSuccess() {
                    // Optionnel : Notifier l'utilisateur en cas de succès
                }

                @Override
                public void onDeleteFailure() {
                    // Optionnel : Notifier l'utilisateur en cas d'échec
                }
            }).execute(String.valueOf(currentPosition.getIdposition()));

            positions.remove(holder.getAdapterPosition());
            notifyItemRemoved(holder.getAdapterPosition());
            notifyItemRangeChanged(holder.getAdapterPosition(), positions.size());
        });
    }


    @Override
    public int getItemCount() {
        return positions.size();
    }

    public void updateList(ArrayList<Position> newPositions) {
        positions = new ArrayList<>(newPositions);
        notifyDataSetChanged();
    }

    static class PositionViewHolder extends RecyclerView.ViewHolder {
        final TextView pseudoTextView;
        final TextView typeTextView; // Nouveau champ
        final ImageView deleteIcon;

        public PositionViewHolder(@NonNull View itemView) {
            super(itemView);
            pseudoTextView = itemView.findViewById(R.id.tv_position);
            typeTextView = itemView.findViewById(R.id.tv_type); // Lier le TextView du type
            deleteIcon = itemView.findViewById(R.id.iv_delete);
        }
    }


    public Context getContext() {
        return context;
    }

    public interface OnItemClickListener {
        void onItemClick(Position position);
    }
}