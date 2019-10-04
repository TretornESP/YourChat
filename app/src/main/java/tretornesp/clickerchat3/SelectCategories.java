package tretornesp.clickerchat3;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.accessibility.AccessibilityRecord;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class SelectCategories extends AppCompatActivity {

    private MaterialSearchView searchView;
    private ListView categoryList;
    private ArrayList<Category> categories = new ArrayList<>();
    private ArrayList<String> categories_id_list;
    private ArrayList<String> categories_selected_list;
    private  Intent intent;

    private boolean flag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_categories_layout);

        categories_selected_list = ActiveUser.activeuser.getCategories();

        loadToolbar();
        loadSearchView();
        loadGroupList();
        loadOkayButton();
    }

    private void loadToolbar() {
        Toolbar toolbar = findViewById(R.id.categories_toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Seleccionar Categorias");
        toolbar.setTitleTextColor(Color.parseColor("#FFFFFF"));
    }

    private void loadGroupList() {
       categoryList = findViewById(R.id.categories_listView);

        loadGroups();

        CategoryListAdapterListView adapter = new CategoryListAdapterListView(SelectCategories.this, R.layout.adapter_view_category_equal_layout, categories);
        categoryList.setAdapter(adapter);

        categoryList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View v, int position, long arg3) {
                Category item = (Category) adapter.getItemAtPosition(position);

                for (String s: categories_selected_list) {
                    if (s.equals(item.getName())) {
                        RelativeLayout layout = (RelativeLayout) categoryList.getChildAt(position);
                        layout.setBackgroundResource(R.drawable.category_bg);
                        TextView text = (TextView) layout.findViewById(R.id.category_name_equal);
                        text.setTextColor(getResources().getColor(R.color.black));
                        categories_selected_list.remove(s);
                        return;
                    }
                }
                categories_selected_list.add(item.getName());
                RelativeLayout layout = (RelativeLayout) categoryList.getChildAt(position);
                TextView text = (TextView) layout.findViewById(R.id.category_name_equal);
                text.setTextColor(getResources().getColor(R.color.white));
                layout.setBackgroundResource(R.drawable.category_bg_selected);

            }
        });

        updateAdapter();
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    public void onResume()
    {
        super.onResume();

        loadGroups();
        updateAdapter();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.group_menu_item, menu);
        MenuItem item = menu.findItem(R.id.action_search);
        searchView.setMenuItem(item);
        return true;
    }

    private void loadGroups() {

        categories = new ArrayList<>();

        categories_id_list = new ArrayList<>();

        for (String s : getResources().getStringArray(R.array.categories)) {
            categories_id_list.add(s);
            categories.add(new Category(s));

        }

        updateAdapter();
    }

    private void updateAdapter() {

        if (categories == null || categories.size() == 0) {
            loadGroups();
        }

        categoryList = findViewById(R.id.categories_listView);
        CategoryListAdapterListView adapter = new CategoryListAdapterListView(SelectCategories.this, R.layout.adapter_view_category_equal_layout, categories);
        categoryList.setAdapter(adapter);

    }

    private void loadOkayButton() {
        Button button = findViewById(R.id.categories_okay);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActiveUser.activeuser.setCategories(categories_selected_list);
                updateCategories();
                finish();
            }
        });
    }

    private void updateCategories() {
        Map<String, Object> parsed = new HashMap<>();
        parsed.put("categories", ActiveUser.activeuser.getCategories());
        parsed.put("groups", ActiveUser.activeuser.getGroups());
        parsed.put("imageURL", ActiveUser.activeuser.getImageURL());
        parsed.put("location", ActiveUser.activeuser.getLocation());
        parsed.put("name", ActiveUser.activeuser.getName());
        parsed.put("status", ActiveUser.activeuser.getStatus());

        User tmp_user = new User(
                ActiveUser.activeuser.getUid(),
                ActiveUser.activeuser.getName(),
                ActiveUser.activeuser.getImageURL(),
                ActiveUser.activeuser.getStatus(),
                ActiveUser.activeuser.getLocation(),
                ActiveUser.activeuser.getGroups(),
                ActiveUser.activeuser.getCategories()
        );

        if (FirestoreLink.checkIntegrity(tmp_user)) {

            FirebaseFirestore db = FirebaseFirestore.getInstance();

            db.collection("users").document(ActiveUser.activeuser.getUid())
                    .set(parsed)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            finish();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            new AlertDialog.Builder(getApplicationContext())
                                    .setIcon(R.drawable.image_error)
                                    .setTitle("Error")
                                    .setMessage("El grupo no se ha creado por un error desconocido, inténtelo de nuevo mas tarde")
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

    private void loadSearchView() {
        searchView = findViewById(R.id.categories_search_view);

        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {

            }

            @Override
            public void onSearchViewClosed() {
                categoryList = findViewById(R.id.categories_listView);
                CategoryListAdapterListView adapter = new CategoryListAdapterListView(SelectCategories.this, R.layout.adapter_view_category_equal_layout, categories);
                categoryList.setAdapter(adapter);
            }
        });

        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText != null && !newText.isEmpty()) {
                    ArrayList<Category> lstFound = new ArrayList<>();
                    for (Category category : categories) {
                        if (category.getName().toLowerCase().contains(newText.toLowerCase())) {
                            lstFound.add(category);
                        }
                    }

                    CategoryListAdapterListView adapter = new CategoryListAdapterListView(SelectCategories.this, R.layout.adapter_view_category_equal_layout, lstFound);
                    categoryList.setAdapter(adapter);
                } else {
                    CategoryListAdapterListView adapter = new CategoryListAdapterListView(SelectCategories.this, R.layout.adapter_view_category_equal_layout, categories);
                    categoryList.setAdapter(adapter);
                }
                return true;
            }
        });
    }
}
