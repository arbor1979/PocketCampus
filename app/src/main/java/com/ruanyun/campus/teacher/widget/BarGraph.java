/*
 *     Created by Daniel Nadeau
 *     daniel.nadeau01@gmail.com
 *     danielnadeau.blogspot.com
 * 
 *     Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
 */

package com.ruanyun.campus.teacher.widget;

import java.util.ArrayList;

import com.ruanyun.campus.teacher.R;
import com.ruanyun.campus.teacher.entity.Bar;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.drawable.NinePatchDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

public class BarGraph extends View {

	private ArrayList<Bar> points = new ArrayList<Bar>();
	private Paint p = new Paint();
	private Rect r;
	private boolean showBarText = true;
	private int indexSelected = -1;
	private OnBarClickedListener listener;
	private Bitmap fullImage;
	private boolean shouldUpdate = false;
	private String[] values = { "0", "20", "40", "60", "80", "100" };

	public BarGraph(Context context) {
		super(context);
	}

	public BarGraph(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void setShowBarText(boolean show) {
		showBarText = show;
	}

	public void setBars(ArrayList<Bar> points) {
		this.points = points;
		postInvalidate();
	}

	public ArrayList<Bar> getBars() {
		return this.points;
	}

	public void onDraw(Canvas ca) {

		if (fullImage == null || shouldUpdate) {
			fullImage = Bitmap.createBitmap(getWidth(), getHeight(),
					Config.ARGB_8888);
			Canvas canvas = new Canvas(fullImage);
			canvas.drawColor(Color.TRANSPARENT);
			// NinePatchDrawable popup =
			// (NinePatchDrawable)this.getResources().getDrawable(R.drawable.popup_black);

			float maxValue = 100;
			float padding = 20;
			int selectPadding = 4;
			float bottomPadding = 40;
			float leftPadding = 60;
			float topPadding = 40;
			float usableHeight;
			float rightPadding = 10;
			/**
			 * 每段线条的高度；
			 */
			float lineHeight;
			usableHeight = getHeight() - topPadding - bottomPadding;
			lineHeight = usableHeight / 5;
			// Draw x-axis line
			p.setColor(Color.BLACK);
			p.setStrokeWidth(1);
			p.setAlpha(50);
			p.setAntiAlias(true);
			for (int i = 0; i <= 5; i++) {
				canvas.drawLine(leftPadding, getHeight() - bottomPadding
						- lineHeight * i, getWidth() - rightPadding, getHeight()
						- bottomPadding - lineHeight * i, p);

			}
			float barWidth = (getWidth() - (padding * 4) * points.size() - leftPadding)
					/ points.size();
			// 绘制线条高度文字
			Rect r3 = new Rect();

			p.setColor(Color.BLACK);
			p.setAlpha(200);
			p.setTextSize(20);
			for (int i = 0; i <= 5; i++) {
				this.p.getTextBounds(values[i], 0, 1, r3);
				canvas.drawText(values[i],
						leftPadding - this.p.measureText(values[i]) - padding,
						getHeight() - bottomPadding - lineHeight * i, p);
			}
			// Maximum y value = sum of all values.
			// for (Bar p : points) {
			// maxValue += p.getValue();
			// }

			r = new Rect();

			int count = 0;
			for (Bar p : points) {
				// Set bar bounds
				int left = (int) ((padding * 4) * count + padding + leftPadding + barWidth
						* count);
				int top = (int) (getHeight() - bottomPadding - (usableHeight * (p
						.getValue() / maxValue)));
				int right = (int) ((padding * 4) * count + padding
						+ leftPadding + barWidth * (count + 1));
				int bottom = (int) (getHeight() - bottomPadding);
				r.set(left, top, right, bottom);

				// Draw bar
				this.p.setColor(p.getColor());
				this.p.setAlpha(255);
				canvas.drawRect(r, this.p);

				// Create selection region
				Path path = new Path();
				path.addRect(new RectF(r.left - selectPadding, r.top
						- selectPadding, r.right + selectPadding, r.bottom
						+ selectPadding), Path.Direction.CW);
				p.setPath(path);
				p.setRegion(new Region(r.left - selectPadding, r.top
						- selectPadding, r.right + selectPadding, r.bottom
						+ selectPadding));

				// Draw x-axis label text
				this.p.setTextSize(20);
				int x = (int) (((r.left + r.right) / 2) - (this.p.measureText(p
						.getName()) / 2));
				int y = getHeight() - 5;
				canvas.drawText(p.getName(), x, y, this.p);

				// Draw value text
				if (showBarText) {
					this.p.setTextSize(20);
					this.p.setColor(p.getColor());
					Rect r2 = new Rect();
					this.p.getTextBounds(String.valueOf(p.getValue()), 0, 1, r2);
					// popup.setBounds((int)(((r.left+r.right)/2)-(this.p.measureText(String.valueOf(p.getValue()))/2))-
					// 14, r.top+(r2.top-r2.bottom),
					// (int)(((r.left+r.right)/2)+(this.p.measureText(String.valueOf(p.getValue()))/2))+14,
					// r.top);
					// popup.draw(canvas);
					canvas.drawText(
							String.valueOf(p.getValue()),
							(int) (((r.left + r.right) / 2) - (this.p
									.measureText(String.valueOf(p.getValue())) / 2)),
							r.top-5, this.p);
				}
				if (indexSelected == count && listener != null) {
					this.p.setColor(Color.parseColor("#33B5E5"));
					this.p.setAlpha(100);
					canvas.drawPath(p.getPath(), this.p);
					this.p.setAlpha(255);
				}
				count++;
			}
			shouldUpdate = false;
		}

		ca.drawBitmap(fullImage, 0, 0, null);

	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		Point point = new Point();
		point.x = (int) event.getX();
		point.y = (int) event.getY();

		int count = 0;
		for (Bar bar : points) {
			Region r = new Region();
			r.setPath(bar.getPath(), bar.getRegion());
			if (r.contains(point.x, point.y)
					&& event.getAction() == MotionEvent.ACTION_DOWN) {
				indexSelected = count;
			} else if (event.getAction() == MotionEvent.ACTION_UP) {
				if (r.contains(point.x, point.y)
						&& listener != null) {
					listener.onClick(indexSelected);
				}
				indexSelected = -1;
			}
			count++;
		}

		if (event.getAction() == MotionEvent.ACTION_DOWN
				|| event.getAction() == MotionEvent.ACTION_UP) {
			shouldUpdate = true;
			postInvalidate();
		}

		return true;
	}

	public void setOnBarClickedListener(OnBarClickedListener listener) {
		this.listener = listener;
	}

	public interface OnBarClickedListener {
		void onClick(int index);
	}
}
