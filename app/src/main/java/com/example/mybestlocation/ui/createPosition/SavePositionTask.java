package com.example.mybestlocation.ui.createPosition;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;
import com.example.mybestlocation.Config;
import com.example.mybestlocation.JSONParser;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;


public class SavePositionTask extends AsyncTask<HashMap<String, String>, Void, JSONObject> {
    private final Context context;

    public SavePositionTask(Context context) {
        this.context = context;
    }

    @Override
    protected JSONObject doInBackground(HashMap<String, String>... params) {
        if (params.length > 0) {
            HashMap<String, String> data = params[0];
            JSONParser jsonParser = new JSONParser();
            return jsonParser.makeHttpRequest(Config.Url_AddPosition, "POST", data);
        }
        return null;
    }

    @Override
    protected void onPostExecute(JSONObject response) {
        if (response == null) {
            Toast.makeText(context, "Error: No response from server", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int success = response.getInt("success");
            if (success > 0) {
                Toast.makeText(context, "Position saved successfully!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Failed to save position!", Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(context, "Error processing server response", Toast.LENGTH_SHORT).show();
        }
    }
}
