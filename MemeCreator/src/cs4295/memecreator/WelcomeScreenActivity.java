package cs4295.memecreator;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

public class WelcomeScreenActivity extends Activity {
	private static int LOAD_IMAGE_RESULTS = 1;
	private ImageView image;
	private WelcomeScreenActivity selfRef;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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

		setContentView(R.layout.activity_welcome_screen);
		image = (ImageView) findViewById(R.id.image);
		selfRef = this;
		// If there is no instance, use the normal layout
		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.welcomeScreenActivity, new PlaceholderFragment())
					.commit();

		}
		// Pass the image to the next activity
		else {

		}
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	@SuppressLint("ValidFragment")
	public class PlaceholderFragment extends Fragment {
		private ImageView welcomeScreenImage;

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(
					R.layout.fragment_welcome_screen_acivity, container, false);
			welcomeScreenImage = (ImageView) rootView
					.findViewById(R.id.welcomeScreenImage);
			welcomeScreenImage
					.setImageResource(R.drawable.welcome_screen_image);
			welcomeScreenImage.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					Toast.makeText(getActivity(), "Clicked Image",
							Toast.LENGTH_SHORT).show();
					Intent i = new Intent(
							Intent.ACTION_PICK,
							android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

					startActivityForResult(i, LOAD_IMAGE_RESULTS);
				}
			});

			return rootView;
		}

		public void onActivityResult(int requestCode, int resultCode,
				Intent data) {
			super.onActivityResult(requestCode, resultCode, data);

			if (requestCode == LOAD_IMAGE_RESULTS && resultCode == RESULT_OK
					&& data != null) {
				Uri pickedImage = data.getData();
				String[] filePath = { MediaStore.Images.Media.DATA };

				Cursor cursor = getContentResolver().query(pickedImage,
						filePath, null, null, null);
				cursor.moveToFirst();

				String imagePath = cursor.getString(cursor
						.getColumnIndex(filePath[0]));
				// image.setImageBitmap(BitmapFactory
				// .decodeFile(imagePath));

				Intent forward = new Intent(selfRef,
						SaveResultImageActivity.class);
				forward.putExtra("imagePath", imagePath);
				startActivity(forward);
				cursor.close();
			}
		}
	}
}
