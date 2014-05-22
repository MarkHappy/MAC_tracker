package com.peterfile.mac_tracker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class WifiListAdapter extends ArrayAdapter<AccessPoint> {

	private ArrayList<AccessPoint> items;
	private Context adapterContext;

	public WifiListAdapter(Context context, ArrayList<AccessPoint> items) {
		super(context, R.layout.list_item, items);
		adapterContext = context;
		this.items = items;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		try {
			AccessPoint ap = items.get(position);
			if (v == null) {
				LayoutInflater vi = (LayoutInflater) adapterContext
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.list_item, null);
			}
			TextView tvEssid = (TextView) v.findViewById(R.id.textView_List_essid);
			TextView tvBssid = (TextView) v.findViewById(R.id.textView_List_bssid);
			TextView tvPower = (TextView) v.findViewById(R.id.textView_List_power);

			tvEssid.setText(ap.getApEssid());
			tvBssid.setText(ap.getApBssid());
			tvPower.setText(String.valueOf(ap.getApPowerLevel()));
		} catch (Exception e) {
			e.printStackTrace();
			e.getCause();
		}
		return v;
	}

	/** Sort access points by power level */
	public void sortByPowerAsc() {
		Comparator<AccessPoint> comparator = new Comparator<AccessPoint>() {

			@Override
			public int compare(AccessPoint object1, AccessPoint object2) {
				return ((Integer) object1.getApPowerLevel()).compareTo((Integer) object2.getApPowerLevel());
			}
		};
		Collections.sort(items, comparator);
		notifyDataSetChanged();
	}
	
	/** Sort access points by power level */
	public void sortByPowerDsc() {
		Comparator<AccessPoint> comparator = new Comparator<AccessPoint>() {

			@Override
			public int compare(AccessPoint object1, AccessPoint object2) {
				return ((Integer) object2.getApPowerLevel()).compareTo((Integer) object1.getApPowerLevel());
			}
		};
		Collections.sort(items, comparator);
		notifyDataSetChanged();
	}

} // WifiListAdapter
