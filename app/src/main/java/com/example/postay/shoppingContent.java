package com.example.postay;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.widget.ArrayAdapter;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import androidx.annotation.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

public class shoppingContent extends AppCompatActivity {

    private ListView listView;
    private String[] mProduct,mAmount;
    private HashMap<String,String> shopping;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_content);
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference refShopping = FirebaseDatabase.getInstance().getReference().child("users").child("User").child(userId).child("ShoppingList");
        refShopping.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
              if (dataSnapshot.exists()){
                  shopping = (HashMap<String,String>) dataSnapshot.getValue();
              }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        listView = (ListView) findViewById(R.id.listView);
        Set<String> keys = shopping.keySet();
        int count = keys.size();
        mProduct = keys.toArray(new String[count]);
        Collection<String> values = shopping.values();
        int num = values.size();
        mAmount = values.toArray(new String[num]);

        MyAdapter adapter = new MyAdapter(this,mProduct,mAmount);

        String total = "";

        TextView total_price = new TextView(this);
        total_price.setText(total);

        listView.addFooterView(total_price);
    }

    class MyAdapter extends ArrayAdapter<String> {

        Context context;
        String rProduct[];
        String rAmount[];

        MyAdapter (Context c, String product[], String amount[]){
            super(c, R.layout.row, R.id.product, product);
            this.context = c;
            this.rProduct = product;
            this.rAmount = amount;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent){
            LayoutInflater layoutInflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View row = layoutInflater.inflate(R.layout.row,parent,false);
            TextView myProduct = row.findViewById(R.id.product);
            TextView myAmount = row.findViewById(R.id.amount);

            myProduct.setText(rProduct[position]);
            myAmount.setText(rAmount[position]);

            return super.getView(position,convertView,parent);
        }

    }
}

