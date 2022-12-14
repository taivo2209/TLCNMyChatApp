package com.hcmute.mychatapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import com.hcmute.mychatapp.Pattern.User_SingeTon;
import com.hcmute.mychatapp.adapter.UserAdapter;
import com.hcmute.mychatapp.model.Friends;
import com.hcmute.mychatapp.model.Message;
import com.hcmute.mychatapp.model.Participants;
import com.hcmute.mychatapp.model.User;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ContactsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ContactsFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ContactsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ContactsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ContactsFragment newInstance(String param1, String param2) {
        ContactsFragment fragment = new ContactsFragment();
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
    //Khai b??o c??c view s??? d???ng
    View view;
    //Listview hi???n th??? danh s??ch b???n b??
    ListView lstFriends;
    //N???u kh??ng c?? b???n b?? th?? hi???n textview n??y l??n
    TextView txtNofriend;
    //Khai b??o adapter
    UserAdapter userAdapter;
    //M???ng ch???a danh s??ch b???n b??
    ArrayList<User> arrUser;
    //Khai b??o bi???n l??u th??ng tin ng?????i d??ng hi???n t???i
    User main_user;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //??nh x???
        view = inflater.inflate(R.layout.fragment_contacts, container, false);
        txtNofriend = view.findViewById(R.id.txtNofriend);
        lstFriends = view.findViewById(R.id.lstFriends);
        //Kh???i t???o m???ng
        arrUser = new ArrayList<>();
        //Kh???i t???o adapter
        userAdapter = new UserAdapter(getActivity(),R.layout.user_row,arrUser);
        //Set adapter cho listview
        lstFriends.setAdapter(userAdapter);
//        //B???t s??? ki???n click v??o listview Item. Chuy???n ?????n trang c?? nh??n ng?????i d??ng ???????c ch???n
//        lstFriends.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                //M??? trang c?? nh??n c???a ?????i t?????ng l??n
//                //L??u user_id v??o sharedPreferences
//                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("dataCookie", Context.MODE_MULTI_PROCESS);
//                sharedPreferences.edit().putString("user_id", arrUser.get(i).getPhone()).commit();
//                //Ch???y activity
//                startActivity(new Intent(getActivity(), ViewUserPageActivity.class));
//            }
//        });
        //L???y danh s??ch b???n b??
        //S??? d???ng M???u SingleTon l???y user hi???n t???i ra
        main_user = User_SingeTon.getInstance().getUser();
        //K???t n???i ?????n Database
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference();
        //T???o s??? ki???n cho nh??nh con trong b???ng friends/ addListenerForSingleValueEvent ch??? ch???y 1 l???n
        myRef.child("friends").child(main_user.getPhone()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //X??a h???t ph???n t??? trong m???ng arrUser
                arrUser.clear();
                //V??ng l???p ????? l???y danh s??ch b???n b??
                for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    //??nh x??? qua b???ng users ????? l???y th??ng tin
                    String user_phone = dataSnapshot.getValue(Friends.class).getFriendPhone();
                    //T???o k???t n???i b???ng users
                    DatabaseReference myRef1 = FirebaseDatabase.getInstance().getReference("users");
                    //Add s??? ki???n ????? ?????c d??? li???u.
                    myRef1.child(user_phone).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            //????a user v??o trong Arraylist
                            arrUser.add(snapshot.getValue(User.class));
                            lstFriends.setVisibility(View.VISIBLE);
                            txtNofriend.setVisibility(View.INVISIBLE);
                            userAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
                if(arrUser.size() == 0)
                {
                    txtNofriend.setVisibility(View.VISIBLE);
                    lstFriends.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        //Nh???n v??o th?? chuy???n qua trang tr?? chuy???n v???i ng?????i d??ng ????
        lstFriends.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                User user = arrUser.get(position);
                //Ki???m tra c?? cu???c tr?? chuy???n ? ch??a c?? th?? t???o m???i v?? chuy???n qua giao di???n nh???n tin.
                String phone = user.getPhone();
                if (phone.equals(main_user.getPhone())) {
                    Toast.makeText(getActivity(), "Cant Message to yourself !", Toast.LENGTH_SHORT).show();
                    return;

                }
                //Ki???m tra xem c?? cu???c h???i tho???i n??y ch??a.
                //T???o k???t n???i ??i???n b???ng messages
                DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("messages");
                myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //?????c d??? li???u t??? c?? s??? d??? li???u
                        //L???y id tin nh???n ta c?? 2 tr?????ng h???p l?? phone1_phone2 ho???c phone2_phone1.
                        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("dataCookie", Context.MODE_MULTI_PROCESS);
                        String message_id_1 = main_user.getPhone() + "_" + phone;
                        String message_id_2 = phone + "_" + main_user.getPhone();
                        if (snapshot.child(message_id_1).exists()) {

                            //???? c?? cu???c h???i tho???i gi???a 2 ng?????i.
                            Log.d("TAG", "???? c??" + message_id_1);
                            sharedPreferences.edit().putString("message_id", message_id_1).commit();
                        } else if (snapshot.child(message_id_2).exists()) {
                            Log.d("TAG", "???? c??" + message_id_2);
                            sharedPreferences.edit().putString("message_id", message_id_2).commit();

                        } else {
                            //Ch??a c?? h???i tho???i gi???a 2 ng?????i
                            Log.d("TAG", "Ch??a c?? v?? ti???n h??nh t???o");
                            //Ti???n h??nh th??m h???i tho???i.
                            //T???o m???t message
                            Message message = new Message(message_id_1, user.getFullname());
                            myRef.child(message_id_1).setValue(message);
                            //Th??m v??o b???ng participants cho c??? 2 ng?????i

                            DatabaseReference newRef = FirebaseDatabase.getInstance().getReference("participants");
                            Participants participants1 = new Participants(message_id_1, phone);
                            newRef.child(main_user.getPhone()).child(message_id_1).setValue(participants1);
                            Participants participants2 = new Participants(message_id_1, main_user.getPhone());
                            newRef.child(phone).child(message_id_1).setValue(participants2);
                            sharedPreferences.edit().putString("message_id", message_id_1).commit();

                        }
                        //M??? trang tr?? chuy???n
                        startActivity(new Intent(getActivity(), ChatActivity.class));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });
        return view;
    }
}