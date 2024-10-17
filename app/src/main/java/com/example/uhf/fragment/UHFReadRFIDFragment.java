package com.example.uhf.fragment;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.fragment.app.FragmentManager;

import com.example.uhf.ConnectHelper;
import com.example.uhf.R;
import com.example.uhf.UhfInfo;
import com.example.uhf.activity.UHFMainActivity;
import com.example.uhf.tools.NumberTool;
import com.example.uhf.tools.StringUtils;
import com.example.uhf.tools.UIHelper;
import com.rscja.deviceapi.RFIDWithUHFUART;
import com.rscja.deviceapi.entity.UHFTAGInfo;

import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;


public class UHFReadRFIDFragment extends KeyDwonFragment {
    private static final String TAG = "UHFReadRFIDFragment";
    private boolean loopFlag = false;
    private int inventoryFlag = 1;
    private List<String> tempDatas = new ArrayList<>();
    UHFReadRFIDFragment.MyAdapter adapter;
    Connection connect;
    String ConnectionRessult="";

    ImageView distance;
    Spinner item_distance;

    TextView data_rfid;
    Button BtInventory;

    TextView ondata_rfid;
    Button   btn_ondata;
    TextView status_scan;

    public String id_rfid_time;
    //ListView LvTags;
    private UHFMainActivity mContext;
    private HashMap<String, String> map;

    private int total;
    private long time;

    //  private CheckBox cbFilter;
    //  private ViewGroup layout_filter;

    public static final String TAG_EPC = "tagEPC";
    public static final String TAG_EPC_TID = "tagEpcTID";
    public static final String TAG_COUNT = "tagCount";
    public static final String TAG_RSSI = "tagRssi";
    String usercode;
    String RFID_VALUE;
    String last_rfid = "1";
    String rfid = "2";
    int Flag_timer = 0;
    int count_timer = 0;
    int keep_alive = 0;
    private Handler handlertimer = new Handler();
    private Runnable runnable;

// CheckBox cbEPC_Tam;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            UHFTAGInfo info = (UHFTAGInfo) msg.obj;
            //addDataToList(info.getEPC(),mergeTidEpc(info.getTid(), info.getEPC(),info.getUser()), info.getRssi());
            mergeTidEpc(info.getTid(), info.getEPC(),info.getUser());
            //setTotalTime();
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(TAG, "UHFReadRFIDFragment.onCreateView");
        return inflater.inflate(R.layout.uhf_readrfid_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.i(TAG, "UHFReadRFIDFragment.onActivityCreated");
        super.onActivityCreated(savedInstanceState);
        mContext = (UHFMainActivity) getActivity();
        mContext.currentFragment=this;

        //tvTime = (TextView) getView().findViewById(R.id.tvTime);
        //tvTime.setText("0s");
        data_rfid = (TextView) getView().findViewById(R.id.getdata_rfid);


        distance = (ImageView) getView().findViewById(R.id.distance);

        item_distance = (Spinner) getView().findViewById(R.id.item_distance);

        ondata_rfid = (TextView) getView() .findViewById(R.id.ondata_rfid);


        status_scan = (TextView) getView() .findViewById(R.id.status_scan);



        distance.setImageResource(R.drawable.ic_baseline_compare_arrows_24);

       // BtInventory = (Button) getView().findViewById(R.id.BtInventory);


        ConnectSQL(requireContext());

        runnable = new Runnable() {
            @Override
            public void run() {
                count_timer ++;
                keep_alive ++ ;
                if(count_timer == 6){
                    Flag_timer = 1;
                    count_timer = 0;
                }
                if(keep_alive == 600){
                    keep_alive = 0;
                    try {
                        if (connect != null) {
                            String query = "SELECT 1";
                            Statement st = connect.createStatement();
                            Log.d("TAG",query);
                            int rowsInserted = st.executeUpdate(query);
                            if (rowsInserted > 0) {
                                Log.d("TAG", "Keep alive successfully");
                            } else {
                                Log.d("TAG", "Keep alive failed");
                            }
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        Log.d("TAG", " failed");
                    }
                }
                handlertimer.postDelayed(this, 500);
            }
        };

        // Bắt đầu timer khi Activity được tạo
        handlertimer.postDelayed(runnable, 500);


        String[] items_distance = {"1", "2", "3", "4", "5", "6", "7", "8","9","10","11", "12", "13", "14", "15", "16", "17", "18","19","20","21", "22", "23", "24", "25", "26", "27", "28","29","30"};

        Bundle bundle = getArguments();
        if (bundle != null) {
//            String user = bundle.getString("user");
//            String userrfid = bundle.getString("userrfid");
            usercode = bundle.getString("usercode");
//
//            Log.d("user", user);
//            Log.d("userrfid", userrfid);
            Log.d("usercode", usercode);
            // Bây giờ bạn có thể sử dụng dữ liệu trong biến 'data' theo ý muốn.
        }


        int index_Power = mContext.mReader.getPower();
        int defaultPosition = Arrays.asList(items_distance).indexOf(String.valueOf(index_Power));

        ArrayAdapter<String> adapter_distance = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, items_distance);
        adapter_distance.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);


        item_distance.setAdapter(adapter_distance);

        item_distance.setSelection(defaultPosition);

        item_distance.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Xử lý khi một mục được chọn
                String selectedItem = parent.getItemAtPosition(position).toString();
                //Toast.makeText(requireContext(), "Đã chọn: " + selectedItem, Toast.LENGTH_SHORT).show();
                if (mContext.mReader.setPower(Integer.parseInt(selectedItem))) {

                    UIHelper.ToastMessage(mContext, R.string.power_distance_succ);
                } else {
                    UIHelper.ToastMessage(mContext, R.string.power_distance_fail);
//            mContext.playSound(2);
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Xử lý khi không có mục nào được chọn
            }
        });


    }

    public void ConnectSQL(Context context) {
        ConnectHelper connectHelper = new ConnectHelper();
        connect = connectHelper.connectionclass(context);
    }
    public void insertData(String time_scan, String c72_id, String rfid) {
        try {
            if (connect != null) {
                String query = "INSERT INTO scanDataValue (PATNO, PATNAME, BLOCK_LINE," +
                        " KE, NGAN, SNP, SO_TO, KE_SVN, RFID, ROUND, USERNAME, LEDNO, SCAN_TIME, DEVICE_ID,SCAN_DAY) " +
                        "SELECT cv.PATNO as PATNO,cv.PATNAME AS PATNAME,cv.BLOCK_LINE AS BLOCK_LINE," +
                        "cv.KE AS KE,cv.NGAN AS NGAN,cv.SNP AS SNP,cv.SO_TO AS SO_TO,cv.KE_SVN AS KE_SVN,cv.RFID_PRODUCT AS RFID,sr.ROUND AS ROUND," +
                        "ud.USER_NAME as USERNAME,ld.LED AS LEDNO,'"+time_scan+"' AS SCAN_TIME,'"+c72_id.substring(0, 3)+"' AS DEVICE_ID,CONVERT(VARCHAR, GETDATE(), 111) AS SCAN_DAY FROM CODE_VALUE cv JOIN LED_DATA ld ON cv.PATNAME = ld.PATNAME JOIN SCAN_ROUND sr ON sr.START_TIME <= '"+time_scan+"' AND '"+time_scan+"' < sr.END_TIME JOIN USER_DATA ud ON ud.MNV = "+usercode+" WHERE cv.RFID_PRODUCT = '"+rfid+"'";
                Statement st = connect.createStatement();
                Log.d("TAG",query);
                int rowsInserted = st.executeUpdate(query);
                if (rowsInserted > 0) {
                    Log.d("TAG", "Data inserted successfully");
                } else {
                    Log.d("TAG", "Data insertion failed");
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.d("TAG", " failed");
        }
    }

    @Override
    public void onPause() {
        Log.i(TAG, "UHFReadRFIDFragment.onPause");
        super.onPause();

        stopInventory();
    }


    private void addDataToList(String epc,String epcAndTidUser, String rssi) {
        if (StringUtils.isNotEmpty(epc)) {
            int index = checkIsExist(epc);
            map = new HashMap<String, String>();
            map.put(TAG_EPC, epc);
            map.put(TAG_EPC_TID, epcAndTidUser);
            map.put(TAG_COUNT, String.valueOf(1));
            map.put(TAG_RSSI, rssi);
            if (index == -1) {
                mContext.tagList.add(map);
                tempDatas.add(epc);
                //tv_count.setText(String.valueOf(adapter.getCount()));
            } else {
                int tagCount = Integer.parseInt(mContext.tagList.get(index).get(TAG_COUNT), 10) + 1;
                map.put(TAG_COUNT, String.valueOf(tagCount));
                map.put(TAG_EPC_TID, epcAndTidUser);
                mContext.tagList.set(index, map);
            }
            //tv_total.setText(String.valueOf(++total));
            //adapter.notifyDataSetChanged();

            //----------
            mContext.uhfInfo.setTempDatas(tempDatas);
            mContext.uhfInfo.setTagList(mContext.tagList);
            mContext.uhfInfo.setCount(total);
            //mContext.uhfInfo.setTagNumber(adapter.getCount());
        }
    }




    private void clearData() {
        //tv_count.setText("0");
        //tv_total.setText("0");
        //tvTime.setText("0s");
        data_rfid.setText("");
        total = 0;
        mContext.tagList.clear();
        tempDatas.clear();


        //adapter.notifyDataSetChanged();
    }



    public class BtInventoryClickListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            readTag();
        }
    }

    private void readTag() {
        //   cbFilter.setChecked(false);
        if (status_scan.getText().equals(mContext.getString(R.string.btInventory))) {// 识别标签
            switch (inventoryFlag) {
                case 0:// 单步
                    time = System.currentTimeMillis();
                    UHFTAGInfo uhftagInfo = mContext.mReader.inventorySingleTag();
                    if (uhftagInfo != null) {
                        String tid = uhftagInfo.getTid();
                        String epc = uhftagInfo.getEPC();
                        String user=uhftagInfo.getUser();
                        mergeTidEpc(tid, epc, user);

                    } else {
                        UIHelper.ToastMessage(mContext, R.string.uhf_msg_inventory_fail);
//					mContext.playSound(2);
                    }
                    break;
                case 1:// 单标签循环
                    if (mContext.mReader.startInventoryTag()) {
                        //BtInventory.setText(mContext.getString(R.string.title_stop_Inventory));
                        status_scan.setText(R.string.title_stop_Inventory);
                        loopFlag = true;
                        //setViewEnabled(false);
                        time = System.currentTimeMillis();
                        new UHFReadRFIDFragment.TagThread().start();
                    } else {
                        stopInventory();
                        UIHelper.ToastMessage(mContext, R.string.uhf_msg_inventory_open_fail);
//					mContext.playSound(2);
                    }
                    break;
                default:
                    break;
            }
        } else {// 停止识别
            stopInventory();
            setTotalTime();
        }
    }

    private void setTotalTime() {
        float useTime = (System.currentTimeMillis() - time) / 1000.0F;
       // tvTime.setText(NumberTool.getPointDouble(1, useTime) + "s");
    }

    private void setViewEnabled(boolean enabled) {
//        RbInventorySingle.setEnabled(enabled);
//        RbInventoryLoop.setEnabled(enabled);
        //  cbFilter.setEnabled(enabled);
        //   btnSetFilter.setEnabled(enabled);
        //BtClear.setEnabled(enabled);
        // cbEPC_Tam.setEnabled(enabled);
    }

    /**
     * 停止识别
     */
    private void stopInventory() {
        if (loopFlag) {
            loopFlag = false;
            //setViewEnabled(true);
            if (mContext.mReader.stopInventory()) {
                status_scan.setText(R.string.btInventory);
                //BtInventory.setText(mContext.getString(R.string.btInventory));
            } else {
                UIHelper.ToastMessage(mContext, R.string.uhf_msg_inventory_stop_fail);
            }
        }
    }

    /**
     * 判断EPC是否在列表中
     *
     * @param epc 索引
     * @return
     */
    public int checkIsExist(String epc) {
        if (StringUtils.isEmpty(epc)) {
            return -1;
        }
        return binarySearch(tempDatas, epc);
    }

    /**
     * 二分查找，找到该值在数组中的下标，否则为-1
     */
    static int binarySearch(List<String> array, String src) {
        int left = 0;
        int right = array.size() - 1;
        // 这里必须是 <=
        while (left <= right) {
            if (compareString(array.get(left), src)) {
                return left;
            } else if (left != right) {
                if (compareString(array.get(right), src))
                    return right;
            }
            left++;
            right--;
        }
        return -1;
    }

    static boolean compareString(String str1, String str2) {
        if (str1.length() != str2.length()) {
            return false;
        } else if (str1.hashCode() != str2.hashCode()) {
            return false;
        } else {
            char[] value1 = str1.toCharArray();
            char[] value2 = str2.toCharArray();
            int size = value1.length;
            for (int k = 0; k < size; k++) {
                if (value1[k] != value2[k]) {
                    return false;
                }
            }
            return true;
        }
    }

    class TagThread extends Thread {
        public void run() {
            UHFTAGInfo uhftagInfo;
            Message msg;
            while (loopFlag) {
                uhftagInfo = mContext.mReader.readTagFromBuffer();
                if (uhftagInfo != null) {
                    msg = handler.obtainMessage();
                    msg.obj = uhftagInfo;
                    handler.sendMessage(msg);
                    //mContext.playSound(1);
                }
            }
        }
    }

    private String mergeTidEpc(String tid, String epc,String user) {
        String data=""+ epc;
        if (!TextUtils.isEmpty(tid) && !tid.equals("0000000000000000") && !tid.equals("000000000000000000000000")) {
            data+= "\nTID:" + tid ;
        }
        if(user!=null && user.length()>0) {
            data+="\nUSER:"+user;
        }
        if(Flag_timer == 0){
            last_rfid = rfid;
        }
        else {
            last_rfid = "0";
            Flag_timer = 0;
        }
        rfid = data;
        Log.d("MergeTidEpc", "Last RFID: " + last_rfid);
        Log.d("MergeTidEpc", "Current RFID: " + rfid);
        if(last_rfid.equals(rfid)){
            Log.d("MergeTidEpc", "Trung nhau");
        }
        else{
            data_rfid.setText(data);
            String deviceId = getDeviceId(getContext());
            insertData(getCurrentTimeUsingCalendar(),deviceId,data);
            mContext.playSound(1);
            count_timer = 0;
        }
        return  data;
    }

    public static String getDeviceId(Context context) {
        // Lấy ID thiết bị
        String id = Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        int hashedId = id.hashCode();
        hashedId = Math.abs(hashedId);

        int result = hashedId % 1000;
        Log.d("Id",String.valueOf(result));
        return String.valueOf(result);
    }
    public String getCurrentTimeUsingCalendar() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        // Định dạng thời gian thành chuỗi (vd: 09:30:)
        String formattedTime = String.format("%02d:%02d:%02d", hour, minute,second);
        return formattedTime;
    }

    @Override
    public void myOnKeyDwon() {
        readTag();

    }


    //-----------------------------
    private int  selectItem=-1;
    public final class ViewHolder {
        public TextView tvEPCTID;
        public TextView tvTagCount;
        public TextView tvTagRssi;
    }

    public class MyAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        public MyAdapter(Context context) {
            this.mInflater = LayoutInflater.from(context);
        }
        public int getCount() {
            // TODO Auto-generated method stub
            return mContext.tagList.size();
        }
        public Object getItem(int arg0) {
            // TODO Auto-generated method stub
            return mContext.tagList.get(arg0);
        }
        public long getItemId(int arg0) {
            // TODO Auto-generated method stub
            return arg0;
        }
        public View getView(int position, View convertView, ViewGroup parent) {
            UHFReadRFIDFragment.ViewHolder holder = null;
            if (convertView == null) {
                holder = new UHFReadRFIDFragment.ViewHolder();
                convertView = mInflater.inflate(R.layout.listtag_items, null);
                holder.tvEPCTID = (TextView) convertView.findViewById(R.id.TvTagUii);
                holder.tvTagCount = (TextView) convertView.findViewById(R.id.TvTagCount);
                holder.tvTagRssi = (TextView) convertView.findViewById(R.id.TvTagRssi);
                convertView.setTag(holder);
            } else {
                holder = (UHFReadRFIDFragment.ViewHolder) convertView.getTag();
            }

            holder.tvEPCTID.setText((String) mContext.tagList.get(position).get(TAG_EPC_TID));
            holder.tvTagCount.setText((String) mContext.tagList.get(position).get(TAG_COUNT));
            holder.tvTagRssi.setText((String) mContext.tagList.get(position).get(TAG_RSSI));

            if (position == selectItem) {
                convertView.setBackgroundColor(mContext.getResources().getColor(R.color.lfile_colorPrimary));
            }
            else {
                convertView.setBackgroundColor(Color.TRANSPARENT);
            }
            return convertView;
        }
        public  void setSelectItem(int select) {
            if(selectItem==select){
                selectItem=-1;
                mContext.uhfInfo.setSelectItem("");
                mContext.uhfInfo.setSelectIndex(selectItem);
            }else {
                selectItem = select;
                mContext.uhfInfo.setSelectItem(mContext.tagList.get(select).get(TAG_EPC));
                mContext.uhfInfo.setSelectIndex(selectItem);
            }

        }
    }

}
