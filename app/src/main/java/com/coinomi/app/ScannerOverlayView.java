package com.coinomi.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class ScannerOverlayView extends View {
    private Paint paint;
    private Paint bgPaint;
    private float boxSize;

    public ScannerOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(4f); // ضخامت خطوط (نازک)
        paint.setAntiAlias(true);

        bgPaint = new Paint();
        bgPaint.setColor(Color.parseColor("#99000000")); // پس‌زمینه نیمه شفاف
        bgPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        int width = getWidth();
        int height = getHeight();
        
        // اندازه مربع اسکن (۶۰ درصد عرض صفحه)
        boxSize = width * 0.8f;
        float left = (width - boxSize) / 2;
        float top = (height - boxSize) / 2;
        float right = left + boxSize;
        float bottom = top + boxSize;

        // کشیدن پس‌زمینه تاریک
        canvas.drawRect(0, 0, width, top, bgPaint); // بالا
        canvas.drawRect(0, bottom, width, height, bgPaint); // پایین
        canvas.drawRect(0, top, left, bottom, bgPaint); // چپ
        canvas.drawRect(right, top, width, bottom, bgPaint); // راست

        // طول هر گوشه (کشیدگی)
        float cornerLength = boxSize * 0.2f; // ۲۰ درصد اندازه مربع

        // رسم ۴ گوشه سفید
        // بالا-چپ
        canvas.drawLine(left, top, left + cornerLength, top, paint);
        canvas.drawLine(left, top, left, top + cornerLength, paint);
        
        // بالا-راست
        canvas.drawLine(right, top, right - cornerLength, top, paint);
        canvas.drawLine(right, top, right, top + cornerLength, paint);
        
        // پایین-چپ
        canvas.drawLine(left, bottom, left + cornerLength, bottom, paint);
        canvas.drawLine(left, bottom, left, bottom - cornerLength, paint);
        
        // پایین-راست
        canvas.drawLine(right, bottom, right - cornerLength, bottom, paint);
        canvas.drawLine(right, bottom, right, bottom - cornerLength, paint);
    }
}
