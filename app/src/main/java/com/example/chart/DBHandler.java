package com.example.chart;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DBHandler extends SQLiteOpenHelper {

    // creating a constant variables for our database.
    // below variable is for our database name.
    private static final String DB_NAME = "StressApp";

    // below int is our database version
    private static final int DB_VERSION = 1;

    // below variable is for our table name.
    private static final String TABLE_NAME = "Records";

    // below variable is for our id column.
    private static final String ID_COL = "id";

    // below variable is for our course name column
    private static final String MIN_COL = "min";

    // below variable id for our course duration column.
    private static final String MAX_COL = "max";

    // below variable for our course description column.
    private static final String AVG_COL = "avg";

    // below variable is for our course tracks column.
    private static final String DATA_COL = "data";

    // below variable is for our course tracks column.
    private static final String NAME_COL = "name";

    // creating a constructor for our database handler.
    public DBHandler(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    // below method is for creating a database by running a sqlite query
    @Override
    public void onCreate(SQLiteDatabase db) {
        // on below line we are creating
        // an sqlite query and we are
        // setting our column names
        // along with their data types.
        String query = "CREATE TABLE " + TABLE_NAME + " ("
                + ID_COL + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + NAME_COL + " TEXT,"
                + MIN_COL + " TEXT,"
                + MAX_COL + " TEXT,"
                + AVG_COL + " TEXT,"
                + DATA_COL + " TEXT)";

        // at last we are calling a exec sql
        // method to execute above sql query
        db.execSQL(query);
    }

    // this method is use to add new course to our sqlite database.
    public void addNewCourse(String maxValue, String minValue, String avgValue, String dataOverDb, String name) {

        // on below line we are creating a variable for
        // our sqlite database and calling writable method
        // as we are writing data in our database.
        SQLiteDatabase db = this.getWritableDatabase();

        // on below line we are creating a
        // variable for content values.
        ContentValues values = new ContentValues();

        // on below line we are passing all values
        // along with its key and value pair.
        values.put(NAME_COL, name);
        values.put(MAX_COL, maxValue);
        values.put(MIN_COL, minValue);
        values.put(DATA_COL, dataOverDb);
        values.put(AVG_COL, avgValue);

        // after adding all values we are passing
        // content values to our table.
        db.insert(TABLE_NAME, null, values);

        // at last we are closing our
        // database after adding database.
        db.close();
    }

    // we have created a new method for reading all the courses.
    public ArrayList<CourseModal> readCourses()
    {
        // on below line we are creating a
        // database for reading our database.
        SQLiteDatabase db = this.getReadableDatabase();

        // on below line we are creating a cursor with query to
        // read data from database.
        Cursor cursorCourses
                = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);

        // on below line we are creating a new array list.
        ArrayList<CourseModal> courseModalArrayList = new ArrayList<>();

        // moving our cursor to first position.
        if (cursorCourses.moveToFirst()) {
            do {
                // on below line we are adding the data from
                // cursor to our array list.
//                courseModalArrayList.add(new CourseModal(
//                        cursorCourses.getString(1),
//                        cursorCourses.getString(4),
//                        cursorCourses.getString(2),
//                        cursorCourses.getString(3)));
                courseModalArrayList.add(new CourseModal(
                        cursorCourses.getString(3),
                        cursorCourses.getString(2),
                        cursorCourses.getString(4),
                        cursorCourses.getString(5),
                        cursorCourses.getString(1)));


            } while (cursorCourses.moveToNext());
            // moving our cursor to next.
        }
        // at last closing our cursor
        // and returning our array list.
        cursorCourses.close();
        return courseModalArrayList;
    }

    // below is the method for updating our courses
    public void updateCourse(String originalMaxValue, String maxValue, String minValue,
                             String avgValue, String dataOverDb, String fileName) {

        // calling a method to get writable database.
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        // on below line we are passing all values
        // along with its key and value pair.
        values.put(MAX_COL, maxValue);
        values.put(MIN_COL, minValue);
        values.put(AVG_COL, avgValue);
        values.put(DATA_COL, dataOverDb);
        values.put(NAME_COL, fileName);

        // on below line we are calling a update method to update our database and passing our values.
        // and we are comparing it with name of our course which is stored in original name variable.
        db.update(TABLE_NAME, values, "name=?", new String[]{originalMaxValue});
        db.close();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // this method is called to check if the table exists already.
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
}
