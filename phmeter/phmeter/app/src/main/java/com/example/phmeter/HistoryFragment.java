// HistoryFragment.java
package com.example.phmeter;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.phmeter.data.AppDatabase;
import com.example.phmeter.data.Measurement;
import java.util.List;


public class HistoryFragment extends Fragment {
    private RecyclerView recyclerHistory;
    private MeasurementAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerHistory = view.findViewById(R.id.recyclerHistory);
        recyclerHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new MeasurementAdapter();
        recyclerHistory.setAdapter(adapter);
        loadHistory();
    }
    public void updateList(List<Measurement> list) {
        adapter.setItems(list);
    }


    private void loadHistory() {
        new Thread(() -> {
            List<Measurement> list = AppDatabase.getInstance(getContext())
                    .measurementDao()
                    .all();
            requireActivity().runOnUiThread(() -> adapter.setItems(list));
        }).start();
    }

}