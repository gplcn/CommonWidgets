package com.common.widgets;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.StringRes;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.math.BigDecimal;
import java.text.NumberFormat;

@SuppressWarnings("WeakerAccess")
public class ChangeNumberDialog extends Dialog {
    private BigDecimal mMaxValue;
    private OnButtonClickListener listener;
    private EditText etInputText;
    private TextView tvTitle;
    private Button tvLeftButton;
    private Button tvRightButton;
    private int mDecimalLength = Integer.MAX_VALUE, mMaxTextLength = -1;
    private InputFilter lengthFilter = new InputFilter() {

        @Override
        public CharSequence filter(CharSequence source, int start, int end,
                                   Spanned dest, int dstart, int dend) {
            // source:当前输入的字符
            // start:输入字符的开始位置
            // end:输入字符的结束位置
            // dest：当前已显示的内容
            // dstart:当前光标开始位置
            // dent:当前光标结束位置
            if (mMaxTextLength > 0 && dest.length() >= mMaxTextLength) {
                return "";
            }
            if (dest.length() == 0 && source.equals(".")) {
                return "0.";
            }
            String dValue = dest.toString();
            String[] splitArray = dValue.split("\\.");
            int potIndex = dest.toString().indexOf('.');
            if (dstart > potIndex) {
                if (splitArray.length > 1) {
                    String dotValue = splitArray[1];
                    if (dotValue.length() >= mDecimalLength) {
                        return "";
                    }
                }
            }
            return null;
        }

    };
    public ChangeNumberDialog(Context context, BigDecimal oriValue, BigDecimal maxValue, int maxTextLength, int decimalLength) {
        super(context, R.style.dialog_style);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_simple_change_number);
        setCancelable(false);

        this.mDecimalLength = decimalLength;
        this.mMaxTextLength = maxTextLength;
        this.mMaxValue = maxValue;
        tvTitle = findViewById(R.id.tv_title);
        etInputText = findViewById(R.id.et_input_text);
        tvLeftButton = findViewById(R.id.tv_left_button);
        tvRightButton = findViewById(R.id.tv_right_button);

        final NumberFormat numberFormat = NumberFormat.getInstance();
        numberFormat.setGroupingUsed(false);
        String text = null==oriValue?"": numberFormat.format(oriValue);
        etInputText.setText(text);
        etInputText.setSelection(etInputText.getText().length());
        etInputText.setFilters(new InputFilter[]{lengthFilter});

        if (tvLeftButton != null) {
            tvLeftButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        if( listener.onLeftButtonClick(getInputText())){
                            dismiss();
                        }else {
                            return;
                        }
                    }
                    dismiss();
                }
            });
        }

        if (tvRightButton != null) {
            tvRightButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        if( listener.onRightButtonClick(getInputText())){
                            dismiss();
                        }else {
                            String text = null==mMaxValue?"": numberFormat.format(mMaxValue);
                            etInputText.setText(text);
                            return;
                        }
                    }
                    dismiss();
                }
            });
        }

        Window window = getWindow();
        if (window != null) {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }


    @Override
    public void dismiss() {
        hideKeyboard();
        super.dismiss();
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(etInputText.getWindowToken(), 0);
    }

    public ChangeNumberDialog title(String title) {
        tvTitle.setText(title);
        return this;
    }

    public ChangeNumberDialog title(@StringRes int resId) {
        tvTitle.setText(resId);
        return this;
    }

    public ChangeNumberDialog inputHint(String hint) {
        etInputText.setHint(hint);
        return this;
    }

    public ChangeNumberDialog inputHint(@StringRes int resId) {
        etInputText.setHint(resId);
        return this;
    }

    public ChangeNumberDialog inputType(int inputType) {
        etInputText.setInputType(inputType);
        return this;
    }

    public ChangeNumberDialog leftText(String leftButtonText) {
        tvLeftButton.setText(leftButtonText);
        return this;
    }

    public ChangeNumberDialog leftText(@StringRes int resId) {
        tvLeftButton.setText(resId);
        return this;
    }

    public ChangeNumberDialog leftTextColor(int color) {
        tvLeftButton.setTextColor(color);
        return this;
    }

    public ChangeNumberDialog rightText(String rightButtonText) {
        tvRightButton.setText(rightButtonText);
        return this;
    }

    public ChangeNumberDialog rightText(@StringRes int resId) {
        tvRightButton.setText(resId);
        return this;
    }

    public ChangeNumberDialog rightTextColor(int color) {
        tvRightButton.setTextColor(color);
        return this;
    }

    public String getInputText() {
        return etInputText.getText().toString().trim();
    }

    public ChangeNumberDialog buttonClickListener(OnButtonClickListener listener) {
        this.listener = listener;
        return this;
    }

    public interface OnButtonClickListener {
        boolean onLeftButtonClick(String inputText);

        boolean onRightButtonClick(String inputText);
    }
}
