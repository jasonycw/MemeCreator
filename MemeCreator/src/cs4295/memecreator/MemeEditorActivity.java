package cs4295.memecreator;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import cs4295.customView.SandboxView;

public class MemeEditorActivity extends Activity {
	private MemeEditorActivity selfRef;
	private SharedPreferences setting;
	private LinearLayout linlaHeaderProgress;
	private float memeEditorLayoutWidth;
	private float memeEditorLayoutHeight;
	private LinearLayout memeEditorLayout;
	private SandboxView sandboxView;
	private ImageView forwardButtonImageView;
	private Bitmap memeBitmap;
	private File cacheImage_forPassing;
	private File myDir;
	private String dataDir;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		this.setProgressBarIndeterminateVisibility(true);
		setContentView(R.layout.activity_meme_editor);
		selfRef = this;

		// Set the actioin bar style
		ActionBar actionBar = getActionBar();
		actionBar.setBackgroundDrawable(new ColorDrawable(Color
				.parseColor("#003C3C3C")));
		actionBar.setIcon(R.drawable.back_icon);
		actionBar.setHomeButtonEnabled(true);

		// Transparent bar on android 4.4 or above
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			Window window = getWindow();
			// Translucent status bar
			window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
					WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
			// Translucent navigation bar
			window.setFlags(
					WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION,
					WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
		}

		// Initialize progress bar
		linlaHeaderProgress = (LinearLayout) findViewById(R.id.linlaHeaderProgress);
		linlaHeaderProgress.bringToFront();

		// Get the data directory for the app
		PackageManager m = getPackageManager();
		dataDir = getPackageName();
		try {
			PackageInfo p = m.getPackageInfo(dataDir, 0);
			dataDir = p.applicationInfo.dataDir;
			myDir = new File(dataDir + "/cache");
			if (myDir.setWritable(true))
				Log.i("meme", "myDir is writable");
			else
				Log.i("meme", "myDir is not writable");
		} catch (NameNotFoundException e) {
			Log.w("yourtag", "Error Package name not found ", e);
		}

		// Get the intent and get the image path to be the meme image
		Intent shareIntent = getIntent();

		String imagePath = shareIntent.getStringExtra("cs4295.memcreator.imagePath");
		
//		Log.i("Happy", imagePath);
		
		// Create the SandboxView
		setting = getSharedPreferences("path", Context.MODE_PRIVATE);
		int memeSize = Integer.parseInt(setting.getString("image_size", "720"));
		Log.i("meme","memeSize = "+memeSize);
		memeEditorLayout = (LinearLayout) findViewById(R.id.memeEditorLayout);
		memeEditorLayout.setGravity(Gravity.CENTER);
		Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
		Log.i("path", bitmap.toString());
		sandboxView = new SandboxView(this, bitmap);
//		sandboxView.setLayoutParams(new LayoutParams(720, 720));
		sandboxView.setLayoutParams(new LayoutParams(memeSize, memeSize));

		// Scale the sand box and add it into the layout
		ViewTreeObserver vto2 = memeEditorLayout.getViewTreeObserver();
		vto2.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				// For getting the width and height of a dynamic layout during
				// onCreate
				memeEditorLayout.getViewTreeObserver()
						.removeGlobalOnLayoutListener(this);
				memeEditorLayoutWidth = memeEditorLayout.getHeight();
				memeEditorLayoutHeight = memeEditorLayout.getWidth();
				float scalingFactor = memeEditorLayoutWidth / 720f;
				Log.i("memeEditorLayoutWidth",
						Float.toString(memeEditorLayoutWidth));
				Log.i("ScaleFactor", Float.toString(scalingFactor));
				sandboxView.setScaleX(scalingFactor);
				sandboxView.setScaleY(scalingFactor);
			}
		});
		memeEditorLayout.addView(sandboxView);

		// Set save button on click method
		forwardButtonImageView = (ImageView) findViewById(R.id.forwardButtonImage);
		forwardButtonImageView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				forwardButtonImageView.setEnabled(false);
				Forward forward = new Forward();
				forward.execute();
			}
		});
	}

	@Override
	protected void onPause() {
		// Hide the progress bar
		linlaHeaderProgress.setVisibility(View.GONE);
		forwardButtonImageView.setEnabled(true);

		// Try to delete cache if possible
		Log.i("myDir", myDir.toString()
				+ ((myDir.exists()) ? " is Exist" : "is not exist"));
		if (myDir.exists())
			if (myDir.delete())
				Log.i("myDir", "myDir is deleted");
			else
				Log.i("myDir", "myDir is not deleted");
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		// Try to delete cache if possible
		Log.i("myDir", myDir.toString()
				+ ((myDir.exists()) ? " is Exist" : " is not exist"));
		if (myDir.exists())
			if (myDir.delete())
				Log.i("myDir", "myDir is deleted");
			else
				Log.i("myDir", "myDir is not deleted");
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.meme_editor, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		switch (item.getItemId()) {
		case R.id.reset_sandbox:
			sandboxView.reset();
			return true;
		case R.id.action_settings:
			return true;
		case android.R.id.home:
			// When the action bar icon on the top right is clicked, finish this
			// activity
			this.finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	// save image to a specific places
	private void saveImage() {
		// Create the file path and file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
				.format(new Date());
		String fname = timeStamp + ".png";
		cacheImage_forPassing = new File(myDir, fname);

		// Remove duplicates
		if (cacheImage_forPassing.exists())
			cacheImage_forPassing.delete();

		// Try save the bitmap
		try {
			FileOutputStream out = new FileOutputStream(cacheImage_forPassing);
			memeBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
			out.flush();
			out.close();
			Log.i("memeCacheLocation", cacheImage_forPassing.toString());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Async task for onClick
	class Forward extends AsyncTask<Object, Object, Object> {
		// Before forwarding
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			linlaHeaderProgress.setVisibility(View.VISIBLE);
			linlaHeaderProgress.bringToFront();
		}

		// Forwarding
		@Override
		protected String doInBackground(Object... arg0) {
			Intent forward = new Intent(selfRef, SaveResultImageActivity.class);
			sandboxView.setDrawingCacheEnabled(true);
			sandboxView.buildDrawingCache();
			memeBitmap = Bitmap.createBitmap(sandboxView.getDrawingCache());
			saveImage();
			forward.putExtra("cs4295.memcreator.memeImageCache",
					cacheImage_forPassing.getPath());
			startActivity(forward);
			sandboxView.setDrawingCacheEnabled(false);
			return "DONE";
		}

		// After forwarding
		@Override
		protected void onPostExecute(Object result) {
			linlaHeaderProgress.setVisibility(View.GONE);
			super.onPostExecute(result);
		}
	}
}
