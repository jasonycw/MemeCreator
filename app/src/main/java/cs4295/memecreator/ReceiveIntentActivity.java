package cs4295.memecreator;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

public class ReceiveIntentActivity extends Activity {

	private ReceiveIntentActivity selfRef;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_receive_intent);

		selfRef = this;
		// Get the intent that started this activity
		Intent intent = getIntent();
		Uri imageURI = null;

		Log.e("URI information:",
				intent.getExtras().getParcelable(Intent.EXTRA_STREAM)
						.toString());

		if (intent.getAction() != null
				&& intent.getAction().equals(Intent.ACTION_SEND)) {
			Bundle extras = intent.getExtras();
			if (extras.containsKey(Intent.EXTRA_STREAM)) {
				imageURI = (Uri) extras.getParcelable(Intent.EXTRA_STREAM);
			}
		} else {
			imageURI = (Uri) intent.getParcelableExtra("image");
		}

		String imagePath = "";
		Log.i("imageURI", imageURI.toString());

		// Only for Whatsapp intent
		if (imageURI.toString().contains("file:///")) {
			imagePath = imageURI.toString().substring(7);
			imagePath = imagePath.replace("%20", " ");
		} else {
			String[] filePath = { MediaStore.Images.Media.DATA };
			Log.i("Checking", MediaStore.Images.Media.DATA);
			Cursor cursor = getContentResolver().query(imageURI, filePath,
					null, null, null);

			// if cursor != null, that means the image is inside the
			// MediaStore.Images.Media.DATA but not in our Meme folder
			if (cursor != null) {
				Log.i("ImageUri", imageURI.toString());
				Log.i("filePath", filePath[0]);
				cursor.moveToFirst();
				imagePath = cursor
						.getString(cursor.getColumnIndex(filePath[0]));
				Log.i("After!", imagePath);
				cursor.close();
			}
			// if cursor = null, that means the image is inside our Meme file
			else {
				imagePath = Environment.getExternalStorageDirectory().getPath()
						+ File.separator + "DCIM/Meme/Media/temp.png";
			}
		}

		Log.i("Path", imagePath);

		// Forward the image path to the next activity
		forwardImagePath(imagePath, MemeEditorActivity.class);
		finish();
	}

	// Method for forwarding a image path to the next class
	private void forwardImagePath(String imagePath, Class<?> targetClass) {
		// Put the image path to the intent with the variable name
		// "cs4295.memcreator.imagePath"
		Intent forward = new Intent(selfRef, targetClass);
		forward.putExtra("cs4295.memcreator.imagePath", imagePath);
		startActivity(forward);
	}
}
