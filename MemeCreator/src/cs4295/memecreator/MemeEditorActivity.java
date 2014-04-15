package cs4295.memecreator;

import java.io.ByteArrayOutputStream;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
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
import android.widget.Button;
import android.widget.LinearLayout;
import cs4295.gesture.SandboxView;

public class MemeEditorActivity extends Activity {
	private MemeEditorActivity selfRef;
	private View sandboxView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_meme_editor);
		selfRef = this;
		
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
		
		// Get the intent and set the image path to be the result image
		Intent shareIntent = getIntent();
		String imagePath = shareIntent.getStringExtra("cs4295.memcreator.imagePath");
		
		// Adding the SandboxView
		LinearLayout layout = (LinearLayout)findViewById(R.id.memeEditorLayout);
//		layout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
		layout.setGravity(Gravity.CENTER);
//		Bitmap bitmap = BitmapFactory.decodeResource(getResources(),ID_OF_THE_DRAWABLE);
		Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
		sandboxView = new SandboxView(this, bitmap);
		sandboxView.setLayoutParams(new LayoutParams(720, 720));
		layout.addView(sandboxView);
		
		// Adding save button
		Button saveButton = new Button(this);
		saveButton.setText(R.string.save);
		layout.addView(saveButton);
		
		// Set save button on click method
		saveButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				Intent forward = new Intent(selfRef, SaveResultImageActivity.class);
				sandboxView.setDrawingCacheEnabled(true);
				sandboxView.buildDrawingCache();
				Bitmap memeBitmap = Bitmap.createBitmap(sandboxView.getDrawingCache());
				
				//Convert to byte array
				ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
				memeBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
				byte[] memeBitmapByteArray = byteArrayOutputStream.toByteArray();

				forward.putExtra("cs4295.memcreator.memeBitmapByteArray",memeBitmapByteArray);
//				forward.putExtras(extras);
				startActivity(forward);
				sandboxView.setDrawingCacheEnabled(false);
			}});
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
