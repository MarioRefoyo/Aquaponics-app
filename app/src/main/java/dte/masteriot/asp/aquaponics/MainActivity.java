package dte.masteriot.asp.aquaponics;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    TextView text;
    ImageButton bPlant;
    ImageButton bFish;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        bPlant = (ImageButton) findViewById(R.id.bPlant);
        bFish = (ImageButton) findViewById(R.id.bFish);

        bPlant.setOnClickListener(this);
        bFish.setOnClickListener(this);
        bFish.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent i;
        switch(v.getId()){
            case R.id.bPlant:
                i = new Intent(MainActivity.this, PlantActivity.class);
                startActivity(i);
                break;

            case R.id.bFish:
                i = new Intent(MainActivity.this, FishActivity.class);
                startActivity(i);
                break;
        }
    }

    public void onWEBClick(View view) {
        String myUriString = "https://srv-iot.diatel.upm.es/dashboard/2e059050-2ee0-11ea-a9b0-39bc962ae508?publicId=9a64f460-38cd-11ea-a9b0-39bc962ae508";
        // Implicit Intent:
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(myUriString));
        startActivity(intent);
    }
}
