package com.example.stampit;

import android.content.Context;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatEditText;

public class CursorNotifyEditText extends AppCompatEditText
{
    public interface OnSelectionChangedListener
    {
        void onSelectionChanged(int selStart, int selEnd);
    }

    private OnSelectionChangedListener listener;

    public CursorNotifyEditText(Context context){super(context);}
    public CursorNotifyEditText(Context context, AttributeSet attrs){super(context, attrs);}

    public void setOnSelectionChangedListener(OnSelectionChangedListener InListener)
    {
        listener = InListener;
    }

    @Override
    protected void onSelectionChanged(int selStart, int selEnd)
    {
        super.onSelectionChanged(selStart, selEnd);
        if(listener != null)
        {
            listener.onSelectionChanged(selStart, selEnd);
        }
    }
}
