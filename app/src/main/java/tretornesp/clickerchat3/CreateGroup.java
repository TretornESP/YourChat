package tretornesp.clickerchat3;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
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
import android.view.Gravity;
import android.view.Menu;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class CreateGroup extends AppCompatActivity {

    private static final int GALLERY = 1;
    private static final int CAMERA = 2;

    private static final int GROUP_DESCRIPTION_MAX_LINES = 10;
    private static final int GROUP_DESCRIPTION_MAX_LENGHT = 500;
    private static final int GROUP_NAME_MAX_LENGTH = 20;
    private static final int GROUP_NAME_MIN_LENGTH = 5;

    private ProgressDialog progress;

    private Uri image_uri;

    private String m_Text;
    private String m_Text_2;

    private Group group;

    private boolean canBack;
    //private RecyclerView mRecyclerView;
    //private RecyclerView.Adapter mAdapter;
    //private RecyclerView.LayoutManager mLayoutManager;

    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private CategoryListAdapter mAdapter;

    private ListView categoryList;
    private ArrayList<Category> categories;
    private CategoryListAdapterListView adapter;

    private Context mContext;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_group_layout);

        mContext = CreateGroup.this;
        categories = new ArrayList<>();
        group = new Group();
        image_uri = null;
        canBack = true;

        loadCategories();
        loadDescription();
        loadTitleBar();
        loadImage();
        loadCreateGroup();
        loadNameChanger();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == this.RESULT_CANCELED) {
            return;
        }
        if (requestCode == GALLERY) {
            if (data != null) {
                image_uri = data.getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), image_uri);
                    ImageView image = findViewById(R.id.create_group_image);
                    image.setImageBitmap(bitmap);
                } catch (IOException ioe) {
                    image_uri = null;
                }
            }

        } else if (requestCode == CAMERA) {
            Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            thumbnail.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            String path = MediaStore.Images.Media.insertImage(mContext.getContentResolver(), thumbnail, "profile", null);
            image_uri = Uri.parse(path);
            ImageView image = findViewById(R.id.create_group_image);
            image.setImageBitmap(thumbnail);
        }
        System.out.println("loaded: " + image_uri.toString());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.chat_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (!canBack) return true;
                Intent parentActivityIntent = new Intent(this, MainActivity.class);
                parentActivityIntent.addFlags(
                        Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(parentActivityIntent);
                progress.dismiss();
                finish();
                return true;

            case R.id.change_group_name:
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
                        m_Text = input.getText().toString();
                        changeGroupName();
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
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (canBack) {
            super.onBackPressed();
        } else {
            return;
        }
    }

    /*private void loadCategories() {
        for (String s : getResources().getStringArray(R.array.categories)) {
            categories.add(new Category(s));
        }
        categoryList = findViewById(R.id.category_list);
        adapter = new CategoryListAdapterListView(CreateGroup.this, R.layout.adapter_view_category_equal_layout, categories);
        categoryList.setAdapter(adapter);
        categoryList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View v, int position, long arg3) {
                Category item = (Category) adapter.getItemAtPosition(position);
                group.setCategory(item.getName());
                System.out.println(group.getCategory());
            }
        });
    } */

    private void loadNameChanger() {
        ImageView img = findViewById(R.id.create_group_edit_name);
        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle("Nuevo título");

                final EditText input = new EditText(mContext);
                InputFilter[] FilterArray = new InputFilter[1];
                FilterArray[0] = new InputFilter.LengthFilter(GROUP_NAME_MAX_LENGTH);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                input.setFilters(FilterArray);
                builder.setView(input);

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        m_Text = input.getText().toString();
                        changeGroupName();
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

    private void loadDescription() {
        TextView text = findViewById(R.id.create_group_description);
        text.setMovementMethod(new ScrollingMovementMethod());
        text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle("Nueva descripción");

                InputFilter[] FilterArray = new InputFilter[1];
                FilterArray[0] = new InputFilter.LengthFilter(GROUP_DESCRIPTION_MAX_LENGHT);

                final EditText input = new EditText(mContext);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                input.setSingleLine(false);
                input.setLines(4);
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
                        if (null != input.getLayout() && input.getLayout().getLineCount() > GROUP_DESCRIPTION_MAX_LINES) {
                            input.getText().delete(input.getText().length() - 1, input.getText().length());
                        }
                    }
                });
                builder.setView(input);

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        m_Text_2 = input.getText().toString();
                        TextView text = findViewById(R.id.create_group_description);
                        text.setText(m_Text_2);
                        group.setDescription(m_Text_2);
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

    private void changeGroupName() {
        if (m_Text.length()<GROUP_NAME_MIN_LENGTH) {
            LinearLayout layout = findViewById(R.id.create_group_layout);
            Snackbar.make(layout, "El nombre del grupo debe tener por lo menos " + GROUP_NAME_MIN_LENGTH + " carácteres", Snackbar.LENGTH_SHORT).show();
        } else {
            getSupportActionBar().setTitle(m_Text);
            TextView view = findViewById(R.id.create_group_edit_name_text);
            view.setText(m_Text);
            group.setName(m_Text);
        }
    }

    private void loadTitleBar() {
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(false);
        getSupportActionBar().setTitle(R.string.nuevo_grupo);
    }

    private void loadImage() {
        ImageView image = findViewById(R.id.create_group_image);
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickImage();
            }
        });
    }

    private void pickImage() {

        if (ContextCompat.checkSelfPermission(CreateGroup.this,
                android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {

            ActivityCompat.requestPermissions(CreateGroup.this,
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
                                    if (ContextCompat.checkSelfPermission(CreateGroup.this,
                                            android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {

                                        ActivityCompat.requestPermissions(CreateGroup.this,
                                                new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                                                1
                                        );
                                    } else {
                                        choosePhotoFromGallary();
                                    }
                                    break;
                                case 1: {
                                    if (ContextCompat.checkSelfPermission(CreateGroup.this,
                                            android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED
                                            || ContextCompat.checkSelfPermission(CreateGroup.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {

                                        ActivityCompat.requestPermissions(CreateGroup.this,
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

    private void loadCreateGroup() {
        progress = new ProgressDialog(this);
        Button create = findViewById(R.id.create_group);
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (group.getName() == null || group.getName().length()<GROUP_NAME_MIN_LENGTH) {
                    LinearLayout layout = findViewById(R.id.create_group_layout);
                    Snackbar.make(layout, "El nombre del grupo debe tener por lo menos " + GROUP_NAME_MIN_LENGTH + " carácteres", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if (image_uri == null) {
                    LinearLayout layout = findViewById(R.id.create_group_layout);
                    Snackbar.make(layout, "El grupo debe tener una imagen", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if (ActiveCategory.activecategory == null || ActiveCategory.activecategory.getName().equals("")) {
                    LinearLayout layout = findViewById(R.id.create_group_layout);
                    Snackbar.make(layout, "El grupo debe tener una categoría", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if (group.getDescription() == null || group.getDescription().equals("")) {
                    group.setDescription("");
                }

                new AlertDialog.Builder(mContext)
                        .setIcon(R.drawable.image_confirmation)
                        .setTitle("Crear el grupo")
                        .setMessage("¿Desea crear del grupo?")
                        .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                uploadImage();
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
            }
        });
    }

    private void createGroup() {
        ArrayList<String> users = new ArrayList<>();
        ArrayList<String> admins = new ArrayList<>();

        admins.add(ActiveUser.activeuser.getUid());

        group.setCategory(ActiveCategory.activecategory.getName());
        group.setDatabase(UUID.randomUUID().toString());
        group.setId(group.getDatabase());
        group.setAdmins(admins);
        group.setUsers(users);
        group.setDescription(group.getDescription());

        addGroup();
    }

    private void uploadImage() {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference();

        final StorageReference child = storageReference.child("group_images/" + UUID.randomUUID().toString());

        UploadTask uploadTask = child.putFile(image_uri);

        canBack=false;

        LinearLayout layout = findViewById(R.id.create_group_layout);
        Snackbar.make(layout, "Subiendo imagen", Snackbar.LENGTH_SHORT).show();
        Button b = findViewById(R.id.create_group);
        b.setEnabled(false);

        progress.setTitle("Creando grupo");
        progress.setMessage("Espere mientras se crea el grupo");
        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
        progress.show();
// To dismiss the dialog

        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    canBack = true;
                    progress.dismiss();
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
                    LinearLayout layout = findViewById(R.id.create_group_layout);
                    Snackbar.make(layout, "Subida completa", Snackbar.LENGTH_SHORT).show();
                    group.setImageURL(downloadUri.toString());
                    createGroup();
                }
            }
        });
    }

    private void addGroup() {

        if (group.getCategory()==null) {
            new AlertDialog.Builder(mContext)
                    .setIcon(R.drawable.image_error)
                    .setTitle("Error")
                    .setMessage("El grupo no se ha creado por un error desconocido, inténtelo de nuevo mas tarde")
                    .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            progress.dismiss();
                            finish();
                        }
                    })
                    .show();
        }

        Map<String, Object> parsed = new HashMap<>();
        parsed.put("admins", group.getAdmins());
        parsed.put("category", group.getCategory());
        parsed.put("database", group.getDatabase());
        parsed.put("description", group.getDescription());
        parsed.put("imageURL", group.getImageURL());
        parsed.put("name", group.getName());
        parsed.put("users", group.getUsers());

        if (FirestoreLink.checkIntegrity(group)) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            db.collection("chats").document(group.getId())
                    .set(parsed)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            registerInGroup();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            new AlertDialog.Builder(mContext)
                                    .setIcon(R.drawable.image_error)
                                    .setTitle("Error")
                                    .setMessage("El grupo no se ha creado por un error desconocido, inténtelo de nuevo mas tarde")
                                    .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            progress.dismiss();
                                            finish();
                                        }
                                    })
                                    .show();
                        }
                    });
        }

    }

    private void registerInGroup() {
        final FirebaseFirestore db = FirebaseFirestore.getInstance();

        final DocumentReference docRef = db.collection("users").document(ActiveUser.activeuser.getUid());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        ArrayList<String> groups = (ArrayList<String>) document.get("groups");

                        groups.add(group.getId());

                        docRef.update("groups", groups)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        ActiveGroup.activeGroup = group;
                                        Intent intent = new Intent(getApplicationContext(), Chat.class);
                                        intent.putExtra("just_created", "true");
                                        progress.dismiss();
                                        finish();
                                        startActivity(intent);
                                    }})
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        new AlertDialog.Builder(mContext)
                                                .setIcon(R.drawable.image_error)
                                                .setTitle("Error")
                                                .setMessage("El grupo se ha creado pero no se ha podido registrar en el")
                                                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        progress.dismiss();
                                                        finish();
                                                    }
                                                })
                                                .show();
                                    }
                                });
                    }
                }
            }

        });
    }

    private void loadCategories() {
        ActiveCategory.activecategory = null;
        mRecyclerView = (RecyclerView) findViewById(R.id.create_group_view_categories);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new CategoryListAdapter(mContext, categories);
        mRecyclerView.setAdapter(mAdapter);

        mLayoutManager = new LinearLayoutManager(CreateGroup.this, LinearLayoutManager.HORIZONTAL, false);
        mRecyclerView.setLayoutManager(mLayoutManager);

        for (String s : getResources().getStringArray(R.array.categories)) {
            System.out.println("CREATING CATEGORY: " + s);
            categories.add(new Category(s));

        }
        mRecyclerView = (RecyclerView) findViewById(R.id.create_group_view_categories);
        mAdapter = new CategoryListAdapter(mContext, categories);
        mRecyclerView.setAdapter(mAdapter);
    }
}
