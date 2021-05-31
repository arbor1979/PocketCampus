package com.ruanyun.campus.teacher.util;

import android.content.Context;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.Toast;

public class MaxLengthWatcher implements TextWatcher {

    private int maxLen = 0;
    private EditText editText = null;
    private Context ctx = null;
    private static Toast mToast;

    public MaxLengthWatcher(int maxLen, EditText editText, Context ctx) {
        this.maxLen = maxLen;
        this.editText = editText;
        this.ctx=ctx;
    }

    public void afterTextChanged(Editable arg0) {
        // TODO Auto-generated method stub

    }

    public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                  int arg3) {
        // TODO Auto-generated method stub

    }

    public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
        // TODO Auto-generated method stub
        Editable editable = editText.getText();
        int len = editable.length();

        if (len > maxLen) {
            int selEndIndex = Selection.getSelectionEnd(editable);
            String str = editable.toString();
            //截取新字符串
            String newStr = str.substring(0, maxLen);
            editText.setText(newStr);
            editable = editText.getText();

            //新字符串的长度
            int newLen = editable.length();
            //旧光标位置超过字符串长度
            if (selEndIndex > newLen) {
                selEndIndex = editable.length();
            }
            //设置新光标所在的位置
            Selection.setSelection(editable, selEndIndex);
            len=maxLen;
        }
        String tipmsg="字符数:"+len + "/" + maxLen;
        if(mToast!=null)
            mToast.cancel();
        mToast=Toast.makeText(ctx,tipmsg,Toast.LENGTH_SHORT);
        mToast.setGravity(Gravity.CENTER, 0, 0);
        mToast.show();

    }
}