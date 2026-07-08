package com.example.stampit;

import android.graphics.Bitmap;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.BackgroundColorSpan;
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
        ViewHolder holder = new ViewHolder(view, new CustomTextWatcher());

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position)
    {
        TextBlock item = items.get(position);

        holder.textWatcher.updatePosition(position);
        holder.editText.removeTextChangedListener(holder.textWatcher);



        if(item.isBitmapped())
        {
            Bitmap bitmap = item.getBitmap();
            SpannableStringBuilder ssb = new SpannableStringBuilder("\uFFFC");
            ImageSpan imageSpan = new ImageSpan(holder.editText.getContext(), bitmap,
                    ImageSpan.ALIGN_BOTTOM);
            ssb.setSpan(imageSpan, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            holder.editText.setText(ssb);
        }
        else
        {
            holder.editText.setText(item.getText());
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

        if(currItem.getText().length() == 0)
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
        public void beforeTextChanged(CharSequence s, int start, int count, int after)
        {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count)
        {

        }

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
                    CharSequence newText = editable.subSequence(1, editable.length());

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


            items.get(position).setText(editable);

        }
    }




















    public class ViewHolder extends RecyclerView.ViewHolder
    {
        private CursorNotifyEditText editText;
        private CustomTextWatcher textWatcher;

        public ViewHolder(@NonNull View itemView, CustomTextWatcher watcher)
        {
            super(itemView);
            editText = itemView.findViewById(R.id.editTextBlock);
            textWatcher = watcher;


            editText.setOnSelectionChangedListener(new CursorNotifyEditText.OnSelectionChangedListener()
            {
                @Override
                public void onSelectionChanged(int selStart, int selEnd)
                {
                    if(selStart == selEnd)
                    {
                        View linearParent = (View) attachedRecyclerView.getParent();
                        if(linearParent != null)
                        {
                            Note rootNote = (Note) linearParent.getParent();
                            if(rootNote != null)
                            {
                                Editable editable = editText.getText();
                                if(selStart > 0)
                                {
                                    AbsoluteSizeSpan[] spans = ((Spannable) editable)
                                            .getSpans(selStart - 1, selStart,
                                                    AbsoluteSizeSpan.class);
                                    if(spans != null && spans.length > 0)
                                    {
                                        rootNote.setTextSizeSpinner(spans[0].getSize());
                                        return;
                                    }
                                }
                                else
                                {
                                    AbsoluteSizeSpan[] spans = ((Spannable) editable)
                                            .getSpans(0, 0, AbsoluteSizeSpan.class);
                                    if(spans != null && spans.length > 0)
                                    {
                                        rootNote.setTextSizeSpinner(spans[0].getSize());
                                        return;
                                    }

                                }

                                rootNote.setTextSizeSpinner(17);
                            }
                        }
                    }
                }
            });

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


    public void setCurrTextSize(int newSize)
    {

        if(focusPosition != -1 && attachedRecyclerView != null)
        {
            RecyclerView.ViewHolder holder
                    = attachedRecyclerView.findViewHolderForAdapterPosition(focusPosition);
            if(holder instanceof ViewHolder)
            {
                EditText et = ((ViewHolder) holder).editText;

                int start = et.getSelectionStart();
                int end = et.getSelectionEnd();
                if(start != end)
                {
                    Spannable spannable = et.getText();
                    AbsoluteSizeSpan[] oldSpans = spannable.getSpans(start, end,
                            AbsoluteSizeSpan.class);
                    // (3) : 가져오고자 하는 스팬의 타입 지정. Ex) AbsoluteSizeSpan, BackgroundColorSpan..

                    for(AbsoluteSizeSpan span : oldSpans)
                    {
                        spannable.removeSpan(span);
                    }
                    spannable.setSpan(new AbsoluteSizeSpan(newSize, true) ,
                            // dip(true) 일시, 사이즈의 단위는 dp. false 일시, 사이즈의 단위는 pixel
                            start, end, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                    // SPAN_EXCLUSIVE_EXCLUSIVE : start, end 인근에 텍스트가 추가돼도, 스팬을 적용 안함
                    // SPAN_INCLUSIVE_INCLUSIVE : start 앞에 텍스트가 추가되면, 스팬 적용. 뒤는 적용 안함
                    // SPAN_EXCLUSIVE_INCLUSIVE : end 뒤에 텍스트가 추가되면, 스팬 적용. 앞은 적용 안함
                    // SPAN_INCLUSIVE_INCLUSIVE : start, end 인근에 텍스트가 추가되면, 모두 스팬 적용
                }
                else
                {
                    Spannable spannable = et.getText();
                    AbsoluteSizeSpan[] oldSpans = spannable.getSpans(start, start,
                            AbsoluteSizeSpan.class);
                    for (AbsoluteSizeSpan span : oldSpans) {
                        spannable.removeSpan(span);
                    }
                    et.getText().setSpan(new AbsoluteSizeSpan(newSize, true),
                            start, start, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                }
            }
        }
    }
}
