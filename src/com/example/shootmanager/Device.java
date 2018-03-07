package com.example.shootmanager;

import java.io.IOException;
import java.net.InetAddress;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.util.EntityUtils;

import android.graphics.Point;
import android.util.Log;

public class Device {

	public InetAddress Address;
	public String Location;
	public Integer Power;
	public Integer WIFI;
	public Score LastScore;
	public DeviceStatus Status;
	private static int NetworkRequestTimeout = 1000 * 10;
	private static int NetworkRequestSoTimeout = 1000 * 5;
	private static int ServicePort = 8000;
	public Point Point;
	public static String tag = "ShootManager";

	public enum DeviceStatus {
		Init, AutoZooming, NoTargetFound, TargetMatched, Shooting, Pausing, Closed, Reset
	};

	private static String getResponse(InetAddress address, String method) {

		final BasicHttpParams parameter = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(parameter,
				NetworkRequestTimeout);
		HttpClient http = new DefaultHttpClient(parameter);

		String uri = String.format("http://%s:%d/%s", address.getHostAddress(),
				ServicePort, method);
		Log.d(tag, "Connect to " + uri);

		HttpGet request = new HttpGet(uri);
		HttpResponse response = null;
		try {
			response = http.execute(request);

			if (response != null) {
				HttpEntity entity = response.getEntity();
				return EntityUtils.toString(entity);
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	public void pause() {
		getResponse(Address, Commander.PAUSE);
	}

	public void reset() {
		getResponse(Address, Commander.RESET);
	}

	public void clear() {
		getResponse(Address, Commander.CLEAR);
	}

	public void resume() {
		getResponse(Address, Commander.RESUME);

	}

	public void update() {
		
	}

	public static Device getDevice(InetAddress address) {

		String response = getResponse(address, Commander.GETVALUE);

		Device device = parseDevice(address, response);

		return device;
	}

	public static Device parseDevice(InetAddress address, String response) {

		Device device = new Device();
		device.Address = address;
		if (parseDevice(response, device)) {
			return device;
		} else {
			return null;
		}
	}

	public static boolean parseDevice(String response, Device device) {
		if (response == null) {
			return false;
		}

		String[] values = response.split(",");
		if (values.length != 6) {
			return false;
		}

		try {
			device.Location = "1";
			device.LastScore = new Score();
			device.LastScore.Shoots = Integer.parseInt(values[0]);
			device.LastScore.Ring = Integer.parseInt(values[1]);
			device.LastScore.Rings = Integer.parseInt(values[2]);
			device.Status = (DeviceStatus.valueOf(values[3]));
			device.Power = Integer.parseInt(values[4]);
			device.WIFI = Integer.parseInt(values[5]);

			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	public static Score parseScore(String response) {
		String[] values = response.split(",");
		if (values.length != 5) {
			return null;
		}

		Score score = new Score();

		try {
			score.Shoots = Integer.parseInt(values[0]);
			score.Ring = Integer.parseInt(values[1]);
			score.Rings = Integer.parseInt(values[2]);
			score.LastX = Double.parseDouble(values[3]);
			score.LastY = Double.parseDouble(values[4]);
		} catch (Exception e) {
			return null;
		}

		return score;
	}

	public boolean refresh() {
		// TODO Auto-generated method stub
		String response = getResponse(Address, Commander.GETVALUE);

		return parseDevice(response, this);
	}

	public void updatePoint(int width, int height) {
		// TODO Auto-generated method stub
		String response = getResponse(Address, Commander.POINT + "/" + width
				+ "/" + height);
		String[] value = response.split(",");
		if (value.length == 2) {
			try {
				Double x = Double.parseDouble(value[0]);
				Double y = Double.parseDouble(value[1]);
				
				Point = new Point(x.intValue(), y.intValue());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}

class Commander {
	public static String GETVALUE = "getvalue";
	public static String PAUSE = "pause";
	public static String RESUME = "resume";
	public static String CLEAR = "clear";
	public static String RESET = "reset";
	public static String RED = "red";
	public static String POINT = "point";
}
