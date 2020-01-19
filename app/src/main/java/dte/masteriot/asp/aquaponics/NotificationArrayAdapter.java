package dte.masteriot.asp.aquaponics;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;


public class NotificationArrayAdapter extends ArrayAdapter<NOTIFICATION> {
    private ArrayList<NOTIFICATION> items;
    private Context mContext;

    public NotificationArrayAdapter(Context context, ArrayList<NOTIFICATION> notifications ) {
        super( context, 0, notifications );
        items = notifications;
        mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent ) {

        ViewHolder holder;
        NOTIFICATION notification = items.get(position);
        // This approach can be improved for performance
        if ( convertView == null ) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.notification_list_item, null);
            /*LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            newView = inflater.inflate(R.layout.country_list_item, parent, false);*/
            holder = new ViewHolder();
            holder.textView = (TextView) convertView.findViewById(R.id.NType);
            convertView.setTag(holder);
        } else{
            holder = (ViewHolder) convertView.getTag();
        }
        //-----
        holder.textView.setText(notification.params.type);
        return convertView;
    }

    public class ViewHolder{
        TextView textView;
    }
}
