package dte.masteriot.asp.aquaponics;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class NutrientsActivity extends AppCompatActivity implements View.OnClickListener{

    Button bcmd1;
    Button bcmd2;
    Button bcmd3;
    Button bcmd4;

    TextView ironDesc;
    TextView potassiumDesc;
    TextView calciumDesc;
    TextView phosporusDesc;

    String resultString;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nutrients);

        getSupportActionBar().hide();

        ironDesc = (TextView) findViewById(R.id.ironDesc);
        ironDesc.setText("This nutrient is easy to spot. If they have yellowish growth then the plant is low on iron");
        potassiumDesc = (TextView) findViewById(R.id.potassiumDesc);
        potassiumDesc.setText("Search for brown scorching and curling of leaf tips like the one in the image");
        calciumDesc = (TextView) findViewById(R.id.calciumDesc);
        calciumDesc.setText("You will notice the plant needs calcium if it doesn’t lose water and very little is being taken in by them");
        phosporusDesc = (TextView) findViewById(R.id.phosphorusDesc);
        phosporusDesc.setText("To spot a phosphorus deficiency, you have to observe a stunted growth in your plants; especially in the early stages of its development. Take a look at the leaves, they may appear dark and even take a reddish or purple hue");




        bcmd1 = (Button) findViewById(R.id.command1);
        bcmd1.setOnClickListener(this);
        bcmd2 = (Button) findViewById(R.id.command2);
        bcmd2.setOnClickListener(this);
        bcmd3 = (Button) findViewById(R.id.command3);
        bcmd3.setOnClickListener(this);
        bcmd4 = (Button) findViewById(R.id.command4);
        bcmd4.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()){
            case R.id.command1:
                resultString = "iron";
                break;
            case R.id.command2:
                resultString = "potassium";
                break;
            case R.id.command3:
                resultString = "calcium";
                break;
            case R.id.command4:
                resultString = "phosphorus";
                break;
        }
        intent.putExtra("result", resultString);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }
}
