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
import com.pharma.android.ObjectBox;
import com.pharma.android.R;
import com.pharma.android.models.MedicalItem;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.joda.time.PeriodType;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import io.objectbox.Box;

public class MedicalItemFragment extends Fragment {

    private OnListFragmentInteractionListener mListener;
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
        adapter = new MyMedicalItemRecyclerViewAdapter(medicalItemBox.getAll(), mListener);
        recyclerView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        FloatingActionButton fab = getView().findViewById(R.id.fab);
        fab.setOnClickListener(view12 -> {
            //TODO: SHOW dialog
            LayoutInflater li = LayoutInflater.from(getContext());
            View promptsView = li.inflate(R.layout.add_medical_item_dialog, null);

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                    getContext());

            alertDialogBuilder.setView(promptsView);

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
                            Toast.makeText(getContext(), "New Item Added", Toast.LENGTH_SHORT).show();
                            // update UI
                            adapter.setMedicalItems(medicalItemBox.getAll());
                            adapter.notifyDataSetChanged();

                            DateTime expireDate = new DateTime(newMedicalItem.getExpireDate());
                            int notificationTime = new Period(DateTime.now(), expireDate.minusDays(7), PeriodType.millis()).getValue(0);
                            scheduleNotification(getNotification(newMedicalItem), notificationTime);

                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

        });
    }


    private void scheduleNotification(Notification notification, int delay) {

        Intent notificationIntent = new Intent(getContext(), NotificationPublisher.class);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID, 1);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION, notification);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        long futureInMillis = SystemClock.elapsedRealtime() + delay;
        AlarmManager alarmManager = (AlarmManager)getActivity().getSystemService(Context.ALARM_SERVICE);
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

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction(MedicalItem medicalItem);
    }
}
