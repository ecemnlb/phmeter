// MeasurementAdapter.java
package com.example.phmeter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.phmeter.data.Measurement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import com.example.phmeter.data.AppDatabase;
import com.example.phmeter.data.Measurement;


class MeasurementAdapter extends RecyclerView.Adapter<MeasurementAdapter.ViewHolder> {
    private List<Measurement> items = new ArrayList<>();

    public void setItems(List<Measurement> newItems) {
        items = newItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Measurement m = items.get(position);
        holder.phText.setText(String.format(Locale.US, "pH: %.2f", m.pH));
        holder.dateText.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
                .format(new Date(m.timestamp)));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView phText, dateText;
        ViewHolder(View itemView) {
            super(itemView);
            phText = itemView.findViewById(R.id.textItemPH);
            dateText = itemView.findViewById(R.id.textItemDate);
        }
    }
}