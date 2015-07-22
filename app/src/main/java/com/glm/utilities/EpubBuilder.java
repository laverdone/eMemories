package com.glm.utilities;

import android.content.Context;

import com.glm.bean.Diary;

import java.io.IOException;

import nl.siegmann.epublib.domain.Author;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;

/**
 * Created by gianluca on 08/11/13.
 */
public class EpubBuilder {
    private Book eBookBuilded;
    private Diary mDiaryToBuild;
    private Context mContext;
    public EpubBuilder(Diary diaryToBuild){
        mDiaryToBuild=diaryToBuild;
        eBookBuilded = new Book();
    }

    public boolean eBookBuilder(){
        eBookBuilded.getMetadata().addAuthor(new Author(mDiaryToBuild.getDiaryAuthor(),""));
        eBookBuilded.getMetadata().addTitle(mDiaryToBuild.getDiaryName());
        try {

            eBookBuilded.setCoverImage(new Resource(mContext.getAssets().open("template/1/cover.png"), "cover.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }
}
