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
import android.os.Environment;
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
import android.widget.Toast;

public class WelcomeScreenActivity extends Activity {
	private static int IMPORT_IMAGE_RESULT = 1;
	static final int REQUEST_IMAGE_CAPTURE = 2;
	private WelcomeScreenActivity selfRef;
	private LinearLayout tutorial;
	private ImageView welcomeScreenImage;
	private ImageView settingImageButton;
	private boolean firsttimes;
	private boolean tutorialPreference;
	private File myDir;
	private String dataDir;
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
		deleteFile(cacheImage_forPassing);
//		bp_release();
		super.onDestroy();
	}

	// Delete a files
	private void deleteFile(File file) {
		Log.i("deleteFile", file.toString()
				+ ((file.exists()) ? " is Exist." : "is not exist!!!!"));

		// Check if the file exist for deletion
		if (file.exists())
			file.delete();
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

					// Intent for calling gallery
					Intent importFromGalleryIntent = new Intent(
							Intent.ACTION_PICK,
							android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

					// Intent for calling camera and store the image at setImageUri()
					Intent importFromCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
					importFromCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, makeCacheImageUri());

					// Intent to include camera and image intents
					Intent chooserIntent = Intent.createChooser(
							importFromGalleryIntent,
							getResources().getText(
									R.string.chooser_intent_title));
					chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS,
							new Intent[] { importFromCameraIntent });
					startActivityForResult(chooserIntent, IMPORT_IMAGE_RESULT);
				}
			});
			return rootView;
		}

		// Create a temp uri for camera
		public Uri makeCacheImageUri() {
			// Store image in dcim
			cacheImage_forPassing = new File(Environment.getExternalStorageDirectory()
					+ "/DCIM/", "tempCameraImage.png");
			Uri imgUri = Uri.fromFile(cacheImage_forPassing);
			return imgUri;
		}

		// Method that will be call when the action pick is completed
		public void onActivityResult(int requestCode, int resultCode,
				Intent data) {
			super.onActivityResult(requestCode, resultCode, data);
			// Re-enable the button after result
			welcomeScreenImage.setEnabled(true);
//			Log.i("a", "0");
//			Log.i("a", "0");
//			Log.i("a", "0");
//			Log.i("a", "0");
//			Log.i("a", "0");
//			Log.i("a", Boolean.toString(requestCode == IMPORT_IMAGE_RESULT));

			// If the result is okay
			if (requestCode == IMPORT_IMAGE_RESULT && resultCode == RESULT_OK) {
				if (data != null)
					// Get the image path of the image
					if (data.getData() != null) {
						Log.i("a", data.getData().toString());

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
					} else {
						Bundle extras = data.getExtras();

						Log.i("a", extras.toString());
						Log.i("a", extras.get("data").toString());
						for (int i = 0; i < extras.keySet().toArray().length; i++)
							Log.i("a", extras.keySet().toArray()[i].toString());

						// cameraBitmap = (Bitmap) extras.get("data");
						// saveImage();
						Log.i("a", cacheImage_forPassing.toString());
						forwardImagePath(cacheImage_forPassing.toString(),
								MemeEditorActivity.class);
					}
				else if(cacheImage_forPassing!=null)
					forwardImagePath(cacheImage_forPassing.toString(),
							MemeEditorActivity.class);
				else
					Toast.makeText(selfRef, "Image import is failed.", Toast.LENGTH_LONG);
			}
		}
	}

//	// save image to a specific places
//	private void saveImage() {
//		// Create the file path and file name
//		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
//				.format(new Date());
//		String fname = timeStamp + ".png";
//		cacheImage_forPassing = new File(myDir, fname);
//
//		// Remove duplicates
//		if (cacheImage_forPassing.exists())
//			cacheImage_forPassing.delete();
//
//		// Try save the bitmap
//		try {
//			FileOutputStream out = new FileOutputStream(cacheImage_forPassing);
//			cameraBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
//			out.flush();
//			out.close();
//			Log.i("memeCacheLocation", cacheImage_forPassing.toString());
//
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}

//	// Clear the Bitmap from memory
//	private void bp_release() {
//		if (cameraBitmap != null && !cameraBitmap.isRecycled()) {
//			cameraBitmap.recycle();
//			cameraBitmap = null;
//		}
//	}
}
