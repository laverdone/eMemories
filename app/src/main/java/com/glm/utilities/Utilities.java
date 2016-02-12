package com.glm.utilities;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.glm.bean.User;
import com.glm.db.PersistenceException;
import com.glm.db.Repository;

import android.content.Context;
import android.database.Cursor;
import android.os.Environment;
import android.util.Log;

public class Utilities {

	private static final String TAG = "Utilies";

	private Context context;
	private Repository dbAdapter;
	private Exporter _export;

	private String EXPORT_FILE_NAME = Environment.getExternalStorageDirectory().getPath()+ "/AppGianoNotes/";

	public Utilities(Context context) {
		setDbAdapter(new Repository(context));
		this.setContext(context);	
	}

	public void openGoogleDrive() {

	}

	public void closeGoogleDrive() {

	}


	public Context getContext() {
		return context;
	}

	public void setContext(Context context) {
		this.context = context;
	}

	public Repository getDbAdapter() {
		return dbAdapter;
	}

	public void setDbAdapter(Repository dbAdapter) {
		this.dbAdapter = dbAdapter;
	}


	public void initXmlDB(User user) {
		File f = new File(Environment.getExternalStorageDirectory().getPath()+ "/AppGianoNotes/" + 
				user.getId() + "_" + user.getUsername() + "_" + user.getPassword() + "_" +
				user.getEmail() + "_" + user.getName() + "_" + user.getSurname() + ".xml");
		
		if(f.exists()) 
			f.delete();
		
		File myFile = new File(EXPORT_FILE_NAME.concat(user.getId() + "_" + user.getUsername() + "_" +
				user.getPassword() + "_" + user.getEmail() + "_" + user.getName() + "_" +
				user.getSurname() + ".xml"));

		try {
			myFile.createNewFile();

			FileOutputStream fos = new FileOutputStream(myFile);
			BufferedOutputStream bos = new BufferedOutputStream(fos);

			this._export = new Exporter(bos);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public void exportData(int idUser) {

		Log.v(TAG, "Exporting data");

		try {

			this._export.startDbExport(this.dbAdapter.getmDb().getPath());

			//get the tables out of the given sqlite database
			String sql = "SELECT * FROM sqlite_master";


			this.getDbAdapter().open();

			Cursor cur = this.dbAdapter.getmDb().rawQuery(sql, new String[0]);
			Log.v(TAG , "show tables, cur size " + cur.getCount() );
			cur.moveToFirst();

			String tableName;

			while (cur.getPosition() < cur.getCount()) {

				tableName = cur.getString(cur.getColumnIndex("name"));
				Log.v(TAG, "table name " + tableName );

				// don't process these two tables since they are used
				// for metadata
				if (!tableName.equals( "android_metadata" )&&
						!tableName.equals( "sqlite_sequence" )&&
						!tableName.equals("sqlite_autoindex_Users_1")&&
						!tableName.equals("Users")) {

					exportTable(tableName, idUser);
				}

				cur.moveToNext();
			}
			_export.endDbExport();
			_export.close();
			
			cur.close();
			this.getDbAdapter().close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (PersistenceException e) {
			e.printStackTrace();
		}
	}


	private void exportTable(String tableName, int idUser) throws IOException {

		_export.startTable(tableName);

		// get everything from the table
		String sql = "select * from " + tableName + " where ID_USER = '"+ idUser + "'";
		Cursor cur = this.dbAdapter.getmDb().rawQuery(sql, new String[0]);

		int numcols = cur.getColumnCount();

		Log.v(TAG, "Start exporting table " + tableName);

		//		// logging
		//		for( int idx = 0; idx < numcols; idx++ )
		//		{
		//			log( "column " + cur.getColumnName(idx) );
		//		}

		cur.moveToFirst();

		// move through the table, creating rows
		// and adding each column with name and value
		// to the row
		while(cur.getPosition() < cur.getCount()) {

			_export.startRow();

			String name;
			String val;

			for(int idx = 0; idx < numcols; idx++) {

				name = cur.getColumnName(idx);
				val = cur.getString( idx );

				Log.v(TAG, "col '" + name + "' -- val '" + val + "'");

				_export.addColumn(name, val);
			}

			_export.endRow();
			cur.moveToNext();
		}

		cur.close();

		_export.endTable();
	}

	/**
	 * Questa classe mi serve per convetire i dati nel db
	 * in xml
	 * @author coluzza
	 */
	class Exporter {

		private BufferedOutputStream _bos;

		private static final String CLOSING_WITH_TICK = "'>";
		private static final String START_DB = "<export-database name='";
		private static final String END_DB = "</export-database>";
		private static final String START_TABLE = "<table name='";
		private static final String END_TABLE = "</table>";
		private static final String START_ROW = "<row>";
		private static final String END_ROW = "</row>";
		private static final String START_COL = "<col name='";
		private static final String END_COL = "</col>";

		public Exporter() throws FileNotFoundException {

			this(new BufferedOutputStream(context.openFileOutput(EXPORT_FILE_NAME, Context.MODE_WORLD_READABLE)));
		}


		public Exporter( BufferedOutputStream bos ) {
			_bos = bos;
		}


		public void close() throws IOException {

			if (_bos != null) {
				_bos.close();
			}
		}


		public void startDbExport(String dbName) throws IOException {

			String s = START_DB + dbName + CLOSING_WITH_TICK;
			_bos.write(s.getBytes());
		}


		public void endDbExport() throws IOException {
			_bos.write(END_DB.getBytes());
		}


		public void startTable(String tableName ) throws IOException {
			String s = START_TABLE + tableName + CLOSING_WITH_TICK;
			_bos.write( s.getBytes() );
		}


		public void endTable() throws IOException {
			_bos.write(END_TABLE.getBytes());
		}


		public void startRow() throws IOException {
			_bos.write( START_ROW.getBytes() );
		}


		public void endRow() throws IOException {
			_bos.write( END_ROW.getBytes() );
		}


		public void addColumn(String name, String val) throws IOException {
			String s = START_COL + name + CLOSING_WITH_TICK + val + END_COL;
			_bos.write(s.getBytes());
		}
	}
}