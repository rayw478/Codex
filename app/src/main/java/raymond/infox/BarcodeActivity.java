package raymond.infox;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;

import java.io.File;


public class BarcodeActivity extends AppCompatActivity {

    private final static int ADD_REQUEST = 2;
    TextView barcodeEntry;
    TextView descEntry;
    TextView priceEntry;
    ImageView imageEntry;
    Database db;
    String imagePath = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode);
        db = new Database(this);
        barcodeEntry = findViewById(R.id.code_entry);
        descEntry = findViewById(R.id.desc_entry);
        priceEntry = findViewById(R.id.price_entry);
        imageEntry = findViewById(R.id.item_image);
        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        myToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(CommonStatusCodes.CANCELED);
                db.close();
                finish();
            }
        });


        if (getIntent().getIntExtra("requestCode", 0) != ADD_REQUEST) {
            String code = getIntent().getStringExtra("code");
            String desc = getIntent().getStringExtra("desc");
            String price = getIntent().getStringExtra("price");
            String image = getIntent().getStringExtra("imgPath");

            if (code != null) {
                barcodeEntry.setText(code);
            }
            if (desc != null) {
                descEntry.setText(desc);
            }
            if (price != null) {
                priceEntry.setText(price);
            }
            if (image != null) {
                if (!image.isEmpty()) {
                    File photo = new File(image);
                    if (photo.exists()) {
                        Bitmap bmp = BitmapFactory.decodeFile(image);
                        imageEntry.setImageBitmap(bmp);
                    }
                }
            }
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
        startActivityForResult(intent, 3);
    }

    // Add entry to database
    public void addEntry(View v) {
        if (barcodeEntry.length() != 0 && descEntry.length() != 0) {
            if (db.addOrUpdateData(barcodeEntry.getText().toString(), descEntry.getText().toString(),
                                   priceEntry.getText().toString(), "")) {
                Toast.makeText(this, "Entry add success!", Toast.LENGTH_SHORT).show();
                setResult(CommonStatusCodes.SUCCESS);
                db.close();
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
        if (getIntent().getIntExtra("requestCode", 0) == ADD_REQUEST) {
            setResult(CommonStatusCodes.CANCELED);
            finish();
        } else {
            db.removeData(getIntent().getStringExtra("code"));
            setResult(CommonStatusCodes.SUCCESS);
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 3) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                String imgPath;
                Barcode barcode;
                if ((barcode = data.getParcelableExtra("barcode")) != null) {
                    barcodeEntry.setText(barcode.displayValue);
                    Bitmap bmp = BitmapFactory.decodeFile(data.getStringExtra("imagePath"));
                    imageEntry.setImageBitmap(bmp);

                } else {
                    Toast.makeText(this, "No barcode detected!", Toast.LENGTH_SHORT).show();
                }
                //if ((imgPath = data.getStringExtra("imagePath")) != null) {
                //        Bitmap bmp = BitmapFactory.decodeFile(imgPath);
                //        imageEntry.setImageBitmap(bmp);

                // }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


}
