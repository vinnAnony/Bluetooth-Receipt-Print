package com.vinnjeru.btprint;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.dantsu.escposprinter.connection.DeviceConnection;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections;
import com.vinnjeru.btprint.async.AsyncBluetoothEscPosPrint;
import com.vinnjeru.btprint.async.AsyncEscPosPrinter;

import java.text.SimpleDateFormat;
import java.util.Date;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = (Button) this.findViewById(R.id.button_bluetooth_browse);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                browseBluetoothDevice();
            }
        });
        button = (Button) findViewById(R.id.button_bluetooth);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                printBluetooth();
            }
        });
    }
     /*==============================================================================================
    ======================================BLUETOOTH PART============================================
    ==============================================================================================*/

    public static final int PERMISSION_BLUETOOTH = 1;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            switch (requestCode) {
                case MainActivity.PERMISSION_BLUETOOTH:
                    this.printBluetooth();
                    break;
            }
        }
    }

    private BluetoothConnection selectedDevice;

    public void browseBluetoothDevice() {
        final BluetoothConnection[] bluetoothDevicesList = (new BluetoothPrintersConnections()).getList();

        if (bluetoothDevicesList != null) {
            final String[] items = new String[bluetoothDevicesList.length + 1];
            items[0] = "Select printer";
            int i = 0;
            for (BluetoothConnection device : bluetoothDevicesList) {
                items[++i] = device.getDevice().getName();
            }

            AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
            alertDialog.setTitle("Bluetooth printer selection");
            alertDialog.setItems(items, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    int index = i - 1;
                    if(index == -1) {
                        selectedDevice = null;
                    } else {
                        selectedDevice = bluetoothDevicesList[index];
                    }
                    Button button = (Button) findViewById(R.id.button_bluetooth_browse);
                    button.setText(items[i]);
                }
            });

            AlertDialog alert = alertDialog.create();
            alert.setCanceledOnTouchOutside(false);
            alert.show();

        }
    }

    public void printBluetooth() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH}, MainActivity.PERMISSION_BLUETOOTH);
        } else {
            new AsyncBluetoothEscPosPrint(this).execute(this.getAsyncEscPosPrinter(selectedDevice));
        }
    }

    /**
     * Asynchronous printing
     */
    @SuppressLint("SimpleDateFormat")
    public AsyncEscPosPrinter getAsyncEscPosPrinter(DeviceConnection printerConnection) {
        SimpleDateFormat format = new SimpleDateFormat("'on' yyyy-MM-dd 'at' HH:mm:ss");
        AsyncEscPosPrinter printer = new AsyncEscPosPrinter(printerConnection, 203, 48f, 32);
        String receiptHeader =
                "[C]<b><font size='tall'>NAIROBI STORES</font></b>" +"\n" +
                        "[C]<b><font size='normal'>P.O BOX 61 HAIWEI</font></b>" +"\n" +
                        "[C]<b><font size='normal'>Tel: 0712345678</font></b>" +"\n" +
                        "[C]<b><font size='normal'>PIN: SDF777HFH</font></b>\n" +
                        "[L]\n" ;
        String secondHeader =
                "[L]Sale #: "+ "0012135" +
                        "[L]\n" +
                        "[L]Date: "+ format.format(new Date()) +
                        "[L]" +"\n" +
                        "[L]Store: "+ "Cambodi" +
                        "[C]\n" +
                        "[C]"+ "CASH SALE" + "\n";
        String singleProduct =
                "[L]" + "Pro Gas Cylinder 50 kg" + "\n" +
                        "[L][R]"+"10"+" x [R]"+"450.00"+"[R]"+"4500.00"+"\n" +
                        "[L]\n" ;
        String discountSection =
                "[L]Discount/Promo :[R]"+"10% on all"+"\n" +
                        "[L]Amount :[R]"+"450.00"+"\n" +
                        "[C]--------------------------------\n" ;
        return printer.setTextToPrint(receiptHeader + secondHeader +
                "[C]================================\n" +
                "[L]ITEM[R]QTY[R]PRICE[R]TOTAL\n"+
                "[C]--------------------------------\n" +
                singleProduct +
                "[C]--------------------------------\n" +
                discountSection +
                "[L]SUBTOTAL :[R]"+"4500.00"+"\n" +
                "[L]BARGAIN :[R]"+"0.00"+"\n" +
                "[L]DISCOUNT :[R]"+"450.00"+"\n" +
                "[L]TOTAL :[R]"+"4050.00"+"\n" +
                "[C]================================\n" +
                "[L]<b>METHOD</b>[L]<b>REF#</b>[R]<b>AMOUNT</b>\n"+
                "[L]"+"MPESA"+"[L]"+"PHA9GJVRZB"+"[R]"+"4000.00"+"\n"+
                "[L]"+"CASH"+"[L][L]"+""+"[R]"+"50.00"+"\n"+
                "[C]--------------------------------\n" +
                "[L]Served By :[R]"+"Kokoriko"+"\n" +
                "[C]--------------------------------\n" +
                "[C]*********Customer Copy**********\n" +
                "[L]Customer :[L]"+"Kastomaaa"+"\n" +
                "[C]\n" +
                "[C]--------------------------------\n" +
                "[C]\n" +
                "[C]*\n"
        );
    }
}