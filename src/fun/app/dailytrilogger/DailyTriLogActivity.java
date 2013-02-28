package fun.app.dailytrilogger;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;

public class DailyTriLogActivity extends Activity implements TextView.OnEditorActionListener {
	private TextView logDate;
	private TextView logDateVal;
	private TextView morningTV, afternoonTV, eveningTV;
	private EditText morningTweet, afternoonTweet, eveningTweet;

	private int currentYear, currentMonth, currentDay;
	private int currentMediMins;
	private Date currentDate;
	
	private String currentTweetM, currentTweetA, currentTweetE;

	public final String logTag = "DailyTriLogActivity";
	//public HashMap<Date, Integer> mediMap;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_logger);

		if (logDate == null)
			logDate = (TextView) findViewById(R.id.logDate);

		if (logDateVal == null)
			logDateVal = (TextView) findViewById(R.id.logDateVal);
		
		if (morningTV == null)
			morningTV = (TextView) findViewById(R.id.logMorningTV);
		
		if (afternoonTV == null)
			afternoonTV = (TextView) findViewById(R.id.logAfternoonTV);
		
		if (eveningTV == null)
			eveningTV = (TextView) findViewById(R.id.logEveningTV);
		
		if (morningTweet == null)
			morningTweet = (EditText) findViewById(R.id.logMorning);
		
		if (afternoonTweet == null)
			afternoonTweet = (EditText) findViewById(R.id.logAfternoon);
		
		if (eveningTweet == null)
			eveningTweet = (EditText) findViewById(R.id.logEvening);


		// This is to prevent automatic popping of keyboard
		// for the edittext
		// http://stackoverflow.com/questions/2496901/android-on-screen-keyboard-auto-popping-up
		this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

		// Get the date that was clicked and add it to
		// info row
		Intent intent = getIntent();
		currentYear = Integer.parseInt(intent.getStringExtra(DailyTriLoggerMainActivity.YEAR));
		currentMonth = Integer.parseInt(intent.getStringExtra(DailyTriLoggerMainActivity.MONTH));
		currentDay = Integer.parseInt(intent.getStringExtra(DailyTriLoggerMainActivity.DAY));
		
		currentTweetM = new String();
		currentTweetA = new String();
		currentTweetE = new String();
		if (intent.hasExtra(DailyTriLoggerMainActivity.EXISTING_LOGTEXT))
		{
			ArrayList<String> tweetS = intent.getStringArrayListExtra(DailyTriLoggerMainActivity.EXISTING_LOGTEXT);
			currentTweetM = tweetS.get(0);
			currentTweetA = tweetS.get(1);
			currentTweetE = tweetS.get(2);
		}
		
		//mediMap = (HashMap) intent.getSerializableExtra(MediRunMainActivity.MEDIMAP);

		Log.i(logTag, "onCreate:" +  String.valueOf(currentMonth) + "/" + 
				String.valueOf(currentDay) + "/" + String.valueOf(currentYear));
		Date d = null;
		Calendar cal = GregorianCalendar.getInstance();
		cal.set(currentYear, currentMonth, currentDay);
		d = cal.getTime();
		currentDate = d;
		String strDate = DateFormat.format("MMMM dd, yyyy", d).toString();
		logDateVal.setText(strDate);


	}

  public void updateLogButtonClick(View view) {
		Log.i(logTag, "updateLogButtonClick called!");
        Intent intent = new Intent(this, DailyTriLoggerMainActivity.class);
        intent.putExtra(DailyTriLoggerMainActivity.CURRENT_LOG_DATE, currentDate);
        intent.putExtra(DailyTriLoggerMainActivity.CURRENT_TWEET_MORNING, String.valueOf(currentTweetM));
        intent.putExtra(DailyTriLoggerMainActivity.CURRENT_TWEET_AFTERNOON, String.valueOf(currentTweetA));
        intent.putExtra(DailyTriLoggerMainActivity.CURRENT_TWEET_EVENING, String.valueOf(currentTweetE));
        // Activity finished ok, return the data
        setResult(RESULT_OK, intent);
        finish();
	}

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
         return true;
		/*boolean handled = false;
		if (actionId == EditorInfo.IME_ACTION_DONE) {
			String s = v.getText().toString();
			Log.i(logTag, "Medi Mins:" + s + " on " + String.valueOf(currentMonth) + "/" + 
					String.valueOf(currentDay) + "/" + String.valueOf(currentYear));
			try {
			currentMediMins = Integer.parseInt(s);
			}
			catch (NumberFormatException e) {
				currentMediMins = 0;
			}
			//mediMap.put(currentDate, Integer.decode(s));
		}
		return handled;*/
	}
}
