package com.example.spider.NewTextView;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

public class fzjljtView extends TextView {

    public fzjljtView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setTypeface(Typeface tf) {
        super.setTypeface(tf);

        //根据路径得到Typeface
        tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/hksnzt.ttf");//方正静蕾简体
        super.setTypeface(tf);
    }
}
