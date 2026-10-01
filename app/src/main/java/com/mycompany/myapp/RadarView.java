package com.mycompany.myapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.view.View;

import java.util.ArrayList;
import java.util.Random;

public class RadarView extends View {

    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private float sweepAngle = 0;
    private boolean sweeping = false;

    private final ArrayList<PointF> blips =
	new ArrayList<PointF>();

    private final Random random =
	new Random();

    private Runnable animationRunnable =
	new Runnable() {
		@Override
		public void run() {

			if (sweeping) {

				sweepAngle += 3;

				if (sweepAngle >= 360) {
					sweepAngle = 0;
				}

				invalidate();

				postDelayed(
					this,
					25
				);
			}
		}
	};

    public RadarView(Context context) {
        super(context);

        paint.setStrokeWidth(2);
        paint.setStyle(Paint.Style.STROKE);

        glowPaint.setStyle(Paint.Style.STROKE);

        setBackgroundColor(
			Color.rgb(2, 15, 10)
        );
    }

    @Override
    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);

        float centerX = getWidth() / 2f;
        float centerY = getHeight() / 2f;

        float radius =
			Math.min(
			getWidth(),
			getHeight()
		) * 0.42f;

        // Màu radar xanh
        int green =
			Color.rgb(40, 255, 100);

        // Vòng tròn radar
        paint.setColor(green);
        paint.setAlpha(180);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);

        for (int i = 1; i <= 4; i++) {

            float r =
				radius * i / 4f;

            canvas.drawCircle(
				centerX,
				centerY,
				r,
				paint
            );
        }

        // Vòng ngoài
        paint.setAlpha(230);
        paint.setStrokeWidth(3);

        canvas.drawCircle(
			centerX,
			centerY,
			radius,
			paint
        );

        // Trục ngang
        paint.setAlpha(150);
        paint.setStrokeWidth(1);

        canvas.drawLine(
			centerX - radius,
			centerY,
			centerX + radius,
			centerY,
			paint
        );

        // Trục dọc
        canvas.drawLine(
			centerX,
			centerY - radius,
			centerX,
			centerY + radius,
			paint
        );

        // Các vạch góc
        paint.setAlpha(180);

        for (int angle = 0;
             angle < 360;
		angle += 15) {

            double rad =
				Math.toRadians(angle);

            float x1 =
				centerX +
				(float)Math.cos(rad)
				* radius;

            float y1 =
				centerY +
				(float)Math.sin(rad)
				* radius;

            float x2 =
				centerX +
				(float)Math.cos(rad)
				* (radius - 15);

            float y2 =
				centerY +
				(float)Math.sin(rad)
				* (radius - 15);

            canvas.drawLine(
				x1,
				y1,
				x2,
				y2,
				paint
            );
        }

        // Vòng quét
        if (sweeping) {

            float sweepRad =
				(float)Math.toRadians(
				sweepAngle
			);

            float endX =
				centerX +
				(float)Math.cos(sweepRad)
				* radius;

            float endY =
				centerY +
				(float)Math.sin(sweepRad)
				* radius;

            glowPaint.setColor(green);
            glowPaint.setAlpha(220);
            glowPaint.setStrokeWidth(5);

            canvas.drawLine(
				centerX,
				centerY,
				endX,
				endY,
				glowPaint
            );

            // Vệt sáng phía sau tia quét
            glowPaint.setAlpha(45);
            glowPaint.setStrokeWidth(18);

            canvas.drawLine(
				centerX,
				centerY,
				endX,
				endY,
				glowPaint
            );
        }

        // Các thiết bị phát hiện được
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(green);

        for (PointF point : blips) {

            canvas.drawCircle(
				point.x,
				point.y,
				5,
				paint
            );

            paint.setStyle(
				Paint.Style.STROKE
            );

            paint.setStrokeWidth(1);
            paint.setAlpha(120);

            canvas.drawCircle(
				point.x,
				point.y,
				10,
				paint
            );

            paint.setStyle(
				Paint.Style.FILL
            );

            paint.setAlpha(255);
        }

        // Tâm radar
        paint.setColor(green);
        paint.setAlpha(255);

        canvas.drawCircle(
			centerX,
			centerY,
			5,
			paint
        );

        // Chữ 360°
        paint.setStyle(
			Paint.Style.FILL
        );

        paint.setTextSize(18);
        paint.setAlpha(230);

        canvas.drawText(
			"360°",
			centerX - 20,
			centerY - radius - 12,
			paint
        );

        paint.setTextSize(13);

        canvas.drawText(
			"N",
			centerX - 5,
			centerY - radius + 25,
			paint
        );

        canvas.drawText(
			"S",
			centerX - 5,
			centerY + radius - 10,
			paint
        );

        canvas.drawText(
			"W",
			centerX - radius + 10,
			centerY + 5,
			paint
        );

        canvas.drawText(
			"E",
			centerX + radius - 18,
			centerY + 5,
			paint
        );
    }

    public void startSweep() {

        sweeping = true;

        if (blips.size() == 0) {
            addRandomBlips(5);
        }

        removeCallbacks(
			animationRunnable
        );

        post(
			animationRunnable
        );

        invalidate();
    }

    public void stopSweep() {

        sweeping = false;

        removeCallbacks(
			animationRunnable
        );

        invalidate();
    }

    public void addBlip() {

        float centerX =
			getWidth() / 2f;

        float centerY =
			getHeight() / 2f;

        float radius =
			Math.min(
			getWidth(),
			getHeight()
		) * 0.38f;

        double angle =
			random.nextDouble()
			* Math.PI * 2;

        float distance =
			radius *
			(0.25f +
			random.nextFloat() * 0.70f);

        float x =
			centerX +
			(float)Math.cos(angle)
			* distance;

        float y =
			centerY +
			(float)Math.sin(angle)
			* distance;

        blips.add(
			new PointF(x, y)
        );

        if (blips.size() > 25) {
            blips.remove(0);
        }

        invalidate();
    }

    private void addRandomBlips(
		int amount) {

        for (int i = 0;
             i < amount;
		i++) {

            addBlip();
        }
    }
}
