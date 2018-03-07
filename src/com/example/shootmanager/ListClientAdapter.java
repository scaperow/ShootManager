package com.example.shootmanager;

import java.util.List;
import java.util.Map;

import com.example.shootmanager.Device.DeviceStatus;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ListClientAdapter extends BaseAdapter {

	private LayoutInflater inflater;
	private List<Device> data;
	private Context context;

	public ListClientAdapter(Context context, List<Device> data) {
		this.context = context;
		this.inflater = LayoutInflater.from(context);
		this.data = data;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return data.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ContactViewHolder holder = null;
		if (convertView == null) {

			holder = new ContactViewHolder();
			convertView = inflater.inflate(R.layout.layout_client, null);
			holder.client_location = (TextView) convertView
					.findViewById(R.id.client_location);
			holder.client_power = (TextView) convertView
					.findViewById(R.id.client_power);
			holder.client_WIFI = (TextView) convertView
					.findViewById(R.id.client_WIFI);
			holder.client_rings = (TextView) convertView
					.findViewById(R.id.client_rings);
			holder.client_shoots = (TextView) convertView
					.findViewById(R.id.client_shoots);
			holder.client_ring = (TextView) convertView
					.findViewById(R.id.client_ring);
			holder.client_status = (TextView) convertView
					.findViewById(R.id.client_status);

			convertView.setTag(holder);
		} else {
			holder = (ContactViewHolder) convertView.getTag();
		}

		final Device device = data.get(position);
		String status = "";
		if (device.Status == DeviceStatus.Init) {
			status = "正在初始化";
		} else if (device.Status == DeviceStatus.AutoZooming) {
			status = "正在对焦";
		} else if (device.Status == DeviceStatus.Closed) {
			status = "正在关闭";
		} else if (device.Status == DeviceStatus.NoTargetFound) {
			status = "没有找到靶环";
		} else if (device.Status == DeviceStatus.Pausing) {
			status = "已停止";
		} else if (device.Status == DeviceStatus.Reset) {
			status = "重置";
		} else if (device.Status == DeviceStatus.Shooting) {
			status = "正在射击";
		} else if (device.Status == DeviceStatus.TargetMatched) {
			status = "已找到靶环";
		}
		
		holder.client_location.setText(device.Location);
		holder.client_power.setText(device.Power.toString());
		holder.client_WIFI.setText(device.WIFI.toString());
		holder.client_rings.setText(String.format("%2s",
				device.LastScore.Rings.toString()));
		holder.client_shoots.setText(String.format("%2s",
				device.LastScore.Shoots.toString()));
		holder.client_ring.setText(String.format("%2s",
				device.LastScore.Ring.toString()));
		holder.client_status.setText(status);
		convertView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				RingActivity.device = device;
				context.startActivity(new Intent().setClass(context,
						RingActivity.class));
			}
		});

		return convertView;
	}
}

final class ContactViewHolder {
	public TextView client_location;
	public TextView client_power;
	public TextView client_WIFI;
	public TextView client_shoots;
	public TextView client_ring;
	public TextView client_rings;
	public TextView client_status;
}
