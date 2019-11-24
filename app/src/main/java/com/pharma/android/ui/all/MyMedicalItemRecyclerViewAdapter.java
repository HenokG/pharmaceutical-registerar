package com.pharma.android.ui.all;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.pharma.android.R;
import com.pharma.android.models.MedicalItem;
import com.pharma.android.ui.all.MedicalItemFragment.OnListFragmentInteractionListener;

import org.joda.time.DateTime;
import org.joda.time.Days;

import java.util.List;

public class MyMedicalItemRecyclerViewAdapter extends RecyclerView.Adapter<MyMedicalItemRecyclerViewAdapter.ViewHolder> {

    private List<MedicalItem> medicalItems;
    private final OnListFragmentInteractionListener mListener;

    public MyMedicalItemRecyclerViewAdapter(List<MedicalItem> items, OnListFragmentInteractionListener listener) {
        System.out.println("medapp: " + items.size());
        medicalItems = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_medicalitem, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        MedicalItem medicalItem = medicalItems.get(position);
        holder.medicalItem = medicalItem;
        holder.itemNumber.setText(String.valueOf(medicalItem.getQuantity()));
        holder.itemName.setText(medicalItem.getName());
        holder.itemExpirationDate.setText(medicalItem.getExpireDateString());

        Days daysLeftToExpire = Days.daysBetween(DateTime.now(), new DateTime(medicalItem.getExpireDate()));

        holder.itemDaysLeftToExpire.setText("( " + daysLeftToExpire.getDays() + " days ) ");

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.medicalItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return medicalItems.size();
    }

    public void setMedicalItems(List<MedicalItem> medicalItems) {
        this.medicalItems = medicalItems;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView itemNumber;
        public final TextView itemName;
        public final TextView itemExpirationDate;
        public final TextView itemDaysLeftToExpire;
        public MedicalItem medicalItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            itemNumber = view.findViewById(R.id.item_number);
            itemName = view.findViewById(R.id.input_name);
            itemExpirationDate = view.findViewById(R.id.expiration_date);
            itemDaysLeftToExpire = view.findViewById(R.id.expiration_date_in_numberof_days);
        }
    }
}
