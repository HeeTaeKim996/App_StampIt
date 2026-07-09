package com.example.stampit;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stampit.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.List;

public class Note
{
    private ActivityMainBinding mainBinding;

    private TextFormat currFormat;
    private boolean isCursorSelection = false;

    public Note(Context context)
    {
        mainBinding = ActivityMainBinding.inflate(LayoutInflater.from(context));

        currFormat = new TextFormat();

        mainBinding.recycleView.setLayoutManager(new LinearLayoutManager(context));

        int initSize = 10;
        List<TextBlock> data = new ArrayList<TextBlock>(initSize);
        while(initSize -- > 0)
        {
            data.add(new TextBlock(""));
        }

        EditorAdapter adapter = new EditorAdapter(data);
        mainBinding.recycleView.setAdapter(adapter);

        mainBinding.buttonAddResource.setOnClickListener(v->
        {
            Bitmap tempBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.imgflag8);
            adapter.InsertImage(tempBitmap);
        });
        adapter.setOnTextFormatChangedListener(new EditorAdapter.OnTextFormatChangedListener()
        {
            @Override
            public void onTextFormatChanged(TextFormat textFormat)
            {
                OnTextFormatChanged(textFormat);
            }
        });




        ArrayAdapter<Integer> spinnerAdapter = new ArrayAdapter<Integer>(context,
                android.R.layout.simple_list_item_1, TextFormat.textSize);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        mainBinding.spinnerTextSize.setAdapter(spinnerAdapter);
        mainBinding.spinnerTextSize.setSelection(1);
        mainBinding.spinnerTextSize.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l)
            {
                if(adapter == null) return;
                if(isCursorSelection) return;

                if(i == currFormat.sizeIndex) return;

                currFormat.sizeIndex = i;
                adapter.ChangeTextFormat(currFormat);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }
        });


        ArrayAdapter<Integer> colorAdapter = new ArrayAdapter<>(context,
                android.R.layout.simple_list_item_1, TextFormat.colorValue);
        colorAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        mainBinding.spinnerColorValue.setAdapter(colorAdapter);
        mainBinding.spinnerColorValue.setSelection(0);
        mainBinding.spinnerColorValue.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l)
            {
                if(adapter == null) return;
                if(isCursorSelection) return;

                if(i == currFormat.colorIndex) return;

                currFormat.colorIndex = i;
                adapter.ChangeTextFormat(currFormat);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });
    }

    public void OnTextFormatChanged(TextFormat newFormat)
    {
        if(currFormat.equals(newFormat)) return;

        currFormat = newFormat;
        isCursorSelection = true;

        if(mainBinding.spinnerTextSize.getSelectedItemPosition() != currFormat.sizeIndex)
        {
            mainBinding.spinnerTextSize.setSelection(currFormat.sizeIndex);
        }

        if(mainBinding.spinnerColorValue.getSelectedItemPosition() != currFormat.colorIndex)
        {
            mainBinding.spinnerColorValue.setSelection(currFormat.colorIndex);
        }

        // TODO : 다른 인자들 처리

        isCursorSelection = false;
    }

    public View GetRootView()
    {
        return mainBinding.getRoot();
    }



}
