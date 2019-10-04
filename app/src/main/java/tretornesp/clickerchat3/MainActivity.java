package tretornesp.clickerchat3;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class MainActivity extends AppCompatActivity {
    private static final int SPACE_BETWEEN_CATEGORIES = 20;
    private static final int SIGN_IN_REQUEST_CODE = 1;

    private FirebaseAnalytics mFirebaseAnalytics;

    private ProgressDialog progress;

    private boolean doubleBackToExitPressedOnce = false;
    private boolean flag, flag2, flag3, flag4;
    private NonScrollableListView groupList;
    private ArrayList<Group> groups = new ArrayList<>();
    private ArrayList<String> group_id_list;
    private  Intent intent;
    private ArrayList<CategoryAndURL> categories;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private Context mContext;

    private boolean active;

    @Override
    public void onStart() {
        super.onStart();
        active = true;
    }

    @Override
    public void onStop() {
        super.onStop();
        active = false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        //Intent intent = new Intent(MainActivity.this, NewUser.class);
        //startActivity(intent);

        mContext = getApplicationContext();
        categories = new ArrayList<>();

        login();
        loadBar();
        checkLoaded();
        loadGroupList();
        loadCategories();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.main_search: {
                Intent intent = new Intent(MainActivity.this, GroupList.class);
                intent.putExtra("global", "true");
                finish();
                startActivity(intent);
            }
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (getIntent().hasExtra("returnToGroupList")) {
            if (getIntent().getStringExtra("returnToGroupList").equals("true")) {
                getIntent().removeExtra("returnToGroupList");
                startActivity(new Intent(MainActivity.this, GroupList.class));
            }
        }

        if (FirebaseAuth.getInstance().getCurrentUser()!=null) {
            checkRegistered();
        }
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 1000);
    }

    private void checkLoaded() {

        progress = new ProgressDialog(this);
        progress.setTitle("Cargando");
        progress.setMessage("Cargando portada");
        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
        progress.show();
    }

    private void checkRegistered() {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        DocumentReference docRef = db.collection("user_data").document(FirebaseAuth.getInstance().getCurrentUser().getUid());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    if (!doc.exists()) {
                        Intent intent = new Intent(MainActivity.this, NewUser.class);
                        startActivity(intent);
                    }
                } else {
                    Intent intent = new Intent(MainActivity.this, NewUser.class);
                    startActivity(intent);
                }
            }
        });
    }

    private void login() {
        final FirebaseAuth auth = FirebaseAuth.getInstance();

        auth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() != null && ActiveUser.activeuser == null) {
                    RelativeLayout activity_main = findViewById(R.id.activity_main);
                    Snackbar.make(activity_main, "Bienvenido " + Objects.requireNonNull(auth.getCurrentUser()).getDisplayName(), Snackbar.LENGTH_SHORT).show();

                    setActiveUser();
                    //checkRegistered();
                    loadGroupList();
                }
            }
        });

        if (auth.getCurrentUser() == null) {
            Intent intent = new Intent(MainActivity.this, Login.class);
            finish();
            startActivity(intent);
        }
    }

    private void loadGroupList() {
        groupList = findViewById(R.id.main_listView);

        loadGroups();

        GroupListAdapter adapter = new GroupListAdapter(MainActivity.this, R.layout.adapter_view_layout, groups);
        groupList.setAdapter(adapter);

        groupList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapter, View v, int position, long arg3) {
                    Group item = (Group) adapter.getItemAtPosition(position);
                    RelativeLayout group_list = findViewById(R.id.activity_main);
                    Snackbar.make(group_list, "" + item.getName(), Snackbar.LENGTH_SHORT).show();

                    Intent intent = new Intent(getApplicationContext(), Chat.class);
                    intent.putExtra("global", "true");
                    ActiveGroup.activeGroup = item;
                    startActivity(intent);
                }
            });
        updateAdapter();
    }

    private void updateAdapter() {

        if (groups == null || groups.size() == 0) {
            loadGroups();
        }

        groupList = findViewById(R.id.main_listView);
        GroupListAdapter adapter = new GroupListAdapter(MainActivity.this, R.layout.adapter_view_layout, groups);
        groupList.setAdapter(adapter);
    }

    private void loadGroups() {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        groups = new ArrayList<>();

        group_id_list = new ArrayList<>();

        FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentFirebaseUser == null) return;

        DocumentReference docRef = db.collection("tendencias").document("tendencias");
        flag2 = true;
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (flag2) {
                    flag2 = false;
                } else {
                    return;
                }
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        group_id_list = (ArrayList<String>) document.get("tendencias");
                        loadGroupsFromId();
                    }
                }
                progress.dismiss();
            }
        });
    }

    private void loadGroupsFromId() {
        if (group_id_list==null || group_id_list.size() <= 0) return;
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        for (final String group: group_id_list) {
            System.out.println("GRUPOS: "+group);
            final DocumentReference docRef = db.collection("chats").document(group);
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
                            ArrayList<String> admins = (ArrayList<String>) document.get("admins");

                            System.out.println("CREANDO GRUPO: "+ name);

                            Group group_tmp = new Group(name, description, group, users, admins, imageURL, database, category);

                            if (FirestoreLink.checkIntegrity(group_tmp)) {
                                groups.add(group_tmp);
                                updateAdapter();
                            }

                        }
                    }
                }
            });
        }
    }

    private void setActiveUser() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        final FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentFirebaseUser == null) {
            System.err.println("ERROR GENERANDO USUARIO");
        }

        DocumentReference docRef = db.collection("users").document(currentFirebaseUser.getUid());
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
                        String name = document.getString("name");
                        String uid = currentFirebaseUser.getUid();
                        String imageURL = document.getString("imageURL");
                        String status = document.getString("status");
                        String location = document.getString("location");

                        ArrayList<String> groups = (ArrayList<String>) document.get("groups");
                        ArrayList<String> categories = (ArrayList<String>) document.get("categories");

                        User user = new User(uid, name, imageURL, status, location, groups, categories);
                        if (FirestoreLink.checkIntegrity(user)) {
                            ActiveUser.activeuser = new User(uid, name, imageURL, status, location, groups, categories);
                            System.out.println("EL UID ES ESTE: " + ActiveUser.activeuser.getUid());
                        }
                    } else {
                        //register();
                        Intent intent = new Intent(MainActivity.this, NewUser.class);
                        startActivity(intent);
                    }
                }
            }
        });
    }

    private void register() {

        FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        ActiveUser.activeuser = new User(currentFirebaseUser.getUid(), currentFirebaseUser.getDisplayName(), "", "", "", new ArrayList<String>(), new ArrayList<String>());

        Map<String, Object> parsed = new HashMap<>();
        parsed.put("categories", ActiveUser.activeuser.getCategories());
        parsed.put("groups", ActiveUser.activeuser.getGroups());
        parsed.put("imageURL", ActiveUser.activeuser.getImageURL());
        parsed.put("location", ActiveUser.activeuser.getLocation());
        parsed.put("name", ActiveUser.activeuser.getName());
        parsed.put("status", ActiveUser.activeuser.getStatus());

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

    private void loadCategories() {

        mRecyclerView = (RecyclerView) findViewById(R.id.main_categories);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new CategoryListPhotoAdapter(mContext, categories);
        mRecyclerView.setAdapter(mAdapter);

        mLayoutManager = new LinearLayoutManager(MainActivity.this, LinearLayoutManager.HORIZONTAL, false);
        mRecyclerView.setLayoutManager(mLayoutManager);

        VerticalSpaceItemDecoration dividerItemDecoration = new VerticalSpaceItemDecoration();
        mRecyclerView.addItemDecoration(dividerItemDecoration);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("tendencias").document("categories");
        flag3 = true;
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (flag3) {
                    flag3 = false;
                } else {
                    return;
                }
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        ArrayList<String> cat = (ArrayList<String>) document.get("categories");
                        for (String s: cat) {
                            getUrlFrom(s);
                        }
                    }
                }
            }
        });
    }

    private void getUrlFrom(final String s) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("categories").document(s);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        CategoryAndURL category = new CategoryAndURL(new Category(s), document.getString("imageURL"));
                        if (FirestoreLink.checkIntegrity(category)) {
                            categories.add(category);
                            mRecyclerView = (RecyclerView) findViewById(R.id.main_categories);
                            mAdapter = new CategoryListPhotoAdapter(mContext, categories);
                            mRecyclerView.setAdapter(mAdapter);
                        }
                    }
                }
            }
        });
    }

    private void loadBar() {
        BottomNavigationView view = findViewById(R.id.main_home_bar);
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

                        //finish();
                        Intent openMainActivity= new Intent(MainActivity.this,GroupList.class);
                        openMainActivity.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
                        startActivityIfNeeded(openMainActivity, 0);
                        break;
                    }
                    case R.id.bar_menu_home: {
                        break;
                    }
                    case R.id.bar_menu_profile: {
                        Intent openMainActivity= new Intent(MainActivity.this, CreateUser.class);
                        openMainActivity.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
                        startActivityIfNeeded(openMainActivity, 0);
                        break;
                    }
                }
                return true;
            }
        });
    }

    public class VerticalSpaceItemDecoration extends RecyclerView.ItemDecoration {

        public VerticalSpaceItemDecoration() {
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                                   RecyclerView.State state) {
            outRect.right = SPACE_BETWEEN_CATEGORIES;
        }
    }
}