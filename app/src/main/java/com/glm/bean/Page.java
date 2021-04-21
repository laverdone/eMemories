package com.glm.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;

public class Page implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private long PageID;
	private long DiaryID;
	private byte[] byteImageHW;
	private byte[] byteImagePreviewPage;

	private Date PageDTCreation;
	private int PageNumber;
	private Hashtable<Long, Row> PageRows;
	private boolean PageBookMark;
	private double PageLat;
	private double PageLong;
	private double PageAlt;
	private String PageLoc;
	private Hashtable<Long, DiaryPicture> DiaryImage;
	private ArrayList<HandWritePath> mPath = new ArrayList<HandWritePath>();
	//1 LANDSPACE=verticale 2 PORTRAIN=orizzontale
	private int PageOrientation=1;
	public long getPageID() {
		return PageID;
	}
	public void setPageID(long pageID) {
		this.PageID = pageID;
	}
	/**
	 * @return the diaryID
	 */
	public synchronized long getDiaryID() {
		return DiaryID;
	}
	
	/**
	 * @return the pageDTCreation
	 */
	public synchronized Date getPageDTCreation() {
		return PageDTCreation;
	}
	/**
	 * @param pageDTCreation the pageDTCreation to set
	 */
	public synchronized void setPageDTCreation(Date pageDTCreation) {
		PageDTCreation = pageDTCreation;
	}
	/**
	 * @return the pageNumber
	 */
	public synchronized int getPageNumber() {
		return PageNumber;
	}
	/**
	 * @param pageNumber the pageNumber to set
	 */
	public synchronized void setPageNumber(int pageNumber) {
		PageNumber = pageNumber;
	}
	/**
	 * @return the pageRows
	 */
	public synchronized Hashtable<Long, Row> getPageRows() {
		return PageRows;
	}
	/**
	 * @param pageRows the pageRows to set
	 */
	public synchronized void setPageRows(Hashtable<Long, Row> pageRows) {
		PageRows = pageRows;
	}
	/**
	 * @return the pageBookMark
	 */
	public synchronized boolean isPageBookMark() {
		return PageBookMark;
	}
	/**
	 * @param pageBookMark the pageBookMark to set
	 */
	public synchronized void setPageBookMark(boolean pageBookMark) {
		PageBookMark = pageBookMark;
	}
	/**
	 * @return the pageLat
	 */
	public synchronized double getPageLat() {
		return PageLat;
	}
	/**
	 * @param pageLat the pageLat to set
	 */
	public synchronized void setPageLat(double pageLat) {
		PageLat = pageLat;
	}
	/**
	 * @return the pageLong
	 */
	public synchronized double getPageLong() {
		return PageLong;
	}
	/**
	 * @param pageLong the pageLong to set
	 */
	public synchronized void setPageLong(double pageLong) {
		PageLong = pageLong;
	}
	/**
	 * @return the pageAlt
	 */
	public synchronized double getPageAlt() {
		return PageAlt;
	}
	/**
	 * @param pageAlt the pageAlt to set
	 */
	public synchronized void setPageAlt(double pageAlt) {
		PageAlt = pageAlt;
	}
	/**
	 * @return the pageLoc
	 */
	public synchronized String getPageLoc() {
		return PageLoc;
	}
	/**
	 * @param pageLoc the pageLoc to set
	 */
	public synchronized void setPageLoc(String pageLoc) {
		PageLoc = pageLoc;
	}
	/**
	 * @return the diaryImage
	 */
	public synchronized Hashtable<Long, DiaryPicture> getDiaryImage() {
		return DiaryImage;
	}
	/**
	 * @param diaryImage the diaryImage to set
	 */
	public synchronized void setDiaryImage(Hashtable<Long, DiaryPicture> diaryImage) {
		DiaryImage = diaryImage;
	}
	/**
	 * @return the serialversionuid
	 */
	public static synchronized long getSerialversionuid() {
		return serialVersionUID;
	}
	/**
	 * @param diaryID the diaryID to set
	 */
	public synchronized void setDiaryID(long diaryID) {
		DiaryID = diaryID;
	}
	/**
	 * @return the mPath
	 */
	public synchronized ArrayList<HandWritePath> getmPath() {
		return mPath;
	}
	/**
	 * @param mPath the mPath to set
	 */
	public synchronized void setmPath(ArrayList<HandWritePath> mPath) {
		this.mPath = mPath;
	}
	/**
	 * @return the pageOrientation
	 */
	public synchronized int getPageOrientation() {
		return PageOrientation;
	}
	/**
	 * @param pageOrientation the pageOrientation to set
	 */
	public synchronized void setPageOrientation(int pageOrientation) {
		PageOrientation = pageOrientation;
	}
	/**
	 * @return byteImageHW the Hand Write byte[]
	 */
	public byte[] getByteImageHW() {
		return byteImageHW;
	}
	/**
	 * @param byteImageHW the Hand Write byte[] to set
	 */
	public void setByteImageHW(byte[] byteImageHW) {
		this.byteImageHW = byteImageHW;
	}

	/**
	 * @return byteImagePreviewPage the Hand Write byte[]
	 */
	public byte[] getByteImagePreviewPage() {
		return byteImagePreviewPage;
	}
	/**
	 * @param byteImagePreviewPage the Hand Write byte[] to set
	 */
	public void setByteImagePreviewPage(byte[] byteImagePreviewPage) {
		this.byteImagePreviewPage = byteImagePreviewPage;
	}
}
