package net.sf.openrocket.android.filebrowser;

import java.io.File;
import java.io.FileFilter;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import net.sf.openrocket.R;
import net.sf.openrocket.android.actionbarcompat.ActionBarListActivity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class SimpleFileBrowser extends ActionBarListActivity {

	private List<String> item = null;
	private List<String> path = null;
	private String root = "/";

	private static final OrkFileFilter filter = new OrkFileFilter();
	private static final Collator sorter = Collator.getInstance();
	static {
		sorter.setStrength(Collator.TERTIARY);
		sorter.setDecomposition(Collator.CANONICAL_DECOMPOSITION);
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.simplefilebrowser);
		getDir(	Environment.getExternalStorageDirectory().getAbsolutePath() );
	}

	private static class OrkFileFilter implements FileFilter {

		/* (non-Javadoc)
		 * @see java.io.FileFilter#accept(java.io.File)
		 */
		@Override
		public boolean accept(File arg0) {
			if ( arg0.isDirectory() ) { 
				return true;
			}
			if ( arg0.getName().endsWith(".ork") ) {
				return true;
			}
			return false;
		}

	}

	private static class FileComparator implements Comparator<File> {

		/* (non-Javadoc)
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(File arg0, File arg1) {
			// Directories come before files, otherwise alpha.
			if ( arg0.isDirectory() ) {
				if ( ! arg1.isDirectory() ) {
					return -1;
				}
				return sorter.compare(arg0.getName(), arg1.getName());
			}

			// arg0 is not a directory.
			if ( arg1.isDirectory() ) {
				return 1;
			}

			return sorter.compare(arg0.getName(), arg1.getName());
		}

	}

	private void getDir(String dirPath) {
		setTitle(dirPath);
		item = new ArrayList<String>();
		path = new ArrayList<String>();

		File f = new File(dirPath);
		File[] files = f.listFiles(filter);

		if (!dirPath.equals(root)) {
			item.add(root);
			path.add(root);
			item.add("../");
			path.add(f.getParent());
		}

		Arrays.sort(files, new FileComparator() );
		for (int i = 0; i < files.length; i++) {
			File file = files[i];
			path.add(file.getPath());
			if (file.isDirectory())
				item.add(file.getName() + "/");
			else
				item.add(file.getName());
		}

		ArrayAdapter<String> fileList = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, item);
		setListAdapter(fileList);

	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		final File file = new File(path.get(position));
		if (file.isDirectory()) {
			if (file.canRead())
				getDir(path.get(position));
			else {
				new AlertDialog.Builder(this).setIcon(R.drawable.or_launcher)
				.setTitle("[" + file.getName() + "] folder can't be read!")
				.setPositiveButton("OK",
						new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
					}
				}).show();
			}
		}
		else
		{
			Intent resultData = new Intent(Intent.ACTION_VIEW);
			resultData.setData( Uri.fromFile(file) );
			setResult(RESULT_OK,resultData);
			finish();
		}
	}
}
