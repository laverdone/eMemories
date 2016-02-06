package com.glm.db;

import android.provider.BaseColumns;
/**CREATE  TABLE IF NOT EXISTS `diary` (
		  `diaryid` INT NOT NULL ,
		  `diaryname` VARCHAR(45) NULL ,
		  `diarydtcreation` DATETIME NULL DEFAULT CURRENT_TIMESTAMP ,
		  PRIMARY KEY (`diaryid`) );
*/
public interface DiaryTable extends BaseColumns {
	
	String TABLE_NAME = "diary";
	
	String DIARYID = "diaryid";
	
	String DIARYNAME = "diaryname";
	
	String DIARYTEMPLATE = "diarytemplate";
	
	String DIARYDTCREATION = "diarydtcreation";
	
	String DIARYDTMODIFY = "diarydtmodify";

    String CLOUDID = "cloudid";
	
	String[] COLUMNS = new String[] {DIARYID, DIARYNAME, DIARYTEMPLATE, DIARYDTCREATION, DIARYDTMODIFY,CLOUDID};

	
}
