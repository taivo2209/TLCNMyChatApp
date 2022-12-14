package com.hcmute.mychatapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.net.URL;

import de.hdodenhof.circleimageview.CircleImageView;
import com.hcmute.mychatapp.Pattern.UserImageBitmap_SingleTon;
import com.hcmute.mychatapp.Pattern.User_SingeTon;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MoreFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MoreFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public MoreFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MoreFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MoreFragment newInstance(String param1, String param2) {
        MoreFragment fragment = new MoreFragment();
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
//Khai b??o c??c View
    View view;
    //Khai b??o c??c layout hi???n th??? ch???c n??ng
    LinearLayout linearAccount,lineartop,linearPrivacy;
    //CircleImageView Hi???n th??? h??nh ???nh c???a ng?????i d??ng
    private CircleImageView profile_image;
    //TextView: Hi???n th??? t??n v?? s??? ??i???n tho???i c???a ng???i d??ng
    private TextView txtUserPhone,txtUserName;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //??nh x??? c??c view
        view = (View) inflater.inflate(R.layout.fragment_more, container, false);
        profile_image = view.findViewById(R.id.profile_image);
        txtUserPhone = view.findViewById(R.id.txtUserPhone);
        txtUserName = view.findViewById(R.id.txtUserName);
        linearAccount = (LinearLayout) view.findViewById(R.id.linearAccount);
        lineartop = (LinearLayout) view.findViewById(R.id.lineartop);
        linearPrivacy = (LinearLayout) view.findViewById(R.id.linearPrivacy);
        //D??ng SingleTon ????? l???y d??? li???u
        UserImageBitmap_SingleTon userImageBitmap_singleTon = UserImageBitmap_SingleTon.getInstance();
        User_SingeTon user_singeTon = User_SingeTon.getInstance();

        //N???u kh??ng c?? user tr??? v??? trang login
        if(user_singeTon.getUser() == null)
        {
            startActivity(new Intent(getActivity(), loginActivity.class));
            getActivity().finish();
        }
        //B???t s??? ki???n onclick. Chuy???n ?????n AccountFragment
        linearAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //M??? fragmentAccount
                AccountFragment accountFragment = new AccountFragment();
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, accountFragment).commit();
            }
        });
        //B???t s??? ki???n onclick. Chuy???n ?????n AccountInformationFragment
        lineartop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //M??? fragment AccountInformationFragment
                AccountInformationFragment accountInformationFragment = new AccountInformationFragment();
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, accountInformationFragment).commit();
            }
        });
        //B???t s??? ki???n onclick. Chuy???n ?????n PrivacyFragment
        linearPrivacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //M??? fragment PrivacyFragment

                PrivacyFragment privacyFragment = new PrivacyFragment();
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, privacyFragment).commit();
            }
        });
        //Ki???m tra n???u ???? c?? ???nh m???i th???c hi???n l???y ???nh ?????i di???n
        if(!user_singeTon.getUser().getAvatar().equals("")) {
            //L???y bitmao ???nh ?????i di???n trong SingleTon
            Bitmap avatar = userImageBitmap_singleTon.getAnhdaidien();
            //N???u ch??a c?? th?? l???y t??? tr??n firebase xu???ng
            if (avatar == null) {
                //T???o k???t n???i ?????n FirebaseStorage
                FirebaseStorage storage = FirebaseStorage.getInstance();
                //L???y h??nh ???nh c?? ?????a ch??? user_singeTon.getUser().getAvatar()
                StorageReference storageReference = storage.getReference(user_singeTon.getUser().getAvatar());
                //T???o s??? ki???n khi l???y h??nh ???nh th??nh c??ng
                storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        //L???y ???????c Uri th??nh c??ng. D??ng picasso ????? ????a h??nh v??o Circle View ???nh ?????i di???n
                        Picasso.get().load(uri).fit().centerCrop().into(profile_image);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //Th???t b???i th?? s??? in ra l???i
                    }
                });
            } else {
                profile_image.setImageBitmap(avatar);
            }
        }
        //????a d??? li???u cho c??c textview
        txtUserName.setText(user_singeTon.getUser().getFullname());
        txtUserPhone.setText(user_singeTon.getUser().getPhone());


        return view;
    }
}