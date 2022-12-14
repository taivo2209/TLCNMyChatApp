package com.hcmute.mychatapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;
import com.hcmute.mychatapp.Pattern.User_SingeTon;
import com.hcmute.mychatapp.R;
import com.hcmute.mychatapp.ZoomImageActivity;
import com.hcmute.mychatapp.model.MessageDetails;
import com.hcmute.mychatapp.model.User;

public class MessageDetailsAdapter extends RecyclerView.Adapter {
    public MessageDetailsAdapter(Context context, ArrayList<MessageDetails> arrMessDetails) {
        this.context = context;
        this.arrMessDetails = arrMessDetails;
    }

    Context context;
    ArrayList<MessageDetails> arrMessDetails;
    //G???n lo???i tin nh???n l?? c???a ng?????i g???i
    int ITEM_SEND = 1;
    //G???n lo???i tin nh???n l?? c???a ng?????i nh???n
    int ITEM_RECEIVE=2;
    ScheduledExecutorService timer;
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //N???u ng?????i g???i g???i tin nh???n
        if(viewType == ITEM_SEND) {
            //g???n view l?? tin nh???n c???a ng?????i g???i
            View view = LayoutInflater.from(context).inflate(R.layout.message_adapter_sent,parent,false);
            return new SenderViewHolder(view);
        }else{  //N???u ng?????i nh???n g???i tin nh???n
            //g???n view l?? tin nh???n c???a ng?????i nh???n
            View view = LayoutInflater.from(context).inflate(R.layout.message_adapter_received,parent,false);
            return new ReciverViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MessageDetails messageDetails = arrMessDetails.get(position);
        //N???u ng?????i g???i g???i tin nh???n
        if(holder.getClass() == SenderViewHolder.class){
            SenderViewHolder senderViewHolder = (SenderViewHolder) holder;
            //n???u message l?? d???ng h??nh ???nh
            if(messageDetails.getContent().startsWith("message_images/"+ messageDetails.getMessageId())){
                //T???o k???t n???i ?????n FirebaseStorage
                StorageReference storageReference = FirebaseStorage.getInstance().getReference(messageDetails.getContent());
                storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        //l??u ???????ng d???n h??nh ???nh
                        senderViewHolder.sendImageUri = uri;
                        //L???y ???????c Uri th??nh c??ng. D??ng picasso ????? ????a h??nh v??o Circle View
                        Picasso.get().load(uri).fit().centerCrop().into(senderViewHolder.chatPicture);
                    }
                });
                senderViewHolder.sendMessage.setVisibility(View.INVISIBLE);
                senderViewHolder.chatPicture.setVisibility(View.VISIBLE);
                senderViewHolder.linearAudioSend.setVisibility(View.INVISIBLE);

                ConstraintSet constraintSet = new ConstraintSet();
                constraintSet.clone(senderViewHolder.sendParent_layout);
                constraintSet.connect(R.id.chatPicture,ConstraintSet.RIGHT,R.id.txtTimeSent,ConstraintSet.RIGHT,0);
                constraintSet.connect(R.id.txtTimeSent,ConstraintSet.TOP,R.id.chatPicture,ConstraintSet.BOTTOM,0);
                constraintSet.applyTo(senderViewHolder.sendParent_layout);

            }
            //n???u message l?? d???ng voice clip
            else if(messageDetails.getContent().startsWith("message_records/"+ messageDetails.getMessageId())){
                //T???o k???t n???i ?????n FirebaseStorage
                StorageReference storageReference = FirebaseStorage.getInstance().getReference(messageDetails.getContent());
                storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        //T???o media ????? c?? th??? nghe file ghi ??m
                        senderViewHolder.sendMediaPlayer = new MediaPlayer();
                        senderViewHolder.sendMediaPlayer.setAudioAttributes(
                                new AudioAttributes.Builder()
                                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                        .setUsage(AudioAttributes.USAGE_MEDIA)
                                        .build()
                                );
                        try{
                            //l???y file ghi ??m t??? ???????ng d???n
                            senderViewHolder.sendMediaPlayer.setDataSource(context.getApplicationContext(),uri);
                            senderViewHolder.sendMediaPlayer.prepare();
                            //g???n seekbar ch???y
                            int millis = senderViewHolder.sendMediaPlayer.getDuration();
                            senderViewHolder.seekbarSend.setMax(millis);
                            //T???o s??? ki???n khi file ph??t ??m thanh k???t th??c
                            senderViewHolder.sendMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                @Override
                                public void onCompletion(MediaPlayer mp) {
                                    //G??n seekbar v??? v??? tri ????u
                                    timer.shutdown();
                                    senderViewHolder.seekbarSend.setProgress(0);
                                    senderViewHolder.btn_actionAudioSend.setImageResource(R.drawable.ic_play);
                                }
                            });

                        }catch (IOException e){
                            Toast.makeText(context, "Error "+e, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                senderViewHolder.linearAudioSend.setVisibility(View.VISIBLE);
                senderViewHolder.chatPicture.setVisibility(View.INVISIBLE);
                senderViewHolder.sendMessage.setVisibility(View.INVISIBLE);
            }
            else{
                senderViewHolder.sendMessage.setText(messageDetails.getContent());
                senderViewHolder.sendMessage.setVisibility(View.VISIBLE);
                senderViewHolder.chatPicture.setVisibility(View.INVISIBLE);
                senderViewHolder.linearAudioSend.setVisibility(View.INVISIBLE);

            }
            //B???m v??o n??t ????? d???ng ho???c ti???p t???c file audio
            senderViewHolder.btn_actionAudioSend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //N???u media c?? d??? li???u
                    if(senderViewHolder.sendMediaPlayer!=null){
                        //N???u nh???n v??o khi media ??ang ch???y th?? s??? d???ng l???i
                        if(senderViewHolder.sendMediaPlayer.isPlaying()) {
                            senderViewHolder.sendMediaPlayer.pause();
                            senderViewHolder.btn_actionAudioSend.setImageResource(R.drawable.ic_play);
                            timer.shutdown();
                        }
                        //N???u nh???n khi media d???ng l???i th?? b???t ?????u
                        else {
                            senderViewHolder.sendMediaPlayer.start();
                            senderViewHolder.btn_actionAudioSend.setImageResource(R.drawable.ic_pause);

                            timer = Executors.newScheduledThreadPool(1);
                            timer.scheduleAtFixedRate(new Runnable() {
                                @Override
                                public void run() {
                                    senderViewHolder.seekbarSend.setProgress(senderViewHolder.sendMediaPlayer.getCurrentPosition());
                                }
                            }, 10, 10, TimeUnit.MILLISECONDS);
                        }
                    }
                }
            });

            SimpleDateFormat simpleDateFormat;
            //ki???m tra ng??y g???i v?? ng??y hi???n t???i
            Date today = new Date();
            //N???u l?? ng??y g???i l?? ng??y hi???n t???i
            if(today.getYear() == messageDetails.getTimeSended().getYear()
            && today.getMonth() == messageDetails.getTimeSended().getMonth()
                    && today.getDate() == messageDetails.getTimeSended().getDate())
            {
                //Ch??? hi???n th???i gian b??? ??i ng??y
                simpleDateFormat = new SimpleDateFormat("hh:mm");
            }//N???u ng??y g???i kh??ng ph???i l?? ng??y hi???n t???i
            else {
                //Hi???n ng??y g???i c??ng v???i th???i gian
                simpleDateFormat = new SimpleDateFormat("dd-M hh:mm");
            }
            senderViewHolder.txtTimeSent.setText(simpleDateFormat.format(messageDetails.getTimeSended()));
            //B???m v??o h??nh ???nh ????? xem h??nh ???nh ??? ch??? ????? ph??ng to
            senderViewHolder.chatPicture.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Chuy???n sang activity ph??ng to h??nh c??ng v???i ???????ng d???n
                    Intent zoomImageIntent = new Intent(context, ZoomImageActivity.class);
                    zoomImageIntent.putExtra("uri",senderViewHolder.sendImageUri.toString());
                    context.startActivity(zoomImageIntent);
                }
            });

        } //N???u ng?????i g???i g???i tin nh???n
        else{
            //n???u message l?? d???ng h??nh ???nh
            ReciverViewHolder reciverViewHolder = (ReciverViewHolder) holder;
            if(messageDetails.getContent().startsWith("message_images/"+ messageDetails.getMessageId())){
                //T???o k???t n???i ?????n FirebaseStorage
                StorageReference storageReference = FirebaseStorage.getInstance().getReference(messageDetails.getContent());
                storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        //l??u ???????ng d???n c???a h??nh ???nh
                        reciverViewHolder.receiveImageUri = uri;
                        //L???y ???????c Uri th??nh c??ng. D??ng picasso ????? ????a h??nh v??o Circle View
                        Picasso.get().load(uri).fit().centerCrop().into(reciverViewHolder.chatPicture);
                    }
                });
                reciverViewHolder.receiveMessage.setVisibility(View.INVISIBLE);
                reciverViewHolder.linearAudioReceive.setVisibility(View.INVISIBLE);
                reciverViewHolder.chatPicture.setVisibility(View.VISIBLE);

                ConstraintSet constraintSet = new ConstraintSet();
                constraintSet.clone(reciverViewHolder.receiveParent_layout);
                constraintSet.connect(R.id.chatPicture,ConstraintSet.START,R.id.receiveTime,ConstraintSet.START,0);
                constraintSet.connect(R.id.receiveTime,ConstraintSet.TOP,R.id.chatPicture,ConstraintSet.BOTTOM,0);
                constraintSet.applyTo(reciverViewHolder.receiveParent_layout);

            }//n???u message l?? voice clip
            else if(messageDetails.getContent().startsWith("message_records/"+ messageDetails.getMessageId())){
                //T???o k???t n???i ?????n FirebaseStorage
                StorageReference storageReference = FirebaseStorage.getInstance().getReference(messageDetails.getContent());
                storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        //l???y file ghi ??m t??? ???????ng d???n
                        reciverViewHolder.receiveMediaPlayer = new MediaPlayer();
                        reciverViewHolder.receiveMediaPlayer.setAudioAttributes(
                                new AudioAttributes.Builder()
                                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                        .setUsage(AudioAttributes.USAGE_MEDIA)
                                        .build()
                        );
                        try{
                            //????a d??? li???u v??o media
                            reciverViewHolder.receiveMediaPlayer.setDataSource(context.getApplicationContext(),uri);
                            reciverViewHolder.receiveMediaPlayer.prepare();
                            //g???n seekbar ch???y
                            int millis = reciverViewHolder.receiveMediaPlayer.getDuration();
                            reciverViewHolder.seekbarReceive.setMax(millis);
                            //t???o s??? ki???n khi media k???t th??c
                            reciverViewHolder.receiveMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                @Override
                                public void onCompletion(MediaPlayer mp) {
                                    //G??n seekbar v??? v??? tri ????u
                                    timer.shutdown();
                                    reciverViewHolder.seekbarReceive.setProgress(0);
                                    reciverViewHolder.btn_actionAudioReceive.setImageResource(R.drawable.ic_play);
                                }
                            });

                        }catch (IOException e){
                            Toast.makeText(context, "Error "+e, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                reciverViewHolder.linearAudioReceive.setVisibility(View.VISIBLE);
                reciverViewHolder.receiveMessage.setVisibility(View.INVISIBLE);
                reciverViewHolder.chatPicture.setVisibility(View.INVISIBLE);
            }
            else {
                reciverViewHolder.receiveMessage.setText(messageDetails.getContent());
                reciverViewHolder.receiveMessage.setVisibility(View.VISIBLE);
                reciverViewHolder.chatPicture.setVisibility(View.INVISIBLE);
                reciverViewHolder.linearAudioReceive.setVisibility(View.INVISIBLE);

            }
            //l???y ???nh ?????i di???n c???a ng?????i nh???n
            //Ti???n h??nh t??m ki???m tr??n FirebaseDatabase
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference myRef = database.getReference("users");
            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    //l???y user trong database
                    DataSnapshot dataSnapshot = snapshot.child(messageDetails.getSenderPhone());
                    User user = dataSnapshot.getValue(User.class);
                    //Ki???m tra n???u ???? c?? ???nh m???i th???c hi???n l???y ???nh ?????i di???n
                    if(!user.getAvatar().equals("")) {
                        //????a d??? li???u cho ???nh ?????i di???n d??ng Firebase Storage
                        FirebaseStorage storage = FirebaseStorage.getInstance();
                        StorageReference storageReference = storage.getReference(user.getAvatar());
                        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                //L???y ???????c Uri th??nh c??ng. D??ng picasso ????? ????a h??nh v??o Circle View ???nh ?????i di???n
                                Picasso.get().load(uri).fit().centerCrop().into(reciverViewHolder.imgUser);

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
            //reciverViewHolder.receiveMessage.setText(messageDetails.getContent());
           // Picasso.get().load(uri).fit().centerCrop().into(holder.imageBoxChat);
            //B???m v??o n??t ????? d???ng ho???c ti???p t???c file audio
            reciverViewHolder.btn_actionAudioReceive.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //N???u media c?? d??? li???u
                    if(reciverViewHolder.receiveMediaPlayer != null){
                        //N???u nh???n v??o khi media ??ang ch???y th?? s??? d???ng l???i
                        if(reciverViewHolder.receiveMediaPlayer.isPlaying()) {
                            reciverViewHolder.receiveMediaPlayer.pause();
                            reciverViewHolder.btn_actionAudioReceive.setImageResource(R.drawable.ic_play);
                            timer.shutdown();
                        }//N???u media ??ang d???ng th?? s??? ti???p t???c ch???y
                        else {
                            reciverViewHolder.receiveMediaPlayer.start();
                            reciverViewHolder.btn_actionAudioReceive.setImageResource(R.drawable.ic_pause);

                            timer = Executors.newScheduledThreadPool(1);
                            timer.scheduleAtFixedRate(new Runnable() {
                                @Override
                                public void run() {
                                    reciverViewHolder.seekbarReceive.setProgress(reciverViewHolder.receiveMediaPlayer.getCurrentPosition());
                                }
                            }, 10, 10, TimeUnit.MILLISECONDS);
                        }
                    }
                }
            });
            SimpleDateFormat simpleDateFormat;
            //Ki???m tra ng??y g???i v?? ng??y hi???n t???i
            Date today = new Date();
            //N???u ng??y g???i l?? ng??y hi???n t???i
            if(today.getYear() == messageDetails.getTimeSended().getYear()
                    && today.getMonth() == messageDetails.getTimeSended().getMonth()
                    && today.getDate() == messageDetails.getTimeSended().getDate())
            {
                //Ch??? hi???n th???i gian b??? ??i ng??y
                simpleDateFormat = new SimpleDateFormat("hh:mm");;
            }//N???u ng??y g???i kh??ng ph???i ng??y hi???n t???i
            else {
                //Hi???n ng??y c??ng v???i th???i gian g???i
                simpleDateFormat = new SimpleDateFormat("dd-M hh:mm");
            }
            reciverViewHolder.receiveTime.setText(simpleDateFormat.format(messageDetails.getTimeSended()));
            //B???m v??o h??nh ???nh ????? xem h??nh ???nh ??? ch??? ????? ph??ng to
            reciverViewHolder.chatPicture.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Chuy???n sang activity ph??ng to h??nh c??ng v???i ???????ng d???n
                    Intent zoomImageIntent = new Intent(context, ZoomImageActivity.class);
                    zoomImageIntent.putExtra("uri",reciverViewHolder.receiveImageUri.toString());
                    context.startActivity(zoomImageIntent);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return arrMessDetails.size();
    }

    @Override
    public int getItemViewType(int position) {
        User user = User_SingeTon.getInstance().getUser();
        MessageDetails messageDetails = arrMessDetails.get(position);
        //Ki???m tra n???u ng?????i g???i l?? ng?????i d??ng hi???n t???i th?? g??n tin nh???n g???i
        if(messageDetails.getSenderPhone().equals(user.getPhone())) {
            return ITEM_SEND;
        }//Ki???m tra n???u ng?????i g???i kh??ng l?? ng?????i d??ng hi???n t???i th?? g??n tin nh???n nh???n
        else {
            return ITEM_RECEIVE;
        }
    }

    class SenderViewHolder extends RecyclerView.ViewHolder {
        TextView sendMessage;
        TextView txtTimeSent;
        LinearLayout linearAudioSend;
        ConstraintLayout sendParent_layout;
        ImageView chatPicture,btn_actionAudioSend;
        SeekBar seekbarSend;
        MediaPlayer sendMediaPlayer;
        Uri sendImageUri;
        public SenderViewHolder(@NonNull View itemView) {
            super(itemView);
            //??nh x??? c??c view
            sendMessage = itemView.findViewById(R.id.sendMessage);
            txtTimeSent = itemView.findViewById(R.id.txtTimeSent);
            chatPicture = itemView.findViewById(R.id.chatPicture);
            btn_actionAudioSend = itemView.findViewById(R.id.btn_actionAudioSend);
            seekbarSend = itemView.findViewById(R.id.seekbarSend);
            linearAudioSend = itemView.findViewById(R.id.linearAudioSend);
            sendParent_layout = itemView.findViewById(R.id.sendParent_layout);
        }
    }

    class ReciverViewHolder extends RecyclerView.ViewHolder {
        LinearLayout linearAudioReceive;
        ConstraintLayout receiveParent_layout;
        TextView receiveMessage;
        CircleImageView imgUser;
        TextView receiveTime;
        SeekBar seekbarReceive;
        ImageView chatPicture,btn_actionAudioReceive;
        MediaPlayer receiveMediaPlayer;
        Uri receiveImageUri;
        public ReciverViewHolder(@NonNull View itemView) {
            super(itemView);
            //??nh x??? c??c view
            imgUser = itemView.findViewById(R.id.imgUser);
            receiveMessage = itemView.findViewById(R.id.receiveMessage);
            receiveTime = itemView.findViewById(R.id.receiveTime);
            chatPicture = itemView.findViewById(R.id.chatPicture);
            btn_actionAudioReceive = itemView.findViewById(R.id.btn_actionAudioReceive);
            seekbarReceive = itemView.findViewById(R.id.seekbarReceive);
            linearAudioReceive = itemView.findViewById(R.id.linearAudioReceive);
            receiveParent_layout = itemView.findViewById(R.id.receiveParent_layout);
        }
    }

}
