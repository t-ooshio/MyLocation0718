package jp.sio.testapp.mylocation.Presenter;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;

import java.util.logging.Handler;

import jp.sio.testapp.mylocation.Activity.MyLocationActivity;
import jp.sio.testapp.mylocation.Activity.SettingActivity;
import jp.sio.testapp.mylocation.L;
import jp.sio.testapp.mylocation.R;
import jp.sio.testapp.mylocation.Service.UebService;
import jp.sio.testapp.mylocation.Usecase.MyLocationUsecase;
import jp.sio.testapp.mylocation.Usecase.SettingUsecase ;


/**
 * Created by NTT docomo on 2017/05/23.
 * ActivityとServiceの橋渡し
 * Activityはなるべく描画だけに専念させたいから分けるため
 */

public class MyLocationPresenter {
    private MyLocationActivity activity;
    SettingUsecase settingUsecase;
    MyLocationUsecase myLocationUsecase;
    Intent settingIntent;
    Intent locationserviceIntent;

    private String receiveCategory;
    private String categoryLocation;
    private String categoryColdStart;
    private String categoryColdStop;
    private String categoryServiceStop;

    private UebService uebService;

    private String locationType;
    private int count;
    private long timeout;
    private long interval;
    private boolean isCold;
    private int suplendwaittime;
    private int delassisttime;

    private ServiceConnection serviceConnectionUeb = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            uebService = ((UebService.UebService_Binder)service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            uebService = null;
        }
    };

    private final LocationReceiver locationReceiver = new LocationReceiver();

    public MyLocationPresenter(MyLocationActivity activity){
        this.activity = activity;
        myLocationUsecase = new MyLocationUsecase(activity);
        settingUsecase = new SettingUsecase(activity);

        categoryLocation = activity.getResources().getString(R.string.categoryLocation);
        categoryColdStart = activity.getResources().getString(R.string.categoryColdStart);
        categoryColdStop = activity.getResources().getString(R.string.categoryColdStop);
        categoryServiceStop = activity.getResources().getString(R.string.categoryServiceEnd);
    }

    public void checkPermission(){
        myLocationUsecase.hasPermissions();
    }

    public void mStart(){
        locationType = settingUsecase.getLocationType();
        count = settingUsecase.getCount();
        timeout = settingUsecase.getTimeout();
        interval = settingUsecase.getInterval();
        isCold = settingUsecase.getIsCold();
        suplendwaittime = settingUsecase.getSuplEndWaitTime();
        delassisttime = settingUsecase.getDelAssistDataTime();

        activity.showTextViewSetting("測位方式:" + locationType + "\n" + "測位回数:" + count + "\n" + "タイムアウト:" + timeout + "\n" +
                "測位間隔:" + interval + "\n" + "Cold:" + isCold + "\n"
                + "suplEndWaitTime:" + suplendwaittime + "\n" + "アシストデータ削除時間:" + delassisttime + "\n");
        activity.showTextViewState(activity.getResources().getString(R.string.locationStop));
    }

    public void locationStart(){

        locationserviceIntent = new Intent(activity.getApplicationContext(),UebService.class);

        locationserviceIntent.putExtra(activity.getResources().getString(R.string.settingCount),count);
        locationserviceIntent.putExtra(activity.getResources().getString(R.string.settingTimeout),timeout);
        locationserviceIntent.putExtra(activity.getResources().getString(R.string.settingInterval),interval);
        locationserviceIntent.putExtra(activity.getResources().getString(R.string.settingIsCold),isCold);
        locationserviceIntent.putExtra(activity.getResources().getString(R.string.settingSuplEndWaitTime),suplendwaittime);
        locationserviceIntent.putExtra(activity.getResources().getString(R.string.settingDelAssistdataTime),delassisttime);
        activity.startService(locationserviceIntent);
        IntentFilter filter = new IntentFilter(activity.getResources().getString(R.string.locationUeb));
        activity.registerReceiver(locationReceiver,filter);
        activity.bindService(locationserviceIntent,serviceConnectionUeb ,Context.BIND_AUTO_CREATE);
    }

    public void locationStop(){
        activity.unbindService(serviceConnectionUeb);
        activity.stopService(locationserviceIntent);
    }

    public void settingStart(){
        settingIntent = new Intent(activity.getApplicationContext(), SettingActivity.class);
        activity.startActivity(settingIntent);
    }

    public void showToast(String message){
        activity.showToast(message);
    }

    public class LocationReceiver extends BroadcastReceiver{
        Boolean isFix;
        double lattude, longitude, ttff;
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            receiveCategory = bundle.getString(activity.getResources().getString(R.string.category));

            if(receiveCategory.equals(categoryLocation)){
                isFix = bundle.getBoolean(activity.getResources().getString(R.string.TagisFix));
                lattude = bundle.getDouble(activity.getResources().getString(R.string.TagLat));
                longitude = bundle.getDouble(activity.getResources().getString(R.string.TagLong));
                ttff = bundle.getDouble(activity.getResources().getString(R.string.Tagttff));
                L.d("onReceive");
                L.d(isFix + "," + lattude + "," + longitude + "," + ttff );
                activity.showTextViewResult("測位成否："+ isFix + "\n" + "緯度:" + lattude + "\n" + "軽度：" + longitude + "\n" + "TTFF：" + ttff);
                activity.showTextViewState(activity.getResources().getString(R.string.locationWait));
            }else if(receiveCategory.equals(categoryColdStart)){
                L.d("ReceiceColdStart");
                activity.showTextViewState(activity.getResources().getString(R.string.locationPositioning));
                showToast("アシストデータ削除中");
            }else if(receiveCategory.equals(categoryColdStop)){
                L.d("ReceiceColdStop");
                showToast("アシストデータ削除終了");
            }else if(receiveCategory.equals(categoryServiceStop)){
                L.d("ServiceStop");
                activity.showTextViewState(activity.getResources().getString(R.string.locationStop));
                showToast("測位サービス終了");
                activity.onBtnStart();
                activity.offBtnStop();
                activity.onBtnSetting();
            }
        }
    }
}