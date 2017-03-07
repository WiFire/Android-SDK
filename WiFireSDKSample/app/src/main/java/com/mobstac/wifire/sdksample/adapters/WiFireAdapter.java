package com.mobstac.wifire.sdksample.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mobstac.wifire.WiFireHotspot;
import com.mobstac.wifire.sdksample.MainActivity;
import com.mobstac.wifire.sdksample.R;
import com.mobstac.wifire.sdksample.utils.Util;

import java.util.ArrayList;

/**
 * Created by Kislay on 08/08/16.
 */
public class WiFireAdapter extends RecyclerView.Adapter<WiFireAdapter.ViewHolder> {

    private ArrayList<WiFireHotspot> mDataSet = new ArrayList<>();
    public Context mContext;

    public WiFireAdapter(ArrayList<WiFireHotspot> myDataSet, Context context) {
        mDataSet = myDataSet;
        mContext = context;
    }

    @Override
    public WiFireAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.wifire_list_item, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        WiFireHotspot hotspot = mDataSet.get(position);
        holder.name.setText(hotspot.getName());
        holder.extraInfo.setVisibility(View.VISIBLE);
        holder.extraInfo.setText(hotspot.getSsid());
        holder.icon.setImageResource(Util.getWifireSignalIcon(hotspot.getSignalLevel(4)));
    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView name, extraInfo;
        public ImageView icon;
        View divider;

        public ViewHolder(View v) {
            super(v);
            name = (TextView) v.findViewById(R.id.wifi_ssid);
            extraInfo = (TextView) v.findViewById(R.id.wifi_extra);
            icon = (ImageView) v.findViewById(R.id.wifi_icon);
            divider = v.findViewById(R.id.divider);
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (getLayoutPosition() <= mDataSet.size()) {
                WiFireHotspot hotspot = mDataSet.get(getLayoutPosition());
                if (mContext instanceof MainActivity) {
                    MainActivity mainActivity = (MainActivity) mContext;
                    mainActivity.onItemClick(hotspot);
                }
            }
        }
    }

}