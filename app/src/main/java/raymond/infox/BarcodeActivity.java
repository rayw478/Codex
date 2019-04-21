package raymond.infox;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
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
    TextView sizeEntry;
    ImageView imageEntry;
    Spinner  departmentEntry;
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
        departmentEntry = findViewById(R.id.department_spinner);
        sizeEntry = findViewById(R.id.size_entry);
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

        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(this.getResources().getColor(R.color.colorPrimaryDark));
        myToolbar.setTitleTextColor(Color.parseColor("#FFFFFF"));


        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addEntry(view);
            }
        });
        imageEntry.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (imagePath.equals("")) {
                    Toast.makeText(BarcodeActivity.this, "No picture", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(getApplicationContext(), ImageActivity.class);
                    intent.putExtra("imagePath", imagePath);
                    startActivity(intent);
                }
            }
        });
        updateEntries();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 3) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    Barcode barcode = data.getParcelableExtra("barcode");
                    barcodeEntry.setText(barcode.displayValue);
                } else {
                    Toast.makeText(this, "No barcode detected!", Toast.LENGTH_SHORT).show();
                }
            } else if (resultCode == 999) {
                imagePath = data.getStringExtra("imagePath");
                String barcode = data.getStringExtra("barcode");
                barcodeEntry.setText(barcode);
                Bitmap bmp = BitmapFactory.decodeFile(imagePath);
                imageEntry.setImageBitmap(bmp);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


    /**
     * Starts a camera activity to scan a barcode and/or take a picture
     *
     * @param v Scan button
     */
    public void scanBarcode(View v) {
        Intent intent = new Intent(this, ScanBarcodeActivity.class);
        intent.putExtra("barcode", barcodeEntry.getText().toString());
        startActivityForResult(intent, 3);
    }

    /**
     * Creates a new entry in database with given information. A barcode and description is required.
     *
     * @param v Add button
     */
    public void addEntry(View v) {
        if (barcodeEntry.length() != 0 && descEntry.length() != 0) {
            if (db.addOrUpdateData(barcodeEntry.getText().toString(), descEntry.getText().toString(),
                    priceEntry.getText().toString(), imagePath, sizeEntry.getText().toString(),
                    (Department) departmentEntry.getSelectedItem())) {
                Toast.makeText(this, "Entry added successfully!", Toast.LENGTH_SHORT).show();
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

    /**
     * Removes current entry from database, or discards any information currently entered.
     *
     * @param v Delete button
     */
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

    /**
     * If an entry is clicked on from the main activity, update all current views with all available
     * information.
     */
    public void updateEntries() {
        ArrayAdapter<Department> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, Department.values());
        departmentEntry.setAdapter(adapter);
        if (getIntent().getIntExtra("requestCode", 0) != ADD_REQUEST) {
            String code = getIntent().getStringExtra("code");
            String desc = getIntent().getStringExtra("desc");
            String price = getIntent().getStringExtra("price");
            String image = getIntent().getStringExtra("imgPath");
            String size = getIntent().getStringExtra("size");
            String dep = getIntent().getStringExtra("department");


            if (code != null) {
                barcodeEntry.setText(code);
            }
            if (desc != null) {
                descEntry.setText(desc);
            }
            if (price != null) {
                priceEntry.setText(price);
            }
            if (size != null) {
                sizeEntry.setText(size);
            }
            if (dep != null) {
                if (!dep.equals("")) {
                    Department cat = getCategory(dep);
                    if (cat != null) {
                        int spinnerPosition = adapter.getPosition(cat);
                        departmentEntry.setSelection(spinnerPosition);
                    }
                }
            }
            if (image != null) {
                if (!image.isEmpty()) {
                    File photo = new File(image);
                    if (photo.exists()) {
                        imagePath = image;
                        Bitmap bmp = BitmapFactory.decodeFile(image);
                        imageEntry.setImageBitmap(bmp);
                    }
                }
            }
        }
    }

    /**
     * Gets the Category enum constant given a name.
     *
     * @param name  String value of the enum constant in question
     * @return      The corresponding Department enum
     */
    private Department getCategory(String name) {
        for (Department c : Department.values()) {
            if (c.toString().equals(name)) {
                return c;
            }
        }
        return null;
    }

}
