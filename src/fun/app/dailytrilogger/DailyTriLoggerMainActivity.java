package fun.app.dailytrilogger;

import java.util.Date;
import java.util.NoSuchElementException;

import java.util.ArrayList;

import fun.app.dailytrilogger.R;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.Toast;
import android.widget.CalendarView.OnDateChangeListener;

public class DailyTriLoggerMainActivity extends FragmentActivity {

	public final static String logTag = "DailyTriLoggerMainActivity";
	public final static String YEAR = "fun.app.dailytrilogger.YEAR";
	public final static String MONTH = "fun.app.dailytrilogger.MONTH";
	public final static String DAY = "fun.app.dailytrilogger.DAY";
	public final static String EXISTING_LOGTEXT = "fun.app.dailytrilogger.LOGTEXT";
	public final static String CURRENT_LOG_DATE = "fun.app.dailytrilogger.CURRENT_LOG_DATE";
	public final static String CURRENT_LOG_TIME_BUCKET = "fun.app.dailytrilogger.CURRENT_LOG_TIME_BUCKET";
	public final static String CURRENT_TWEET_MORNING = "fun.app.dailytrilogger.CURRENT_TWEET_MORNING";
	public final static String CURRENT_TWEET_AFTERNOON = "fun.app.dailytrilogger.CURRENT_TWEET_AFTERNOON";
	public final static String CURRENT_TWEET_EVENING = "fun.app.dailytrilogger.CURRENT_TWEET_EVENING";
	public final static String CURRENT_SUMMARY_DATE = "fun.app.dailytrilogger.CURRENT_SUMMARY_DATE";
	public final static String CURRENT_SUMMARY_MODE = "fun.app.dailytrilogger.CURRENT_SUMMARY_MODE";


	private DailyTriLogStore dailyTriLogStore;
	private final static int LOGGER_ACTIVITY = 1;
	private final static int SUMMARY_ACTIVITY = 1;

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide fragments for each of the
	 * sections. We use a {@link android.support.v4.app.FragmentPagerAdapter} derivative, which will
	 * keep every loaded fragment in memory. If this becomes too memory intensive, it may be best
	 * to switch to a {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_daily_tri_logger_main);

		// Create the adapter that will return a fragment for each of the three primary sections
		// of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		if (dailyTriLogStore == null)	
		{
			dailyTriLogStore = DailyTriLogStore.getInstance();
			dailyTriLogStore.bootUp(this);
		}
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one of the primary
	 * sections of the app.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter  {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int i) {
			Fragment fragment = new DummySectionFragment();
			Bundle args = new Bundle();
			args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, i + 1);
			fragment.setArguments(args);
			return fragment;
		}

		@Override
		public int getCount() {
			return 3;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			switch (position) {
			case 0: return getString(R.string.title_section1).toUpperCase();
			case 1: return getString(R.string.title_section2).toUpperCase();
			}
			return null;
		}

	}


	/**
	 * A dummy fragment representing a section of the app, but that simply displays dummy text.
	 */
	public class DummySectionFragment extends Fragment  {
		public DummySectionFragment() {
		}

		public static final String ARG_SECTION_NUMBER = "section_number";


		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {

			// Get section number
			Bundle args = getArguments();
			int sectionNum = args.getInt(ARG_SECTION_NUMBER);

			// Section 1: Logger with Calendar
			if (sectionNum == 1) {

				// Create calendar view, can click on it, and it listens to date change
				CalendarView calView = new CalendarView(getActivity());
				calView.setClickable(true);

				calView.setOnDateChangeListener(new OnDateChangeListener() {

					@Override
					public void onSelectedDayChange(CalendarView view, int year, int month,
							int dayOfMonth) {

						Intent intent = new Intent(getBaseContext(), DailyTriLogActivity.class);
						intent.putExtra(YEAR, String.valueOf(year));
						intent.putExtra(MONTH, String.valueOf(month));
						intent.putExtra(DAY, String.valueOf(dayOfMonth));
						try {
							ArrayList<String> logTextArr  = dailyTriLogStore.getLogTextForDate(year, month, dayOfMonth);
							intent.putStringArrayListExtra(EXISTING_LOGTEXT, logTextArr);
						} 
						catch (NoSuchElementException e) {
							
						}
						finally {
							getActivity().startActivityForResult(intent, LOGGER_ACTIVITY);
						}
					}
				});

				return calView;
			} // Section 2: Summary Reports of Logs
			else {

				View v = inflater.inflate(R.layout.activity_summarizer, container, false);
				return v;
			}
		}


	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.i(logTag, "onActivityResult called: (Is resultCode RESULT_OK:" 
				+ String.valueOf(resultCode == RESULT_OK) + "), (Request code:" +
				String.valueOf(requestCode) + ")");

		if (resultCode == RESULT_OK && requestCode == LOGGER_ACTIVITY) {
			Date currentDate = (Date)data.getSerializableExtra(CURRENT_LOG_DATE);
			String tweetM = data.getStringExtra(CURRENT_TWEET_MORNING);
			String tweetA = data.getStringExtra(CURRENT_TWEET_AFTERNOON);
			String tweetE = data.getStringExtra(CURRENT_TWEET_EVENING);
			Log.i(logTag, "onActivityResult: Got current date " + currentDate.toString() +  " Tweet:" 
					+ "(" + tweetM + ", " + tweetA + ", " + tweetE + ")");
			dailyTriLogStore.appendTweet(currentDate, tweetM, tweetA, tweetE);
		} else if (resultCode == RESULT_OK && requestCode == SUMMARY_ACTIVITY) {
			Date currentDate = (Date)data.getSerializableExtra(CURRENT_SUMMARY_DATE);
			String mode = data.getStringExtra(CURRENT_SUMMARY_MODE);
			Log.i(logTag, "onActivityResult: Got current date " + currentDate.toString() + " Mode:" +
					mode);
		}
	}


}
