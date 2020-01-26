package dte.masteriot.asp.aquaponics;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.ArrayList;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class FishActivity extends AppCompatActivity {

    ImageView imgStatus;
    ImageView alarmStatus;
    TextView textView;
    Button bCommand1;
    Button bCommand2;
    private ListView lvalarmList;
    private ListView lvreportList;
    private ListView lvcriticalList;
    private ArrayList<NOTIFICATION> alarmList = new ArrayList<NOTIFICATION>();
    private ArrayList<NOTIFICATION> reportList = new ArrayList<NOTIFICATION>();
    private ArrayList<NOTIFICATION> criticalList = new ArrayList<NOTIFICATION>();
    NotificationArrayAdapter notificationArrayAdapterA;
    NotificationArrayAdapter notificationArrayAdapterR;
    NotificationArrayAdapter notificationArrayAdapterC;


    MqttAndroidClient mqttAndroidClient;
    boolean connected = false;
    boolean subbed = false;

    final String serverUri = "tcp://srv-iot.diatel.upm.es:1883";
    String clientId = "AZzXWLsbsUlkFBg5ncoK";
    final String publishTopic = "v1/devices/me/telemetry";
    final String subscriptionTopic = "v1/devices/me/rpc/request/+";
    private int pos = -1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fish);

        getSupportActionBar().hide();

        textView = (TextView) findViewById(R.id.tv);
        imgStatus = (ImageView) findViewById(R.id.imgStatus);
        alarmStatus = (ImageView) findViewById(R.id.alarmStatus);
        lvalarmList = (ListView)findViewById(R.id.lvalarm);
        lvreportList = (ListView)findViewById(R.id.lvreports);
        lvcriticalList = (ListView)findViewById(R.id.lvcritical);

        notificationArrayAdapterA = new NotificationArrayAdapter( this, alarmList );
        lvalarmList.setAdapter(notificationArrayAdapterA);
        lvalarmList.setChoiceMode( ListView.CHOICE_MODE_SINGLE );
        lvalarmList.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                pos = lvalarmList.getCheckedItemPosition();
                SweetAlertDialog sd = new SweetAlertDialog(FishActivity.this);
                sd.setCancelable(true);
                sd.setTitle(alarmList.get(pos).params.type);
                sd.setContentText(alarmList.get(pos).params.text + "\n\n\n");
                sd.setCanceledOnTouchOutside(true);
                sd.show();
            }
        });

        notificationArrayAdapterR = new NotificationArrayAdapter( this, reportList );
        lvreportList.setAdapter(notificationArrayAdapterR);
        lvreportList.setChoiceMode( ListView.CHOICE_MODE_SINGLE );
        lvreportList.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                pos = lvreportList.getCheckedItemPosition();
                SweetAlertDialog sd = new SweetAlertDialog(FishActivity.this);
                sd.setCancelable(true);
                sd.setTitle(reportList.get(pos).params.type);
                sd.setContentText(reportList.get(pos).params.text + "\n\n\n");
                sd.setCanceledOnTouchOutside(true);
                sd.show();
            }
        });

        notificationArrayAdapterC = new NotificationArrayAdapter( this, criticalList );
        lvcriticalList.setAdapter(notificationArrayAdapterC);
        lvcriticalList.setChoiceMode( ListView.CHOICE_MODE_SINGLE );
        lvcriticalList.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                pos = lvcriticalList.getCheckedItemPosition();
                SweetAlertDialog sd = new SweetAlertDialog(FishActivity.this);
                sd.setCancelable(true);
                sd.setTitle(criticalList.get(pos).params.type);
                sd.setContentText(criticalList.get(pos).params.text + "\n\n\n");
                sd.setCanceledOnTouchOutside(true);
                sd.show();
            }
        });

        mqttConnection();
    }

    private void mqttConnection(){
        clientId = clientId + System.currentTimeMillis();

        mqttAndroidClient = new MqttAndroidClient(getApplicationContext(), serverUri, clientId);
        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {

                if (reconnect) {
                    addToHistory("Reconnected to : " + serverURI);
                    // Because Clean Session is true, we need to re-subscribe
                    subscribeToTopic();
                } else {
                    addToHistory("Connected to: " + serverURI);
                }
                imgStatus.setImageResource(R.drawable.fish_online);
                connected = true;
            }

            @Override
            public void connectionLost(Throwable cause) {
                addToHistory("The Connection was lost.");
                imgStatus.setImageResource(R.drawable.fish_offline);
                connected = false;
                subbed = false;
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {

                Gson gson = new Gson();
                NOTIFICATION notification = null;
                notification = gson.fromJson(new String(message.getPayload()), NOTIFICATION.class);
                addToHistory(notification.method);
                if (notification.method.equals("newNotification")){
                    if(notification.params.originator.equals("Fish")) {
                        if(!notification.params.type.equals("")) {
                            alarmList.add(notification);
                            notificationArrayAdapterA.notifyDataSetChanged();
                        }
                    }
                } else if (notification.method.equals("newReport")) {
                    if (notification.params.originator.equals("Fish")) {
                        reportList.add(notification);
                        notificationArrayAdapterR.notifyDataSetChanged();
                    }
                } else if (notification.method.equals("newCriticalAlarm")) {
                    if (notification.params.originator.equals("Fish")) {
                        if (notification.params.type.equals("WATER LEAK DETECTED")) {
                            if(criticalList.isEmpty()) {
                                criticalList.add(notification);
                                notificationArrayAdapterC.notifyDataSetChanged();
                                alarmStatus.setImageResource(R.drawable.alarm);
                            }
                        } else {
                            criticalList.clear();
                            notificationArrayAdapterC.notifyDataSetChanged();
                            alarmStatus.setImageResource(R.drawable.noalarm);
                        }

                    }
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
            }
        });

        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        // mqttConnectOptions.setCleanSession(false);
        mqttConnectOptions.setCleanSession(true);

        mqttConnectOptions.setUserName( "AZzXWLsbsUlkFBg5ncoK" );
        mqttConnectOptions.setPassword( "<MQTT KEY>".toCharArray() );

        try {
            //addToHistory("Connecting to " + serverUri);
            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                    disconnectedBufferOptions.setBufferEnabled(true);
                    disconnectedBufferOptions.setBufferSize(100);
                    disconnectedBufferOptions.setPersistBuffer(false);
                    disconnectedBufferOptions.setDeleteOldestMessages(false);
                    mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);
                    subscribeToTopic();
                    connected = true;
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    addToHistory("Failed to connect to: " + serverUri);
                    connected = false;
                }
            });


        } catch (MqttException ex){
            ex.printStackTrace();
        }
    }

    private void addToHistory(String mainText){
        System.out.println("LOG: " + mainText);
        Snackbar.make(findViewById(android.R.id.content), mainText, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    public void subscribeToTopic(){
        try {
            mqttAndroidClient.subscribe(subscriptionTopic, 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    addToHistory("Subscribed!");
                    subbed = true;
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    addToHistory("Failed to subscribe");
                    subbed = false;
                }
            });

        } catch (MqttException ex){
            System.err.println("Exception whilst subscribing");
            ex.printStackTrace();
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        try {
            mqttAndroidClient.unregisterResources();
            mqttAndroidClient.close();
            IMqttToken disconToken = mqttAndroidClient.disconnect();
            mqttAndroidClient.setCallback(null);
            disconToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // we are now successfully disconnected
                    addToHistory("Succesfully disconnected");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken,
                                      Throwable exception) {
                    // something went wrong, but probably we are disconnected anyway
                }
            });
            mqttAndroidClient = null;
            Thread.sleep(200);
            subbed = false;

        } catch (MqttException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
