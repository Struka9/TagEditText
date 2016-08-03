package presidente.oscar.tagedittext;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.GradientDrawable;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;

/**
 * Created by oscarr on 8/2/16.
 */
public class TagEditText extends MultiAutoCompleteTextView {

    private LayoutInflater mLayoutInflater;

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
        mLayoutInflater = LayoutInflater.from(getContext());

        //Whenever the user presses "Enter" we add the text
        setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP) {
                    TagEditText.this.append(",");
                }
                return true;
            }
        });

        addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count >= 1) {
                    if (s.charAt(start) == ',') {
                        setTags();
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
    }

    private void setTags() {
        Editable editable = getText();

        if (editable.toString().contains(",")) {
            SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(editable);

            String[] tags = editable.toString().split(",");

            int x = 0;
            for (String tag : tags) {
                TextView tv = (TextView) mLayoutInflater.inflate(R.layout.tag_layout, null, false);
                tv.setText(tag);

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

                spannableStringBuilder.setSpan(new ImageSpan(bitmapDrawable), x, x + tag.length(),  Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                x += tag.length() + 1;

            }

            setText(spannableStringBuilder);
            setSelection(getText().length());
        }
    }
}
