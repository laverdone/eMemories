package com.glm.db;

import android.provider.BaseColumns;

/**CREATE  TABLE IF NOT EXISTS `diary` (
		  `diaryid` INT NOT NULL ,
		  `diaryname` VARCHAR(45) NULL ,
		  `diarydtcreation` DATETIME NULL DEFAULT CURRENT_TIMESTAMP ,
		  PRIMARY KEY (`diaryid`) );
*/
public interface DiarySearchTable extends BaseColumns {
	
	String TABLE_NAME = "diary_search";

    String ROWID = "rowid";

	String DIARYID = "diaryid";
	
	String PAGEID = "pageID";
	
	String PAGETEXT = "pagetext";
	
	String[] COLUMNS = new String[] {ROWID, DIARYID, PAGEID, PAGETEXT};

	
}
