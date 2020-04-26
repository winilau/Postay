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

public class UserShopping extends AppCompatActivity {

    private ListView listView;
    private EditText amount;
    private String[] mProduct = {"Milk","Bread","Toilet Paper","Pasta","Eggs","Laundry Pods","Ricotta Cheese"};
    private String[] mPrice = {"£3.0", "£1.6", "£2.0", "£1.5", "£1.0", "£1.5", "£1.0"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_shopping);

        listView = findViewById(R.id.listView);
        MyAdapter adapter = new MyAdapter(this,mProduct,mPrice);
        listView.setAdapter(adapter);

        Button submit = new Button(this);
        submit.setText("Submit");

        listView.addFooterView(submit);

        String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final DatabaseReference current_user_db = FirebaseDatabase.getInstance().getReference().child("users")
                .child("User").child(user_id);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference shopping = current_user_db.child("Shopping List");
                DatabaseReference total_price = current_user_db.child("Total Price");
                double count = 0.0;
                for(int i = 0;i < mProduct.length ;i++){
                    View view =listView.getChildAt(i);
                    String myAmount = view.findViewById(R.id.amount).toString();
                    if (myAmount != ""){
                        String item = view.findViewById(R.id.product).toString();
                        String price = view.findViewById(R.id.price).toString();
                        String concat = item + ", " + myAmount + ", " + price;
                        shopping.child(concat);
                        double price1 = Double.parseDouble(price.substring(1));
                        int amount = Integer.parseInt(myAmount);
                        count += price1 * amount;
                    }
                }
                total_price.child(Double.toString(count));
            }
        });
    }

    public class InputFilterMinMax implements InputFilter {

        private int min, max;

        public InputFilterMinMax(int min, int max) {
            this.min = min;
            this.max = max;
        }

        public InputFilterMinMax(String min, String max) {
            this.min = Integer.parseInt(min);
            this.max = Integer.parseInt(max);
        }

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            try {
                int input = Integer.parseInt(dest.toString() + source.toString());
                if (isInRange(min, max, input))
                    return null;
            } catch (NumberFormatException nfe) { }
            return "";
        }

        private boolean isInRange(int a, int b, int c) {
            return b > a ? c >= a && c <= b : c >= b && c <= a;
        }
    }


    class MyAdapter extends ArrayAdapter<String> {

        Context context;
        String[] rProduct;
        String[] rPrice;

        MyAdapter (Context c, String[] product, String[] price){
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
            EditText myAmount = row.findViewById(R.id.amount);
            TextView myProduct = row.findViewById(R.id.product);
            TextView myPrice = row.findViewById(R.id.price);

            myAmount.setFilters(new InputFilter[]{ new InputFilterMinMax("1", "12")});


            myProduct.setText(rProduct[position]);
            myPrice.setText(rPrice[position]);
            return row;
        }

    }
}
