package com.common.widgets;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import java.math.BigDecimal;
import java.text.NumberFormat;

public class FlexNumberPicker extends FrameLayout {

    private static final String TAG = "NumberPicker";
    protected static final int ANIMATION_DURATION = 300;
    protected ViewGroup mContent;
    protected int mExpectWidth, mExpectHeight;
    public  int mMaxTextLength = -1;
    protected int mCollapsedIconRes = 0;
    private OnLimitListener mOnLimitListener;
    private static final NumberFormat numberInstance = NumberFormat.getNumberInstance();
    private Runnable mCriticalityAction;
    String mMinLimitNoticePri = "起订量";
    private String mMaxLimitNoticePrefix="限定量";
    private final String mErrorInput = "输入不合法";

    {
        numberInstance.setGroupingUsed(false);
    }
    public FlexNumberPicker(Context context) {
        this(context, null);
    }

    public FlexNumberPicker(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }


    protected ImageView btnNpPlus, btnNpMinus;
    protected View leftArea;
    protected boolean mCollapsed = true;
    private BigDecimal mValue = BigDecimal.ZERO;
    protected boolean mAutoCollapsed = true;
    private BigDecimal minValue = BigDecimal.ZERO;
    private BigDecimal maxValue;
    private EditText mEditText;
    protected boolean isUnFolding = false; //正在播放展开动画
    protected ValueAnimator valueAnimator;
    protected boolean isReverse = false;
    private int maxDecimal;
    private boolean reactLoseFocus;
    private String dialogTitle;
    private OnClickListener editTextClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            ChangeNumberDialog changeNumberDialog = new ChangeNumberDialog(getContext(), mValue, maxValue, mMaxTextLength, maxDecimal);
            if (!TextUtils.isEmpty(dialogTitle)) {
                changeNumberDialog.title(dialogTitle);
            }
            changeNumberDialog.buttonClickListener(new ChangeNumberDialog.OnButtonClickListener() {
                @Override
                public boolean onLeftButtonClick(String inputText) {
                    return true;
                }

                @Override
                public boolean onRightButtonClick(String inputText) {
                    BigDecimal newValue;
                    if (TextUtils.isEmpty(inputText)) {
                        newValue = BigDecimal.ZERO;
                    } else {
                        try {
                            newValue = new BigDecimal(inputText);
                        } catch (Exception e) {
                            ToastUtils.showShortToast(getContext(), mErrorInput);
                            return false;
                        }
                    }

                    if (maxValue != null && (newValue.compareTo(maxValue) > 0)) {
                        showMaxLimitNotice();
                        return false;
                    }
                    if(newValue.compareTo(BigDecimal.ZERO)!=0 && newValue.compareTo(minValue)<0){
                        showMinLimitNotice();
                    }
                    changeValue(newValue, true, true);
                    return true;
                }
            });
            changeNumberDialog.show();
        }
    };

    public FlexNumberPicker(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        int initViewWidth= 110;
        int[] arr = new int[] { android.R.attr.layout_width, android.R.attr.layout_height };
        TypedArray typedArray = context.obtainStyledAttributes(attrs, arr);
        // MATCH_PARENT -1 WRAP_CONTENT-2
        try {
            TypedValue typeValue = typedArray.peekValue(0);
            if (typeValue.type == TypedValue.TYPE_DIMENSION) {// 0x10
                initViewWidth = (int) typedArray.getDimension(1, ViewGroup.LayoutParams.MATCH_PARENT);
                initViewWidth = TypedValue.complexToDimensionPixelSize(typeValue.data, getResources().getDisplayMetrics());
            } else if (typeValue.type == TypedValue.TYPE_FIRST_INT) {// 0x05
                initViewWidth = typeValue.data;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        typedArray.recycle();

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.FlexNumberPicker);
        mExpectWidth = ta.getDimensionPixelOffset(R.styleable.FlexNumberPicker_expect_layout_with,-1)-initViewWidth;
        mExpectHeight = ta.getDimensionPixelOffset(R.styleable.FlexNumberPicker_expect_layout_height,-1);
        mAutoCollapsed = ta.getBoolean(R.styleable.FlexNumberPicker_autoCollapse, true);
        minValue = new BigDecimal(ta.getInt(R.styleable.FlexNumberPicker_minValue, 0));
        reactLoseFocus = ta.getBoolean(R.styleable.FlexNumberPicker_reactLoseFocus, true);
        mMaxTextLength = ta.getInt(R.styleable.FlexNumberPicker_maxLength, -1);
        mContent = (ViewGroup) LayoutInflater.from(context).inflate(getContentLayoutRes(), null);
        addView(mContent);
        leftArea = mContent.findViewById(R.id.npLeftArea);
        initChildLayoutParams();
        btnNpPlus = mContent.findViewById(R.id.btnNpPlus);
        btnNpMinus = mContent.findViewById(R.id.btnNpMinus);
        mEditText = mContent.findViewById(R.id.npEditText);
        btnNpPlus.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickPlus();
            }
        });
        btnNpMinus.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickMinus();
            }
        });
        initAnimator();
        addEditTextListener();
        updateStatus(mCollapsed);
    }

    protected void initChildLayoutParams() {
     //do nothing
    }

    protected int getContentLayoutRes() {
        return R.layout.number_picker;
    }

    public void setCollapsedIconRes(int drawableRes){
        this.mCollapsedIconRes =drawableRes;
        if(mCollapsedIconRes!=0){
            updateStatus(mCollapsed);
        }
    }
    public void setWholeEnabled(boolean enabled) {
        super.setEnabled(enabled);
        btnNpMinus.setEnabled(enabled);
        btnNpPlus.setEnabled(enabled);
        if(enabled && editTextClickListener!=null){
            mEditText.setEnabled(true);
        }else {
            mEditText.setEnabled(false);
        }
    }

    private void addEditTextListener() {

        setEditTextClickListener(editTextClickListener);

        mEditText.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mEditText.setCursorVisible(true);
                return false;
            }
        });

        mEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
//                if (actionId == EditorInfo.IME_ACTION_DONE || actionId==EditorInfo.IME_ACTION_NEXT ||) {//点击软键盘完成控件时触发的行为
                //关闭光标并且关闭软键盘
                mEditText.setCursorVisible(false);
                onUseInputFinished();
//                }
                return false;
            }
        });
    }


    private void onUseInputFinished() {
        String text = mEditText.getText().toString();
        if (!TextUtils.isEmpty(text)) {
            try {
                BigDecimal value = new BigDecimal(text);
                if (value.scale() > maxDecimal) {
                    value = value.setScale(maxDecimal, BigDecimal.ROUND_HALF_UP);
                }
                if (minValue != null && value.compareTo(minValue) < 0) {
                    showMinLimitNotice();
                }
                if (maxValue != null && value.compareTo(maxValue) > 0) {
                    showMaxLimitNotice();
                }
                changeValue(value, mAutoCollapsed, true);
            } catch (NumberFormatException e) {
                ToastUtils.showShortToast(getContext(), mErrorInput);
                mEditText.setText("0");
                changeValue(BigDecimal.ZERO, mAutoCollapsed, true);
            }
        } else {
            mEditText.setText("0");
            changeValue(BigDecimal.ZERO, false, false);
            updateStatus(true);
        }
    }


    protected void initAnimator() {
        //正向为展开动画
        valueAnimator = ValueAnimator.ofFloat(1, 0);
        //监听变化过程
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                //获取当前值
                float percent = (float) animation.getAnimatedValue();
                updateLeftAreaView(percent);
            }
        });
        valueAnimator.setDuration(ANIMATION_DURATION);
        valueAnimator.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationStart(Animator animation) {
                btnNpPlus.setClickable(false);
                btnNpMinus.setClickable(false);
                leftArea.setVisibility(VISIBLE);
                isUnFolding = !isReverse;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                btnNpPlus.setClickable(true);
                btnNpMinus.setClickable(true);
                updateStatus(isReverse);
            }
        });
    }


    public void onClickPlus() {
        loseFocus();
        BigDecimal toValue;
        if (mValue.compareTo(minValue) < 0) {
            //只校验最小值
            showMinLimitNotice();
            toValue = minValue;
            changeValue(toValue, true && mAutoCollapsed, true,true,false);
            return;
        } else {
            toValue = mValue.add(BigDecimal.ONE);
        }
        if (maxValue != null && toValue.compareTo(maxValue) > 0) {
            showMaxLimitNotice();
            if(mValue.compareTo(maxValue) > 0){
                toValue = maxValue;
            }else if(mValue.compareTo(maxValue) <0){
                toValue = maxValue;
            }else  {
                return;
            }
        }
        changeValue(toValue, true && mAutoCollapsed, true);
    }

    private void showMinLimitNotice() {
        if(null != mOnLimitListener){
            mOnLimitListener.onMinLimit();
        }else {
            ToastUtils.showShortToast(getContext(), mMinLimitNoticePri + "：" + minValue);
        }
    }

    private void showMaxLimitNotice() {
        if(null != mOnLimitListener){
            mOnLimitListener.onMaxLimit();
        }else {
            ToastUtils.showShortToast(getContext(), mMaxLimitNoticePrefix + "：" + maxValue);
        }
    }

    private void onClickMinus() {
        loseFocus();
        BigDecimal toValue;
        //当前值大于最大值 -->最大值
        if (maxValue!=null && mValue.compareTo(maxValue) > 0) {
            showMaxLimitNotice();
            toValue = maxValue;
            changeValue(toValue, true && mAutoCollapsed, true,true,false);
            return;
        }
        //最大值小于最小值时 -->清零
        if(maxValue!=null && maxValue.compareTo(minValue)<0){
            changeValue(BigDecimal.ZERO, true && mAutoCollapsed, true);
            return;
        }

        if (mValue.compareTo(minValue) == 0 && mValue.compareTo(BigDecimal.ZERO) > 0) {
            toValue = BigDecimal.ZERO;
        } else {
            toValue = mValue.subtract(BigDecimal.ONE);
        }
        if (toValue.compareTo(minValue) < 0 && mValue.compareTo(minValue) > 0) {
            toValue = minValue;
            showMinLimitNotice();
        }
        if (toValue.compareTo(BigDecimal.ZERO) > 0 && toValue.compareTo(minValue) < 0) {
            showMinLimitNotice();
        }
        changeValue(toValue, true && mAutoCollapsed, true);
    }

    private void loseFocus() {
        if (mEditText.hasFocus()) {
            mEditText.clearFocus();
        }
        InputMethodManager im = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (im != null) {
            im.hideSoftInputFromWindow(getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }


    private void changeValue(BigDecimal value, boolean animate, boolean updateText) {
        changeValue(value, animate, updateText, true, true);
    }

    private void changeValue(BigDecimal value, boolean animate, boolean updateText, boolean notify, boolean checkLimit) {
        if (checkLimit) {
            //设置值超过最大值 自动用最大值替代，通过键盘输入数字后清除焦点确认值时同样要有此逻辑；
            if (maxValue != null && value.compareTo(maxValue) > 0) {
                value = maxValue;
                if (mValue.compareTo(value) == 0) {
                    mValue = mValue.subtract(BigDecimal.ONE);
                }
            }
            if (value.compareTo(minValue) < 0 && value.compareTo(BigDecimal.ZERO) != 0) {
                value = minValue;
                if (mValue.compareTo(value) == 0) {
                    mValue = mValue.subtract(BigDecimal.ONE);
                }
            }

        }

        if (mValue.compareTo(value) != 0) {
            mValue = value;
            if (mValue.scale() > maxDecimal) {
                mValue = mValue.setScale(maxDecimal, BigDecimal.ROUND_HALF_UP);
            }
            if (updateText) {
                String text = numberInstance.format(mValue);
                mEditText.setText(text);
                mEditText.setSelection(mEditText.getText().length());//将光标移至文字末尾
            }
            /*if (minValue > 0) {
                btnNpMinus.setEnabled(mValue > 0);
            } else {
                btnNpMinus.setEnabled(mValue > minValue);
            }
            btnNpPlus.setEnabled(mValue < maxValue);*/


            if (mValue.compareTo(BigDecimal.ZERO) == 0) {
                collapse();
                criticalityProcess(animate, notify);
            } else if (mCollapsed && !isUnFolding) {
                unfold();
                criticalityProcess(animate, notify);
            }else if (notify && mListener != null) {
                mListener.onValueChanged(mValue);
            }
        }
    }

    private void criticalityProcess(boolean animate, boolean notify) {
        if (!animate) {
            valueAnimator.end();
            if (notify && mListener != null) {
                mListener.onValueChanged(mValue);
            }
        } else {
            if(mCriticalityAction !=null){
                removeCallbacks(mCriticalityAction);
            }
            mCriticalityAction = () -> {
                if (notify && mListener != null) {
                    mListener.onValueChanged(mValue);
                }
            };
            postDelayed(mCriticalityAction, ANIMATION_DURATION/2);
        }
    }

    private void unfold() {
        stopAnimator();
        valueAnimator.start();
    }

    private void stopAnimator() {
        if (valueAnimator.isStarted()) {
            valueAnimator.end();
        }
    }


    protected void updateStatus(boolean collapsed) {
        mCollapsed = collapsed && mAutoCollapsed;
        if (mCollapsed) {
            updateLeftAreaView(1f);
            if(mCollapsedIconRes!=0){
                btnNpPlus.setImageResource(mCollapsedIconRes);
            }
            btnNpPlus.setBackgroundResource(R.drawable.square_bg);
            leftArea.setVisibility(GONE);
        } else {
            updateLeftAreaView(0f);
            if(mCollapsedIconRes!=0){
                btnNpPlus.setImageResource(R.drawable.np_plus_selector);
            }
            btnNpPlus.setBackgroundResource(R.drawable.square_bg_right);
        }
        isUnFolding = false;
        isReverse = false;
    }

    protected void updateLeftAreaView(float percent) {
//        ViewGroup.MarginLayoutParams
//                layoutParams = (ViewGroup.MarginLayoutParams) leftArea.getLayoutParams();
//        layoutParams.leftMargin = (int) (leftArea.getWidth() * percent);
//        leftArea.setLayoutParams(layoutParams);
//        leftArea.setAlpha((1f - percent));

        if(null==getLayoutParams())return;
        getLayoutParams().width = 120+(int) (mExpectWidth *(1-percent));
        leftArea.setAlpha((1f - percent));
        requestLayout();

    }

    private void collapse() {
        if (valueAnimator.isStarted()) {
            valueAnimator.end();
        }
        isReverse = true;
        valueAnimator.reverse();
    }


    /**
     * 设置可以切换到的最小值；如果达到最小值，是否自动折叠
     *
     * @param minValue      默认最小值为0
     * @param autoCollapsed 达到最小值时自动折叠,默认为true
     */
    public void setMinValue(BigDecimal minValue, boolean autoCollapsed) {
        mAutoCollapsed = autoCollapsed;
//        mEditText.setHint(minValue + "");
        this.minValue = minValue;
        //控件的值更新为最新的最小值
        changeValue(mValue, mAutoCollapsed, true, false, true);
    }

    public void setMaxValue(BigDecimal maxValue) {
//        if (maxValue != null && maxValue.compareTo(minValue) < 0) {
//            throw new IllegalArgumentException("maxValue(" + maxValue + ")can't be smaller than the minValue(" + minValue + ")!");
//        }
        this.maxValue = maxValue;
    }

    /**
     * 设置数值
     * <p>
     * 这个方法的调用应该在 {@link #setMinValue} 和{@link #setMaxValue(BigDecimal)} 之后
     *
     * @param value
     */
    public void setValue(BigDecimal value) {
        setValue(value, false);
    }

    /**
     * 设置数值
     * <p>
     * 这个方法的调用应该在 {@link #setMinValue} 和{@link #setMaxValue(BigDecimal)} 之后
     *
     * @param value
     * @param notify 是否回调设置值变更
     */
    public void setValue(BigDecimal value, boolean notify) {
        stopAnimator();
        if (value.compareTo(minValue) >= 0 && (maxValue == null || value.compareTo(maxValue) <= 0)) {
            changeValue(value, false, true, notify, true);
        } else {
            Log.w(TAG, "设置的值不在最小值和最大值之间，忽略");
        }
    }

    /**
     * 设置数值(设置时不检查最大最小值限制)
     * <p>
     *
     * @param value
     */
    public void setValueWithoutCHeckLimit(BigDecimal value) {
        stopAnimator();
        changeValue(value, false, true, false, false);
    }
    public void setValueWithoutCHeckLimit(BigDecimal value, boolean redLight) {
        stopAnimator();
        changeValue(value, false, true, false, false);
//        mEditText.setTextColor(ResUtil.getColor(redLight?R.color.light_red:R.color.common_title));
    }

    public void setEditTextClickListener(OnClickListener editTextClickListener) {
        this.editTextClickListener = editTextClickListener;
        mEditText.setOnClickListener(editTextClickListener);
    }

    public void setDialogTitle(String dialogTitle) {
        this.dialogTitle = dialogTitle;
    }

    public interface OnChangeListener {
        void onValueChanged(BigDecimal value);
    }

    private OnChangeListener mListener;

    /**
     * 设置数值改变时的回调
     *
     * @param listener
     */
    public void setOnChangeListener(OnChangeListener listener) {
        this.mListener = listener;
    }

    /**
     * 获取当前数值
     *
     * @return
     */
    public BigDecimal getValue() {
        return mValue;
    }

    /**
     * 设置小数位数最多保留几位
     * @param decimal
     */
    public void setMaxDecimal(int decimal) {
        if (decimal < 0) {
            throw new IllegalArgumentException("com.hecom.widget.NumberPicker.setMaxDecimal设定的小数位数必须是非负整数！");
        }
        this.maxDecimal = decimal;
    }

    public EditText getEditText() {
        return this.mEditText;
    }

    public void setOnLimitListener(OnLimitListener onLimitListener){
        this.mOnLimitListener = onLimitListener;
    }

    public interface OnLimitListener{
        void onMaxLimit();
        void onMinLimit();
    }

}
