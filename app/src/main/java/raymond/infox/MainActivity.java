package raymond.infox;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;

public class MainActivity extends Activity {

    TextView barcodeResult;
    Database db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        barcodeResult = findViewById(R.id.barcode_entry);
        db = new Database(this);
    }

    //add a click event on the button
    public void scanBarcode(View v) {
        Intent intent = new Intent(this, ScanBarcodeActivity.class);
        startActivityForResult(intent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 0) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    Barcode barcode = data.getParcelableExtra("barcode");
                    String val = barcode.displayValue;
                    String result = "Value : " + val ;
                    barcodeResult.setText(result);


                } else {
                    barcodeResult.setText("No barcode detected");
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
