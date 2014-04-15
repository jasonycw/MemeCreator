package cs4295.gesture;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

import cs4295.math.Vector2D;

public class SandboxView extends View implements OnTouchListener {

	private Bitmap bitmap;
	private int width;
	private int height;
	private Matrix transform = new Matrix();

	private Vector2D position = new Vector2D();
	private float scale = 1;
	private float angle = 0;

	private TouchManager touchManager = new TouchManager(2);
	private boolean isInitialized = false;

	// Debug helpers to draw lines between the two touch points
	private Vector2D vca = null;
	private Vector2D vcb = null;
	private Vector2D vpa = null;
	private Vector2D vpb = null;
	private Vector2D middlePoint = null;

	public SandboxView(Context context, Bitmap bitmap) {
		super(context);

		setBitmap(bitmap);

		setOnTouchListener(this);
	}
	
	public void setBitmap(Bitmap bitmap){
		this.bitmap = bitmap;
		this.width = bitmap.getWidth();
		this.height = bitmap.getHeight();
	}
	
	private static float getDegreesFromRadians(float angle) {
		return (float)(angle * 180.0 / Math.PI);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		if (!isInitialized) {
			int w = getWidth();
			int h = getHeight();
			position.set(w / 2, h / 2);
			isInitialized = true;
		}

		Paint paint = new Paint();

		transform.reset();
		transform.postTranslate(-width / 2.0f, -height / 2.0f);
		if(middlePoint!=null){
			Matrix inverse = new Matrix();
			transform.invert(inverse);
			float x = middlePoint.getX();
			float y = middlePoint.getY();
			float[] touchPoint = new float[] {x, y};
			inverse.mapPoints(touchPoint);
			transform.postRotate(getDegreesFromRadians(angle),0,0);
			
//			paint.setColor(0xFF00007F);
//			canvas.drawCircle(position.getX(),position.getY(), 64, paint);
		}else
			transform.postRotate(getDegreesFromRadians(angle));
		transform.postScale(scale, scale);
		transform.postTranslate(position.getX(), position.getY());

		canvas.drawBitmap(bitmap, transform, paint);

		try {
			paint.setColor(0xFF007F00);
			canvas.drawCircle(vca.getX(), vca.getY(), 64, paint);
			paint.setColor(0xFF7F0000);
			canvas.drawCircle(vcb.getX(), vcb.getY(), 64, paint);
			paint.setColor(0xFF00007F);
			canvas.drawCircle(position.getX(),position.getY(), 64, paint);
			
			paint.setColor(0xFFFF0000);
			canvas.drawLine(vpa.getX(), vpa.getY(), vpb.getX(), vpb.getY(), paint);
			paint.setColor(0xFF00FF00);
			canvas.drawLine(vca.getX(), vca.getY(), vcb.getX(), vcb.getY(), paint);
		}
		catch(NullPointerException e) {
			// Just being lazy here...
		}
	}


	@Override
	public boolean onTouch(View v, MotionEvent event) {
		vca = null;
		vcb = null;
		vpa = null;
		vpb = null;
		middlePoint = null;

		try {
			touchManager.update(event);
			
			if (touchManager.getPressCount() == 1) {
				vca = touchManager.getPoint(0);
				vpa = touchManager.getPreviousPoint(0);
				position.add(touchManager.moveDelta(0));
			}
			else {
				if (touchManager.getPressCount() == 2) {
					vca = touchManager.getPoint(0);
					vpa = touchManager.getPreviousPoint(0);
					vcb = touchManager.getPoint(1);
					vpb = touchManager.getPreviousPoint(1);
					position.add(touchManager.moveDelta());
					
					Vector2D current = touchManager.getVector(0, 1);
					Vector2D previous = touchManager.getPreviousVector(0, 1);
					float currentDistance = current.getLength();
					float previousDistance = previous.getLength();

					if (previousDistance != 0) {
						scale *= currentDistance / previousDistance;
					}
					
					middlePoint = touchManager.getMiddlePoint();
					angle -= Vector2D.getSignedAngleBetween(current, previous);
				}
			}

			invalidate();
		}
		catch(Throwable t) {
			// So lazy...
		}
		return true;
	}
}
