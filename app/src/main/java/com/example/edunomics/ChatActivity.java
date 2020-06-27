package com.example.edunomics;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ChatActivity extends AppCompatActivity {
    private ProgressDialog pDialog;
    private ListView lv;
    ArrayList<HashMap<String, String>> contactList;


   // List<GetContacts.Message> messages = new ArrayList<>();
    //List<GetContacts.Message> Mymessages = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences prefs;
        prefs = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        setTitle(prefs.getString("name",""));
        setContentView(R.layout.activity_chat);
        contactList = new ArrayList<>();
        lv = (ListView) findViewById(R.id.messages_view);
        new GetContacts().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        Button but=(Button)findViewById(R.id.button);
        but.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                postData();

            }
        });


    }
    public void postData() {
        // Create a new HttpClient and Post Header
        class ss extends AsyncTask<Void, Void, String>
        {

            @Override
            protected String doInBackground(Void... params) {
                // TODO Auto-generated method stub
              //  HttpClient httpclient = new DefaultHttpClient();
               // String result = null;
                //HttpPost httppost = new HttpPost();
                String roomId = "";
                try {
                    String _currentLoginId;
                    SharedPreferences prefs;
                    prefs = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
                    _currentLoginId = prefs.getString("id","");
                    Intent ch = getIntent();
                    String roomid = prefs.getString("roomid","");
                    String loginid = ch.getStringExtra("loginid");
                    EditText edit1=(EditText)findViewById(R.id.editText);
                    String query = "https://www.niceonecode.com/api/NCChat";
                    String json = "[\""+edit1.getText()+"\",\"\",\""+roomid+"\",\""+_currentLoginId+"\",\""+loginid+"\"]";

                    URL url = null;
                    try {
                        url = new URL(query);
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                    HttpURLConnection conn = null;
                    try {
                        conn = (HttpURLConnection) url.openConnection();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    //conn.setConnectTimeout(5000);
                    conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    conn.setDoOutput(true);
                    conn.setDoInput(true);
                    try {
                        conn.setRequestMethod("POST");
                    } catch (ProtocolException e) {
                        e.printStackTrace();
                    }

                    OutputStream os = null;
                    try {
                        os = conn.getOutputStream();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        os.write(json.getBytes("UTF-8"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        os.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    // read the response
                    InputStream in = null;
                    try {
                        in = new BufferedInputStream(conn.getInputStream());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    HttpHandler sh = new HttpHandler();
                    String jsonStr = sh.convertStreamToString(in);
                    conn.disconnect();

                    //JSONObject roomIDResponse = new JSONObject(jsonStr);
                    roomId=jsonStr.replace("\"","").replace("\\n","");
                    // Simulate network access.
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    roomId="";
                }

                return  roomId;
            }
            @Override
            protected void onPostExecute(String result) {
                // TODO Auto-generated method stub
                if(!result.equals(""))
                {
                    SharedPreferences prefs;
                    SharedPreferences.Editor edit;
                    prefs=getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
                    edit=prefs.edit();
                    edit.putString("roomid", result);
                    edit.commit();
                    Toast.makeText(getApplicationContext(), "message sent", Toast.LENGTH_LONG).show();
                    EditText edit1=(EditText)findViewById(R.id.editText);
                    edit1.setText("");
                    new GetContacts().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
                else
                    Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();

                super.onPostExecute(result);
            }

        }
        ss s=new ss();
        s.execute(null,null,null);
    }


    public class GetContacts extends AsyncTask<Void, Void, Void>  {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog

            pDialog = new ProgressDialog(ChatActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

        }


        @Override
        protected Void doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            boolean IsValidUser = false;
            String _currentLoginId;
            SharedPreferences prefs;
            prefs = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
            _currentLoginId = prefs.getString("id","");
            Intent ch = getIntent();
            String roomid = prefs.getString("roomid","");
            String loginid = ch.getStringExtra("loginid");
            try {
                String query = "https://www.niceonecode.com/api/NCChat/?RowCount=0&TableId=&roomid="+roomid+"&CurrentLoginId="+_currentLoginId+"";
                HttpHandler sh = new HttpHandler();
                // Making a request to url and getting response
                String jsonStr = sh.makeServiceCall(query);


                JSONArray messageDetails = new JSONArray(jsonStr);

                Message message;
                for (int i = 0; i < messageDetails.length(); i++) {
                    JSONObject c = messageDetails.getJSONObject(i);

                    String MessageData  = c.getString("MessageData");
                    boolean  IsFriend   =c.getBoolean("IsFriend");
                   String id = c.getString("LoginID");
                    String userID = c.getString("UserID");
                    String name = c.getString("UserName");
                    String photo = c.getString("UserImageBase64String");


                    HashMap<String, String> contact = new HashMap<>();

                    contact.put("id", id);
                    contact.put("name", name);
                    contact.put("MessageData",MessageData);
                    contact.put("IsFriend", String.valueOf(IsFriend));
                    contact.put("photo",photo);


                    contactList.add(contact);

                  /*  if(message.belongsToCurrentUser)
                       contactList.add(message);
                    else
                        contactList.add(message);*/
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if (pDialog.isShowing())
                pDialog.dismiss();
            ListAdapter adapter = new SimpleAdapter(
                    ChatActivity.this, contactList,
                    R.layout.their_message, new String[]{"MessageData", "name"
            }, new int[]{R.id.message_body, R.id.name
            });
            lv.setAdapter(adapter);
            /*for (HashMap message:contactList
                 ) {

                ArrayList<HashMap<String, String>> mymessagelist=new ArrayList<HashMap<String, String>>();
                HashMap<String, String> contact = new HashMap<>();
                contact.put("id", message.get("id").toString());
                contact.put("name", message.get("name").toString());
                contact.put("MessageData",message.get("MessageData").toString());
                contact.put("IsFriend", message.get("IsFriend").toString());
                contact.put("photo",message.get("photo").toString());
                mymessagelist.add(contact);
           if(message.get("IsFriend").equals("false"))
            {
                ListAdapter adapter = new SimpleAdapter(
                        ChatActivity.this, mymessagelist,
                        R.layout.my_message, new String[]{"MessageData",
                }, new int[]{R.id.message_body
                });
                lv.setAdapter(adapter);
            }
            else{
                ListAdapter adapter = new SimpleAdapter(
                        ChatActivity.this, mymessagelist,
                        R.layout.their_message, new String[]{"MessageData", "name"
                }, new int[]{R.id.message_body, R.id.name
                });


                lv.setAdapter(adapter);

            }}*/


        }

        public class Message {
            private String text; // message body
            private String data; // data of the user that sent this message
            private boolean belongsToCurrentUser; // is this message sent by us?

            public Message(String text, String data, boolean belongsToCurrentUser) {
                this.text = text;
                this.data = data;
                this.belongsToCurrentUser = belongsToCurrentUser;
            }

            public String getText() {
                return text;
            }

            public String getData() {
                return data;
            }

            public boolean isBelongsToCurrentUser() {
                return belongsToCurrentUser;
            }

        }


    }


}
