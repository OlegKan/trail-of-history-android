/*
 * Copyright (C) 2016 Oleg Kan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.charmeck.trailofhistory.dao;

import android.content.Context;
import android.util.Log;

import org.charmeck.trailofhistory.model.PointOfInterest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class JsonPointOfInterestDAO implements PointOfInterestDAO {

    private static final String TAG = "JsonPointOfInterestDAO";

    private static final String JSON_FILE_NAME = "point_of_interest.json";
    private static final String JSON_LAST_ID = "last_id";
    private static final String JSON_ARRAY = "array";

    private Context context;

    public JsonPointOfInterestDAO(Context context) {
        this.context = context;
    }

    @Override
    public int create(PointOfInterest pointOfInterest) {
        int id = -1;
        try {
            JSONArray jsonArray = loadJsonArray();

            id = getNextId();
            pointOfInterest.setId(id);

            jsonArray.put(JsonPointOfInterestUtils.toJson(pointOfInterest));
            saveJsonArray(jsonArray);
        } catch (IOException | JSONException e) {
            Log.d(TAG, e.getLocalizedMessage());
        }

        return id;
    }

    @Override
    public List<PointOfInterest> read() {
        List<PointOfInterest> list = new ArrayList<>();

        try {
            JSONArray jsonArray = loadJsonArray();
            for (int i = 0; i < jsonArray.length(); i++) {
                list.add(JsonPointOfInterestUtils.toPointOfInterest(jsonArray.getJSONObject(i)));
            }
        } catch (IOException | JSONException e) {
            Log.d(TAG, e.getLocalizedMessage());
        }

        return list;
    }

    @Override
    public PointOfInterest read(int id) {
        List<PointOfInterest> list = read();

        for (PointOfInterest pointOfInterest : list) {
            if (pointOfInterest.getId() == id) {
                return pointOfInterest;
            }
        }

        return null;
    }

    @Override
    public boolean update(PointOfInterest pointOfInterest) {
        boolean result = false;

        List<PointOfInterest> list = read();

        try {
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).getId() == pointOfInterest.getId()) {
                    list.set(i, pointOfInterest);

                    saveJsonArray(JsonPointOfInterestUtils.toJsonArray(list));

                    result = true;
                    break;
                }
            }
        } catch (IOException | JSONException e) {
            Log.d(TAG, e.getLocalizedMessage());
        }

        return result;
    }

    @Override
    public boolean delete(PointOfInterest pointOfInterest) {
        if (pointOfInterest == null) return false;

        return delete(pointOfInterest.getId());
    }

    @Override
    public boolean delete(int id) {
        boolean result = false;

        List<PointOfInterest> list = read();

        try {
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).getId() == id) {
                    list.remove(i);

                    saveJsonArray(JsonPointOfInterestUtils.toJsonArray(list));

                    result = true;
                    break;
                }
            }
        } catch (IOException | JSONException e) {
            Log.d(TAG, e.getLocalizedMessage());
        }

        return result;
    }

    private JSONObject loadJsonFile() throws IOException, JSONException {
        JSONObject jsonObject = new JSONObject();

        BufferedReader reader = null;
        try {
            InputStream in = context.openFileInput(JSON_FILE_NAME);
            reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder jsonString = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonString.append(line);
            }

            jsonObject = (JSONObject) new JSONTokener(jsonString.toString()).nextValue();
        } catch (FileNotFoundException e) {
            // probably this is the first launch
            Log.d(TAG, e.getLocalizedMessage());
        } finally {
            if (reader != null) {
                reader.close();
            }
        }

        return jsonObject;
    }

    private JSONArray loadJsonArray() throws IOException, JSONException {
        JSONObject jsonObject = loadJsonFile();

        JSONArray jsonArray = new JSONArray();
        try {
            jsonArray = jsonObject.getJSONArray(JSON_ARRAY);
        } catch (JSONException e) {
            // probably this is the first launch
            Log.d(TAG, e.getLocalizedMessage());
        }

        return jsonArray;
    }

    private int getNextId() throws IOException, JSONException {
        JSONObject jsonObject = loadJsonFile();
        int lastId = 0;

        try {
            lastId = jsonObject.getInt(JSON_LAST_ID);
        } catch (JSONException e) {
            // probably this is the first launch
            Log.d(TAG, e.getLocalizedMessage());
        }

        return lastId + 1;
    }

    private void saveJsonArray(JSONArray jsonArray) throws FileNotFoundException, JSONException {
        int lastId;
        if (jsonArray.length() == 0) {
            lastId = 0;
        } else {
            PointOfInterest pointOfInterest = JsonPointOfInterestUtils.toPointOfInterest(
                    ((JSONObject) jsonArray.get(jsonArray.length() - 1)));
            lastId = pointOfInterest.getId();
        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put(JSON_LAST_ID, lastId);
        jsonObject.put(JSON_ARRAY, jsonArray);
        PrintWriter writer = null;
        try {
            OutputStream out = context.openFileOutput(JSON_FILE_NAME, Context.MODE_PRIVATE);
            writer = new PrintWriter(out);
            writer.println(jsonObject.toString());
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }
}
