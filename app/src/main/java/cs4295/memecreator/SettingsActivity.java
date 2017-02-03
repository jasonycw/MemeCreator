

package cs4295.memecreator;


import java.util.List;
import cs4295.customPreference.NumberPickerDialogPreference;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;


/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html"> Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends PreferenceActivity
{
	/**
	 * Determines whether to always show the simplified settings UI, where
	 * settings are presented in a single list. When false, settings are shown
	 * as a master/detail two-pane view on tablets. When true, a single pane is
	 * shown on tablets.
	 */
	private static final boolean	ALWAYS_SIMPLE_PREFS	= false;
	SharedPreferences				settings;
	SharedPreferences.Editor		editor;
	
	
	@ Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
	}
	
	
	@ Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if(item.getItemId()==android.R.id.home)
		{
			// When the action bar icon on the top right is clicked, finish this
			// activity
			this.finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	
	@ Override
	protected void onPostCreate(Bundle savedInstanceState)
	{
		super.onPostCreate(savedInstanceState);
		
		settings = getSharedPreferences("path", Context.MODE_PRIVATE);
		setupSimplePreferencesScreen();
		
		final NumberPickerDialogPreference numberPicker = (NumberPickerDialogPreference)getPreferenceScreen()
					.findPreference("image_size");
		numberPicker.setMinValue(300);
		numberPicker.setOrder(Preference.DEFAULT_ORDER);
		numberPicker
					.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
					{
						@ Override
						public boolean onPreferenceChange(Preference preference,
									Object newValue)
						{
							Integer value = (Integer)newValue;
							preference.setSummary(value.toString());
							return true;
						}
					});
		
		// get the preference for image path
		final Preference pathSelector = findPreference("image_path");
		pathSelector.setSummary(settings.getString("image_path", "/sdcard/DCIM/Meme/Media/"));
		
		// set the onClickListener for path selection
		pathSelector
					.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
					{
						
						private String	m_chosenDir			= "";
						private boolean	m_newFolderEnabled	= true;
						
						
						@ Override
						public boolean onPreferenceClick(Preference preference)
						{
							
							Toast.makeText(SettingsActivity.this, "Press back to go upper directory.", 2000)
										.show();
							DirectoryChooser DirectoryChooser = new DirectoryChooser(
										SettingsActivity.this,
										new DirectoryChooser.ChosenDirectoryListener()
										{
											@ Override
											public void onChosenDir(String chosenDir)
											{
												editor = settings.edit();
												editor.putString("image_path",
															chosenDir);
												editor.commit();
												m_chosenDir = chosenDir;
												pathSelector.setSummary(settings.getString("image_path", "/sdcard/DCIM/Meme/Media/"));
												Log.i("default value", pathSelector.getSummary().toString());
												Toast.makeText(SettingsActivity.this, "Chosen directory: "+chosenDir, Toast.LENGTH_LONG).show();
											}
										});
							// Toggle new folder button enabling
							DirectoryChooser.setNewFolderEnabled(m_newFolderEnabled);
							// Load directory chooser dialog for initial
							// 'm_chosenDir' directory.
							// The registered callback will be called upon final
							// directory selection.
							DirectoryChooser.chooseDirectory(m_chosenDir);
							m_newFolderEnabled = !m_newFolderEnabled;
							return false;
						}
					});
		
	}
	
	
	/**
	 * Shows the simplified settings UI if the device configuration if the
	 * device configuration dictates that a simplified, single-pane UI should be
	 * shown.
	 */
	private void setupSimplePreferencesScreen()
	{
		if(!isSimplePreferences(this))
		{
			return;
		}
		
		// In the simplified UI, fragments are not used at all and we instead
		// use the older PreferenceActivity APIs.
		
		// Add 'general' preferences.
		addPreferencesFromResource(R.xml.pref_general);
		// When their values change, their summaries are updated
		// to reflect the new value, per the Android Design guidelines.
		
		bindPreferenceSummaryToValue(findPreference("image_size"));
		
	}
	
	
	/** {@inheritDoc} */
	@ Override
	public boolean onIsMultiPane()
	{
		return isXLargeTablet(this)&&!isSimplePreferences(this);
	}
	
	
	/**
	 * Helper method to determine if the device has an extra-large screen. For
	 * example, 10" tablets are extra-large.
	 */
	private static boolean isXLargeTablet(Context context)
	{
		return (context.getResources().getConfiguration().screenLayout&Configuration.SCREENLAYOUT_SIZE_MASK)>=Configuration.SCREENLAYOUT_SIZE_XLARGE;
	}
	
	
	/**
	 * Determines whether the simplified settings UI should be shown. This is
	 * true if this is forced via {@link #ALWAYS_SIMPLE_PREFS}, or the device
	 * doesn't have newer APIs like {@link PreferenceFragment}, or the device
	 * doesn't have an extra-large screen. In these cases, a single-pane
	 * "simplified" settings UI should be shown.
	 */
	private static boolean isSimplePreferences(Context context)
	{
		return ALWAYS_SIMPLE_PREFS
					||Build.VERSION.SDK_INT<Build.VERSION_CODES.HONEYCOMB
					||!isXLargeTablet(context);
	}
	
	
	/** {@inheritDoc} */
	@ Override
	@ TargetApi (Build.VERSION_CODES.HONEYCOMB)
	public void onBuildHeaders(List<Header> target)
	{
		if(!isSimplePreferences(this))
		{
			loadHeadersFromResource(R.xml.pref_headers, target);
		}
	}
	
	/**
	 * A preference value change listener that updates the preference's summary
	 * to reflect its new value.
	 */
	private static Preference.OnPreferenceChangeListener	sBindPreferenceSummaryToValueListener	=
																												new Preference.OnPreferenceChangeListener()
																												{
																													@ Override
																													public boolean onPreferenceChange(Preference preference, Object value)
																													{
																														String stringValue = value.toString();
																														
																														if(preference instanceof ListPreference)
																														{
																															// For list preferences,
																															// look up the
																															// correct display value
																															// in
																															// the preference's
																															// 'entries' list.
																															ListPreference listPreference = (ListPreference)preference;
																															int index = listPreference.findIndexOfValue(stringValue);
																															
																															// Set the summary to
																															// reflect the new
																															// value.
																															preference.setSummary(index>=0?listPreference.getEntries()[index]:null);
																															
																														}
																														else
																														{
																															// For all other
																															// preferences, set the
																															// summary to the value's
																															// simple string
																															// representation.
																															preference.setSummary(stringValue);
																														}
																														return true;
																													}
																												};
	
	
	/**
	 * Binds a preference's summary to its value. More specifically, when the
	 * preference's value is changed, its summary (line of text below the
	 * preference title) is updated to reflect the value. The summary is also
	 * immediately updated upon calling this method. The exact display format is
	 * dependent on the type of preference.
	 * 
	 * @see #sBindPreferenceSummaryToValueListener
	 */
	private static void bindPreferenceSummaryToValue(Preference preference)
	{
		// Set the listener to watch for value changes.
		preference
					.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);
		
		// Trigger the listener immediately with the preference's
		// current value.
		Log.i("setting", preference.getKey());
		sBindPreferenceSummaryToValueListener.onPreferenceChange(
					preference,
					PreferenceManager.getDefaultSharedPreferences(preference.getContext()).getInt(preference.getKey(), 720));
	}
	
	/**
	 * This fragment shows general preferences only. It is used when the
	 * activity is showing a two-pane settings UI.
	 */
	@ TargetApi (Build.VERSION_CODES.HONEYCOMB)
	public static class GeneralPreferenceFragment extends PreferenceFragment
	{
		@ Override
		public void onCreate(Bundle savedInstanceState)
		{
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.pref_general);
			
			// When their values change, their summaries are
			// updated to reflect the new value, per the Android Design
			// guidelines.
			
			bindPreferenceSummaryToValue(findPreference("image_size"));
		}
	}
	
}
