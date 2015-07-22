package com.glm.bean;

public class Row {

	private long RowID;
	
	private String RowText;
	
	private long PageID;

	private int RowNumber;
	
	private int RowPosX;
	
	private int RowPosY;
	/**
	 * @return the rowID
	 */
	public synchronized long getRowID() {
		return RowID;
	}

	/**
	 * @param rowID the rowID to set
	 */
	public synchronized void setRowID(long rowID) {
		RowID = rowID;
	}

	/**
	 * @return the rowText
	 */
	public synchronized String getRowText() {
		return RowText;
	}

	/**
	 * @param rowText the rowText to set
	 */
	public synchronized void setRowText(String rowText) {
		RowText = rowText;
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
	 * @return the rowNumber
	 */
	public synchronized int getRowNumber() {
		return RowNumber;
	}

	/**
	 * @param rowNumber the rowNumber to set
	 */
	public synchronized void setRowNumber(int rowNumber) {
		RowNumber = rowNumber;
	}

	/**
	 * @return the rowPosX
	 */
	public synchronized int getRowPosX() {
		return RowPosX;
	}

	/**
	 * @param rowPosX the rowPosX to set
	 */
	public synchronized void setRowPosX(int rowPosX) {
		RowPosX = rowPosX;
	}

	/**
	 * @return the rowPosY
	 */
	public synchronized int getRowPosY() {
		return RowPosY;
	}

	/**
	 * @param rowPosY the rowPosY to set
	 */
	public synchronized void setRowPosY(int rowPosY) {
		RowPosY = rowPosY;
	}
}
