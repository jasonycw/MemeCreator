

package cs4295.memecreator;


import java.io.File;
import java.io.FileOutputStream;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


public class SaveResultImageActivity extends Activity
{
	private ImageView				resultImage;
	private Bitmap					tempImage;
	final Context					context			= this;
	private SaveResultImageActivity	selfRef			= this;
	private SharedPreferences		setting;
	private boolean					saveAndShare	= false;
	private String					path;
	private Intent					imageIntent;
	private Uri						uriToImage;
	private String					dataDir;
	private File					myDir;
	private String					imagePath;
	private ImageView				share;
	private ImageView				save;
	
	
	@ Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_save_result_image);
		
		// Set the actioin bar style
		ActionBar actionBar = getActionBar();
		actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.action_bar_color)));
		actionBar.setIcon(R.drawable.back_icon_black);
		actionBar.setHomeButtonEnabled(true);
		int titleId = Resources.getSystem().getIdentifier("action_bar_title", "id", "android");
		TextView yourTextView = (TextView)findViewById(titleId);
		yourTextView.setTextColor(getResources().getColor(R.color.black));
		
		// Transparent bar on android 4.4 or above
		if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.KITKAT)
		{
			Window window = getWindow();
			// Translucent status bar
			window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
						WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
			// Translucent navigation bar
			window.setFlags(
						WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION,
						WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
			
			// Get the data directory for the app
			PackageManager m = getPackageManager();
			dataDir = getPackageName();
			try
			{
				PackageInfo p = m.getPackageInfo(dataDir, 0);
				dataDir = p.applicationInfo.dataDir;
				myDir = new File(dataDir+"/cache");
			}catch(NameNotFoundException e)
			{
				Log.w("yourtag", "Error Package name not found ", e);
			}
			
		}
		
		// Get the intent and set the image path to be the result image
		Intent shareIntent = getIntent();
		imagePath = shareIntent.getStringExtra("cs4295.memcreator.imagePath");
		imagePath = getIntent().getStringExtra("cs4295.memcreator.memeImageCache");
		Bitmap memeBitmap = BitmapFactory.decodeFile(imagePath);
		
		// Set result and temp image
		resultImage = (ImageView)this.findViewById(R.id.resultImage);
		resultImage.setImageBitmap(memeBitmap);
		resultImage.setDrawingCacheEnabled(true);
		resultImage.buildDrawingCache();
		tempImage = ((BitmapDrawable)resultImage.getDrawable()).getBitmap();
		
		// Prepare the sharing image here
		setting = PreferenceManager.getDefaultSharedPreferences(SaveResultImageActivity.this);
		saveAndShare = setting.getBoolean("saveAndShare", false);
		setting = getSharedPreferences("path", Context.MODE_PRIVATE);
		path = setting.getString("image_path", Environment.getExternalStorageDirectory().getPath()+"/DCIM/Meme/Media/");
		saveTempImageForSharing();
		File imageFileToShare = new File(path+"/temp.png");
		uriToImage = Uri.fromFile(imageFileToShare);
		
		// Share button on click
		share = (ImageView)findViewById(R.id.shareButton);
		share.setOnClickListener(new OnClickListener()
		{
			@ Override
			public void onClick(View arg0)
			{
				// Disable share button to prevent multiple on click
				share.setEnabled(false);
				if(saveAndShare)
					saveAndShareImageHelper();
				else
					shareHelper();
			}
		});
		
		// Save button on click
		save = (ImageView)findViewById(R.id.saveButton);
		save.setOnClickListener(new OnClickListener()
		{
			@ Override
			public void onClick(View arg0)
			{
				// Disable save button to prevent multiple on click
				save.setEnabled(false);
				saveImageHelper();
			}
		});
	}
	
	
	// Helper method for sharing image only
	private void shareHelper()
	{
		
		Log.i("path:", path);
		Log.i("Uri:", uriToImage.toString());
		try
		{
			imageIntent = new Intent(Intent.ACTION_SEND);
			imageIntent.setType("image/*");
			imageIntent.putExtra(Intent.EXTRA_STREAM, uriToImage);
			
			startActivity(Intent.createChooser(imageIntent, "Share Image!"));
		}catch(Exception e)
		{
			Toast.makeText(selfRef, "This application is not availabled.", Toast.LENGTH_LONG).show();
		}
	}
	
	
	// Helper method for save image first before sharing image
	private void saveAndShareImageHelper()
	{
		
		Log.i("preference", setting.toString());
		
		// set default input value
		final EditText input = new EditText(context);
		File direct = new File(path);
		int number = countImageNo(direct)+1;
		input.setText("MemeImage "+number);
		
		// set dialog message
		// save Image in Internal with own Folder
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle("Save Image")
					.setMessage("Input Image Name")
					.setCancelable(true)
					.setView(input)
					.setPositiveButton("Save",
								new DialogInterface.OnClickListener()
								{
									public void onClick(DialogInterface dialog, int id)
									{
										// if this button is clicked, close
										// current activity
										saveImage(tempImage, input.getText()+".png");
										share.setEnabled(true);
										shareHelper();
									}
								})
					.setNegativeButton("Cancel",
								new DialogInterface.OnClickListener()
								{
									public void onClick(DialogInterface dialog, int id)
									{
										// if this button is clicked, just close
										// the dialog box and do nothing
										share.setEnabled(true);
										dialog.cancel();
									}
								});
		
		// create alert dialog
		AlertDialog alertDialog = builder.create();
		alertDialog.show();
	}
	
	
	// Helper method to save an image
	private void saveImageHelper()
	{
		
		Log.i("preference", setting.toString());
		
		// set default input value
		final EditText input = new EditText(context);
		File direct = new File(path);
		int number = countImageNo(direct)+1;
		input.setText("MemeImage "+number);
		
		// set dialog message
		// save Image in Internal with own Folder
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle("Save Image")
					.setMessage("Input Image Name")
					.setCancelable(true)
					.setView(input)
					.setPositiveButton("Save",
								new DialogInterface.OnClickListener()
								{
									public void onClick(DialogInterface dialog, int id)
									{
										// if this button is clicked, close
										// current activity
										save.setEnabled(true);
										saveImage(tempImage, input.getText()+".png");
									}
								})
					.setNegativeButton("Cancel",
								new DialogInterface.OnClickListener()
								{
									public void onClick(DialogInterface dialog, int id)
									{
										// if this button is clicked, just close
										// the dialog box and do nothing
										save.setEnabled(true);
										dialog.cancel();
									}
								});
		
		// create alert dialog
		AlertDialog alertDialog = builder.create();
		alertDialog.show();
	}
	
	
	// Method to count the number of Image inside the file
	private int countImageNo(File dir)
	{
		try
		{
			File[] files = dir.listFiles();
			Log.i("File Number", " "+files.length);
			return files.length;
		}catch(Exception e)
		{}
		return 0;
	}
	
	
	// Method to save the bitmap into a specific location
	private void saveImage(Bitmap image, String fileName)
	{
		File direct = new File(path);
		
		if(!direct.exists())
		{
			direct.mkdirs();
		}
		
		File file = new File(new File(path), fileName);
		if(file.exists())
			file.delete();
		try
		{
			FileOutputStream out = new FileOutputStream(file);
			image.compress(Bitmap.CompressFormat.PNG, 100, out);
			out.flush();
			out.close();
			
			Toast.makeText(this, fileName+" is saved at "+path, 2000)
						.show();
			
			// update after the media scanner after saving
			MediaScannerConnectionClient client = new MyMediaScannerConnectionClient(getApplicationContext(), file, null);
			
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		
	}
	
	// An instance of MediaScannerConnection(use for update gallery )
	final class MyMediaScannerConnectionClient implements MediaScannerConnectionClient
	{
		
		private String					mFilename;
		private String					mMimetype;
		private MediaScannerConnection	mConn;
		
		
		public MyMediaScannerConnectionClient(Context ctx, File file, String mimetype)
		{
			this.mFilename = file.getAbsolutePath();
			mConn = new MediaScannerConnection(ctx, this);
			mConn.connect();
		}
		
		
		@ Override
		public void onMediaScannerConnected()
		{
			mConn.scanFile(mFilename, mMimetype);
		}
		
		
		@ Override
		public void onScanCompleted(String path, Uri uri)
		{
			mConn.disconnect();
		}
	}
	
	
	@ Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.save_result_image, menu);
		return true;
	}
	
	
	@ Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		switch(item.getItemId())
		{
			case R.id.action_settings:
				Intent intent = new Intent(selfRef, SettingsActivity.class);
				startActivity(intent);
				
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
	
	
	@ Override
	protected void onResume()
	{
		// Re-enable the share and save buttons
		share.setEnabled(true);
		save.setEnabled(true);
		
		// For update path after setting change
		setting = PreferenceManager
					.getDefaultSharedPreferences(SaveResultImageActivity.this);
		saveAndShare = setting.getBoolean("saveAndShare", false);
		setting = getSharedPreferences("path", Context.MODE_PRIVATE);
		path = setting.getString("image_path", Environment.getExternalStorageDirectory().getPath()+"/DCIM/Meme/Media/");
		
		super.onResume();
	}
	
	
	protected void onStart()
	{
		// Re-enable the share and save buttons
		share.setEnabled(true);
		save.setEnabled(true);
		
		super.onStart();
	}
	
	
	// Remove the temp Image used for sharing before
	@ Override
	protected void onDestroy()
	{
		File temp = new File(new File(path), "temp.png");
		
		if(temp.exists())
			temp.delete();
		
		bp_release();
		super.onDestroy();
	}
	
	
	// Save the image as a temp file and used for sharing later
	private void saveTempImageForSharing()
	{
		// Create the file path and file name
		File direct = new File(path);
		if(!direct.exists())
		{
			direct.mkdirs();
		}
		File file = new File(new File(path), "temp.png");
		if(file.exists())
			file.delete();
		
		// Try saving image
		try
		{
			FileOutputStream out = new FileOutputStream(file);
			tempImage.compress(Bitmap.CompressFormat.PNG, 100, out);
			out.flush();
			out.close();
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	
	// Clear the Bitmap from memory
	private void bp_release()
	{
		if(tempImage!=null&&!tempImage.isRecycled())
		{
			tempImage.recycle();
			tempImage = null;
		}
	}
}
