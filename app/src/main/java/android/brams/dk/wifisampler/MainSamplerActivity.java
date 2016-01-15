package android.brams.dk.wifisampler;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainSamplerActivity extends AppCompatActivity {
    private static final String TAG = "MainSamplerActivity";
    private static final int REQUEST_FINE_LOCATION=0;
    private static boolean allClear=false;

    ListView lv;
    ImageView iv;
    WifiManager wifi;
    String wifis[], infos[];
    WifiScanReceiver wifiReciever;

    private Toolbar mToolbar;
    private Button btnSimpleSnackbar, btnActionCallback, btnCustomView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_sampler);

        lv=(ListView)findViewById(R.id.listView);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                toast(infos[position]);
            }

        });

        if (ContextCompat.checkSelfPermission(MainSamplerActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            Log.i(TAG, "onCreate: We do not have the permission");
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainSamplerActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                Log.i(TAG, "onCreate: we should ask...");

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response!

                // After the user sees the explanation, try again to request the permission.
                CoordinatorLayout coordinatorLayout;
                coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
                Snackbar snackbar = Snackbar
                        .make( coordinatorLayout , R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                        .setAction(android.R.string.ok, new View.OnClickListener() {
                            @Override
                            @TargetApi(Build.VERSION_CODES.M)
                            public void onClick(View v) {
                                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_FINE_LOCATION);
                            }
                        });
                snackbar.show();

            } else {
                Log.i(TAG, "onCreate: No need to ask, we can just request");

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(MainSamplerActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_FINE_LOCATION);

                // REQUEST_FINE_LOCATON is an app-defined int constant.
                // Use it in the callback method that gets the result of the request.
            }
        } else {
            startWifiScanner();
        }

        Log.i(TAG, "onCreate: after permission check");

        iv=(ImageView)findViewById(R.id.logo_image);
        iv.setOnClickListener(new ImageView.OnClickListener() {

            @Override
            public void onClick(View v) {
                toast("Scanning...");
                lv.setAdapter(new ArrayAdapter<>(getApplicationContext(), R.layout.listitem, new String[0]));
                wifi.startScan();


            }
        });


    }

    protected void onPause() {
        if (allClear)
            unregisterReceiver(wifiReciever);

        super.onPause();
    }

    protected void onResume() {
        registerReceiver(wifiReciever, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        super.onResume();
    }


    private class WifiScanReceiver extends BroadcastReceiver {
        public void onReceive(Context c, Intent intent) {
            List<ScanResult> wifiScanList = wifi.getScanResults();
            wifis = new String[wifiScanList.size()];
            infos = new String[wifiScanList.size()];

            for(int i = 0; i < wifiScanList.size(); i++){
                ScanResult scanResult=wifiScanList.get(i);
                wifis[i] = scanResult.SSID+" "+scanResult.level+"dB";
                infos[i]=scanResult.toString();
                Log.d(TAG, "Added to list: " + wifis[i]);
            }
            lv.setAdapter(new ArrayAdapter<>(getApplicationContext(), R.layout.listitem, wifis));
        }
    }

    protected void toast( String text )
    {
        Toast.makeText(MainSamplerActivity.this,
                String.format("%s", text), Toast.LENGTH_LONG)
                .show();
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_FINE_LOCATION: {
                Log.i(TAG, "onRequestPermissionsResult: case statement begin");

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Yeah - permission granted, we can sample away

                    Log.i(TAG, "onRequestPermissionsResult: all clear set");

                    startWifiScanner();

                } else {
                    Toast.makeText(MainSamplerActivity.this, "Not permitted to scan WiFi", Toast.LENGTH_SHORT).show();
                }
                return;

            // other 'case' lines to check for other permissions this app might request
            }
        }
    }

    private void startWifiScanner() {
        allClear=true;
        wifi=(WifiManager)getSystemService(Context.WIFI_SERVICE);
        wifiReciever = new WifiScanReceiver();
        wifi.startScan();
    }
}
