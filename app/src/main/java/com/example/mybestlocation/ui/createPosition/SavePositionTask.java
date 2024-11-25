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
    private final SavePositionListener listener;

    public SavePositionTask(Context context, SavePositionListener listener) {
        this.context = context;
        this.listener = listener;
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
            showToast("Error: No response from server");
            return;
        }

        try {
            if (response.getInt("success") > 0) {
                showToast("Position saved successfully!");
                if (listener != null) listener.onSavePositionSuccess();
            } else {
                showToast("Failed to save position!");
            }
        } catch (JSONException e) {
            e.printStackTrace();
            showToast("Error processing server response");
        }
    }

    /**
     * Displays a Toast message.
     *
     * @param message The message to be displayed.
     */
    private void showToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Interface for handling success callbacks after saving a position.
     */
    public interface SavePositionListener {
        void onSavePositionSuccess();
    }
}
