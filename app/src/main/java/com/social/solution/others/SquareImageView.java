package com.social.solution.others;

import android.content.Context;
import android.util.AttributeSet;

import com.mopub.volley.toolbox.NetworkImageView;

public class SquareImageView extends NetworkImageView {
    public SquareImageView(Context context) {
        super(context);
    }

    public SquareImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(getMeasuredWidth(), getMeasuredHeight());
        //setMeasuredDimension(getMeasuredWidth(), getMeasuredWidth()*55/100); //Snap to width
    }
}