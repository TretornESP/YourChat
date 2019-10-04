package tretornesp.clickerchat3;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Objects;

public class ViewProfile extends AppCompatActivity {

    private Context mContext;
    //private CategoryListAdapter adapter;
    private ArrayList<Category> categories;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ListView groupList;
    private ArrayList<Group> groups = new ArrayList<>();
    private ArrayList<String> group_id_list;
    private boolean flag;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_view_layout);

        mContext = ViewProfile.this;
        categories = new ArrayList<>();

        loadTitleBar();
        loadProfilePicture();
        loadDescription();
        loadCategories();
        loadLocation();
        loadGroups();
    }

    private void loadTitleBar() {
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(false);
        getSupportActionBar().setTitle(VisualizingUser.visualizing_user.getName());
    }

    private void loadProfilePicture() {
        setupImageLoader();

        ImageLoader imageLoader = ImageLoader.getInstance();

        int defaultImage = mContext.getResources().getIdentifier("@drawable/image_default", null, mContext.getPackageName());

        DisplayImageOptions options = new DisplayImageOptions.Builder().cacheInMemory(true)
                .cacheOnDisc(true).resetViewBeforeLoading(true)
                .showImageForEmptyUri(defaultImage)
                .showImageOnFail(defaultImage)
                .showImageOnLoading(defaultImage).build();

        Bitmap bitmap = imageLoader.loadImageSync(VisualizingUser.visualizing_user.getImageURL(), options);

        ImageView icon = (ImageView) findViewById(R.id.view_profile_image);

        icon.setImageBitmap(bitmap);
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

    private void loadDescription() {
        TextView description = findViewById(R.id.view_profile_description);
        description.setText(VisualizingUser.visualizing_user.getStatus());
    }

    private void loadCategories() {

        mRecyclerView = (RecyclerView) findViewById(R.id.profile_view_categories);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new CategoryListAdapter(mContext, categories);
        mRecyclerView.setAdapter(mAdapter);

        mLayoutManager = new LinearLayoutManager(ViewProfile.this, LinearLayoutManager.HORIZONTAL, false);
        mRecyclerView.setLayoutManager(mLayoutManager);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        for (String s: VisualizingUser.visualizing_user.getCategories()) {
            categories.add(new Category(s));
            mRecyclerView = (RecyclerView) findViewById(R.id.profile_view_categories);
            mAdapter = new CategoryListAdapter(mContext, categories);
            mRecyclerView.setAdapter(mAdapter);
        }
    }

    private void loadLocation() {
        TextView location = findViewById(R.id.location_text);
        location.setText(VisualizingUser.visualizing_user.getLocation());
    }

    private void loadGroups() {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        groups = new ArrayList<>();

        group_id_list = new ArrayList<>();

        FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentFirebaseUser == null) return;

        DocumentReference docRef = db.collection("users").document(VisualizingUser.visualizing_user.getUid());
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
            System.out.println("GRUPOS: " + group);
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

                            System.out.println("CREANDO GRUPO: " + name);

                            Group tmp_group = new Group(name, description, group, users, admins, imageURL, database, category);

                            if (FirestoreLink.checkIntegrity(tmp_group)) {
                                groups.add(tmp_group);

                                updateAdapter();
                            }
                        }
                    }
                }
            });
        }
    }

    private void updateAdapter() {

        if (groups == null || groups.size() == 0) {
            loadGroups();
        }

        groupList = findViewById(R.id.view_profile_group_list);
        GroupListAdapter adapter = new GroupListAdapter(ViewProfile.this, R.layout.adapter_view_layout, groups);
        groupList.setAdapter(adapter);
    }
}
