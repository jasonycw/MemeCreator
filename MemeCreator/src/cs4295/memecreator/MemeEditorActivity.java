package cs4295.memecreator;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import cs4295.customView.SandboxView;

public class MemeEditorActivity extends Activity {
	private MemeEditorActivity selfRef;
	private View sandboxView;
	private File cacheImage_forPassing;
	private String dataDir;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_meme_editor);
		selfRef = this;
		PackageManager m = getPackageManager();
		dataDir = getPackageName();
		try {
		    PackageInfo p = m.getPackageInfo(dataDir, 0);
		    dataDir = p.applicationInfo.dataDir;
		} catch (NameNotFoundException e) {
		    Log.w("yourtag", "Error Package name not found ", e);
		}
		
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
		
		// Get the intent and get the image path to be the meme image
		Intent shareIntent = getIntent();
		String imagePath = shareIntent.getStringExtra("cs4295.memcreator.imagePath");
		
		// Adding the SandboxView
		LinearLayout layout = (LinearLayout)findViewById(R.id.memeEditorLayout);
		layout.setGravity(Gravity.CENTER);
		Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
		sandboxView = new SandboxView(this, bitmap);
		sandboxView.setLayoutParams(new LayoutParams(720, 720));
		layout.addView(sandboxView);
		
		// Adding save button
//		Button saveButton = new Button(this);
//		saveButton.setText(R.string.save);
//		layout.addView(saveButton);
		
		// Set save button on click method
		ImageView forwardButtonImageView = (ImageView) findViewById(R.id.forwardButtonImage);
		forwardButtonImageView.setOnClickListener(new OnClickListener(){
			Bitmap memeBitmap;
			
			@Override
			public void onClick(View arg0) {
				Intent forward = new Intent(selfRef, SaveResultImageActivity.class);
				sandboxView.setDrawingCacheEnabled(true);
				sandboxView.buildDrawingCache();
				memeBitmap = Bitmap.createBitmap(sandboxView.getDrawingCache());
				
				
//				//Convert to byte array
//				ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//				memeBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
//				byte[] memeBitmapByteArray = byteArrayOutputStream.toByteArray();
				saveImage();
//				forward.putExtra("cs4295.memcreator.memeBitmapByteArray",memeBitmapByteArray);
				forward.putExtra("cs4295.memcreator.memeImageCache",cacheImage_forPassing.getPath());
				startActivity(forward);
				sandboxView.setDrawingCacheEnabled(false);
			}
			
			// save image to a specific places
			private void saveImage() {
				File myDir = new File(dataDir+"/cache");
				
				String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
				String fname = timeStamp+".png";
				cacheImage_forPassing = new File (myDir, fname);
				if (cacheImage_forPassing.exists ()) cacheImage_forPassing.delete (); 
				try {
					   FileOutputStream out = new FileOutputStream(cacheImage_forPassing);
					   memeBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
					   out.flush();
					   out.close();
					   Log.i("memeCacheLocation",cacheImage_forPassing.toString());

				} catch (Exception e) {
					   e.printStackTrace();
				}
			}
		});
	}

	
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (cacheImage_forPassing.exists ()) cacheImage_forPassing.delete ();
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
}
