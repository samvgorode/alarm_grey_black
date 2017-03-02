package com.example.who.alarm;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.who.alarm.utils.Calendar;

import java.text.SimpleDateFormat;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    Calendar mCalendar;
    java.util.Calendar calendar;
    TextView timeFromText;
    TextView dateText;
    EditText title;
    EditText description;
    Button timeStart;
    Button dateDate;
    Button ok;
    TextView dateTimeParse;
    int myHour;
    int myMinute;
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
        timeStart = (Button) findViewById(R.id.dialog_time);
        setupUI(timeStart);
        timeFromText = (TextView) findViewById(R.id.timeFromText);
        setupUI(timeFromText);
        dateText = (TextView) findViewById(R.id.dateText);
        setupUI(dateText);
        dateDate = (Button) findViewById(R.id.dialog_date);
        setupUI(dateDate);
        ok = (Button) findViewById(R.id.good);
        setupUI(ok);
        dateTimeParse = (TextView) findViewById(R.id.date_time_parse);
        setupUI(dateTimeParse);
        getCurrentDate();
        getParsedData(myYear, myMonth, myDay, myHour, myMinute);
        getSupportActionBar().hide();
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR},
                1);

    }

    private void getCurrentDate() {
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
        SimpleDateFormat format = new SimpleDateFormat("EEEE, dd MMMM, yyyy '\n' HH:mm:ss");
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
        AlertDialog.Builder dialog = new AlertDialog.Builder(this, R.style.DialogTheme);
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
        setReminder(createEvent(mCalendar.id), 1);
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

    public void showTimeDialog(View v) {
        if (v == timeStart) {
            TimePickerDialog tpd = new TimePickerDialog(this, R.style.DialogTheme, myCallBackTime, myHour, myMinute, true);
            tpd.show();
        }
    }

    public void showDateDialog(View v) {
        if (v == dateDate) {
            DatePickerDialog dpd = new DatePickerDialog(this, R.style.DialogTheme, myCallBackDate, myYear, myMonth, myDay);
            dpd.show();
        }
    }

    TimePickerDialog.OnTimeSetListener myCallBackTime = new TimePickerDialog.OnTimeSetListener() {
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            myHour = hourOfDay;
            myMinute = minute;
            getParsedData(myYear, myMonth, myDay, hourOfDay, minute);
        }
    };

    DatePickerDialog.OnDateSetListener myCallBackDate = new DatePickerDialog.OnDateSetListener() {

        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            myYear = year;
            myMonth = monthOfYear;
            myDay = dayOfMonth;
            getParsedData(year, monthOfYear, dayOfMonth, myHour, myMinute);
        }
    };
}
