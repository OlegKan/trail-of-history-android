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

import org.charmeck.trailofhistory.model.PointOfInterest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class JsonPointOfInterestUtils {

    private static final String JSON_ID = "id";
    private static final String JSON_NAME = "name";
    private static final String JSON_DESCRIPTION = "description";
    private static final String JSON_LATITUDE = "latitude";
    private static final String JSON_LONGITUDE = "longitude";

    public static JSONArray toJsonArray(List<PointOfInterest> list) throws JSONException {
        JSONArray jsonArray = new JSONArray();

        for (PointOfInterest pointOfInterest : list) {
            jsonArray.put(toJson(pointOfInterest));
        }

        return jsonArray;
    }

    public static JSONObject toJson(PointOfInterest pointOfInterest) throws JSONException {
        JSONObject json = new JSONObject();
        json.put(JSON_ID, pointOfInterest.getId());
        json.put(JSON_NAME, pointOfInterest.getName());
        json.put(JSON_DESCRIPTION, pointOfInterest.getDescription());
        json.put(JSON_LATITUDE, pointOfInterest.getLatitude());
        json.put(JSON_LONGITUDE, pointOfInterest.getLongitude());
        return json;
    }

    public static PointOfInterest toPointOfInterest(JSONObject jsonObject) throws JSONException {
        return new PointOfInterest(jsonObject.getInt(JSON_ID),
                jsonObject.getString(JSON_NAME),
                jsonObject.getString(JSON_DESCRIPTION),
                jsonObject.getDouble(JSON_LATITUDE),
                jsonObject.getDouble(JSON_LONGITUDE));
    }
}
