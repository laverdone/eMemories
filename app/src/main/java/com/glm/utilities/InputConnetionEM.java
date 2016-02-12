package com.glm.utilities;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.CorrectionInfo;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.InputMethodManager;

import com.glm.view.TextureHandWrite;

/**
 * Created by gianluca on 26/11/13.
 */
public class InputConnetionEM extends BaseInputConnection{
    private TextureHandWrite mSurface;
    private final InputMethodManager mIMM;
    private final View mTargetView;
    public InputConnetionEM(View targetView, boolean fullEditor) {
        super(targetView, fullEditor);
        mTargetView=targetView;
        mIMM = (InputMethodManager)targetView.getContext().getSystemService(
                Context.INPUT_METHOD_SERVICE);
    }

    @Override
    public CharSequence getTextBeforeCursor(int i, int i2) {
        return null;
    }

    @Override
    public CharSequence getTextAfterCursor(int i, int i2) {
        return null;
    }

    @Override
    public CharSequence getSelectedText(int i) {
        return null;
    }

    @Override
    public int getCursorCapsMode(int i) {
        return 0;
    }

    @Override
    public ExtractedText getExtractedText(ExtractedTextRequest extractedTextRequest, int i) {

        return new ExtractedText();
    }

    @Override
    public boolean deleteSurroundingText(int beforeLength, int afterLength) {
        mSurface.deleteCharRowString(mSurface.iCurrentRow);
        mSurface.drawTextOnSurface();
        Log.d(this.getClass().getCanonicalName(), "deleteSurroundingText");
        return true;
    }

    @Override
    public boolean setComposingText(CharSequence charSequence, int i) {
        return false;
    }

    @Override
    public boolean setComposingRegion(int i, int i2) {
        return false;
    }

    @Override
    public boolean finishComposingText() {
        return false;
    }

    @Override
    public boolean commitText(CharSequence charSequence, int i) {
        if(charSequence.toString().compareToIgnoreCase("\n")==0){
            //NEW LINE
            mSurface.iCurrentRow++;
            mSurface.init(mSurface.iCurrentRow);
        }else{
            if(charSequence.length()>0)
                mSurface.setRowString(mSurface.iCurrentRow, charSequence.charAt(charSequence.length() - 1));
        }
        mSurface.drawTextOnSurface();
        //Log.d(this.getClass().getCanonicalName(), "commitText");
        return false;
    }

    @Override
    public boolean commitCompletion(CompletionInfo completionInfo) {
        return false;
    }

    @Override
    public boolean commitCorrection(CorrectionInfo correctionInfo) {
        return false;
    }

    @Override
    public boolean setSelection(int i, int i2) {
        return true;
    }

    @Override
    public boolean performEditorAction(int i) {
        mSurface.iCurrentRow++;
        mSurface.init(mSurface.iCurrentRow);
        mSurface.drawTextOnSurface();
        return true;
    }

    @Override
    public boolean performContextMenuAction(int i) {
        return false;
    }

    @Override
    public boolean beginBatchEdit() {
        return true;
    }

    @Override
    public boolean endBatchEdit() {

        return true;
    }

    @Override
    public boolean sendKeyEvent(KeyEvent keyEvent){
        //Log.d(this.getClass().getCanonicalName(), "sendKeyEvent");
        if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
            if(keyEvent.getKeyCode()==KeyEvent.KEYCODE_DEL){
                mSurface.deleteCharRowString(mSurface.iCurrentRow);

            }else if(keyEvent.getKeyCode()==KeyEvent.KEYCODE_ENTER){
                mSurface.iCurrentRow++;
                mSurface.init(mSurface.iCurrentRow);
            }else {
                char pressedKey = (char) keyEvent.getUnicodeChar();
                mSurface.setRowString(mSurface.iCurrentRow, pressedKey);
            }
            mSurface.drawTextOnSurface();
        }
        return true;
    }

    @Override
    public boolean clearMetaKeyStates(int i) {
        return false;
    }

    @Override
    public boolean reportFullscreenMode(boolean b) {
        return false;
    }

    @Override
    public boolean performPrivateCommand(String s, Bundle bundle) {
        return false;

    }



    public void setView(TextureHandWrite view) {
        mSurface = view;
    }
}
