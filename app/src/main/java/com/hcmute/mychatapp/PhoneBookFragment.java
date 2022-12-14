package com.hcmute.mychatapp;

import static android.content.Context.MODE_PRIVATE;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import com.hcmute.mychatapp.Pattern.User_SingeTon;
import com.hcmute.mychatapp.adapter.PhonebookAdapter;
import com.hcmute.mychatapp.model.LoginHistory;
import com.hcmute.mychatapp.model.PhoneBook;
import com.hcmute.mychatapp.model.User;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PhoneBookFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PhoneBookFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public PhoneBookFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment BlankFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PhoneBookFragment newInstance(String param1, String param2) {
        PhoneBookFragment fragment = new PhoneBookFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }
    private View view;
    private ImageView btn_addFriend,btn_updatePhonebook;
    private TextView textviewTimeUpdate;
    private ListView listviewPhonebook;
    PhonebookAdapter adapter;
    ArrayList<PhoneBook> phoneBookList;
    String id, name, phone,timeUpdate;
    //G???i m???u singleton l???y ra user
    User_SingeTon user_singeTon = User_SingeTon.getInstance();
    User user = user_singeTon.getUser();
    //D??ng ????? l??u th???i gian l???n g???n nh???t c???p nh???p t??? danh b???
    SharedPreferences sharedPreferences;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_phone_book, container, false);
        //??nh x??? c??c view
        btn_updatePhonebook = (ImageView) view.findViewById(R.id.btn_updatePhonebook);
        textviewTimeUpdate = (TextView) view.findViewById(R.id.textviewTimeUpdate);
        listviewPhonebook = (ListView) view.findViewById(R.id.listviewPhonebook);

        //N???u kh??ng c?? user tr??? v??? trang login
        if(user == null) {
            startActivity(new Intent(getActivity(), loginActivity.class));
            getActivity().finish();
        }

        //Khai b??o m???ng ????? l??u danh b???
        phoneBookList = new ArrayList<>();
        //Load th??ng tin v??o listview
        adapter = new PhonebookAdapter(getActivity(),R.layout.phonebook_row,phoneBookList);
        listviewPhonebook.setAdapter(adapter);
        //L???y danh s??ch ng?????i d??ng trong danh b??? m?? c?? t??i kho???n
        getListPhoneBook();

        //B???m v??o ng?????i d??ng ????? ?????n trang profile c???a h???
        listviewPhonebook.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("dataCookie", Context.MODE_MULTI_PROCESS);
                sharedPreferences.edit().putString("user_id", phoneBookList.get(i).getPhonebookNumber()).commit();
                startActivity(new Intent(getActivity(), ViewUserPageActivity.class));
            }
        });
        //L???y th???i gian update g???n nh???t ????? hi???n th???
        sharedPreferences = getActivity().getSharedPreferences("dataTimePhonebook",MODE_PRIVATE);
        timeUpdate = sharedPreferences.getString("timeUpdate","");
        textviewTimeUpdate.setText(timeUpdate);

        //B???m v??o n??t update ????? l???y t???t c??? s??? ??i???n tho???i t??? danh b???
        btn_updatePhonebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //L???y th???i gian c???p nh???p
                Date currentTime = Calendar.getInstance().getTime();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                timeUpdate = dateFormat.format(currentTime);
                //L??u th???i gian c???p nh???p l??n sharedPreferences
                sharedPreferences = getActivity().getSharedPreferences("dataTimePhonebook",MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("timeUpdate",timeUpdate);
                editor.commit();
                textviewTimeUpdate.setText(timeUpdate);

                //Ti???n h??nh t??m ki???m tr??n FirebaseDatabase
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference myPhoneBookRef = database.getReference("PhoneBook");
                myPhoneBookRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //N???u ???? c?? danh b??? c?? th?? x??a ??i
                        if(snapshot.child(user.getPhone()).exists()){
                            //X??a danh b??? c??
                            myPhoneBookRef.child(user.getPhone()).removeValue();
                        }

                        //Xin quy???n truy c???p
                        checkPermission();
                        //getContactListFromPhone();

                        Toast.makeText(getActivity(), "Update success!!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

        });

        return view;
    }
    //Ki???m tra c?? ???????c quy???n ?????c danh b??? kh??ng
    private void checkPermission(){
        //Xin quy???n truy c???p
        if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_CONTACTS)
        != PackageManager.PERMISSION_GRANTED){
            //khi kh??ng ???????c quy???n truy c???p
            ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.READ_CONTACTS},1);
        }
        else {
            getContactListFromPhone();
        }
    }
    private void getContactListFromPhone(){
        //L???y ???????ng d???n truy c???p danh b???
        Uri uri = ContactsContract.Contacts.CONTENT_URI;
        //L???y t???t c??? s??? ??i???n tho???i
        Cursor cursor = getActivity().getContentResolver().query(uri,null,null,null,null);
        //N???u s??? l?????ng l???y ra l???n h??n 0

//        if(cursor.moveToFirst()){
            while (cursor.moveToNext()){

                //L???y t??n c???a s??? ??i???n tho???i
                id = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
                //L???y t??n c???a s??? ??i???n tho???i
                name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));
                //L???y s??? ??i???n tho???i
                Uri uriPhone = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
                String selection = ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?";
                Cursor phoneCursor = getActivity().getContentResolver().query(
                        uriPhone,null,selection,new String[]{id},null
                );
                //Ki???m tra c?? s??? ??i???n tho???i
                if(phoneCursor.moveToNext()) {
                    phone = phoneCursor.getString(
                            phoneCursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));

                    //B??? h???t kho???ng tr???ng trong sdt l???y ???????c

                    String phone_number = phone.replace(" ", "");
                    StringBuffer s_phone = new StringBuffer();
                    //T??ch s??? ra kh???i chu???i
                    for (int i = 0; i < phone_number.length(); i++) {
                        if(Character.isDigit(phone_number.charAt(i)))
                            s_phone.append(phone_number.charAt(i));
                    }
                    String phone_name = name.toString();
                    //Ti???n h??nh t??m ki???m tr??n FirebaseDatabase
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference myRef = database.getReference("users");
                    //?????c d??? li???u
                    myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            //N???u s??? ??i???n tho???i trong danh b??? ???? ????ng k?? t??i kho???n
                            if (snapshot.child(s_phone.toString()).exists()) {
                                //Th??m v??o database
                                PhoneBook phoneBook = new PhoneBook(user.getPhone(), phone_name, s_phone.toString());
                                DatabaseReference myPhoneBookRef = database.getReference("PhoneBook");
                                myPhoneBookRef.child(user.getPhone()).child(s_phone.toString()).setValue(phoneBook);
                                //Hi???n th??? ng?????i d??ng ???????c l??u trong danh b??? tr??n database
                                getListPhoneBook();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }
        cursor.close();
    }
    //hi???n th??? nh???ng sdt trong database
    private void getListPhoneBook(){
        //K???t n???i c?? s??? d??? li???u v?? truy xu???t v??o b???ng l???ch s??? ????ng nh???p
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef1 = database.getReference("PhoneBook");
        //D??ng m???u thi???t k??? singleTon ????? l??u l???i user sau khi login
        user_singeTon = User_SingeTon.getInstance();
        user = user_singeTon.getUser();
        myRef1.child(user.getPhone()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //X??a m???ng c??
                phoneBookList.clear();
                //L???y danh b??? c???a ng?????i d??ng t??? database v?? th??m v??o m???ng
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    PhoneBook phoneBook = dataSnapshot.getValue(PhoneBook.class);
                    phoneBookList.add(phoneBook);
                }
                adapter.notifyDataSetChanged();
            }
            //Kh??ng l???y ???????c d??? li???u v?? th??ng b??o
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), "Get phonebook failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //Khi ???????c quy???n truy c???p
        if(requestCode == 1 && grantResults.length > 0 && grantResults[0]
        == PackageManager.PERMISSION_GRANTED){
            getContactListFromPhone();
        }
        else{
            //Kh??ng ???????c quy???n truy c???p -th??ng b??o
            Toast.makeText(getActivity(), "Permission denied", Toast.LENGTH_SHORT).show();
            checkPermission();
        }
    }

}