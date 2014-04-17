package cs4295.customView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import cs4295.gesture.TouchManager;
import cs4295.math.Vector2D;
import cs4295.memecreator.R;

public class SandboxView extends View implements OnTouchListener {
	private SandboxView selfRef = this;
	
	private Bitmap bitmap;
	private int width;
	private int height;
	private Matrix transform = new Matrix();

	private Vector2D position = new Vector2D();
	private float scale = 1;
	private float angle = 0;

	private TouchManager touchManager = new TouchManager(2);
	private boolean onePress = true;
	private boolean noTranslate = true;
	private long startTime;
	private boolean isInitialized = false;
	private boolean showUpperText = false;
	private boolean showLowerText = false;
	private String upperText = "Nailed it!";
	private String lowerText = "Fuck Yeah!";
	
	

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
	
	public SandboxView(Context context, Bitmap bitmap, int dp) {
		super(context);

		setBitmap(bitmap,dp);

		setOnTouchListener(this);
	}
	
	public void setBitmap(Bitmap bitmap){
		this.bitmap = bitmap;
		this.width = bitmap.getWidth();
		this.height = bitmap.getHeight();
	}
	
	public void setBitmap(Bitmap bitmap, int dp){
		this.bitmap = bitmap;
		
		// Scaling
		float scaleValue = scale;
		if(bitmap.getWidth()>bitmap.getHeight())
			scaleValue = ((float)dp)/bitmap.getWidth();
		else
			scaleValue = ((float)dp)/bitmap.getHeight();
		
		Log.i("scaleValue",Float.toString(scaleValue));
		this.width = (int) (bitmap.getWidth()*scaleValue);
		this.height = (int) (bitmap.getHeight()*scaleValue);
	}
	
	private static float getDegreesFromRadians(float angle) {
		return (float)(angle * 180.0 / Math.PI);
	}
	
	private int determineMaxTextSize(String str, float maxWidth, float maxHeight)
	{
	    int size = 0;       
	    Paint paint = new Paint();

	    do {
	        paint.setTextSize(++ size);
	    } while(paint.measureText(str) < maxWidth && paint.getFontMetrics().top<maxHeight);

	    return size;
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
		paint.setColor(0xFF000000);
		canvas.drawColor(R.color.meme_background_color);

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

		// Add font to the canvase
		float upperTextSize = determineMaxTextSize(upperText,this.getWidth(),this.getHeight()/3);
		float lowerTextSize = determineMaxTextSize(lowerText,this.getWidth(),this.getHeight()/3);
		Typeface tf = Typeface.createFromAsset(getContext().getAssets(),"impact.ttf");
		
		Paint strokePaint = new Paint();
		strokePaint.setDither(true);
        strokePaint.setColor(0xFF000000);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeJoin(Paint.Join.ROUND);
        strokePaint.setStrokeCap(Paint.Cap.ROUND);
        strokePaint.setStrokeWidth(7);
        strokePaint.setTextAlign(Paint.Align.CENTER);
        strokePaint.setTypeface(tf);
        
        Paint textPaint = new Paint();
        textPaint.setDither(true);
        textPaint.setColor(0xFFFFFFFF);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTypeface(tf);
        
		if(showUpperText)
		{
			Paint text = textPaint;
			text.setTextSize(upperTextSize);
			Paint stroke = strokePaint;
			stroke.setTextSize(upperTextSize);
	        canvas.drawText(upperText,this.getWidth()/2,this.getHeight()/3,text);
	        canvas.drawText(upperText,this.getWidth()/2,this.getHeight()/3,stroke);
		}
		if(showLowerText)
		{
			Paint text = textPaint;
			text.setTextSize(lowerTextSize);
			Paint stroke = strokePaint;
			stroke.setTextSize(lowerTextSize);
	        canvas.drawText(lowerText,this.getWidth()/2,this.getHeight()/3*2,textPaint);
	        canvas.drawText(lowerText,this.getWidth()/2,this.getHeight()/3*2,strokePaint);
		}
        
		// For debugging
//		try {
//			paint.setColor(0xFF007F00);
//			canvas.drawCircle(vca.getX(), vca.getY(), 64, paint);
//			paint.setColor(0xFF7F0000);
//			canvas.drawCircle(vcb.getX(), vcb.getY(), 64, paint);
//			paint.setColor(0xFF00007F);
//			canvas.drawCircle(position.getX(),position.getY(), 64, paint);
//			
//			paint.setColor(0xFFFF0000);
//			canvas.drawLine(vpa.getX(), vpa.getY(), vpb.getX(), vpb.getY(), paint);
//			paint.setColor(0xFF00FF00);
//			canvas.drawLine(vca.getX(), vca.getY(), vcb.getX(), vcb.getY(), paint);
//		}
//		catch(NullPointerException e) {
//			// Just being lazy here...
//		}
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
				if(touchManager.moveDelta(0).getLength()>0)
					noTranslate = false;
			}
			else {
				if (touchManager.getPressCount() == 2) {
					onePress = false;
					vca = touchManager.getPoint(0);
					vpa = touchManager.getPreviousPoint(0);
					vcb = touchManager.getPoint(1);
					vpb = touchManager.getPreviousPoint(1);
					position.add(touchManager.moveDelta());
					if(touchManager.moveDelta(0).getLength()>0)
						noTranslate = false;
					
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
		if (event.getAction() == MotionEvent.ACTION_DOWN)
			startTime = System.nanoTime();
		else if (event.getAction() == MotionEvent.ACTION_UP) {
			long elapseTime = System.nanoTime() - startTime;
			Log.i("meme", "onTouchEvent time: " + elapseTime+" nanoseconds");
			Log.i("meme", (onePress)?"Only one touch point":"Two touch points");
			if(elapseTime < 100000000 && onePress && noTranslate) 
				selfRef.onClick(event.getX(),event.getY());
			onePress = true;
			noTranslate = true;
		}
		return true;
	}

	private void onClick(float x, float y) {
		// TODO Auto-generated method stub
		Log.i("meme","OnClick is called");
		Log.i("meme","X: "+x);
		Log.i("meme","Y: "+y);
		int divideRegion = 3;
		if(y<this.getHeight()/divideRegion)
			showUpperText = !showUpperText;
		else if(y>this.getHeight()/divideRegion*(divideRegion-1))
			showLowerText = !showLowerText;
	}
}
