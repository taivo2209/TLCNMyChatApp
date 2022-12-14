package com.hcmute.mychatapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hcmute.mychatapp.Pattern.UserImageBitmap_SingleTon;
import com.hcmute.mychatapp.Pattern.User_SingeTon;
import com.hcmute.mychatapp.model.User;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AccountFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AccountFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public AccountFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AccountFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AccountFragment newInstance(String param1, String param2) {
        AccountFragment fragment = new AccountFragment();
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
    //Khai b??o c??o View
    View view;
    //N??t back ????? tr??? v??? trang tr?????c
    ImageView btnBack;
    //C??c text view hi???n c??c ch???c n??ng
    TextView txtChangePassword, txtLoginHistory,txtLogOut,txtAddFriendRequest,txtPhonenumber;
    LinearLayout linearChangeNumber;
    //Bi???n ch??a th??ng tin ng?????i d??ng hi???n t???i
    User main_user;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //D??ng singleTon ????? l???y user hi???n t???i
        main_user = User_SingeTon.getInstance().getUser();
        //Anh x??? c??c view
        view = (View)inflater.inflate(R.layout.fragment_account, container, false);
        btnBack = (ImageView) view.findViewById(R.id.btnBack);
        txtChangePassword = (TextView) view.findViewById(R.id.txtChangePassword);
        linearChangeNumber = (LinearLayout) view.findViewById(R.id.linearChangeNumber);
        txtLoginHistory = (TextView) view.findViewById(R.id.txtLoginHistory);
        txtLogOut = view.findViewById(R.id.txtLogOut);
        txtAddFriendRequest = view.findViewById(R.id.txtAddFriendRequest);
        txtPhonenumber = view.findViewById(R.id.txtPhonenumber);
        txtPhonenumber.setText("(+84) " + main_user.getPhone());
        //b???t s??? ki???n onclick cho n??t back -> Khi nh???n v??o s??? quay v??? trang tr?????c
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Tr??? v??? MoreFragment
                MoreFragment moreFragment = new MoreFragment();
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, moreFragment).commit();
            }
        });
        //b???t s??? ki???n onclick khi nh???n v??o hi???n th??? trang ?????i m???t kh???u
        txtChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //M??? PasswordFragment l??n thay th??? v??o FrameLayout trong activity_main
                PasswordFragment passwordFragment = new PasswordFragment();
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, passwordFragment).commit();
            }
        });
        //B???t s??? ki???n onClick c???a textview linearChangeNumber. Nh???n v??o chuy???n ?????n trang ?????i s??? ??i???n tho???i (Ch???c n??ng n??y ch??a ???????c c??i ?????t)
        linearChangeNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //M??? PhoneNumberFragment l??n thay th??? v??o FrameLayout trong activity_main
                PhoneNumberFragment phoneNumberFragment = new PhoneNumberFragment();
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, phoneNumberFragment).commit();
            }
        });
        //B???t s??? ki???n onClick c???a textview txtLoginHistory. B???m v??o hi???n th??? l??n trang hi???n danh s??ch l???ch s??? ????ng nh???p
        txtLoginHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //M??? LoginHistoryFragment l??n thay th??? v??o FrameLayout trong activity_main
                LoginHistoryFragment loginHistoryFragment = new LoginHistoryFragment();
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flFragment,loginHistoryFragment).commit();
            }
        });
        //B???t s??? ki???n onClick c???a textview txtLogout. Nh???n v??o s??? ????ng xu???t v?? chuy???n ?????n trang ????ng nh???p
        txtLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //G??n User l?? null v?? tr??? v??? trang login
                //G??n user trong singleTon l?? null
                User_SingeTon user_singeTon = User_SingeTon.getInstance();
                user_singeTon.setUser(null);
                user_singeTon = null;
                //G??n h??nh ???nh l??u th??nh null
                UserImageBitmap_SingleTon userImageBitmap_singleTon = UserImageBitmap_SingleTon.getInstance();
                userImageBitmap_singleTon.setAnhbia(null);
                userImageBitmap_singleTon.setAnhdaidien(null);
                userImageBitmap_singleTon = null;

                //Tr??? v??? trang login
                startActivity(new Intent(getActivity(),loginActivity.class));
                getActivity().finish();

            }
        });
        //B???t s??? ki???n onClick c???a textview txtAddFriendRequest.
        // Nh???n v??o hi???n th??? l??n trang hi???n danh s??ch c??i l???i m???i k???t b???n, c??ng nh?? l???i m???i k???t b???n ???? g???i
        txtAddFriendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //M??? activity FriendRequestActivity
                startActivity(new Intent(getActivity(), FriendRequestActivity.class));
            }
        });
        return view;
    }
}