package com.example.spider.NewTextView;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

public class fzktjtView extends TextView {

    public fzktjtView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setTypeface(Typeface tf) {
        super.setTypeface(tf);

        //根据路径得到Typeface
        tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/fzktjt.ttf");//方正卡通简体
        super.setTypeface(tf);
    }
}
