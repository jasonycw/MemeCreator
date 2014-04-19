package cs4295.memecreator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class FileExplorerActivity extends ListActivity {

	private List<String> item = null;
	private List<String> path = null;
	private String root;
	private TextView myPath;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_file_explorer);
		myPath = (TextView) findViewById(R.id.path);
		root = Environment.getExternalStorageDirectory().getPath();
		getDir(root);
	}

	private void getDir(String dirPath) {
		myPath.setText("Location: " + dirPath);
		item = new ArrayList<String>();
		path = new ArrayList<String>();
		File f = new File(dirPath);
		File[] files = f.listFiles();
		
		if (!dirPath.equals(root)) {
			item.add(root);
			path.add(root);
			item.add("../");
			path.add(f.getParent());
		}

		for (int i = 0; i < files.length; i++) {
			File file = files[i];

			if (!file.isHidden() && file.canRead()) {
				path.add(file.getPath());
				if (file.isDirectory()) {
					item.add(file.getName() + "/");
				} else {
					item.add(file.getName());
				}
			}
		}

		ArrayAdapter<String> fileList = new ArrayAdapter<String>(this,
				R.layout.row, item);
		setListAdapter(fileList);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		File file = new File(path.get(position));

		if (file.isDirectory()) {
			if (file.canRead()) {
				getDir(path.get(position));
			} else {
				new AlertDialog.Builder(this)
						.setIcon(R.drawable.ic_launcher)
						.setTitle(
								"[" + file.getName()
										+ "] folder can't be read!")
						.setPositiveButton("OK", null).show();
			}
		} else {
			new AlertDialog.Builder(this).setIcon(R.drawable.ic_launcher)
					.setTitle("[" + file.getName() + "]")
					.setPositiveButton("OK", null).show();

		}
	}

}