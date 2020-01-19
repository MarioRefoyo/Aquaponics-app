package dte.masteriot.asp.aquaponics;

import androidx.appcompat.app.AppCompatActivity;


import android.content.Intent;
import android.os.Bundle;
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


public class PlantActivity extends AppCompatActivity implements View.OnClickListener{

    ImageView imgStatus;
    TextView textView;
    Button bCommand1;
    Button bCommand2;
    private ListView lvalarmList;
    private ListView lvreportList;
    private ArrayList<NOTIFICATION> alarmList = new ArrayList<NOTIFICATION>();
    private ArrayList<NOTIFICATION> reportList = new ArrayList<NOTIFICATION>();
    NotificationArrayAdapter notificationArrayAdapterA;
    NotificationArrayAdapter notificationArrayAdapterR;

    MqttAndroidClient mqttAndroidClient;
    boolean connected = false;


    final String serverUri = "tcp://srv-iot.diatel.upm.es:1883";
    String clientId = "AZzXWLsbsUlkFBg5ncoK";
    final String publishTopic = "v1/devices/me/telemetry";
    final String subscriptionTopic = "v1/devices/me/rpc/request/+";

    private int pos = -1;

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
        bCommand2 = (Button) findViewById(R.id.command2);
        bCommand2.setOnClickListener(this);

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
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    addToHistory("Failed to connect to: " + serverUri);
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
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    addToHistory("Failed to subscribe");
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
        } catch (MqttException e) {
            System.err.println("Error Publishing: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void onClick(View v) {
        Intent i;
        if (connected) {
            switch (v.getId()) {

                case R.id.command1:
                    String commandON = "{ \"nutrientsAct\": 1}";
                    publishMessage(commandON);
                    new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                            .setTitleText("Done!")
                            .setContentText("Command was sent!")
                            .show();
                    break;

                case R.id.command2:
                    String commandOFF = "{ \"nutrientsAct\": 0}";
                    publishMessage(commandOFF);
                    new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                            .setTitleText("Done!")
                            .setContentText("Command was sent!")
                            .show();
                    break;

            }
        }
        else {
            new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Oops...")
                    .setContentText("You are not connected!")
                    .show();
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        try {
            IMqttToken disconToken = mqttAndroidClient.disconnect();
            disconToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // we are now successfully disconnected
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken,
                                      Throwable exception) {
                    // something went wrong, but probably we are disconnected anyway
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    /*public class NOTIFICATION{
        String method;
        PARAMS params;
    }
    class PARAMS{
        String type;
        String text;
        String originator;
    }*/
}
