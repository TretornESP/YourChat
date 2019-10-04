package tretornesp.clickerchat3;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class UserListAdapter extends ArrayAdapter<User> {

    private Context mContext;
    private int mResource;

    private static class ViewHolder {
        TextView name;
        ImageView image;
        Button kick;
    }

    UserListAdapter(Context context, int resource, ArrayList<User> users) {
        super(context, resource, users);
        mContext = context;
        mResource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        setupImageLoader();

        final String uid = Objects.requireNonNull(getItem(position)).getUid();
        String name = Objects.requireNonNull(getItem(position)).getName();
        String imgUrl = Objects.requireNonNull(getItem(position)).getImageURL();

        ViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(mResource, parent, false);
            holder = new ViewHolder();
            holder.name = convertView.findViewById(R.id.textView1_group);
            holder.image = convertView.findViewById(R.id.image_group);
            //holder.kick = convertView.findViewById(R.id.kick);

            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        ImageLoader imageLoader = ImageLoader.getInstance();

        int defaultImage = mContext.getResources().getIdentifier("@drawable/image_default_user", null, mContext.getPackageName());

        DisplayImageOptions options = new DisplayImageOptions.Builder().cacheInMemory(true)
                .cacheOnDisc(true).resetViewBeforeLoading(true)
                .showImageForEmptyUri(defaultImage)
                .showImageOnFail(defaultImage)
                .showImageOnLoading(defaultImage).build();

        imageLoader.displayImage(imgUrl, holder.image, options);

        holder.name.setText(name);
        /*
        holder.kick.setVisibility(View.GONE);

        for (String admins: ActiveGroup.activeGroup.getAdmins()) {
            if (admins.equals(ActiveUser.activeuser.getUid())) {
                holder.kick.setVisibility(View.VISIBLE);
                holder.kick.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        new AlertDialog.Builder(mContext)
                                .setIcon(R.drawable.image_confirmation)
                                .setTitle("Expulsar")
                                .setMessage("¿Desea expulsar al usuario?")
                                .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        System.out.println("KICKING: "+uid);
                                        kick(uid);
                                    }
                                })
                                .setNegativeButton("No", null)
                                .show();
                    }
                });
            }
        }
        */

        return convertView;

    }

    private void setupImageLoader() {
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheOnDisk(true).cacheInMemory(true)
                .imageScaleType(ImageScaleType.EXACTLY)
                .displayer(new FadeInBitmapDisplayer(300)).build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                mContext)
                .defaultDisplayImageOptions(defaultOptions)
                .memoryCache(new WeakMemoryCache())
                .discCacheSize(1000 * 1024 * 1024).build();

        ImageLoader.getInstance().init(config);
    }
}
