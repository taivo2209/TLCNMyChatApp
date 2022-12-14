package com.hcmute.mychatapp;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import com.hcmute.mychatapp.Pattern.User_SingeTon;
import com.hcmute.mychatapp.adapter.LoginHistoryAdapter;
import com.hcmute.mychatapp.model.LoginHistory;
import com.hcmute.mychatapp.model.User;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LoginHistoryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LoginHistoryFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public LoginHistoryFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment LoginHistoryFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LoginHistoryFragment newInstance(String param1, String param2) {
        LoginHistoryFragment fragment = new LoginHistoryFragment();
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
    private View view;
    //N??t tr??? v???
    private ImageView btnBackPageHistory;
    //List view hi???n danh s??ch l???ch s??? ????ng nh???p
    private ListView listviewHistory;
    //Adapter cho listview
    LoginHistoryAdapter adapter;
    //M???ng ch???a l???ch s??? ????ng nh???p
    ArrayList<LoginHistory> historyList;
    //Ng?????i d??ng hi???n t???i
    User user;
    User_SingeTon user_singeTon;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_login_history, container, false);
        //??nh x??? c??c object view
        btnBackPageHistory = (ImageView) view.findViewById(R.id.btnBackPageHistory);
        listviewHistory = (ListView) view.findViewById(R.id.listviewHistory);

        //Khai b??o m???ng ????? l??u l???ch s??? ????ng nh???p
        historyList = new ArrayList<>();
        //Load l???ch s??? ????ng nh???p v??o listview
        adapter = new LoginHistoryAdapter(getActivity(),R.layout.history_row,historyList);
        listviewHistory.setAdapter(adapter);

        //B???t s??? ki???n khi b???m v??o n??t back. Tr??? v??? AccountFragment
        btnBackPageHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Chuy???n sang page Account
                AccountFragment accountFragment = new AccountFragment();
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flFragment,accountFragment).commit();

            }
        });

        //L???y danh s??ch l???ch s??? ????ng nh???p t??? database
        getListHistory();

        return view;
    }
    //H??m l???y l???ch s??? ????ng nh???p v?? ????a l??n listview
    private void getListHistory() {
        //K???t n???i c?? s??? d??? li???u v?? truy xu???t v??o b???ng l???ch s??? ????ng nh???p
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("loginHistory");

        //D??ng m???u thi???t k??? singleTon ????? l??u l???i user sau khi login
        user_singeTon = User_SingeTon.getInstance();
        user = user_singeTon.getUser();

        //T??m trong b???ng l???ch s??? ????ng nh???p
        myRef.child(user.getPhone()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //X??a m???ng c??
                historyList.clear();
                //L???y l???ch s??? ?????ng nh??p c???a ng?????i d??ng t??? database v?? th??m v??o m???ng
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    LoginHistory history = dataSnapshot.getValue(LoginHistory.class);
                    historyList.add(history);
                }
                adapter.notifyDataSetChanged();
            }
            //Kh??ng l???y ???????c d??? li???u v?? th??ng b??o
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), "Get login history failed", Toast.LENGTH_SHORT).show();
            }
        });
    }
}