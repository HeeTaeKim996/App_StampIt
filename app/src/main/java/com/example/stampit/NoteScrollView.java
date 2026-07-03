package com.example.stampit;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.ScrollView;


public class NoteScrollView extends ScrollView
{
    private ScaleGestureDetector scaleDetector;
    private float scale = 1.f;
    private final float minScale = 1.f;
    private final float maxScale = 5.f;

    private float lastX;
    private float lastY;


    public NoteScrollView(Context context)
    {super(context);                        init(context);}
    public NoteScrollView(Context context, AttributeSet attrs)
    {super(context, attrs);                 init(context);}
    public NoteScrollView(Context context, AttributeSet attrs, int defStyleAttr)
    {super(context, attrs, defStyleAttr);   init(context);}

    private void init(Context context)
    {
        scaleDetector = new ScaleGestureDetector(context,
                new ScaleGestureDetector.SimpleOnScaleGestureListener()
                {
                    @Override
                    public boolean onScale(ScaleGestureDetector detector)
                    {
                        // getFocusX 는 눈에 보이는 화면 기준 좌표. getScrollX,Y 는 scroll 이 얼마나 
                        // 이동했는지를 나타냄
                        setPivotX(detector.getFocusX() + getScrollX());
                        setPivotY(detector.getFocusY() + getScrollY());

                        scale *= detector.getScaleFactor();
                        scale = Math.max(minScale, Math.min(maxScale, scale));

                        setScaleX(scale);
                        setScaleY(scale);

                        clampTranslation();

                        invalidate();
                        return true;
                    }
                });
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev)
    // 자식 뷰에 onTouchEvent 가 발동하기 전에, 위 함수가 먼저 발동됨
    {
        // 두 손가락 이상 터치시, 자식 뷰들의 터치 이벤트를 가로채서 줌 동작 수행
        if(ev.getPointerCount() > 1)
        {
            return true;
        }

        // 임시로 처리. 이게 맞나 모르겠다.
        if(scale > 1.f)
        {
            return true;
        }


        return super.onInterceptTouchEvent(ev);
        // retur true 일시, 자식 뷰의 onTouchEvent를 차단하며, 자신의 onTouchEvent 를 발동.
        // return false 일시, 차단하지 않음
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        // 손가락 개수와 상관없이, 이벤트의 시작부터 끝까지를 detector 가 감지해야 하므로, 항상 앞에 둔다
        scaleDetector.onTouchEvent(event);

        if(event.getPointerCount() > 1)
        {
            return true;
        }

        int action = event.getAction();
        switch(action)
        {
            case MotionEvent.ACTION_DOWN:
                lastX = event.getX();
                lastY = event.getY();
                break;

            case MotionEvent.ACTION_MOVE:
                if(scale > 1.f)
                {
                    float dx = event.getX() - lastX;
                    float dy = event.getY() - lastY;

                    // X 는 TranslationX 로 처리
                    float nextTranslationX = getTranslationX() + dx / scale * 1.3f; 
                    // @@@@@@@@@@@@@ TODO : 아래 clampTranslation 내 getRanslationX 같이 참조해서,
                    // getTranslationX 역할 질문하자

                    setTranslationX(nextTranslationX);

                    // Y는 ScrollView 기능 사용
                    scrollBy(0, (int)-dy);

                    clampTranslation();

                    lastX = event.getX();
                    lastY = event.getY();
                    return true;
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                break;
        }

        return super.onTouchEvent(event);

        // ACTION_DOWN 에서,
        // return true 일시, 현 View 가 이벤트를 소유하여,
        // 후속 이벤트(ACTION_MOVE, ACTION_UP..) 등을 받을 수 있지만,
        // return false 라면, 소유권을 갖지 않고, 부모 뷰에 onTouchEvent를 발동

        // ACTION_DOWN 제외에서는, true 일시, 소유권을 유지. false 일시, 소유권을 갖지 않고,
        // 아무도 소유권을 갖지 않음(이벤트 종료)
    }


    private void clampTranslation()
    {
        if(scale <= 1.f)
        {
            setTranslationX(0);
            return;
        }

        float width = getWidth();

        float maxTranslationX = (width * scale - width) / 2.f;

        float currTx = getTranslationX();
        if(currTx > maxTranslationX)
        {
            setTranslationX(maxTranslationX);
        }
        else if(currTx < -maxTranslationX)
        {
            setTranslationX(-maxTranslationX);
        }
    }

}
