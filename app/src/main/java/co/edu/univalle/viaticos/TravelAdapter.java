package co.edu.univalle.viaticos;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import co.edu.univalle.viaticos.data.entity.Travel;

public class TravelAdapter extends RecyclerView.Adapter<TravelAdapter.TravelViewHolder> {
    private List<Travel> travelList;
    private OnTravelClickListener listener;
    private SimpleDateFormat dateFormat;

    public interface OnTravelClickListener {
        void onTravelClick(Travel travel);
    }

    public TravelAdapter(List<Travel> travelList, OnTravelClickListener listener) {
        this.travelList = travelList;
        this.listener = listener;
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    }

    @NonNull
    @Override
    public TravelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_travel, parent, false);
        return new TravelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TravelViewHolder holder, int position) {
        Travel travel = travelList.get(position);
        holder.bind(travel, listener);
    }

    @Override
    public int getItemCount() {
        return travelList.size();
    }

    class TravelViewHolder extends RecyclerView.ViewHolder {
        private TextView destinationTextView;
        private TextView dateTextView;
        private TextView amountTextView;
        private TextView statusTextView;

        public TravelViewHolder(@NonNull View itemView) {
            super(itemView);
            destinationTextView = itemView.findViewById(R.id.destinationTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            amountTextView = itemView.findViewById(R.id.amountTextView);
            statusTextView = itemView.findViewById(R.id.statusTextView);
        }

        public void bind(final Travel travel, final OnTravelClickListener listener) {
            destinationTextView.setText(travel.getDestinationCity());
            dateTextView.setText(String.format("%s - %s",
                    dateFormat.format(travel.getStartDate()),
                    dateFormat.format(travel.getEndDate())));
            amountTextView.setText(String.format("$%.2f", travel.getTotalSpent()));
            statusTextView.setText(travel.isStatus() ? "Aprobado" : "Pendiente");

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onTravelClick(travel);
                }
            });
        }
    }
} 