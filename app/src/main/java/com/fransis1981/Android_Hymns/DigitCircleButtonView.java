package com.fransis1981.Android_Hymns;

import android.content.Context;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

/**
 * Created by francesco.vitullo on 28/01/14.
 * This class implements a circle style button View for a single digit.
 * (similar to numeric keypad on iOS7 unlock screen).
 */
public class DigitCircleButtonView extends Button {
   private static Paint buttonPaint;               //Paint object for the outer line
   private static Paint buttonPaint_blur;          //Paint object for the blurred outer line
   private static Paint textPaint;                 //Paint object for text in enabled state
   private static Paint textPaint_obscured;        //Paint object for text in obscured state
   private static Paint bgIllumination;            //Paint object for background when button is touched

   //Helper object for centering text; preallocated for efficiency.
   private static Rect txtBounds = new Rect();

   int buttonX, buttonY, radius;
   boolean isPressed = false;

   // Margin provided between button outer circle line and measured button's dimensions
   private int _margin;

    //Shared init method for all constructors.
    private void init(int color) {
        if (isInEditMode()) return;
        _margin = (int) HymnsApplication.myResources.getDimension(R.dimen.circleButton_Margin);
         try {
            if (buttonPaint == null) {
               buttonPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
               buttonPaint.setColor(color);
               buttonPaint.setStyle(Paint.Style.STROKE);
               buttonPaint.setStrokeWidth((int) HymnsApplication.myResources.getDimension(R.dimen.circleButton_LineWidth));
            }

            if (buttonPaint_blur == null) {
               buttonPaint_blur = new Paint(Paint.ANTI_ALIAS_FLAG);
               buttonPaint_blur.setColor(color);
               buttonPaint_blur.setStyle(Paint.Style.STROKE);
               buttonPaint_blur.setStrokeWidth((int) HymnsApplication.myResources.getDimension(R.dimen.circleButton_LineWidth));
               buttonPaint_blur.setMaskFilter(new BlurMaskFilter(R.integer.circleButton_BlurRadius, BlurMaskFilter.Blur.NORMAL));
            }

            if (textPaint == null) {
               textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
               textPaint.setColor(HymnsApplication.myResources.getColor(R.color.strofaLabel_color));
               textPaint.setStrokeWidth((int) HymnsApplication.myResources.getDimension(R.dimen.circleButton_TextStroke));
               textPaint.setStyle(Paint.Style.FILL_AND_STROKE);
               textPaint.setTextAlign(Paint.Align.CENTER);
               textPaint.setTypeface(HymnsApplication.fontLabelStrofa);
            }

            if (textPaint_obscured == null) {
               textPaint_obscured = new Paint(Paint.ANTI_ALIAS_FLAG);
               textPaint_obscured.setColor(HymnsApplication.myResources.getColor(R.color.strofaLabel_color_faded));
               textPaint_obscured.setStrokeWidth((int) HymnsApplication.myResources.getDimension(R.dimen.circleButton_TextStroke));
               textPaint_obscured.setStyle(Paint.Style.STROKE);
               textPaint_obscured.setTextAlign(Paint.Align.CENTER);
               textPaint_obscured.setTypeface(HymnsApplication.fontLabelStrofa);
            }

            if (bgIllumination == null) {
               bgIllumination = new Paint();
               bgIllumination.setColor(HymnsApplication.myResources.getColor(R.color.circleButton_Illumination));
               bgIllumination.setStyle(Paint.Style.FILL);
            }

            setOnTouchListener(new OnTouchListener() {
               @Override
               public boolean onTouch(View v, MotionEvent event) {
                  if (event.getActionMasked() == MotionEvent.ACTION_DOWN) { isPressed = true; }
                  else if (event.getActionMasked() == MotionEvent.ACTION_UP
                        || event.getActionMasked() == MotionEvent.ACTION_CANCEL
                        || event.getActionMasked() == MotionEvent.ACTION_OUTSIDE)
                     { isPressed = false; }
                  invalidate();
                  return false;           //IMPORTANT: Returns false so that we may continue further event processing
               }
            });
         } catch (Exception e) {
             Log.e(MyConstants.LogTag_STR, e.getMessage());
         }
    }



    public DigitCircleButtonView(Context context, int color) {
        super(context);
        init(color);
    }

    public DigitCircleButtonView(Context context) {
        super(context);
        init(HymnsApplication.myResources.getColor(R.color.circleButton_outerLine));
    }

    public DigitCircleButtonView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(HymnsApplication.myResources.getColor(R.color.circleButton_outerLine));
    }

    public DigitCircleButtonView(Context context, AttributeSet attrs) {
       super(context, attrs);
       init(HymnsApplication.myResources.getColor(R.color.circleButton_outerLine));
    }

    @Override
    protected void onDraw(Canvas canvas) {

        if (isInEditMode()) {
            super.onDraw(canvas);
        }
        else {
           // Just for Debug:  canvas.drawRect(0,0, getMeasuredWidth(), getMeasuredHeight(), textPaint);
           buttonX = getMeasuredWidth() / 2;
           buttonY = getMeasuredHeight() / 2;
           radius = (Math.min(buttonX, buttonY)) - _margin;
           setBackgroundColor(HymnsApplication.myResources.getColor(android.R.color.transparent));
           if (!isEnabled()) {
              textPaint_obscured.setTextSize(radius);
              if (getText().length() > 0) {
                 textPaint_obscured.getTextBounds(getText().toString(), 0, 1, txtBounds);
                 canvas.drawText(getText().toString(), buttonX, buttonY - txtBounds.centerY(), textPaint_obscured);
              }
           }
           else {
              textPaint.setTextSize(radius);
              if (getText().length() > 0) {
                 textPaint.getTextBounds(getText().toString(), 0, 1, txtBounds);
                 canvas.drawText(getText().toString(), buttonX, buttonY - txtBounds.centerY(), textPaint);
              }
              if (isPressed) {
                 canvas.drawCircle(buttonX, buttonY, radius, bgIllumination);
                 //canvas.drawCircle(buttonX, buttonY, radius, buttonPaint_blur);
              }
              canvas.drawCircle(buttonX, buttonY, radius, buttonPaint);
           }
        }
    }

   @Override
   protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
      //super.onMeasure(widthMeasureSpec, heightMeasureSpec);
      //Decoding width and height
      int modeX = MeasureSpec.getMode(widthMeasureSpec);
      int willX = MeasureSpec.getSize(widthMeasureSpec);
      int modeY = MeasureSpec.getMode(heightMeasureSpec);
      int willY = MeasureSpec.getSize(heightMeasureSpec);
      //Log.i(MyConstants.LogTag_STR, "PROPOSED MEASUREMENT:" + getText() + " (" + willX + ", " + willY + ")");

      //Willing to make the maximum possible square (some redundant assignments better than comparison and jump).
      int defaultX = (int) HymnsApplication.myResources.getDimension(R.dimen.circleButton_defaultWidth),
            defaultY = (int) HymnsApplication.myResources.getDimension(R.dimen.circleButton_defaultHeight);
      if (modeX == MeasureSpec.UNSPECIFIED) willX = defaultX;
      if (modeY == MeasureSpec.UNSPECIFIED) willY = defaultY;
      if (willX < willY) { willY = willX; }
      else { willX = willY; }
      setMeasuredDimension(willX, willY);
   }
}
