package com.glm.bean;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;

public class DiaryPicture {

	private long DiaryPictureID;
	
	private long PageID;
	
	private String DiaryImageURI;

	private int DiaryPictureH;
	
	private int DiaryPictureW;
	
	private int DiaryPictureRotation;
	
	private int DiaryPictureX;
	
	private int DiaryPictureY;
	/**identifica se l'immagine e una writehand*/
	private boolean DiaryHandImage=false;

	private byte[] byteImage;

	private Bitmap bitmapImage;
	/**
	 * @return the diaryImageURI
	 */
	public synchronized String getDiaryImageURI() {
		return DiaryImageURI;
	}

	/**
	 * @param diaryImageURI the diaryImageURI to set
	 */
	public synchronized void setDiaryImageURI(String diaryImageURI) {
		DiaryImageURI = diaryImageURI;
	}

	/**
	 * @return the diaryPictureH
	 */
	public synchronized int getDiaryPictureH() {
		return DiaryPictureH;
	}

	/**
	 * @param diaryPictureH the diaryPictureH to set
	 */
	public synchronized void setDiaryPictureH(int diaryPictureH) {
		DiaryPictureH = diaryPictureH;
	}

	/**
	 * @return the diaryPictureW
	 */
	public synchronized int getDiaryPictureW() {
		return DiaryPictureW;
	}

	/**
	 * @param diaryPictureW the diaryPictureW to set
	 */
	public synchronized void setDiaryPictureW(int diaryPictureW) {
		DiaryPictureW = diaryPictureW;
	}

	/**
	 * @return the diaryPictureRotation
	 */
	public synchronized int getDiaryPictureRotation() {
		return DiaryPictureRotation;
	}

	/**
	 * @param diaryPictureRotation the diaryPictureRotation to set
	 */
	public synchronized void setDiaryPictureRotation(int diaryPictureRotation) {
		DiaryPictureRotation = diaryPictureRotation;
	}

	/**
	 * @return the diaryPictureX
	 */
	public synchronized int getDiaryPictureX() {
		return DiaryPictureX;
	}

	/**
	 * @param diaryPictureX the diaryPictureX to set
	 */
	public synchronized void setDiaryPictureX(int diaryPictureX) {
		DiaryPictureX = diaryPictureX;
	}

	/**
	 * @return the diaryPictureY
	 */
	public synchronized int getDiaryPictureY() {
		return DiaryPictureY;
	}

	/**
	 * @param diaryPictureY the diaryPictureY to set
	 */
	public synchronized void setDiaryPictureY(int diaryPictureY) {
		DiaryPictureY = diaryPictureY;
	}

	/**
	 * @return the pageID
	 */
	public synchronized long getPageID() {
		return PageID;
	}

	/**
	 * @param pageID the pageID to set
	 */
	public synchronized void setPageID(long pageID) {
		PageID = pageID;
	}

	/**
	 * @return the diaryPictureID
	 */
	public synchronized long getDiaryPictureID() {
		return DiaryPictureID;
	}

	/**
	 * @param diaryPictureID the diaryPictureID to set
	 */
	public synchronized void setDiaryPictureID(long diaryPictureID) {
		DiaryPictureID = diaryPictureID;
	}

	/**
	 * @return the diaryHandImage
	 */
	public synchronized boolean isDiaryHandImage() {
		return DiaryHandImage;
	}

	/**
	 * @param diaryHandImage the diaryHandImage to set
	 */
	public synchronized void setDiaryHandImage(boolean diaryHandImage) {
		DiaryHandImage = diaryHandImage;
	}

	/**
	 * @return the bitmapImage
	 */
	public synchronized Bitmap getBitmapImage() {
		if(this.bitmapImage==null){
			if(this.byteImage!=null){
				this.bitmapImage = BitmapFactory.decodeByteArray(this.byteImage,0,this.byteImage.length);
				return this.bitmapImage;
			}
		}
		return this.bitmapImage;
	}

	/**
	 * @param bitmapImage the bitmapImage to set
	 */
	public synchronized void setBitmapImage(Bitmap bitmapImage) {
		this.bitmapImage = bitmapImage;
		this.setByteImage();
	}

	public synchronized byte[] getByteImage() {
		return this.byteImage;
	}

	private synchronized void setByteImage() {
		if(this.bitmapImage!=null){
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			this.bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, stream);
			byte[] byteArray = stream.toByteArray();
			this.bitmapImage.recycle();
			this.byteImage = byteImage;
		}
	}

	public synchronized void setByteImage(byte[] streamBytes){
			this.byteImage = streamBytes;
	}
}
