/*
  AuditPRO Mobile Client Android
  Copyright 2018 AuditPRO All Rights Reserved
 */
package com.auditpro.mobile_client.controls;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Typeface;
import android.util.AttributeSet;

import com.auditpro.mobile_client.entities.ReorderStatus;
import com.auditpro.mobile_client.test.R;


/**
 * Displays custom price button.
 * @author  Eric Ruck
 */
public class PriceButton extends android.support.v7.widget.AppCompatTextView {
	private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

	public PriceButton(Context context) {
		super(context);
	}

	public PriceButton(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public PriceButton(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	/**
	 * Implement this to do your drawing.
	 *
	 * @param canvas the canvas on which the background will be drawn
	 */
	@SuppressLint("DrawAllocation")
	@Override
	protected void onDraw(Canvas canvas) {
		// Draw the background
		paint.setStyle(Paint.Style.FILL);
		paint.setColor(Color.WHITE);
		canvas.drawRoundRect(0, 0, getWidth(), getHeight(), 5, 5, paint);

		// Determine what to paint
		String text = getText().toString();
		String[] tokens = text.split(" ");
		int paintIcon = 0;
		paint.setColor(Color.LTGRAY);
		if (tokens.length > 0) {
			ReorderStatus status = ReorderStatus.fromCode(tokens[0]);
			if ((status == ReorderStatus.IN_STOCK) || (status == ReorderStatus.OUT_OF_STOCK)) {
				// Display for in/out of stock
				text = (tokens.length > 1) ? tokens[1] : status.getCode();
				if (status == ReorderStatus.IN_STOCK) {
					paint.setColor(getResources().getColor(R.color.colorInStock, null));
					paintIcon = R.drawable.baseline_check_black_18;
				} else {
					paint.setColor(Color.RED);
				}
			} else if (status != null) {
				// Display code only
				text = status.getCode();
			}
		}

		// Draw the border
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(2);
		canvas.drawRoundRect(0, 0, getWidth(), getHeight(), 5, 5, paint);
		if (paintIcon > 0) {
			// Draw in the icon
			@SuppressLint("DrawAllocation") Bitmap bmp = BitmapFactory.decodeResource(getResources(), paintIcon);
			paint.setColorFilter(new PorterDuffColorFilter(paint.getColor(), PorterDuff.Mode.SRC_IN));
			canvas.drawBitmap(bmp, getWidth() - bmp.getWidth(), 0, paint);
			paint.setColorFilter(null);
		}

		// Draw the text
		Typeface typeface = Typeface.createFromAsset(getContext().getAssets(), "hero.otf");
		paint.setTextAlign(Paint.Align.CENTER);
		paint.setColor(Color.BLACK);
		paint.setTypeface(typeface);
		paint.setTextSize(getTextSize());
		paint.setStyle(Paint.Style.FILL);
		canvas.drawText(text, getWidth() / 2, (getHeight() + getTextSize()) / 2, paint);
	}
}
