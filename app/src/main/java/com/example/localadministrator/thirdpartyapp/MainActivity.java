package com.example.localadministrator.thirdpartyapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.example.localadministrator.runtime.*;
import android.content.Intent;
import android.widget.Toast;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.content.ComponentName;
import android.os.RemoteException;
import android.util.Log;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends ActionBarActivity {
    private final String TAG = "ThirdPartyApp";
    Button buttonGPS;
    Button buttonFaceRec;
    Button buttonHTTP;
    TextView resultText;
    TextView logText;
    innerDeviceAIDL innerDeviceAIDLInstant;
    private Boolean connectionEstablished = false;
    private HashMap<Integer,String>  resultHM;
    Context mContext;
    public static Class lock = MainActivity.class;
    ImageUtil imageUtil =null;
    List<String> filePath = null;

    ServiceConnection connection=new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG,"service connected, sending notification");
            innerDeviceAIDLInstant=innerDeviceAIDL.Stub.asInterface(service);
            connectionEstablished = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            innerDeviceAIDLInstant=null;
            connectionEstablished = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        buttonGPS = (Button) findViewById(R.id.buttonGPS);
        buttonFaceRec = (Button) findViewById(R.id.buttonFaceRec);
        buttonHTTP = (Button) findViewById(R.id.buttonHTTP);
        logText = (TextView) findViewById(R.id.textView);
        resultText = (TextView) findViewById(R.id.textView2);
        buttonHTTP.setOnClickListener(mGlobal_OnClickListener);
        buttonFaceRec.setOnClickListener(mGlobal_OnClickListener);
        buttonGPS.setOnClickListener(mGlobal_OnClickListener);
        resultHM = new HashMap();
        mContext = this.getBaseContext();
        imageUtil = new ImageUtil();

        filePath = imageUtil.getImagePath();
        Log.d(TAG,"the imagePath is :"+filePath.toString());
        Intent mIntent = new Intent();
        mIntent.setAction("com.example.localadministrator.runtime.innerDeviceService");
        Intent intent = new Intent(getExplicitIntent(mContext, mIntent));
        mContext.bindService(intent, connection, Context.BIND_AUTO_CREATE);
        registerReceiver(rqlResultReceiver, resultBroadcastFilter());
        Log.d(TAG, "Initializing connection");


    }

    /**
     *
     * @return
     */
    // two kinds of broadcast can be processed.
    private IntentFilter resultBroadcastFilter() {
        final IntentFilter intentFilter = new IntentFilter();
       /* intentFilter.addAction("RQLFeedback");
        intentFilter.addAction("RQLResult");*/
        intentFilter.addAction("result of task execution");
        return intentFilter;
    }

    // Handles various events fired by the Service.
    private  BroadcastReceiver rqlResultReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if(action.equals("result of task execution")){
                String result = intent.getExtras().getString("resultValue");
                System.out.println("the result is :"+result);
                logText.append("\n"+result);
            }
            /*if (action.equals("RQLResult")) {
                String result = intent.getStringExtra("data");
                int id = intent.getIntExtra("TaskID", -1);
                if(resultHM.containsKey(id)){
                    String RQLRequest = resultHM.get(id);
                    String ShowResult = "\nRQLResult for "+RQLRequest+":"+result;
                    Log.d(TAG,ShowResult);
                    logText.append(ShowResult);
                }
            } else if (action.equals("RQLFeedback")) {
                String feedback = intent.getStringExtra("data");
                int id = intent.getIntExtra("TaskID", -1);
                if(resultHM.containsKey(id)){
                    String RQLRequest = resultHM.get(id);
                    String ShowFeedBack = "\nRQL feedback for "+RQLRequest+":"+feedback;
                    Log.d(TAG,ShowFeedBack);
                    logText.append(ShowFeedBack);
                }
            }*/
        }
    };
    // for sending intents in android 5.0
    public static Intent getExplicitIntent(Context context, Intent implicitIntent) {
        // Retrieve all services that can match the given intent
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> resolveInfo = pm.queryIntentServices(implicitIntent, 0);
        // Make sure only one match was found
        if (resolveInfo == null || resolveInfo.size() != 1) {
            return null;
        }
        // Get component info and create ComponentName
        ResolveInfo serviceInfo = resolveInfo.get(0);
        String packageName = serviceInfo.serviceInfo.packageName;
        String className = serviceInfo.serviceInfo.name;
        ComponentName component = new ComponentName(packageName, className);
        // Create a new intent. Use the old one for extras and such reuse
        Intent explicitIntent = new Intent(implicitIntent);
        // Set the component to be explicit
        explicitIntent.setComponent(component);
        return explicitIntent;


    }

    // send RQL requests
    final View.OnClickListener mGlobal_OnClickListener = new View.OnClickListener() {
        int tempResultId = 0;
        String RQL = "";

        public void onClick(final View v) {
            if (connectionEstablished != false) {
                Log.d(TAG,"AIDL connection established!");
                switch(v.getId()) {
                    case R.id.buttonGPS:
                        //send the RQL request...for GPS
                        Toast.makeText(v.getContext(), "ButtonGPS clicked.", Toast.LENGTH_SHORT).show();
                        RQL = "pull GPS:sensor/gps ";
                        break;
                    case R.id.buttonFaceRec:
                        //Inform the user the button2 has been clicked
                        Toast.makeText(v.getContext(), "ButtonFACEREC clicked.", Toast.LENGTH_SHORT).show();
                        RQL = "push any:image/facedetection";
                        System.out.println("the rql is:"+RQL);
                        break;
                    case R.id.buttonHTTP:
                        //Inform the user the button2 has been clicked
                        Toast.makeText(v.getContext(), "ButtonHTTP clicked.", Toast.LENGTH_SHORT).show();
                        RQL="execute any:service/http/www.google.com/test1.html|pull file/test1.html";
                        break;
                }
                try {
                    System.out.println("no return");
                    tempResultId = innerDeviceAIDLInstant.sendRQL(RQL);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                resultHM.put(tempResultId,RQL);
                logText.append("\n"+RQL+"_"+Integer.toString(tempResultId));
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
