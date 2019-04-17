package raymond.infox;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;

public class BarcodeActivity extends AppCompatActivity {

    TextView barcodeEntry;
    TextView descEntry;
    TextView priceEntry;
    Database db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode);
        barcodeEntry = findViewById(R.id.code_entry);
        descEntry = findViewById(R.id.desc_entry);
        priceEntry = findViewById(R.id.price_entry);
        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        myToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        db = new Database(this);

        String code = getIntent().getStringExtra("code");
        String desc = getIntent().getStringExtra("desc");
        String price = getIntent().getStringExtra("price");

        if (code != null) {
            barcodeEntry.setText(code);
        }
        if (desc != null) {
            descEntry.setText(desc);
        }
        if (price != null) {
            priceEntry.setText(price);
        }

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addEntry(view);
            }
        });
    }

    //add a click event on the button
    public void scanBarcode(View v) {
        Intent intent = new Intent(this, ScanBarcodeActivity.class);
        startActivityForResult(intent, 0);
    }

    // Add entry to database
    public void addEntry(View v) {
        if (!barcodeEntry.getText().toString().equals("") && !descEntry.getText().toString().equals("")) {
            if (db.addOrUpdateData(barcodeEntry.getText().toString(), descEntry.getText().toString(), priceEntry.getText().toString())) {
                Toast.makeText(this, "Entry add success!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Unable to add entry", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Barcode or description cannot be empty!", Toast.LENGTH_SHORT).show();
        }
    }

    // delete entry
    public void removeEntry(View v) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 0) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    Barcode barcode = data.getParcelableExtra("barcode");
                    barcodeEntry.setText(barcode.displayValue);
                } else {
                    Toast.makeText(this, "No barcode detected!", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


}
