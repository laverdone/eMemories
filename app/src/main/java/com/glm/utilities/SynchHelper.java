/*
package com.glm.utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.baasbox.android.BaasBox;
import com.baasbox.android.BaasClientException;
import com.baasbox.android.BaasDocument;
import com.baasbox.android.BaasException;
import com.baasbox.android.BaasIOException;
import com.baasbox.android.BaasResult;
import com.baasbox.android.BaasUser;
import com.baasbox.android.json.JsonObject;
import com.glm.bean.Diary;
import com.glm.db.DiaryRepositoryHelper;
import com.glm.db.Repository;
import com.glm.ememories.Const;
import com.google.gson.Gson;


import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

*/
/**
 * Created by gianluca on 23/06/14.
 *//*

public class SynchHelper {

    private BaasBox mSynchClient;
    private Context mContext;
    private BaasResult<BaasUser> mUser=null;
    private Diary mDiaryToSynch;
    private SharedPreferences mPrefs;
    private SharedPreferences.Editor mEditor;
    public SynchHelper(Context context){
        mContext=context;

        BaasBox.Builder b =  new BaasBox.Builder(mContext);
        b.setApiDomain("ememories.dnsd.me")
                .setAppCode(Const.CLOUD_APPCODE)
                .init();

        mPrefs = mContext.getSharedPreferences(Const.CLOUD_COLLECTION, Context.MODE_PRIVATE);
        mEditor= mPrefs.edit();
    }

    */
/**
     * Registra l'utente in baasBox
     * *//*

    public boolean userSignUp(String username, String password){

        BaasUser user = BaasUser.withUserName(username);
        user.setPassword(password);
        mUser=user.signupSync();

        if(mUser.isSuccess()){
            mEditor.putString("username", username);
            mEditor.putString("password", password);
            mEditor.putBoolean("isCloudEnabled", true);
            mEditor.commit();
            Log.d(SynchHelper.this.getClass().getCanonicalName(), "SignUp User OK");
            return true;
        }else{
            Log.e(SynchHelper.this.getClass().getCanonicalName(),"SignUp User KO");
            return false;
        }

    }

    */
/**
     * Login l'utente in baasBox
     * *//*

    public boolean userLogIn(String username, String password){

        BaasUser user = BaasUser.withUserName(username);
        user.setPassword(password);
        mUser=user.loginSync();

        if(mUser.isSuccess()){
            mEditor.putString("username", username);
            mEditor.putString("password", password);
            mEditor.putBoolean("isCloudEnabled", true);
            mEditor.commit();
            Log.d(SynchHelper.this.getClass().getCanonicalName(),"Login User OK");
            return true;
        }else{
            Log.e(SynchHelper.this.getClass().getCanonicalName(),"Login User KO");
            return false;
        }
    }

    */
/**
     * Logout l'utente in baasBox
     * *//*

    public boolean userLogOut(){
        if(BaasUser.current()!=null)
            return BaasUser.current().logoutSync().isSuccess();
        else
            return true;
    }

    */
/**
     * Sincronizza il diario con il Cloud
     *
     * *//*

    public Diary synchDiaryToCloud(Diary diary){
        mDiaryToSynch=diary;
        //Test load all diary durind save to cloud
        Repository mRepository = new Repository(mContext);
        //Il diario su Cloud non ha le immagini
        mDiaryToSynch= mRepository.reloadDiary(mDiaryToSynch);
        Gson gson = new Gson();
        StringBuffer jSonString = new StringBuffer(gson.toJson(mDiaryToSynch));
        BaasDocument doc=null;
        JsonObject jsonObject = new JsonObject();
        jsonObject.putString("diaryObject",jSonString.toString());

        if(diary.getCloudDiaryID()!=null){
            BaasResult<BaasDocument> result = BaasDocument.fetchSync(Const.CLOUD_COLLECTION,diary.getCloudDiaryID());
            try {
                doc=result.get();
                doc.putObject("diary",jsonObject);
            }catch (BaasIOException e1){
                //Diario in locale ma non nel cloud anche se CloidId è presente
                doc = new BaasDocument(Const.CLOUD_COLLECTION);
                mDiaryToSynch.setCloudDiaryID("");
                jsonObject.putString("diaryObject",gson.toJson(mDiaryToSynch));
                doc.putObject("diary",jsonObject);
            }catch (BaasClientException e) {
                if(e.getMessage().toString().contains("not found")){
                    doc = new BaasDocument(Const.CLOUD_COLLECTION);
                    mDiaryToSynch.setCloudDiaryID("");
                    jsonObject.putString("diaryObject",gson.toJson(mDiaryToSynch));
                    doc.putObject("diary",jsonObject);
                }
                Log.e(SynchHelper.this.getClass().getCanonicalName(),"BaasClientException Error retreving Document: "+diary.getCloudDiaryID());
            }catch (BaasException e2){
                Log.e(SynchHelper.this.getClass().getCanonicalName(),"BaasException Error retreving Document: "+diary.getCloudDiaryID());
            }
        }else{
            doc = new BaasDocument(Const.CLOUD_COLLECTION);
            doc.putObject("diary",jsonObject);
            //BaasDocument.fetchSync("eMemories",diary.getDiaryID());
            Log.d(SynchHelper.this.getClass().getCanonicalName(), "Save Documento to the Cloud");
        }
        if(doc!=null) {
            if(doc.saveSync().isSuccess()){
                Log.d(this.getClass().getCanonicalName(),"Dump document on the cloud");
                mDiaryToSynch.setCloudDiaryID(doc.getId());
            }else{
                Log.e(this.getClass().getCanonicalName(),"Dump document on the cloud ERROR");
            }

        }

        return mDiaryToSynch;
    }

    */
/**
     * Preleva l'elenco dei diari dal Cloud
     *
     * *//*

    public ArrayList<Diary> getCloudDiaries(ArrayList<Diary> diaries){

        Gson gson = new Gson();
        BaasResult<List<BaasDocument>> listCloudDoc;
        BaasDocument cloudDocument=null;
        Diary diaryFromCloud;
        int iLocalDiares=diaries.size();
        //Identifica se il diario e sia in locale che sul cloud
        boolean isLocalToCloud=false;
        listCloudDoc=BaasDocument.fetchAllSync(Const.CLOUD_COLLECTION);
        try {
            int iSize=listCloudDoc.get().size();
            if(listCloudDoc.isSuccess()){
                for (int i = 0; i < iSize; i++) {
                    cloudDocument=listCloudDoc.get().get(i);
                    isLocalToCloud=false;
                    Log.d(SynchHelper.this.getClass().getCanonicalName(),"Cloud Document is: "+cloudDocument.getId());
                    Hashtable tmpHashFromCloud = gson.fromJson(cloudDocument.values().getObject(0).encode(),Hashtable.class);
                    diaryFromCloud=gson.fromJson(tmpHashFromCloud.get("diaryObject").toString(),Diary.class);

                    //Ciclo tutti i Diari in locale
                    for(int j=0;j<iLocalDiares;j++){
                        if(cloudDocument.getId().equals(diaries.get(j).getCloudDiaryID())){
                            isLocalToCloud=true;
                            break;
                        }
                        //Controllo che il diario sul Cloud non sia uguale a quello in locale ma senza CloudID
                        if(diaryFromCloud.getDiaryID()==diaries.get(j).getDiaryID()){
                            DiaryRepositoryHelper.dumpDiary(mContext, diaryFromCloud);
                            isLocalToCloud=true;
                            break;
                        }
                    }
                    if(!isLocalToCloud){
                       Log.d(SynchHelper.this.getClass().getCanonicalName(),
                               "Doc From Cloud: " + cloudDocument.values().encode());
                       //Aggiungo il diari cloud alla lista dei diari
                       diaries.add(diaryFromCloud);
                       //Salvo nel DB
                       DiaryRepositoryHelper.dumpDiary(mContext, diaryFromCloud);
                    }
                }
            }else{
                Log.e(SynchHelper.this.getClass().getCanonicalName(),"Error fetch list of document from Cloud");
            }
        } catch (BaasException e) {
            e.printStackTrace();
        }
        return diaries;
    }

    */
/***
     *
     * Cancella un documento sul Cloud
     *
     * @param diaryToDelete
     * @return
     *//*

    public boolean deleteDocFromCloud(Diary diaryToDelete){


       BaasResult<Void> documentToDelete = BaasDocument.deleteSync(Const.CLOUD_COLLECTION,diaryToDelete.getCloudDiaryID());
       if(documentToDelete.isSuccess()){
           Log.d(this.getClass().getCanonicalName(),"Delete document from Cloud");
           return true;
       }else{
           Log.e(this.getClass().getCanonicalName(),"Error Delete document from Cloud:"+diaryToDelete.getCloudDiaryID());
           return false;
        }

    }
}
*/
