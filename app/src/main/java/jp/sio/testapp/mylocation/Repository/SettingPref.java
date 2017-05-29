package jp.sio.testapp.mylocation.Repository;

import android.content.Context;
import android.content.SharedPreferences;

import jp.sio.testapp.mylocation.R;

/**
 * Created by NTT docomo on 2017/05/23.
 */

public class SettingPref {
    SharedPreferences settingPref;
    SharedPreferences.Editor editor;
    Context context;

    //SharedPreference名
    public final String PREFNAME = "MyLocationSetting";

    /**
     * SettingActivityのContextを取得する
     * @param context
     */
    public SettingPref(Context context){
        this.context = context;
    }
    public void createPref(){
        settingPref = context.getSharedPreferences(PREFNAME,Context.MODE_PRIVATE);
        editor = settingPref.edit();
    }

    public void setCount(int count){
    }

    public void setTestParam(String str){
        editor.putString(String.valueOf(R.string.settingTestParam),str);
        editor.apply();
        editor.commit();
    }
    public String getTestParam(){
        return settingPref.getString(String.valueOf(String.valueOf(R.string.settingTestParam)),"default");
    }
    public String testMethod(String str){
        createPref();
        editor.putString(String.valueOf(R.string.settingTestParam),str);
        return settingPref.getString(String.valueOf(R.string.settingTestParam),"default");
    }
}