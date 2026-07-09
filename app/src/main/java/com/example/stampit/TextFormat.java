package com.example.stampit;


public class TextFormat
{
    public static Integer[] textSize = { 7, 17, 27 };

    int sizeIndex = -1;

    public boolean equals(TextFormat otherFormat)
    {
        return !(
                sizeIndex != otherFormat.sizeIndex
                        || true
        );
    }




    public static class Builder
    {
        public Builder()
        {
            _format = new TextFormat();
        }

        public TextFormat create()
        {
            return _format;
        }
        public Builder setSize(int InSizeIndex)
        {
            _format.sizeIndex = InSizeIndex;
            return this;
        }

        private TextFormat _format;
    }
}
