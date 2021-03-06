package com.sharukhhasan.androidbluetoothscanner.ui;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.os.Parcelable;

import com.github.ybq.android.spinkit.style.FadingCircle;
import com.sharukhhasan.androidbluetoothscanner.util.DeviceItem;
import com.sharukhhasan.androidbluetoothscanner.R;
import com.sharukhhasan.androidbluetoothscanner.util.DeviceListAdapter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

/**
 * Created by sharukhhasan on 6/1/16.
 */
public class DeviceListFragment extends Fragment implements AbsListView.OnItemClickListener{
    public static final String UUIDTAG = "UUID";
    private ArrayList <DeviceItem>deviceItemList;
    private OnFragmentInteractionListener mListener;
    private static BluetoothAdapter bTAdapter;
    private AbsListView mListView;
    private ArrayAdapter<DeviceItem> mAdapter;
    private ParcelUuid[] uuids;
    private ArrayList<UUID> uuidList;
    private static Method getUuids;
    private Parcelable[] uuidExtra;

    private final BroadcastReceiver bReciever = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action))
            {
                Log.d("DEVICELIST", "Bluetooth device found\n");
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                uuidExtra = intent.getParcelableArrayExtra(BluetoothDevice.EXTRA_UUID);

                DeviceItem newDevice = new DeviceItem(device.getName(), device.getAddress(), "false");
                mAdapter.add(newDevice);
                mAdapter.notifyDataSetChanged();
            }
        }
    };

    public static DeviceListFragment newInstance(BluetoothAdapter adapter)
    {
        DeviceListFragment fragment = new DeviceListFragment();
        bTAdapter = adapter;
        try {
            getUuids = BluetoothAdapter.class.getDeclaredMethod("getUuids", null);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return fragment;
    }

    public DeviceListFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Log.d("DEVICELIST", "Super called for DeviceListFragment onCreate\n");
        deviceItemList = new ArrayList<DeviceItem>();
        uuidList = new ArrayList<UUID>();

        Set<BluetoothDevice> pairedDevices = bTAdapter.getBondedDevices();
        if(pairedDevices.size() > 0)
        {
            for(BluetoothDevice device : pairedDevices)
            {
                DeviceItem newDevice= new DeviceItem(device.getName(),device.getAddress(),"false");
                deviceItemList.add(newDevice);
            }
        }

        if(deviceItemList.size() == 0)
        {
            deviceItemList.add(new DeviceItem("No Devices", "", "false"));
        }

        try {
            uuids = (ParcelUuid[])getUuids.invoke(bTAdapter, null);

        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        Log.d("DEVICELIST", "DeviceList populated\n");
        mAdapter = new DeviceListAdapter(getActivity(), deviceItemList, bTAdapter);
        Log.d("DEVICELIST", "Adapter created\n");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_deviceitem_list, container, false);
        ToggleButton scan = (ToggleButton) view.findViewById(R.id.scan);
        final ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.spin_kit);
        FadingCircle fadingCircle = new FadingCircle();
        progressBar.setIndeterminateDrawable(fadingCircle);
        progressBar.setVisibility(View.INVISIBLE);

        mListView = (AbsListView) view.findViewById(android.R.id.list);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);

        scan.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                if (isChecked) {
                    mAdapter.clear();
                    getActivity().registerReceiver(bReciever, filter);
                    progressBar.setVisibility(View.VISIBLE);
                    bTAdapter.startDiscovery();
                } else {
                    getActivity().unregisterReceiver(bReciever);
                    progressBar.setVisibility(View.INVISIBLE);
                    bTAdapter.cancelDiscovery();
                }
            }
        });

        return view;
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);

        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        Log.d("DEVICELIST", "onItemClick position: " + position + " id: " + id + " name: "
                + deviceItemList.get(position).getDeviceName() + " address: " + deviceItemList.get(position).getAddress() + "\n");
        if(null != mListener)
        {
            mListener.onFragmentInteraction(deviceItemList.get(position).getDeviceName());
        }

    }

    public void setEmptyText(CharSequence emptyText)
    {
        View emptyView = mListView.getEmptyView();

        if(emptyView instanceof TextView)
        {
            ((TextView) emptyView).setText(emptyText);
        }
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(String id);
    }

}
