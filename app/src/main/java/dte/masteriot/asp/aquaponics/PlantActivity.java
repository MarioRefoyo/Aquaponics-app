package dte.masteriot.asp.aquaponics;

import androidx.appcompat.app.AppCompatActivity;


import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import cn.pedant.SweetAlert.SweetAlertDialog;


import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;


public class PlantActivity extends AppCompatActivity implements View.OnClickListener{

    ImageView imgStatus;
    TextView textView;
    Button bCommand1;
    private ListView lvalarmList;
    private ListView lvreportList;
    private ArrayList<NOTIFICATION> alarmList = new ArrayList<NOTIFICATION>();
    private ArrayList<NOTIFICATION> reportList = new ArrayList<NOTIFICATION>();
    NotificationArrayAdapter notificationArrayAdapterA;
    NotificationArrayAdapter notificationArrayAdapterR;


    MqttAndroidClient mqttAndroidClient;
    boolean connected = false;
    boolean subbed = false;


    final String serverUri = "tcp://srv-iot.diatel.upm.es:1883";
    String clientId = "AZzXWLsbsUlkFBg5ncoK";
    final String publishTopic = "v1/devices/me/telemetry";
    final String subscriptionTopic = "v1/devices/me/rpc/request/+";

    private int pos = -1;
    static final int PICK_BUTTON = 1;  // The request code

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plant);

        getSupportActionBar().hide();

        textView = (TextView) findViewById(R.id.tv);
        imgStatus = (ImageView) findViewById(R.id.imgStatus);
        lvalarmList = (ListView)findViewById(R.id.lvalarm);
        lvreportList = (ListView)findViewById(R.id.lvreports);
        bCommand1 = (Button) findViewById(R.id.command1);
        bCommand1.setOnClickListener(this);


        notificationArrayAdapterA = new NotificationArrayAdapter( this, alarmList );
        lvalarmList.setAdapter(notificationArrayAdapterA);
        lvalarmList.setChoiceMode( ListView.CHOICE_MODE_SINGLE );
        lvalarmList.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                pos = lvalarmList.getCheckedItemPosition();
                SweetAlertDialog sd = new SweetAlertDialog(PlantActivity.this);
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
                SweetAlertDialog sd = new SweetAlertDialog(PlantActivity.this);
                sd.setCancelable(true);
                sd.setTitle(reportList.get(pos).params.type);
                sd.setContentText(reportList.get(pos).params.text + "\n\n\n");
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
                imgStatus.setImageResource(R.drawable.plant_online);
                connected = true;
            }

            @Override
            public void connectionLost(Throwable cause) {
                addToHistory("The Connection was lost.");
                imgStatus.setImageResource(R.drawable.plant_offline);
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
                    if(notification.params.originator.equals("Plant")) {
                        alarmList.add(notification);
                        notificationArrayAdapterA.notifyDataSetChanged();
                    }
                } else if (notification.method.equals("newReport")) {
                    if (notification.params.originator.equals("Plant")) {
                        reportList.add(notification);
                        notificationArrayAdapterR.notifyDataSetChanged();
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


    public void publishMessage(String command){
        try {
            MqttMessage message = new MqttMessage();
            Gson gson = new Gson();

            message.setPayload(command.getBytes());

            //--- MDP added
            message.setRetained(false);
            message.setQos( 0 );
            //---
            mqttAndroidClient.publish(publishTopic, message);
            addToHistory("Message Published");
            if(!mqttAndroidClient.isConnected()){
                addToHistory(mqttAndroidClient.getBufferedMessageCount() + " messages in buffer.");
            }
        } catch (Exception e) {
            System.err.println("Error Publishing: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void onClick(View v) {
        Intent i;
        if (connected) {
            Intent intent = new Intent(PlantActivity.this, NutrientsActivity.class);
            startActivityForResult(intent, PICK_BUTTON);
        }

        else {
            new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Oops...")
                    .setContentText("You are not connected!")
                    .show();
        }
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        super.onActivityResult(requestCode, resultCode, data);
        mqttConnection();
        if (requestCode == PICK_BUTTON) {
            if (resultCode == RESULT_OK) {
                final SweetAlertDialog dialog2 = new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE);
                dialog2.setTitleText("Done!");
                dialog2.setContentText(data.getStringExtra("result") + " applyed");
                dialog2.show();

                final SweetAlertDialog dialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
                dialog.setTitleText("Applying " + data.getStringExtra("result") + " ...");
                dialog.setCancelable(true);
                dialog.show();

                //while(!subbed){}
                final String commandON = "{ \"nutrientsAct\": 1}";
                final String commandOFF = "{ \"nutrientsAct\": 0}";

                new Timer().schedule(
                        new TimerTask() {
                            @Override
                            public void run() {
                                publishMessage(commandON);
                            }
                        }, 1000);

                new Timer().schedule(
                        new TimerTask() {
                            @Override
                            public void run() {
                                String commandON = "{ \"nutrientsAct\": 0}";
                                publishMessage(commandON);
                                dialog.dismiss();

                            }
                        }, 5000);

            }
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



    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
