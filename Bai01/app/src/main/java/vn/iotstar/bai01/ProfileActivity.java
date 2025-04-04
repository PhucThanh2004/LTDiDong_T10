package vn.iotstar.bai01;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    private static final int MY_REQUEST_CODE = 100; // Mã yêu cầu quyền
    Button btnChoose, btnUpload;
    ImageView imageViewChoose, imageViewUpload;
    EditText editTextUserName;
    TextView textViewUsername, txtName;
    private Uri mUri;
    private ProgressDialog mProgressDialog;

    // Mảng quyền cho các phiên bản Android cũ hơn
    public static String[] storage_permissions = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    // Mảng quyền cho các phiên bản Android Tiramisu (API 33 trở lên)
    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    public static String[] storage_permissions_33 = {
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_AUDIO,
            Manifest.permission.READ_MEDIA_VIDEO
    };

    // Phương thức trả về các quyền phù hợp với SDK
    public static String[] permissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return storage_permissions_33;
        } else {
            return storage_permissions;
        }
    }

    // Phương thức kiểm tra quyền
    private void checkPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            openGallery();  // Mở gallery nếu không cần yêu cầu quyền
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                // Yêu cầu quyền
                ActivityCompat.requestPermissions(this, permissions(), MY_REQUEST_CODE);
            }
        }
    }

    // Xử lý kết quả yêu cầu quyền
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();  // Nếu được cấp quyền, mở gallery
            } else {
                // Thông báo khi quyền bị từ chối
            }
        }
    }

    // Phương thức mở gallery
    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        activityResultLauncher.launch(Intent.createChooser(intent, "Select Picture"));
    }

    // Định nghĩa ActivityResultLauncher để nhận kết quả từ việc chọn hình ảnh
    private final ActivityResultLauncher<Intent> activityResultLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    mUri = result.getData().getData();
                    imageViewChoose.setImageURI(mUri);  // Hiển thị ảnh đã chọn
                }
            });

    // Khởi tạo ánh xạ các view
    private void AnhXa() {
        btnChoose = findViewById(R.id.btn_choose_file);
        btnUpload = findViewById(R.id.btn_upload);
        imageViewUpload = findViewById(R.id.img_Upload);
        txtName = findViewById(R.id.txtName);
        textViewUsername = findViewById(R.id.txtUsername);
        imageViewChoose = findViewById(R.id.img_Upload);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Gọi hàm ánh xạ
        AnhXa();

        // Khởi tạo ProgressDialog
        mProgressDialog = new ProgressDialog(ProfileActivity.this);
        mProgressDialog.setMessage("Please wait, uploading...");

        // Bắt sự kiện nút chọn ảnh
        btnChoose.setOnClickListener(v -> {
            checkPermission();  // Kiểm tra quyền
        });

        // Bắt sự kiện upload ảnh
        btnUpload.setOnClickListener(v -> {
            if (mUri != null) {
                uploadImage1();  // Gọi hàm upload ảnh (phương thức này bạn cần tự triển khai)
            }
        });
    }

    // Phương thức upload ảnh
    public void uploadImage1(){
        mProgressDialog.show();

        // khai báo biến và setText nếu có
        String username = editTextUserName.getText().toString().trim();
        RequestBody requestUsername = RequestBody.create(MediaType.parse("multipart/form-data"), username);

        // create RequestBody instance from file
        String IMAGE_PATH = RealPathUtil.getRealPath(ProfileActivity.this, mUri);
        Log.e("ffff", IMAGE_PATH);
        File file = new File(IMAGE_PATH);
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);

        // MultipartBody.Part is used to send also the actual file name
        MultipartBody.Part partbodyavatar =
                MultipartBody.Part.createFormData("MY_IMAGES", file.getName(), requestFile);

        // gọi Retrofit
        ServiceAPI.serviceapi.upload(requestUsername, partbodyavatar).enqueue(new Callback<List<ImageUpload>>() {
            @Override
            public void onResponse(Call<List<ImageUpload>> call, Response<List<ImageUpload>> response) {
                mProgressDialog.dismiss();
                List<ImageUpload> imageUpload = response.body();
                if (imageUpload != null && !imageUpload.isEmpty()) {
                    for (ImageUpload image : imageUpload) {
                        textViewUsername.setText(image.getUsername());
                        Glide.with(ProfileActivity.this)
                                .load(image.getAvatar())
                                .into(imageViewUpload);
                    }
                    Toast.makeText(ProfileActivity.this, "Upload thành công", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ProfileActivity.this, "Upload thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<ImageUpload>> call, Throwable t) {
                mProgressDialog.dismiss();
                Log.e("TAG", t.toString());
                Toast.makeText(ProfileActivity.this, "Gọi API thất bại", Toast.LENGTH_LONG).show();
            }
        });
    }
}
