package com.example.kamal.flee5;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class ClientOrders extends AppCompatActivity {

    ListView ordersList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_orders);

        ordersList = (ListView)findViewById(R.id.ordersList);
        myAdapter adapter = new myAdapter(this);
        ordersList.setAdapter(adapter);
        ordersList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });

    }

    class myAdapter extends ArrayAdapter<String>{
        Context context;

        myAdapter(Context c){
            super(c,R.layout.orders_list_item);
            this.context = c;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater layoutInflater = (LayoutInflater)getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View row = layoutInflater.inflate(R.layout.orders_list_item,parent,false);
            TextView textView1 =(TextView) row.findViewById(R.id.textView11);
            TextView textView2 =(TextView) row.findViewById(R.id.textView12);

            textView1.setText(String.valueOf(position));
            textView2.setText(";-0");
            return row;
        }
    }
}
