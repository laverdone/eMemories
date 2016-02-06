package com.glm.utilities.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.glm.labs.diary.ememories.R;


/**Adapter per la gestione della lista della spase sia raggruppata che espansa*/
public class ImageFilterListAdapter extends BaseAdapter {
    private Context mContext;
    private String[] mImageFilterList;

    public ImageFilterListAdapter(Context context, String[] imageFilterList){
        mContext=context;
        mImageFilterList =imageFilterList;
    }

    @Override
    public int getCount() {
        return mImageFilterList.length;
    }

    @Override
    public Object getItem(int position) {
        return mImageFilterList[position];
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }


    public String getProduct(int position) {
        return mImageFilterList[position];
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        final String mImageFilter = (String) getItem(position);

        //if (convertView == null) {
        LayoutInflater infalInflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = infalInflater.inflate(R.layout.image_filter_item_list, null);
        //}
        TextView txtImageFilter     = (TextView) convertView.findViewById(R.id.imageFilterText);
        txtImageFilter.setText(mImageFilter);
        txtImageFilter.setTextColor(Color.BLACK);
        return convertView;
    }
}
