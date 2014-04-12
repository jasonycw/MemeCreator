package cs4295.memecreator;

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
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

public class WelcomeScreenAcivity extends Activity {

	private static int LOAD_IMAGE_RESULTS = 1;
	private ImageView image;
	private ImageView imagebutton;

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

		// If there is no instance, use the normal layout
		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.welcomeScreenActivity, new PlaceholderFragment())
					.commit();
		}
		// Pass the image to the next activity
		else {

		}
		
		// Use imageButton
		imagebutton = (ImageView)findViewById(R.id.imagebutton);
        image = (ImageView)findViewById(R.id.image);
        imagebutton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	Intent i = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        		startActivityForResult(i,LOAD_IMAGE_RESULTS);
             }
         });        
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.welcome_screen_acivity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
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
            View rootView = inflater.inflate(R.layout.fragment_welcome_screen_acivity, container, false);
            return rootView;
        }
    }
    
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
    	
    	if(requestCode == LOAD_IMAGE_RESULTS && resultCode == RESULT_OK && data != null)
    	{
    		Uri pickedImage = data.getData();
    		String[] filePath = { MediaStore.Images.Media.DATA };
    		
    		Cursor cursor = getContentResolver().query(pickedImage, filePath, null, null, null);
    		cursor.moveToFirst();
    		
    		String imagePath = cursor.getString(cursor.getColumnIndex(filePath[0]));
    		image.setImageBitmap(BitmapFactory.decodeFile(imagePath));
    		
    		cursor.close();
    	}
    }
}
