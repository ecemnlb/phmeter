package com.example.phmeter;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.phmeter.data.AppDatabase;
import com.example.phmeter.data.Measurement;
import com.example.phmeter.network.EspClient;
import com.example.phmeter.utils.ImageProcessor;
import com.example.phmeter.utils.PhStripProcessor;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;
import java.util.Locale;

public class MeasureFragment extends Fragment {
    private ImageButton btnInfo, btnDebugOverlay;
    private MaterialButton btnLedToggle, btnSave;
    private FloatingActionButton fabCapture;
    private ImageView[] rois = new ImageView[4];
    private TextView textPH;
    private Bitmap lastCapture;
    private float lastPH;
    private boolean ledOn = false;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_measure, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Bind views
        btnInfo      = view.findViewById(R.id.btnInfo);
        btnDebugOverlay = view.findViewById(R.id.btnDebugOverlay);
        btnLedToggle = view.findViewById(R.id.btnLedToggle);
        fabCapture   = view.findViewById(R.id.fabCapture);
        btnSave      = view.findViewById(R.id.btnSave);
        textPH       = view.findViewById(R.id.textPHValue);
        rois[0]      = view.findViewById(R.id.roi1);
        rois[1]      = view.findViewById(R.id.roi2);
        rois[2]      = view.findViewById(R.id.roi3);
        rois[3]      = view.findViewById(R.id.roi4);

        // 1) Info popup
        btnInfo.setOnClickListener(v -> new AlertDialog.Builder(requireContext())
                .setTitle("Wi-Fi Info")
                .setMessage("Make sure you’re on Wi-Fi SSID “pH-Cam” (pw: 12345678)")
                .setPositiveButton("OK", null)
                .show()
        );

        // 2) LED toggle
        btnLedToggle.setOnClickListener(v -> {
            ledOn = !ledOn;
            // change icon
            btnLedToggle.setIconResource(ledOn
                    ? R.drawable.ic_flash_on
                    : R.drawable.ic_flash_off
            );
            // send HTTP
            String url = "http://192.168.4.1/led?state=" + (ledOn ? "on" : "off");
            EspClient.get(url, body -> requireActivity().runOnUiThread(() ->
                    Toast.makeText(getContext(),
                            ledOn ? "LED ON" : "LED OFF",
                            Toast.LENGTH_SHORT).show()
            ));
        });

        // 3) Capture
        fabCapture.setOnClickListener(v -> {
            // append a dummy query param so the URL is unique every time
            String url = "http://192.168.4.1/capture.jpg?ts=" + System.currentTimeMillis();
            EspClient.getImage(url, bmp -> {
                try {
                    if (bmp == null) {
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(getContext(),
                                        "Capture failed",
                                        Toast.LENGTH_SHORT).show()
                        );
                        return;
                    }

                    lastCapture = bmp;

                    // show ROIs
                    for (int i = 0; i < 4; i++) {
                        Bitmap roiBmp = ImageProcessor.cropROI(bmp, i);
                        ImageView iv = rois[i];
                        iv.post(() -> iv.setImageBitmap(roiBmp));
                    }

                    // compute pH
                    PhStripProcessor.ROIResult result = PhStripProcessor.process(bmp);
                    List<float[]> rgbPatches = result.colors;

                    float[] avg = new float[]{0, 0, 0};
                    for (float[] patch : rgbPatches) {
                        avg[0] += patch[0];
                        avg[1] += patch[1];
                        avg[2] += patch[2];
                    }
                    avg[0] /= rgbPatches.size();
                    avg[1] /= rgbPatches.size();
                    avg[2] /= rgbPatches.size();
                    lastPH = ImageProcessor.mapColorToPH(avg[0], avg[1], avg[2]);

                    // update swatches
                    for (int i = 0; i < rgbPatches.size(); i++) {
                        float[] rgb = rgbPatches.get(i);
                        Bitmap swatch = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
                        swatch.eraseColor(android.graphics.Color.rgb(
                                (int) rgb[0],
                                (int) rgb[1],
                                (int) rgb[2]
                        ));
                        //here is the place for squares instead of circle you can make them into a ph stirp type ui
                        Bitmap scaled = Bitmap.createScaledBitmap(swatch, 60, 60, false);
                        int finalI = i;
                        rois[i].post(() -> rois[finalI].setImageBitmap(scaled));
                    }

                    // final average & display pH
                    avg = ImageProcessor.avgColor(bmp);
                    lastPH = ImageProcessor.mapColorToPH(avg[0], avg[1], avg[2]);
                    textPH.post(() ->
                            textPH.setText(String.format(Locale.US, "pH: %.2f", lastPH))
                    );

                } catch (Exception e) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(),
                                    "Error processing image",
                                    Toast.LENGTH_SHORT).show()
                    );
                    e.printStackTrace();
                }
            });
        });

        // 4) Save
        btnDebugOverlay.setOnClickListener(v -> {
            if (lastCapture == null) {
                Toast.makeText(getContext(), "No image to debug", Toast.LENGTH_SHORT).show();
                return;
            }
            Bitmap overlay = PhStripProcessor.process(lastCapture).overlay;
            ImageView debugView = new ImageView(getContext());
            debugView.setImageBitmap(overlay);
            new AlertDialog.Builder(getContext())
                    .setTitle("Debug View")
                    .setView(debugView)
                    .setPositiveButton("OK", null)
                    .show();
        });

        btnSave.setOnClickListener(v -> {
            if (lastCapture == null) {
                Toast.makeText(getContext(), "No measurement to save", Toast.LENGTH_SHORT).show();
                return;
            }
            new Thread(() -> {
                // 1) Veriyi kaydet
                Measurement m = new Measurement();
                m.pH        = lastPH;
                m.timestamp = System.currentTimeMillis();
                AppDatabase.getInstance(getContext())
                        .measurementDao()
                        .insert(m);

                // 2) Kayıttan hemen sonra tüm listeyi yeniden çek
                List<Measurement> list = AppDatabase.getInstance(getContext())
                        .measurementDao()
                        .all();

                // 3) UI thread’te adapter’a set et ve fragment’taki RecyclerView’i güncelle
                requireActivity().runOnUiThread(() -> {
                    // eğer history fragment açıksa
                    HistoryFragment hf = (HistoryFragment) getParentFragmentManager()
                            .findFragmentByTag("HistoryFragmentTag");
                    if (hf != null) {
                        hf.updateList(list);
                    }
                    Toast.makeText(getContext(), "Measurement saved", Toast.LENGTH_SHORT).show();
                });
            }).start();
        });
    }
}
