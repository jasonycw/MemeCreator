package cs4295.memecreator;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class WelcomeScreenActivity extends Activity {
	private static int LOAD_IMAGE_RESULTS = 1;
	static final int REQUEST_IMAGE_CAPTURE = 2;
	private WelcomeScreenActivity selfRef;
	private LinearLayout tutorial;
	private ImageView welcomeScreenImage;
	private ImageView settingImageButton;
	private boolean firsttimes;
	private boolean tutorialPreference;
	private File myDir;
	private String dataDir;
	private Bitmap cameraBitmap;
	private File cacheImage_forPassing;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_welcome_screen);
		selfRef = this;

		// Hide action bar
		ActionBar actionBar = getActionBar();
		actionBar.hide();

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

		// If there is no instance, use the normal layout
		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.welcomeScreenActivity, new PlaceholderFragment())
					.commit();
		}
		// Get the data directory for the app
				PackageManager m = getPackageManager();
				dataDir = getPackageName();
				try {
					PackageInfo p = m.getPackageInfo(dataDir, 0);
					dataDir = p.applicationInfo.dataDir;
					myDir = new File(dataDir + "/cache");
					if (!myDir.exists())
						myDir.mkdirs();
					if (myDir.setWritable(true))
						Log.i("meme", "myDir is writable");
					else
						Log.i("meme", "myDir is not writable");
				} catch (NameNotFoundException e) {
					Log.w("yourtag", "Error Package name not found ", e);
				}
	}

	@Override
	protected void onResume() {
		super.onResume();
		settingImageButton.setEnabled(true);
	}
	
	@Override
	protected void onDestroy() {
		// Try to delete cache if possible
		deleteFile(myDir);
		bp_release();
		super.onDestroy();
	}
	
	// Delete a files
	private void deleteFile(File file) {
		Log.i("deleteFile", file.toString()
				+ ((file.exists()) ? " is Exist." : "is not exist!!!!"));

		// Check if the file exist
		if (file.exists())
			// Clear the file inside if it is a directory
			if (file.isDirectory()) {
				String[] children = file.list();
				for (int i = 0; i < children.length; i++) {
					File f = new File(file, children[i]);
					if (f.delete())
						Log.i("deleteFile", f.getAbsolutePath()
								+ " is deleted....");
					else
						Log.i("deleteFile", f.getAbsolutePath()
								+ " is not deleted!!!!");
				}
			}
	}

	// Method for forwarding a image path to the next class
	private void forwardImagePath(String imagePath, Class<?> targetClass) {
		// Put the image path to the intent with the variable name
		// "cs4295.memcreator.imagePath"
		Intent forward = new Intent(selfRef, targetClass);
		forward.putExtra("cs4295.memcreator.imagePath", imagePath);
		startActivity(forward);
	}

	/**
	 * A fragment containing a simple view.
	 */
	@SuppressLint("ValidFragment")
	public class PlaceholderFragment extends Fragment {
		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			// Get the settings
			SharedPreferences setting = PreferenceManager
					.getDefaultSharedPreferences(WelcomeScreenActivity.this);
			SharedPreferences prefre = getSharedPreferences("task_Pref",
					Context.MODE_PRIVATE);
			firsttimes = prefre.getBoolean("task_Pref", true);
			tutorialPreference = setting.getBoolean("Tutor_Preference", false);
			SharedPreferences.Editor firstTimeEditor = prefre.edit();

			// Update the views
			View rootView = inflater.inflate(
					R.layout.fragment_welcome_screen_acivity, container, false);

			// Show tutorial only in some conditions
			tutorial = (LinearLayout) rootView
					.findViewById(R.id.welcome_screen_tutorial);
			tutorial.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					tutorial.setVisibility(View.GONE);
					tutorial.setEnabled(false);
				}
			});
			if (firsttimes) {
				tutorial.bringToFront();
				firstTimeEditor.putBoolean("task_Pref", false);
				firstTimeEditor.commit();

			} else if (tutorialPreference) {
				tutorial.bringToFront();
				tutorialPreference = setting.getBoolean("Tutor_Preference",
						false);
			} else {
				tutorial.setVisibility(View.GONE);
				tutorial.setEnabled(false);
			}

			// Set the onClick for the setting image
			settingImageButton = (ImageView) rootView
					.findViewById(R.id.welcomeScreenSetting);
			settingImageButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					settingImageButton.setEnabled(false);
					Intent intent = new Intent(selfRef, SettingsActivity.class);
					startActivity(intent);
				}
			});

			// Set the oClick for the main welcome screen image
			welcomeScreenImage = (ImageView) rootView
					.findViewById(R.id.welcomeScreenImage);
			welcomeScreenImage.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					// Prevent multiple click
					welcomeScreenImage.setEnabled(false);

					// Call the action picker for selecting image
					Intent i = new Intent(
							Intent.ACTION_PICK,
							android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//					startActivityForResult(i, LOAD_IMAGE_RESULTS);
					Intent c = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
					Intent chooserIntent = Intent.createChooser(i, R.string.chooser_intent_title);
					chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] { c });
					startActivityForResult(chooserIntent,LOAD_IMAGE_RESULTS);
				}
			});
			return rootView;
		}

		// Method that will be call when the action pick is completed
		public void onActivityResult(int requestCode, int resultCode,
				Intent data) {
			super.onActivityResult(requestCode, resultCode, data);
			// Re-enable the button after result
			welcomeScreenImage.setEnabled(true);
			Log.i("a", "0");
			// If the result is okay
			if (requestCode == LOAD_IMAGE_RESULTS && resultCode == RESULT_OK
					&& data != null) {
				// Get the image path of the image
				Uri pickedImage = data.getData();
				String[] filePath = { MediaStore.Images.Media.DATA };
				Cursor cursor = getContentResolver().query(pickedImage,
						filePath, null, null, null);
				cursor.moveToFirst();
				String imagePath = cursor.getString(cursor
						.getColumnIndex(filePath[0]));
				cursor.close();

				// Forward the image path to the next activity
				forwardImagePath(imagePath, MemeEditorActivity.class);
			}
			 if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
				 	Log.i("a", "1");
			        Bundle extras = data.getExtras();
			        Log.i("a", "2");
			        cameraBitmap = (Bitmap) extras.get("data");
			        Log.i("a", "3");
			        saveImage();
			        Log.i("a", "4");
			        forwardImagePath(cacheImage_forPassing.toString(), MemeEditorActivity.class);
			    }
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
				cameraBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
				out.flush();
				out.close();
				Log.i("memeCacheLocation", cacheImage_forPassing.toString());

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		// Clear the Bitmap from memory
		private void bp_release() {
			if (cameraBitmap != null && !cameraBitmap.isRecycled()) {
				cameraBitmap.recycle();
				cameraBitmap = null;
			}
		}
}
