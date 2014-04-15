package com.bornander.gestures;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;

public class GesturesActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		LinearLayout layout = new LinearLayout(this);
		// lLayout.setOrientation(LinearLayout.VERTICAL);
		// -1(LayoutParams.MATCH_PARENT) is fill_parent or match_parent since
		// API level 8
		// -2(LayoutParams.WRAP_CONTENT) is wrap_content
		layout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
		layout.setGravity(Gravity.CENTER);
		

		Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.advert);
		View view = new SandboxView(this, bitmap);
		view.setLayoutParams(new LayoutParams(1024, 1024));

		layout.addView(view);
		
		setContentView(layout);
		// SandboxView view = (SandboxView)findViewById(R.id.sandbox);
	}
}