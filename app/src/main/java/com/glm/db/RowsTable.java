package com.glm.db;

import android.provider.BaseColumns;
/**
 * CREATE  TABLE IF NOT EXISTS `rows` (
  `rowid` INT NOT NULL ,
  `diaryid` INT NOT NULL ,
  `pageid` INT NOT NULL ,
  `rowtext` TEXT NULL ,
  `rownumber` INT NOT NULL DEFAULT 1,
   PRIMARY KEY (`rowid`));
 * */
public interface RowsTable extends BaseColumns {
	
	String TABLE_NAME = "rows";
	
	String ROWID = "rowid";
	
	String DIARYID = "diaryid";
	
	String PAGEID = "pageid";
	
	String ROWTEXT = "rowtext";
	
	String ROWNUMBER = "rownumber";
	
	String ROWPOSX = "rowx";
	
	String ROWPOSY = "rowy";
	
	String[] COLUMNS = new String[] {ROWID, DIARYID, PAGEID, ROWTEXT, ROWNUMBER,ROWPOSX,ROWPOSY};
}
