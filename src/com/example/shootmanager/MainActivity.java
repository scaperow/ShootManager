package com.example.shootmanager;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.util.ByteArrayBuffer;
import org.apache.http.util.EntityUtils;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.GridLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private int ipStart = 250;
	private int ServicePort = 8000;
	private HashSet Connections;
	private Resources resources;

	ListClientAdapter adapter_clients;
	Button button_clear, button_reset, button_suppend;
	ProgressBar progress_icon;
	TextView text_progress;
	EditText text;
	ListView list_clients;
	List<Device> devices = new LinkedList<Device>();
	private ExecutorService threadPools = Executors.newFixedThreadPool(1);

	Handler setMessage = new Handler() {
		public void handleMessage(Message message) {
			text.append(message.getData().getString("message"));
		}
	};

	Handler invokeHandler = new Handler() {
	};

	private void setMessageToView(String message) {
		Bundle bundle = new Bundle();
		bundle.putString("message", message);

		Message m = new Message();
		m.setData(bundle);

		setMessage.sendMessage(m);
	}

	private void setControls() {
		resources = getResources();
		list_clients = (ListView) this.findViewById(R.id.list_clients);

		adapter_clients = new ListClientAdapter(MainActivity.this, devices);
		list_clients.setAdapter(adapter_clients);

		button_clear = (Button) this.findViewById(R.id.button_clear);
		button_reset = (Button) this.findViewById(R.id.button_reset);
		button_suppend = (Button) this.findViewById(R.id.button_pause);
		button_clear.setOnClickListener(clear_clicker);
		button_suppend.setOnClickListener(pause_clicker);
		button_reset.setOnClickListener(reset_clicker);
		progress_icon = (ProgressBar) this.findViewById(R.id.progress_icon);
		text_progress = (TextView) this.findViewById(R.id.progress_label);

	}

	View.OnClickListener reset_clicker = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			button_reset.setEnabled(false);
			AlertDialog.Builder builder = new Builder(MainActivity.this);
			builder.setMessage("会重置所有的设备，继续吗？");

			builder.setTitle("提示");

			builder.setPositiveButton("是", new OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();

					setProgress("正在重置");

					threadPools.submit(new Runnable() {
						public void run() {
							for (Device device : devices) {
								device.reset();
							}
							MainActivity.this.invokeHandler
									.post(new Runnable() {
										@Override
										public void run() {
											// TODO Auto-generated method stub
											disableProgress();
											button_reset.setEnabled(true);
										}
									});
						}
					});
				}
			});

			builder.setNegativeButton("取消", new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});

			builder.create().show();
		}
	};

	View.OnClickListener clear_clicker = new View.OnClickListener() {

		@Override
		public void onClick(View v) {

			// TODO Auto-generated method stub

			AlertDialog.Builder builder = new Builder(MainActivity.this);
			builder.setMessage("会清除所有设备的记录，继续吗？");
			builder.setTitle("提示");
			builder.setPositiveButton("是", new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					threadPools.submit(new Runnable() {
						@Override
						public void run() {
							for (Device device : devices) {
								device.clear();
							}
						}
					});

					invokeHandler.post(new Runnable() {
						@Override
						public void run() {
							Toast.makeText(MainActivity.this, "已清除", 5000)
									.show();
						}

					});
				}
			});

			builder.setNegativeButton("取消", new OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});

			builder.create().show();
		}

	};

	View.OnClickListener resume_clicker = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			button_suppend.setEnabled(false);
			button_suppend.setBackground(resources
					.getDrawable(R.drawable.pause));
			button_suppend.setOnClickListener(pause_clicker);
			setProgress("正在恢复 ...");
			threadPools.submit(new Runnable() {
				public void run() {
					for (Device device : devices) {
						device.resume();
					}

					MainActivity.this.invokeHandler.post(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							disableProgress();
							button_suppend.setEnabled(true);
						}
					});
				}
			});
		}
	};

	public boolean existed;

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		existed = true;
	}

	View.OnClickListener pause_clicker = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			button_suppend.setEnabled(false);
			button_suppend
					.setBackground(resources.getDrawable(R.drawable.play));
			button_suppend.setOnClickListener(resume_clicker);
			setProgress("正在暂停 ...");
			threadPools.submit(new Runnable() {
				public void run() {
					for (Device device : devices) {
						device.pause();

					}

					MainActivity.this.invokeHandler.post(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							disableProgress();
							button_suppend.setEnabled(true);
						}

					});

				}
			});
		}
	};

	public void setProgress(String text) {
		progress_icon.setVisibility(View.VISIBLE);
		text_progress.setText(text);
		text_progress.setVisibility(View.VISIBLE);
	}

	public void disableProgress() {
		progress_icon.setVisibility(View.GONE);
		text_progress.setVisibility(View.GONE);
		text_progress.setText("");
	}

	private List<Map<String, Object>> getContactData() {
		// TODO Auto-generated method stub
		return null;
	}

	private void startRefershDevices() {
		threadPools.submit(new Runnable() {
			@Override
			public void run() {

				invokeHandler.post(enableLoadControlsWorker);

				getDevices();

				invokeHandler.post(disableLoadControlsWorker);

				while (true) {

					for (Device device : devices) {
						if (device.refresh() == true) {
							invokeHandler.post(notifyDatasetWorker);
						}

						if (device == RingActivity.device) {
							RingActivity.refreshHandler
									.sendEmptyMessage(0);
						}
					}

					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});
	}

	Runnable notifyDatasetWorker = new Runnable() {
		public void run() {
			adapter_clients.notifyDataSetChanged();
		}
	};

	Runnable enableLoadControlsWorker = new Runnable() {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			button_clear.setEnabled(false);
			setProgress("正在刷新设备 ...");
		}
	};

	Runnable disableLoadControlsWorker = new Runnable() {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			disableProgress();
			button_clear.setEnabled(true);
		}
	};

	private void getDevices() {
		// TODO Auto-generated method stub
		for (int i = ipStart; i < 255; i++) {
			InetAddress address = null;

			try {
				address = InetAddress.getByName("192.168.1." + i);
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if (address == null)
				continue;

			final Device device = Device.getDevice(address);
			device.Location = devices.size() + "";

			if (device == null)
				continue;

			devices.add(device);
			invokeHandler.post(notifyDatasetWorker);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setControls();
		startRefershDevices();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
