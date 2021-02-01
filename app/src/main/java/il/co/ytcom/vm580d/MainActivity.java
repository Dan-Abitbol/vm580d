package il.co.ytcom.vm580d;

import android.Manifest;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;

    public static String getExternalSDCardPath(Context context) {
        List<StorageVolume> storageVolumes = ((StorageManager) context.getSystemService(Context.STORAGE_SERVICE)).getStorageVolumes();
        try {
            Class<?> cls = Class.forName("android.os.storage.StorageVolume");
            Method declaredMethod = cls.getDeclaredMethod("getPath", new Class[0]);
            Method declaredMethod2 = cls.getDeclaredMethod("isRemovable", new Class[0]);
            declaredMethod.setAccessible(true);
            declaredMethod2.setAccessible(true);
            for (int i = 0; i < storageVolumes.size(); i++) {
                StorageVolume storageVolume = storageVolumes.get(i);
                String str = (String) declaredMethod.invoke(storageVolume, new Object[0]);
                Boolean bool = (Boolean) declaredMethod2.invoke(storageVolume, new Object[0]);
                Log.d("cary", "path is " + str + "  isRemove is " + bool);
                if (bool.booleanValue()) {
                    return str;
                }
            }
            return "";
        } catch (ClassNotFoundException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
            return "";
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });


        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (shouldShowRequestPermissionRationale(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                // Explain to the user why we need to read the contacts
            }

            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE },
                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);

            // MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE is an
            // app-defined int constant that should be quite unique

            return;
        }


//        File yourFile = new File("/storage/3E90-110A/DCIM/Video/20180104_204217"+"_HASH.sig");
        File default_folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        try {
            Log.d("ddd", "onCreate: " + default_folder.getCanonicalPath());
            Log.d("ddd", "getExternalSDCardPath() CanRead(: " + new File(getExternalSDCardPath(this)).canRead() + ") CanWrite("+ new File(getExternalSDCardPath(this)).canWrite()+")");
        } catch (IOException e) {
            e.printStackTrace();
        }

        File yourFile = new File(new File(getExternalSDCardPath(this) + File.separator+ "/DCIM/Video/"),"20180104_204217"+"_HASH.sig");
        try {
            yourFile.createNewFile(); // if file already exists will do nothing
        } catch (IOException e) {
            e.printStackTrace();
        }


        RunJobAndFinish();
    }

    private void RunJobAndFinish() {
        JobScheduler jobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);

        JobInfo jobInfo = new JobInfo.Builder(11, new ComponentName(this, VM580Service.class))
                // only add if network access is required
//                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setOverrideDeadline(0)
                .setPersisted(true)
                .build();

        jobScheduler.schedule(jobInfo);

        finish();
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions, @NonNull final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted.
                RunJobAndFinish();
            } else {
                // User refused to grant permission.
            }
        }
    }
}