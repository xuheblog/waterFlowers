package com.xuhe.waterflower;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2016/11/26.
 */
public class SharedHelper {
    private Context mContext;
    public SharedHelper(){
    }
    public SharedHelper(Context mContext){
        this.mContext = mContext;
    }

    public void save(String humidity_one,String humidity_two,String humidity_one_high,String humidity_two_high){
        SharedPreferences sp = mContext.getSharedPreferences("mysp",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        if (humidity_two != null)
            editor.putString("humidity_two",humidity_two);
        else if (humidity_one !=null)
            editor.putString("humidity_one",humidity_one);
        else if (humidity_one_high != null)
            editor.putString("humidity_one_high",humidity_one_high);
        else if (humidity_two_high !=null)
            editor.putString("humidity_two_high",humidity_two_high);
        editor.apply();
    }
    public Map<String,String> read(){
        Map<String,String> data = new HashMap<String,String>();
        SharedPreferences sp = mContext.getSharedPreferences("mysp",Context.MODE_PRIVATE);
        data.put("humidity_one",sp.getString("humidity_one",""));
        data.put("humidity_two",sp.getString("humidity_two",""));
        data.put("humidity_one_high",sp.getString("humidity_one_high",""));
        data.put("humidity_two_high",sp.getString("humidity_two_high",""));
        return data;
    }
}
