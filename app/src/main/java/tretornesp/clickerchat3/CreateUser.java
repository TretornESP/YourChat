package tretornesp.clickerchat3;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.internal.BottomNavigationMenu;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class CreateUser extends AppCompatActivity{

    private static final int GALLERY = 1;
    private static final int CAMERA = 2;

    private static final int USER_LOCATION_MAX_LENGHT = 150;
    private static final int USER_DESCRIPTION_MAX_LENGHT = 200;
    private static final int USER_STATUS_MAX_LINES = 5;
    private static final int USER_LOCATION_MAX_LINES = 1;

    private String m_Text_2;

    private Context mContext;
    private ArrayList<Category> categories;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ListView groupList;
    private ArrayList<Group> groups = new ArrayList<>();
    private ArrayList<String> group_id_list;
    private boolean flag;

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
    public boolean onOptionsItemSelected(MenuItem item) {
        final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch(item.getItemId()) {
            case R.id.home_menu_create_group:
                Intent intent = new Intent(CreateUser.this, CreateGroup.class);
                startActivity(intent);
                return true;
            case R.id.home_menu_terms_and_conditions:
                navigateTerms();
                return true;
            case R.id.home_menu_contact:
                navigateContact();
                return true;
           /* case R.id.home_menu_config:
                String[] languages = {"Galego", "Castellano"};
                final String[] codes = {"gl", "es"};

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Selecciona un idoma");
                builder.setItems(languages, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setLocale(codes[which]);
                    }
                });
                builder.show();
                return true; */
            case R.id.home_menu_logout:
                AuthUI.getInstance().signOut(this).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        ActiveUser.activeuser = null;
                        View v = findViewById(R.id.create_user_layout);

                        Snackbar.make(v, "Has cerrado sesión", Snackbar.LENGTH_SHORT).show();
                        finish();
                        Intent intent = new Intent(CreateUser.this, MainActivity.class);
                        startActivity(intent);
                    }
                });
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.create_user_menu, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        return;
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

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId()==R.id.home_menu) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.create_user_menu, menu);
        }
    }

    @Override
    public void onResume() {

        checkActiveUser();

        super.onResume();
        loadCategories();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_user_layout);

        if (!checkActiveUser()) return;

        mContext = CreateUser.this;
        categories = new ArrayList<>();


        loadToolbar();
        loadProfilePicture();
        loadDescription();
        loadCategories();
        loadLocation();
        loadName();
        loadBar();
    }

    private void loadToolbar() {
        Objects.requireNonNull(getSupportActionBar()).setTitle("Home");
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
    }

    private void loadProfilePicture() {
        setupImageLoader();

        ImageLoader imageLoader = ImageLoader.getInstance();

        int defaultImage = mContext.getResources().getIdentifier("@drawable/image_default_user", null, mContext.getPackageName());

        DisplayImageOptions options = new DisplayImageOptions.Builder().cacheInMemory(true)
                .cacheOnDisc(true).resetViewBeforeLoading(true)
                .showImageForEmptyUri(defaultImage)
                .showImageOnFail(defaultImage)
                .showImageOnLoading(defaultImage).build();

        ImageView icon = (ImageView) findViewById(R.id.home_view_profile_image);

        imageLoader.displayImage(ActiveUser.activeuser.getImageURL(), icon, options);

        icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickImage();
            }
        });
    }

    private void loadDescription() {
        TextView description = findViewById(R.id.home_description);
        description.setText(ActiveUser.activeuser.getStatus());
        description.setMovementMethod(new ScrollingMovementMethod());
        description.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle("Nuevo estado");

                InputFilter[] FilterArray = new InputFilter[1];
                FilterArray[0] = new InputFilter.LengthFilter(USER_DESCRIPTION_MAX_LENGHT);

                final EditText input = new EditText(mContext);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                input.setSingleLine(false);
                input.setLines(4);
                input.setMaxLines(USER_STATUS_MAX_LINES);
                input.setGravity(Gravity.START | Gravity.TOP);
                input.setHorizontalScrollBarEnabled(false); //this
                input.setFilters(FilterArray);
                input.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) { }
                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) { }

                    @Override
                    public void afterTextChanged(Editable editable) {
                        if (null != input.getLayout() && input.getLayout().getLineCount() > USER_STATUS_MAX_LINES) {
                            input.getText().delete(input.getText().length() - 1, input.getText().length());
                        }
                    }
                });

                builder.setView(input);

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        m_Text_2 = input.getText().toString();
                        TextView description = findViewById(R.id.home_description);
                        description.setText(m_Text_2);
                        ActiveUser.activeuser.setStatus(m_Text_2);
                        updateUserDatabase();
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
        });
    }

    private void loadName() {
        TextView name = findViewById(R.id.home_name);
        name.setText(ActiveUser.activeuser.getName());
    }

    private void loadCategories() {

        mRecyclerView = (RecyclerView) findViewById(R.id.home_view_categories);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new CategoryListAdapter(mContext, categories);
        mRecyclerView.setAdapter(mAdapter);
        categories = new ArrayList<>();

        mLayoutManager = new LinearLayoutManager(CreateUser.this, LinearLayoutManager.HORIZONTAL, false);
        mRecyclerView.setLayoutManager(mLayoutManager);

        for (String s: ActiveUser.activeuser.getCategories()) {
            categories.add(new Category(s));
            mRecyclerView = (RecyclerView) findViewById(R.id.home_view_categories);
            mAdapter = new CategoryListAdapter(mContext, categories);
            mRecyclerView.setAdapter(mAdapter);
        }

        ImageView image = findViewById(R.id.home_add_categories);
        image.setOnClickListener(new View.OnClickListener() {
                                     @Override
                                     public void onClick(View view) {
                                         Intent intent = new Intent(CreateUser.this, SelectCategories.class);
                                         startActivity(intent);
                                     }
                                 }
        );
    }

    private void loadLocation() {
        TextView location = findViewById(R.id.home_location_text);
        location.setText(ActiveUser.activeuser.getLocation());
        location.setMovementMethod(new ScrollingMovementMethod());
        location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle("Localización");

                InputFilter[] FilterArray = new InputFilter[1];
                FilterArray[0] = new InputFilter.LengthFilter(USER_DESCRIPTION_MAX_LENGHT);

                final EditText input = new EditText(mContext);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                input.setSingleLine(true);
                input.setLines(1);
                input.setGravity(Gravity.START | Gravity.TOP);
                input.setHorizontalScrollBarEnabled(false); //this
                input.setFilters(FilterArray);

                builder.setView(input);

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        m_Text_2 = input.getText().toString();
                        TextView description = findViewById(R.id.home_location_text);
                        description.setText(m_Text_2);
                        ActiveUser.activeuser.setLocation(m_Text_2);
                        updateUserDatabase();
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
        });

        ImageView image = findViewById(R.id.home_location_image);
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle("Localización");

                final EditText input = new EditText(mContext);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                input.setSingleLine(false);
                input.setLines(4);
                input.setMaxLines(USER_LOCATION_MAX_LINES);
                input.setGravity(Gravity.START | Gravity.TOP);
                input.setHorizontalScrollBarEnabled(false); //this

                builder.setView(input);

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        m_Text_2 = input.getText().toString();
                        TextView description = findViewById(R.id.home_location_text);
                        description.setText(m_Text_2);
                        ActiveUser.activeuser.setLocation(m_Text_2);
                        updateUserDatabase();
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
        });

    }

    private void pickImage() {

        if (ContextCompat.checkSelfPermission(CreateUser.this,
                android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {

            ActivityCompat.requestPermissions(CreateUser.this,
                    new String[]{android.Manifest.permission.CAMERA},
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
                                    if (ContextCompat.checkSelfPermission(CreateUser.this,
                                            android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {

                                        ActivityCompat.requestPermissions(CreateUser.this,
                                                new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                                                1
                                        );
                                    } else {
                                        choosePhotoFromGallary();
                                    }
                                    break;
                                case 1: {
                                    if (ContextCompat.checkSelfPermission(CreateUser.this,
                                            android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED
                                            || ContextCompat.checkSelfPermission(CreateUser.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {

                                        ActivityCompat.requestPermissions(CreateUser.this,
                                                new String[]{android.Manifest.permission.CAMERA,
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

    private void uploadImage(Uri data) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference();

        final StorageReference child = storageReference.child("profile_images/" + ActiveUser.activeuser.getUid() + "/"+ UUID.randomUUID().toString());

        UploadTask uploadTask  = child.putFile(data);

        RelativeLayout layout = findViewById(R.id.create_user_layout);
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
                    RelativeLayout layout = findViewById(R.id.create_user_layout);
                    Snackbar.make(layout, "Subida completa", Snackbar.LENGTH_SHORT).show();
                    ActiveUser.activeuser.setImageURL(downloadUri.toString());
                    updateUserDatabase();
                    loadProfilePicture();
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

    private void updateUserDatabase() {
        Map<String, Object> parsed = new HashMap<>();
        parsed.put("categories", ActiveUser.activeuser.getCategories());
        parsed.put("groups", ActiveUser.activeuser.getGroups());
        parsed.put("imageURL", ActiveUser.activeuser.getImageURL());
        parsed.put("location", ActiveUser.activeuser.getLocation());
        parsed.put("name", ActiveUser.activeuser.getName());
        parsed.put("status", ActiveUser.activeuser.getStatus());

        if (FirestoreLink.checkIntegrity(ActiveUser.activeuser)) {

            FirebaseFirestore db = FirebaseFirestore.getInstance();

            db.collection("users").document(ActiveUser.activeuser.getUid())
                    .set(parsed)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            new AlertDialog.Builder(getApplicationContext())
                                    .setIcon(R.drawable.image_error)
                                    .setTitle("Error")
                                    .setMessage("El usuario no se ha creado por un error desconocido, inténtelo de nuevo mas tarde")
                                    .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            finish();
                                        }
                                    })
                                    .show();
                        }
                    });
        }
    }

    private void loadBar() {
        BottomNavigationView view = findViewById(R.id.create_user_home_bar);
        BottomNavigationMenuView menuView = (BottomNavigationMenuView) view.getChildAt(0);
        for (int i = 0; i < menuView.getChildCount(); i++) {
            final View iconView = menuView.getChildAt(i).findViewById(android.support.design.R.id.icon);
            final ViewGroup.LayoutParams layoutParams = iconView.getLayoutParams();
            final DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            layoutParams.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, displayMetrics);
            layoutParams.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, displayMetrics);
            iconView.setLayoutParams(layoutParams);
        }
        view.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.bar_menu_groups: {
                        Intent openMainActivity= new Intent(CreateUser.this, GroupList.class);
                        openMainActivity.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
                        startActivityIfNeeded(openMainActivity, 0);
                        break;
                    }
                    case R.id.bar_menu_home: {
                        Intent openMainActivity= new Intent(CreateUser.this, MainActivity.class);
                        openMainActivity.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
                        startActivityIfNeeded(openMainActivity, 0);
                        break;
                    }
                    case R.id.bar_menu_profile: {
                        break;
                    }
                }
                return true;
            }
        });
    }

    private void navigateTerms() {
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final DocumentReference docRef = db.collection("urls").document("urls");
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String terms = document.getString("terms");
                        Intent browse = new Intent( Intent.ACTION_VIEW , Uri.parse( terms ) );
                        startActivity( browse );
                    }
                }
            }
        });
    }

    private void navigateContact() {
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final DocumentReference docRef = db.collection("urls").document("urls");
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String terms = document.getString("contact");
                        Intent browse = new Intent( Intent.ACTION_VIEW , Uri.parse( terms ) );
                        startActivity( browse );
                    }
                }
            }
        });
    }

    /*public void setLocale(String lang) {
        Locale myLocale = new Locale(lang);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);
        Intent refresh = new Intent(this, CreateUser.class);
        refresh.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(refresh);
        finish();
    } */

    //@SuppressWarnings("deprecation")
    public void setLocale(String lang) {
        Locale locale = new Locale(lang);
        Resources resources = mContext.getResources();
        Configuration configuration = resources.getConfiguration();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        configuration.setLocale(locale);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            getApplicationContext().createConfigurationContext(configuration);
        } else {
            resources.updateConfiguration(configuration,displayMetrics);
        }

        Intent i = getBaseContext().getPackageManager()
                .getLaunchIntentForPackage(getBaseContext().getPackageName());
        if (i != null) {
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        } else {
            new AlertDialog.Builder(mContext)
                    .setIcon(R.drawable.image_error)
                    .setTitle("Error")
                    .setMessage("No se pudo cambiar el idioma")
                    .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .show();
        }
        startActivity(i);
        this.finish();

    }

    private boolean checkActiveUser() {
        if (ActiveUser.activeuser==null ||
            ActiveUser.activeuser.getUid() == null ||
            ActiveUser.activeuser.getName() == null ||
            ActiveUser.activeuser.getImageURL() == null ||
            ActiveUser.activeuser.getStatus() == null ||
            ActiveUser.activeuser.getLocation() == null ||
            ActiveUser.activeuser.getGroups() == null ||
            ActiveUser.activeuser.getCategories() == null) {
                Intent intent = new Intent(CreateUser.this, MainActivity.class);
                finish();
                startActivity(intent);
             return false;
        }
        return true;
}

}
