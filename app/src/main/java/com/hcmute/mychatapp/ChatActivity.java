package com.hcmute.mychatapp;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.hcmute.mychatapp.Pattern.User_SingeTon;
import com.hcmute.mychatapp.adapter.MessageDetailsAdapter;
import com.hcmute.mychatapp.model.MessageDetails;
import com.hcmute.mychatapp.model.User;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    //Khai b??o c??c view v?? c??c bi???n ???????c s??? d???ng
    //N??t g???i tin nh???n.
    ImageView sendMessageButton;
    //Hi???n th??? t??n ng?????i ??ang nh???n tin
    TextView txtUserChatName;
    //V??ng nh???p tin nh???n
    EditText inputMessage;
    //Th??ng tin ng?????i d??ng hi???n t???i
    User main_user = User_SingeTon.getInstance().getUser();
    //Danh s??ch c??c tin nh???n c???a 2 ng?????i
    ArrayList<MessageDetails> messageDetails;
    //V??ng hi???n danh s??ch c??c tin nh???n
    RecyclerView rcvChat;
    //Adapter ????? hi???n tin nh???n cho RecyclerView
    MessageDetailsAdapter messageDetailsAdapter;
    //D??ng ????? t???o cu???n m??n h??nh ????? t???i th??m d??? li???u tin nh???n
    NestedScrollView idNestedSV;
    //?????m th???i gian thu ??m
    Chronometer recordTimer;
    //Hi???n avatar ng?????i nh???n tin
    CircleImageView imageProfileChat;
    //Bi???n ?????m s??? l?????ng tin nh???n c???n t???i l??n
    int count = 0;
    //Hi???n thanh ??ang t???i khi t???i th??m tin nh???n
    ProgressBar progressBar;
    //Hi???n c??c n??t g???i ??m thanh, h??nh ???nh, n??t tr??? v???
    ImageView iconMedia, iconMicro,imageBack;
    //Bi???n l??u id c???a cu???c tr?? chuy???n, s??? ??i???n tho???i c???a ng?????i nh???n
    String message_id, viewer;
    //Hi???n n??t camera trong v??ng nh???p tin nh???n
    ImageView iconCamera;
    //C??c bi???n d??ng cho vi???c ch???n h??nh ???nh t??? gallery
    //Bi???n ????? chia tr?????ng h???p khi l???y ???nh
    private int PICK_IMAGE_REQUEST = 22;
    //Bi???n ch???a Uri c???a h??nh ???nh sau khi ch???n
    private Uri filePath;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_chat);
        //??nh x??? c??c view
        iconMicro = findViewById(R.id.iconMicro);
        iconMedia = findViewById(R.id.iconMedia);
        idNestedSV = findViewById(R.id.idNestedSV);
        progressBar = findViewById(R.id.progressBar);
        inputMessage = findViewById(R.id.inputMessage);
        sendMessageButton = findViewById(R.id.sendMessageButton);
        txtUserChatName = findViewById(R.id.txtUserChatName);
        rcvChat = findViewById(R.id.rcvChat);
        imageBack = findViewById(R.id.imageBack);
        recordTimer = findViewById(R.id.recordTimer);
        imageProfileChat = findViewById(R.id.imageProfileChat);
        iconCamera = findViewById(R.id.iconCamera);
        //C??i ?????t cho RecyclerView
        rcvChat.setLayoutManager(new LinearLayoutManager(this));
        //Kh???i t???o messageDetails
        messageDetails = new ArrayList<>();
        //Kh???i t???o messageDetailsAdapter
        messageDetailsAdapter = new MessageDetailsAdapter(ChatActivity.this,messageDetails);
        //Set adapter cho RecyclerView
        rcvChat.setAdapter(messageDetailsAdapter);
        //B???t s??? ki???n onClick. Nh???n v??o hi???n l??n trang c?? nh??n c???a ng?????i nh???n tin nh???n
        imageProfileChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Khi click v??o n??t th?? s??? m??? m???t activity hi???n trang c?? nh??n c???a ?????i t?????ng ??ang nh???n tin
                //????a d??? li???u v??o trong sharedPreferences
                SharedPreferences sharedPreferences = getSharedPreferences("dataCookie",Context.MODE_MULTI_PROCESS);
                sharedPreferences.edit().putString("user_id", viewer).commit();
                //M??? activity ViewUserPageActivity
                startActivity(new Intent(ChatActivity.this, ViewUserPageActivity.class));
            }
        });
        //S??? ki???n khi nh???p th??ng tin trong Edittext khi c?? ch??? th?? ???n c??c icon ??i v?? khi kh??ng c?? s??? hi???n l??n l???i
        inputMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //N???u kh??ng nh???p s??? hi???n c??c icon nh?? micro ????? g???i record, picture ????? g???i ???nh, camera ????? ch???p ???nh
                String text = inputMessage.getText().toString();
                if(text.length() == 0)
                {
                    //Hi???n c??c n??t camera, voice, picture
                    iconCamera.setVisibility(View.VISIBLE);
                    iconMedia.setVisibility(View.VISIBLE);
                    iconMicro.setVisibility(View.VISIBLE);

                }else
                {
                    //???n c??c n??t ?????y ??i
                    iconCamera.setVisibility(View.INVISIBLE);
                    iconMedia.setVisibility(View.INVISIBLE);
                    iconMicro.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        //B???t s??? ki???n onclick. B???m v?? tr??? l???i trang tr?????c
        imageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Tr??? v??? mainActivity
                startActivity(new Intent(ChatActivity.this,MainActivity.class));
                //K???t th??c activity n??y ??i
                finish();
            }
        });
        //l???y id c???a ph??ng chat trong SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("dataCookie", Context.MODE_MULTI_PROCESS);
        message_id = sharedPreferences.getString("message_id","");
        //l???y id c???a ng?????i nh???n t??? message_id
        if(main_user.getPhone().equals(message_id.substring(0,10))){
            viewer = message_id.substring(11);
        }
        else {
            viewer = message_id.substring(0,10);
        }
        if(message_id.equals("") == false ){
            //L???y t??n chat box
            //T???o li??n k???t ?????n b???ng users
            DatabaseReference myUserRef = FirebaseDatabase.getInstance().getReference("users");
            //T???o s??? ki???n onDatachange cho nh??nh con
            myUserRef.child(viewer).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    //L???y user ra
                    User user = snapshot.getValue(User.class);
                    //????a t??n c???a user l??n thanh ti??u ????? ??o???n chat
                    txtUserChatName.setText(user.getFullname());
                    //N???u user c?? avatar
                    if(!user.getAvatar().equals("")) {
                        //????a d??? li???u cho ???nh ?????i di???n d??ng Firebase Storage
                        FirebaseStorage storage = FirebaseStorage.getInstance();
                        StorageReference storageReference = storage.getReference(user.getAvatar());
                        //T???o k???t n???i ?????n Firebase Storage v?? add s??? ki???n l???y h??nh ???nh th??nh c??ng
                        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                //L???y ???????c Uri th??nh c??ng. D??ng picasso ????? ????a h??nh v??o Circle View ???nh ?????i di???n
                                Picasso.get().load(uri).fit().centerCrop().into(imageProfileChat);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                //Th???t b???i th?? s??? in ra l???i
                                Log.d("TAG", "onFailure: " + e.getMessage());
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
            //L???y h???t tin nh???n c???a 2 ng?????i l??n ?????ng th???i k??m ph??n trang
            //T???o li??n k???t ?????n b???ng message_details
            DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("message_details");
            //??i v??o nh??nh message_id v?? l???y 1 tin nh???n ?????u ti??n
            //S??? ki??n addValueEventListener s??? ???????c l???p l???i. khi 1 trong 2 ng?????i g???i 1 tin nh???n th?? s??? ki???n s??? ???????c ch???y
            myRef.child(message_id).limitToFirst(1).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    //L???y m???t tin nh???n m???i nh???t
                    for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        //????a tin nh???n v??o Arraylist
                        messageDetails.add(dataSnapshot.getValue(MessageDetails.class));
                    }
                    //Th??ng b??o cho adapter
                    messageDetailsAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
            //L???y ti???p 9 tin nh???n ti???p theo, s??? ki???n addListenerForSingleValueEvent ch??? ch???y 1 l???n k??? c??? khi c?? s??? thay ?????i trong Database
            myRef.child(message_id).limitToFirst(10).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists())
                    {
                        //Load 9 tin nh???n ?????u tin khi b???t l??n
                        int i = 0;
                        for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            if(i == 0) {
                                i++;
                                continue;
                            }
                            //????a tin nh???n v??o Arraylist
                            messageDetails.add(0,dataSnapshot.getValue(MessageDetails.class));
                        }
                        //Th??ng b??o cho adapter
                        messageDetailsAdapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
        //S??? ki???n vu???t m??n h??nh xu???ng ????? t???i th??m tin nh???n
        idNestedSV.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                //N???u k??o xu???ng h???t c??? th?? scrolly == 0
                if (scrollY == 0) {
                    //T??ng count l??n
                    count++;
                    //S??? l?????ng tin nh???n l???y l??n
                    int start_index = count*10 + 10;
                    //L???y th??m 10 tin m???i l???n vu???t
                    //T???o k???t n???i ?????n b???ng message_details
                    DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("message_details");
                    //T???o s??? ki???n l???y tin nh???n.  limitToFirst l???y ????ng s??? l?????ng c???n l???y v??
                    // addListenerForSingleValueEvent ch??? ch???y m???t l???n
                    myRef.child(message_id).limitToFirst(start_index).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            //Hi???n progressBar cho bi???t h??? th???ng ??ang t???i
                            progressBar.setVisibility(View.VISIBLE);
                            messageDetails.clear();
                            for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                //????a tin nh???n v??o arraylist
                                messageDetails.add(0,dataSnapshot.getValue(MessageDetails.class));
                            }
                            //Th??ng b??o adapter
                            messageDetailsAdapter.notifyDataSetChanged();
                            //B??? hi???n progressBar khi t???i xong
                            progressBar.setVisibility(View.INVISIBLE);
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
                }
            }
        });
        //B???m n??t g???i ????? g???i tin nh???n (d???ng text).
        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = inputMessage.getText().toString();
                //N???u tin nh???n kh??ng ph???i r???ng
                if(message.equals("") == false){
                    //?????t l???i cho input
                    inputMessage.setText("");
                    //T???o id cho tin nh???n
                    String message_detail_id = Long.toString(Long.MAX_VALUE - new Date().getTime());
                    //T???o object l??u l???i tin nh???n
                    MessageDetails messageDetails = new MessageDetails(message_id,main_user.getPhone(),new Date(),message, viewer);
                    //T???o li??n k???t ????n b???ng message_details
                    DatabaseReference sendMessRef = FirebaseDatabase.getInstance().getReference("message_details");
                    //Vi???t v??o c?? s??? d??? li???u
                    sendMessRef.child(message_id).child(message_detail_id).setValue(messageDetails);
                }
            }
        });
        //B???m v??o imageview h??nh ???nh ????? ti???n h??nh ch???n ???nh v?? g???i
        iconMedia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Ch???n h??nh ???nh trong m??y
                SelectImage();
            }
        });
        //Khi nh???n gi??? v??o trong icon Micro th?? h??? th???ng s??? record
        iconMicro.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //Chu???n b??? cho record
                //?????m th???i gian
                recordTimer.setBase(SystemClock.elapsedRealtime());
                recordTimer.start();
                //???n c??c view trong thanh nh???p tin nh???n
                iconCamera.setVisibility(View.INVISIBLE);
                iconMedia.setVisibility(View.INVISIBLE);
                inputMessage.setVisibility(View.INVISIBLE);
                //Hi???n timer ?????m th???i gian
                recordTimer.setVisibility(View.VISIBLE);
                //B???t ?????u record
                startRecording();
                Toast.makeText(ChatActivity.this, "Start Recording...", Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        //B???t s??? ki???n click v??o icon Micro 1 l???n n???a ????? k???t th??c ghi ??m
        iconMicro.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //Khi Micro ???????c gi??? l??u v?? ??ang ghi ??m th?? s??? b??? qua l???nh n??y
                if(mRecorder == null)
                    return false;
                //S??? ki???n ch??nh
                switch (event.getAction()){
                    case MotionEvent.ACTION_UP:
                    {
                        //D???ng record
                        mRecorder.stop();
                        //Hi???n c??c view ???? t???t
                        recordTimer.setVisibility(View.INVISIBLE);
                        iconMedia.setVisibility(View.VISIBLE);
                        inputMessage.setVisibility(View.VISIBLE);
                        iconCamera.setVisibility(View.VISIBLE);

                        // gi???i ph??ng cho Recorder
                        mRecorder.release();
                        mRecorder = null;
                        //Th??ng b??o ng?????i d??ng
                        Toast.makeText(ChatActivity.this, "Stop Recording...", Toast.LENGTH_SHORT).show();
                        //T???o y??u c???u x??c nh???n g???i b???ng AlertDialog
                        AlertDialog.Builder dialogCheck = new AlertDialog.Builder(ChatActivity.this);
                        dialogCheck.setMessage("Do you want to send this record ?");
                        //S??? ki???n thi nh???n v??o n??t Yes
                        dialogCheck.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //????a record l??n Database
                                UploadRecord();
                            }
                        });
                        //S??? ki???n khi nh???n v??o n??t No
                        dialogCheck.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        });
                        //Hi???n dialog l??n
                        dialogCheck.show();
                        return true;
                    }
                }
                return false;
            }
        });
        //S??? ki???n khi nh???n v??o IconCamera. Xin quy???n camera v?? b???t camera ????? ch???p ???nh
        iconCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Ki???m tra quy???n s??? d???ng m??y ???nh
                if(CheckPermissions())
                {
                    //N???u ???????c quy???n th?? ch???y Activity ch???p ???nh
                    Intent takePicture = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    //Request code = 0
                    startActivityForResult(takePicture, 0);
                }else
                {
                    RequestPermissions();
                }
            }
        });
    }
    //l??u file record l??n database
    public void UploadRecord() {
        //L???y Uri c???a file
        filePath = Uri.fromFile(new File(mFileName));
        //Ki???m tra ???????ng d???n file
        if (filePath != null) {
            //L???y ng??y hi???n t???i
            Date today = new Date();
            //L???y th???i gian d???ng Long
            String pic_id = Long.toString(today.getTime());
            //T???o object l??u d??? lueeyj c???a tin nh???n
            MessageDetails mes = new MessageDetails(message_id, main_user.getPhone(),today,"message_records/" + message_id + "/" + pic_id,viewer);
            // Hi???n ProgressDialog trong khi ??ang t???i l??n
            ProgressDialog progressDialog
                    = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();
            //Khai b??o FirebaseStorage
            StorageReference storageReference= FirebaseStorage.getInstance().getReference();
            // ??i v??o nh??nh con
            StorageReference ref;
            ref = storageReference.child("message_records/" + message_id).child(pic_id);
            // T???o s??? ki??n cho vi???c upload file c??? khi th??nh c??ng hay th???t b???i v?? hi???n thanh progress theo %
            ref.putFile(filePath)
                    .addOnSuccessListener(
                            new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    // T???i ???nh l??n th??nh c??ng
                                    // T???t dialog progress ??i
                                    progressDialog.dismiss();
                                    //C???p nh???t l???i cho b???ng user v??? ?????a ch??? c???a avatar
                                    //T???o id cho tin nh???n
                                    String message_detail_id = Long.toString(Long.MAX_VALUE - today.getTime());;
                                    //K???t n???i ?????n b???ng message_details
                                    DatabaseReference sendMessRef = FirebaseDatabase.getInstance().getReference("message_details");
                                    //????a d??? li???u l??n
                                    sendMessRef.child(message_id).child(message_detail_id).setValue(mes);
                                }
                            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // L???i, kh??ng t???i l??n th??nh c??ng
                            // T???t progress ??i v?? in ra l???i
                            progressDialog.dismiss();
                            Log.d("TAG", "onFailure: " + e.getMessage());
                            Toast.makeText(ChatActivity.this, "Update failed. Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        // S??? ki???n cho Progress
                        // Hi???n th??? % ho??n th??nh
                        @Override
                        public void onProgress(
                                UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                            progressDialog.setMessage("Uploaded " + (int) progress + "%");
                        }
                    });
        }
    }
    // H??m ????? ch???n h??nh ???nh
    private void SelectImage() {
        // X??c ?????nh m??? th?? m???c ???nh
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        //Ch???y Activity ch???n ???nh
        startActivityForResult(Intent.createChooser(intent,"Select image..."), PICK_IMAGE_REQUEST);
    }
    //Sau khi ch???n ???nh , ch???p ???nh xong ch???y v??o h??m n??y
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Ch???n ???nh xong v??o ????y
        if (requestCode == PICK_IMAGE_REQUEST    && resultCode == RESULT_OK && data != null && data.getData() != null) {
            // L???y ???????c uri c???a ???nh
            filePath = data.getData();
            //Sau ???? g???i h??nh ???nh
            //????a ???nh l??n Firebase Storage
            uploadImage();

        }else if (requestCode == 0 && resultCode == RESULT_OK && data != null)
        {
            //Ch???p ???nh xong th?? v??o block n??y
            //L???y bitmap c???a ???nh
            Bitmap selectedImage = (Bitmap) data.getExtras().get("data");
            //?????i bitmap th??nh byte array
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            selectedImage.compress(Bitmap.CompressFormat.PNG,100,stream);
            byte[] byteArray = stream.toByteArray();
            //L???y ng??y hi???n t???i ????? l???y d???ng Long c???a ng??y l??m id cho tin nh???n
            Date today = new Date();
            String pic_id = Long.toString(today.getTime());
            //T???o object l??u d??? li???u tin nh???n
            MessageDetails mes = new MessageDetails(message_id, main_user.getPhone(),today,"message_images/" + message_id + "/" + pic_id,viewer);
            // Hi???n ProgressDialog trong khi ??ang t???i l??n
            ProgressDialog progressDialog
                    = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();
            //Khai b??o FirebaseStorage
            StorageReference storageReference= FirebaseStorage.getInstance().getReference();
            // ??i v??o nh??nh con
            StorageReference ref;
            ref = storageReference.child("message_images/" + message_id).child(pic_id);
            // T???o s??? ki??n cho vi???c upload file c??? khi th??nh c??ng hay th???t b???i v?? hi???n thanh progress theo %
            ref.putBytes(byteArray).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // T???i ???nh l??n th??nh c??ng
                    // T???t dialog progress ??i
                    progressDialog.dismiss();
                    //T???o id cho tin nh???n
                    String message_detail_id = Long.toString(Long.MAX_VALUE - today.getTime());
                    //T???o k???t n???i ?????n b???ng message_details
                    DatabaseReference sendMessRef = FirebaseDatabase.getInstance().getReference("message_details");
                    //????a d??? li???u l??n
                    sendMessRef.child(message_id).child(message_detail_id).setValue(mes);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    // L???i, kh??ng t???i l??n th??nh c??ng
                    // T???t progress ??i v?? in ra l???i
                    progressDialog.dismiss();
                    Log.d("TAG", "onFailure: " + e.getMessage());
                    Toast.makeText(ChatActivity.this, "Update failed. Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                    //Hi???n % ho??n th??nh vi???c ????a ???nh l??n
                    double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                    progressDialog.setMessage("Uploaded " + (int) progress + "%");
                }
            });
        }

    }
    //H??m n??y d??ng ????? ????a ???nh l??n tr??n firebase storage
    private void uploadImage() {
        //Ki???m tra ???????ng d???n file
        if (filePath != null) {
            //L???y ng??y hi???n t???i -> l???y d???ng s??? c???a ng??y l??m id cho tin nh???n
            Date today = new Date();
            String pic_id = Long.toString(today.getTime());
            //T???o object l??u th??ng tin tin nh???n
            MessageDetails mes = new MessageDetails(message_id, main_user.getPhone(),today,"message_images/" + message_id + "/" + pic_id,viewer);
            // Hi???n ProgressDialog trong khi ??ang t???i l??n
            ProgressDialog progressDialog
                    = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();
            //Khai b??o FirebaseStorage
            StorageReference storageReference= FirebaseStorage.getInstance().getReference();
            // ??i v??o nh??nh con
            StorageReference ref;
            ref = storageReference.child("message_images/" + message_id).child(pic_id);
            // T???o s??? ki??n cho vi???c upload file c??? khi th??nh c??ng hay th???t b???i v?? hi???n thanh progress theo %
            ref.putFile(filePath)
                    .addOnSuccessListener(
                            new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    // T???i ???nh l??n th??nh c??ng
                                    // T???t dialog progress ??i
                                    progressDialog.dismiss();
                                    //C???p nh???t l???i cho b???ng user v??? ?????a ch??? c???a avatar
                                    //T???o id cho tin nh???n
                                    String message_detail_id = Long.toString(Long.MAX_VALUE - today.getTime());;
                                    //T???o k???t n???i ?????n b???ng message_details
                                    DatabaseReference sendMessRef = FirebaseDatabase.getInstance().getReference("message_details");
                                    //????a d??? li???u l??n
                                    sendMessRef.child(message_id).child(message_detail_id).setValue(mes);
                                }
                            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // L???i, kh??ng t???i l??n th??nh c??ng
                            // T???t progress ??i v?? in ra l???i
                            progressDialog.dismiss();
                            Log.d("TAG", "onFailure: " + e.getMessage());
                            Toast.makeText(ChatActivity.this, "Update failed. Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        // S??? ki???n cho Progress
                        // Hi???n th??? % ho??n th??nh
                        @Override
                        public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                            progressDialog.setMessage("Uploaded " + (int) progress + "%");
                        }
                    });
        }
    }
    //CHia tr?????ng h???p khi xin quy???n xong
    public static final int REQUEST_AUDIO_PERMISSION_CODE = 1;
        //Ki???m tra quy???n c???a ???ng d???ng
    public boolean CheckPermissions() {
        // Ki???m tra c??c quy???n c?? ???????c c???p ch??a
        //Quy???n vi???t d??? li???u v??o b??? nh???
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
        //Qu???n s??? d???ng micro ghi ??m
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(), RECORD_AUDIO);
        //Quy???n d??ng m??y ???nh
        int result2 = ContextCompat.checkSelfPermission(getApplicationContext(), CAMERA);
        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED && result2 == PackageManager.PERMISSION_GRANTED;
    }
    //Xin quy???n t??? h??? th???ng
    private void RequestPermissions() {
        //Xin c???p quy???n t??? h??? th???ng
        ActivityCompat.requestPermissions(ChatActivity.this, new String[]{RECORD_AUDIO, WRITE_EXTERNAL_STORAGE, CAMERA}, REQUEST_AUDIO_PERMISSION_CODE);
    }
    // Sau khi ta ch???n ch???p nh???n hay t??? ch???i s??? tr??? k???t qu??? v??o h??m n??y

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_AUDIO_PERMISSION_CODE:
                if (grantResults.length > 0) {
                    boolean permissionToRecord = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean permissionToStore = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    boolean permissionToCamera = grantResults[2] == PackageManager.PERMISSION_GRANTED;
                    //N???u ???????c ch???p nh???n h???t
                    if (permissionToRecord && permissionToStore && permissionToCamera) {
                        //Th??ng b??o
                        Toast.makeText(getApplicationContext(), "Permission Granted", Toast.LENGTH_LONG).show();
                    } else {
                        //Ng?????c l???i th??ng b??o denied
                        Toast.makeText(getApplicationContext(), "Permission Denied", Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }
    }
    //Khai b??o bi???n d??ng cho vi???c ghi ??m
    //T??n c???a file khi l??u
    private static String mFileName = null;
    //Bi???n h??? th???ng d??ng ????? ghi ??m
    private MediaRecorder mRecorder;

    private void startRecording() {
        //Ki???m tra quy???n
        if (CheckPermissions()) {
            //T???o ?????a ch??? file l??u
            mFileName =  Environment.getExternalStorageDirectory() + File.separator
                    + Environment.DIRECTORY_DCIM + File.separator + "AudioRecording.3gp";
            //Kh???i t???o
            mRecorder = new MediaRecorder();

            //S??? d???ng micro
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mRecorder.setOutputFile(mFileName);
            try {
                // Chu???n b??? micro cho vi???c ghi ??m
                mRecorder.prepare();
            } catch (IOException e) {
                Log.e("TAG", "prepare() failed");
            }
            //B???t ?????u ghi ??m
            mRecorder.start();
        } else {
            // Ti???n h??nh xin c???p quy???n
            RequestPermissions();
        }
    }

}