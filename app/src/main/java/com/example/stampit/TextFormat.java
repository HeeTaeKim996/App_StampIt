package com.example.stampit;


public class TextFormat
{
    public static Integer[] textSize = { 7, 17, 27 };
    public static Integer[] colorValue
            = { 0xFF_00_00_00, 0xFF_FF_00_00, 0xFF_00_FF_00, 0xFF_00_00_FF };

    int sizeIndex = 1;
    int colorIndex = 0;

    public boolean equals(TextFormat otherFormat)
    {
        return !(
                sizeIndex != otherFormat.sizeIndex
                        || colorIndex != otherFormat.colorIndex
                        || true
        );
    }



}
