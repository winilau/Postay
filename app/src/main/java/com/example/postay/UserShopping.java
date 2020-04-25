package com.example.postay;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewDebug;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

public class UserShopping extends AppCompatActivity {

    private ListView listView;
    private Spinner spinner;
    private String mProduct[] = {"Milk","Bread","Toilet Paper","Pasta","Eggs","Laundry Pods","Ricotta Cheese"};
    private String mPrice[] = {"3.0", "1.6", "2.0", "1.5", "1.0", "1.5", "1.0"};
    private Button submit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_shopping);

        spinner = findViewById(R.id.spinner);
        String[] num = new String[]{"1","2","3","4","5","6","7","8","9","10"};
        ArrayAdapter<String> adapter1 = new ArrayAdapter<>(this,R.layout.support_simple_spinner_dropdown_item,num);
        spinner.setAdapter(adapter1);
        listView = findViewById(R.id.listView);

        MyAdapter adapter = new MyAdapter(this,mProduct,mPrice);
        listView.setAdapter(adapter);

    }


    class MyAdapter extends ArrayAdapter<String> {

        Context context;
        String rProduct[];
        String rPrice[];

        MyAdapter (Context c, String product[], String price[]){
            super(c, R.layout.row, R.id.product, product);
            this.context = c;
            this.rProduct = product;
            this.rPrice = price;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent){
            LayoutInflater layoutInflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View row = layoutInflater.inflate(R.layout.row,parent,false);
            TextView myProduct = row.findViewById(R.id.product);
            TextView myPrice = row.findViewById(R.id.price);

            myProduct.setText(rProduct[position]);
            myPrice.setText(rPrice[position]);

            return super.getView(position,convertView,parent);
        }

    }
}
