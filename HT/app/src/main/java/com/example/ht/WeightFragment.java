package com.example.ht;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jjoe64.graphview.GraphView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

public class WeightFragment extends Fragment {

    View view;
    MainActivity main;
    EntryManager em = EntryManager.getInstance();
    UserManager um = UserManager.getInstance();
    ArrayList<String> date_log = new ArrayList<>();
    ArrayList<Double> weight_log = new ArrayList<>();
    SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");

    GraphView graph;
    EditText edit_weight;
    EditText edit_date;
    TextView text_reminder;
    Button button_new_entry;
    RecyclerView recycler_log;
    Button button_export;

    double weight;
    Date selected_date;

    public WeightFragment(MainActivity main) { this.main = main; }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_weight, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        updateLog();

        graph = (GraphView) view.findViewById(R.id.graph);
        GraphManager gm = new GraphManager(main);
        gm.updateWeightGraph(graph);

        edit_weight = (EditText) view.findViewById(R.id.editWeight);
        edit_date = (EditText) view.findViewById(R.id.editDateWeight);
        edit_date.setShowSoftInputOnFocus(false);
        recycler_log = (RecyclerView) view.findViewById(R.id.recyclerLogWeight);
        text_reminder = (TextView) view.findViewById(R.id.textReminderWeight);
        button_new_entry = (Button) view.findViewById(R.id.buttonAddWeight);
        button_export = (Button) view.findViewById(R.id.buttonExport);

        edit_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDateDialog();
            }
        });

        // Adding a new entry
        button_new_entry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                weight = Double.parseDouble(edit_weight.getText().toString());
                if (weight > 0 && weight < 450) {
                    if (selected_date != null) {
                        em.setEntry(new WeightEntry(weight));
                        em.getEntry().setDate(selected_date);
                        um.getUser().addEntry(em.getEntry());
                        em.sortEntries(um.getUser().getEntries(1));
                        um.setUserWeight();
                        updateLog();
                        gm.updateWeightGraph(graph);
                        setRecyclerAdapter();
                        resetInputs();
                        text_reminder.setText("");
                        um.saveUsers(main.getApplicationContext());
                    } else {
                        text_reminder.setText("Please select a date");
                    }
                } else {
                    text_reminder.setText("Please set a valid weight");
                }
            }
        });
        button_export.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                em.writeJSON(1, main.getApplicationContext());
            }
        });
        setRecyclerAdapter();
    }

    // This method shows the date picker
    private void showDateDialog() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
        DatePickerDialog.OnDateSetListener dsl = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);

                edit_date.setText(sdf.format(calendar.getTime()));
                selected_date = calendar.getTime();
            }
        };

        new DatePickerDialog(getContext(), dsl, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    // This method sets up the recycler adapter for the log's recycler view
    private void setRecyclerAdapter() {
        RecyclerAdapter adapter = new RecyclerAdapter(date_log, weight_log);
        RecyclerView.LayoutManager lm = new LinearLayoutManager(getContext());
        recycler_log.setLayoutManager(lm);
        recycler_log.setItemAnimator(new DefaultItemAnimator());
        recycler_log.setAdapter(adapter);
    }

    // This method resets the helper arraylists used for populating the log's recycler view
    private void updateLog() {
        date_log.clear();
        weight_log.clear();
        if (um.getUser().getEntries(1).size() > 0) {
            for (Entry e : um.getUser().getEntries(1)) {
                if (e instanceof WeightEntry) {
                    date_log.add(sdf.format(e.getDate()));
                    weight_log.add(((WeightEntry) e).getWeight());
                }
            }
        }
        Collections.reverse(date_log);
        Collections.reverse(weight_log);
    }

    // This method resets the input fields and the selected date variable
    private void resetInputs() {
        edit_date.setText("");
        edit_weight.setText("");
        selected_date = null;
    }
}