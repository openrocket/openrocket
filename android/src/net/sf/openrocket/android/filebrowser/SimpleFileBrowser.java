package net.sf.openrocket.android.filebrowser;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import net.sf.openrocket.R;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListActivity;

public class SimpleFileBrowser extends SherlockListActivity {

	private List<File> path = null;
	private final static File root = new File("/");

	private File previousDirectory = root;
	
	private String baseDirPrefKey;
	private String baseDirName;

	private boolean showOnlyOrkFiles;
	
	private static final OrkFileFilter filter = new OrkFileFilter();
	private static final Comparator<String> sorter = new AlphanumComparator();

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.simplefilebrowser);

		Resources resources = this.getResources();
		baseDirPrefKey = resources.getString(R.string.PreferenceFileBrowserBaseDirectory);
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);

		String showOnlyOrkFilesKey = resources.getString(R.string.PreferenceShowOnlyOrkFiles);
		showOnlyOrkFiles = pref.getBoolean(showOnlyOrkFilesKey, false);

		baseDirName = pref.getString(baseDirPrefKey, Environment.getExternalStorageDirectory().getAbsolutePath() );
		getDir(	new File(baseDirName) );
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
			return isOrk(arg0);
		}

		public boolean isOrk(File arg0) {
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

	private void getDir(final File dirPath) {
		
		// A little sanity check.  It could be possible the directory saved in the preference
		// is no longer mounted, is not a directory (any more), or cannot be read.
		// if any of these are the case, we display a little dialog, then revert to the
		// previousDirectory.
		if ( !dirPath.exists() || !dirPath.isDirectory() || !dirPath.canRead() ) {
			
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Unable to open directory " + dirPath.getAbsolutePath() );
			builder.setCancelable(true);
			builder.setOnCancelListener( new Dialog.OnCancelListener() {

				@Override
				public void onCancel(DialogInterface arg0) {
					if ( root.getAbsolutePath().equals(dirPath.getAbsolutePath()) ) {
						SimpleFileBrowser.this.finish();
					} else {
						SimpleFileBrowser.this.getDir( previousDirectory );
					}
				}
				
			});
			builder.show();
			return;
		}
		
		previousDirectory = dirPath;
		
		setTitle(dirPath.getAbsolutePath());
		path = new ArrayList<File>();

		File[] files = dirPath.listFiles((showOnlyOrkFiles) ? filter : null );

		boolean hasUp = false;
		if ( !dirPath.getAbsolutePath().equals("/")) {
			path.add(root);
			path.add( dirPath.getParentFile() );
			hasUp = true;
		}

		Arrays.sort(files, new FileComparator() );
		for( File file : files ) {
			path.add(file);
		}

		DirectoryList fileList = new DirectoryList(hasUp, path);
		setListAdapter(fileList);

	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		final File file = path.get(position);
		if (file.isDirectory()) {
			if (file.canRead())
				getDir(file);
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

	private class DirectoryList extends BaseAdapter {

		List<File> listing;
		boolean hasUp;

		DirectoryList( boolean hasUp ,List<File> listing ) {
			this.listing = listing;
			this.hasUp = hasUp;
		}

		@Override
		public int getCount() {
			return listing.size();
		}

		@Override
		public Object getItem(int arg0) {
			return listing.get(arg0);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if ( convertView == null ) {
				convertView = getLayoutInflater().inflate(R.layout.filebrowser_list_item, parent, false);
			}

			File file = (File) getItem(position);

			// Set the name of the field.
			{
				String fileName = file.getName();
				if ( hasUp ) {
					if (position == 0 ) {
						fileName = root.getAbsolutePath();
					} else if (position == 1) {
						fileName = "..";
					}
				}

				((TextView) convertView.findViewById(R.id.filebrowser_list_item_name)).setText(fileName);
			}
			
			// Set the "type icon"  directory, ork file, or none.
			{
				ImageView v = (ImageView) (convertView.findViewById(R.id.filebrowser_list_item_typeicon));
				if ( file.isDirectory() ) {
					v.setVisibility(View.VISIBLE);
					v.setImageResource(R.drawable.ic_directory);
				} else if ( filter.isOrk( file ) ) {
					v.setVisibility(View.VISIBLE);
					v.setImageResource(R.drawable.or_launcher);
				} else {
					v.setVisibility(View.INVISIBLE);
				}
			}

			// Set the "favorite directory" thing.
			{
				ImageView v = (ImageView) (convertView.findViewById(R.id.filebrowser_list_item_homeicon));
				if ( file.isDirectory() && hasUp && position > 1 ) {
					v.setVisibility(View.VISIBLE);
					if ( baseDirName.equals( file.getAbsolutePath() ) )  {
						v.setSelected(true);
					} else {
						v.setSelected(false);
						v.setClickable(true);
						v.setOnClickListener( new ChangeBaseDirectory(file.getAbsolutePath()));
					}
				} else {
					v.setVisibility(View.INVISIBLE);
					v.setClickable(false);
				}
			}
			return convertView;
		}
	}

	private class ChangeBaseDirectory implements View.OnClickListener {

		private final String dirname;
		
		ChangeBaseDirectory ( String dirname ) {
			this.dirname = dirname;
		}
		
		@Override
		public void onClick(View v) {
			if ( v.isSelected() == false ) {
				SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(SimpleFileBrowser.this);
				baseDirName = dirname;
				pref.edit().putString(baseDirPrefKey, dirname).commit();
				((BaseAdapter)SimpleFileBrowser.this.getListAdapter()).notifyDataSetChanged();
			}
		}
		
	}
	
}
