package raymond.infox;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    Database db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        db = new Database(this);
        FloatingActionButton myFab = findViewById(R.id.fab);
        fillEntries();
        myFab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                addEntry(v);
            }
        });
    }

    public void fillEntries() {
        ListView list = findViewById(R.id.entry_list);
        //list.removeAllViews();
        Cursor data = db.getData();
        ArrayList<String> entries = new ArrayList<>();
        while (data.moveToNext()) {
            entries.add(data.getString(1));
        }
        ListAdapter adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, entries);
        list.setAdapter(adapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String name = parent.getItemAtPosition(position).toString();
                Cursor data = db.getItem(name);
                data.moveToNext();
                updateEntry(view, data.getString(0), data.getString(1), data.getString(2));
                //Toast.makeText(MainActivity.this, data.getString(0), Toast.LENGTH_SHORT).show();
            }
        });

    }


    protected void updateEntry(View v, String barcode, String desc, String price) {
        Intent intent = new Intent(this, BarcodeActivity.class);
        intent.putExtra("code", barcode);
        intent.putExtra("desc", desc);
        intent.putExtra("price", price);
        startActivityForResult(intent, 1);
        fillEntries();
    }

    protected void addEntry(View v) {
        Intent intent = new Intent(this, BarcodeActivity.class);
        startActivityForResult(intent, 1);
        fillEntries();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 1) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                fillEntries();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


}
