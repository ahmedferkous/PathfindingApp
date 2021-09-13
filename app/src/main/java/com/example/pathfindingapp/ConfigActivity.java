package com.example.pathfindingapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.material.slider.RangeSlider;
import com.google.android.material.slider.Slider;

import java.util.ArrayList;

public class ConfigActivity extends AppCompatActivity implements GridAutofitLayoutManager.SpanCountUpdatedResult {
    private static final String TAG = "ConfigActivity";
    public static final String TYPE_ALGORITHM = "type_algorithm";
    public static final String TYPE_SPAN_COUNT = "type_span_count";
    public static final String TYPE_ROW_COUNT = "type_row_count";
    public static final String TYPE_MILLI_INCREMENT = "type_milli_increment";
    public static final String A_STAR = "A_STAR";
    public static final String DIJKSTRA = "DIJKSTRA";

    @Override
    public void onSpanCountResult(int spanCount) {
        mainRelLayout.setVisibility(View.GONE);
        pickerRelLayout.setVisibility(View.VISIBLE);
        this.spanCount = spanCount;
        Log.d(TAG, "onSpanCountResult: " + spanCount);
    }

    private TextView txtNumberOfRows;
    private Slider slider;
    private RadioGroup groupRadio;
    private EditText edtTxtMilli;
    private RecyclerView recyclerView;
    private NodeAdapter adapter;
    private RelativeLayout mainRelLayout, pickerRelLayout;
    private Button btnSubmit;
    private int spanCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);
        initViews();

        slider.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                String valueStr = String.valueOf((int) (value));
                txtNumberOfRows.setText("Number Of Rows (" + valueStr + ")");
            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public void onClick(View v) {
                int numberOfRows = (int) slider.getValue();
                String milli = edtTxtMilli.getText().toString();
                int milliIncrement;
                String type = "";

                if (milli.equals("")) {
                    milliIncrement = 1000;
                } else {
                    milliIncrement = Integer.parseInt(milli);
                }

                switch (groupRadio.getCheckedRadioButtonId()) {
                    case R.id.radio_astar:
                        type = A_STAR;
                        break;
                    case R.id.radio_dijk:
                        type = DIJKSTRA;
                        break;
                    default:
                        break;
                }

                Intent intent = new Intent(ConfigActivity.this, PathfindingActivity.class);
                intent.putExtra(TYPE_SPAN_COUNT, spanCount);
                intent.putExtra(TYPE_ROW_COUNT, numberOfRows);
                intent.putExtra(TYPE_ALGORITHM, type);
                intent.putExtra(TYPE_MILLI_INCREMENT, milliIncrement);
                startActivity(intent);
            }
        });

    }

    private void initViews() {
        setTitle("Configuration");
        edtTxtMilli = findViewById(R.id.edtTxtMilli);
        txtNumberOfRows = findViewById(R.id.txtNumberOfRows);
        slider = findViewById(R.id.slider);
        groupRadio = findViewById(R.id.groupRadio);
        recyclerView = findViewById(R.id.recView);
        mainRelLayout = findViewById(R.id.mainRelLayout);
        pickerRelLayout = findViewById(R.id.pickRelLayout);
        btnSubmit = findViewById(R.id.btnSubmit);
        adapter = new NodeAdapter(this);

        GridAutofitLayoutManager manager = new GridAutofitLayoutManager(this, 65, this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(manager);
        adapter.setNodes(new ArrayList<>());
    }
}