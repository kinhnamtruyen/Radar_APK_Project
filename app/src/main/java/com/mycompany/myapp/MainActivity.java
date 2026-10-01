package com.mycompany.myapp;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends Activity {

    private static final int REQUEST_LOCATION = 100;

    private WifiManager wifiManager;
    private BluetoothAdapter bluetoothAdapter;

    private RadarView radarView;
    private Button scanButton;
    private TextView statusText;
    private LinearLayout resultsLayout;

    private final Set<String> wifiNames = new HashSet<>();
    private final Set<String> bluetoothNames = new HashSet<>();

    private final BroadcastReceiver wifiReceiver =
	new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            showWifiResults();
        }
    };

    private final BroadcastReceiver bluetoothReceiver =
	new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {

                BluetoothDevice device =
					intent.getParcelableExtra(
					BluetoothDevice.EXTRA_DEVICE);

                if (device != null) {

                    String name = device.getName();

                    if (name == null || name.trim().isEmpty()) {
                        name = "Thiết bị Bluetooth";
                    }

                    bluetoothNames.add(
						name + "  (" + device.getAddress() + ")"
                    );

                    radarView.addBlip();
                    updateStatus();
                }
            }

            if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                updateStatus();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        radarView = findViewById(R.id.radarView);
        scanButton = findViewById(R.id.scanButton);
        statusText = findViewById(R.id.statusText);
        resultsLayout = findViewById(R.id.resultsLayout);

        wifiManager =
			(WifiManager)
			getApplicationContext()
			.getSystemService(WIFI_SERVICE);

        bluetoothAdapter =
			BluetoothAdapter.getDefaultAdapter();

        registerReceiver(
			wifiReceiver,
			new IntentFilter(
				WifiManager.SCAN_RESULTS_AVAILABLE_ACTION
			)
        );

        IntentFilter bluetoothFilter =
			new IntentFilter();

        bluetoothFilter.addAction(
			BluetoothDevice.ACTION_FOUND
        );

        bluetoothFilter.addAction(
			BluetoothAdapter.ACTION_DISCOVERY_FINISHED
        );

        registerReceiver(
			bluetoothReceiver,
			bluetoothFilter
        );

        scanButton.setOnClickListener(
			new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					startScan();
				}
			}
        );

        if (android.os.Build.VERSION.SDK_INT >= 23) {

            if (checkSelfPermission(
                    Manifest.permission.ACCESS_FINE_LOCATION)
				!= PackageManager.PERMISSION_GRANTED) {

                requestPermissions(
					new String[]{
						Manifest.permission.ACCESS_FINE_LOCATION,
						Manifest.permission.ACCESS_COARSE_LOCATION
					},
					REQUEST_LOCATION
                );
            }
        }
    }

    private boolean hasRequiredPermissions() {
        if (android.os.Build.VERSION.SDK_INT < 23) {
            return true;
        }

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return false;
        }

        if (android.os.Build.VERSION.SDK_INT >= 31) {
            return checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN)
                    == PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT)
                    == PackageManager.PERMISSION_GRANTED;
        }

        return true;
    }

    private void requestRequiredPermissions() {
        if (android.os.Build.VERSION.SDK_INT < 23) {
            return;
        }

        java.util.ArrayList<String> permissions =
                new java.util.ArrayList<String>();

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }

        if (android.os.Build.VERSION.SDK_INT >= 31) {
            if (checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN)
                    != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.BLUETOOTH_SCAN);
            }
            if (checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT)
                    != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.BLUETOOTH_CONNECT);
            }
        }

        if (!permissions.isEmpty()) {
            requestPermissions(
                    permissions.toArray(new String[permissions.size()]),
                    REQUEST_LOCATION
            );
        }
    }

    private void startScan() {

        if (android.os.Build.VERSION.SDK_INT >= 23) {

            if (checkSelfPermission(
                    Manifest.permission.ACCESS_FINE_LOCATION)
				!= PackageManager.PERMISSION_GRANTED) {

                requestPermissions(
					new String[]{
						Manifest.permission.ACCESS_FINE_LOCATION,
						Manifest.permission.ACCESS_COARSE_LOCATION
					},
					REQUEST_LOCATION
                );

                statusText.setText(
					"Hãy cấp quyền vị trí để quét Wi-Fi."
                );

                return;
            }
        }

        wifiNames.clear();
        bluetoothNames.clear();

        resultsLayout.removeAllViews();

        radarView.startSweep();

        statusText.setText(
			"Đang quét Wi-Fi + Bluetooth..."
        );

        scanButton.setEnabled(false);
        scanButton.setText("ĐANG QUÉT...");

        try {
            wifiManager.startScan();
        } catch (Exception e) {
        }

        if (bluetoothAdapter != null) {

            try {

                if (!bluetoothAdapter.isEnabled()) {

                    Intent enableBluetooth =
						new Intent(
						BluetoothAdapter.ACTION_REQUEST_ENABLE
					);

                    startActivityForResult(
						enableBluetooth,
						200
                    );

                } else {

                    bluetoothAdapter.cancelDiscovery();

                    bluetoothAdapter.startDiscovery();
                }

            } catch (SecurityException e) {
            }
        }

        new android.os.Handler().postDelayed(
			new Runnable() {
				@Override
				public void run() {

					showWifiResults();

					scanButton.setEnabled(true);
					scanButton.setText("QUÉT LẠI");

					radarView.stopSweep();

					updateStatus();
				}
			},
			12000
        );
    }

    private void showWifiResults() {

        if (wifiManager == null) {
            return;
        }

        try {

            List<ScanResult> results =
				wifiManager.getScanResults();

            Collections.sort(
				results,
				new Comparator<ScanResult>() {
					@Override
					public int compare(
						ScanResult a,
						ScanResult b) {

						return Integer.compare(
							b.level,
							a.level
						);
					}
				}
            );

            wifiNames.clear();

            resultsLayout.removeAllViews();

            addHeader(
				"📶  WI-FI (" +
				results.size() +
				")"
            );

            for (ScanResult result : results) {

                String ssid = result.SSID;

                if (ssid == null ||
					ssid.trim().isEmpty()) {

                    ssid = "Mạng Wi-Fi ẩn";
                }

                wifiNames.add(ssid);

                addRow(
					ssid +
					"    " +
					result.level +
					" dBm"
                );

                radarView.addBlip();
            }

            addHeader(
				"🔵  BLUETOOTH (" +
				bluetoothNames.size() +
				")"
            );

            for (String name : bluetoothNames) {
                addRow(name);
            }

            updateStatus();

        } catch (SecurityException e) {

            statusText.setText(
				"Chưa được cấp quyền Wi-Fi."
            );
        }
    }

    private void addHeader(String text) {

        TextView textView =
			new TextView(this);

        textView.setText(text);
        textView.setTextColor(0xFF39FF78);
        textView.setTextSize(16);
        textView.setPadding(
			4, 10, 4, 6
        );

        resultsLayout.addView(textView);
    }

    private void addRow(String text) {

        TextView textView =
			new TextView(this);

        textView.setText(
			"  • " + text
        );

        textView.setTextColor(
			0xFFD8FFE4
        );

        textView.setTextSize(14);

        textView.setPadding(
			4, 5, 4, 5
        );

        resultsLayout.addView(textView);
    }

    private void updateStatus() {

        statusText.setText(
			"Wi-Fi: " +
			wifiNames.size() +
			"    |    Bluetooth: " +
			bluetoothNames.size()
        );
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

        try {
            unregisterReceiver(wifiReceiver);
        } catch (Exception e) {
        }

        try {
            unregisterReceiver(bluetoothReceiver);
        } catch (Exception e) {
        }
    }
}
