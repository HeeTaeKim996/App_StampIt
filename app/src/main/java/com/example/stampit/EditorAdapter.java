package com.example.stampit;

import android.graphics.Bitmap;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
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
    private int nextFocusSelection = -1;
    private boolean isTextChangeHandling = false;
    private RecyclerView attachedRecyclerView;

    public EditorAdapter(List<TextBlock> items)
    {
        this.items = items;
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView)
    {
        super.onAttachedToRecyclerView(recyclerView);
        attachedRecyclerView = recyclerView;
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView)
    {
        super.onDetachedFromRecyclerView(recyclerView);
        attachedRecyclerView = null;
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



        String rawText = item.getText();
        if(item.isBitmapped())
        {
            Bitmap bitmap = item.getBitmap();
            SpannableStringBuilder ssb = new SpannableStringBuilder(rawText);
            int index = rawText.indexOf("\uFFFC");

            ImageSpan imageSpan = new ImageSpan(holder.editText.getContext(), bitmap,
                    ImageSpan.ALIGN_BOTTOM);
            ssb.setSpan(imageSpan, index, index + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            holder.editText.setText(ssb);
        }
        else
        {
            holder.editText.setText(rawText);
        }



        holder.editText.addTextChangedListener(holder.textWatcher);

        // [중요] 뷰 재활용시 기존 포커스 리스너 오작동 방지를 위해 초기화
        holder.editText.setOnFocusChangeListener(null);

        if(position == focusPosition)
        {
            holder.editText.requestFocus();
            if(nextFocusSelection != -1)
            {
                holder.editText.setSelection(nextFocusSelection);
            }
            else
            {
                holder.editText.setSelection(holder.editText.getText().length());
            }

            nextFocusSelection = -1;
        }

        holder.editText.setOnFocusChangeListener(new View.OnFocusChangeListener()
        {
            @Override
            public void onFocusChange(View view, boolean hasFocus)
            {
                if(hasFocus)
                {
                    focusPosition = holder.getAdapterPosition();
                }
            }
        });

    }

    @Override
    public int getItemCount()
    {
        return items.size();
    }




    public void InsertImage(Bitmap bitmap)
    {
        if(focusPosition == -1 || focusPosition >= items.size()) return;
        TextBlock currItem = items.get(focusPosition);

        if(currItem.getText().isEmpty())
        {
            currItem.setBitmap(bitmap);
            notifyItemChanged(focusPosition);
        }
        else
        {
            int beforePos = focusPosition++;
            TextBlock newBlock = new TextBlock("");
            newBlock.setBitmap(bitmap);

            items.add(focusPosition, newBlock);
            nextFocusSelection = 1;

            notifyItemInserted(focusPosition);
            notifyItemChanged(beforePos);

            attachedRecyclerView.scrollToPosition(focusPosition);
        }
    }







    private class CustomTextWatcher implements TextWatcher
    {
        private int position;

        public void updatePosition(int position)
        {
            this.position = position;
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2){}

        @Override
        public void afterTextChanged(Editable editable)
        {
            if(position < 0 || position >= items.size()) return;
            if(isTextChangeHandling) return;

            TextBlock changingBlock = items.get(position);
            if(changingBlock.isBitmapped())
            {
                // '\uFFFC' 가 지워졌음
                if(editable.length() == 0)
                {
                    changingBlock.setBitmap(null);
                }
                // 이미지가 있는데, 이미지 옆에 텍스트 입력을 시했기에, 다음 블록으로 이전
                else
                {
                    String newText = editable.toString().substring(1, editable.length());

                    isTextChangeHandling = true;
                    editable.delete(1, editable.length());
                    isTextChangeHandling = false;



                    TextBlock newBlock = new TextBlock(newText);
                    focusPosition = position + 1;
                    items.add(focusPosition, newBlock);
                    nextFocusSelection = newText.length();

                    notifyItemInserted(focusPosition);
                    attachedRecyclerView.scrollToPosition(focusPosition);

                    return;
                }
            }


            items.get(position).setText(editable.toString());
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
                            nextFocusSelection = 0;

                            notifyItemInserted(nextPos);

                            RecyclerView recyclerView = (RecyclerView) view.getParent();
                            recyclerView.scrollToPosition(nextPos);

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

                            nextFocusSelection = beforeBlock.getText().length();
                            beforeBlock.addText(currBlock.getText());
                            items.remove(currPos);
                            focusPosition = beforePos;

                            notifyItemRemoved(currPos);
                            notifyItemChanged(beforePos);

                            return true;
                        }
                    }

                    return false;
                }
            });
        }
    }
}
