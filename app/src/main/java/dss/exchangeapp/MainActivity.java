package dss.exchangeapp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    private RequestQueue mQueue;
    private Thread secThread;
    private Runnable runnable;
    private ListView listView;
    private CustomArrayAdapter adapter;
    private List<ListItemClass> arrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        listView = findViewById(R.id.listView);
        mQueue = Volley.newRequestQueue(this);
        arrayList = new ArrayList<>();
        adapter = new CustomArrayAdapter(this, R.layout.list_item_1, arrayList, getLayoutInflater());
        listView.setAdapter(adapter);
        Button buttonParse = findViewById(R.id.buttonParse);

        buttonParse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                        adapter.clear();
                        adapter.notifyDataSetChanged();
                        getWeb();
            }
        });

        runnable = new Runnable() {
            @Override
            public void run() {
                getWeb();
            }
        };

        secThread = new Thread(runnable);
        secThread.start();
        reset();
    }

    public void reset() {
        long timeInterval = 0;
        long delaySeconds = 10000;
        TimerTask taskOne = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.clear();
                        adapter.notifyDataSetChanged();
                    }
                });
                getWeb();
            }
        };
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(taskOne, timeInterval, delaySeconds);
    }

    public void getWeb() {

        String url = "https://www.cbr-xml-daily.ru/daily_json.js";

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject valuteObj = response.getJSONObject("Valute");
                            Iterator x = valuteObj.keys();
                            JSONArray listOfValute = new JSONArray();

                            while (x.hasNext()) {
                                String key = (String) x.next();
                                listOfValute.put(valuteObj.get(key));
                            }

                            for (int i = 0; i < listOfValute.length(); i++) {
                                JSONObject nameVal = listOfValute.getJSONObject(i);
                                Iterator y = nameVal.keys();
                                JSONArray mapOfValute = new JSONArray();

                                while (x.hasNext()) {
                                    String key = (String) x.next();
                                    mapOfValute.put(nameVal.get(key));
                                }

                                String charCode = nameVal.getString("CharCode");
                                String nominal = nameVal.getString("Nominal");
                                String name = nameVal.getString("Name");
                                String value = nameVal.getString("Value");

                                ListItemClass items = new ListItemClass();
                                items.setData_1(charCode);
                                items.setData_2(nominal);
                                items.setData_3(name);
                                items.setData_4(value);
                                arrayList.add(items);

                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    adapter.notifyDataSetChanged();
                                }
                            });

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        mQueue.add(request);
    }
}