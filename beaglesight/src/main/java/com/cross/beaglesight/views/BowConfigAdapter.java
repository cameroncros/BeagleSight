package com.cross.beaglesight.views;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.cross.beaglesight.R;
import com.cross.beaglesightlibs.BowConfig;

import java.util.List;

import androidx.annotation.NonNull;

public class BowConfigAdapter implements SpinnerAdapter {
    List<BowConfig> bowConfigs;

    public BowConfigAdapter(@NonNull List<BowConfig> objects) {
        bowConfigs = objects;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getView(position, convertView, parent);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            View newView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.bowlist_item, parent, false);
            newView.setBackgroundColor(parent.getResources().getColor(R.color.colorPrimaryDark));
            convertView = newView;
        }

        TextView mNameView = convertView.findViewById(R.id.itemName);
        TextView mDescriptionView = convertView.findViewById(R.id.itemDescription);

        if (position == 0) {
            mNameView.setText(R.string.no_bow_selected);
            mDescriptionView.setText("");
            return convertView;
        }
        BowConfig bc = bowConfigs.get(position-1);
        mNameView.setText(bc.getName());
        mNameView.setText(bc.getDescription());
        return convertView;
    }

    @Override
    public int getItemViewType(int i) {
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver dataSetObserver) {

    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver dataSetObserver) {

    }

    @Override
    public int getCount()
    {
        return bowConfigs.size() + 1;
    }

    @Override
    public Object getItem(int i) {
        if (i == 0) {
            return null;
        }
        return bowConfigs.get(i-1);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }
}
