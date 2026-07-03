package com.example.stampit;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.ViewGroup;

import androidx.constraintlayout.widget.ConstraintLayout;

public class Page extends ConstraintLayout
{
    public Page(Context context)
    {super(context);                        init(context);}
    public Page(Context context, AttributeSet attrs)
    {super(context, attrs);                 init(context);}
    public Page(Context context, AttributeSet attrs, int defStyleAttr)
    {super(context, attrs, defStyleAttr);   init(context);}



    private void init(Context context)
    {

    }

    @Override
    protected void onAttachedToWindow()
    {
        super.onAttachedToWindow();

        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        int screenWidth = displayMetrics.widthPixels;
        int targetHeight = (int) (screenWidth * 1.414f);
        // 기기의 가로 기준으로, A4 용지 비율로 세로 길이 설정

        ViewGroup.LayoutParams params = getLayoutParams();

        if(params != null)
        {
            params.height = targetHeight;
            setLayoutParams(params);
        }
    }

}
