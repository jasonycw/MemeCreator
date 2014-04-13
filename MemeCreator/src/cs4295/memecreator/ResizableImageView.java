package cs4295.memecreator;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

public class ResizableImageView extends ImageView {

    public ResizableImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override 
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
         Drawable d = this.getDrawable();

         if(d!=null){
                 // ceil not round - avoid thin vertical gaps along the left/right edges
                 final int width = MeasureSpec.getSize(widthMeasureSpec);
                 
                 
                 Log.i("Test", "getIntrinsicWidth = " + Float.toString((float) d.getIntrinsicWidth()));
                 Log.i("Test", "getIntrinsicHeight = " + Float.toString((float) d.getIntrinsicHeight()));
                 final int height = (int) Math.ceil((float) width * (float) d.getIntrinsicHeight() / (float) d.getIntrinsicWidth());
                 
                 Log.i("Test", "width = " + Float.toString(width));
                 Log.i("Test", "height = " + Float.toString(height));
                 this.setMeasuredDimension(width, height);
         }else{
                 super.onMeasure(widthMeasureSpec, heightMeasureSpec);
         }
    }

}