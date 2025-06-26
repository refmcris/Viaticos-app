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
    private List<Travel> travels;
    private OnTravelClickListener listener;

    public interface OnTravelClickListener {
        void onTravelClick(Travel travel);
    }

    public TravelAdapter(List<Travel> travels, OnTravelClickListener listener) {
        this.travels = travels;
        this.listener = listener;
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
        Travel travel = travels.get(position);
        holder.bind(travel, listener);
    }

    @Override
    public int getItemCount() {
        return travels != null ? travels.size() : 0;
    }

    public void updateTravels(List<Travel> newTravels) {
        this.travels = newTravels;
        notifyDataSetChanged();
    }

    static class TravelViewHolder extends RecyclerView.ViewHolder {
        private TextView cityName;
        private TextView dateRange;
        private TextView budget;

        public TravelViewHolder(@NonNull View itemView) {
            super(itemView);
            cityName = itemView.findViewById(R.id.cityName);
            dateRange = itemView.findViewById(R.id.dateRange);
            budget = itemView.findViewById(R.id.budget);
        }

        public void bind(Travel travel, OnTravelClickListener listener) {
            cityName.setText(travel.getDestinationCity());
            
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
            String dateRangeText = String.format("%s - %s",
                    dateFormat.format(travel.getStartDate()),
                    dateFormat.format(travel.getEndDate()));
            dateRange.setText(dateRangeText);
            
            budget.setText(String.format(Locale.getDefault(), "$%,.2f", travel.getTravelBudget()));

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTravelClick(travel);
                }
            });
        }
    }
} 