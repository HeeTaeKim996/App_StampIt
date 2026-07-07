package com.example.stampit;

public class TextBlock
{
    private String text;

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

}
