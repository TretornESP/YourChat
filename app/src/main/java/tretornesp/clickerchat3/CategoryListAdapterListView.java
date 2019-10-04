package tretornesp.clickerchat3;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
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

public class CategoryListAdapterListView extends ArrayAdapter<Category> {
    private Context mContext;
    private int mResource;

    private static class ViewHolder {
        TextView name;
    }

    CategoryListAdapterListView(Context context, int resource, ArrayList<Category> categories) {
        super(context, resource,categories);
        mContext = context;
        mResource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

        String name = Objects.requireNonNull(getItem(position)).getName();

        ViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(mResource, parent, false);
            holder = new ViewHolder();
            holder.name = convertView.findViewById(R.id.category_name_equal);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (ActiveUser.activeuser.getCategories().contains(name)) {
            convertView.setBackgroundResource(R.drawable.category_bg_selected);
            holder.name.setTextColor(holder.name.getResources().getColor(R.color.white));
        } else {
            holder.name.setTextColor(holder.name.getResources().getColor(R.color.black));
        }
        holder.name.setText(name);


        return convertView;

    }
}
