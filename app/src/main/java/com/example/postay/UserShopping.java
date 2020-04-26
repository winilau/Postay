package com.example.postay;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewDebug;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.text.InputFilter;
import android.text.Spanned;

import com.google.android.gms.common.server.converter.StringToIntConverter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class UserShopping extends AppCompatActivity {

    private EditText[] amount = new EditText[7];
    private Button submit;
    private String[] mProduct = {"Milk","Bread","Toilet Paper","Pasta","Eggs","Laundry Pods","Ricotta Cheese"};
    private String[] mPrice = {"£3.0", "£1.6", "£2.0", "£1.5", "£1.0", "£1.5", "£1.0"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_shopping);

        String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final DatabaseReference current_user_db = FirebaseDatabase.getInstance().getReference().child("users")
                .child("User").child(user_id);

        submit = (Button) findViewById(R.id.submit);

        amount[0] = findViewById(R.id.amount1);
        amount[1] = findViewById(R.id.amount2);
        amount[2] = findViewById(R.id.amount3);
        amount[3] = findViewById(R.id.amount4);
        amount[4] = findViewById(R.id.amount5);
        amount[5] = findViewById(R.id.amount6);
        amount[6] = findViewById(R.id.amount7);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference shopping = current_user_db.child("ShoppingList");
                DatabaseReference total_price = current_user_db.child("TotalPrice");
                HashMap<String,String> map  = new HashMap<>();
                String countStr;
                double count = 0.0;
                for (int i = 0; i < 7; i++){
                    String myAmount = amount[i].getText().toString();
                    if (!myAmount.matches("")){
                        String item = mProduct[i];
                        String price = mPrice[i];
                        map.put(item,myAmount);
                        double price1 = Double.parseDouble(price.substring(1));
                        int amount = Integer.parseInt(myAmount);
                        count += price1 * amount;
                    }
                }
                countStr = Double.toString(count);
                shopping.setValue(map);
                total_price.setValue(countStr);
                Intent intent = new Intent(UserShopping.this, MapsActivityUser.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
