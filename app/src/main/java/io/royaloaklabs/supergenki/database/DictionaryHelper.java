package io.royaloaklabs.supergenki.database;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.*;

public class DictionaryHelper extends SQLiteOpenHelper {
  private static final String DATABASE_NAME = "jisho-main.db";
  private static final int DATABASE_VERSION = 2;
  private final String DATABASE_PATH;

  private SQLiteDatabase database;

  private final Context context;

  public DictionaryHelper(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
    this.context = context;
    DATABASE_PATH = this.context.getDatabasePath(DATABASE_NAME).getPath();
    Log.i("DictionaryHelper", "Loading DB");
    if(this.hasNoDatabase()) {
      try {
        this.copyInternalDatabase();
      } catch(IOException e) {
        e.printStackTrace();
      }
    } else {
      this.getWritableDatabase();
    }
    // TODO is this even needed after copy?
    database = (database == null)
        ? SQLiteDatabase.openDatabase(DATABASE_PATH, null, SQLiteDatabase.OPEN_READONLY)
        : database;
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    // TODO Autogenerated onCreate method
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    try {
      Log.i("onUpgrade", "Upgrading SQL Database");
      File databaseFile = this.context.getDatabasePath(DATABASE_NAME);
      if (databaseFile.exists()) {
        databaseFile.delete();
      }
      this.copyInternalDatabase();
    } catch(IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public SQLiteDatabase getReadableDatabase() throws SQLException {
    return database;
  }

  private boolean hasNoDatabase() {
    File db = this.context.getDatabasePath(DATABASE_NAME);
    if(db.length() == 0) {
      return true;
    }
    return false;
  }

  private void copyInternalDatabase() throws IOException {
    File databaseFile = this.context.getDatabasePath(DATABASE_NAME);

    if (!databaseFile.exists()) {
      if(!databaseFile.getParentFile().exists()) {
        databaseFile.getParentFile().mkdir();
      }
    }

    InputStream is = this.context.getAssets().open(DATABASE_NAME);
    OutputStream os = new FileOutputStream(databaseFile);

    byte[] buffer = new byte[1028];
    int length;
    while((length = is.read(buffer)) > 0) {
      os.write(buffer, 0, length);
    }

    os.flush();

    is.close();
    os.close();
  }
}
