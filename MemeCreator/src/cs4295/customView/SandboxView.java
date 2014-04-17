package cs4295.customView;

import java.util.Random;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.Editable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
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
	private String upperText = null;
	private String lowerText = null;
	private Typeface tf = Typeface.createFromAsset(getContext().getAssets(),
			"impact.ttf");
	private Paint strokePaint;
	private Paint textPaint;
	private float upperTextSize;
	private float lowerTextSize;
	private String[] sample_memes = getResources().getStringArray(
			R.array.sample_meme);

	// Debug helpers to draw lines between the two touch points
	private Vector2D vca = null;
	private Vector2D vcb = null;
	private Vector2D vpa = null;
	private Vector2D vpb = null;
	private Vector2D middlePoint = null;

	private Bitmap backup_bitmap;
	private int backup_width;
	private int backup_height;

	public SandboxView(Context context, Bitmap bitmap) {
		super(context);

		setBitmap(bitmap);
		setPaints();
		backup();

		setOnTouchListener(this);
	}

	public SandboxView(Context context, Bitmap bitmap, int dp) {
		super(context);

		setBitmap(bitmap, dp);
		setPaints();
		backup();

		setOnTouchListener(this);
	}

	private void backup() {
		this.backup_bitmap = this.bitmap.copy(this.bitmap.getConfig(), true);
		this.backup_width = this.width;
		this.backup_height = this.height;
	}

	public void reset() {
		this.bitmap = this.backup_bitmap.copy(this.backup_bitmap.getConfig(),
				true);
		this.width = this.backup_width;
		this.height = this.backup_height;
		this.transform = new Matrix();
		this.position = new Vector2D();
		this.scale = 1;
		this.angle = 0;
		this.onePress = true;

		this.noTranslate = true;
		this.isInitialized = false;
		// this.showUpperText = false;
		// this.showLowerText = false;
		// this.upperText = null;
		// this.lowerText = null;

		this.invalidate();
	}

	public void setPaints() {
		// Initialize default paint style
		strokePaint = new Paint();
		strokePaint.setDither(true);
		strokePaint.setColor(0xFF000000);
		strokePaint.setStyle(Paint.Style.STROKE);
		strokePaint.setStrokeJoin(Paint.Join.ROUND);
		strokePaint.setStrokeCap(Paint.Cap.ROUND);
		strokePaint.setStrokeWidth(7);
		strokePaint.setTextAlign(Paint.Align.CENTER);
		strokePaint.setTypeface(tf);
		strokePaint.setFlags(Paint.ANTI_ALIAS_FLAG);
		textPaint = new Paint();
		textPaint.setDither(true);
		textPaint.setColor(0xFFFFFFFF);
		textPaint.setTextAlign(Paint.Align.CENTER);
		textPaint.setTypeface(tf);
		textPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
	}

	public void setBitmap(Bitmap bitmap) {
		this.bitmap = bitmap;
		this.width = bitmap.getWidth();
		this.height = bitmap.getHeight();
	}

	public void setBitmap(Bitmap bitmap, int dp) {
		this.bitmap = bitmap;

		// Scaling
		float scaleValue = scale;
		if (bitmap.getWidth() > bitmap.getHeight())
			scaleValue = ((float) dp) / bitmap.getWidth();
		else
			scaleValue = ((float) dp) / bitmap.getHeight();

		Log.i("scaleValue", Float.toString(scaleValue));
		this.width = (int) (bitmap.getWidth() * scaleValue);
		this.height = (int) (bitmap.getHeight() * scaleValue);
	}

	private static float getDegreesFromRadians(float angle) {
		return (float) (angle * 180.0 / Math.PI);
	}

	private int determineMaxTextSize(Paint paint, String str, float maxWidth,
			float maxHeight) {
		if (str != null) {
			int size = 0;
			Rect bounds = new Rect();
			do {
				paint.setTextSize(++size);
				paint.getTextBounds(str, 0, str.length(), bounds);
			} while (paint.measureText(str) < maxWidth
					&& bounds.height() < maxHeight);
			return size;
		}
		return 0;
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
		paint.setFlags(Paint.ANTI_ALIAS_FLAG);
		canvas.drawColor(R.color.meme_background_color);

		transform.reset();
		transform.postTranslate(-width / 2.0f, -height / 2.0f);
		if (middlePoint != null) {
			Matrix inverse = new Matrix();
			transform.invert(inverse);
			float x = middlePoint.getX();
			float y = middlePoint.getY();
			float[] touchPoint = new float[] { x, y };
			inverse.mapPoints(touchPoint);
			transform.postRotate(getDegreesFromRadians(angle), 0, 0);

			// paint.setColor(0xFF00007F);
			// canvas.drawCircle(position.getX(),position.getY(), 64, paint);
		} else
			transform.postRotate(getDegreesFromRadians(angle));
		transform.postScale(scale, scale);
		transform.postTranslate(position.getX(), position.getY());

		canvas.drawBitmap(bitmap, transform, paint);

		// Add font to the canvas
		if (showUpperText) {
			textPaint.setTextSize(upperTextSize);
			strokePaint.setTextSize(upperTextSize);
			Rect bounds = new Rect();
			strokePaint.getTextBounds(upperText, 0, upperText.length(), bounds);
			canvas.drawText(upperText, this.getWidth() / 2,
					bounds.height() + 20, textPaint);
			canvas.drawText(upperText, this.getWidth() / 2,
					bounds.height() + 20, strokePaint);
		}
		if (showLowerText) {
			textPaint.setTextSize(lowerTextSize);
			strokePaint.setTextSize(lowerTextSize);
			canvas.drawText(lowerText, this.getWidth() / 2,
					this.getHeight() - 30, textPaint);
			canvas.drawText(lowerText, this.getWidth() / 2,
					this.getHeight() - 30, strokePaint);
		}

		// For debugging
		// try {
		// paint.setColor(0xFF007F00);
		// canvas.drawCircle(vca.getX(), vca.getY(), 64, paint);
		// paint.setColor(0xFF7F0000);
		// canvas.drawCircle(vcb.getX(), vcb.getY(), 64, paint);
		// paint.setColor(0xFF00007F);
		// canvas.drawCircle(position.getX(),position.getY(), 64, paint);
		//
		// paint.setColor(0xFFFF0000);
		// canvas.drawLine(vpa.getX(), vpa.getY(), vpb.getX(), vpb.getY(),
		// paint);
		// paint.setColor(0xFF00FF00);
		// canvas.drawLine(vca.getX(), vca.getY(), vcb.getX(), vcb.getY(),
		// paint);
		// }
		// catch(NullPointerException e) {
		// // Just being lazy here...
		// }
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
				if (touchManager.moveDelta(0).getLength() > 0)
					noTranslate = false;
			} else {
				if (touchManager.getPressCount() == 2) {
					onePress = false;
					vca = touchManager.getPoint(0);
					vpa = touchManager.getPreviousPoint(0);
					vcb = touchManager.getPoint(1);
					vpb = touchManager.getPreviousPoint(1);
					position.add(touchManager.moveDelta());
					if (touchManager.moveDelta(0).getLength() > 3)
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
		} catch (Throwable t) {
			// So lazy...
		}
		if (event.getAction() == MotionEvent.ACTION_DOWN)
			startTime = System.nanoTime();
		else if (event.getAction() == MotionEvent.ACTION_UP) {
			long elapseTime = System.nanoTime() - startTime;
			Log.i("meme", "onTouchEvent time: " + elapseTime + " nanoseconds");
			Log.i("meme", (onePress) ? "Only one touch point"
					: "Two touch points");
			if (elapseTime < 100000000 && onePress && noTranslate)
				selfRef.onClick(event.getX(), event.getY());
			onePress = true;
			noTranslate = true;
		}
		return true;
	}

	private String getRandomMeme() {
		int randomIndex = new Random().nextInt(sample_memes.length);
		// int resId = getResources().getIdentifier(sample_memes[randomIndex],
		// "String", null);
		return sample_memes[randomIndex];
	}

	private void onClick(float x, float y) {
		int divideRegion = 3;
		final EditText input = new EditText(this.getContext());
		input.setHint(getRandomMeme());
		input.setSelectAllOnFocus(true);
		input.postDelayed(new Runnable(){
			@Override
			public void run(){
				InputMethodManager keyboard = (InputMethodManager)selfRef.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
				keyboard.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
			}
		}, 200);
		
		// Upper text dialog
		if (y < this.getHeight() / divideRegion) {
			this.setEnabled(false);
			if (upperText != null)
				input.setText(upperText);
			new AlertDialog.Builder(this.getContext())
					.setTitle(R.string.set_upper_text_dialog_title)
					.setMessage(R.string.set_meme_text_dialog_message)
					.setView(input)
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									Editable value = input.getText();
									if (value.toString().isEmpty())
										upperText = (String) input.getHint();
									else
										upperText = value.toString();

									// Determine the text sizes
									upperTextSize = determineMaxTextSize(
											strokePaint, upperText,
											selfRef.getWidth() * 0.9f,
											selfRef.getHeight() / 4);
									showUpperText = true;
									selfRef.setEnabled(true);
								}
							})
					.setNegativeButton("Cancel",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									upperText = null;
									showUpperText = false;
									selfRef.setEnabled(true);
								}
							}).show();
		}
		// Lower text dialog
		else if (y > this.getHeight() / divideRegion * (divideRegion - 1)) {
			this.setEnabled(false);
			if (lowerText != null)
				input.setText(lowerText);
			new AlertDialog.Builder(this.getContext())
					.setTitle(R.string.set_lower_text_dialog_title)
					.setMessage(R.string.set_meme_text_dialog_message)
					.setView(input)
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									Editable value = input.getText();
									if (value.toString().isEmpty())
										lowerText = (String) input.getHint();
									else
										lowerText = value.toString();

									// Determine the text sizes
									lowerTextSize = determineMaxTextSize(
											strokePaint, lowerText,
											selfRef.getWidth() * 0.9f,
											selfRef.getHeight() / 4);
									showLowerText = true;
									selfRef.setEnabled(true);
								}
							})
					.setNegativeButton("Cancel",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									lowerText = null;
									showLowerText = false;
									selfRef.setEnabled(true);
								}
							}).show();
		}
	}
}
