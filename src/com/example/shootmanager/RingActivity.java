package com.example.shootmanager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

public class RingActivity extends Activity {

	public static Device device;
	public static RingActivity instance;
	public static Handler refreshHandler = new Handler() {
		@Override
		public void handleMessage(Message message) {
			if (instance == null)
				return;

			instance.setControlValue();
		}
	};

	TextView text_ring, text_rings, text_shoots;
	Button button_back, button_reset, button_clear, button_pause;
	ExecutorService threadPools = Executors.newFixedThreadPool(5);
	ImageView image_target;
	Handler invokeHandler = new Handler();
	Resources resources = null;
	Point lastBullHole;
	Canvas targetCanvas;
	Bitmap targetBmp;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ring);
		setControls();
		
		VideoView vv =new VideoView(this);

		
	}

	private void refreshPoint() {
		if (device == null) {
			return;
		}

		image_target.post(new Runnable() {

			@Override
			public void run() {
				Drawable drawable = image_target.getDrawable();
				Rect bound = drawable.getBounds();
				int width = bound.width();
				int height = bound.height();
				Matrix matrix = image_target.getMatrix();
				float[] values = new float[10];
				matrix.getValues(values);

				float mx = values[0];
				float my = values[4];

				final int x = (int) (width * mx);
				final int y = (int) (height * my);

				resetPoint(x, y);
			}
		});
	}

	private void resetPoint(final int x, final int y) {

		threadPools.submit(new Runnable() {
			@Override
			public void run() {
				device.updatePoint(x, y);

				if (device.Point == null) {
					image_target.setImageDrawable(resources
							.getDrawable(R.drawable.shoot));
				} else {
					drawPoint(device.Point.x, device.Point.y);
				}
			}
		});

	}

	private void drawPoint(int x, int y) {
		invokeHandler.post(new Runnable() {

			@Override
			public void run() {

				Paint paint = new Paint();
				paint.setARGB(255, 255, 2, 0);
				paint.setStyle(Style.FILL);

				targetCanvas.drawCircle(device.Point.x, device.Point.y, 10,
						paint);

			}

		});
	}

	private void setControlValue() {
		if (device != null) {
			text_ring.setText(device.LastScore.Ring.toString());
			text_rings.setText(device.LastScore.Rings.toString());
			text_shoots.setText(device.LastScore.Shoots.toString());

			refreshPoint();
		}
	}

	private void setControls() {

		instance = this;
		resources = this.getResources();

		targetBmp = BitmapFactory.decodeResource(resources, R.drawable.shoot);
		targetCanvas = new Canvas(targetBmp);

		text_ring = (TextView) this.findViewById(R.id.client_ring);
		text_rings = (TextView) this.findViewById(R.id.client_rings);
		text_shoots = (TextView) this.findViewById(R.id.client_shoots);
		button_back = (Button) this.findViewById(R.id.button_back);
		button_reset = (Button) this.findViewById(R.id.button_reset);
		button_clear = (Button) this.findViewById(R.id.button_clear);
		button_pause = (Button) this.findViewById(R.id.button_pause);
		image_target = (ImageView) this.findViewById(R.id.image_target);
		button_back.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				RingActivity.this.finish();
			}
		});

		button_pause.setOnClickListener(pause_clicker);
		button_reset.setOnClickListener(reset_clicker);
		button_clear.setOnClickListener(clear_clicker);

	}

	View.OnClickListener reset_clicker = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub

			button_reset.setEnabled(false);

			threadPools.submit(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					device.reset();
					invokeHandler.post(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							button_reset.setEnabled(true);
							setControlValue();

							Toast.makeText(RingActivity.this, "正在重置 ...", 5000)
									.show();
						}

					});
				}

			});
		}
	};

	View.OnClickListener clear_clicker = new View.OnClickListener() {

		@Override
		public void onClick(View v) {

			button_clear.setEnabled(false);

			threadPools.submit(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					device.clear();
					invokeHandler.post(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							button_clear.setEnabled(true);
							setControlValue();

							Toast.makeText(RingActivity.this, "已清除", 5000)
									.show();
						}

					});
				}

			});
		}

	};

	View.OnClickListener resume_clicker = new View.OnClickListener() {

		@Override
		public void onClick(View v) {

			button_pause.setEnabled(false);

			threadPools.submit(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					device.resume();
					invokeHandler.post(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							button_pause.setEnabled(true);
							button_pause.setOnClickListener(pause_clicker);
							button_pause.setBackground(resources
									.getDrawable(R.drawable.pause));
							setControlValue();

							Toast.makeText(RingActivity.this, "已经恢复", 5000)
									.show();
						}
					});
				}

			});
		}

	};

	View.OnClickListener pause_clicker = new View.OnClickListener() {
		@Override
		public void onClick(View v) {

			button_pause.setEnabled(false);

			threadPools.submit(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					device.pause();
					invokeHandler.post(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							button_pause.setEnabled(true);
							button_pause.setOnClickListener(resume_clicker);
							button_pause.setBackground(resources
									.getDrawable(R.drawable.play));
							setControlValue();

							Toast.makeText(RingActivity.this, "已暂停", 5000)
									.show();
						}

					});
				}

			});
		}

	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.ring, menu);
		return true;
	}

}
