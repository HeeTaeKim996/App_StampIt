package com.example.stampit;

import android.graphics.Bitmap;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

import java.util.List;

public class EditorAdapter extends RecyclerView.Adapter<EditorAdapter.ViewHolder>
{
    private final List<TextBlock> items;
    private int focusPosition = -1;
    private int nextFocusSelection = -1;
    private boolean isTextChangeOnTextWatcher = false;
    private RecyclerView attachedRecyclerView;

    private TextFormat currTextFormat = new TextFormat();
    private TextFormat nextTextFormat = null;


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
                // [주의]   setOnFocusChangeListener 에 focusPosition = holder.getAdapterPosition();
                //         이 있고, 저 위에 보면 if(position == focusPosition) 이면 분기 처리가 있다.
                //          시발 좆같다. 이미 스파게티 코드 같다. 지금은 리사이클 뷰가 재활용된걸 본경험이 없어서,
                //          어떻게 될지 모르겠는데. 이 시발 리스터가 등록되고, 리사이클이 풀에 들어가고, 재활용
                //          될 때, 또 어떤 좆같은 일이 일어날지 장담 못하겠다. 그때읒ㅁ이면 또 좆같은 코드들이
                //          난잡해져있을텐데.
                //
                if(hasFocus)
                {
                    focusPosition = holder.getAdapterPosition();

                    int selStart = holder.editText.getSelectionStart();
                    int selEnd = holder.editText.getSelectionEnd();

                    if(selStart == selEnd)
                    {
                        if(selStart > 0)
                        {
                            OnCursorOnTexted(holder.editText, selStart);
                        }
                        else if(holder.editText.length() == 0)
                        {
                            setEditTextToCurrFormat(holder.editText);
                        }
                    }
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
        private int beforeLen;

        public void updatePosition(int position)
        {
            this.position = position;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after)
        {
            beforeLen = s.length();
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count)
        {

        }

        @Override
        public void afterTextChanged(Editable editable)
        {
            if(position < 0 || position >= items.size()) return;
            if(isTextChangeOnTextWatcher) return;

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

                    isTextChangeOnTextWatcher = true;
                    editable.delete(1, editable.length());
                    isTextChangeOnTextWatcher = false;



                    TextBlock newBlock = new TextBlock(newText);
                    focusPosition = position + 1;
                    items.add(focusPosition, newBlock);
                    nextFocusSelection = newText.length();

                    notifyItemInserted(focusPosition);
                    attachedRecyclerView.scrollToPosition(focusPosition);

                    return;
                }
            }
            else if(nextTextFormat != null && beforeLen < editable.length())
            {
                // [해결 핵심 로직]
                // 에디터 텍스트 결합 시 선택 영역이 없었던 상태에서 글자가 입력된 상황
                // 현재 포커스된 EditText를 찾아 커서 위치(선택 영역 끝)를 기준으로 방금 입력된 글자 위치를 파악함
                if (attachedRecyclerView != null)
                {
                    RecyclerView.ViewHolder holder = attachedRecyclerView.findViewHolderForAdapterPosition(position);
                    if (holder instanceof ViewHolder)
                    {
                        EditText et = ((ViewHolder) holder).editText;
                        truncateSpanAndMakeNewSpan(et);
                    }
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
                        if(selStart > 0)
                        {
                            OnCursorOnTexted(editText, selStart);
                        }
                        else if(editText.length() == 0)
                        {
                            setEditTextToCurrFormat(editText);
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


























    public void ChangeTextFormat(TextFormat newFormat)
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
                    setTextsToOtherFormat(spannable, start, end, newFormat);
                }
                else
                {
                    if(et.length() == 0)
                    {
                        if(currTextFormat.equals(newFormat)) return;
                        currTextFormat = newFormat;

                        setEditTextToCurrFormat(et);
                    }
                    else
                    {
                        nextTextFormat = newFormat;
                    }
                }
            }
        }
    }

    private void setTextsToOtherFormat(Spannable spannable, int start, int end, TextFormat newFormat)
    {
        removeAllSpanInWidth(spannable, start, end, AbsoluteSizeSpan.class);
        spannable.setSpan(new AbsoluteSizeSpan(TextFormat.textSize[newFormat.sizeIndex], true),
                // dip(true) 일시, 사이즈의 단위는 dp. false 일시, 사이즈의 단위는 pixel
                start, end, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);

        removeAllSpanInWidth(spannable, start, end, ForegroundColorSpan.class);
        spannable.setSpan(new ForegroundColorSpan(TextFormat.colorValue[newFormat.colorIndex]),
                start, end, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        // TODO : 서식에 다른 요소들 추가되면, 추가 처리



        // SPAN_EXCLUSIVE_EXCLUSIVE : start, end 인근에 텍스트가 추가돼도, 스팬을 적용 안함
        // SPAN_INCLUSIVE_INCLUSIVE : start 앞에 텍스트가 추가되면, 스팬 적용. 뒤는 적용 안함
        // SPAN_EXCLUSIVE_INCLUSIVE : end 뒤에 텍스트가 추가되면, 스팬 적용. 앞은 적용 안함
        // SPAN_INCLUSIVE_INCLUSIVE : start, end 인근에 텍스트가 추가되면, 모두 스팬 적용
    }
    private <T> void removeAllSpanInWidth(Spannable spannable, int start, int end, Class<T> t)
    {
        T[] spans = spannable.getSpans(start, end, t);
        for(T span : spans)
        {
            spannable.removeSpan(span);
        }
    }

    private void setEditTextToCurrFormat(EditText et)
    {
        et.setTextSize(TextFormat.textSize[currTextFormat.sizeIndex]);
        et.setTextColor(TextFormat.colorValue[currTextFormat.colorIndex]);
        // TODO : 서식에 다른 요소들 추가되면, 추가 처리
    }

    private void truncateSpanAndMakeNewSpan(EditText et)
    {
        int currCursor = et.getSelectionStart();

        int insertPos = currCursor - 1;

        if(insertPos < 0)
        {
            currTextFormat = nextTextFormat;
            nextTextFormat = null;
//            setEditTextToCurrFormat(et);
            return;
        }

        Editable editable = et.getText();

        isTextChangeOnTextWatcher = true;




        trimAndReapplySpan(editable, insertPos, currCursor, AbsoluteSizeSpan.class);
        trimAndReapplySpan(editable, insertPos, currCursor, ForegroundColorSpan.class);
        // TODO : 서식에 다른 요소들 추가되면, Truncate 추가 처리



        editable.setSpan(new AbsoluteSizeSpan(
                TextFormat.textSize[nextTextFormat.sizeIndex], true),
                insertPos, currCursor, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        editable.setSpan(new ForegroundColorSpan(TextFormat.colorValue[nextTextFormat.colorIndex]),
                insertPos, currCursor, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        // TODO : 서식에 다른 요소들 추가되면, 추가 처리


        currTextFormat = nextTextFormat;
        nextTextFormat = null;

        isTextChangeOnTextWatcher = false;
    }

    private <T> void trimAndReapplySpan(Editable editable, int insertPos, int currCursor,
                                        Class<T> spanClass)
    {
        T[] oldSpans = editable.getSpans(insertPos, currCursor, spanClass);
        for(T span : oldSpans)
        {
            int spanStart = editable.getSpanStart(span);

            editable.removeSpan(span);

            if(spanStart < insertPos)
            {
                editable.setSpan(span, spanStart, insertPos, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
            }
        }
    }

    public interface OnTextFormatChangedListener
    {
        void onTextFormatChanged(TextFormat textFormat);
    }
    public void setOnTextFormatChangedListener(OnTextFormatChangedListener listener)
    {
        textFormatChangedListener = listener;
    }
    private OnTextFormatChangedListener textFormatChangedListener;

    private void OnCursorOnTexted(EditText editText, int cursorPos)
    {
        if(nextTextFormat != null)
        {
            nextTextFormat = null;  //
        }

        boolean hasSpan = false;
        boolean changed = false;




        Editable editable = editText.getText();
        Spannable spannable = editable;
        AbsoluteSizeSpan[] sizeSpans = spannable.getSpans(cursorPos - 1, cursorPos,
                AbsoluteSizeSpan.class);
        if(sizeSpans != null && sizeSpans.length > 0)
        {
            hasSpan = true;
            int textSize = sizeSpans[0].getSize();
            if(TextFormat.textSize[currTextFormat.sizeIndex] != textSize)
            {
                for(int i = 0; i < TextFormat.textSize.length; i++)
                {
                    if(TextFormat.textSize[i] == textSize)
                    {
                        currTextFormat.sizeIndex = i;
                        changed = true;
                        break;
                    }
                }
            }
        }
        ForegroundColorSpan[] colorSpans = spannable.getSpans(cursorPos - 1, cursorPos,
                ForegroundColorSpan.class);
        if(colorSpans != null && colorSpans.length > 0)
        {
            hasSpan = true;
            int colorValue = colorSpans[0].getForegroundColor();
            if(TextFormat.colorValue[currTextFormat.colorIndex] != colorValue)
            {
                for(int i = 0; i < TextFormat.colorValue.length; i++)
                {
                    if(TextFormat.colorValue[i] == colorValue)
                    {
                        currTextFormat.colorIndex = i;
                        changed = true;
                        break;
                    }
                }
            }
        }
        // TODO : 서식에 다른 요소들 추가되면, Spannable 에서 추가 추출


        // Spannable 이 아니니, editText 에서 추출
        if(hasSpan == false)
        {
            // 이게 맞나 싶다. editText.getTextSize 만 유일하게 sp단위로 받지 못하고 픽셀 단위로만 받는다 한다.
            // (Spannable의 get,set, editText의 set은 sp단위가 가능한데. 저것만 유일하게 안된다 해서 아래처럼
            // 코드가 굉장히 좆같다
            float pixelSize = editText.getTextSize();
            float scaledDensity = editText.getContext().getResources().getDisplayMetrics().scaledDensity;
            int textSize = Math.round(pixelSize / scaledDensity);

            if(TextFormat.textSize[currTextFormat.sizeIndex] != textSize)
            {
                for(int i = 0; i < TextFormat.textSize.length; i++)
                {
                    if(TextFormat.textSize[i] == textSize)
                    {
                        currTextFormat.sizeIndex = i;
                        changed = true;
                        break;
                    }
                }
            }

            int colorValue = editText.getCurrentTextColor();
            if(TextFormat.colorValue[currTextFormat.colorIndex] != colorValue)
            {
                for(int i = 0; i < TextFormat.colorValue.length; i++)
                {
                    if(TextFormat.colorValue[i] == colorValue)
                    {
                        currTextFormat.colorIndex = i;
                        changed = true;
                        break;
                    }
                }
            }
            // TODO : 서식에 다른 요소들 추가되면, editText에서 다른 요소 추출
        }

        if(changed)
        {
            if(textFormatChangedListener != null)
            {
                textFormatChangedListener.onTextFormatChanged(currTextFormat);
            }
        }
    }
}
