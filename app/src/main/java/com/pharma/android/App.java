package com.pharma.android;

import android.app.Application;
import com.pharma.android.models.MedicalItem;

import net.danlew.android.joda.JodaTimeAndroid;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import io.objectbox.Box;

public class App extends Application {

    public static App app;

    @Override
    public void onCreate() {
        super.onCreate();
        ObjectBox.init(this);
        JodaTimeAndroid.init(this);

//        Box<MedicalItem> medicalItemBox = ObjectBox.get().boxFor(MedicalItem.class);
//        medicalItemBox.removeAll();
//        ArrayList<MedicalItem> medicalItemArrayList = new ArrayList<>();
//        try {
//            Date date = new SimpleDateFormat("dd/MM/yyyy").parse("01/12/2019");
//            for (int i = 0; i < 25; i++) {
//                medicalItemArrayList.add(new MedicalItem("Morphin " + i, i, date));
//            }
//            medicalItemBox.put(medicalItemArrayList);
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }

        app = this;
    }

    public static App getApp() {
        return app;
    }

}
