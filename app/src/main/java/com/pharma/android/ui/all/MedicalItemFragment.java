package com.pharma.android.ui.all;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.pharma.android.MainActivity;
import com.pharma.android.NotificationPublisher;
import com.pharma.android.ObjectBox;
import com.pharma.android.R;
import com.pharma.android.models.MedicalItem;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.LocalDate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import io.objectbox.Box;

public class MedicalItemFragment extends Fragment {

    private DatePickerDialog picker;
    private MyMedicalItemRecyclerViewAdapter adapter;
    private Box<MedicalItem> medicalItemBox;
    private RecyclerView recyclerView;

    public MedicalItemFragment() {
    }

    public static MedicalItemFragment newInstance() {
        return new MedicalItemFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_medicalitem_list, container, false);

        recyclerView = view.findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        medicalItemBox = ObjectBox.get().boxFor(MedicalItem.class);
        List<MedicalItem> medicalItems = medicalItemBox.getAll();
        adapter = new MyMedicalItemRecyclerViewAdapter(medicalItems);
        recyclerView.setAdapter(adapter);
        // hide instruction based on number of items
        View instructions = view.findViewById(R.id.instructions);
        instructions.setVisibility(medicalItems.size() > 0 ? View.GONE : View.VISIBLE);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        FloatingActionButton fab = getView().findViewById(R.id.fab);
        fab.setOnClickListener(view12 -> {
            LayoutInflater li = LayoutInflater.from(getContext());
            View promptsView = li.inflate(R.layout.medical_item_dialog, null);

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                    getContext());

            alertDialogBuilder.setView(promptsView);

            final TextView dialogTitle = promptsView
                    .findViewById(R.id.dialog_title);

            dialogTitle.setText("Add Medical Item");

            final EditText inputName = promptsView
                    .findViewById(R.id.input_name);

            final EditText inputQuantity = promptsView
                    .findViewById(R.id.input_quantity);

            final EditText inputExpireDate = promptsView
                    .findViewById(R.id.input_expiration_date);

            inputExpireDate.setOnClickListener(v -> {
                picker = new DatePickerDialog(getContext(),
                        (view1, year, monthOfYear, dayOfMonth) ->
                                inputExpireDate.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year),
                        new LocalDate().getYear(), DateTime.now().getMonthOfYear() - 1, new LocalDate().getDayOfMonth());
                picker.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
                picker.show();
            });

            alertDialogBuilder.setCancelable(false).setPositiveButton("Add", (dialog, id) -> {
            })
                    .setNegativeButton("Cancel", (dialog, id) -> dialog.cancel());

            // create alert dialog
            AlertDialog alertDialog = alertDialogBuilder.create();

            // show it
            alertDialog.show();
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (inputName.getText().length() <= 0) {
                        inputName.setError("Please provide a proper name!");
                    } else if (inputQuantity.getText().length() <= 0) {
                        inputQuantity.setError("Please provide a proper quantity!");
                    } else if (inputExpireDate.getText() == null) {
                        inputExpireDate.setError("Please provide a proper expire date!");
                    } else {
                        try {
                            MedicalItem newMedicalItem = new MedicalItem(inputName.getText().toString(), Integer.valueOf(inputQuantity.getText().toString()),
                                    new SimpleDateFormat("dd/MM/yyyy").parse(inputExpireDate.getText().toString()));
                            medicalItemBox.put(newMedicalItem);
                            alertDialog.dismiss();
                            Toast.makeText(getContext(), "New Item Added!", Toast.LENGTH_SHORT).show();
                            // update UI
                            adapter.setMedicalItems(medicalItemBox.getAll());
                            adapter.notifyDataSetChanged();

                            scheduleNotification(getNotification(newMedicalItem), newMedicalItem);

                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

        });
        for (MedicalItem medicalItem :
                medicalItemBox.getAll()) {
            scheduleNotification(getNotification(medicalItem), medicalItem);
        }
    }

    private void scheduleNotification(Notification notification, MedicalItem medicalItem) {
        DateTime expireDate = new DateTime(medicalItem.getExpireDate());

        DateTime weekBeforeExpireDate = expireDate.minusDays(7);
        if (weekBeforeExpireDate.isBeforeNow()) return;

        Interval delay = new Interval(DateTime.now(), weekBeforeExpireDate);
        long futureInMillis = SystemClock.elapsedRealtime() + delay.toDurationMillis();

        Intent notificationIntent = new Intent(getContext(), NotificationPublisher.class);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID, 1);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION, notification);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis, pendingIntent);
    }

    private Notification getNotification(MedicalItem newMedicalItem) {

        NotificationManager mNotificationManager;
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(getContext().getApplicationContext(), "notify_001");
        Intent ii = new Intent(getContext().getApplicationContext(), MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(getActivity().getApplicationContext(), 0, ii, 0);

        NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle();
        bigText.bigText("Notification Group");
        bigText.setBigContentTitle("Grouped Notifications");
        bigText.setSummaryText("Item Expiring Soon");

        mBuilder.setContentIntent(pendingIntent);
        mBuilder.setContentTitle("An Item With " + newMedicalItem.getQuantity() + " Quantity is Expiring in a Week");
        mBuilder.setContentText(newMedicalItem.getName() + " is expiring.");
        mBuilder.setSmallIcon(R.drawable.ic_notifications_active_black_24dp);
        mBuilder.setContentIntent(pendingIntent);
        mBuilder.setPriority(Notification.PRIORITY_MAX);
        mBuilder.setStyle(bigText);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "Your_channel_id";
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Notification Channel",
                    NotificationManager.IMPORTANCE_DEFAULT);
            mBuilder.setChannelId(channelId);
        }

        return mBuilder.build();
    }
}
