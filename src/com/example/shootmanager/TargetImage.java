package com.example.shootmanager;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.util.AttributeSet;
import android.widget.ImageView;

public class TargetImage extends ImageView {

	private final Paint paint;
	private final Context context;
	private Point target;
	public int radius;

	public TargetImage(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		this.context = context;
		this.paint = new Paint();
		this.paint.setAntiAlias(true); // 消除锯齿
		this.paint.setStyle(Style.FILL); // 绘制空心圆或 空心矩形
		this.radius = 10;
	}

	public void setTarget(Point target) {
		target = target;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (target != null) {
			canvas.drawCircle(target.x, target.y, radius, paint);
		}
	}
}
