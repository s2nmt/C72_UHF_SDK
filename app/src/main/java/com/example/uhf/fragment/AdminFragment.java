package com.example.uhf.fragment;

import android.database.Cursor;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.uhf.DatabaseHelper;
import com.example.uhf.R;


public class AdminFragment extends Fragment {

    private EditText edtIP, edtPort, edtUsername, edtPassword, Name_server;
    private Button btnConfirm;
    private DatabaseHelper dbHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin, container, false);

        edtIP = view.findViewById(R.id.editTextTextPersonName);
        edtPort = view.findViewById(R.id.edt_port);
        edtUsername = view.findViewById(R.id.editTextTextPersonName2);
        edtPassword = view.findViewById(R.id.editTextTextPersonName3);
        Name_server = view.findViewById(R.id.Name_sever);
        btnConfirm = view.findViewById(R.id.button);

        dbHelper = new DatabaseHelper(getActivity());

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveDataToDatabase();
            }
        });
        displaySavedData();
        return view;
    }

    private void saveDataToDatabase() {
        String ip = edtIP.getText().toString().trim();
        String port = edtPort.getText().toString().trim();
        String username = edtUsername.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String nameServer = Name_server.getText().toString().trim();
        if (!ip.isEmpty() && !port.isEmpty() && !username.isEmpty() && !password.isEmpty() && !nameServer.isEmpty()) {
            long result = dbHelper.insertData(ip, port, username, password, nameServer); // Modify the insertData method to accept Name_sever
            if (result != -1) {
                Toast.makeText(getActivity(), "Lưu dữ liệu thành công", Toast.LENGTH_SHORT).show();
                UHFLoginFragment LoginFragment = new UHFLoginFragment();

                // Use a FragmentTransaction to replace the current fragment with HelloFragment
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, LoginFragment)
                        .addToBackStack(null)
                        .commit();
            } else {
                Toast.makeText(getActivity(), "Lưu dữ liệu thất bại", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getActivity(), "Vui lòng điền đủ thông tin", Toast.LENGTH_SHORT).show();
        }

    }
    private void displaySavedData() {
        Cursor cursor = dbHelper.getWritableDatabase().rawQuery(
                "SELECT * FROM " + DatabaseHelper.TABLE_NAME + " ORDER BY " + DatabaseHelper.COL_ID + " DESC LIMIT 1",
                null
        );

        if (cursor.moveToFirst()) {
            String ip = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COL_IP));
            String port = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COL_PORT));
            String username = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COL_USERNAME));
            String password = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COL_PASSWORD));
            String nameServer = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COL_NAME_SERVER)); // Add this line

            edtIP.setText(ip);
            edtPort.setText(port);
            edtUsername.setText(username);
            edtPassword.setText(password);
            Name_server.setText(nameServer); // Add this line
        }

        cursor.close();
    }

}