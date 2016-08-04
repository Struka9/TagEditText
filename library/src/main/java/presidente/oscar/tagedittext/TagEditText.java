package presidente.oscar.tagedittext;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.GradientDrawable;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by oscarr on 8/2/16.
 */
public class TagEditText extends EditText {

    //TODO: Make sure we only add the tag if is not in the list
    private LayoutInflater mLayoutInflater;

    private boolean mShouldTriggerTextWatcher = true;

    private InputFilter mFilter = new InputFilter() {
        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {

            if (source != null && source.toString().contains(",")) {
                return "";
            }

            return null;
        }
    };

    private List<Tag> mTagList;


    public TagEditText(Context context) {
        this(context, null);
    }

    public TagEditText(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.editTextStyle);
    }

    public TagEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setMovementMethod(LinkMovementMethod.getInstance());
        mTagList = new ArrayList<>();

        //We don't want to allow ',' to be entered
        setFilters(new InputFilter[]{mFilter});
        mLayoutInflater = LayoutInflater.from(getContext());

        //Whenever the user presses "Enter" we add the text
        setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP) {
                    checkForNewTag();
                    setTags();
                }
                return true;
            }
        });

        addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (after < count && mShouldTriggerTextWatcher) {
                    //The user is deleting tags with the inputmethod
                    removeByPositionInText(start, count);
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {

        CharSequence text = getText();
        if (text != null) {
            if (selStart != text.length() || selEnd != text.length()) {
                setSelection(text.length(), text.length());
                return;
            }
        }
        super.onSelectionChanged(selStart, selEnd);
    }

    private void removeByTagValue(String tagValue) {
        int found = -1;
        int count = -1;

        for (int i = 0; i < mTagList.size(); i++) {
            Tag tag = mTagList.get(i);
            if (found != -1) {
                tag.from -= count;
                tag.to -= count;
            }

            if (tag.tagValue.compareTo(tagValue) == 0) {
                found = i;
                count = tag.to - tag.from;
            }

        }

        if (found != -1) {
            Tag removed = mTagList.remove(found);

            String textAsString = getText().toString();
            String firstSlice = textAsString.substring(0, removed.from);
            String secondSlice = textAsString.substring(removed.to, textAsString.length());

            mShouldTriggerTextWatcher = false;
            setText(firstSlice + secondSlice);
            mShouldTriggerTextWatcher = true;
            setTags();
        }
    }

    private void removeByPositionInText(int start, int count) {

        //It can actually be many tags at the same time
        int found = -1;
        for (int i = 0; i < mTagList.size(); i++) {
            Tag tag = mTagList.get(i);

            if (tag.from == start) {
                found = i;
                break;
            }

            if (found != -1) {
                tag.from -= count;
                tag.to -= count;
            }
        }

        if (found != -1) {
            mTagList.remove(found);
        }
    }

    private void checkForNewTag() {
        String wholeText = getText().toString();
        if (mTagList.size() > 0) {
            Tag lastTag = mTagList.get(mTagList.size() - 1);
            String newTagValue = wholeText.substring(lastTag.to);

            if (newTagValue.length() > 0) {
                Tag newTag = new Tag();
                newTag.from = lastTag.to;
                newTag.to = wholeText.length();
                newTag.tagValue = newTagValue;

                mTagList.add(newTag);
            }
            setTags();
        } else if (wholeText.length() > 0) {
            Tag newTag = new Tag();
            newTag.from = 0;
            newTag.to = wholeText.length();
            newTag.tagValue = wholeText;

            mTagList.add(newTag);
            setTags();
        }
    }

    private void setTags() {
        Editable editable = getText();
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(editable);

        for (int i = 0; i < mTagList.size(); i++) {
            final Tag tag = mTagList.get(i);
            TextView tv = (TextView) mLayoutInflater.inflate(R.layout.tag_layout, null, false);
            tv.setText(tag.tagValue);

            GradientDrawable d = (GradientDrawable) tv.getBackground();

            int measureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
            tv.measure(measureSpec, measureSpec);
            tv.layout(0, 0, tv.getMeasuredWidth(), tv.getMeasuredHeight());

            Bitmap b = Bitmap.createBitmap(tv.getWidth(), tv.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(b);
            canvas.translate(-tv.getScrollX(), -tv.getScrollY());
            tv.draw(canvas);
            tv.setDrawingCacheEnabled(true);

            Bitmap cacheBm = tv.getDrawingCache();
            Bitmap bitmap = cacheBm.copy(Bitmap.Config.ARGB_8888, true);
            tv.destroyDrawingCache();

            BitmapDrawable bitmapDrawable = new BitmapDrawable(getResources(), bitmap);
            bitmapDrawable.setBounds(0, 0, bitmapDrawable.getIntrinsicWidth(), bitmapDrawable.getIntrinsicHeight());

            spannableStringBuilder.setSpan(new ImageSpan(bitmapDrawable), tag.from, tag.to, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableStringBuilder.setSpan(new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    //FIXME: Calling this makes the app to crash, found that the frameworks returns a wrong offset(-1) and makes the Editor to throw an exception
                    //removeByTagValue(tag.tagValue);
                    //setSelection(getText().length());
                }
            }, tag.from, tag.to, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        setText(spannableStringBuilder);
        setSelection(getText().length());
    }

    private class Tag {
        String tagValue;
        //The indices in the text that represent the range of this tag
        int from;
        int to;
    }
}
