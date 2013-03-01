package fun.app.dailytrilogger;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import android.app.Activity;
import android.content.Context;
import android.util.Log;


public class DailyTriLogStore {

	private static final String logTag = "DailyTriLoggerDataStore";
	private static final String logDataFileName = "DailyTriLog.json";
	private static volatile DailyTriLogStore instance = null;
	private Activity mActivity;
	HashMap<String, ArrayList<String>> logMap;
	
	public static DailyTriLogStore getInstance() {
		if (instance == null) {
			synchronized (DailyTriLogStore.class) {
				if (instance == null)
					instance = new DailyTriLogStore();
			}
		}
		return instance;
	}
	
	public boolean bootUp(Activity mainActivity) {
		Log.i(logTag, "bootUp called!");
		if (mActivity == null)
			mActivity = mainActivity;
		JsonParser parser = new JsonParser();
		try {
			FileInputStream fis =  mActivity.openFileInput(logDataFileName);
			BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
			Object obj = parser.parse(reader);
			JsonObject jsonObject = (JsonObject) obj;
			String jStr = jsonObject.toString();
			Gson gson = new GsonBuilder().create();
			Type typeOfHashMap = new TypeToken<Map<String, ArrayList<String>>>() { }.getType();
			logMap = gson.fromJson(jStr, typeOfHashMap);
			return true;
		}
		catch (FileNotFoundException e) {
			Log.i(logTag, "mediFileNotFound");
			e.printStackTrace();
		}
		catch (Exception e) {
			Log.i(logTag, "some other exception on medi file open");
			e.printStackTrace();
		}
		if (logMap == null)
			logMap = new HashMap<String, ArrayList<String>>();
		Log.i(logTag, "Size of logMap:" + String.valueOf(logMap.size()));
		return false;
}
	
	private Date getDateForYYMMDD(int yy, int mm, int dd) {
		Calendar cl = GregorianCalendar.getInstance();
		cl.set(yy, mm, dd, 0, 0, 0);
		Date current = cl.getTime();
		return current;
	}
	
	public ArrayList<String> getLogTextForDate(int yy, int mm, int dd) {
		Date d = getDateForYYMMDD(yy, mm, dd);
		String key = getDateAsStringKey(d);
		if (logMap.containsKey(key))
			return logMap.get(key);
		throw new NoSuchElementException();
	}
	
	private String getDateAsStringKey(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH);
		int day = cal.get(Calendar.DAY_OF_MONTH);

		String strDate = String.valueOf(day) + "/" + String.valueOf(month) + "/" + String.valueOf(year);
		return strDate;
	}
	
	// THIS IS FUNDAMENTALLY UNSCALABLE!
		// Figure out a way to append to existing json
		// and not serialize everything on every append
		private boolean saveMapAsJsonToFile(HashMap mMap, String fileName) {
			GsonBuilder gsb = new GsonBuilder();
			Gson gson = gsb.create();
			String json = gson.toJson(mMap);
			Log.i(DailyTriLoggerMainActivity.logTag, "JSON:" + json);
			try {
				FileOutputStream fos = mActivity.openFileOutput(fileName, Context.MODE_PRIVATE);
				fos.write(json.getBytes());
				fos.close();
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
			return true;
		}
	
    public boolean appendTweet(Date currentDate, String tweetM, String tweetA, String tweetE) {
    	String strDate = getDateAsStringKey(currentDate);
    	ArrayList<String> aList = new ArrayList<String>();
    	aList.add(tweetM);
    	aList.add(tweetA);
    	aList.add(tweetE);
    	logMap.put(strDate, aList);
    	return saveMapAsJsonToFile(logMap, logDataFileName);
    }
}
