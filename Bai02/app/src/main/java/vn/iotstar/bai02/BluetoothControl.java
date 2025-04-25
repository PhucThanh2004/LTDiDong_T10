package vn.iotstar.bai02;


import static vn.iotstar.bai02.MainActivity.REQUEST_BLUETOOTH;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

public class BluetoothControl extends AppCompatActivity {

    ImageButton btnTb1, btnTb2, btnDis;
    TextView txt1, txtMAC;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    Set<BluetoothDevice> pairedDevices1;
    String address = null;
    private ProgressDialog progress;
    int flaglamp1;
    int flaglamp2;

    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_BLUETOOTH);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_SCAN}, REQUEST_BLUETOOTH);
        }

        super.onCreate(savedInstanceState);
        Intent newint = getIntent();
        address = newint.getStringExtra(MainActivity.EXTRA_ADDRESS);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_control);

        btnTb1 = (ImageButton) findViewById(R.id.btnTb1);
        btnTb2 = (ImageButton) findViewById(R.id.btnTb2);
        txt1 = (TextView) findViewById(R.id.textV1);
        txtMAC = (TextView) findViewById(R.id.textViewMAC);
        btnDis = (ImageButton) findViewById(R.id.btnDisc);
        new ConnectBT().execute();
        btnTb1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                thietTbi1();
            }
        });
        btnTb2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                thietTbi7();
            }
        });
        btnDis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Disconnect();
            }
        });
    }

    private void Disconnect() {
        if(btSocket != null){
            try{
                btSocket.close();
            } catch (IOException e){
                msg("Lỗi");
            }
        }
        finish();
    }

    private void thietTbi7() {

        if (btSocket != null) {
            try {
                if (this.flaglamp2 == 0) {
                    this.flaglamp2 = 1;
                    this.btnTb1.setBackgroundResource(R.drawable.bton);
                    btSocket.getOutputStream().write("7".toString().getBytes());
                    txt1.setText("Thiết bị số 7 đang bật");
                    return;
                } else {
                    if (this.flaglamp2 != 1) return;
                    {
                        this.flaglamp2 = 0;
                        this.btnTb1.setBackgroundResource(R.drawable.btoff);
                        btSocket.getOutputStream().write("G".toString().getBytes());
                        txt1.setText("Thiết bị số 7 đang tắt");
                        return;
                    }
                }
            } catch (IOException e) {
                msg("Lỗi");
            }
        }
        else {
            msg("Null");
        }
    }

    private void thietTbi1() {
        Log.d("Tag:", "Click");
        if (btSocket != null) {
            try {
                if (this.flaglamp1 == 0) {
                    this.flaglamp1 = 1;
                    this.btnTb1.setBackgroundResource(R.drawable.bton);
                    btSocket.getOutputStream().write("1".toString().getBytes());
                    txt1.setText("Thiết bị số 1 đang bật");
                    return;
                } else {
                    if (this.flaglamp1 != 1) return;
                    {
                        this.flaglamp1 = 0;
                        this.btnTb1.setBackgroundResource(R.drawable.btoff);
                        btSocket.getOutputStream().write("A".toString().getBytes());
                        txt1.setText("Thiết bị số 1 đang tắt");
                        return;
                    }
                }
            } catch (IOException e) {
                msg("Lỗi");

            }
        }
        else {
            msg("Null");
        }
    }
    private class ConnectBT extends AsyncTask<Void, Void, Boolean> {
        private boolean ConnectSuccess = true;

        @Override
        protected void onPreExecute() {
            // Hiển thị ProgressDialog trước khi kết nối
            progress = ProgressDialog.show(BluetoothControl.this, "Đang kết nối...", "Xin vui lòng đợi");
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                // Kiểm tra trạng thái kết nối và socket
                if (btSocket == null || !isBtConnected) {
                    myBluetooth = BluetoothAdapter.getDefaultAdapter();

                    // Kiểm tra nếu không có Bluetooth adapter
                    if (myBluetooth == null) {
                        ConnectSuccess = false;
                        return false;
                    }

                    // Lấy thiết bị từ địa chỉ MAC
                    BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(address);

                    // Kiểm tra quyền kết nối Bluetooth
                    if (ActivityCompat.checkSelfPermission(BluetoothControl.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        return false; // Thiếu quyền kết nối Bluetooth
                    }

                    // Tạo socket không bảo mật và kết nối
                    btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();  // Hủy bỏ tìm kiếm thiết bị để tiết kiệm tài nguyên
                    btSocket.connect();  // Kết nối socket
                }
            } catch (IOException e) {
                ConnectSuccess = false;  // Xử lý lỗi kết nối
                Log.e("Bluetooth", "Lỗi kết nối: " + e.getMessage());
                return false;
            }
            return true;  // Kết nối thành công
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            // Kiểm tra kết quả kết nối
            if (!result) {
                msg("Kết nối thất bại, Kiểm tra thiết bị và quyền Bluetooth");
                finish();  // Kết thúc activity nếu kết nối thất bại
            } else {
                msg("Kết nối thành công");
                isBtConnected = true;  // Đánh dấu là đã kết nối
                pairedDevicesList1();  // Cập nhật thông tin thiết bị kết nối
            }

            progress.dismiss();  // Ẩn ProgressDialog
        }
    }


    private void pairedDevicesList1() {
        if(ActivityCompat.checkSelfPermission(this,
                Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED){
            pairedDevices1 = myBluetooth.getBondedDevices();

            if(pairedDevices1.size() > 0){
                for(BluetoothDevice bt : pairedDevices1){
                    txtMAC.setText(bt.getName() + " - " + bt.getAddress());
                }
            } else {
                Toast.makeText(getApplicationContext(), "Không tìm thấy thiết bị kết nối", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void msg(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }
}