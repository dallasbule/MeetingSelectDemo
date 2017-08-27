package example.com.meetingselectdemo;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;

/**
 * Created by Administrator on 2017/8/24.
 */

public class PeopleListView extends Activity {


    private ListView listView;

    @Override
    public void onCreate(Bundle saveInstanceState){
        super.onCreate(saveInstanceState);
        setContentView(R.layout.listview);
        listView=(ListView)findViewById(R.id.lv);


    }

}
