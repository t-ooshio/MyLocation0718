package jp.sio.testapp.mylocation.Repository;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;

import jp.sio.testapp.mylocation.L;

/**
 * Logファイルに関するクラス
 * Created by NTT docomo on 2017/05/22.
 */

public class LocationLog {
    private long createLogTime;
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");

    private File file;
    private String fileName;
    private String filePath;
    private BufferedWriter writer;

    //ファイルインデックス強制作成用
    private MediaScannerConnection scanner;
    private MediaScannerConnection.MediaScannerConnectionClient scannerConnectionClient;
    private Context context;

    public LocationLog(Context context){
        this.context = context;
    }

    /**
     * Logファイルを作成
     */
    public void makeLogFile(String settingHeader){
        if(isExternalStrageWriteable()){
            createLogTime = System.currentTimeMillis();
            fileName = simpleDateFormat.format(createLogTime) + ".txt";
            filePath = Environment.getExternalStorageDirectory().getPath() + "/"+ "MyLocation/" + fileName;
            L.d("LogFilePath:" + filePath);

            file = new File(filePath);
            file.getParentFile().mkdir();
        }else{
            L.d("ExternalStrage書き込み不可");
        }
        try{
            writer = new BufferedWriter(new FileWriter(file,true));
            L.d("settingHeader:" + settingHeader);
            writer.write(settingHeader);
            writer.newLine();
        } catch (FileNotFoundException e) {
            L.d(e.getMessage());
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            L.d("UTF-8使えない");
            e.printStackTrace();
        } catch (IOException e) {
            L.d("write失敗");
            e.printStackTrace();
        }
    }

    /**
     * Logファイルへの書き込み
     */
    public void writeLog(String log){
        try {
            L.d("Log" + log);
            writer.write(log);
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * Logファイルを閉じる(Readerとかを閉じる処理を想定)
     *
     */
    public void endLogFile(){
        try {
            scanFile();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * ログファイルを端末再起動無しでも読み込むための処理
     * ファイルインデックスを作成しなおせば良いと見たのでそれを実装
     */
    public void scanFile(){
        MediaScannerConnection.OnScanCompletedListener listener = new MediaScannerConnection.OnScanCompletedListener() {
            @Override
            public void onScanCompleted(String path, Uri uri) {

            }
        };
        MediaScannerConnection.scanFile(context, new String[]{filePath}, new String[]{"txt"},listener);
    }


    //externalStrageのReadとWriteが可能かチェック
    private boolean isExternalStrageWriteable(){
        boolean result = false;
        String state = Environment.getExternalStorageState();
        if(Environment.MEDIA_MOUNTED.equals(state)){
            result = true;
        }
        return result;
    }
}