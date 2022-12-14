package com.hcmute.mychatapp;

import static android.Manifest.permission.CAMERA;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;
import com.hcmute.mychatapp.Pattern.UserImageBitmap_SingleTon;
import com.hcmute.mychatapp.Pattern.User_SingeTon;
import com.hcmute.mychatapp.model.User;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AccountInformationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AccountInformationFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public AccountInformationFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AccountInformationFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AccountInformationFragment newInstance(String param1, String param2) {
        AccountInformationFragment fragment = new AccountInformationFragment();
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
    //background ch???a ???nh b??a, btnBack l?? n??t tr??? l???i, btnEditInfor nh???n v??o ????? hi???n th??m ch???c n??ng
    private ImageView background,btnBack,btnEditInfor;
    //avatar ch???a ???nh ?????i di???n
    private CircleImageView avatar;
    //View hi???n th??? t??n v?? m?? t??? b???n th??n
    private TextView txtfullname, txtdescription;
    //Bi???n d??ng ????? x??c nh???n h??m x??? l?? sau khi ch???n ???nh th??nh c??ng
    private final int PICK_IMAGE_REQUEST = 22;
    //Uri c???a h??nh ???nh sau khi ch???n t??? danh m???c h??nh ???nh
    private Uri filePath;
    //Bi???n c???a Firebase. ????? vi???t v?? ?????c d??? li???u t??? Firebase Storage
    FirebaseStorage storage;
    StorageReference storageReference;
    //Bi???n l??u th??ng tin ng?????i d??ng
    User user;
    User_SingeTon user_singeTon;
    private int type;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_account_information, container, false);
        //??nh x??? c??c view
        background = (ImageView) view.findViewById(R.id.background);
        avatar = (CircleImageView) view.findViewById(R.id.avatar);
        btnEditInfor = (ImageView) view.findViewById(R.id.btnEditInfor);
        txtfullname = view.findViewById(R.id.txtFullName);
        txtdescription = view.findViewById(R.id.txtDescription);
        //T???o s??? ki???n onclick cho n??t s???a th??ng tin (n??t 3 ch???m). Nh???n v??o ????? hi???n th??m ch???c n??ng thay ?????i
        btnEditInfor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(),AdjustInforActivity.class));
            }
        });
        //T???o s??? ki??n click cho ???nh b??a. Nh???n v??o show l??n dialog ????? thay ?????i ???nh b??a
        background.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Dialog dialog = new Dialog(getActivity());
                dialog.setContentView(R.layout.dialog_anhbia);
                dialog.show();
                LinearLayout linearChooseBackgroundImage = dialog.findViewById(R.id.linearChooseBackgroundImage);
                linearChooseBackgroundImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //Type = 0 la anh bia
                        SelectImage(0);
                    }
                });
                LinearLayout linearTakeNewImageBackground = dialog.findViewById(R.id.linearTakeNewImageBackground);
                linearTakeNewImageBackground.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Ki???m tra quy???n s??? d???ng m??y ???nh
                        if(CheckPermissions()) {
                            //G???n lo???i ???nh l?? ???nh b??a
                            type = 0;
                            //N???u ???????c quy???n th?? ch???y Activity ch???p ???nh
                            Intent takePicture = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                            startActivityForResult(takePicture, 0);
                        }else {
                            RequestPermissions();
                        }
                    }
                });
                //Trong dialog ???nh ?????i di???n nh???n v??o xem ???nh d??? xem ???nh
                LinearLayout linearViewBackground = dialog.findViewById(R.id.linearViewBackground);
                linearViewBackground.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                        //M??? dialog ????? c?? th??? xem full h??nh ???nh
                        final Dialog viewPictureDialog = new Dialog(getActivity());
                        viewPictureDialog.setContentView(R.layout.dialog_zoom);
                        viewPictureDialog.show();
                        //l???y ???nh b??a l??n view
                        ImageView mainpicture = viewPictureDialog.findViewById(R.id.mainpicture);
                        mainpicture.setImageBitmap(UserImageBitmap_SingleTon.getInstance().getAnhbia());
                    }
                });
            }
        });
        //T???o s??? ki??n click cho ???nh ?????i di???n. Nh???n v??o show l??n dialog ????? thay ?????i ???nh ?????i di???n
        avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Dialog dialog = new Dialog(getActivity());
                dialog.setContentView(R.layout.dialog_anhdaidien);
                dialog.show();
                //Trong dialog ???nh ?????i di???n nh???n v??o d??ng ch???n ???nh trong ??i???n tho???i s??? hi???n l??n h??nh ???nh trong ??i???n tho???i cho ng?????i d??ng ch???n
                LinearLayout linearChooseImageAvatar = dialog.findViewById(R.id.linearChooseImageAvatar);
                linearChooseImageAvatar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //H??m d??ng ????? hi???n l??n h??nh ???nh
                        //Type = 1 la anh anh dai dien
                        SelectImage(1);
                    }
                });
                //Trong dialog ???nh ?????i di???n nh???n v??o d??ng ch???p ???nh s??? cho ph??p ng?????i d??ng ch???p ???nh
                LinearLayout linearTakeNewImageAvatar = dialog.findViewById(R.id.linearTakeNewImageAvatar);
                linearTakeNewImageAvatar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Ki???m tra quy???n s??? d???ng m??y ???nh
                        if(CheckPermissions()) {
                            //G???n lo???i ???nh l?? ???nh ?????i di???n
                            type = 1;
                            //N???u ???????c quy???n th?? ch???y Activity ch???p ???nh
                            Intent takePicture = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                            startActivityForResult(takePicture, 0);
                        }else {
                            RequestPermissions();
                        }
                    }
                });
                //Trong dialog ???nh ?????i di???n nh???n v??o xem ???nh d??? xem ???nh
                LinearLayout linearViewAvatar = dialog.findViewById(R.id.linearViewAvatar);
                linearViewAvatar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                        //M??? dialog ????? c?? th??? xem full h??nh ???nh
                        final Dialog viewPictureDialog = new Dialog(getActivity());
                        viewPictureDialog.setContentView(R.layout.dialog_zoom);
                        viewPictureDialog.show();
                        //l???y ???nh ?????i di???n l??n view
                        ImageView mainpicture = viewPictureDialog.findViewById(R.id.mainpicture);
                        mainpicture.setImageBitmap(UserImageBitmap_SingleTon.getInstance().getAnhdaidien());
                    }
                });
            }
        });
        //B???m n??t back ????? quay l???i
        btnBack = (ImageView) view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MoreFragment moreFragment = new MoreFragment();
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, moreFragment).commit();
            }
        });

        //????a d??? li???u v??o c??c view
        putDataToView();

        return view;
    }
    //H??m n??y d??ng ????? ????a th??ng tin ng?????i d??ng v??o c??c view ????? hi???n th???
    void putDataToView(){
        //D??ng m???u thi???t k??? singleTon ????? l??u l???i user sau khi login
        user_singeTon = User_SingeTon.getInstance();

        user = user_singeTon.getUser();

        //N???u kh??ng c?? user tr??? v??? trang login
        if(user == null)
        {
            startActivity(new Intent(getActivity(), loginActivity.class));
            getActivity().finish();
        }
        //C?? user v?? b???t ?????u ????a d??? li???u cho c??c view
        txtdescription.setText(user.getDescription());
        txtfullname.setText(user.getFullname());
        // L???y ???nh b??a , ???nh ?????i di???n
        //G???i l???p singleTon
        UserImageBitmap_SingleTon userImageBitmap_singleTon = UserImageBitmap_SingleTon.getInstance();
        //N???u ch??a c?? ???nh th?? d??ng local storage t???i l??n - T???n th???i gian x??? l??
        if(userImageBitmap_singleTon.getAnhbia() == null || userImageBitmap_singleTon.getAnhdaidien() == null)
        {
            //Ki???m tra n???u ???? c?? ???nh m???i th???c hi???n l???y ???nh ?????i di???n
            if(!user.getAvatar().equals("")) {
                //????a d??? li???u cho ???nh ?????i di???n d??ng Firebase Storage
                storage = FirebaseStorage.getInstance();
                storageReference = storage.getReference(user.getAvatar());
                storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        //L???y ???????c Uri th??nh c??ng. D??ng picasso ????? ????a h??nh v??o Circle View ???nh ?????i di???n
                        Picasso.get().load(uri).fit().centerCrop().into(avatar);

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //Th???t b???i th?? s??? in ra l???i
                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
            //Ki???m tra n???u ???? c?? ???nh m???i th???c hi???n l???y ???nh b??a
            if(!user.getBackground().equals("")) {
                //????a d??? li???u cho ???nh b??a d??ng Firebase Storage
                storage = FirebaseStorage.getInstance();
                storageReference = storage.getReference(user.getBackground());
                storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        //L???y ???????c Uri th??nh c??ng. D??ng picasso ????? ????a h??nh v??o Circle View ???nh ?????i di???n
                        Picasso.get().load(uri).fit().centerCrop().into(background);

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //Th???t b???i th?? s??? in ra l???i
                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }else
        {
            //N???u ???? c?? ???nh th?? set v??o lu??n
            avatar.setImageBitmap(userImageBitmap_singleTon.getAnhdaidien());
            background.setImageBitmap(userImageBitmap_singleTon.getAnhbia());
        }
    }
    //Khi tr??? l???i Activity c???p nh???t l???i cho c??c view
    @Override
    public void onResume() {
        super.onResume();
        putDataToView();
    }

    // H??m ????? ch???n h??nh ???nh
    private void SelectImage(int type)
    {
        this.type = type;
        // X??c ?????nh m??? th?? m???c ???nh
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select image..."), PICK_IMAGE_REQUEST);
    }
    //Sau khi ch???n ???nh, ch???p ???nh xong ch???y v??o h??m n??y
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Ch???n ???nh xong ch???y v??o ??i???u ki???n n??y
        if (requestCode == PICK_IMAGE_REQUEST
                && resultCode == getActivity().RESULT_OK
                && data != null
                && data.getData() != null) {

            // L???y ???????c uri c???a ???nh
            filePath = data.getData();
            try {
                //????a ???nh l??n Firebase Storage
                uploadImage();
                // Chuy???n th??nh bitmap v?? ????a v??o ???nh ?????i di???n
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(),filePath);
                UserImageBitmap_SingleTon userImageBitmap_singleTon = UserImageBitmap_SingleTon.getInstance();
                //Type 1 l?? ???nh ?????i di???n, 0 l?? ???nh b??a
                if(this.type == 1) {
                    userImageBitmap_singleTon.setAnhdaidien(bitmap);
                    avatar.setImageBitmap(bitmap);
                }
                else {
                    userImageBitmap_singleTon.setAnhbia(bitmap);
                    background.setImageBitmap(bitmap);
                }
            }
            catch (IOException e) {
                // In ra l???i
                e.printStackTrace();
            }
        }
        else if(requestCode == 0
                && resultCode == getActivity().RESULT_OK
                && data != null)
        {
            //Sau khi ch???p ???nh b???ng camera xong ch???y v??o block n??y
            Bitmap selectedImage = (Bitmap) data.getExtras().get("data");
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            selectedImage.compress(Bitmap.CompressFormat.PNG,100,stream);
            byte[] byteArray = stream.toByteArray();
            //T??? camera
            Date today = new Date();
            String pic_id = Long.toString(today.getTime());
            // Hi???n ProgressDialog trong khi ??ang t???i l??n
            ProgressDialog progressDialog
                    = new ProgressDialog(getActivity());
            progressDialog.setTitle("Uploading...");

            progressDialog.show();
            //Khai b??o FirebaseStorage
            FirebaseStorage storage;
            StorageReference storageReference;

            storage = FirebaseStorage.getInstance();
            storageReference = storage.getReference();
            // ??i v??o nh??nh con
            StorageReference ref;
            if(type == 1){
                ref = storageReference.child("images/" + user.getPhone() + "_avatar");
            }else
                ref = storageReference.child("images/" + user.getPhone() + "_background");
            // T???o s??? ki??n cho vi???c upload file c??? khi th??nh c??ng hay th???t b???i v?? hi???n thanh progress theo %
            ref.putBytes(byteArray)
                    .addOnSuccessListener(
                            new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                                {
                                    // T???i ???nh l??n th??nh c??ng
                                    // T???t dialog progress ??i
                                    progressDialog.dismiss();
                                    //C???p nh???t l???i cho b???ng user v??? ?????a ch??? c???a avatar
                                    //C???p nh???t l???i cho c??? user_singleTon
                                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                                    DatabaseReference myRef = database.getReference("users");
                                    if(type == 1) {
                                        user.setAvatar("images/" + user.getPhone() + "_avatar");
                                        myRef.child(user.getPhone()).setValue(user);
                                    }else {
                                        user.setBackground("images/" + user.getPhone() + "_background");
                                        myRef.child(user.getPhone()).setValue(user);
                                    }
                                    user_singeTon.setUser(user);
                                    Toast.makeText(getActivity(), "Update successful!!", Toast.LENGTH_SHORT).show();
                                }
                            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e)
                        {
                            // L???i, kh??ng t???i l??n th??nh c??ng
                            // T???t progress ??i v?? in ra l???i
                            progressDialog.dismiss();
                            Toast.makeText(getActivity(),"Update failed. Error: " + e.getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        // S??? ki???n cho Progress
                        // Hi???n th??? % ho??n th??nh
                        @Override
                        public void onProgress(
                                UploadTask.TaskSnapshot taskSnapshot)
                        {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred()/ taskSnapshot.getTotalByteCount());
                            progressDialog.setMessage("Downloaded " + (int)progress + "%");
                        }
                    });
            // Chuy???n th??nh bitmap v?? ????a v??o ???nh ?????i di???n
            //byte -> bitmap
            Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray,0,byteArray.length);
            UserImageBitmap_SingleTon userImageBitmap_singleTon = UserImageBitmap_SingleTon.getInstance();

            if(this.type == 1) {
                userImageBitmap_singleTon.setAnhdaidien(bitmap);
            }
            else {
                userImageBitmap_singleTon.setAnhbia(bitmap);
            }
        }
    }
    //H??m n??y d??ng ????? ????a ???nh l??n tr??n firebase storage
    private void uploadImage()
    {
        //Ki???m tra ???????ng d???n file
        if (filePath != null) {

            // Hi???n ProgressDialog trong khi ??ang t???i l??n
            ProgressDialog progressDialog
                    = new ProgressDialog(getActivity());
            progressDialog.setTitle("Uploading...");

            progressDialog.show();
            //Khai b??o FirebaseStorage
            storage = FirebaseStorage.getInstance();
            storageReference = storage.getReference();
            // ??i v??o nh??nh con
            StorageReference ref;
            if(type == 1){
                ref = storageReference.child("images/" + user.getPhone() + "_avatar");
            }else
                ref = storageReference.child("images/" + user.getPhone() + "_background");
            // T???o s??? ki??n cho vi???c upload file c??? khi th??nh c??ng hay th???t b???i v?? hi???n thanh progress theo %
            ref.putFile(filePath)
                    .addOnSuccessListener(
                            new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                                {
                                    // T???i ???nh l??n th??nh c??ng
                                    // T???t dialog progress ??i
                                    progressDialog.dismiss();
                                    //C???p nh???t l???i cho b???ng user v??? ?????a ch??? c???a avatar
                                    //C???p nh???t l???i cho c??? user_singleTon
                                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                                    DatabaseReference myRef = database.getReference("users");
                                    if(type == 1) {
                                        user.setAvatar("images/" + user.getPhone() + "_avatar");
                                        myRef.child(user.getPhone()).setValue(user);
                                    }else {
                                        user.setBackground("images/" + user.getPhone() + "_background");
                                        myRef.child(user.getPhone()).setValue(user);
                                    }
                                    user_singeTon.setUser(user);
                                    Toast.makeText(getActivity(), "Update successful!!", Toast.LENGTH_SHORT).show();
                                }
                            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e)
                        {
                            // L???i, kh??ng t???i l??n th??nh c??ng
                            // T???t progress ??i v?? in ra l???i
                            progressDialog.dismiss();
                            Toast.makeText(getActivity(),"Update failed. Error: " + e.getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        // S??? ki???n cho Progress
                        // Hi???n th??? % ho??n th??nh
                        @Override
                        public void onProgress(
                                UploadTask.TaskSnapshot taskSnapshot)
                        {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred()/ taskSnapshot.getTotalByteCount());
                            progressDialog.setMessage("Downloaded " + (int)progress + "%");
                        }
                    });
        }
    }
    //Bi???n d??ng ????? chia tr?????ng h???p xin permission t??? h??? th???ng
    public static final int REQUEST_AUDIO_PERMISSION_CODE = 1;
    //H??m ki???m tra quy???n c???a ???ng d???ng
    public boolean CheckPermissions() {
        // Ki???m tra c??c quy???n c?? ???????c c???p ch??a
        //Quy???n d??ng m??y ???nh
        int result = ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), CAMERA);
        return result == PackageManager.PERMISSION_GRANTED;
    }
    //H??m d??ng ????? xin quy???n s??? d???ng camera
    private void RequestPermissions() {
        //Xin c???p quy???n t??? h??? th???ng
        ActivityCompat.requestPermissions(getActivity(), new String[]{CAMERA}, REQUEST_AUDIO_PERMISSION_CODE);
    }

    //Sau khi ng?????i d??ng x??c nh???n hay t??? ch???i c???p quy???n s??? ch???y h??m n??y
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Sau khi ta ch???n ch???p nh???n hay t??? ch???i s??? tr??? k???t qu??? v??o h??m n??y
        switch (requestCode) {
            case REQUEST_AUDIO_PERMISSION_CODE:
                //N???u ???????c ch???p nh???n h???t
                if (grantResults.length > 0) {
                    boolean permissionToCamera = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (permissionToCamera) {
                        //Th??ng b??o th??nh c??ng
                        Toast.makeText(getActivity().getApplicationContext(), "Permission Granted", Toast.LENGTH_LONG).show();
                    } else {
                        //Th??ng b??o t??? ch???i
                        Toast.makeText(getActivity().getApplicationContext(), "Permission Denied", Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }
    }
}