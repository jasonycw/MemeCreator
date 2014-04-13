package cs4295.memecreator;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ReceiveIntentActivity extends Activity {
	
	private ReceiveIntentActivity selfRef;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_receive_intent);
		
		selfRef = this;
		// Get the intent that started this activity
	    Intent intent = getIntent();
	    Uri imageURI = null;//intent.getData();
	    Log.e("URI:", intent.getData() + "");
        if (intent.getAction() != null && intent.getAction().equals(Intent.ACTION_SEND)) {
            Bundle extras = intent.getExtras();
            if (extras.containsKey(Intent.EXTRA_STREAM)) {
                imageURI = (Uri) extras.getParcelable(Intent.EXTRA_STREAM);
            }
        } else {
            imageURI = (Uri) intent.getParcelableExtra("image");
        }
	    String[] filePath = { MediaStore.Images.Media.DATA };
		Cursor cursor = getContentResolver().query(imageURI, filePath, null, null, null);
		cursor.moveToFirst();
		String imagePath = cursor.getString(cursor.getColumnIndex(filePath[0]));
		cursor.close();
		// Forward the image path to the next activity
		forwardImagePath(imagePath, SaveResultImageActivity.class);
		finish();
	}
	


	// Method for forwarding a image path to the next class
	private void forwardImagePath(String imagePath, Class<?> targetClass) {
		// Put the image path to the intent with the variable name "cs4295.memcreator.imagePath"
		Intent forward = new Intent(selfRef, SaveResultImageActivity.class);
		forward.putExtra("cs4295.memcreator.imagePath", imagePath);
		startActivity(forward);
	}
	

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_receive_intent, container,
					false);
			return rootView;
		}
	}

}
