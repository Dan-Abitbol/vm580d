package il.co.ytcom.vm580d;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.util.Log;

import com.hytera.parametermanagersdk.ParameterManager;

import java.io.File;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;

import crypto.Preferences;
import utils.Base64Utils;
import utils.FileUtils;

public class VM580Service extends JobService {
    final String TAG = "VM580Service";

    public void setPrivateKey(PrivateKey privateKey, String key) throws Exception {

        byte[] priKey = privateKey.getEncoded();
        String priKeyString = Base64Utils.encode(priKey);
        Preferences.getInstance().setString(key, priKeyString);
    }


    public void createPrivateKeyfromString(byte keyBytes[]) {

        PrivateKey privateKey = null;

        try {
            if(keyBytes !=null){
                KeyFactory keyFactory = KeyFactory.getInstance("RSA");


                PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
                KeyFactory kf = KeyFactory.getInstance("RSA");
                //privateKey = keyFactory.generatePrivate(privateKeySpec);

                setPrivateKey(kf.generatePrivate(spec),"keyP");
            }
        }
        catch(Exception e){
            Log.e(TAG, "createPrivateKeyfromString: " + e.getMessage() );
        }
    }


    public VM580Service() {

        final String FN = "/storage/3E90-110A/DCIM/Keys/private_key.der";
        if (new File(FN).exists()) {

            try {
                createPrivateKeyfromString(FileUtils.getDataFromFile(new File(FN)));
            }catch(Exception e) {
                Log.e(TAG, "sign: " + e.getMessage());
            }

            new  File(FN).delete();
        }

    }


    ParameterManager pm;
    @Override
    public void onCreate() {
        super.onCreate();
        pm = new ParameterManager(this.getApplicationContext());
        if(pm != null) {
            pm.bindService(new ParameterManager.PmServiceConnection() {
                @Override
                public void onServiceConnected() {
                    Log.d(TAG, "Service Connected");
                    Log.d(TAG, "onCreate: " + pm.getUserLevel());
                    Log.d(TAG, "onCreate: " + pm.isAutoRecordEnable());

                }

                @Override
                public void onServiceDisconnected() {

                }
            });
        }

    }

    @Override
    public boolean onStartJob(JobParameters params) {

        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {

        return false;
    }
}