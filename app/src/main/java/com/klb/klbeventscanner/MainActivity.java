package com.klb.klbeventscanner;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.klb.klbeventscanner.network.NetworkUtils;
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
    StringBuilder stringBuilder = new StringBuilder();
    private Picasso picasso;
    private SimpleDateFormat formatter;
    private TextView textWelcome, textViewNameUser, textViewNamePosition, textPositionManager, textGenderManager, textNameManager;
    private Boolean preState = false;
    private View layoutNormal, layoutManager;
    private ImageView imageAvatarView, imageAvatarManager, imageBgNormal, imageBgManager;
    private ShimmerFrameLayout shimmerLayoutNormal, shimmerLayoutManager;
    private Animation fadeInAnimation, fadeOutAnimation;
    private final int TiMER_DIALOG = 5000;

    // Biến để theo dõi số lần click
    private int backPressedCount = 0;

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
                String[] subValues = stringBuilder.toString().split("/");
                Log.d("onKeyUp is: ", subValues[subValues.length - 1]);
                getInfoUser(subValues[subValues.length - 1].replaceAll("\n", ""), this);
                stringBuilder.setLength(0);
            }
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onBackPressed() {
        backPressedCount++;
        if (backPressedCount == 1) {
            Toast.makeText(this, "Nhấn Back lần nữa để thoát", Toast.LENGTH_SHORT).show();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    backPressedCount = 0;
                }
            }, 2000);
        } else if (backPressedCount == 2) {
            super.onBackPressed();
        }
    }

    public void initData() {
        // Create a Picasso instance with the custom OkHttpClient
        picasso = new Picasso.Builder(this)
                .downloader(new OkHttp3Downloader(RetrofitClient.getClientImage()))
                .indicatorsEnabled(true)
                .build();
        // Load the animation
        fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        fadeOutAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_out);
        formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        imageBgNormal = findViewById(R.id.imageBgNormal);
        imageBgManager = findViewById(R.id.imageBgManager);
        layoutNormal = findViewById(R.id.layoutNormal);
        layoutManager = findViewById(R.id.layoutManager);
        textViewNameUser = findViewById(R.id.textNameUser);
        textViewNamePosition = findViewById(R.id.textPositionUser);
        textWelcome = findViewById(R.id.textWelcome);
        textPositionManager = findViewById(R.id.textPositionManager);
        imageAvatarView = findViewById(R.id.imageAvatar);
        imageAvatarManager = findViewById(R.id.imageAvatarManager);
        textGenderManager = findViewById(R.id.textGenderManager);
        textNameManager = findViewById(R.id.textNameManager);
        shimmerLayoutNormal = findViewById(R.id.placeholderViewNormal);
        shimmerLayoutManager = findViewById(R.id.placeholderViewManger);

        Typeface yenTu = Typeface.createFromAsset(getAssets(), "utm_yen_tu.ttf");
        Typeface romanClassic = Typeface.createFromAsset(getAssets(), "utm_roman_classic.ttf");
        textGenderManager.setTypeface(yenTu);
        textNameManager.setTypeface(romanClassic);

        layoutManager.setVisibility(View.INVISIBLE);
        imageBgManager.setVisibility(View.INVISIBLE);
        shimmerLayoutNormal.setVisibility(View.INVISIBLE);
        shimmerLayoutManager.setVisibility(View.INVISIBLE);

        setRickText("HUỲNH LỆ NHƯ SƯƠNG", "Chị");
    }

    public void getInfoUser(String valueQRCode, Context context) {
        if(!NetworkUtils.isNetworkAvailable(context)){
            AppDialog.showDialog(context, "Không có kết nối mạng", "Vui lòng kiểm tra kết nối internet và thử lại sau", 7500);
            return;
        }
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
                        getAvatar(data);
                    } else {
                        Log.e("Get info user", "data null");
                    }
                } else {
                    if (response.code() == 404) {
                        setShowContentView(true);
                        AppDialog.showDialog(context, "Lỗi " + response.code(), "QR code không chính xác.\nVui lòng thử lại", TiMER_DIALOG);
                    } else {
                        setShowContentView(true);
                        AppDialog.showDialog(context, "Lỗi " + response.code(), "Đã có lỗi xảy ra.\nVui lòng thử lại sau", TiMER_DIALOG);
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
                AppDialog.showDialog(context, "Lỗi ", "Đã có lỗi xảy ra.\nVui lòng thử lại sau", TiMER_DIALOG);
            }
        });
    }

    @SuppressLint("SetTextI18n")
    public void getAvatar(UserInfo info) {
        String BASE_IMAGE_URL = "http://event2024.kienlongbank.com/api/v1/public-attachment/";
        picasso.load(BASE_IMAGE_URL + info.getPortraitId())
                .centerCrop()
                .fit()
                .placeholder(R.drawable.avatar_placeholder)
                .error(R.drawable.avatar_placeholder)
                .into(info.isManager() ? imageAvatarManager : imageAvatarView, new com.squareup.picasso.Callback() {
                    @Override
                    public void onSuccess() {
                        setLayoutView(info.isManager());
                        setData(info);
                        setShowContentView(true);
                        fadeIn();
                    }

                    @Override
                    public void onError(Exception e) {
                        (info.isManager() ? imageAvatarManager : imageAvatarView).setImageResource(R.drawable.avatar_placeholder);
                        setLayoutView(info.isManager());
                        setData(info);
                        setShowContentView(true);
                        fadeIn();
                    }
                });
    }

    /// ẩn hiện view dành cho manager hoặc nhân viên.
    void setLayoutView(boolean isManager) {
        boolean isChangeTheme = isManager != preState;

        if (isManager && isChangeTheme) {
            layoutManager.setVisibility(View.VISIBLE);
            imageBgManager.setVisibility(View.VISIBLE);
            imageBgManager.startAnimation(fadeInAnimation);
            imageBgNormal.startAnimation(fadeOutAnimation);
            layoutNormal.setVisibility(View.INVISIBLE);
            imageBgNormal.setVisibility(View.INVISIBLE);
        }

        if (!isManager && isChangeTheme) {
            imageBgManager.startAnimation(fadeOutAnimation);
            layoutManager.setVisibility(View.INVISIBLE);
            imageBgManager.setVisibility(View.INVISIBLE);
            layoutNormal.setVisibility(View.VISIBLE);
            imageBgNormal.setVisibility(View.VISIBLE);
            imageBgNormal.startAnimation(fadeInAnimation);
        }

        preState = isManager;
    }


    /// fill data vào các text
    void setData(UserInfo info) {
        if (info.isManager()) {
            textGenderManager.setText(" " + info.getGender() + " ");
            textNameManager.setText(info.getName().toUpperCase());
            textPositionManager.setText(info.getPosition().toUpperCase());
        } else {
            setRickText(info.getName().toUpperCase(), info.getGender());
            textViewNamePosition.setText(info.getPosition().toUpperCase());
        }
    }

    void setRickText(String userName, String gender) {
        String textNameUser = gender + " " + userName.toUpperCase();
        SpannableString spannableString = new SpannableString(textNameUser);
        StyleSpan boldSpan = new StyleSpan(Typeface.BOLD);
        spannableString.setSpan(boldSpan, gender.length() + 1, textNameUser.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        textViewNameUser.setText(spannableString);
    }

    void fadeIn() {
        imageAvatarView.startAnimation(fadeInAnimation);
        textViewNameUser.startAnimation(fadeInAnimation);
        textViewNamePosition.startAnimation(fadeInAnimation);
        textWelcome.startAnimation(fadeInAnimation);

        imageAvatarManager.startAnimation(fadeInAnimation);
        textNameManager.startAnimation(fadeInAnimation);
        textPositionManager.startAnimation(fadeInAnimation);
        textGenderManager.startAnimation(fadeInAnimation);
    }

    public void setShowContentView(boolean isShowData) {
        if (isShowData) {
            imageAvatarView.setVisibility(View.VISIBLE);
            textViewNameUser.setVisibility(View.VISIBLE);
            textViewNamePosition.setVisibility(View.VISIBLE);
            textWelcome.setVisibility(View.VISIBLE);
            imageAvatarManager.setVisibility(View.VISIBLE);
            textNameManager.setVisibility(View.VISIBLE);
            textGenderManager.setVisibility(View.VISIBLE);
            textPositionManager.setVisibility(View.VISIBLE);

            //shimmer
            shimmerLayoutNormal.setVisibility(View.INVISIBLE);
            shimmerLayoutManager.setVisibility(View.INVISIBLE);
            shimmerLayoutNormal.stopShimmer();
            shimmerLayoutManager.stopShimmer();
        } else {
            imageAvatarView.setVisibility(View.INVISIBLE);
            textViewNameUser.setVisibility(View.INVISIBLE);
            textViewNamePosition.setVisibility(View.INVISIBLE);
            textWelcome.setVisibility(View.INVISIBLE);
            imageAvatarManager.setVisibility(View.INVISIBLE);
            textNameManager.setVisibility(View.INVISIBLE);
            textGenderManager.setVisibility(View.INVISIBLE);
            textPositionManager.setVisibility(View.INVISIBLE);

            //shimmer
            shimmerLayoutNormal.setVisibility(View.VISIBLE);
            shimmerLayoutManager.setVisibility(View.VISIBLE);
            shimmerLayoutNormal.startShimmer();
            shimmerLayoutManager.startShimmer();
        }
    }
}