package com.glm.db;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.text.MessageFormat;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

public class DBAdapter {
	private static final String TAG = "DBAdapter";
	private DatabaseHelper mDbHelper;
	private static SQLiteDatabase mDb;
	private static String DB_PATH = "/data/data/%PACKAGE%/databases/";
	private static final String DATABASE_NAME = "DiaryDB";
	private static final int DB_VERSION = 5;
	private final Context adapterContext;
	private static String mDbFullPath;
	public DBAdapter(Context context) {
		this.adapterContext = context;
	}

	public DBAdapter open() throws PersistenceException {
		mDbHelper = new DatabaseHelper(adapterContext);

		/*try {
			mDbHelper.checkDatabase();
		} catch (NullPointerException e) {
			Log.e(TAG, "NullPointer nell' open() quando faccio .createDataBase()");
		}*/

		try {
			mDbHelper.openDataBase();
			Log.v(this.getClass().getCanonicalName(), "Database aperto");
		} catch (IllegalStateException e) {
			Log.e(this.getClass().getCanonicalName(), "Unable to open database");
			throw new PersistenceException(e.getMessage());
		}
		return this;
	}

	public void close() {
		if(mDbHelper!=null) mDbHelper.close();
	}
	
	public Cursor fetchAll(String table) {
		return mDb.query(table, null,null,null,null,null,null);              
	}
	
	public SQLiteDatabase getmDb() {
		return mDb;
	}

	public void setmDb(SQLiteDatabase mDb) {
		DBAdapter.mDb = mDb;
	}


	private static class DatabaseHelper extends SQLiteOpenHelper {	
        private Context mContext;
		public DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DB_VERSION);
            mContext=context;

            /*if(Environment.getExternalStorageDirectory().exists() && Environment.getExternalStorageDirectory().canWrite()){
				File fAppPath=new File(Environment.getExternalStorageDirectory().getPath() + "/"+context.getPackageName()+"/");
	            if(!fAppPath.exists()) fAppPath.mkdir();
	            DB_PATH = Environment.getExternalStorageDirectory().getPath() + "/"+context.getPackageName()+"/db/";//
			}else{
				DB_PATH=DB_PATH.replace("%PACKAGE%", context.getPackageName());
			}
			File fPath = new File(DB_PATH);
			if(!fPath.exists()) fPath.mkdir();
			Log.v(getClass().getName(), "new DatabaseHelper: DB_NAME="+DATABASE_NAME+" DB_PATH="+DB_PATH);*/
		}


		public void createDB(SQLiteDatabase db) throws PersistenceException {
			try {
		
				/***
				 * 
				 * Table Diary
				 * 
				 */
				String creationDiary = "CREATE TABLE IF NOT EXISTS {0} " +
						"({1} INT PRIMARY KEY," +
						"{2} VARCHAR(100) NULL," +
						"{3} BLOB," +
						"{4} DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
						"{5} DATETIME NOT NULL," +
						"{6} INT NOT NULL DEFAULT 0, " +
						"{7} VARCHAR(80) NULL)";

				db.execSQL(MessageFormat.format(creationDiary, DiaryTable.TABLE_NAME, DiaryTable.DIARYID, 
						DiaryTable.DIARYNAME, DiaryTable.DIARYPREVIEW,DiaryTable.DIARYDTCREATION,DiaryTable.DIARYDTMODIFY, DiaryTable.DIARYTEMPLATE,DiaryTable.CLOUDID));
				
				/***
				 * 
				 * Table Pages
				 * 
				 */
				String creationPages = "CREATE TABLE IF NOT EXISTS {0} " +
						"({1} INT PRIMARY KEY," +
						"{2} INT NOT NULL," +
						"{3} BLOB," +
						"{4} BLOB," +
						"{5} DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP," +
						"{6} INT NOT NULL DEFAULT 1," +
						"{7} TINYINT(1) NULL DEFAULT 0," +
						"{8} DOUBLE NULL," +
						"{9} DOUBLE NULL," +
						"{10} DOUBLE NULL," +
						"{11} VARCHAR(100) NULL," +
						"{12} INT NOT NULL DEFAULT 1)";

				db.execSQL(MessageFormat.format(creationPages, PagesTable.TABLE_NAME, PagesTable.PAGEID, PagesTable.DIARYID,
						PagesTable.PAGEPREVIEW, PagesTable.PAGEHANDWRITE, PagesTable.PAGEDTCREATION, PagesTable.PAGENUMBER, PagesTable.PAGEBOOKMARK, PagesTable.PAGEALT,
						PagesTable.PAGELAT, PagesTable.PAGELONG, PagesTable.PAGELOC, PagesTable.PAGEORIENTATION));
				
				
				/***
				 * 
				 * Table Rows
				 * 
				 */
				String creationRows = "CREATE TABLE IF NOT EXISTS {0} " +
						"({1} INT NOT NULL," +
						"{2} INT NOT NULL," +
						"{3} INT NOT NULL," +
						"{4} TEXT NOT NULL," +
						"{5} INT NOT NULL DEFAULT 1," +
						"{6} INT NOT NULL DEFAULT 0,"+
						"{7} INT NOT NULL DEFAULT 0," +
						"PRIMARY KEY ( {1},{2},{3}))";

				db.execSQL(MessageFormat.format(creationRows, RowsTable.TABLE_NAME, RowsTable.ROWID, RowsTable.DIARYID, 
						RowsTable.PAGEID, RowsTable.ROWTEXT, RowsTable.ROWNUMBER,RowsTable.ROWPOSX,RowsTable.ROWPOSY));


				/***
				 * 
				 * Table Picture
				 * 
				 */
				String creationPicture = "CREATE TABLE IF NOT EXISTS {0} " +
						"({1} INT NOT NULL," +
						"{2} INT NOT NULL," +
						"{3} INT NOT NULL," +
						"{4} BLOB," +
						"{5} VARCHAR(100) NOT NULL," +
						"{6} INT NOT NULL," +
						"{7} INT NOT NULL," +
						"{8} DOUBLE NOT NULL DEFAULT 0," +
						"{9} INT NOT NULL DEFAULT 0," +
						"{10} INT NOT NULL DEFAULT 0," +
						"{11} TINYINT(1) NULL DEFAULT 0," +
						"PRIMARY KEY ( {1},{2},{3}))";

				db.execSQL(MessageFormat.format(creationPicture, PictureTable.TABLE_NAME, PictureTable.PICTUREID, PictureTable.DIARYID, PictureTable.PAGEID,
						PictureTable.PICTUREPREVIEW,PictureTable.PICTUREURI, PictureTable.PICTUREH, PictureTable.PICTUREW, PictureTable.PICTUREROTATION,
						PictureTable.PICTUREX, PictureTable.PICTUREY,PictureTable.PICTUREHAND));
				
				/***
				 * 
				 * Table PATH
				 * 
				 */
				String creationPaths = "CREATE TABLE IF NOT EXISTS {0} " +
						"({1} INT NOT NULL," +
						"{2} INT NOT NULL," +
						"{3} INT NOT NULL," +
						"{4} float NOT NULL DEFAULT 0," +
						"{5} float NOT NULL DEFAULT 0," +
						"{6} INT NOT NULL DEFAULT 0,"+
						"{7} float NOT NULL DEFAULT 3)";

				db.execSQL(MessageFormat.format(creationPaths, PathsTable.TABLE_NAME, PathsTable.PATHID, PathsTable.DIARYID, 
						PathsTable.PAGEID, PathsTable.PATHX, PathsTable.PATHY,PathsTable.PATHCOLOR,PathsTable.PATHSTROKEWIDTH));

			} catch (SQLiteException e) {
				Log.e(TAG, "Errore nelle stringhe di creazione del db");
				e.printStackTrace();
				//Cancello il DB se la creazione 
				File fdb = new File(mDbFullPath);
				if(fdb.exists()) fdb.delete();
				throw new PersistenceException(e.getMessage());
			}
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			try{
                Log.d(this.getClass().getCanonicalName(), "DB version " + oldVersion + " new " + newVersion + " Success!");

                if(oldVersion < 1 ) {
                    String creationSearch = "CREATE VIRTUAL TABLE {0} USING FTS3" +
                            "({1} INT NOT NULL," +
                            "{2} INT NOT NULL," +
                            "{3} INT NOT NULL," +
                            "{4} TEXT)";

                    db.execSQL(MessageFormat.format(creationSearch, DiarySearchTable.TABLE_NAME, DiarySearchTable.ROWID, DiarySearchTable.PAGEID, DiarySearchTable.DIARYID,
                            DiarySearchTable.PAGETEXT));
                    Log.d(this.getClass().getCanonicalName(), "Create Search Virtual Table success!");

                    db.execSQL("insert into diary_search " +
                            "select rows.rowid as rowid, pages.pageid as pageid, " +
                            "       pages.diaryid as diaryid, " +
                            "       rows.rowtext as pagetext " +
                            "       from pages,rows where pages.pageid=rows.pageid and pages.diaryid=rows.diaryid");
                    Log.d(this.getClass().getCanonicalName(), "Populate Search Virtual Table Success!");

                    db.execSQL("CREATE TRIGGER update_search_trigger " +
                            "       AFTER UPDATE ON rows " +
                            "BEGIN " +
                            "    UPDATE diary_search " +
                            "       SET pagetext = new.rowtext " +
                            "     WHERE rowid = old.rowid  " +
                            "           AND " +
                            "           pageid = old.pageid  " +
                            "           AND " +
                            "           diaryid = old.diaryid; " +
                            "END;");
                    Log.d(this.getClass().getCanonicalName(), "CREATE TRIGGER ON UPDATE Success!");

                    db.execSQL("CREATE TRIGGER insert_diary_search after insert ON rows " +
                            "                        BEGIN " +
                            "                insert into diary_search select rows.rowid as rowid, rows.pageid as pageid, " +
                            "                rows.diaryid as diaryid, " +
                            "                        rows.rowtext as pagetext " +
                            "                from rows where rows.pageid=new.pageid and rows.diaryid=new.diaryid and rows.rowid=new.rowid; " +
                            "                END;");
                    Log.d(this.getClass().getCanonicalName(), "CREATE TRIGGER ON INSERT Success!");


                    db.execSQL("CREATE TRIGGER delete_diary_search after delete ON rows " +
                            "                        BEGIN " +
                            "                delete from diary_search where pageid=old.pageid and diaryid=old.diaryid and rowid=old.rowid; " +
                            "                END;");
                    Log.d(this.getClass().getCanonicalName(), "CREATE TRIGGER ON DELETE Success!");

                    String creationPaths = "DROP TABLE {0} ";

                    db.execSQL(MessageFormat.format(creationPaths, PathsTable.TABLE_NAME));
                    Log.d(this.getClass().getCanonicalName(), "Drop Paths Table Success!");

                    Log.d(this.getClass().getCanonicalName(), "Upgrade DB from " + oldVersion + " To " + newVersion + " Success!");
                }else if(oldVersion < 5 ) {
                    Log.d(this.getClass().getCanonicalName(), "ALTER TABLE FOR cluodid Success!");
                    db.execSQL("alter table diary add column "+DiaryTable.CLOUDID+"  VARCHAR(80);");
                }
            }catch (SQLiteException e) {
                Log.e(this.getClass().getCanonicalName(), "Errore Upgrade DB from "+oldVersion+" To "+newVersion);
                e.printStackTrace();

            }
		}

		/**
		 * Questo metodo apre il database
		 * @throws SQLException
		 */
		public void openDataBase() {
			try{
                String myPath = DB_PATH + DATABASE_NAME;
                //mDb = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
                mDb = this.getWritableDatabase();
            }catch (IllegalStateException e1){
                Log.e(this.getClass().getCanonicalName(), "IllegalStateException on open DB");
                System.exit(0);
            }
		}
		
		/**
		 * Questo metodo chiude il database
		 */
		@Override
		public synchronized void close() {

			if (mDb != null)
				mDb.close();

			super.close();

		}


		@Override
		public void onCreate(SQLiteDatabase db) {

                File fdb = new File(mContext.getFilesDir().getPath() + "/"+mContext.getPackageName()+"/db/"+DATABASE_NAME);
                //Copio i vecchi dati
                if(fdb.exists()){
                    db.close();
                    //Copio e rimuovo il vecchio DB
                    if(copyOldDataBase()){
                        db=getWritableDatabase();
                        //Aggiorno la vecchia struttura
                        //onUpgrade(db,DB_VERSION-1,DB_VERSION);
                    }
                }else{
					/***
					 *
					 * Table Diary
					 *
					 */
					String creationDiary = "CREATE TABLE IF NOT EXISTS {0} " +
							"({1} INT PRIMARY KEY," +
							"{2} VARCHAR(100) NULL," +
							"{3} BLOB," +
							"{4} DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
							"{5} DATETIME NOT NULL," +
							"{6} INT NOT NULL DEFAULT 0, " +
							"{7} VARCHAR(80) NULL)";

					db.execSQL(MessageFormat.format(creationDiary, DiaryTable.TABLE_NAME, DiaryTable.DIARYID,
							DiaryTable.DIARYNAME, DiaryTable.DIARYPREVIEW,DiaryTable.DIARYDTCREATION,DiaryTable.DIARYDTMODIFY, DiaryTable.DIARYTEMPLATE,DiaryTable.CLOUDID));

					/***
					 *
					 * Table Pages
					 *
					 */
					String creationPages = "CREATE TABLE IF NOT EXISTS {0} " +
							"({1} INT PRIMARY KEY," +
							"{2} INT NOT NULL," +
							"{3} BLOB," +
							"{4} BLOB," +
							"{5} DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP," +
							"{6} INT NOT NULL DEFAULT 1," +
							"{7} TINYINT(1) NULL DEFAULT 0," +
							"{8} DOUBLE NULL," +
							"{9} DOUBLE NULL," +
							"{10} DOUBLE NULL," +
							"{11} VARCHAR(100) NULL," +
							"{12} INT NOT NULL DEFAULT 1)";

					db.execSQL(MessageFormat.format(creationPages, PagesTable.TABLE_NAME, PagesTable.PAGEID, PagesTable.DIARYID,
							PagesTable.PAGEPREVIEW, PagesTable.PAGEHANDWRITE, PagesTable.PAGEDTCREATION, PagesTable.PAGENUMBER, PagesTable.PAGEBOOKMARK, PagesTable.PAGEALT,
							PagesTable.PAGELAT, PagesTable.PAGELONG, PagesTable.PAGELOC, PagesTable.PAGEORIENTATION));


					/***
					 *
					 * Table Rows
					 *
					 */
					String creationRows = "CREATE TABLE IF NOT EXISTS {0} " +
							"({1} INT NOT NULL," +
							"{2} INT NOT NULL," +
							"{3} INT NOT NULL," +
							"{4} TEXT NOT NULL," +
							"{5} INT NOT NULL DEFAULT 1," +
							"{6} INT NOT NULL DEFAULT 0,"+
							"{7} INT NOT NULL DEFAULT 0," +
							"PRIMARY KEY ( {1},{2},{3}))";

					db.execSQL(MessageFormat.format(creationRows, RowsTable.TABLE_NAME, RowsTable.ROWID, RowsTable.DIARYID,
							RowsTable.PAGEID, RowsTable.ROWTEXT, RowsTable.ROWNUMBER,RowsTable.ROWPOSX,RowsTable.ROWPOSY));


					/***
					 *
					 * Table Picture
					 *
					 */
					String creationPicture = "CREATE TABLE IF NOT EXISTS {0} " +
							"({1} INT NOT NULL," +
							"{2} INT NOT NULL," +
							"{3} INT NOT NULL," +
							"{4} BLOB," +
							"{5} VARCHAR(100) NOT NULL," +
							"{6} INT NOT NULL," +
							"{7} INT NOT NULL," +
							"{8} DOUBLE NOT NULL DEFAULT 0," +
							"{9} INT NOT NULL DEFAULT 0," +
							"{10} INT NOT NULL DEFAULT 0," +
							"{11} TINYINT(1) NULL DEFAULT 0," +
							"PRIMARY KEY ( {1},{2},{3}))";

					db.execSQL(MessageFormat.format(creationPicture, PictureTable.TABLE_NAME, PictureTable.PICTUREID, PictureTable.DIARYID, PictureTable.PAGEID,
							PictureTable.PICTUREPREVIEW,PictureTable.PICTUREURI, PictureTable.PICTUREH, PictureTable.PICTUREW, PictureTable.PICTUREROTATION,
							PictureTable.PICTUREX, PictureTable.PICTUREY,PictureTable.PICTUREHAND));

					/***
					 *
					 * Table PATH
					 *
					 */
					String creationPaths = "CREATE TABLE IF NOT EXISTS {0} " +
							"({1} INT NOT NULL," +
							"{2} INT NOT NULL," +
							"{3} INT NOT NULL," +
							"{4} float NOT NULL DEFAULT 0," +
							"{5} float NOT NULL DEFAULT 0," +
							"{6} INT NOT NULL DEFAULT 0,"+
							"{7} float NOT NULL DEFAULT 3)";

					db.execSQL(MessageFormat.format(creationPaths, PathsTable.TABLE_NAME, PathsTable.PATHID, PathsTable.DIARYID,
							PathsTable.PAGEID, PathsTable.PATHX, PathsTable.PATHY,PathsTable.PATHCOLOR,PathsTable.PATHSTROKEWIDTH));

                    String creationSearch = "CREATE VIRTUAL TABLE {0} USING FTS3" +
                            "({1} INT NOT NULL," +
                            "{2} INT NOT NULL," +
                            "{3} INT NOT NULL," +
                            "{4} TEXT)";

                    db.execSQL(MessageFormat.format(creationSearch, DiarySearchTable.TABLE_NAME, DiarySearchTable.ROWID, DiarySearchTable.PAGEID, DiarySearchTable.DIARYID,
                            DiarySearchTable.PAGETEXT));
                    Log.d(this.getClass().getCanonicalName(),"Create Search Virtual Table success!");

                    db.execSQL("insert into diary_search " +
                            "select rows.rowid as rowid, pages.pageid as pageid, " +
                            "       pages.diaryid as diaryid, " +
                            "       rows.rowtext as pagetext " +
                            "       from pages,rows where pages.pageid=rows.pageid and pages.diaryid=rows.diaryid");
                    Log.d(this.getClass().getCanonicalName(),"Populate Search Virtual Table Success!");

                    db.execSQL("CREATE TRIGGER update_search_trigger " +
                            "       AFTER UPDATE ON rows " +
                            "BEGIN " +
                            "    UPDATE diary_search " +
                            "       SET pagetext = new.rowtext " +
                            "     WHERE rowid = old.rowid  " +
                            "           AND " +
                            "           pageid = old.pageid  " +
                            "           AND " +
                            "           diaryid = old.diaryid; " +
                            "END;");
                    Log.d(this.getClass().getCanonicalName(),"CREATE TRIGGER ON UPDATE Success!");

                    db.execSQL("CREATE TRIGGER insert_diary_search after insert ON rows " +
                            "                        BEGIN " +
                            "                insert into diary_search select rows.rowid as rowid, rows.pageid as pageid, " +
                            "                rows.diaryid as diaryid, " +
                            "                        rows.rowtext as pagetext " +
                            "                from rows where rows.pageid=new.pageid and rows.diaryid=new.diaryid and rows.rowid=new.rowid; " +
                            "                END;");
                    Log.d(this.getClass().getCanonicalName(),"CREATE TRIGGER ON INSERT Success!");


                    db.execSQL("CREATE TRIGGER delete_diary_search after delete ON rows " +
                            "                        BEGIN " +
                            "                delete from diary_search where pageid=old.pageid and diaryid=old.diaryid and rowid=old.rowid; " +
                            "                END;");
                    Log.d(this.getClass().getCanonicalName(),"CREATE TRIGGER ON DELETE Success!");

                    Log.v(TAG, "Create DataBase ");
                }
        }
        /**
         * Copies your database from your local assets-folder to the just created
         * empty database in the system folder, from where it can be accessed and
         * handled. This is done by transfering bytestream.
         * */
        private boolean copyOldDataBase() {
            boolean nResult = false;

            try {
                DB_PATH=DB_PATH.replace("%PACKAGE%", mContext.getPackageName());
                File fdb = new File(mContext.getFilesDir().getPath() + "/"+mContext.getPackageName()+"/db/"+DATABASE_NAME);
                if(fdb.exists()){
                    int iDbLen = (int)fdb.length();
                    byte [] imgData = new byte[iDbLen];
                    FileInputStream fis = new FileInputStream(fdb);
                    fis.read(imgData);
                    fis.close();
                    FileOutputStream f = new FileOutputStream(new File(DB_PATH+DATABASE_NAME));
                    f.write(imgData);
                    f.flush();
                    f.close();
                    if(fdb.delete()){
                        //Cancello il file di journal se presente
                        fdb = new File(mContext.getFilesDir().getPath() + "/"+mContext.getPackageName()+"/db/"+DATABASE_NAME+"-journal");
                        fdb.delete();
                        File fdbFolder = new File(mContext.getFilesDir().getPath() + "/"+mContext.getPackageName()+"/db");
                        nResult= fdbFolder.delete();
                    }
                }
                return nResult;



            } catch (FileNotFoundException e) {
                Log.e(this.getClass().getCanonicalName(),"FileNotFoundException coping old DB");
                e.printStackTrace();
                return nResult;
            } catch (IOException e) {
                Log.e(this.getClass().getCanonicalName(),"Error coping old DB");
                e.printStackTrace();
                return nResult;
            }
        }
	}
}