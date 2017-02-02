package cs4295.customView;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

public class ResizableImageView extends ImageView {

	public ResizableImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		Drawable d = this.getDrawable();

		if (d != null) {
			// Set the height of the view to fit content width
			final int width = MeasureSpec.getSize(widthMeasureSpec);
			final int height = (int) Math.ceil((float) width
					* (float) d.getIntrinsicHeight()
					/ (float) d.getIntrinsicWidth());
			this.setMeasuredDimension(width, height);
		} else {
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		}
	}

}