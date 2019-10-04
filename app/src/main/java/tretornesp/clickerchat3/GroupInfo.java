package tretornesp.clickerchat3;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.text.InputType;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class GroupInfo extends AppCompatActivity {

    private static final int GALLERY = 1;
    private static final int CAMERA = 2;

    private static final int GROUP_DESCRIPTION_MAX_LINES = 10;
    private static final int GROUP_DESCRIPTION_MAX_LENGHT = 500;
    private static final int GROUP_NAME_MAX_LENGTH = 20;
    private static final int GROUP_NAME_MIN_LENGTH = 5;



    private UserListAdapter adapter;
    private TextView description;

    private Context mContext;
    private NonScrollableListView userList;
    private ArrayList<User> users;
    private ArrayList<String> UIDS;

    private boolean flag;

    private ImageView icon;

    private String m_Text;
    private String m_Text_2;

    private boolean auth;

    //private GestureDetector detector;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.group_info_layout);

        mContext = GroupInfo.this;


        // icon.setAnimation(scroll);

        updateAuth();

        loadImage();
        loadExitButton();
        loadUserMenu();
        loadDeleteButton();
        loadTitleBar();
        loadDescription();

        /*
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.RelativeLayout1);
        detector = new GestureDetector(layout.getContext(), new GestureListener());

        layout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (detector.onTouchEvent(motionEvent)) {
                    icon.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
                    Button b = (Button) findViewById(R.id.exit_group);
                    if (ActionSwipe.down == 1) {
                        b.setVisibility(View.GONE);
                        icon.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
                        icon.requestLayout();
                    } else if (ActionSwipe.down == 0) {
                        b.setVisibility(View.VISIBLE);
                        float scale = mContext.getResources().getDisplayMetrics().density;
                        icon.getLayoutParams().height = (int) (60 * scale + 0.5f);
                        icon.requestLayout();
                    }
                    return true;
                }
                return false;
            }
        }); */
    }

    @Override
    public void onResume() {
        super.onResume();

        updateAuth();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (isAdmin()) {
            getMenuInflater().inflate(R.menu.chat_menu, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:

                Intent parentActivityIntent = new Intent(this, Chat.class);
                parentActivityIntent.addFlags(
                        Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(parentActivityIntent);
                finish();
                return true;

            case R.id.change_group_name:
                if (isAdmin()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Nuevo título");

                    final EditText input = new EditText(this);
                    InputFilter[] FilterArray = new InputFilter[1];
                    FilterArray[0] = new InputFilter.LengthFilter(GROUP_NAME_MAX_LENGTH);
                    input.setInputType(InputType.TYPE_CLASS_TEXT);
                    input.setFilters(FilterArray);
                    builder.setView(input);

                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (input.getText().toString().length()>=GROUP_NAME_MIN_LENGTH &&
                                    input.getText().toString().length()<=GROUP_NAME_MAX_LENGTH) {
                                m_Text = input.getText().toString();
                                changeGroupName();
                            } else {
                                RelativeLayout layout = findViewById(R.id.group_info_layout);
                                Snackbar.make(layout, "El nombre del grupo debe tener por lo menos " + GROUP_NAME_MIN_LENGTH + " carácteres", Snackbar.LENGTH_SHORT).show();
                                return;
                            }
                        }
                    });
                    builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                    builder.show();
                }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == this.RESULT_CANCELED) {
            return;
        }
        if (requestCode == GALLERY) {
            if (data != null) {
                uploadImage(data.getData());
            }

        } else if (requestCode == CAMERA) {
            Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            thumbnail.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            String path = MediaStore.Images.Media.insertImage(mContext.getContentResolver(), thumbnail, "profile", null);
            uploadImage(Uri.parse(path));
        }

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId()==R.id.group_info_user_list) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.menu_user_list, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch(item.getItemId()) {
            case R.id.kick:
                new AlertDialog.Builder(mContext)
                        .setIcon(R.drawable.image_confirmation)
                        .setTitle("Expulsar")
                        .setMessage("¿Desea expulsar al usuario?")
                        .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                System.out.println("KICKING: "+adapter.getItem(info.position).getUid());
                                kick(adapter.getItem(info.position).getUid());
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
                return true;
            case R.id.make_admin:
                makeAdmin(adapter.getItem(info.position).getUid());
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pickImage();
                }
            }
        }
    }

    private void uploadImage(Uri data) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference();

        final StorageReference child = storageReference.child("group_images/" + UUID.randomUUID().toString());

        UploadTask uploadTask  = child.putFile(data);

        RelativeLayout layout = findViewById(R.id.RelativeLayout1);
        Snackbar.make(layout, "Subiendo imagen", Snackbar.LENGTH_SHORT).show();

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
                    RelativeLayout layout = findViewById(R.id.RelativeLayout1);
                    Snackbar.make(layout, "Subida completa", Snackbar.LENGTH_SHORT).show();
                    setGroupImage(downloadUri.toString());
                }
            }
        });
    }

    private void setGroupImage (String imageURL) {

        Objects.requireNonNull(getSupportActionBar()).setTitle(m_Text);
        final String image = imageURL;
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        if (ActiveGroup.activeGroup.getId()==null) return;
        final DocumentReference docRef = db.collection("chats").document(ActiveGroup.activeGroup.getId());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String name = document.getString("name");
                        String description = document.getString("description");
                        String imageURL = image;
                        String database = document.getString("database");
                        String category = document.getString("category");

                        ArrayList<String> users = (ArrayList<String>) document.get("users");
                        ArrayList<String> admins = (ArrayList<String>) document.get("admins");

                        Map<String, Object> group = new HashMap<>();
                        group.put("name", name);
                        group.put("description", description);
                        group.put("imageURL", imageURL);
                        group.put("database", database);
                        group.put("users", users);
                        group.put("category", category);
                        group.put("admins", admins);

                        Group group_tmp = new Group(name, description, ActiveGroup.activeGroup.getId(), users, admins, imageURL, database,  category);

                        if (FirestoreLink.checkIntegrity(group_tmp)) {
                            db.collection("chats").document(ActiveGroup.activeGroup.getId()).set(group);

                            ActiveGroup.activeGroup.setImageURL(image);

                            Intent intent = new Intent(GroupInfo.this, Chat.class);
                            intent.putExtra("returnToGroupInfo", "true");
                            finish();
                            startActivity(intent);
                        }
                    }
                }
            }
        });
    }

    private void loadTitleBar() {
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(false);
        getSupportActionBar().setTitle(ActiveGroup.activeGroup.getName());
    }

    private void loadDescription() {
        description = (TextView) findViewById(R.id.group_info_description);
        description.setText(ActiveGroup.activeGroup.getDescription());
        description.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isAdmin()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setTitle("Nueva descripción");

                    final EditText input = new EditText(mContext);
                    input.setInputType(InputType.TYPE_CLASS_TEXT);
                    input.setSingleLine(false);
                    input.setLines(4);
                    input.setMaxLines(GROUP_DESCRIPTION_MAX_LINES);
                    input.setGravity(Gravity.LEFT | Gravity.TOP);
                    input.setHorizontalScrollBarEnabled(false); //this

                    builder.setView(input);

                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (input.getText().toString().length() <= GROUP_DESCRIPTION_MAX_LENGHT)
                                m_Text_2 = input.getText().toString();
                                changeDescription();
                            }
                    });
                    builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                    builder.show();
                }
            }
        });
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

    private void loadImage() {
        setupImageLoader();

        ImageLoader imageLoader = ImageLoader.getInstance();

        int defaultImage = mContext.getResources().getIdentifier("@drawable/image_default", null, mContext.getPackageName());

        DisplayImageOptions options = new DisplayImageOptions.Builder().cacheInMemory(true)
                .cacheOnDisc(true).resetViewBeforeLoading(true)
                .showImageForEmptyUri(defaultImage)
                .showImageOnFail(defaultImage)
                .showImageOnLoading(defaultImage).build();

        Bitmap bitmap = imageLoader.loadImageSync(ActiveGroup.activeGroup.getImageURL(), options);

        icon = (ImageView) findViewById(R.id.group_info_image);

        /* Animation scroll = new TranslateAnimation(
                TranslateAnimation.INFINITE,
                -1000f,
                TranslateAnimation.INFINITE,
                1100f,
                TranslateAnimation.ABSOLUTE,
                0f,
                TranslateAnimation.ABSOLUTE,
                0f
        );

        scroll.setDuration(5000);
        scroll.setRepeatCount(-1);
        scroll.setRepeatMode(Animation.INFINITE);
        scroll.setInterpolator(new LinearInterpolator()); */

        icon.setImageBitmap(bitmap);
        icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isAdmin()) {pickImage();}
            }
        });
    }

    private void pickImage() {

        if (ContextCompat.checkSelfPermission(GroupInfo.this,
                Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {

            ActivityCompat.requestPermissions(GroupInfo.this,
                        new String[]{Manifest.permission.CAMERA},
                    1
                    );

        } else {
            AlertDialog.Builder pictureDialog = new AlertDialog.Builder(this);
            pictureDialog.setTitle("Seleccionar Acción");
            String[] pictureDialogItems = {
                    "Selecciona una imagen de la galería",
                    "Saca una foto"};
            pictureDialog.setItems(pictureDialogItems,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case 0:
                                    if (ContextCompat.checkSelfPermission(GroupInfo.this,
                                            Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {

                                        ActivityCompat.requestPermissions(GroupInfo.this,
                                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                                1
                                        );
                                    } else {
                                        choosePhotoFromGallary();
                                    }
                                    break;
                                case 1: {
                                    if (ContextCompat.checkSelfPermission(GroupInfo.this,
                                            Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED
                                            || ContextCompat.checkSelfPermission(GroupInfo.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {

                                        ActivityCompat.requestPermissions(GroupInfo.this,
                                                new String[]{Manifest.permission.CAMERA,
                                                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                                                },
                                                1
                                        );
                                    } else {
                                        takePhotoFromCamera();
                                    }
                                    break;
                                }
                            }
                        }
                    });
            pictureDialog.show();
        }
    }

    private void choosePhotoFromGallary() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        startActivityForResult(galleryIntent, GALLERY);
    }

    private void takePhotoFromCamera() {
        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, CAMERA);
    }

    private void loadUsers() {
        users = new ArrayList<>();
        FirebaseFirestore db = FirebaseFirestore.getInstance();


        FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentFirebaseUser == null || ActiveGroup.activeGroup.getId()==null) return;

        DocumentReference docRef = db.collection("chats").document(ActiveGroup.activeGroup.getId());
        flag = true;
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (flag) {
                    flag = false;
                } else {
                    return;
                }
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        UIDS = (ArrayList<String>) document.get("users");
                        UIDS.addAll((ArrayList<String>) document.get("admins"));
                        loadUsersFromUID();
                    }
                }
            }
        });

        userList = findViewById(R.id.group_info_user_list);
        adapter = new UserListAdapter(GroupInfo.this, R.layout.adapter_view_group_layout, users);
        userList.setAdapter(adapter);
        userList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View v, int position, long arg3) {
                User item = (User) adapter.getItemAtPosition(position);
                VisualizingUser.visualizing_user = item;

                Intent intent = new Intent(getApplicationContext(), ViewProfile.class);
                startActivity(intent);
            }
        });
    }

    private void loadUsersFromUID() {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        for (final String UID : UIDS) {
            DocumentReference docRef = db.collection("users").document(UID);
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            String name = document.getString("name");
                            String imageURL = document.getString("imageURL");
                            String status = document.getString("status");
                            String location = document.getString("location");

                            ArrayList<String> groups = (ArrayList<String>) document.get("groups");
                            ArrayList<String> categories = (ArrayList<String>) document.get("categories");

                            User tmp_user = new User(UID, name, imageURL, status, location, groups, categories);
                            System.out.println("CREATING USER: " + UID);

                            if (FirestoreLink.checkIntegrity(tmp_user)) {
                                users.add(tmp_user);
                                userList = findViewById(R.id.group_info_user_list);
                                adapter = new UserListAdapter(GroupInfo.this, R.layout.adapter_view_group_layout, users);
                                userList.setAdapter(adapter);
                            }
                        }
                    }
                }
            });
        }
    }

    private void loadUserNumber() {
        TextView numero_usuarios = findViewById(R.id.group_info_user_number);
        numero_usuarios.setText(getString(R.string.usuarios, ActiveGroup.activeGroup.getNumero_usuarios()));
    }

    private void loadExitButton() {
        Button button = (Button) findViewById(R.id.exit_group);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                new AlertDialog.Builder(mContext)
                        .setTitle("Salir del grupo")
                        .setMessage("¿Desea salir del grupo?")
                        .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (isAdmin()) {
                                    if (onlyAdmin()) {
                                        return;
                                    }
                                }
                                Intent backToGroupList = new Intent(view.getContext(), GroupList.class);
                                backToGroupList.putExtra("exitActive", "true");
                                startActivity(backToGroupList);
                                finish();
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
            }
        });
    }

    private void loadDeleteButton() {
        Button button = (Button) findViewById(R.id.delete_group);
        button.setVisibility(View.GONE);

        /*
        if (isAdmin()) {
            button.setVisibility(View.VISIBLE);

            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View view) {
                    new AlertDialog.Builder(mContext)
                            .setIcon(R.drawable.image_confirmation)
                            .setTitle("Eliminar el grupo")
                            .setMessage("¿Desea eliminar el grupo?")
                            .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent backToGroupList = new Intent(view.getContext(), GroupList.class);
                                    backToGroupList.putExtra("deleteActive", "true");
                                    startActivity(backToGroupList);
                                    finish();
                                }
                            })
                            .setNegativeButton("No", null)
                            .show();
                }
            });
        } */
    }

    private boolean isAdmin() {
        for (String admins: ActiveGroup.activeGroup.getAdmins()) {
            if (admins.equals(ActiveUser.activeuser.getUid())) {
                return true;
            }
        }
        return false;
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
                                loadUsers();
                                loadUserNumber();
                                return;
                            }
                        }

                        for (String s: admins) {
                            if (s.equals(ActiveUser.activeuser.getUid())) {
                                auth = true;
                                loadUsers();
                                loadUserNumber();
                                return;
                            }
                        }

                        auth = false;
                    }
                }
            }
        });
    }

    private void changeGroupName() {
        Objects.requireNonNull(getSupportActionBar()).setTitle(m_Text);
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        if (ActiveGroup.activeGroup.getId()==null) return;
        final DocumentReference docRef = db.collection("chats").document(ActiveGroup.activeGroup.getId());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            String name = m_Text;
                            String description = document.getString("description");
                            String imageURL = document.getString("imageURL");
                            String database = document.getString("database");
                            String category = document.getString("category");

                            ArrayList<String> users = (ArrayList<String>) document.get("users");
                            ArrayList<String> admins = (ArrayList<String>) document.get("admins");

                            Map<String, Object> group = new HashMap<>();
                            group.put("name", name);
                            group.put("description", description);
                            group.put("imageURL", imageURL);
                            group.put("database", database);
                            group.put("category", category);
                            group.put("users", users);
                            group.put("admins", admins);

                            Group tmp_group = new Group(name, description, ActiveGroup.activeGroup.getId(), users, admins, imageURL, database, category);
                            if (FirestoreLink.checkIntegrity(tmp_group)) {
                                db.collection("chats").document(ActiveGroup.activeGroup.getId()).set(group);
                            }
                        }
                    }
                }
        });
    }

    private void changeDescription() {
        description.setText(m_Text_2);
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        if (ActiveGroup.activeGroup.getId()==null) return;
        final DocumentReference docRef = db.collection("chats").document(ActiveGroup.activeGroup.getId());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String name = document.getString("name");
                        String description = m_Text_2;
                        String imageURL = document.getString("imageURL");
                        String database = document.getString("database");
                        String category = document.getString("category");

                        ArrayList<String> users = (ArrayList<String>) document.get("users");
                        ArrayList<String> admins = (ArrayList<String>) document.get("admins");

                        Map<String, Object> group = new HashMap<>();
                        group.put("name", name);
                        group.put("description", description);
                        group.put("imageURL", imageURL);
                        group.put("database", database);
                        group.put("users", users);
                        group.put("admins", admins);
                        group.put("category", category);

                        Group tmp_group = new Group(name, description, ActiveGroup.activeGroup.getId(), users, admins, imageURL, database, category);
                        if (FirestoreLink.checkIntegrity(tmp_group)) {
                            db.collection("chats").document(ActiveGroup.activeGroup.getId()).set(group);
                        }
                    }
                }
            }
        });
    }

    private void loadUserMenu() {
        if (isAdmin()) {
            ListView view = (ListView) findViewById(R.id.group_info_user_list);
            registerForContextMenu(view);
        }
    }

    private void kick(final String uid) {

        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentFirebaseUser == null || ActiveGroup.activeGroup.getId()== null) return;

        DocumentReference docRef = db.collection("chats").document(ActiveGroup.activeGroup.getId());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        ArrayList<String> users = (ArrayList<String>) document.get("users");
                        ArrayList<String> admins = (ArrayList<String>) document.get("admins");

                        String toRemove = "";
                        boolean found = false;

                        for (String s : users) {
                            if (s.equals(uid)) {
                                toRemove = s;
                                found = true;
                            }
                        }

                        for (String s : admins) {
                            if (s.equals(uid)) {
                                toRemove = s;
                                found = true;
                            }
                        }

                        if (!found) return;

                        users.remove(toRemove);

                        Map<String, Object> group = new HashMap<>();
                        group.put("name", ActiveGroup.activeGroup.getName());
                        group.put("imageURL", ActiveGroup.activeGroup.getImageURL());
                        group.put("description", ActiveGroup.activeGroup.getDescription());
                        group.put("database", ActiveGroup.activeGroup.getDatabase());
                        group.put("category", ActiveGroup.activeGroup.getCategory());
                        group.put("admins", admins);
                        group.put("users", users);

                        Group tmp_group = new Group(ActiveGroup.activeGroup.getName(), ActiveGroup.activeGroup.getDescription(), ActiveGroup.activeGroup.getId(), users, admins, ActiveGroup.activeGroup.getImageURL(), ActiveGroup.activeGroup.getDatabase(), ActiveGroup.activeGroup.getCategory());
                        if (FirestoreLink.checkIntegrity(tmp_group)) {
                            db.collection("chats").document(ActiveGroup.activeGroup.getId()).set(group);

                            for (int i = 0; i < adapter.getCount(); i++) {
                                if (adapter.getItem(i).getUid().equals(uid)) {
                                    adapter.remove(adapter.getItem(i));
                                }
                            }
                        }
                    }
                }
            }
        });
    }

    private void makeAdmin(final String uid) {
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentFirebaseUser == null) return;

        DocumentReference docRef = db.collection("chats").document(ActiveGroup.activeGroup.getId());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        ArrayList<String> users = (ArrayList<String>) document.get("users");
                        ArrayList<String> admins = (ArrayList<String>) document.get("admins");

                        String toRemove = "";
                        boolean found = false;

                        for (String s : users) {
                            if (s.equals(uid)) {
                                toRemove = s;
                                found = true;
                            }
                        }

                        if (!found) return;

                        admins.add(uid);
                        users.remove(toRemove);

                        Map<String, Object> group = new HashMap<>();
                        group.put("name", ActiveGroup.activeGroup.getName());
                        group.put("imageURL", ActiveGroup.activeGroup.getImageURL());
                        group.put("description", ActiveGroup.activeGroup.getDescription());
                        group.put("database", ActiveGroup.activeGroup.getDatabase());
                        group.put("category", ActiveGroup.activeGroup.getCategory());
                        group.put("admins", admins);
                        group.put("users", users);

                        Group tmp_group = new Group(ActiveGroup.activeGroup.getName(), ActiveGroup.activeGroup.getDescription(), ActiveGroup.activeGroup.getId(), users, admins, ActiveGroup.activeGroup.getImageURL(), ActiveGroup.activeGroup.getDatabase(), ActiveGroup.activeGroup.getCategory());
                        if (FirestoreLink.checkIntegrity(tmp_group)) {
                            db.collection("chats").document(ActiveGroup.activeGroup.getId()).set(group);
                        }
                    }
                }
            }
        });
    }

    private boolean onlyAdmin() {
        if (ActiveGroup.activeGroup.getUsers().size()==0 && ActiveGroup.activeGroup.getAdmins().size()==1) return false;
        if (ActiveGroup.activeGroup.getAdmins().size()<=1) {
            new AlertDialog.Builder(mContext)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("Error al salir del grupo")
                    .setMessage("Antes de salir del grupo nombre a otro Administrador")
                    .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .show();
            return true;
        } else {
            return false;
        }
    }
}