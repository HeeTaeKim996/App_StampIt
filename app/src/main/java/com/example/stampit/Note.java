package com.example.stampit;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class Note extends ConstraintLayout
{
    public Note(Context context)
    {super(context);                        init(context);}
    public Note(Context context, AttributeSet attrs)
    {super(context, attrs);                 init(context);}
    public Note(Context context, AttributeSet attrs, int defStyleAttr)
    {super(context, attrs, defStyleAttr);   init(context);}


    private void init(Context context)
    {
        inflate(context, R.layout.activity_main, this);

        RecyclerView recyclerView = findViewById(R.id.recycleView);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        int initSize = 10;
        List<TextBlock> data = new ArrayList<TextBlock>(initSize);
        while(initSize -- > 0)
        {
            data.add(new TextBlock(""));
        }

        EditorAdapter adapter = new EditorAdapter(data);
        recyclerView.setAdapter(adapter);
    }
}
