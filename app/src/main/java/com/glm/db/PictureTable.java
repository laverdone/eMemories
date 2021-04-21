package com.glm.db;

import android.provider.BaseColumns;
/**
 * CREATE  TABLE IF NOT EXISTS `picture` (
  `pictureid` INT NOT NULL ,
  `diaryid` INT NOT NULL ,
  `pageid` INT NOT NULL ,
  `pictureuri` VARCHAR(100) NOT NULL ,
  `pictureh` INT NOT NULL ,
  `picturew` INT NOT NULL ,
  `picturerotation` DOUBLE NOT NULL DEFAULT 0 ,
  `picturex` INT NOT NULL DEFAULT 0 ,
  `picturey` INT NOT NULL DEFAULT 0 ,
  PRIMARY KEY (`pictureid`));
 * */
public interface PictureTable extends BaseColumns {
	
	String TABLE_NAME = "picture";
	
	String PICTUREID = "pictureid";
	
	String DIARYID = "diaryid";
	
	String PAGEID = "pageid";

	String PICTUREPREVIEW = "picturpreview";

	String PICTUREURI = "pictureuri";
	
	String PICTUREH = "pictureh";
	
	String PICTUREW = "picturew";
	
	String PICTUREROTATION = "picturerotation";
	
	String PICTUREX = "picturex";
	
	String PICTUREY = "picturey";
	
	String PICTUREHAND = "picturehand";
	
	String[] COLUMNS = new String[] {PICTUREID, DIARYID, PAGEID, PICTUREPREVIEW, PICTUREURI, PICTUREH, PICTUREW, PICTUREROTATION, PICTUREX, PICTUREY,PICTUREHAND};
}
