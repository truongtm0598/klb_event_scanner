package com.klb.klbeventscanner;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.klb.klbeventscanner.dialogs.AppDialog;
import com.klb.klbeventscanner.models.AppRequestBody;
import com.klb.klbeventscanner.models.UserInfo;
import com.klb.klbeventscanner.network.ApiService;
import com.klb.klbeventscanner.network.RetrofitClient;
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final long TIMER_VALUE = 10000;
    StringBuilder stringBuilder = new StringBuilder();
    private Picasso picasso;
    private SimpleDateFormat formatter;
    private TextView textViewNameUser;
    private TextView textViewNamePosition;
    private TextView textViewNameUnit;
    private TextView textViewWelcome;
    private ImageView imageAvatarView;
    private ShimmerFrameLayout shimmerFrameLayout;
    private Animation fadeInAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initData();

//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
////                getInfoUser("209675af-92e8-4f9e-ac6a-27b8fb6a76c3");
////                navigatorNextScreen();
//            }
//        }, TIMER_VALUE);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            if (event.getKeyCode() != KeyEvent.KEYCODE_SHIFT_LEFT) {
                char pressedKey = (char) event.getUnicodeChar();
                stringBuilder.append(pressedKey);
            }

            if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                Log.d("onKeyUp is: ", stringBuilder.toString());
                getInfoUser(stringBuilder.toString().replaceAll("\n", ""), this);
                stringBuilder.setLength(0);
            }
        }

        return super.dispatchKeyEvent(event);
    }

    public void initData() {
        // Create a Picasso instance with the custom OkHttpClient
        picasso = new Picasso.Builder(this)
                .downloader(new OkHttp3Downloader(RetrofitClient.getClientImage()))
                .indicatorsEnabled(true)
                .build();

        // Load the animation
        fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in);

        formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        shimmerFrameLayout = findViewById(R.id.placeholderView);
        textViewNameUser = findViewById(R.id.textNameUser);
        textViewNamePosition = findViewById(R.id.textPositionUser);
        textViewNameUnit = findViewById(R.id.textUnitUser);
        imageAvatarView = findViewById(R.id.imageAvatar);
        textViewWelcome = findViewById(R.id.textViewWelcome);
        shimmerFrameLayout.setVisibility(View.INVISIBLE);
    }

    public void getInfoUser(String valueQRCode, Context context) {
        Date date = new Date();
        formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
        String timeCheckIn = formatter.format(date);
        Log.d("timezone", timeCheckIn);

        AppRequestBody appRequestBody = new AppRequestBody(timeCheckIn, "POS01");
        RetrofitClient.getClient().create(ApiService.class).getInfoUser(valueQRCode, appRequestBody).enqueue(new Callback<UserInfo>() {
            @Override
            public void onResponse(@NonNull Call<UserInfo> call, @NonNull Response<UserInfo> response) {
                if (response.isSuccessful()) {
                    UserInfo data = response.body();
                    if (data != null) {
                        setShowContentView(false);
                        imageAvatarView.setImageResource(R.drawable.avatar_placeholder);
                        textViewNameUser.setText(data.getName().toUpperCase());
                        textViewNamePosition.setText(data.getPosition().toUpperCase());
                        textViewNameUnit.setText(data.getBranch().toUpperCase());
                        textViewWelcome.setText("CHÀO MỪNG ".toUpperCase() + data.getGender().toUpperCase() + " THAM DỰ HỘI NGHỊ".toUpperCase());
                        getAvatar(data.getPortraitId());
                    } else {
                        Log.e("Get info user", "data null");
                    }
                } else {

                    if (response.code() == 400) {
                        setShowContentView(true);
                        AppDialog.showDialog(context, "Lỗi " + response.code(), "QR code không chính xác.\nVui lòng thử lại");
                    } else {
                        setShowContentView(true);
                        AppDialog.showDialog(context, "Lỗi " + response.code(), "Đã có lỗi xảy ra.\nVui lòng thử lại");
                    }

                    // Log mã trạng thái của phản hồi
                    Log.e("Get info user", "Response code: " + response.code());
                    String errorBody = response.errorBody().toString();
                    Log.e("Get info user", "Error: " + errorBody);

                }
            }

            @Override
            public void onFailure(@NonNull Call<UserInfo> call, @NonNull Throwable t) {
                setShowContentView(true);
                Log.e("Get info user", t.getMessage() + "\n" + t.getCause() + "\n" + Arrays.toString(t.getStackTrace()));
                AppDialog.showDialog(context, "Lỗi ", "Đã có lỗi xảy ra.\nVui lòng thử lại sau");
            }
        });
    }

    @SuppressLint("SetTextI18n")
    public void getAvatar(String portraitId) {

        // Load image from URL into ImageView using Picasso
        String BASE_IMAGE_URL = "http://event2024.kienlongbank.com/?entryPoint=image&id=";
        picasso.load(BASE_IMAGE_URL + portraitId)
                .centerCrop()
                .fit()
                .placeholder(R.drawable.avatar_placeholder)
                .error(R.drawable.avatar_placeholder)
                .into(imageAvatarView, new com.squareup.picasso.Callback() {
                    @Override
                    public void onSuccess() {
                        setShowContentView(true);
                        textViewWelcome.startAnimation(fadeInAnimation);
                        imageAvatarView.startAnimation(fadeInAnimation);
                        textViewNameUser.startAnimation(fadeInAnimation);
                        textViewNamePosition.startAnimation(fadeInAnimation);
                        textViewNameUnit.startAnimation(fadeInAnimation);
                    }

                    @Override
                    public void onError(Exception e) {
                        setShowContentView(true);
                    }
                });
    }

    public void setShowContentView(boolean isShowData) {
        if (isShowData) {
            textViewWelcome.setVisibility(View.VISIBLE);
            imageAvatarView.setVisibility(View.VISIBLE);
            textViewNameUser.setVisibility(View.VISIBLE);
            textViewNamePosition.setVisibility(View.VISIBLE);
            textViewNameUnit.setVisibility(View.VISIBLE);
            shimmerFrameLayout.setVisibility(View.INVISIBLE);
            shimmerFrameLayout.stopShimmer();
        } else {
            textViewWelcome.setVisibility(View.INVISIBLE);
            imageAvatarView.setVisibility(View.INVISIBLE);
            textViewNameUser.setVisibility(View.INVISIBLE);
            textViewNamePosition.setVisibility(View.INVISIBLE);
            textViewNameUnit.setVisibility(View.INVISIBLE);
            shimmerFrameLayout.setVisibility(View.VISIBLE);
            shimmerFrameLayout.startShimmer();
        }
    }
}