package com.hcmute.mychatapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.hcmute.mychatapp.Pattern.UserImageBitmap_SingleTon;
import com.hcmute.mychatapp.Pattern.User_SingeTon;
import com.hcmute.mychatapp.model.User;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PasswordFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PasswordFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public PasswordFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PasswordFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PasswordFragment newInstance(String param1, String param2) {
        PasswordFragment fragment = new PasswordFragment();
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
    View view;
    //btnBackChangePassword: N??t tr??? v???
    ImageView btnBackChangePassword;
    //N??t hi???n m???t kh???u
    TextView txtShowPassword;
    //C??c edittext ????? nh???p m???t kh???u c??, m???t kh???u m???i v?? x??c nh???n m???t kh???u m???i
    EditText edittextCurrentPassword,edittextNewPassword,edittextConfirmNewPassword;
    //N??t x??c nh???n thay ?????i
    Button btnConfirmChangePassword;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = (View)inflater.inflate(R.layout.fragment_password, container, false);
        //??nh x??? c??c view
        btnBackChangePassword = (ImageView) view.findViewById(R.id.btnBackChangePassword);
        txtShowPassword = (TextView) view.findViewById(R.id.txtShowPassword);
        edittextCurrentPassword = (EditText) view.findViewById(R.id.edittextCurrentPassword);
        edittextNewPassword = (EditText) view.findViewById(R.id.edittextNewPassword);
        edittextConfirmNewPassword = (EditText) view.findViewById(R.id.edittextConfirmNewPassword);
        btnConfirmChangePassword = (Button) view.findViewById(R.id.btnConfirmChangePassword);

        //B???m n??t back ????? quay l???i Account setting
        btnBackChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AccountFragment accountFragment = new AccountFragment();
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, accountFragment).commit();
            }
        });
        //B???m n??t show ????? hi???n ho???c ???n t???t c??? c??c password
        txtShowPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //N???u ng?????i d??ng mu???n hi???n m???t kh???u
                if(txtShowPassword.getText().toString().equals("SHOW")) {
                    //Chuy???n c??c edittext sang ki???u text
                    edittextCurrentPassword.setInputType(1);
                    edittextNewPassword.setInputType(1);
                    edittextConfirmNewPassword.setInputType(1);
                    txtShowPassword.setText("HIDE");
                }
                //N???u ng?????i d??ng mu???n ???n m???t kh???u
                else{
                    //Chuy???n c??c edittext sang ki???u password
                    edittextCurrentPassword.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD | InputType.TYPE_CLASS_TEXT);
                    edittextNewPassword.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD | InputType.TYPE_CLASS_TEXT);
                    edittextConfirmNewPassword.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD | InputType.TYPE_CLASS_TEXT);
                    txtShowPassword.setText("SHOW");
                }
            }
        });
        //B???m n??t Update ????? th???c hi???n thay ?????i password
        btnConfirmChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //T???o dialog process ??ang ????ng nh???p
                ProgressDialog progressDialog = new ProgressDialog(getActivity());
                progressDialog.setTitle("Please wait...");
                progressDialog.show();
                //L???y m???t kh???u b??n view
                String currentPassword = edittextCurrentPassword.getText().toString();
                String newPassword = edittextNewPassword.getText().toString();
                String confirmNewPassword = edittextConfirmNewPassword.getText().toString();

                //D??ng m???u thi???t k??? singleTon ????? l??u l???i user sau khi login
                User_SingeTon user_singeTon = User_SingeTon.getInstance();
                User user = user_singeTon.getUser();

                //Ki???m tra m???t kh???u hi???n t???i ng?????i d??ng nh???p c?? ????ng kh??ng
                if(!currentPassword.equals(user.getPassword())){
                    progressDialog.dismiss();
                    Toast.makeText(getActivity(), "Incorrect current password!", Toast.LENGTH_SHORT).show();
                    return;
                }
                //Ki???m tra m???t kh???u m???i kh??ng ???????c tr???ng v?? ph???i c?? ??t nh???t 6 k?? t???
                if(newPassword.isEmpty() || newPassword.length()<6){
                    progressDialog.dismiss();
                    Toast.makeText(getActivity(), "Invalid password!", Toast.LENGTH_SHORT).show();
                    return;
                }
                //Ki???m tra m???t kh???u m???i v?? m???t kh???u x??c nh???n
                //Kh??ng gi???ng nhau
                if(!newPassword.equals(confirmNewPassword)){
                    progressDialog.dismiss();
                    Toast.makeText(getActivity(), "The confirm password does not match!!", Toast.LENGTH_SHORT).show();
                }
                //Gi???ng nhau
                else{
                    //C???p nh???t thay ?????i l??n FirebaseDatabase v?? user
                    //C???p nh???t cho user
                    user.setPassword(newPassword);
                    //C???p nh???t cho database
                    //G???i k???t n???i ?????n FirebaseDatabase v??o b???ng users
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference myRef = database.getReference("users");
                    //Truy c???p ?????n nh??nh c?? id l?? s??? ??i???n tho???i v?? c???p nh???t l???i th??ng tin
                    myRef.child(user.getPhone()).setValue(user);
                    progressDialog.dismiss();
                    Toast.makeText(getActivity(), "Change password successful.", Toast.LENGTH_SHORT).show();
                    logout();
                }
            }
        });

        return view;
    }
    //H??m ????? ????ng xu???t
    private void logout(){
        //G??n User l?? null v?? tr??? v??? trang login
        User_SingeTon user_singeTon = User_SingeTon.getInstance();
        user_singeTon.setUser(null);
        user_singeTon = null;
        UserImageBitmap_SingleTon userImageBitmap_singleTon = UserImageBitmap_SingleTon.getInstance();
        userImageBitmap_singleTon.setAnhbia(null);
        userImageBitmap_singleTon.setAnhdaidien(null);
        userImageBitmap_singleTon = null;

        //Tr??? v??? trang login
        startActivity(new Intent(getActivity(),loginActivity.class));
        getActivity().finish();
    }
}