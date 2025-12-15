package com.example.mobileapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DashboardActivity extends BaseActivity {
    private TextView tvTemperature, tvWeatherDescription, tvWardrobeCount;
    private TextView tvStylePreference, tvTodaySuggestion;
    private CardView cardAiOutfit, cardWardrobe, cardVirtualTryOn;
    private SharedPreferences prefs;
    private ExecutorService executorService;

    // OpenWeatherMap API (bạn cần đăng ký key miễn phí tại openweathermap.org)
    private static final String WEATHER_API_KEY = "YOUR_API_KEY_HERE";
    private static final String WEATHER_API_URL = "https://api.openweathermap.org/data/2.5/weather?q=Hanoi&units=metric&appid=";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        prefs = getSharedPreferences("FashionAssistant", MODE_PRIVATE);
        executorService = Executors.newSingleThreadExecutor();

        initViews();
        loadUserData();
        fetchWeatherData();
        setupClickListeners();
        setupBottomNavigation();
    }

    private void initViews() {
        tvTemperature = findViewById(R.id.tvTemperature);
        tvWeatherDescription = findViewById(R.id.tvWeatherDescription);
        tvWardrobeCount = findViewById(R.id.tvWardrobeCount);
        tvStylePreference = findViewById(R.id.tvStylePreference);
        tvTodaySuggestion = findViewById(R.id.tvTodaySuggestion);
        cardAiOutfit = findViewById(R.id.cardAiOutfit);
        cardWardrobe = findViewById(R.id.cardWardrobe);
        cardVirtualTryOn = findViewById(R.id.cardVirtualTryOn);
    }

    private void loadUserData() {
        String styles = prefs.getString("styles", "Thanh Lịch");
        String[] styleArray = styles.split(",");
        if (styleArray.length > 0) {
            tvStylePreference.setText(styleArray[0]);
        }

        int wardrobeCount = prefs.getInt("wardrobe_count", 0);
        tvWardrobeCount.setText(String.valueOf(wardrobeCount));
    }

    private void fetchWeatherData() {
        executorService.execute(() -> {
            try {
                URL url = new URL(WEATHER_API_URL + WEATHER_API_KEY);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JSONObject jsonObject = new JSONObject(response.toString());
                JSONObject main = jsonObject.getJSONObject("main");
                double temp = main.getDouble("temp");
                JSONObject weather = jsonObject.getJSONArray("weather").getJSONObject(0);
                String description = weather.getString("description");

                runOnUiThread(() -> {
                    tvTemperature.setText(String.format("%.0f°C", temp));
                    tvWeatherDescription.setText(translateWeather(description));
                    updateSuggestion(temp, description);
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    tvTemperature.setText("28°C");
                    tvWeatherDescription.setText("Nắng");
                    updateSuggestion(28, "clear");
                });
            }
        });
    }

    private String translateWeather(String description) {
        if (description.contains("clear")) return "Nắng";
        if (description.contains("cloud")) return "Nhiều mây";
        if (description.contains("rain")) return "Mưa";
        if (description.contains("snow")) return "Tuyết";
        return "Nắng";
    }

    private void updateSuggestion(double temp, String weather) {
        String suggestion;
        String colors = prefs.getString("colors", "");
        String styles = prefs.getString("styles", "");

        if (temp > 30) {
            suggestion = "💡 Với thời tiết " + String.format("%.0f", temp) + "°C hôm nay rất nóng, " +
                    "bạn nên chọn trang phục nhẹ nhàng, thoáng mát như áo thun và quần short. " +
                    "Màu sắc sáng sẽ giúp bạn mát mẻ hơn.";
        } else if (temp > 25) {
            suggestion = "💡 Với thời tiết " + String.format("%.0f", temp) + "°C hôm nay ấm áp, " +
                    "bạn nên chọn trang phục nhẹ nhàng như áo sơ mi và quần jeans. " +
                    "Phù hợp cho phong cách " + (styles.isEmpty() ? "thanh lịch" : styles.split(",")[0]) + ".";
        } else if (temp > 20) {
            suggestion = "💡 Với thời tiết " + String.format("%.0f", temp) + "°C hôm nay mát mẻ, " +
                    "bạn có thể kết hợp áo khoác nhẹ với quần dài. " +
                    "Thời điểm tuyệt vời để thể hiện phong cách của bạn!";
        } else {
            suggestion = "💡 Với thời tiết " + String.format("%.0f", temp) + "°C hôm nay lạnh, " +
                    "bạn nên mặc áo khoác ấm và quần dài. " +
                    "Đừng quên mang theo khăn choàng!";
        }

        tvTodaySuggestion.setText(suggestion);
    }

    private void setupClickListeners() {
        cardAiOutfit.setOnClickListener(v -> {
            Intent intent = new Intent(this, AiOutfitActivity.class);
            startActivity(intent);
        });

        cardWardrobe.setOnClickListener(v -> {
            Intent intent = new Intent(this, WardrobeActivity.class);
            startActivity(intent);
        });

        cardVirtualTryOn.setOnClickListener(v -> {
            Intent intent = new Intent(this, VirtualTryOnActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}

