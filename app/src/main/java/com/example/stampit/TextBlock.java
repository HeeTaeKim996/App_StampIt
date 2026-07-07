package com.example.stampit;

import android.graphics.Bitmap;
import android.text.TextUtils;

public class TextBlock
{
    private CharSequence text = ""; // Char 하나당 스타일 정보(Span)를 담을 수 있는 CharSequence 사용
    private Bitmap bitmap = null;   // 이미지는 블록당 하나만 사용 가능 (갤럭시 노트가 참조함)


    public TextBlock(CharSequence InSequence)
    {
        text = InSequence;
    }


    public CharSequence getText()
    {
        return text;
    }


    public void setText(CharSequence InSequence)
    {
        text = InSequence;
    }


    public void addText(CharSequence addedText)
    {
        text = TextUtils.concat(text, addedText);
    }

    public CharSequence removeTextFromSel(int sel)
    {
        CharSequence newSequence = text.subSequence(sel, text.length());
        text = text.subSequence(0, sel);
        return newSequence;
    }


    public void setBitmap(Bitmap InBitmap)
    {
        if(InBitmap == null)
        {
            bitmap = null;
        }
        else
        {
            text = "\uFFFC";
            bitmap = InBitmap;
        }
    }

    public boolean isBitmapped()
    {
        return bitmap != null;
    }

    public Bitmap getBitmap()
    {
        return bitmap;
    }

}
