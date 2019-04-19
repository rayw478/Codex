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
        db = new Database(this);
        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        FloatingActionButton myFab = findViewById(R.id.fab);
        myFab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                addEntry(v);
            }
        });
        fillEntries();
    }

    public void fillEntries() {
        ListView list = findViewById(R.id.entry_list);
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
                updateEntry(view, data.getString(0), data.getString(1),
                                  data.getString(2), data.getString(3));
            }
        });

    }


    protected void updateEntry(View v, String barcode, String desc, String price, String img) {
        Intent intent = new Intent(this, BarcodeActivity.class);
        int requestCode = 1;
        intent.putExtra("code", barcode);
        intent.putExtra("desc", desc);
        intent.putExtra("price", price);
        intent.putExtra("imgPath", img);
        intent.putExtra("requestCode", requestCode);
        startActivityForResult(intent, requestCode);
    }

    protected void addEntry(View v) {
        Intent intent = new Intent(this, BarcodeActivity.class);
        int requestCode = 2;
        intent.putExtra("requestCode", requestCode);
        startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == CommonStatusCodes.SUCCESS) {
            fillEntries();
        }
    }


}
