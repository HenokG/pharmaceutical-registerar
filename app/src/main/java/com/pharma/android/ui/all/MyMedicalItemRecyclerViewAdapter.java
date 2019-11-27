package com.pharma.android.ui.all;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.pharma.android.ObjectBox;
import com.pharma.android.R;
import com.pharma.android.models.MedicalItem;

import org.joda.time.DateTime;
import org.joda.time.Days;

import java.util.Collections;
import java.util.List;

import io.objectbox.Box;

public class MyMedicalItemRecyclerViewAdapter extends RecyclerView.Adapter<MyMedicalItemRecyclerViewAdapter.ViewHolder> {

    private List<MedicalItem> medicalItems;
    private Box<MedicalItem> medicalItemBox;

    public MyMedicalItemRecyclerViewAdapter(List<MedicalItem> items) {
        // we are doing some manipulations on the list so
        // we rather do it in one place for the sake of D.R.Y
        this.setMedicalItems(items);
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

        medicalItemBox = ObjectBox.get().boxFor(MedicalItem.class);

        holder.mView.setOnClickListener(v -> {
            medicalItemEditPopUp(holder, position);
        });

        holder.mView.setOnLongClickListener(v -> {
            MedicalItem medicalItemToDelete = medicalItems.get(position);
            new AlertDialog.Builder(v.getContext())
                    .setTitle("Delete " + medicalItemToDelete.getName())
                    .setMessage("Are you sure?")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.yes, (dialog, whichButton) -> {
                        medicalItems.remove(medicalItemToDelete);
                        medicalItemBox.remove(medicalItemToDelete.getId());
                        setMedicalItems(medicalItems);
                        notifyDataSetChanged();
                        Toast.makeText(v.getContext(), "Item Deleted!", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton(android.R.string.no, null).show();
            return false;
        });
    }

    private void medicalItemEditPopUp(ViewHolder holder, int position) {
        MedicalItem clickedMedicalItem = medicalItems.get(position);
        LayoutInflater li = LayoutInflater.from(holder.mView.getContext());
        View promptsView = li.inflate(R.layout.medical_item_dialog, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                holder.mView.getContext());

        alertDialogBuilder.setView(promptsView);

        final TextView dialogTitle = promptsView
                .findViewById(R.id.dialog_title);

        dialogTitle.setText("Edit Medical Item");

        final EditText inputName = promptsView
                .findViewById(R.id.input_name);

        final EditText inputQuantity = promptsView
                .findViewById(R.id.input_quantity);

        final EditText inputExpireDate = promptsView
                .findViewById(R.id.input_expiration_date);

        inputName.setText(clickedMedicalItem.getName());
        inputQuantity.setText(String.valueOf(clickedMedicalItem.getQuantity()));
        // hide expiration date
        inputExpireDate.setVisibility(View.INVISIBLE);

        alertDialogBuilder.setCancelable(false).setPositiveButton("Save", (dialog, id) -> {
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
                } else {
                    clickedMedicalItem.setName(inputName.getText().toString());
                    clickedMedicalItem.setQuantity(Integer.valueOf(inputQuantity.getText().toString()));
                    // update database
                    medicalItemBox.put(clickedMedicalItem);
                    alertDialog.dismiss();
                    Toast.makeText(holder.mView.getContext(), "Changes Saved!", Toast.LENGTH_SHORT).show();
                    // update UI
                    MyMedicalItemRecyclerViewAdapter.this.notifyItemChanged(position);

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
        // reverse list to cause a desc sort of items
        Collections.reverse(this.medicalItems);
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
