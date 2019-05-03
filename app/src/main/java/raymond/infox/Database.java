package raymond.infox;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;

public class Database extends SQLiteOpenHelper implements Serializable {

    private static final String TAG = "Database";
    private static final String TABLE_NAME = "barcode_table";
    private static final String COL1 = "Barcode";
    private static final String COL2 = "Item";
    private static final String COL3 = "Price";
    private static final String COL4 = "Image";
    private static final String COL5 = "Size";
    private static final String COL6 = "Category";

    /**
     * Constructor
     */
    Database(Context context) {
        super(context, TABLE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (" + COL1 + " varchar(255) UNIQUE, " +
                              COL2 + " varchar(255), " + COL3 + " varchar(16), " + COL4 + " varchar(255), " +
                              COL5 + " varchar(16), " + COL6 + " varchar(255))";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    /**
     * Adds data to the table
     *
     * @param code  Barcode
     * @param desc  Description of the item
     * @param price Price at scan
     * @return      true if successfully added, false otherwise
     */
    boolean addOrUpdateData(String code, String desc, String price, String image, String size, Department dep) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL1, code);
        contentValues.put(COL2, desc);
        contentValues.put(COL3, price);
        contentValues.put(COL4, image);
        contentValues.put(COL5, size);
        contentValues.put(COL6, dep.toString());

        Log.d(TAG, "addOrUpdateData: Adding " + code + " to " + TABLE_NAME);
        long result = db.insert(TABLE_NAME, null, contentValues);

        //if data is inserted incorrectly it will return -1
        if (result == -1) {
            db.update(TABLE_NAME, contentValues, String.format("%s = ?", COL1), new String[]{code} );
        }
        return true;
    }

    /**
     * Removes an entry with matching barcode in the database
     *
     * @param code  The barcode to be queried and deleted
     * @return      true if successful, else false
     */
    boolean removeData(String code) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_NAME, String.format("%s = ?", COL1), new String[]{code}) != 0;
    }

    /**
     * Returns the data in the database
     * @return  A cursor to the first entry in the database
     */
    Cursor getData(){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME;
        return db.rawQuery(query, null);
    }

    /**
     * Returns only the ID that matches the name passed in
     * @param name  The description of the item to be fetched
     * @return      The cursor to the item to be fetched
     */
    Cursor getItem(String name){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + COL2 + "=\"" + name + "\"";
        return db.rawQuery(query, null);
    }

    /**
     * Retrieves all entries in the database sorted by category/department.
     *
     * @return  An array of Cursors, each containing the items in their respective department
     */
    ArrayList<Cursor> getCategorizedData() {
        ArrayList<Cursor> ret = new ArrayList<>();
        SQLiteDatabase db = this.getWritableDatabase();
        for (Department d : Department.values()) {
            String department = d.toString();
            Bundle bundle = new Bundle();
            bundle.putString("department", department);
            String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + COL6 + "=\"" + department + "\"";
            Cursor cursor = db.rawQuery(query, null);
            cursor.setExtras(bundle);
            ret.add(cursor);
        }
        return  ret;
    }


}
