package com.glm.bean;

import java.io.Serializable;
import java.util.Date;
import java.util.Hashtable;

public class Diary implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**identifica l'ID del diario*/
	private long DiaryID;

    /**identifica l'ID del diario nel cloud*/
    private String CloudDiaryID;
	/**Data di creazione*/
	private Date DiaryDTCreation;
	/**Data di modifica*/
	private Date DiaryDTModify;
	
	private int DiaryTemplate;
	/**Nome del diario*/
	private String DiaryName;
    /**Autore del diaro*/
    private String DiaryAuthor;



    public String getDiaryAuthor() {
        return DiaryAuthor;
    }

    public void setDiaryAuthor(String diaryAuthor) {
        DiaryAuthor = diaryAuthor;
    }

	private Hashtable<Long, Page> DiaryPages;

	/**
	 * @return the diaryID
	 */
	public synchronized long getDiaryID() {
		return DiaryID;
	}

	/**
	 * @param diaryID the diaryID to set
	 */
	public synchronized void setDiaryID(long diaryID) {
		DiaryID = diaryID;
	}

	/**
	 * @return the diaryDTCreation
	 */
	public synchronized Date getDiaryDTCreation() {
		return DiaryDTCreation;
	}

	/**
	 * @param diaryDTCreation the diaryDTCreation to set
	 */
	public synchronized void setDiaryDTCreation(Date diaryDTCreation) {
		DiaryDTCreation = diaryDTCreation;
	}

	/**
	 * @return the diaryTemplate
	 */
	public synchronized int getDiaryTemplate() {
		return DiaryTemplate;
	}

	/**
	 * @param diaryTemplate the diaryTemplate to set
	 */
	public synchronized void setDiaryTemplate(int diaryTemplate) {
		DiaryTemplate = diaryTemplate;
	}

	/**
	 * @return the diaryPages
	 */
	public synchronized Hashtable<Long, Page> getDiaryPages() {
		return DiaryPages;
	}

	/**
	 * @param diaryPages the diaryPages to set
	 */
	public synchronized void setDiaryPages(Hashtable<Long,Page> diaryPages) {
		DiaryPages = diaryPages;
	}

	/**
	 * @return the serialversionuid
	 */
	public static synchronized long getSerialversionuid() {
		return serialVersionUID;
	}

	/**
	 * @return the diaryName
	 */
	public synchronized String getDiaryName() {
		return DiaryName;
	}

	/**
	 * @param diaryName the diaryName to set
	 */
	public synchronized void setDiaryName(String diaryName) {
		DiaryName = diaryName;
	}

	/**
	 * @return the diaryDTModify
	 */
	public synchronized Date getDiaryDTModify() {
		return DiaryDTModify;
	}

	/**
	 * @param diaryDTModify the diaryDTModify to set
	 */
	public synchronized void setDiaryDTModify(Date diaryDTModify) {
		DiaryDTModify = diaryDTModify;
	}

    public String getCloudDiaryID() {
        return CloudDiaryID;
    }

    public void setCloudDiaryID(String cloudDiaryID) {
        CloudDiaryID = cloudDiaryID;
    }
}