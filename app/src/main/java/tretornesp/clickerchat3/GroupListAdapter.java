package tretornesp.clickerchat3;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import java.util.ArrayList;
import java.util.Objects;

public class GroupListAdapter extends ArrayAdapter<Group> {

    private Context mContext;
    private int mResource;

    private static class ViewHolder {
        TextView name;
        TextView description;
        //TextView numero_usuarios;
        ImageView image;
    }

    GroupListAdapter(Context context, int resource, ArrayList<Group> groups) {
        super(context, resource, groups);
        mContext = context;
        mResource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        setupImageLoader();

        String name = Objects.requireNonNull(getItem(position)).getName();
        String description = Objects.requireNonNull(getItem(position)).getDescription();
        String numero_usuarios = Objects.requireNonNull(getItem(position)).getNumero_usuarios_asString();
        String imgUrl = Objects.requireNonNull(getItem(position)).getImageURL();

        ViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(mResource, parent, false);
            holder = new ViewHolder();
            holder.name = convertView.findViewById(R.id.textView1);
            holder.description = convertView.findViewById(R.id.textView3);
            //holder.numero_usuarios = convertView.findViewById(R.id.textView3);
            holder.image = convertView.findViewById(R.id.image);

            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        ImageLoader imageLoader = ImageLoader.getInstance();

        int defaultImage = mContext.getResources().getIdentifier("@drawable/image_default", null, mContext.getPackageName());

        DisplayImageOptions options = new DisplayImageOptions.Builder().cacheInMemory(true)
                .cacheOnDisc(true).resetViewBeforeLoading(true)
                .showImageForEmptyUri(defaultImage)
                .showImageOnFail(defaultImage)
                .showImageOnLoading(defaultImage).build();

        imageLoader.displayImage(imgUrl, holder.image, options);

        holder.name.setText(name);
        //holder.numero_usuarios.setText(numero_usuarios);
        holder.description.setText(description);

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
