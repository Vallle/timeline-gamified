package com.bjorsond.android.timeline.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.widget.TextView;

import com.bjorsond.android.timeline.R;


public class OutlineTextView extends TextView{
    
    private int outlineColor;

    
    public OutlineTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        final TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.OutlineTextView);
        
        outlineColor = a.getInt(R.styleable.OutlineTextView_outlineColor, 0xffB3F1FB);
        a.recycle();
        init();
    }
    

    public OutlineTextView(Context context, AttributeSet attrs, int defStyle) {
    	
        super(context, attrs, defStyle);
        
        final TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.OutlineTextView, defStyle, 0);
        
        outlineColor = a.getInt(R.styleable.OutlineTextView_outlineColor, 0x00000000); //0xffB3F1FB
        a.recycle();
        init();
    }


    public OutlineTextView(Context context) {
        super(context);
        init();
    }
    
    private TextPaint strokePaint;
    private TextPaint textPaint;
    
    
    private void init(){
        
//    	setTypeface(Typeface.createFromAsset(getContext().getAssets(), "fonts/kperry.ttf"));
        setTypeface(Typeface.MONOSPACE);
        strokePaint = new TextPaint();
        strokePaint.setColor(outlineColor);
        strokePaint.setAntiAlias(true);
        strokePaint.setDither(true);

        strokePaint.setTextSize(getTextSize());
        strokePaint.setTypeface(getTypeface());
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(1);

        textPaint = new TextPaint();
        textPaint.setColor(getTextColors().getDefaultColor());

        textPaint.setAntiAlias(true);
        textPaint.setDither(true);
        textPaint.setTextSize(getTextSize());
        textPaint.setTypeface(getTypeface());
    }
    
    @Override	
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    	
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int reqWidth;
        int reqHeight;
        
        // width & height mode
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        
        // specified width & height
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        
        // find out Width based on widthMode
        if (widthMode == MeasureSpec.EXACTLY){
        	
            // set user specified Width
            reqWidth = widthSize;
            
        } else {
        	
            // find out the total pixel size required for first and last text
            reqWidth = (int)(strokePaint.measureText(getText().toString()))+10;
        }
        
        // find out Height based on heightMode
        if (heightMode == MeasureSpec.EXACTLY) {
        	
            // set user specified Height
            reqHeight = heightSize;
            
        } else {
        	
            // get the default height of the Font
            reqHeight = (int) strokePaint.getTextSize();
        }
        
        // set the calculated width and height of your drawing area
        setMeasuredDimension(reqWidth, reqHeight+10);
    }
    

    @Override
    protected void onDraw(Canvas canvas) {

        canvas.drawText(getText().toString(), strokePaint.getStrokeWidth(), getTextSize(), strokePaint);
        canvas.drawText(getText().toString(), strokePaint.getStrokeWidth(), getTextSize(), textPaint);

    }
}
