package com.example.stampit;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

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

        findViewById(R.id.button_add_resource).setOnClickListener(v->
        {
            Bitmap tempBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.imgflag8);
            adapter.InsertImage(tempBitmap);
        });


        Integer[] fontSizes = {7, 17, 27};
        ArrayAdapter<Integer> spinnerAdapter = new ArrayAdapter<Integer>(context,
                android.R.layout.simple_list_item_1, fontSizes);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        Spinner textSizeSpinner = findViewById(R.id.spinner_textSize);
        textSizeSpinner.setAdapter(spinnerAdapter);
        textSizeSpinner.setSelection(1);
        textSizeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l)
            {
                if(adapter == null) return;

                adapter.setCurrTextSize(fontSizes[i]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView)
            {

            }
        });

    }







}
