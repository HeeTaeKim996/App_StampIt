package com.example.stampit;

import android.graphics.Bitmap;

public class TextBlock
{
    private String text;
    private Bitmap bitmap = null;   // 이미지는 블록당 하나만 사용 가능 (갤럭시 노트가 참조함)

    public TextBlock(String text)
    {
        this.text = text;
    }

    public String getText()
    {
        return text;
    }

    public void setText(String text)
    {
        this.text = text;
    }

    public void addText(String addedText)
    {
        text = text + addedText;
    }

    public String removeTextFromSel(int sel)
    {
        String newString = text.substring(sel, text.length());
        text = text.substring(0, sel);
        return newString;
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
