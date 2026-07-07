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
    private int currTextSize = 17;

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
            currItem.setText("");
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
        private int beforeLength = 0;

        public void updatePosition(int position)
        {
            this.position = position;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after)
        {
            beforeLength = s.length();
            // 적용되기 전의 글자 수를 저장. 현재는 beforeLength를 사용 안하지만,
            // 추후 글자수 제한 때 초과시 beforeLength 까지만으로 자르거나,
            // beforeLength < 현재 길이 로 글자 추가, beforeLength > 현재 길이 로 글자 삭제 판별 등에 사용
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count)
        {
            if(count > 0 && s instanceof Spannable)
            {
                Spannable spannable = (Spannable)s;

                if(start > 0)
                {
                    AbsoluteSizeSpan[] existingSpans = spannable.getSpans(start - 1, start,
                            AbsoluteSizeSpan.class);
                    boolean merged = false;
                    for(AbsoluteSizeSpan span : existingSpans)
                    {
                        if(span == s)
                        {
                            merged = true;
                            break;
                        }
                    }
                    if(merged)
                    {
                        return;
                    }
                }

                spannable.setSpan(new AbsoluteSizeSpan(currTextSize, true),
                        start, start + count, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                // 어차피 매번 글자가 입력되기에 SPAN_EXCLUSIVE_EXCLUSIVE 로 해도 글자 사이즈 적용은 되지만,
                // EXCLUSIVE_INCLUSIVE 를 하면 뒤에 글자가 입력될 때 기존 글자와 같은 SPAN 으로 묶여 SPAN
                // 관리가 수월하다 함. EXCLUSIVE_EXCLUSIVE 로 하면 동일한 스팬들이 인접해도 각각의 스팬으로 생성
            }
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

            if(changingBlock.getText() != editable)
            {
                items.get(position).setText(editable);
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
        currTextSize = newSize;

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
                            start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    // SPAN_EXCLUSIVE_EXCLUSIVE : start, end 인근에 텍스트가 추가돼도, 스팬을 적용 안함
                    // SPAN_INCLUSIVE_INCLUSIVE : start 앞에 텍스트가 추가되면, 스팬 적용. 뒤는 적용 안함
                    // SPAN_EXCLUSIVE_INCLUSIVE : end 뒤에 텍스트가 추가되면, 스팬 적용. 앞은 적용 안함
                    // SPAN_INCLUSIVE_INCLUSIVE : start, end 인근에 텍스트가 추가되면, 모두 스팬 적용
                }
            }
        }
    }
}
