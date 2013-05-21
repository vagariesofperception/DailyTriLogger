package fun.app.dailytrilogger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.Html;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;


public class DailyTriLogStore {

	private static final String logTag = "DailyTriLoggerDataStore";
	private static final String logDataFileName = "DailyTriLog.json";
	private static final String htmlLogFileName = "DailyTriLog.html";
	private static volatile DailyTriLogStore instance = null;
	private Activity mActivity;
	private String currentLogStr;
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


	private String getDateAsStringKeyOffset(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH);
		int day = cal.get(Calendar.DAY_OF_MONTH);

		String strDate = String.valueOf(day) + "/" + String.valueOf(month+1) + "/" + String.valueOf(year);
		return strDate;
	}

	private String getDateAsStringMMDDYYYY(Date date) {
		String strDate = DateFormat.format("MMM dd, yyyy", date).toString();
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
	// in format dd/MM/YY
	private Date getDateForString(String dateStr) {
		String[] tokens = dateStr.split("/");
		Calendar cal = Calendar.getInstance();
		cal.set(Integer.parseInt(tokens[2]),
				Integer.parseInt(tokens[1]),
				Integer.parseInt(tokens[0]), 0, 0, 0);
		return cal.getTime();
	}

	private SortedSet<Date> getKeysAsSortedDateList(HashMap<String, ArrayList<String>> dateToLogMap) {
		Set<String> keys = dateToLogMap.keySet();
		SortedSet<Date> sSet = new TreeSet<Date>();
		Iterator iter = keys.iterator();
		while (iter.hasNext()) {
			String s = (String)iter.next();
			Date d = getDateForString(s);
			sSet.add(d);
		}
		return sSet;
	}

	private Date addOneDay(Date dy) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(dy);
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH);
		int day = cal.get(Calendar.DAY_OF_MONTH);
		cal.set(year, month, day, 0, 0, 0);
		cal.add(Calendar.DATE, 1);
		Date current = cal.getTime();
		return current;
	}

	public ArrayList<String> getWeeksDataAsHTMLTables() {
		// Get the sorted list of Dates
		HashMap mMap = logMap;
		HashMap<String, ArrayList<String>> dateToLogMap = (HashMap<String, ArrayList<String>>)mMap;
		SortedSet<Date> sDateList =  getKeysAsSortedDateList(dateToLogMap);
		ArrayList<String> tablesArr = new ArrayList<String>();

		// Corner Case
		if (sDateList.size() == 0)
			return tablesArr;

		// Get the first Sunday before first date
		Iterator iter = sDateList.iterator();
		Date d = (Date)iter.next();
		Calendar cal = Calendar.getInstance();
		cal.setTime(d);
		int offset = (cal.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY)%7;
		cal.add(Calendar.DAY_OF_YEAR, -offset);

		int count = offset;
		String dateStr = getDateAsStringMMDDYYYY(cal.getTime());

		StringBuilder sb = new StringBuilder();

		sb.append("<table border=\"1\" style=\"background-color:#B0B0B0;\">\n");
		sb.append("<tr>\n");
		sb.append("<th>Week of " + dateStr +"</th>\n");
		sb.append("<th> Work </th>\n");
		sb.append("<th> Food </th>\n");
		sb.append("<th> Reading </th>\n");
		sb.append("</tr>\n");

		iter = sDateList.iterator();
		while (iter.hasNext())
		{
			Date dy = (Date)iter.next();
			String dStr = getDateAsStringKey(dy);
			ArrayList<String> aList = dateToLogMap.get(dStr);
			dStr = getDateAsStringKeyOffset(dy);
			sb.append("<tr>\n");
			sb.append("<td><b>");
			sb.append(dStr);
			sb.append("</b></td>\n");

			sb.append("<td>");
			sb.append(aList.get(0));
			sb.append("</td>\n");

			sb.append("<td>");
			sb.append(aList.get(1));
			sb.append("</td>\n");

			sb.append("<td>");
			sb.append(aList.get(2));
			sb.append("</td>\n");
			sb.append("</tr>\n");

			count++;
			if (count %7 == 0)
			{
				Date nextDy = addOneDay(dy);
				sb.append("</table>\n");
				sb.append("<br><br><br>\n");
				tablesArr.add(sb.toString());
				sb = new StringBuilder();
				sb.append("<table border=\"1\" style=\"background-color:#B0B0B0;\">\n");
				sb.append("<tr>\n");
				sb.append("<th>Week of " + getDateAsStringMMDDYYYY(nextDy) +"</th>\n");
				sb.append("<th> Work </th>\n");
				sb.append("<th> Food </th>\n");
				sb.append("<th> Reading </th>\n");
				sb.append("</tr>\n");
			}

		}
		sb.append("</table>\n");
		tablesArr.add(sb.toString());
		return tablesArr;
	}

	private String writeMapAsHTML(HashMap mMap, PrintWriter pw) {

		// Get the sorted list of Dates
		HashMap<String, ArrayList<String>> dateToLogMap = (HashMap<String, ArrayList<String>>)mMap;
		SortedSet<Date> sDateList =  getKeysAsSortedDateList(dateToLogMap);

		// Corner Case
		if (sDateList.size() == 0)
			return new String("");

		// Get the first Sunday before first date
		Iterator iter = sDateList.iterator();
		Date d = (Date)iter.next();
		Calendar cal = Calendar.getInstance();
		cal.setTime(d);
		int offset = (cal.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY)%7;
		cal.add(Calendar.DAY_OF_YEAR, -offset);

		int count = offset;
		String dateStr = getDateAsStringKeyOffset(cal.getTime());

		StringBuilder sb = new StringBuilder();
		sb.append("<!DOCTYPE html><html>\n<head>\n<title>Daily Log Report</title>\n<body>");


		sb.append("<table border=\"1\" style=\"background-color:#B0B0B0;\">\n");
		sb.append("<tr>\n");
		sb.append("<th>Week of " + dateStr +"</th>\n");
		sb.append("<th> Work </th>\n");
		sb.append("<th> Food </th>\n");
		sb.append("<th> Reading </th>\n");
		sb.append("</tr>\n");

		iter = sDateList.iterator();
		while (iter.hasNext())
		{
			Date dy = (Date)iter.next();
			String dStr = getDateAsStringKey(dy);
			ArrayList<String> aList = dateToLogMap.get(dStr);
			dStr = getDateAsStringKeyOffset(dy);
			sb.append("<tr>\n");
			sb.append("<td><b>");
			sb.append(dStr);
			sb.append("</b></td>\n");

			sb.append("<td>");
			sb.append(aList.get(0));
			sb.append("</td>\n");

			sb.append("<td>");
			sb.append(aList.get(1));
			sb.append("</td>\n");

			sb.append("<td>");
			sb.append(aList.get(2));
			sb.append("</td>\n");
			sb.append("</tr>\n");

			count++;
			if (count %7 == 0)
			{
				Date nextDy = addOneDay(dy);
				sb.append("</table>\n");
				sb.append("<br><br><br>\n");
				sb.append("<table border=\"1\" style=\"background-color:#B0B0B0;\">\n");
				sb.append("<tr>\n");
				sb.append("<th>Week of " + getDateAsStringKeyOffset(nextDy) +"</th>\n");
				sb.append("<th> Work </th>\n");
				sb.append("<th> Food </th>\n");
				sb.append("<th> Reading </th>\n");
				sb.append("</tr>\n");
			}

		}
		sb.append("</table>\n");
		sb.append("</body>\n</html>\n");
		currentLogStr = sb.toString();
		pw.write(currentLogStr);
		return currentLogStr;
	}

	// Adapted from : 
	// https://github.com/stephendnicholas/Android-Apps/blob/master/Gmail%20Attacher/src/com/stephendnicholas/gmailattach/Utils.java
	private File createHTMLFileForLog(Context context, String fileName, HashMap mMap) {
		File cFile = new File(context.getCacheDir() + File.separator + fileName);
		try {
			cFile.createNewFile();
			FileOutputStream oStream = new FileOutputStream(cFile);
			OutputStreamWriter osw = new OutputStreamWriter(oStream);
			PrintWriter pw = new PrintWriter(osw);

			writeMapAsHTML(mMap, pw);
			//pw.print(htmlStr);

			pw.flush();
			pw.close();
		}
		catch (IOException e) {
			Log.i(logTag, "IOException thrown");
		}
		return cFile;
	}
	private String getMapAsJsonString(HashMap mMap) {
		GsonBuilder gsb = new GsonBuilder();
		Gson gson = gsb.create();
		String json = gson.toJson(mMap);
		return json;
	}

	// Adapted from : 
	// https://github.com/stephendnicholas/Android-Apps/blob/master/Gmail%20Attacher/src/com/stephendnicholas/gmailattach/Utils.java
	private File createJsonFileInCacheUsingMap(Context context, String fileName, HashMap mMap) {
		File cFile = new File(context.getCacheDir() + File.separator + fileName);
		try {
			cFile.createNewFile();
			FileOutputStream oStream = new FileOutputStream(cFile);
			OutputStreamWriter osw = new OutputStreamWriter(oStream);
			PrintWriter pw = new PrintWriter(osw);

			String jsonStr = getMapAsJsonString(mMap);
			pw.print(jsonStr);

			pw.flush();
			pw.close();
		}
		catch (IOException e) {
			Log.i(logTag, "IOException thrown");
		}
		return cFile;
	}

	public String getLogTextForEmail() {
		if (currentLogStr == null)
			currentLogStr = new String();
		return currentLogStr;
	}


	public File prepareDataForEmail(Context context) {
		createJsonFileInCacheUsingMap(context, logDataFileName, logMap);
		return createHTMLFileForLog(context, htmlLogFileName, logMap);
	}


	// Adapted from: http://stackoverflow.com/questions/9587559/android-intent-send-an-email-with-attachment
	public void email (Context context, String emailTo, String emailCC, 
			String subject, String emailText)
	{
		try {

			final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND_MULTIPLE);
			emailIntent.setType("text/html");
			emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, 
					new String[]{emailTo});
			emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
			emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, Html.fromHtml(emailText));

			ArrayList<Uri> uris = new ArrayList<Uri>();


			Uri logHTMLUri = Uri.parse("content://" + DailyTriLoggerContentProvider.auth + "/" + htmlLogFileName);
			uris.add(logHTMLUri);
			Uri logUri = Uri.parse("content://" + DailyTriLoggerContentProvider.auth + "/" + logDataFileName);
			uris.add(logUri);

			emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
			context.startActivity(emailIntent);
		}
		catch (ActivityNotFoundException ae) {
			Log.i(logTag, "Did not send email! No email activity found");
		}
	}

	public void clearAllData() {

	}
}
