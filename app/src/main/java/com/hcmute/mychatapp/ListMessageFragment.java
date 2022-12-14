package com.hcmute.mychatapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;

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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hcmute.mychatapp.Pattern.User_SingeTon;
import com.hcmute.mychatapp.adapter.MessageListAdapter;
import com.hcmute.mychatapp.adapter.UserAdapter;
import com.hcmute.mychatapp.model.LoginHistory;
import com.hcmute.mychatapp.model.Message;
import com.hcmute.mychatapp.model.MessageDetails;
import com.hcmute.mychatapp.model.Participants;
import com.hcmute.mychatapp.model.PhoneBook;
import com.hcmute.mychatapp.model.User;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ListMessageFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ListMessageFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ListMessageFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ListMessageFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ListMessageFragment newInstance(String param1, String param2) {
        ListMessageFragment fragment = new ListMessageFragment();
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

    //Khai b??o c??c view
    //txtNoMessage: Khi listview kh??ng c?? ph???n t??? th?? hi???n ra cho ng?????i d??ng
    TextView txtNoMessage;
    View view;
    //Thanh t??m ki???m ????? t??m ki???m
    SearchView searchView;
    //N??t n??y ????? t???i l???i trang
    ImageView btn_more;
    //Listview hi???n c??c cu???c h???i tho???i
    ListView listviewMessage;
    //Adapter ng?????i d??ng
    UserAdapter adapter;
    //Adapter tin nh???n
    MessageListAdapter messageListAdapter;
    //Ng?????i d??ng t??m th???y
    User found_user;
    //Ng?????i d??ng hi???n t???i
    User main_user;
    //M???ng ch???a ng?????i d??ng
    ArrayList<User> users = new ArrayList<>();
    //M???ng ch??a cu???c h???i tho???i m?? ng?????i d??ng tham gia
    ArrayList<Participants> participantsList = new ArrayList<>();
    //Bi???n d??ng ????? tr??nh vi???c h??m getListParticipant b??? g???i 2 l???n li??n ti???p
    boolean callParticipant = false;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_list_message, container, false);
        //N???u kh??ng c?? user tr??? v??? trang login
        if(User_SingeTon.getInstance().getUser() == null) {
            startActivity(new Intent(getActivity(), loginActivity.class));
            getActivity().finish();
        }

        //??nh x???
        txtNoMessage = view.findViewById(R.id.txtNoMessage);
        searchView = view.findViewById(R.id.searchView);
        btn_more = (ImageView) view.findViewById(R.id.btn_more);
        listviewMessage = (ListView) view.findViewById(R.id.listviewMessage);
        //Kh???i t???o adapter
        messageListAdapter = new MessageListAdapter(getActivity(),R.layout.message_row,participantsList);
        //?????t addapter cho listview
        listviewMessage.setAdapter(messageListAdapter);
        //B???t swh ki??n onclcick. Nh???n v??o th?? t???i l???i danh s??ch cu???c tr?? chuy???n
        btn_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getListParticipant();
            }
        });
        //B???t s??? ki???n khi nh???p ch??? v??o ?? t??m ki???m.
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.clearFocus();
                return false;
            }
            //Khi thay ?????i th??ng tin t??m ki???m ch???y v??o h??m n??y
            @Override
            public boolean onQueryTextChange(String newText) {
                //Kh???i t???o adapter
                adapter = new UserAdapter(getActivity(),R.layout.user_row,users);
                //?????t adapter user cho listview
                listviewMessage.setAdapter(adapter);
                String search_phone = newText;
                //Ki???m tra ????? d??i c???a s??? ??i???n tho???i
                if(search_phone.length() == 10){
                    //Clear M???ng tham gia h???i tho???i
                    participantsList.clear();
                    //T???o k???t n???i ?????n b???ng users v?? d???c d??? li???u
                    DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("users");
                    //addListenerForSingleValueEvent s??? ki???n ?????c 1 l???n
                    myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.child(search_phone).exists()){
                                //N???u t???n t???i s??? ??i???n tho???i t??m ki???m
                                //L??u l???i th??ng tin ng?????i d??ng ????
                                found_user = snapshot.child(search_phone).getValue(User.class);
                                //L??m m???i m???ng ng?????i d??ng
                                users.clear();
                                //Th??m m???t ph???n t???
                                users.add(found_user);
                                //Th??ng b??o thay ?????i cho userdapter
                                adapter.notifyDataSetChanged();
                                //Hi???n listview
                                listviewMessage.setVisibility(View.VISIBLE);
                                txtNoMessage.setVisibility(View.INVISIBLE);
                            }else{
                                //N???u kh??ng c?? hi???n txtNoMessage
                                listviewMessage.setVisibility(View.INVISIBLE);
                                txtNoMessage.setVisibility(View.VISIBLE);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });
                }else if(search_phone.length() == 0) {
                    //N???u ????? d??i s??t l?? 0 th?? t???i l???i danh s??ch nh???n tin
                      getListParticipant();
                }else{
                    //C??n l???i th?? k l??m g??
                    users.clear();
                    adapter.notifyDataSetChanged();
                }
                return false;
            }
        });
        listviewMessage.setClickable(true);
        //S??? ki???n khi nh???n v??o listview. Chuy???n ?????n trang tr?? chuy???n c???a 2 ng?????i
        listviewMessage.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("dataCookie", Context.MODE_MULTI_PROCESS);
                //User hi???n t???i nh???n v??o nh???n tin v???i user n??o ????
                if(users.isEmpty()){
                    sharedPreferences.edit().putString("message_id", participantsList.get(i).getMessageid()).commit();
                    startActivity(new Intent(getActivity(),ChatActivity.class));
                }
                else {
                    User user = users.get(i);
                    //Ki???m tra c?? cu???c tr?? chuy???n ? ch??a c?? th?? t???o m???i v?? chuy???n qua giao di???n nh???n tin.
                    String phone = user.getPhone();
                    main_user = User_SingeTon.getInstance().getUser();
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
                                sharedPreferences.edit().putString("message_id", message_id_1).commit();
                            } else if (snapshot.child(message_id_2).exists()) {
                                sharedPreferences.edit().putString("message_id", message_id_2).commit();

                            } else {
                                //Ch??a c?? h???i tho???i gi???a 2 ng?????i
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
                            startActivity(new Intent(getActivity(), ChatActivity.class));
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }

            }
        });
        //L???y danh s??ch tr?? chuy???n tin nh???n
        getListParticipant();
        return view;
    }
    //l???y danh s??ch nh???ng ng?????i m?? ???? nh???n tin
    //Bi???n ????? d???c ghi d??? li???u tr??n c?? s??? d??? li???u
    DatabaseReference myParticipantRef;
    //Bi???n s??? ki???n
    ValueEventListener listener;

    private void getListParticipant(){
        //Tr??nh vi???c h??m b??? g???i li??n t???c 2 l???n d???n ?????n d??? li???u l???n x???n trong listview
        if(callParticipant) return;
        callParticipant = true;
            //K???t n???i c?? s??? d??? li???u
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myParticipantRef = database.getReference("participants");
        //D??ng m???u thi???t k??? singleTon ????? l??u l???i user sau khi login
        main_user = User_SingeTon.getInstance().getUser();
        //T??m trong b???ng participants
        listener = myParticipantRef.child(main_user.getPhone()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //X??a m???ng c??
                participantsList.clear();
                //l???y danh s??ch nh???ng ng?????i ???? nh???n tin
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Participants participants = dataSnapshot.getValue(Participants.class);
                    //System.out.println(participants.getUserPhone());
                    participantsList.add(participants);
                }
                List<Date> arrDate = new ArrayList<>();
                int len = participantsList.size();
                for(int i = 0; i < len; i++)
                {
                    //??nh x??? qua b???ng chi ti???t tin nh???n ????? l???y tin nh???n m???i nh???t
                    DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("message_details");
                    int finalI = i;
                    myRef.child(participantsList.get(i).getMessageid()).limitToFirst(1).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                                MessageDetails messageDetails = dataSnapshot.getValue(MessageDetails.class);
                                arrDate.add(messageDetails.getTimeSended());
                            }
                            if(snapshot.exists() == false)
                            {
                                arrDate.add(new Date(0,0,0));

                            }
                            //M???ng arrDate ch???a th???i gian c???a tin nh???n m???i nh???t c???a hai ng?????i d??ng
                            //S???p x???p tin nh???n theo th??? t??? th???i gian g???n nh???t
                            if(finalI == len - 1 )
                            {
                                //Tai vong cuoi cung tien hanh sap xep
                                //Sap xep lai participantsList theo arrDate
                                //Ban ?????u th??? t??? l?? ????ng
                                //Thu???t to??n bubble sort
                                int len = participantsList.size();
                                if(len == arrDate.size())
                                {
                                    for(int i = 0; i < len; i++)
                                        for(int j = 0 ; j < len-1 ; j++)
                                        {
                                            long num1 = arrDate.get(j).getTime();
                                            long num2 = arrDate.get(j+1).getTime();
                                            if(num1 < num2)
                                            {
                                                //Swap j cho j+1
                                                Participants part1 = participantsList.get(j);
                                                Participants part2 = participantsList.get(j+1);
                                                participantsList.set(j,part2);
                                                participantsList.set(j+1,part1);

                                                Date date1 = arrDate.get(j);
                                                Date date2 = arrDate.get(j+1);
                                                arrDate.set(j,date2);
                                                arrDate.set(j+1,date1);
                                            }
                                        }
                                }
                                //Hi???n listview l??n
                                //C??i ?????t messageListAdapter cho listview
                                txtNoMessage.setVisibility(View.INVISIBLE);
                                listviewMessage.setVisibility(View.VISIBLE);
                                messageListAdapter = new MessageListAdapter(getActivity(),R.layout.message_row,participantsList);
                                listviewMessage.setAdapter(messageListAdapter);
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }});
                }
                if (participantsList.size() == 0) {
                    //N???u kh??ng c?? danh s??ch ng?????i ???? nh???n tin hi???n th??? l?? kh??ng c??
                    txtNoMessage.setVisibility(View.VISIBLE);
                    listviewMessage.setVisibility(View.INVISIBLE);

                }
                callParticipant = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    //Khi view b??? t???t ??i. ti???n h??nh h???y Eventlistener: listener v?? l??m m???i l???i listview.
    @Override
    public void onDestroy() {
        super.onDestroy();
        //Khi chuy???n qua fragment kh??c th?? t???t c??c listener ??i
        if (myParticipantRef != null && listener != null){
            myParticipantRef.removeEventListener(listener);
        }
        //L??m m???i array list, adapter, listview
        participantsList.clear();
        listviewMessage.setAdapter(null);
        messageListAdapter = null;
    }
}