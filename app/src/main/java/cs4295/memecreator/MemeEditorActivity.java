

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
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import cs4295.customView.MemeEditorView;


public class MemeEditorActivity extends Activity
{
	private MemeEditorActivity	selfRef;
	private SharedPreferences	setting;
	private LinearLayout		linlaHeaderProgress;
	private float				memeEditorLayoutWidth;
	private float				memeEditorLayoutHeight;
	private LinearLayout		tutorial;
	private LinearLayout		memeEditorLayout;
	private MemeEditorView		memeEditorView;
	private ImageView			forwardButtonImageView;
	private Bitmap				memeBitmap;
	private File				cacheImage_forPassing;
	private File				myDir;
	private String				dataDir;
	private boolean				firsttimes;
	private boolean				tutorialPreference;
	
	
	@ Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setProgressBarIndeterminateVisibility(true);
		setContentView(R.layout.activity_meme_editor);
		selfRef = this;
		
		// Set the actioin bar style
		ActionBar actionBar = getActionBar();
		actionBar.setBackgroundDrawable(new ColorDrawable(getResources()
					.getColor(R.color.action_bar_color)));
		actionBar.setIcon(R.drawable.back_icon_black);
		actionBar.setHomeButtonEnabled(true);
		int titleId = Resources.getSystem().getIdentifier("action_bar_title",
					"id", "android");
		TextView yourTextView = (TextView)findViewById(titleId);
		yourTextView.setTextColor(getResources().getColor(R.color.black));
		
		// Transparent bar on android 4.4 or above
		if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.KITKAT)
		{
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
		linlaHeaderProgress = (LinearLayout)findViewById(R.id.linlaHeaderProgress);
		linlaHeaderProgress.bringToFront();
		
		// Initialize tutorial
		setting = PreferenceManager
					.getDefaultSharedPreferences(MemeEditorActivity.this);
		SharedPreferences prefre = getSharedPreferences("Meme_Pref", Context.MODE_PRIVATE);
		firsttimes = prefre.getBoolean("Meme_Pref", true);
		tutorialPreference = setting.getBoolean("Tutor_Preference", false);
		SharedPreferences.Editor firstTimeEditor = prefre.edit();
		
		// See if tutorial is needed to be shown
		tutorial = (LinearLayout)findViewById(R.id.meme_editor_tutorial);
		tutorial.setEnabled(false);
		tutorial.setOnClickListener(new OnClickListener()
		{
			@ Override
			public void onClick(View view)
			{
				tutorial.setVisibility(View.GONE);
				tutorial.setEnabled(false);
			}
		});
		if(firsttimes)
		{
			tutorial.setVisibility(View.VISIBLE);
			tutorial.bringToFront();
			tutorial.setEnabled(true);
			firstTimeEditor.putBoolean("Meme_Pref", false);
			firstTimeEditor.commit();
			
		}
		else if(tutorialPreference)
		{
			tutorial.setVisibility(View.VISIBLE);
			tutorial.bringToFront();
			tutorial.setEnabled(true);
			tutorialPreference = setting.getBoolean("Tutor_Preference", false);
		}
		else
		{
			tutorial.setVisibility(View.GONE);
			tutorial.setEnabled(false);
		}
		
		// Get the data directory for the app
		PackageManager m = getPackageManager();
		dataDir = getPackageName();
		try
		{
			PackageInfo p = m.getPackageInfo(dataDir, 0);
			dataDir = p.applicationInfo.dataDir;
			myDir = new File(dataDir+"/cache");
			if(!myDir.exists())
				myDir.mkdirs();
			if(myDir.setWritable(true))
				Log.i("meme", "myDir is writable");
			else
				Log.i("meme", "myDir is not writable");
		}catch(NameNotFoundException e)
		{
			Log.w("yourtag", "Error Package name not found ", e);
		}
		
		// Get the intent and get the image path to be the meme image
		Intent shareIntent = getIntent();
		String imagePath = shareIntent.getStringExtra("cs4295.memcreator.imagePath");
		
		// Create the SandboxView
		setting = PreferenceManager
					.getDefaultSharedPreferences(MemeEditorActivity.this);
		// final int memeSize = Integer.valueOf(setting.getString("image_size","720"));
		final int memeSize = setting.getInt("image_size", 720);
		Log.i("meme", "memeSize = "+memeSize);
		memeEditorLayout = (LinearLayout)findViewById(R.id.memeEditorLayout);
		memeEditorLayout.setGravity(Gravity.CENTER);
		try
		{
			Log.i("imagePath", imagePath);
			Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
			memeEditorView = new MemeEditorView(this, bitmap);
			memeEditorView.setLayoutParams(new LayoutParams(memeSize, memeSize));
			
			// Scale the sand box and add it into the layout
			ViewTreeObserver viewTreeObserver = memeEditorLayout
						.getViewTreeObserver();
			// For getting the width and height of a dynamic layout during
			// onCreate
			viewTreeObserver
						.addOnGlobalLayoutListener(new OnGlobalLayoutListener()
						{
							@ Override
							public void onGlobalLayout()
							{
								memeEditorLayout.getViewTreeObserver()
											.removeGlobalOnLayoutListener(this);
								memeEditorLayoutWidth = memeEditorLayout.getHeight();
								memeEditorLayoutHeight = memeEditorLayout.getWidth();
								float scalingFactor = memeEditorLayoutWidth/(float)memeSize;
								Log.i("memeEditorLayoutWidth", Float.toString(memeEditorLayoutWidth));
								Log.i("ScaleFactor", Float.toString(scalingFactor));
								memeEditorView.setScaleX(scalingFactor);
								memeEditorView.setScaleY(scalingFactor);
							}
						});
			memeEditorLayout.addView(memeEditorView);
			
			// Set save button on click method
			forwardButtonImageView = (ImageView)findViewById(R.id.forwardButtonImage);
			forwardButtonImageView.setOnClickListener(new OnClickListener()
			{
				@ Override
				public void onClick(View arg0)
				{
					forwardButtonImageView.setEnabled(false);
					Forward forward = new Forward();
					forward.execute();
				}
			});
		}catch(OutOfMemoryError e)
		{
			Toast.makeText(selfRef, "Your device is out of memory.", Toast.LENGTH_LONG).show();
			finish();
		}catch(Exception e)
		{
			Log.i("Meme Editor Activity", e.toString());
			Toast.makeText(selfRef, "Ops, something went wrong.", Toast.LENGTH_LONG).show();
			finish();
		}
	}
	
	
	// Delete a files
	private void deleteFile(File file)
	{
		if(file!=null)
		{
			Log.i("deleteFile", file.toString()+((file.exists())?" is Exist.":"is not exist!!!!"));
			
			// Check if the file exist
			if(file.exists())
				// Clear the file inside if it is a directory
				if(file.isDirectory())
				{
					String[] children = file.list();
					for(int i = 0;i<children.length;i++)
					{
						File f = new File(file, children[i]);
						if(f.delete())
							Log.i("deleteFile", f.getAbsolutePath()+" is deleted....");
						else
							Log.i("deleteFile", f.getAbsolutePath()+" is not deleted!!!!");
					}
				}
		}
	}
	
	
	@ Override
	protected void onPause()
	{
		// Hide the progress bar
		linlaHeaderProgress.setVisibility(View.GONE);
		forwardButtonImageView.setEnabled(true);
		
		super.onPause();
	}
	
	
	@ Override
	protected void onResume()
	{
		super.onResume();
		memeEditorView.setEnabled(true);
		memeEditorView.resume();
	}
	
	
	@ Override
	protected void onDestroy()
	{
		// Try to delete cache if possible
		deleteFile(myDir);
		bp_release();
		memeEditorView.destroyDrawingCache();
		super.onDestroy();
	}
	
	
	@ Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.meme_editor, menu);
		return true;
	}
	
	
	@ Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		switch(item.getItemId())
		{
			case R.id.reset_sandbox:
				memeEditorView.reset();
				return true;
			case R.id.action_settings:
				Intent intent = new Intent(selfRef, SettingsActivity.class);
				startActivity(intent);
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
	private void saveImage()
	{
		// Create the file path and file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		String fname = timeStamp+".png";
		cacheImage_forPassing = new File(myDir, fname);
		
		// Remove duplicates
		if(cacheImage_forPassing.exists())
			cacheImage_forPassing.delete();
		
		// Try save the bitmap
		try
		{
			FileOutputStream out = new FileOutputStream(cacheImage_forPassing);
			memeBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
			out.flush();
			out.close();
			Log.i("memeCacheLocation", cacheImage_forPassing.toString());
			
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	// Async task for onClick
	class Forward extends AsyncTask<Object,Object,Object>
	{
		// Before forwarding
		@ Override
		protected void onPreExecute()
		{
			super.onPreExecute();
			linlaHeaderProgress.setVisibility(View.VISIBLE);
			linlaHeaderProgress.bringToFront();
			memeEditorView.pause();
			memeEditorView.invalidate();
		}
		
		
		// Forwarding
		@ Override
		protected String doInBackground(Object ... arg0)
		{
			Intent forward = new Intent(selfRef, SaveResultImageActivity.class);
			memeEditorView.setDrawingCacheEnabled(true);
			memeEditorView.buildDrawingCache();
			memeBitmap = Bitmap.createBitmap(memeEditorView.getDrawingCache());
			saveImage();
			forward.putExtra("cs4295.memcreator.memeImageCache",
						cacheImage_forPassing.getPath());
			startActivity(forward);
			memeEditorView.setDrawingCacheEnabled(false);
			return "DONE";
		}
		
		
		// After forwarding
		@ Override
		protected void onPostExecute(Object result)
		{
			linlaHeaderProgress.setVisibility(View.GONE);
			super.onPostExecute(result);
		}
	}
	
	
	// Clear the Bitmap from memory
	private void bp_release()
	{
		if(memeBitmap!=null&&!memeBitmap.isRecycled())
		{
			memeBitmap.recycle();
			memeBitmap = null;
		}
	}
}
