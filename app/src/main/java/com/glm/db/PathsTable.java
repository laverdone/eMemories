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
public interface PathsTable extends BaseColumns {
	
	String TABLE_NAME = "paths";
	
	String PATHID = "pathid";
	
	String DIARYID = "diaryid";
	
	String PAGEID = "pageid";
	
	String PATHX = "pathx";
	
	String PATHY = "pathy";
	
	String PATHCOLOR = "pathcolor";
	
	String PATHSTROKEWIDTH = "pathstrokewidth";
	
	String[] COLUMNS = new String[] {PATHID, DIARYID, PAGEID, PATHX, PATHY,PATHCOLOR,PATHSTROKEWIDTH};
}
