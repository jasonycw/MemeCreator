package cs4295.memecreator;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class SaveResultImageActivity extends Activity {
	private ImageView resultImage;
	private Bitmap tempImage;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_save_result_image);
		
		// Set the actioin bar style
		ActionBar actionBar = getActionBar();
		actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#503C3C3C")));
		actionBar.setIcon(R.drawable.back_icon);
		actionBar.setHomeButtonEnabled(true);
		

		// Transparent bar on android 4.4 or above
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			Window window = getWindow();
			// Translucent status bar
			window.setFlags(
					WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
					WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
			// Translucent navigation bar
			window.setFlags(
					WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION,
					WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
		}
		
		// Get the intent and set the image path to be the result image
		Intent shareIntent = getIntent();
		String imagePath = shareIntent.getStringExtra("cs4295.memcreator.imagePath");
		resultImage = (ImageView) this.findViewById(R.id.resultImage);
		Log.i("imagePath",imagePath);

		resultImage.setImageBitmap(BitmapFactory.decodeFile(imagePath));
		resultImage.setDrawingCacheEnabled(true);
		resultImage.buildDrawingCache();
		
		//tempImage = Bitmap.createBitmap(resultImage.getDrawingCache());
		tempImage = ((BitmapDrawable)resultImage.getDrawable()).getBitmap();
		
		
		
		
		// Share button on click
		Button share = (Button) findViewById(R.id.shareButton);
		share.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {

				// Build the intent
				
				File direct = new File(Environment.getExternalStorageDirectory() + "/Android/data/cs4295.memecreator");
				if (!direct.exists()) {
					File memeDirectory = new File("/sdcard/Android/data/cs4295.memecreator/");
					memeDirectory.mkdirs();
				}
				
				Uri uriToImage = Uri.parse(android.provider.MediaStore.Images.Media.
						insertImage(SaveResultImageActivity.this.getContentResolver(), tempImage, null, null));
				Intent imageIntent = new Intent(Intent.ACTION_SEND);
				imageIntent.setType("image/*");
				imageIntent.putExtra(Intent.EXTRA_STREAM, uriToImage);

				// Verify it resolves
				PackageManager packageManager = getPackageManager();
				List<ResolveInfo> activities = packageManager.queryIntentActivities(imageIntent, 0);
				boolean isIntentSafe = activities.size() > 0;

				// Start an activity if it's safe
				if (isIntentSafe) {
					startActivity(Intent.createChooser(imageIntent, "Share images to.."));
				}
				else {
					show();
				}
			}
		});	
		
		// Save button on click 
		Button save = (Button) findViewById(R.id.saveButton);
		save.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				//save Image in Internal with own Folder
				saveImage(tempImage,"name.png");
			}
		});		
	}
	
	// Method to show notification when sharing is failed
	private void show(){
		Toast.makeText(this, "Sorry, share failed", 2000).show();
	}
	
	// Method to save the image
	private void saveImage(Bitmap image, String fileName) {

	    File direct = new File(Environment.getExternalStorageDirectory() + "/Meme Creator");

		if (!direct.exists()) {
			File memeDirectory = new File("/sdcard/Meme Creator/");
			memeDirectory.mkdirs();
		}

		File file = new File(new File("/sdcard/Meme Creator/"), fileName);
		if (file.exists())
			file.delete();
		try {
			FileOutputStream out = new FileOutputStream(file);
			image.compress(Bitmap.CompressFormat.PNG, 100, out);
			out.flush();
			out.close();
			Toast.makeText(this,fileName + " is saved at /sdcard/Meme Creator/", 2000).show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.save_result_image, menu);
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
			// When the action bar icon on the top right is clicked, finish this activity
			this.finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
