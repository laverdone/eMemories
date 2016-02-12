package com.glm.db;

import android.provider.BaseColumns;
/**
 * CREATE  TABLE IF NOT EXISTS `pages` (
  `pageid` INT NOT NULL ,
  `diaryid` INT NOT NULL ,
  `pagedtcreation` DATETIME NULL ,
  `pagenumber` INT NULL DEFAULT 1 ,
  `pagebookmark` TINYINT(1) NULL DEFAULT 0 ,
  `pagealt` DOUBLE NULL ,
  `pagelat` DOUBLE NULL ,
  `pagelong` DOUBLE NULL ,
  `pageloc` VARCHAR(100) NULL ,
  PRIMARY KEY (`pageid`, `diaryid`));
 * 
 * */
public interface PagesTable extends BaseColumns {
	
	String TABLE_NAME = "pages";
	
	String PAGEID = "pageid";
	
	String DIARYID = "diaryid";
	
	String PAGEDTCREATION = "pagedtcreation";
	
	String PAGENUMBER = "pagenumber";
	
	String PAGEBOOKMARK = "pagebookmark";
	
	String PAGEALT = "pagealt";
	
	String PAGELAT = "pagelat";
	
	String PAGELONG = "pagelong";
	
	String PAGELOC = "pageloc";

	String PAGEORIENTATION = "pageorientation";
	
	String[] COLUMNS = new String[] {PAGEID, DIARYID, PAGEDTCREATION, PAGENUMBER, PAGEBOOKMARK, PAGEALT, PAGELAT, PAGELONG, PAGELOC, PAGEORIENTATION};

}
