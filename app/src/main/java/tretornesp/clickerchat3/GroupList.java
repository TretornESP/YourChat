package tretornesp.clickerchat3;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.Image;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class GroupList extends AppCompatActivity {

    private static final int SIZE_TO_SHOW = 4;

    private MaterialSearchView searchView;
    private ListView groupList;
    private ArrayList<Group> groups = new ArrayList<>();
    private ArrayList<String> group_id_list;
    private  Intent intent;
    private int groups_id_list_index;
    private boolean finishedLoading;

    private boolean flag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.group_list);

        if (ActiveUser.activeuser == null || FirebaseAuth.getInstance().getCurrentUser() == null) {
            Intent returnMain = new Intent(GroupList.this, MainActivity.class);
            startActivity(returnMain);
            finish();
        } else {

            checkGlobal();

            loadToolbar();
            loadGroupList();
            loadSearchView();

            loadBar();
        }
    }

    private void loadToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (checkGlobal()) {
            Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            Objects.requireNonNull(getSupportActionBar()).setTitle("Buscar Grupos");
        } else {
            Objects.requireNonNull(getSupportActionBar()).setTitle("Tus Grupos");
            Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setDisplayShowHomeEnabled(false);
        }
        toolbar.setTitleTextColor(Color.parseColor("#FFFFFF"));
    }

    private void loadGroupList() {
        finishedLoading = false;
        groupList = findViewById(R.id.listView);

        if (checkGlobal()) {
            loadAllGroups();
        } else {
            loadGroups();
        }

        GroupListAdapter adapter = new GroupListAdapter(GroupList.this, R.layout.adapter_view_layout, groups);
        groupList.setAdapter(adapter);
        ImageView nogroups = findViewById(R.id.notfound);

        if (checkGlobal()) {
            groupList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(final AdapterView<?> adapter, View v,final int position, long arg3) {

                            Group item = (Group) adapter.getItemAtPosition(position);
                            RelativeLayout group_list = findViewById(R.id.group_list);
                            Snackbar.make(group_list, "" + item.getName(), Snackbar.LENGTH_SHORT).show();

                            Intent intent = new Intent(getApplicationContext(), Chat.class);
                            intent.putExtra("global", "true");
                            ActiveGroup.activeGroup = item;
                            startActivity(intent);
                }
            });
        } else {
            groupList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapter, View v, int position, long arg3) {
                    Group item = (Group) adapter.getItemAtPosition(position);
                    RelativeLayout group_list = findViewById(R.id.group_list);
                    Snackbar.make(group_list, "" + item.getName(), Snackbar.LENGTH_SHORT).show();

                    Intent intent = new Intent(getApplicationContext(), Chat.class);
                    ActiveGroup.activeGroup = item;
                    startActivity(intent);
                }
            });
        }

        updateAdapter();
    }

    private void loadSearchView() {
        searchView = findViewById(R.id.search_view);
        if (groups.size()==0) {
            showArrow();
        }
        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {
                BottomNavigationView view = findViewById(R.id.group_list_home_bar);
                view.setVisibility(View.GONE);
                hideArrow();
            }

            @Override
            public void onSearchViewClosed() {
                BottomNavigationView view = findViewById(R.id.group_list_home_bar);
                view.setVisibility(View.VISIBLE);
                ImageView nogroups = findViewById(R.id.notfound);
                if (groups.size()==0) {
                    nogroups.setVisibility(View.VISIBLE);
                } else {
                    nogroups.setVisibility(View.GONE);
                }

            }
        });

        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText != null && !newText.isEmpty() && (!checkGlobal() || newText.length()>SIZE_TO_SHOW)) {
                    hideArrow();
                    ArrayList<Group> lstFound = new ArrayList<>();
                    for (Group group : groups) {
                        if (group.getName().toLowerCase().contains(newText.toLowerCase()) ||
                                group.getDescription().toLowerCase().contains(newText.toLowerCase())) {
                            lstFound.add(group);
                        }
                    }

                    GroupListAdapter adapter = new GroupListAdapter(GroupList.this, R.layout.adapter_view_layout, lstFound);
                    groupList.setAdapter(adapter);
                    ImageView nogroups = findViewById(R.id.notfound);
                    if (lstFound.size()==0 && checkGlobal()) {
                        nogroups.setVisibility(View.VISIBLE);
                    } else {
                        nogroups.setVisibility(View.GONE);
                    }
                }
                return true;
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        if (checkGlobal()) {
            Intent intent = new Intent(GroupList.this, MainActivity.class);
            finish();
            startActivity(intent);
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();

        if (ActiveUser.activeuser==null || FirebaseAuth.getInstance().getCurrentUser() == null) {
            Intent returnMain = new Intent(GroupList.this, MainActivity.class);
            startActivity(returnMain);
            finish();
        } else {
            if (checkGlobal()) {
                loadAllGroups();
            } else {
                loadGroups();
            }
        }

        if (getIntent().hasExtra("exitActive")) {
            if (getIntent().getStringExtra("exitActive").equals("true")) {
                exitGroup();
            }
        }

        if (getIntent().hasExtra("deleteActive")) {
            if (getIntent().getStringExtra("deleteActive").equals("true")) {
                deleteGroup();
            }
        }

        if (ActiveGroup.activeGroup == null) return;

        for (int i = 0; i < groups.size(); i++) {
            if (groups.get(i).getId()==ActiveGroup.activeGroup.getId()) {
                groups.set(i, ActiveGroup.activeGroup);
            }
        }

        updateAdapter();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.group_menu_item, menu);
        MenuItem item = menu.findItem(R.id.action_search);
        searchView.setMenuItem(item);
        return true;
    }

    private void updateAdapter() {

        if (groups == null || groups.size() == 0) {
            if (checkGlobal()) {
                loadAllGroups();
            } else {
                loadGroups();
            }
        }

        groupList = findViewById(R.id.listView);
        GroupListAdapter adapter = new GroupListAdapter(GroupList.this, R.layout.adapter_view_layout, groups);
        groupList.setAdapter(adapter);
        ImageView nogroups = findViewById(R.id.notfound);
        if (finishedLoading && checkGlobal()) {
            if (groups.size() == 0) {
                nogroups.setVisibility(View.VISIBLE);
            } else {
                nogroups.setVisibility(View.GONE);
            }
        } else if (finishedLoading) {
            if (groups.size()==0) {
                showArrow();
            } else {
                hideArrow();
            }
        }
    }

    private void loadGroups() {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        groups = new ArrayList<>();

        group_id_list = new ArrayList<>();

        FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentFirebaseUser == null) return;

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
                        group_id_list = (ArrayList<String>) document.get("groups");
                        loadGroupsFromId();
                    }
                }
            }
        });
    }

    private void loadGroupsFromId() {

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

                            Group tmp_group = new Group(name, description, group, users, admins, imageURL, database, category);
                            if (FirestoreLink.checkIntegrity(tmp_group)) {
                                groups.add(tmp_group);

                                if (!checkGlobal()) updateAdapter();
                            }

                        } else {
                            ActiveGroup.activeGroup = new Group(group);
                            exitGroup();
                        }
                    }
                }
            });
        }

        finishedLoading = true;
    }

    private void loadGroupsFromIdCategory() {

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

                            if (category.equals(ActiveCategory.activecategory.getName())) {
                                System.out.println("CREANDO GRUPO: " + name);

                                Group tmp_group = new Group(name, description, group, users, admins, imageURL, database, category);
                                if (FirestoreLink.checkIntegrity(tmp_group)) {
                                    groups.add(new Group(name, description, group, users, admins, imageURL, database, category));
                                    updateAdapter();
                                }
                            }

                        }
                    }
                }
            });
        }
        ActiveCategory.navigateCategory = false;
        finishedLoading = true;
    }

    private void loadAllGroups() {
        flag = true;
        FirebaseFirestore.getInstance()
                .collection("chats")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (flag) {
                            flag = false;
                        } else {
                            return;
                        }
                        if (task.isSuccessful()) {
                            List<DocumentSnapshot> myListOfDocuments = task.getResult().getDocuments();
                            groups = new ArrayList<>();
                            group_id_list = new ArrayList<>();
                            for (DocumentSnapshot d: myListOfDocuments) {
                                group_id_list.add(d.getId());
                            }
                            if(checkCategory()) {
                                loadGroupsFromIdCategory();
                            } else {
                                loadGroupsFromId();
                            }
                        }
                    }
                });
    }

    private void exitGroup() {
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentFirebaseUser == null) return;

        DocumentReference docRef = db.collection("users").document(currentFirebaseUser.getUid());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        ArrayList<String> groups = (ArrayList<String>) document.get("groups");

                        String toRemove = "";
                        boolean found = false;

                        for (String s : groups) {
                            if (s.equals(ActiveGroup.activeGroup.getId())) {
                                toRemove = s;
                                found = true;
                            }
                        }

                        if (!found) return;

                        groups.remove(toRemove);

                        Map<String, Object> user = new HashMap<>();
                        user.put("categories", ActiveUser.activeuser.getCategories());
                        user.put("location", ActiveUser.activeuser.getLocation());
                        user.put("status", ActiveUser.activeuser.getStatus());
                        user.put("name", ActiveUser.activeuser.getName());
                        user.put("imageURL", ActiveUser.activeuser.getImageURL());
                        user.put("groups", groups);

                        User tmp_user = new User(
                                ActiveUser.activeuser.getUid(),
                                ActiveUser.activeuser.getName(),
                                ActiveUser.activeuser.getImageURL(),
                                ActiveUser.activeuser.getStatus(),
                                ActiveUser.activeuser.getLocation(),
                                groups,
                                ActiveUser.activeuser.getCategories()
                        );

                        if (FirestoreLink.checkIntegrity(tmp_user)) {
                            db.collection("users").document(currentFirebaseUser.getUid()).set(user);

                            exitGroupRemoveFromChat();
                        }
                    }
                }
            }
        });
    }

    private void exitGroupRemoveFromChat() {
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
                                                               if (s.equals(currentFirebaseUser.getUid())) {
                                                                   toRemove = s;
                                                                   found = true;
                                                               }
                                                           }

                                                           if (found) {
                                                               users.remove(toRemove);
                                                           }

                                                           found = false;

                                                           for (String s : admins) {
                                                               if (s.equals(ActiveUser.activeuser.getUid())) {
                                                                   toRemove = s;
                                                                   found = true;
                                                               }
                                                           }

                                                           if (found) {
                                                               admins.remove(toRemove);
                                                           }

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

                                                               Intent i = getIntent();
                                                               i.removeExtra("exitActive");

                                                               Group GroupToRemove = null;
                                                               found = false;

                                                               for (Group g : groups) {
                                                                   if (g.getId().equals(ActiveGroup.activeGroup.getId())) {
                                                                       GroupToRemove = g;
                                                                       found = true;
                                                                   }
                                                               }

                                                               if (found) {
                                                                   groups.remove(GroupToRemove);
                                                               }

                                                               ActiveGroup.activeGroup = null;

                                                               intent = new Intent(GroupList.this, MainActivity.class);
                                                               intent.putExtra("returnToGroupList", "true");
                                                               startActivity(intent);
                                                               finish();
                                                           }
                                                       }
                                                   }
                                               }
                                           });
    }

    private void deleteGroup() {
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentFirebaseUser == null) return;

        DocumentReference docRef = db.collection("chats").document(ActiveGroup.activeGroup.getId());
        docRef.delete();
        exitGroup();
    }

    private void loadBar() {
        BottomNavigationView view = findViewById(R.id.group_list_home_bar);
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
                        if (checkGlobal()) {
                            getIntent().removeExtra("global");
                            finish();
                            startActivity(getIntent());
                        }
                        break;
                    }
                    case R.id.bar_menu_home: {
                        Intent openMainActivity= new Intent(GroupList.this, MainActivity.class);
                        openMainActivity.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
                        startActivityIfNeeded(openMainActivity, 0);
                        break;
                    }
                    case R.id.bar_menu_profile: {
                        Intent openMainActivity= new Intent(GroupList.this, CreateUser.class);
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

    private boolean checkGlobal() {
        if (getIntent().hasExtra("global")) {
            if (getIntent().getStringExtra("global").equals("true")) {
                return true;
            }
        }
        return false;
    }

    private boolean checkCategory() {
        return ActiveCategory.navigateCategory;
    }

    private void showArrow() {
        ImageView search_here = findViewById(R.id.search_here);
        // search_here.setVisibility(View.VISIBLE);
    }

    private void hideArrow() {
        ImageView search_here = findViewById(R.id.search_here);
        search_here.setVisibility(View.GONE);
    }
}
