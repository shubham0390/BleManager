
package com.km2.blemanager.scan;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.km2.blemanager.connection.DeviceConnectionActivity;
import com.km2.blemanager.utils.ItemClickSupport;
import com.km2.blemanager.R;

public class DeviceScanActivity extends AppCompatActivity implements BleScanManager.ScanListener {

    private LeDeviceListAdapter mLeDeviceListAdapter;

    private static final int REQUEST_ENABLE_BT = 1;

    private BleScanManager mBleScanManager;

    private RecyclerView mRecyclerView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scan_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.title_devices);
        mRecyclerView = (RecyclerView) findViewById(R.id.deviceList);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mBleScanManager = new BleScanManager(this);
        mBleScanManager.setScanListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        if (!mBleScanManager.isScanning()) {
            menu.findItem(R.id.menu_stop).setVisible(false);
            menu.findItem(R.id.menu_scan).setVisible(true);
            menu.findItem(R.id.menu_refresh).setActionView(null);
        } else {
            menu.findItem(R.id.menu_stop).setVisible(true);
            menu.findItem(R.id.menu_scan).setVisible(false);
            menu.findItem(R.id.menu_refresh).setActionView(R.layout.actionbar_indeterminate_progress);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_scan:
                mLeDeviceListAdapter.clear();
                mBleScanManager.toggleScan(true);
                break;
            case R.id.menu_stop:
                mBleScanManager.toggleScan(false);
                break;
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

       /* // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
*/
        // Initializes list view adapter.
        mLeDeviceListAdapter = new LeDeviceListAdapter(this);
        mRecyclerView.setAdapter(mLeDeviceListAdapter);
        mBleScanManager.toggleScan(true);
        ItemClickSupport.addTo(mRecyclerView).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                onListItemClick(position);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mBleScanManager.toggleScan(false);
        mLeDeviceListAdapter.clear();
    }


    protected void onListItemClick(int position) {
        final BluetoothDevice device = mLeDeviceListAdapter.getDevice(position);
        if (device == null) return;
        final Intent intent = new Intent(this, DeviceConnectionActivity.class);
        intent.putExtra(DeviceConnectionActivity.EXTRAS_DEVICE_NAME, device.getName());
        intent.putExtra(DeviceConnectionActivity.EXTRAS_DEVICE_ADDRESS, device.getAddress());
        if (mBleScanManager.isScanning()) {
            mBleScanManager.toggleScan(false);
        }
        startActivity(intent);
    }

    @Override
    public void onScanStart() {
        supportInvalidateOptionsMenu();
    }

    @Override
    public void onScanStop() {
        supportInvalidateOptionsMenu();
    }

    @Override
    public void onDeviceFound(final BluetoothDevice device, int rssi, byte[] scanRecord) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mLeDeviceListAdapter.addDevice(device);
                mLeDeviceListAdapter.notifyDataSetChanged();
            }
        });
    }

}