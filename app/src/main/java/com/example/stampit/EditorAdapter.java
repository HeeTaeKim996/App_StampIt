package com.example.stampit;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class EditorAdapter extends RecyclerView.Adapter<EditorAdapter.ViewHolder>
{
    private final List<TextBlock> items;
    private int focusPosition = -1;
    private int focusSelection = -1;

    public EditorAdapter(List<TextBlock> items)
    {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_text_block, parent, false);
        return new ViewHolder(view, new CustomTextWatcher());
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position)
    {
        TextBlock item = items.get(position);

        holder.textWatcher.updatePosition(position);
        holder.editText.removeTextChangedListener(holder.textWatcher);
        holder.editText.setText(item.getText());
        holder.editText.addTextChangedListener(holder.textWatcher);

        if(position == focusPosition)
        {
            holder.editText.requestFocus();
            if(focusSelection != -1)
            {
                holder.editText.setSelection(focusSelection);
            }
            else
            {
                holder.editText.setSelection(holder.editText.getText().length());
            }

            focusPosition = -1;
            focusSelection = -1;
        }

    }

    @Override
    public int getItemCount()
    {
        return items.size();
    }


    private class CustomTextWatcher implements TextWatcher
    {
        private int position;

        public void updatePosition(int position)
        {
            this.position = position;
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2){}

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2){}

        @Override
        public void afterTextChanged(Editable editable)
        {
            if(position >= 0 && position < items.size())
            {
                items.get(position).setText(editable.toString());
            }
        }
    }


    public class ViewHolder extends RecyclerView.ViewHolder
    {
        private EditText editText;
        private CustomTextWatcher textWatcher;

        public ViewHolder(@NonNull View itemView, CustomTextWatcher watcher)
        {
            super(itemView);
            editText = itemView.findViewById(R.id.editTextBlock);
            textWatcher = watcher;

            editText.setOnKeyListener(new View.OnKeyListener()
            {

                @Override
                public boolean onKey(View view, int i, KeyEvent keyEvent)
                {
                    if(keyEvent.getAction() == KeyEvent.ACTION_DOWN
                    && i == KeyEvent.KEYCODE_ENTER
                    && editText.getSelectionStart() == editText.getSelectionEnd())
                    {
                        int currPos = getAdapterPosition();
                        if(currPos != RecyclerView.NO_POSITION)
                        {
                            int nextPos = currPos + 1;
                            int currSel = editText.getSelectionStart();

                            TextBlock currBlock = items.get(currPos);
                            TextBlock newBlock =
                                    new TextBlock(currBlock.removeTextFromSel(currSel));
                            items.add(nextPos, newBlock);

                            focusPosition = nextPos;
                            focusSelection = 0;

                            notifyItemInserted(nextPos);
                            notifyItemChanged(currPos, items.size() - currPos);
                            return true;
                        }
                    }
                    else if(keyEvent.getAction() == KeyEvent.ACTION_DOWN
                    && i == KeyEvent.KEYCODE_DEL
                    && editText.getSelectionStart() == 0
                    && editText.getSelectionEnd() == 0)
                    {
                        int currPos = getAdapterPosition();
                        if(currPos != RecyclerView.NO_POSITION && currPos > 0)
                        {
                            int beforePos = currPos - 1;
                            TextBlock beforeBlock = items.get(beforePos);
                            TextBlock currBlock = items.get(currPos);

                            focusSelection = beforeBlock.getText().length();
                            beforeBlock.addText(currBlock.getText());
                            items.remove(currPos);
                            focusPosition = beforePos;

                            notifyItemRemoved(currPos);
                            notifyItemChanged(beforePos, items.size() - beforePos);

                            return true;
                        }
                    }

                    return false;
                }
            });
        }
    }
}
