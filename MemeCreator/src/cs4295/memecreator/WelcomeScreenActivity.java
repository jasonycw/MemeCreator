package cs4295.memecreator;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
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
	private WelcomeScreenActivity selfRef;
	private LinearLayout tutorial;
	private ImageView welcomeScreenImage;
	private ImageView settingImageButton;

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
	}

	@Override
	protected void onResume() {
		super.onResume();
		settingImageButton.setEnabled(true);
	}

	// Method for forwarding a image path to the next class
	private void forwardImagePath(String imagePath, Class<?> targetClass) {
		// Put the image path to the intent with the variable name
		// "cs4295.memcreator.imagePath"
		Intent forward = new Intent(selfRef, MemeEditorActivity.class);
		forward.putExtra("cs4295.memcreator.imagePath", imagePath);
		startActivity(forward);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	@SuppressLint("ValidFragment")
	public class PlaceholderFragment extends Fragment {
		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(
					R.layout.fragment_welcome_screen_acivity, container, false);

			tutorial = (LinearLayout) rootView
					.findViewById(R.id.welcome_screen_tutorial);
			tutorial.bringToFront();
			tutorial.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					tutorial.setVisibility(View.GONE);
					tutorial.setEnabled(false);
				}
			});

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
					startActivityForResult(i, LOAD_IMAGE_RESULTS);
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
				forwardImagePath(imagePath, SaveResultImageActivity.class);
			}
		}
	}
}
