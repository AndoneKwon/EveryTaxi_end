package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class AccountMoneyActivity extends AppCompatActivity {
    Button calculation_Button;
    Button back_Button;
    EditText getMoney;
    EditText getPeople;
    TextView devidedMoney;

    String temp,temp2;
    int total_money;
    int people;
    int devided_money;
    String display_money;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account);

        getMoney = (EditText) findViewById(R.id.edit_money);
        getPeople = (EditText)findViewById(R.id.people_count);
        calculation_Button = (Button)findViewById(R.id.calculation_button);
        back_Button = (Button)findViewById(R.id.back_button);
        devidedMoney = (TextView)findViewById(R.id.money);

        calculation_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                temp=getMoney.getText().toString();
                temp2=getPeople.getText().toString();
                if(temp.matches("")||temp2.matches("")){
                    Toast toast = Toast.makeText(getApplicationContext(),"값을 모두 입력해주세요.",Toast.LENGTH_LONG);
                    toast.show();
                }
                else{
                    total_money=Integer.parseInt(temp);
                    people=Integer.parseInt(temp2);
                    devided_money=total_money/people;
                    //display_money=Integer.toString(devided_money);
                    devidedMoney.setText("분할금액 : " + devided_money);
                }
            }
        });

        back_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MessageActivity.class);

                startActivity(intent);
            }
        });


    }
}
