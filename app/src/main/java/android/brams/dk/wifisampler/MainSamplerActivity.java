package android.brams.dk.wifisampler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;

public class MainSamplerActivity extends AppCompatActivity {
    private static final String TAG = "MainSamplerActivity";

    ListView lv;
    ImageView iv;
    WifiManager wifi;
    String wifis[], infos[];
    WifiScanReceiver wifiReciever;

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

        wifi=(WifiManager)getSystemService(Context.WIFI_SERVICE);
        wifiReciever = new WifiScanReceiver();
        wifi.startScan();

        iv=(ImageView)findViewById(R.id.logo_image);
        iv.setOnClickListener(new ImageView.OnClickListener(){

            @Override
            public void onClick(View v) {
                toast("Scanning...");
                lv.setAdapter(new ArrayAdapter<String>(getApplicationContext(), R.layout.listitem, new String[0]));
                wifi.startScan();


            }
        });
    }

    protected void onPause() {
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
            lv.setAdapter(new ArrayAdapter<String>(getApplicationContext(), R.layout.listitem, wifis));
        }
    }

    protected void toast( String text )
    {
        Toast.makeText(MainSamplerActivity.this,
                String.format("%s", text), Toast.LENGTH_LONG)
                .show();
    }
}
