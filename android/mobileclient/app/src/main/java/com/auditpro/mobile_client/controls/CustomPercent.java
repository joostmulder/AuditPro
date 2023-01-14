/*
  AuditPRO Mobile Client Android
  Copyright 2018 AuditPRO All Rights Reserved
 */
package com.auditpro.mobile_client.controls;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Implements custom view to graph percentage around text.
 * @author Eric Ruck
 */
public class CustomPercent extends android.support.v7.widget.AppCompatTextView {

	private static final float STROKE_WIDTH = 4;
	private static final int FULL_COLOR = Color.LTGRAY;
	private static final int PCT_COLOR = Color.BLUE;

	private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);


	public CustomPercent(Context context, AttributeSet attrs) {
		super(context, attrs);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(STROKE_WIDTH);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		// Is there a percentage in our text?
		Pattern pattern = Pattern.compile("^\\d+");
		Matcher matcher = pattern.matcher(getText());
		if (matcher.find()) {
			int percent = Integer.parseInt(matcher.group());
			if ((percent >= 0) && (percent <= 100)) {
				// Draw the full ring
				float half = STROKE_WIDTH / 2;
				float width = getWidth() - STROKE_WIDTH;
				float height = getHeight() - STROKE_WIDTH;
				paint.setColor(percent == 100 ? PCT_COLOR : FULL_COLOR);
				canvas.drawOval(half, half, width, height, paint);
				if ((percent > 0) && (percent < 100)) {
					// Draw percentage arc
					float sweep= 360 * percent / 100;
					paint.setColor(PCT_COLOR);
					canvas.drawArc(half, half, width, height, 270, sweep, false, paint);
				}
			}
		}
	}
}
