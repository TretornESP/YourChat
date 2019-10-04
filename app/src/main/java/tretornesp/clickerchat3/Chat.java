package tretornesp.clickerchat3;

import android.Manifest;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.media.ExifInterface;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.format.DateFormat;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

import static android.media.ThumbnailUtils.createVideoThumbnail;

public class Chat extends AppCompatActivity {

    private static final int REQUEST_CODE = 1;
    private static final int MAX_USERS = 49;

    private ArrayList<ChatMessage> attachments;
    private FirebaseListAdapter<ChatMessage> adapter;

    private static String database;
    private boolean auth;
    private Group group;
    private Context mContext;
    private Uri data;
    RelativeLayout activity_main;
    ImageView fab;
    ImageView attach;

    private ColorStateList defaultColors;

    private int[] colors;
    Map <String, Integer> colorMap;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (getIntent().hasExtra("just_created")) {
            if (getIntent().getStringExtra("just_created").equals("true")) {

                Intent intent = new Intent(Chat.this, GroupList.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                finish();
                startActivity(intent);
            } else {
                super.onBackPressed();
            }
        } else if (getIntent().hasExtra("global")) {
            if (getIntent().getStringExtra("global").equals("true")) {
                Intent intent = new Intent(Chat.this, GroupList.class);
                intent.putExtra("global", "true");
                finish();
                startActivity(intent);
            }
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == this.RESULT_CANCELED) {
            return;
        }
        if (requestCode == REQUEST_CODE) {
            if (data != null) {
                this.data = data.getData();
                new AlertDialog.Builder(mContext)
                        .setTitle("Enviar archivo")
                        .setMessage("¿Desea enviar el documento?")
                        .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                uploadThumbnail();
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
            }

        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId()==R.id.list_of_message) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.message_list_menu, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch(item.getItemId()) {
            case R.id.download_attachment:
                if (adapter.getItem(info.position).getIsAttachment().equals("true")) {
                    if (ContextCompat.checkSelfPermission(Chat.this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {

                        ActivityCompat.requestPermissions(Chat.this,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                1
                        );
                    } else {
                        downloadFile(adapter.getItem(info.position).getMessageText(), adapter.getItem(info.position).getFileName());
                    }
                }
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        checkForErrors();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_layout);

        mContext = Chat.this;
        attachments = new ArrayList<>();
        group = ActiveGroup.activeGroup;

        //registerInGroup();

        database = group.getDatabase();

        updateColors();
        setupToolbar();
        updateAuth();

        if (!auth) {
            if (getIntent().hasExtra("global")) {
                if (getIntent().getStringExtra("global").equals("true")) {
                    if (ActiveGroup.activeGroup.getUsers().contains(ActiveUser.activeuser.getUid()) ||
                            ActiveGroup.activeGroup.getAdmins().contains(ActiveUser.activeuser.getUid())) {

                    } else {
                        if (ActiveGroup.activeGroup.getNumero_usuarios()>=MAX_USERS) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                            builder.setTitle("El grupo está lleno");

                            builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            });
                            builder.setCancelable(false);
                            builder.show();
                        } else {
                            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                            builder.setTitle("Desea unirse al grupo?");

                            builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    registerInGroup();
                                }
                            });
                            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(Chat.this, GroupList.class);
                                    intent.putExtra("global", "true");
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    finish();
                                    startActivity(intent);
                                }
                            });
                            builder.setCancelable(false);

                            builder.show();
                        }
                    }
                }
            }
        }
        addListener();

        activity_main = findViewById(R.id.chat_layout);
        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateAuth();
                if (!auth) return;
                EditText input = findViewById(R.id.input);
                if (!input.getText().toString().equals("")) {
                    System.out.println("ENVIANDO:" + input.getText());
                    FirebaseDatabase.getInstance().getReference().child(database).push().setValue(new ChatMessage(input.getText().toString(),
                            Objects.requireNonNull(ActiveUser.activeuser.getUid()), ActiveUser.activeuser.getName(), "false", "", "", ""));
                    input.setText("");
                } else {
                    System.out.println("MENSAJE VACIO");
                }
            }
        });

        attach = findViewById(R.id.attach);
        attach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateAuth();
                if (!auth) return;
                attach();
            }
        });

        //Check if not sign-in then navigate signin page
    }

    private void updateColors() {
        colors = getResources().getIntArray(R.array.user_colors);
    }

    private void displayChatMessage() {

        if (!auth) {return;}

        if (colors == null || colors.length!=(ActiveGroup.activeGroup.getNumero_usuarios())) {
            updateColors();
        }

        ListView listOfMessages = findViewById(R.id.list_of_message);
        registerForContextMenu(listOfMessages);

        Query query = FirebaseDatabase.getInstance().getReference().child(database);

        colorMap = new HashMap<>();

        int count;
        for (count = 0; count < ActiveGroup.activeGroup.getUsers().size(); count++) {
            if (ActiveGroup.activeGroup.getUsers().get(count).equals(ActiveUser.activeuser.getUid())) {
                colors[count] = getResources().getColor(R.color.black);
                colorMap.put(ActiveGroup.activeGroup.getUsers().get(count), colors[count]);
            } else {
                colorMap.put(ActiveGroup.activeGroup.getUsers().get(count), colors[count]);
            }
        }

        for (int i = 0; i < ActiveGroup.activeGroup.getAdmins().size();i++) {
            if (ActiveGroup.activeGroup.getAdmins().get(i).equals(ActiveUser.activeuser.getUid())) {
                colors[count+i] = getResources().getColor(R.color.black);
                colorMap.put(ActiveGroup.activeGroup.getAdmins().get(i), colors[count+i]);
            } else {
                colorMap.put(ActiveGroup.activeGroup.getAdmins().get(i), colors[count + i]);
            }
        }

        FirebaseListOptions<ChatMessage> options = new FirebaseListOptions.Builder<ChatMessage>()
                .setQuery(query, ChatMessage.class)
                .setLayout(R.layout.list_item)
                .setLifecycleOwner(this)
                .build();


        adapter = new FirebaseListAdapter<ChatMessage>(options) {
            @Override
            protected void populateView(View v, ChatMessage model, int position) {
                //Get references to the views of list_item.xml
                if (Attachment(model, v)) {

                } else{
                    TextView messageText, messageUser, messageTime;
                    ImageView messageImage, messagePlay;
                    RelativeLayout message;

                    View margin;

                    margin = v.findViewById(R.id.margin);

                    messageText = v.findViewById(R.id.message_text);
                    messageUser = v.findViewById(R.id.message_user);
                    messageTime = v.findViewById(R.id.message_time);
                    messagePlay = v.findViewById(R.id.message_image_play);

                    message = v.findViewById(R.id.list_item);
                    String key = model.getMessageUser();

                    if (model.getMessageUser().equals(ActiveUser.activeuser.getUid())) {
                        margin.setVisibility(View.VISIBLE);
                    } else {
                        margin.setVisibility(View.GONE);
                    }

                    if (colorMap.get(key) != null) {
                        messageUser.setTextColor(colorMap.get(key));
                    }

                    /*RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)message.getLayoutParams();
                    if (model.getMessageUser().equals(ActiveUser.activeuser.getUid())) {
                        params.addRule();
                    }*/

                    messageImage = v.findViewById(R.id.message_image);
                    messageText.setText(model.getMessageText());
                    messageUser.setText(model.getMessageName());
                    messageTime.setText(DateFormat.format("HH:mm", model.getMessageTime()));

                    messageImage.setVisibility(View.GONE);
                    messagePlay.setVisibility(View.GONE);
                    messageText.setVisibility(View.VISIBLE);
                    messageTime.setVisibility(View.VISIBLE);
                    messageUser.setVisibility(View.VISIBLE);


                    v.findViewById(R.id.message_time_attachment).setVisibility(View.GONE);
                    v.findViewById(R.id.message_user_attachment).setVisibility(View.GONE);
                }
            }
        };
        listOfMessages.setAdapter(adapter);
    }

    private boolean Attachment(ChatMessage model, View v) {
        if (model.getIsAttachment().toLowerCase().equals("true")) {
            TextView messageText, messageUser, messageTime;
            TextView messageTime_attachment, messageUser_attachment;
            ImageView messageImage, messagePlay;
            View margin;
            RelativeLayout message;

            messageText = v.findViewById(R.id.message_text);
            messageUser = v.findViewById(R.id.message_user);
            messageTime = v.findViewById(R.id.message_time);
            messagePlay = v.findViewById(R.id.message_image_play);

            message = v.findViewById(R.id.list_item_internal);

            margin = v.findViewById(R.id.margin);


            if (model.getMessageUser().equals(ActiveUser.activeuser.getUid())) {
                margin.setVisibility(View.VISIBLE);
            } else {
                margin.setVisibility(View.GONE);
            }

            String key = model.getMessageUser();

            if (colorMap.get(key) != null) {
                messageUser.setTextColor(colorMap.get(key));
            }

            ViewGroup.LayoutParams layoutParams = message.getLayoutParams();
            layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            message.setLayoutParams(layoutParams);

            messageImage = v.findViewById(R.id.message_image);

            messageText.setText(model.getFileName());
            messageUser.setText(model.getMessageName());
            messageTime.setText(DateFormat.format("HH:mm", model.getMessageTime()));

            messageImage.setVisibility(View.VISIBLE);
            messageText.setVisibility(View.GONE);
            messageTime.setVisibility(View.GONE);
            messageUser.setVisibility(View.VISIBLE);
            messagePlay.setVisibility(View.GONE);

            messageTime_attachment = v.findViewById(R.id.message_time_attachment);
            messageTime_attachment.setVisibility(View.VISIBLE);
            messageUser_attachment = v.findViewById(R.id.message_user_attachment);
            messageUser_attachment.setVisibility(View.GONE);

            messageTime_attachment.setText(DateFormat.format("HH:mm", model.getMessageTime()));
            messageUser_attachment.setText(model.getFileName());

            messageImage.setVisibility(View.VISIBLE);
            messageImage.setImageResource(R.drawable.image_attach_file_white);
            loadAttachmentImage(messageImage, messagePlay, model, v);

            return true;
        }
        return false;
    }

    private void loadAttachmentImage(ImageView image, ImageView play, ChatMessage model, View v) {
        switch (model.getFileType()) {
            case "jpg": {
                loadAttachmentIsImage(image, model);
                break;
            }
            case "jpeg": {
                loadAttachmentIsImage(image, model);
                break;
            }
            case "png": {
                loadAttachmentIsImage(image, model);
                break;
            }
            case "gif": {
                loadAttachmentIsImage(image, model);
                break;
            }
            case "bmp": {
                loadAttachmentIsImage(image, model);
                break;
            }
            case "mp4": {
                loadAttachmentIsVideo(image, play, model);
                break;
            }
            case "pdf": {
                v.findViewById(R.id.message_user).setVisibility(View.VISIBLE);
                v.findViewById(R.id.message_time).setVisibility(View.VISIBLE);
                v.findViewById(R.id.message_text).setVisibility(View.VISIBLE);
                TextView text = v.findViewById(R.id.message_text);
                text.setText(model.getFileName());
                v.findViewById(R.id.message_image_play).setVisibility(View.GONE);
                v.findViewById(R.id.message_user_attachment).setVisibility(View.GONE);
                v.findViewById(R.id.message_time_attachment).setVisibility(View.GONE);
                loadAttachmentIsPdf(image, model);
                break;
            }
            default: {
                v.findViewById(R.id.message_user).setVisibility(View.VISIBLE);
                v.findViewById(R.id.message_time).setVisibility(View.VISIBLE);
                v.findViewById(R.id.message_text).setVisibility(View.VISIBLE);
                TextView text = v.findViewById(R.id.message_text);
                text.setText(model.getFileName());
                v.findViewById(R.id.message_image_play).setVisibility(View.GONE);
                v.findViewById(R.id.message_user_attachment).setVisibility(View.GONE);
                v.findViewById(R.id.message_time_attachment).setVisibility(View.GONE);
                loadAttachmentIsOther(image, model);
            }
        }
    }

    private void loadAttachmentIsImage(final ImageView image, final ChatMessage model) {
        final ImageLoader imageLoader = ImageLoader.getInstance();

        int defaultImage = mContext.getResources().getIdentifier("@drawable/image_default", null, mContext.getPackageName());

        final DisplayImageOptions options = new DisplayImageOptions.Builder().cacheInMemory(true)
                .cacheOnDisc(true).resetViewBeforeLoading(true)
                .showImageForEmptyUri(defaultImage)
                .showImageOnFail(defaultImage)
                .showImageOnLoading(defaultImage).build();


        imageLoader.displayImage(model.getMessageText(), image, options);

    }

    private void loadAttachmentIsPdf(ImageView image, ChatMessage model) {
        image.setImageResource(R.drawable.pdf_attach_file_white);
    }

    private void loadAttachmentIsVideo(final ImageView image, ImageView play, final ChatMessage model) {
        System.out.println("ES VIDEO EH");
        Map<String, Integer> map = new HashMap<String, Integer>();
        map.put("attach", R.drawable.image_attach_video_file_white);
        image.setImageResource(map.get("attach"));

        Bitmap bmFrame;
        try {
            final ImageLoader imageLoader = ImageLoader.getInstance();

            int defaultImage = mContext.getResources().getIdentifier("@drawable/image_default", null, mContext.getPackageName());

            final DisplayImageOptions options = new DisplayImageOptions.Builder().cacheInMemory(true)
                    .cacheOnDisc(true).resetViewBeforeLoading(true)
                    .showImageForEmptyUri(defaultImage)
                    .showImageOnFail(defaultImage)
                    .showImageOnLoading(defaultImage).build();


            imageLoader.displayImage(getThumbnail(model.getMessageText()), image, options);
        } catch (Throwable t) {
            t.printStackTrace();
        }

        play.setVisibility(View.VISIBLE);

        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(model.getMessageText()));
                intent.setDataAndType(Uri.parse(getVideo(model.getMessageText())), "video/mp4");
                startActivity(intent);
            }
        });
    }

    private void loadAttachmentIsOther(ImageView image, ChatMessage model) {
        Map<String, Integer> map = new HashMap<String, Integer>();
        map.put("attach", R.drawable.image_attach_file_white);
        image.setImageResource(map.get("attach"));
    }

    private void setupImageLoader() {
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheOnDisk(true).cacheInMemory(true)
                .imageScaleType(ImageScaleType.EXACTLY)
                .displayer(new FadeInBitmapDisplayer(300)).build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(mContext)
                .defaultDisplayImageOptions(defaultOptions)
                .memoryCache(new WeakMemoryCache())
                .discCacheSize(1000 * 1024 * 1024).build();

        ImageLoader.getInstance().init(config);
    }

    private void setupToolbar() {
        setupImageLoader();

        ImageLoader imageLoader = ImageLoader.getInstance();

        int defaultImage = mContext.getResources().getIdentifier("@drawable/image_default", null, mContext.getPackageName());

        DisplayImageOptions options = new DisplayImageOptions.Builder().cacheInMemory(true)
                .cacheOnDisc(true).resetViewBeforeLoading(true)
                .showImageForEmptyUri(defaultImage)
                .showImageOnFail(defaultImage)
                .showImageOnLoading(defaultImage).build();

        Toolbar mToolbar = findViewById(R.id.chat_toolbar);
        setSupportActionBar(mToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ImageView imageView = findViewById(R.id.conversation_contact_photo);
        imageLoader.displayImage(group.getImageURL(), imageView, options);
        TextView name = findViewById(R.id.action_bar_title_1);
        name.setText(group.getName());

        mToolbar.findViewById(R.id.chat_toolbar_clickable).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Chat.this, GroupInfo.class);
                startActivity(intent);
                finish();
            }
        });

        TextView usuarios = findViewById(R.id.action_bar_title_2);
        usuarios.setText(group.getNumero_usuarios_asString());
    }

    private void checkForErrors() {
        if (ActiveGroup.activeGroup == null) {
            finish();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (getIntent().hasExtra("returnToGroupInfo")) {
            if (getIntent().getStringExtra("returnToGroupInfo").equals("true")) {
                Intent intent = new Intent(Chat.this, GroupInfo.class);
                finish();
                startActivity(intent);
            }
        }
        if (ActiveGroup.activeGroup.getId()==null) {
            return;
        }
        updateAuth();
    }

    private void updateAuth() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        if (ActiveGroup.activeGroup == null) {
            auth = false;
            return;
        } else if (ActiveGroup.activeGroup.getId() == null){
            return;
        }
        final DocumentReference docRef = db.collection("chats").document(ActiveGroup.activeGroup.getId());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        ArrayList<String> users = (ArrayList<String>) document.get("users");
                        ArrayList<String> admins = (ArrayList<String>) document.get("admins");

                        for (String s: users) {
                            if (s.equals(ActiveUser.activeuser.getUid())) {
                                auth = true;
                                displayChatMessage();
                                return;
                            }
                        }

                        for (String s: admins) {
                            if (s.equals(ActiveUser.activeuser.getUid())) {
                                auth = true;
                                displayChatMessage();
                                return;
                            }
                        }
                        auth=false;
                    }
                }
            }
        });
    }

    private void attach() {
        String manufacturer = Build.MANUFACTURER;
        if (ContextCompat.checkSelfPermission(Chat.this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED
                ||

                ContextCompat.checkSelfPermission(Chat.this,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED
                ) {

            ActivityCompat.requestPermissions(Chat.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    1
            );
        } else {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                startActivityForResult(intent, REQUEST_CODE);
        }
    }

    private void uploadThumbnail() {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference();

        if (ActiveGroup.activeGroup.getId()==null) return;

        final String uri = "group_files/" + ActiveGroup.activeGroup.getId() + "/" + UUID.randomUUID().toString();

        final StorageReference child = storageReference.child(uri.concat("_thumbnail"));

        ContentResolver cR = mContext.getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        String fileType = mime.getExtensionFromMimeType(cR.getType(Objects.requireNonNull(data)));

        if (!Objects.equals(fileType, "mp4")) {
            uploadImage(uri, "");
            return;
        }

        Bitmap thumbnail = ThumbnailUtils.createVideoThumbnail(getPath(mContext, data), MediaStore.Images.Thumbnails.MINI_KIND);

        UploadTask uploadTask  = child.putFile(getImageUri(mContext, thumbnail));

        RelativeLayout layout = findViewById(R.id.chat_layout);
        Snackbar.make(layout, "Subiendo archivo", Snackbar.LENGTH_SHORT).show();

        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    SizeExceeded.exceeded(mContext);
                    throw Objects.requireNonNull(task.getException());
                }
                return child.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {

                if (task.isSuccessful()) {
                    uploadImage(uri, task.getResult().toString());
                }
            }
        });
    }

    private void uploadImage(String uri, final String thumbnailDownloadUrl) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference();

        final StorageReference child = storageReference.child(uri);

        UploadTask uploadTask  = child.putFile(data);

        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    SizeExceeded.exceeded(mContext);
                    throw task.getException();
                }
                return child.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {

                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    RelativeLayout layout = findViewById(R.id.chat_layout);
                    Snackbar.make(layout, "Subida completa", Snackbar.LENGTH_SHORT).show();
                    String fileURL = downloadUri.toString();

                    //File file = new File(data.getPath());
                    //String fileSize = ""+file.length();
                    //String fileName = file.getName();

                   Cursor returnCursor =
                            getContentResolver().query(data, null, null, null, null);
                    int sizeIndex = Objects.requireNonNull(returnCursor).getColumnIndex(OpenableColumns.SIZE);
                    int typeIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    returnCursor.moveToFirst();
                    String fileSize = Long.toString(returnCursor.getLong(sizeIndex));
                    String fileName = returnCursor.getString(typeIndex);
                    returnCursor.close();


                    /* File file = new File(data.getPath());
                    String fileType = getFileExtension(file);
                    String fileSize = ""+file.length();
                    String fileName = file.getName(); */

                   ContentResolver cR = mContext.getContentResolver();
                    MimeTypeMap mime = MimeTypeMap.getSingleton();
                    String fileType = mime.getExtensionFromMimeType(cR.getType(data));
                    if (!thumbnailDownloadUrl.equals("")) {
                        FirebaseDatabase.getInstance().getReference().child(database).push().setValue(new ChatMessage(fileURL.concat("&THUMBNAIL:".concat(thumbnailDownloadUrl)),
                                Objects.requireNonNull(ActiveUser.activeuser.getUid()), ActiveUser.activeuser.getName(), "true", fileSize, fileType, fileName));
                    } else {
                        FirebaseDatabase.getInstance().getReference().child(database).push().setValue(new ChatMessage(fileURL,
                                Objects.requireNonNull(ActiveUser.activeuser.getUid()), ActiveUser.activeuser.getName(), "true", fileSize, fileType, fileName));
                    }
                }
            }
        });
    }

    private void addListener() {
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        if (ActiveGroup.activeGroup.getId()==null) {
            return;
        }
        final DocumentReference docRef = db.collection("chats").document(ActiveGroup.activeGroup.getId());
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    if (!auth || ActiveGroup.activeGroup==null) return;
                    Group tmp_group = new Group();
                    tmp_group.setId(snapshot.getString("database"));
                    tmp_group.setImageURL(snapshot.getString("imageURL"));
                    tmp_group.setName(snapshot.getString("name"));
                    tmp_group.setDescription(snapshot.getString("description"));
                    tmp_group.setDatabase(snapshot.getString("database"));
                    tmp_group.setUsers((ArrayList<String>) snapshot.get("users"));
                    tmp_group.setAdmins((ArrayList<String>) snapshot.get("admins"));
                    tmp_group.setCategory(snapshot.getString("category"));
                    tmp_group.updated();
                    
                    if (FirestoreLink.checkIntegrity(tmp_group)) {
                        ActiveGroup.activeGroup = tmp_group;
                        group = ActiveGroup.activeGroup;

                        updateColors();
                        setupToolbar();
                    }
                }
            }
        });
    }

    private void downloadFile(String url, String filename) {
        RelativeLayout layout = findViewById(R.id.chat_layout);
        Snackbar.make(layout, "Descargando archivo", Snackbar.LENGTH_SHORT).show();

        Uri uri =  Uri.parse(url);
        final DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED); // to notify when download is complete
        request.allowScanningByMediaScanner();// if you want to be available from media players
        final DownloadManager manager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        manager.enqueue(request);
        BroadcastReceiver onComplete=new BroadcastReceiver() {
            public void onReceive(Context ctxt, Intent intent) {
                // POR IMPLEMENTAR
            }
        };
        registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));


    }

    private void registerInGroup() {
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("chats").document(ActiveGroup.activeGroup.getId());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String name = document.getString("name");
                        String description = document.getString("description");
                        String imageURL = document.getString("imageURL");
                        String database = document.getString("database");
                        String category = document.getString("category");

                        ArrayList<String> users = (ArrayList<String>) document.get("users");
                        users.add(ActiveUser.activeuser.getUid());
                        ArrayList<String> admins = (ArrayList<String>) document.get("admins");

                        Group group_tmp = new Group();
                        group_tmp.setName(name);
                        group_tmp.setDescription(description);
                        group_tmp.setImageURL(imageURL);
                        group_tmp.setDatabase(database);
                        group_tmp.setUsers(users);
                        group_tmp.setAdmins(admins);
                        group_tmp.setCategory(category);

                        Map<String, Object> group = new HashMap<>();
                        group.put("name", name);
                        group.put("description", description);
                        group.put("imageURL", imageURL);
                        group.put("database", database);
                        group.put("users", users);
                        group.put("admins", admins);
                        group.put("category", category);

                        if (FirestoreLink.checkIntegrity(group_tmp)) {
                            db.collection("chats").document(ActiveGroup.activeGroup.getId()).set(group).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        registerInGroupUser();
                                    }
                                }
                            });
                        }
                    }
                }
            }
        });
    }

    private void registerInGroupUser() {
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("users").document(ActiveUser.activeuser.getUid());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String name = document.getString("name");
                        String status = document.getString("status");
                        String imageURL = document.getString("imageURL");
                        String location = document.getString("location");

                        ArrayList<String> categories = (ArrayList<String>) document.get("categories");
                        ArrayList<String> groups = (ArrayList<String>) document.get("groups");
                        groups.add(ActiveGroup.activeGroup.getId());

                        User user_tmp = new User();

                        user_tmp.setUid(ActiveUser.activeuser.getUid());
                        user_tmp.setName(name);
                        user_tmp.setStatus(status);
                        user_tmp.setImageURL(imageURL);
                        user_tmp.setLocation(location);
                        user_tmp.setCategories(categories);
                        user_tmp.setGroups(groups);

                        Map<String, Object> user = new HashMap<>();
                        user.put("name", name);
                        user.put("status", status);
                        user.put("imageURL", imageURL);
                        user.put("location", location);
                        user.put("categories", categories);
                        user.put("groups", groups);

                        if (FirestoreLink.checkIntegrity(user_tmp)) {
                            db.collection("users").document(ActiveUser.activeuser.getUid()).set(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        System.out.println("USUARIO REGISTRADO CORRECTAMENTE");
                                    }
                                }
                            });
                        }
                    }
                }
            }
        });
        ArrayList<String> users = group.getUsers();
        users.add(ActiveUser.activeuser.getUid());
        group.setUsers(users);
        TextView usuarios = findViewById(R.id.action_bar_title_2);
        usuarios.setText(group.getNumero_usuarios_asString());
    }

    protected void openFile(String fileName) {
        Intent install = new Intent(Intent.ACTION_VIEW);
        install.setDataAndType(Uri.fromFile(new File(fileName)),
                "MIME-TYPE");
        startActivity(install);
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        if (inImage == null) {
            System.err.println("cant parse thumbnail");
            return Uri.parse("");
        } else {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "thumbnail", null);
            return Uri.parse(path);
        }
    }

    private static String getFileExtension(File file) {
        String fileName = file.getName();
        if(fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0)
            return fileName.substring(fileName.lastIndexOf(".")+1);
        else return "";
    }

    public static Bitmap retriveVideoFrameFromVideo(String toParse) throws Throwable
    {
        String[] videoPathArray = toParse.split("&THUMBNAIL:", 2);
        String videoPath = videoPathArray[1];
        System.out.println(videoPath);

        Bitmap bitmap = null;
        MediaMetadataRetriever mediaMetadataRetriever = null;
        try
        {
            mediaMetadataRetriever = new MediaMetadataRetriever();
            if (Build.VERSION.SDK_INT >= 14)
                mediaMetadataRetriever.setDataSource(videoPath, new HashMap<String, String>());
            else
                mediaMetadataRetriever.setDataSource(videoPath);
            //   mediaMetadataRetriever.setDataSource(videoPath);
            bitmap = mediaMetadataRetriever.getFrameAtTime();
        } catch (Exception e) {
            e.printStackTrace();
            throw new Throwable("Exception in retriveVideoFrameFromVideo(String videoPath)" + e.getMessage());

        } finally {
            if (mediaMetadataRetriever != null) {
                mediaMetadataRetriever.release();
            }
        }
        return bitmap;
    }

    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public static MultipartBody.Part getMultiPartBody(String key, String mMediaUrl) {
        if (mMediaUrl != null) {
            File file = new File(mMediaUrl);
            RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
            return MultipartBody.Part.createFormData(key, file.getName(), requestFile);
        } else {
            return MultipartBody.Part.createFormData(key, "");
        }
    }

    private String getVideo(String toParse) {
        String[] array = toParse.split("&THUMBNAIL:", 2);
        return array[0];
    }

    private String getThumbnail(String toParse) {
        String[] array = toParse.split("&THUMBNAIL:", 2);
        return array[1];
    }
}
