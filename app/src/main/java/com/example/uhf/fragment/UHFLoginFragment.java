package com.example.uhf.fragment;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
import android.widget.Toast;

import androidx.fragment.app.FragmentManager;

import com.example.uhf.ConnectHelper;
import com.example.uhf.DatabaseHelper;
import com.example.uhf.R;
import com.example.uhf.UhfInfo;
import com.example.uhf.activity.UHFMainActivity;
import com.example.uhf.tools.NumberTool;
import com.example.uhf.tools.StringUtils;
import com.example.uhf.tools.UIHelper;
import com.rscja.deviceapi.RFIDWithUHFUART;
import com.rscja.deviceapi.entity.UHFTAGInfo;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class UHFLoginFragment extends KeyDwonFragment {
    private static final String TAG = "UHFLoginFragment";
    private boolean loopFlag = false;
    private int inventoryFlag = 1;
    private List<String> tempDatas = new ArrayList<>();
    Connection connect;
    String ConnectionRessult="";

    MyAdapter adapter;
    Button BtClear;
    //TextView tvTime;
    // TextView tv_count;
    //TextView tv_total;
    ImageView employee_code;
    ImageView rfid_employee;
    ImageView factory;
    ImageView username;
    ImageView distance;
    Spinner item_factory;
    Spinner item_distance;
    EditText personName;
    TextView edt_rfid;
    EditText edt_code;

    Button BtInventory;
    Button BtnOk;

    TextView status_scan;
    //ListView LvTags;
    private UHFMainActivity mContext;
    private HashMap<String, String> map;

    private int total;
    private long time;
    private String RFID_VALUE;
    String MNV;
  //  private CheckBox cbFilter;
  //  private ViewGroup layout_filter;

    public static final String TAG_EPC = "tagEPC";
    public static final String TAG_EPC_TID = "tagEpcTID";
    public static final String TAG_COUNT = "tagCount";
    public static final String TAG_RSSI = "tagRssi";

// CheckBox cbEPC_Tam;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            UHFTAGInfo info = (UHFTAGInfo) msg.obj;
            //addDataToList(info.getEPC(),mergeTidEpc(info.getTid(), info.getEPC(),info.getUser()), info.getRssi());
            RFID_VALUE = mergeTidEpc(info.getTid(), info.getEPC(),info.getUser());
            setTotalTime();
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(TAG, "UHFLoginFragment.onCreateView");
        return inflater.inflate(R.layout.uhf_login_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.i(TAG, "UHFLoginFragment.onActivityCreated");
        super.onActivityCreated(savedInstanceState);
        mContext = (UHFMainActivity) getActivity();
        mContext.currentFragment=this;
        BtClear = (Button) getView().findViewById(R.id.BtClear);

        factory = (ImageView) getView().findViewById(R.id.factory);
        username = (ImageView) getView().findViewById(R.id.user);
        distance = (ImageView) getView().findViewById(R.id.distance);
        employee_code = (ImageView) getView() .findViewById(R.id.id_code);
        rfid_employee = (ImageView) getView() .findViewById(R.id.id_rfid);


        item_distance = (Spinner) getView().findViewById(R.id.item_distance);
        item_factory = (Spinner) getView() .findViewById(R.id.item_factory);
        personName = (EditText) getView() .findViewById(R.id.personName);
        edt_rfid = (TextView) getView() .findViewById(R.id.edt_rfid);
        edt_code = (EditText) getView() .findViewById(R.id.edt_code);



        status_scan = (TextView) getView() .findViewById(R.id.status_scan);
        String Name_person = "Minh Tuấn";
        //Logo.setImageResource(R.drawable.logo_yazaky);
        factory.setImageResource(R.drawable.ic_baseline_factory_24);
        username.setImageResource(R.drawable.ic_baseline_supervised_user_circle_24);
        distance.setImageResource(R.drawable.ic_baseline_compare_arrows_24);
        employee_code.setImageResource(R.drawable.ic_baseline_numbers_24);
        rfid_employee.setImageResource(R.drawable.ic_baseline_qr_code_scanner_24);

        //BtInventory = (Button) getView().findViewById(R.id.BtInventory);
        BtnOk = (Button) getView() .findViewById(R.id.ok);

        BtClear.setOnClickListener(new BtClearClickListener());
        try {
            ConnectSQL(requireContext());
        } catch (Exception e) {
            // Handle the exception here
            e.printStackTrace(); // Print the exception details to the console for debugging
            // You can also log the exception or show an error message to the user
        }

       // RgInventory.setOnCheckedChangeListener(new RgInventoryCheckedListener());
        //BtInventory.setOnClickListener(new BtInventoryClickListener());
//        List<String> factoryIPs = new ArrayList<>();
//        List<String> factoryNames = new ArrayList<>();
//        if(status_sql == 1){
//            Getdata_FactoryIPFromSQL(factoryIPs,factoryNames);
//        }

        List<String> factoryNames = new ArrayList<>();
        ArrayAdapter<String> adapter_factory = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, factoryNames);
        adapter_factory.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        item_factory.setAdapter(adapter_factory);

// Fetch the latest Name_server value from the database and add it to factoryNames
        DatabaseHelper dbHelper = new DatabaseHelper(requireContext());
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM connection_settings ORDER BY id DESC LIMIT 1", null);

        while (cursor.moveToNext()) {
            String nameServer = cursor.getString(cursor.getColumnIndex("name_server"));
            factoryNames.add(nameServer);
        }

        cursor.close();
        dbHelper.close();

// Notify the adapter that the data set has changed
        adapter_factory.notifyDataSetChanged();



        String[] items_distance = {"1", "2", "3", "4", "5", "6", "7", "8","9","10","11", "12", "13", "14", "15", "16", "17", "18","19","20","21", "22", "23", "24", "25", "26", "27", "28","29","30"};

//        ArrayAdapter<String> adapter_factory = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, factoryNames);
//        adapter_factory.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        item_factory.setAdapter(adapter_factory);

        ArrayAdapter<String> adapter_distance = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, items_distance);
        adapter_distance.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        item_distance.setAdapter(adapter_distance);


        item_factory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Xử lý khi một mục được chọn
                String selectedItem = parent.getItemAtPosition(position).toString();
                //Toast.makeText(requireContext(), "Đã chọn: " + selectedItem, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Xử lý khi không có mục nào được chọn
            }
        });

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
        BtnOk.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                if(String.valueOf(personName.getText()).equals("admin") && String.valueOf(edt_code.getText()).equals("1111")){
                    AdminFragment AdminFragment = new AdminFragment();

                    FragmentManager fragmentManager = getFragmentManager();
                    fragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, AdminFragment)
                            .addToBackStack(null)
                            .commit();
                }
                else if(Getdata_userRFIDFromSQL() == 1 || Getdata_userinfoFromSQL() == 1){
                    Bundle bundle = new Bundle();
//                    bundle.putString("user", String.valueOf(personName.getText()));
//                    bundle.putString("userrfid", String.valueOf(edt_rfid.getText()));
                    if(Getdata_userRFIDFromSQL() == 1){
                        bundle.putString("usercode", String.valueOf(MNV));
                    }
                    else if(Getdata_userinfoFromSQL() == 1){
                        bundle.putString("usercode", String.valueOf(edt_code.getText()));
                    }
                    edt_code.setText("");
                    personName.setText("");
                    edt_rfid.setText("");
                    UHFReadRFIDFragment UHFReadRFIDFragment = new UHFReadRFIDFragment();
                    UHFReadRFIDFragment.setArguments(bundle);

                    FragmentManager fragmentManager = getFragmentManager();
                    fragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, UHFReadRFIDFragment)
                            .addToBackStack(null)
                            .commit();
                }
                else {
                    CharSequence message = "Sai Thông Tin Đăng Nhập"; // Thay thế bằng thông điệp của bạn
                    int duration = Toast.LENGTH_SHORT; // Hoặc Toast.LENGTH_LONG
                    Toast toast = Toast.makeText(requireContext(), message, duration);
                    // Hiển thị Toast
                    edt_rfid.setText("Mã RFID");
                    edt_code.setText("");
                    personName.setText("");
                    toast.show();
                }
            }
        });
    }

    @Override
    public void onPause() {
        Log.i(TAG, "UHFLoginFragment.onPause");
        super.onPause();

        stopInventory();
    }
    public int ConnectSQL(Context context) {
        ConnectHelper connectHelper = new ConnectHelper();
        connect = connectHelper.connectionclass(context);
        if (connect == null) {
            // Connection failed, show a toast message
            Toast.makeText(context, "Không thể kết nối cơ sở dữ liệu", Toast.LENGTH_SHORT).show();
            return 0;
        }
        Toast.makeText(context, "Kết nối cơ sở dữ liệu thành công", Toast.LENGTH_SHORT).show();
        return 1;
    }

    public void Getdata_FactoryIPFromSQL(List<String> factoryIPs, List<String> factoryNames)
    {
        try {
            if(connect != null)
            {

                String query = "SELECT * FROM FACTORY_IP";
                Statement st = connect.createStatement();
                ResultSet rs = st.executeQuery(query);

                while(rs.next())
                {
                    String ip = rs.getString(2);
                    if(ip != null){
                        Log.d("NAme FACTORY",rs.getString(1));
                        Log.d("IP FACTORY",rs.getString(2));
                        factoryIPs.add(ip);
                        factoryNames.add(rs.getString(1));
                    }
                    else
                    {
                    }
                }
            }
            else
            {
                ConnectionRessult = "Check Connection";
            }

        }
        catch (Exception ex)
        {
        }
    }

    public int Getdata_userRFIDFromSQL()
    {
        try {

            if(connect != null)
            {
                String query = "SELECT MNV FROM USER_DATA WHERE RFID_USER=" + "'"+RFID_VALUE+"'";
                Statement st = connect.createStatement();
                ResultSet rs = st.executeQuery(query);

                while(rs.next())
                {

                    if(rs.getString(1) != null){
                        MNV = rs.getString(1);
                        return 1;
                    }
                    else
                    {
                        return 0;
                    }
                }
                return 0;
            }
            else
            {
                ConnectionRessult = "Check Connection";
                return 0;
            }

        }
        catch (Exception ex)
        {
            return 0;
        }
    }
    public int Getdata_userinfoFromSQL()
    {
        try {

            if(connect != null)
            {
                String query = "SELECT MNV,USER_NAME FROM USER_DATA WHERE MNV = " + edt_code.getText() + " AND USER_NAME = " +"'" + personName.getText() + "'";
                Log.d("Tag",query);
                Statement st = connect.createStatement();
                ResultSet rs = st.executeQuery(query);

                while(rs.next())
                {

                    if(rs.getString(1) != null){
                        return 1;
                    }
                    else
                    {
                        return 0;
                    }
                }
                return 0;
            }
            else
            {
                ConnectionRessult = "Check Connection";
                return 0;
            }

        }
        catch (Exception ex)
        {
            return 0;
        }
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

    public class BtClearClickListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            clearData();
            selectItem=-1;
            mContext.uhfInfo=new UhfInfo();
        }
    }


    private void clearData() {
        //tv_count.setText("0");
        //tv_total.setText("0");
        //tvTime.setText("0s");
        personName.setText("");
        edt_rfid.setText("Mã RFID");
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
                        RFID_VALUE = mergeTidEpc(tid, epc, user);
                        //addDataToList(epc,mergeTidEpc(tid, epc, user), uhftagInfo.getRssi());
                        setTotalTime();
                        mContext.playSound(1);
                    } else {
                        UIHelper.ToastMessage(mContext, R.string.uhf_msg_inventory_fail);
//					mContext.playSound(2);
                    }
                    break;
                case 1:// 单标签循环
                    if (mContext.mReader.startInventoryTag()) {
                        status_scan.setText(R.string.title_stop_Inventory);
                        //BtInventory.setText(mContext.getString(R.string.title_stop_Inventory));
                        loopFlag = true;
                        setViewEnabled(false);
                        time = System.currentTimeMillis();
                        new TagThread().start();
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
        //tvTime.setText(NumberTool.getPointDouble(1, useTime) + "s");
    }

    private void setViewEnabled(boolean enabled) {
//        RbInventorySingle.setEnabled(enabled);
//        RbInventoryLoop.setEnabled(enabled);
      //  cbFilter.setEnabled(enabled);
     //   btnSetFilter.setEnabled(enabled);
        BtClear.setEnabled(enabled);
       // cbEPC_Tam.setEnabled(enabled);
    }

    /**
     * 停止识别
     */
    private void stopInventory() {
        if (loopFlag) {
            loopFlag = false;
            setViewEnabled(true);
            if (mContext.mReader.stopInventory()) {
                //BtInventory.setText(mContext.getString(R.string.btInventory));
                status_scan.setText(getString(R.string.btInventory));
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
                    mContext.playSound(1);
                }
            }
        }
    }

    private String mergeTidEpc(String tid, String epc,String user) {
        String data=epc;
        if (!TextUtils.isEmpty(tid) && !tid.equals("0000000000000000") && !tid.equals("000000000000000000000000")) {
            data+= "\nTID:" + tid ;
        }
        if(user!=null && user.length()>0) {
            data+="\nUSER:"+user;
        }
        edt_rfid.setText(data);

        stopInventory();
        Log.d("TAG", data);
        return  data;
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
            ViewHolder holder = null;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.listtag_items, null);
                holder.tvEPCTID = (TextView) convertView.findViewById(R.id.TvTagUii);
                holder.tvTagCount = (TextView) convertView.findViewById(R.id.TvTagCount);
                holder.tvTagRssi = (TextView) convertView.findViewById(R.id.TvTagRssi);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
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
