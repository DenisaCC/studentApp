package com.example.licentaapp.Utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatImageView;

import com.example.licentaapp.R;

import java.util.Random;

public class LetterImageView extends AppCompatImageView {

    private String mText; // Modificare: Schimbăm tipul de la char la String
    private Paint mTextPaint;
    private Paint mBackgroundPaint;
    private int mTextColor = Color.WHITE;
    private boolean isOval;

    public LetterImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(mTextColor);
        mBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBackgroundPaint.setStyle(Paint.Style.FILL);
        mBackgroundPaint.setColor(randomColor());
    }

    // Modificare: Metoda pentru a seta textul (ziua)
    public void setText(String text) {
        mText = text;
        invalidate();
    }

    public void setOval(boolean oval) {
        isOval = oval;
    }

    public boolean isOval() {
        return isOval;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (getDrawable() == null && mText != null) { // Verificăm dacă textul este setat
            mTextPaint.setTextSize(canvas.getHeight() - getTextPadding() * 2);
            if (isOval()) {
                // Draw a rectangle instead of a circle
                canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), mBackgroundPaint);
            } else {
                canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), mBackgroundPaint);
            }
            Rect textBounds = new Rect();
            mTextPaint.getTextBounds(String.valueOf(mText.charAt(0)), 0, 1, textBounds); // Extragem prima literă din text
            float textWidth = mTextPaint.measureText(String.valueOf(mText.charAt(0))); // Măsurăm lățimea primei litere
            float textHeight = textBounds.height();
            canvas.drawText(String.valueOf(mText.charAt(0)), canvas.getWidth() / 2f - textWidth / 2f, canvas.getHeight() / 2f + textHeight / 2f, mTextPaint); // Afisăm prima literă
        }
    }


    private float getTextPadding() {
        return 8 * getResources().getDisplayMetrics().density;
    }

    private int randomColor() {
        Random random = new Random();
        String[] colorsArr = getResources().getStringArray(R.array.colors);
        return Color.parseColor(colorsArr[random.nextInt(colorsArr.length)]);
    }
}
