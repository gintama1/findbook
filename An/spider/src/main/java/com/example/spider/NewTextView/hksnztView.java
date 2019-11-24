package com.example.spider.NewTextView;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

public class hksnztView extends TextView {

    public hksnztView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setTypeface(Typeface tf) {
        super.setTypeface(tf);

        //根据路径得到Typeface
        tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/hksnzt.ttf");//华康少女字体
        super.setTypeface(tf);
    }
}
