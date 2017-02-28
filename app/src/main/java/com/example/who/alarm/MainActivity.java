package com.example.who.alarm;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.provider.CalendarContract;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.who.alarm.utils.Calendar;
import com.example.who.alarm.utils.Event;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.TimeZone;

import static android.R.attr.cacheColorHint;
import static android.R.attr.id;
import static java.security.AccessController.getContext;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    Calendar mCalendar;
    java.util.Calendar calendar;
    TextView timeFromText;
    TextView dateText;
    EditText title;
    EditText description;
    //TimePicker timeStart;
    Button timeStart;
    //DatePicker dateDate;
    Button dateDate;
    Button ok;
    TextView dateTimeParse;
    int DIALOG_TIME = 1;
    int myHour;
    int myMinute;
    int DIALOG_DATE = 2;
    int myYear;
    int myMonth;
    int myDay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        title = (EditText) findViewById(R.id.title);
        description = (EditText) findViewById(R.id.description);
        description.setVisibility(View.GONE);
//        timeStart = (TimePicker) findViewById(R.id.timeStart);
//        timeStart.setIs24HourView(true);
        timeStart = (Button) findViewById(R.id.dialog_time);
        setupUI(timeStart);
        timeFromText = (TextView) findViewById(R.id.timeFromText);
        setupUI(timeFromText);
        dateText = (TextView) findViewById(R.id.dateText);
        setupUI(dateText);
//        dateDate = (DatePicker) findViewById(R.id.dateAlarm);
//        setupUI(dateDate);
        dateDate = (Button) findViewById(R.id.dialog_date);
        ok = (Button) findViewById(R.id.good);
        dateTimeParse = (TextView) findViewById(R.id.date_time_parse);
        getCurrentDate();
        getParsedData(myYear, myMonth, myDay, myHour, myMinute);
        getSupportActionBar().hide();
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR},
                1);

    }
    private void getCurrentDate(){
        calendar = java.util.Calendar.getInstance();
        myYear = calendar.get(java.util.Calendar.YEAR);
        myMonth = calendar.get(java.util.Calendar.MONTH);
        myDay = calendar.get(java.util.Calendar.DATE);
        myHour = calendar.get(java.util.Calendar.HOUR_OF_DAY);
        myMinute = calendar.get(java.util.Calendar.MINUTE);
    }

    private void getParsedData(int myYear, int myMonth, int myDay, int myHour, int myMinute) {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.set(java.util.Calendar.YEAR, myYear);
        calendar.set(java.util.Calendar.MONTH, myMonth);
        calendar.set(java.util.Calendar.DATE, myDay);
        calendar.set(java.util.Calendar.HOUR_OF_DAY, myHour);
        calendar.set(java.util.Calendar.MINUTE, myMinute);
        SimpleDateFormat format = new SimpleDateFormat("EEEE, MMMM dd, ' ' yyyy ' ' HH:mm:ss");
        String strDate = format.format(calendar.getTime());
        dateTimeParse.setText(strDate);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getCalendar();
                } else {
                    Toast.makeText(MainActivity.this, getResources().getString(R.string.no_permission), Toast.LENGTH_SHORT).show();
                    onBackPressed();
                }
                return;
            }
        }
    }

    private void getCalendar() {
        final List<Calendar> calendars = Calendar.getWritableCalendars(getContentResolver());
        mCalendar = calendars.get(0);

    }

    private long createEvent(long calendarID) throws SecurityException {
        ContentResolver cr = getContentResolver();
        ContentValues contentValues = new ContentValues();
        contentValues.put(CalendarContract.Events.CALENDAR_ID, calendarID);
        contentValues.put(CalendarContract.Events.ALL_DAY, false);
        contentValues.put(CalendarContract.Events.TITLE, title.getText().toString());
        contentValues.put(CalendarContract.Events.DESCRIPTION, description.getText().toString());
        contentValues.put(CalendarContract.Events.DTSTART, getDateStartMillis() + 60000);
        contentValues.put(CalendarContract.Events.DTEND, getDateStartMillis() + 120000);
        contentValues.put(CalendarContract.Events.HAS_ALARM, true);
        contentValues.put(CalendarContract.Events.EVENT_TIMEZONE, CalendarContract.Calendars.CALENDAR_TIME_ZONE);
        Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, contentValues);
        long id = Integer.parseInt(uri.getLastPathSegment());
        return id;
    }


//    private long getDateStartMillis() {
//        calendar = java.util.Calendar.getInstance();
//        int year = dateDate.getYear();
//        int monthOfYear = dateDate.getMonth();
//        int dayOfMonth = dateDate.getDayOfMonth();
//        calendar.set(year, monthOfYear, dayOfMonth);
//        int hourOfDay = 0;
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
//            hourOfDay = timeStart.getHour();
//        } else {
//            hourOfDay = timeStart.getCurrentHour();
//        }
//        int minute = 0;
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
//            minute = timeStart.getMinute();
//        } else {
//            minute = timeStart.getCurrentMinute();
//        }
//        calendar.set(java.util.Calendar.HOUR_OF_DAY, hourOfDay);
//        calendar.set(java.util.Calendar.MINUTE, minute);
//        return calendar.getTimeInMillis();
//    }

    private long getDateStartMillis() {
        int year = myYear;
        int monthOfYear = myMonth;
        int dayOfMonth = myDay;
        calendar.set(year, monthOfYear, dayOfMonth);
        int hourOfDay = 0;
        hourOfDay = myHour;
        int minute = 0;
        minute = myMinute;
        calendar.set(java.util.Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(java.util.Calendar.MINUTE, minute);
        return calendar.getTimeInMillis();
    }


    private void setReminder(long eventID, int timeBefore) throws SecurityException {
        try {
            ContentResolver cr = getContentResolver();
            ContentValues values = new ContentValues();
            values.put(CalendarContract.Reminders.MINUTES, timeBefore);
            values.put(CalendarContract.Reminders.EVENT_ID, eventID);
            values.put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT);
            Uri uri = cr.insert(CalendarContract.Reminders.CONTENT_URI, values);
            long id = Integer.parseInt(uri.getLastPathSegment());
            Cursor c = CalendarContract.Reminders.query(cr, eventID,
                    new String[]{CalendarContract.Reminders.MINUTES});
            if (c.moveToFirst()) {
            }
            values.notifyAll();
            c.notifyAll();
            c.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void runForest(View v) {
        setReminder(createEvent(mCalendar.id), 1);
        AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
        dialog.setTitle(getResources().getString(R.string.more));
        dialog.setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                finish();
                startActivity(getIntent());
            }
        }).setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                onBackPressed();
                finish();
            }
        });
        dialog.setCancelable(true);
        dialog.create();
        dialog.show();
    }

    public void setupUI(View view) {
        if (!(view instanceof EditText)) {
            view.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    hideSoftKeyboard(MainActivity.this);
                    return false;
                }
            });
        }
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(
                activity.getCurrentFocus().getWindowToken(), 0);
    }

    public void showTimeDialog(View v){this.showDialog(DIALOG_TIME);}
    public void showDateDialog(View v){this.showDialog(DIALOG_DATE);}
    protected Dialog onCreateDialog(int id) {
        if (id == DIALOG_TIME) {
            TimePickerDialog tpd = new TimePickerDialog(this, myCallBackTime, myHour, myMinute, true);
            return tpd;
        }
        else if (id == DIALOG_DATE) {
            DatePickerDialog tpd = new DatePickerDialog(this, myCallBackDate, myYear, myMonth, myDay);
            return tpd;
        }

        return super.onCreateDialog(id);
    }

    TimePickerDialog.OnTimeSetListener myCallBackTime = new TimePickerDialog.OnTimeSetListener() {
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            myHour = hourOfDay;
            myMinute = minute;
            getParsedData(myYear, myMonth, myDay, hourOfDay, minute );
        }
    };

    DatePickerDialog.OnDateSetListener myCallBackDate = new DatePickerDialog.OnDateSetListener() {

        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            myYear = year;
            myMonth = monthOfYear;
            myDay = dayOfMonth;
            getParsedData(year, monthOfYear, dayOfMonth, myHour, myMinute );
        }
    };
}
