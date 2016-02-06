package com.glm.utilities.com.glm.utilities.export;

import com.glm.bean.Diary;

import nl.siegmann.epublib.domain.Author;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Date;

/**
 * Created by gianluca on 08/08/13.
 * si occupa della creazione di un eBook a partire da un diario
 *
 */
public class EbookHelper {
    private Book _eBook;
    private Diary mDiary;
    /**
     * converte un diario in un eBook
     *
     * */
    public boolean convertToeBook(Diary diaryToConvert){
        mDiary=diaryToConvert;
        _eBook=new Book();
        _eBook.getMetadata().addTitle(mDiary.getDiaryName());
        //TODO AGGIUNGERE l'autore del diario
        _eBook.getMetadata().addAuthor(new Author(mDiary.getDiaryAuthor()));
        //Data di creazione
        _eBook.getMetadata().addDate(new Date(mDiary.getDiaryDTCreation()));
        //

        //_eBook.addSection(mDiary.getDiaryName(),new Resource())
        return true;
    }
}
