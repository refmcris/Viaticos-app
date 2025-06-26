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

import co.edu.univalle.viaticos.data.entity.Invoice;

public class RecordAdapter extends RecyclerView.Adapter<RecordAdapter.RecordViewHolder> {
    private List<Invoice> invoiceList;
    private SimpleDateFormat dateFormat;

    public RecordAdapter(List<Invoice> invoiceList) {
        this.invoiceList = invoiceList;
        this.dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
    }

    @NonNull
    @Override
    public RecordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_record, parent, false);
        return new RecordViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecordViewHolder holder, int position) {
        Invoice invoice = invoiceList.get(position);
        holder.bind(invoice);
    }

    @Override
    public int getItemCount() {
        return invoiceList.size();
    }

    public void updateList(List<Invoice> newList) {
        this.invoiceList = newList;
        notifyDataSetChanged();
    }

    class RecordViewHolder extends RecyclerView.ViewHolder {
        private TextView recordDateTextView;
        private TextView recordAmountTextView;
        private TextView recordDescriptionTextView;

        public RecordViewHolder(@NonNull View itemView) {
            super(itemView);
            recordDateTextView = itemView.findViewById(R.id.recordDateTextView);
            recordAmountTextView = itemView.findViewById(R.id.recordAmountTextView);
            recordDescriptionTextView = itemView.findViewById(R.id.recordDescriptionTextView);
        }

        public void bind(final Invoice invoice) {
            recordDateTextView.setText(dateFormat.format(invoice.getDate()));
            recordAmountTextView.setText(String.format(Locale.getDefault(), "$%,.2f", invoice.getAmount()));
            recordDescriptionTextView.setText(invoice.getDescription());
        }
    }
} 