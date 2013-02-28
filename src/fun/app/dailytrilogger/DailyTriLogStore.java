package fun.app.dailytrilogger;

import java.util.ArrayList;
import java.util.Date;

import android.app.Activity;


public class DailyTriLogStore {

	private static final String logTag = "DailyTriLoggerDataStore";
	private static volatile DailyTriLogStore instance = null;
	
	public static DailyTriLogStore getInstance() {
		if (instance == null) {
			synchronized (DailyTriLogStore.class) {
				if (instance == null)
					instance = new DailyTriLogStore();
			}
		}
		return instance;
	}
	
	public void bootUp(Activity mainActivity) {
		
	}
	
	public boolean getLogTextForDate(int year, int month, int dayOfMonth, ArrayList<String> logTextArr) {
		
		return false;
	}
	
    public void appendTweet(Date currentDate, String tweetM, String tweetA, String tweetE) {
    	
    }
}
