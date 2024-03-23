package com.example.doan2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAssignedNumbers;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;

import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;

import java.io.UnsupportedEncodingException;


public class MainActivity extends AppCompatActivity {

    TextView sever1, client2;
    Switch switch1, switch2;
    ImageView imgdevice, imgdevice2;

    private TextView batteryPercentage1, batteryPercentage2;
    private ImageView batteryIcon1, batteryIcon2;

    private TextView gas1, gas2, fire1,fire2;

    private int notificationId1 = 1;
    private int notificationId2 = 2;
    private static final int LOW_BATTERY_THRESHOLD = 10;

    MqttAndroidClient client;
    MqttConnectOptions options;
    static String url = "tcp://192.168.1.18:1883";
    int gasValue1;


    int gasValue2;
    int lua1,lua2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sever1 = findViewById(R.id.server1);
        client2 = findViewById(R.id.client2);
        switch1 = findViewById(R.id.switch1);
        switch2 = findViewById(R.id.switch2);
        imgdevice = findViewById(R.id.imagedevice);
        imgdevice2 = findViewById(R.id.imagedevice2);
        batteryPercentage1 = findViewById(R.id.battery_percentage);
        batteryIcon1 = findViewById(R.id.battery_icon);
        batteryPercentage2 = findViewById(R.id.battery_percentage2);
        batteryIcon2 = findViewById(R.id.battery_icon2);
        gas1 = findViewById(R.id.gas_level_1);
        gas2 = findViewById(R.id.gas_level_2);
        fire1 = findViewById(R.id.fire_status_1);
        fire2 = findViewById(R.id.fire_status_2);

        createNotificationChannel("channel1", "Channel 1", "Channel 1 Description");
        createNotificationChannel("channel2", "Channel 2", "Channel 2 Description");

        String clientId = MqttClient.generateClientId();
        client = new MqttAndroidClient(this.getApplicationContext(),url,clientId);
        options = new MqttConnectOptions();

        try {
            IMqttToken token = client.connect(options);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Toast.makeText(MainActivity.this, "Connected", Toast.LENGTH_LONG).show();
                SUB(client,"esp1/gas"); SUB(client,"esp2/gas");
                SUB(client,"esp1/fire"); SUB(client,"esp2/fire");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Toast.makeText(MainActivity.this, "Disconnected", Toast.LENGTH_LONG).show();
                }
            });

        } catch (MqttException e) {
            throw new RuntimeException(e);
        }
        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                // Xử lý khi kết nối mất
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                if (topic.equals("esp1/gas")) {
                    String mydata = message.toString();
                    gasValue1 = Integer.parseInt(mydata);
                }
                if (topic.equals("esp2/gas")) {
                    String mydata1 = message.toString();
                    gasValue2 = Integer.parseInt(mydata1);
                }
                if (topic.equals("esp1/fire")) {
                    String mydata2 = message.toString();
                    lua1 = Integer.parseInt(mydata2);
                }
                if (topic.equals("esp2/fire")) {
                    String mydata3 = message.toString();
                    lua2= Integer.parseInt(mydata3);
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateUI();
                    }
                });
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                // Xử lý khi gửi tin nhắn hoàn tất
            }
        });
        switch1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Kiểm tra xem switch đã được bật hay tắt
                if (isChecked) {
                    // Switch được bật, xuất bản tin nhắn MQTT với giá trị "1"
                    publishMessage("esp1/coi", "1");
                } else {
                    // Switch được tắt, xuất bản tin nhắn MQTT với giá trị "0"
                    publishMessage("esp1/coi", "0");
                }
            }
        });

        switch2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Kiểm tra xem switch đã được bật hay tắt
                if (isChecked) {
                    // Switch được bật, xuất bản tin nhắn MQTT với giá trị "1"
                    publishMessage("esp2/coi", "1");
                } else {
                    // Switch được tắt, xuất bản tin nhắn MQTT với giá trị "0"
                    publishMessage("esp2/coi", "0");
                }
            }
        });

// Đảm bảo rằng client đã được khởi tạo và kết nối trước khi thực hiện các thao tác như subscribe và publish

        sever1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRenameDialogForDevice1();
            }
        });

        client2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRenameDialogForDevice2();
            }
        });

        String savedDevice1Name = DataManager.getDeviceName(MainActivity.this, "Device1", "Thiết bị 1");
        sever1.setText(savedDevice1Name);

        String savedDevice2Name = DataManager.getDeviceName(MainActivity.this, "Device2", "Thiết bị 2");
        client2.setText(savedDevice2Name);

        switch1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                DataManager.saveSwitchState(MainActivity.this, "Switch1State", isChecked);
                if (isChecked) {
                    imgdevice.setImageResource(R.drawable.fire_alarm_on);
                    showFireNotification(sever1.getText().toString());
                    showFireAlert(sever1.getText().toString());
                } else {
                    imgdevice.setImageResource(R.drawable.fire_alarm_off);
                }
            }
        });

        switch2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                DataManager.saveSwitchState(MainActivity.this, "Switch2State", isChecked);
                if (isChecked) {
                    imgdevice2.setImageResource(R.drawable.fire_alarm_on);
                    showFireNotification2(client2.getText().toString());
                    showFireAlert(client2.getText().toString());
                } else {
                    imgdevice2.setImageResource(R.drawable.fire_alarm_off);
                }
            }
        });


        displayBatteryInfo();

        boolean savedSwitch1State = DataManager.getSwitchState(MainActivity.this, "Switch1State", false);
        switch1.setChecked(savedSwitch1State);

        boolean savedSwitch2State = DataManager.getSwitchState(MainActivity.this, "Switch2State", false);
        switch2.setChecked(savedSwitch2State);

        float batteryPercentage = 0;

        DataManager.saveBatteryPercentage(MainActivity.this, batteryPercentage);

        float savedBatteryPercentage = DataManager.getBatteryPercentage(MainActivity.this, 0);
        // Cập nhật giao diện với trạng thái pin đã lưu

    }

    @SuppressLint("StringFormatMatches")
    private void displayBatteryInfo() {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = registerReceiver(null, ifilter);

        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        float batteryPercentage = level * 100 / (float) scale;

        // Hiển thị phần trăm pin và icon tương ứng cho thiết bị 1
        batteryPercentage1.setText(getString(R.string.battery_percentage , batteryPercentage));
        setBatteryIcon(batteryIcon1, batteryPercentage);

        // Hiển thị phần trăm pin và icon tương ứng cho thiết bị 2
        batteryPercentage2.setText(getString(R.string.battery_percentage, batteryPercentage));
        setBatteryIcon(batteryIcon2, batteryPercentage);

        // Kiểm tra nếu pin của một trong hai thiết bị dưới 10% và gửi cảnh báo nếu cần
        if (batteryPercentage < 10) {
            // Gửi cảnh báo vị trí nếu pin dưới 10% ở một trong hai thiết bị
            sendLowBatteryAlert(sever1.getText().toString());
            sendLowBatteryAlert(client2.getText().toString());
        }
    }

    private void sendLowBatteryAlert(String deviceName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Cảnh báo: Pin yếu");
        builder.setMessage("Pin tại vị trí " + deviceName + " dưới " + LOW_BATTERY_THRESHOLD + "%. Hãy thay pin ngay.");

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void setBatteryIcon(ImageView imageView, float batteryPercentage) {
        if (batteryPercentage >= 90) {
            imageView.setImageResource(R.drawable.baseline_battery_full_24);
        } else if (batteryPercentage <= 80 && batteryPercentage > 60) {
            imageView.setImageResource(R.drawable.baseline_battery_70_bar_24);
        } else if (batteryPercentage <= 60 && batteryPercentage > 40 ) {
            imageView.setImageResource(R.drawable.baseline_battery_50_bar_24);
        } else if (batteryPercentage <= 40 && batteryPercentage > 25) {
            imageView.setImageResource(R.drawable.baseline_battery_30_bar_24);
        } else if (batteryPercentage <= 25 && batteryPercentage > 15 ) {
            imageView.setImageResource(R.drawable.baseline_battery_20_bar_24);
        } else if (batteryPercentage <= 15 && batteryPercentage > 10 ) {
            imageView.setImageResource(R.drawable.baseline_battery_15_bar_24);
        } else if (batteryPercentage <= 10 ){
            imageView.setImageResource(R.drawable.baseline_battery_alert_24);
        }
    }


//-----------------------------------------------------------------------------------------------------------------------------------------


    private void showFireNotification(String deviceName) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "fire_channel")
                .setSmallIcon(R.drawable.emergency)
                .setContentTitle("Cảnh báo cháy")
                .setContentText("Tại vị trí " + deviceName + " đang xảy ra cháy")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentIntent(pendingIntent);


        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        notificationManager.notify(notificationId1, builder.build());
    }
    private void showFireNotification2(String deviceName) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "fire_channel")
                .setSmallIcon(R.drawable.emergency)
                .setContentTitle("Cảnh báo cháy")
                .setContentText("Tại vị trí " + deviceName + " đang xảy ra cháy")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentIntent(pendingIntent);


        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        notificationManager.notify(notificationId2, builder.build());
    }

    private void createNotificationChannel(String channelId, String channelName, String channelDescription) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(channelDescription);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void showFireAlert(String deviceName) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Cảnh báo cháy");
            builder.setMessage("Tại vị trí " + deviceName +" đang xảy ra cháy");
            builder.setIcon(R.drawable.emergency);

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Do something when OK is clicked
                    dialog.dismiss();
                }
            });

            builder.setNegativeButton("Tắt cảnh báo", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (deviceName.equals(sever1.getText().toString())) {
                        switch1.setChecked(false);
                        publishMessage("esp1/coi", "0");
                    } else if (deviceName.equals(client2.getText().toString())) {
                        switch2.setChecked(false);
                        publishMessage("esp2/coi", "0");
                    }
                    dialog.dismiss();
                }
            });

            AlertDialog alertDialog = builder.create();
            alertDialog.show();
    }

    private void showRenameDialogForDevice2() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.class_dialog);

        TextView dialogTitle = dialog.findViewById(R.id.txtTitle);
        dialogTitle.setText("Rename Device 2");

        EditText editTextDeviceName = dialog.findViewById(R.id.editName);

        Button buttonSave = dialog.findViewById(R.id.btnAdd);
        Button cancel = dialog.findViewById(R.id.btnCancel);
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newName = editTextDeviceName.getText().toString();
                if (!newName.isEmpty()) {
                    client2.setText(newName);
                    DataManager.saveDeviceName(MainActivity.this, "Device2", newName);
                    dialog.dismiss();
                } else {
                    Toast.makeText(MainActivity.this, "Please enter a device name", Toast.LENGTH_SHORT).show();
                }

            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void showRenameDialogForDevice1() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.class_dialog);

        TextView dialogTitle = dialog.findViewById(R.id.txtTitle);
        dialogTitle.setText("Rename Device ");

        EditText editTextDeviceName = dialog.findViewById(R.id.editName);

        Button buttonSave = dialog.findViewById(R.id.btnAdd);
        Button cancel = dialog.findViewById(R.id.btnCancel);
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newName = editTextDeviceName.getText().toString();
                if (!newName.isEmpty()) {
                    sever1.setText(newName);
                    DataManager.saveDeviceName(MainActivity.this, "Device1", newName);
                    dialog.dismiss();
                } else {
                    Toast.makeText(MainActivity.this, "Please enter a device name", Toast.LENGTH_SHORT).show();
                }

            }

        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }
public void SUB (MqttAndroidClient client, String topic){
        try{
            IMqttToken subToken = client.subscribe(topic,1);
            subToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {

                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {

                }
            });
        }  catch (MqttException e) {
            throw new RuntimeException(e);
        }
}
    private void updateUI() {
        gas1.setText(String.valueOf(gasValue1));
        gas2.setText(String.valueOf(gasValue2));

        updateFireStatus(fire1, switch1, gasValue1, lua1);
        updateFireStatus(fire2, switch2, gasValue2, lua2);

        // Kiểm tra switch1
        if (switch1.isChecked()) {
            publishMessage("esp1/coi", "1");
        } else {
            publishMessage("esp1/coi", "0");
        }

        // Kiểm tra switch2
        if (switch2.isChecked()) {
            publishMessage("esp2/coi", "1");
        } else {
            publishMessage("esp2/coi", "0");
        }
    }

    private void updateFireStatus(TextView fire, Switch switchBtn, int gasValue, int lua) {
        if (gasValue >= 1000 && lua == 0) {
            fire.setText("Có khói");
            switchBtn.setChecked(true);
        } else if (gasValue < 1000 && lua == 1) {
            fire.setText("Cháy");
            switchBtn.setChecked(true);
        } else if (gasValue >= 1000 && lua == 1) {
            fire.setText("Cháy và có khói");
            switchBtn.setChecked(true);
        } else {
            fire.setText("Không cháy");
            switchBtn.setChecked(false);
            imgdevice.setImageResource(R.drawable.fire_alarm_off);
        }
    }

    // Phương thức để xuất bản một tin nhắn đến một chủ đề cụ thể
    private void publishMessage(String topic, String payload) {
        try {
            byte[] encodedPayload = payload.getBytes("UTF-8");
            MqttMessage message = new MqttMessage(encodedPayload);
            client.publish(topic, message);
        } catch (UnsupportedEncodingException | MqttException e) {
            e.printStackTrace();
            // Xử lý lỗi nếu có
        }
    }
}