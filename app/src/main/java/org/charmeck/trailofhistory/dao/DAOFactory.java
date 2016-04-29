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

public class DAOFactory {

    private static DAOFactory instance;
    private Context context;

    public static DAOFactory getInstance(Context context) {
        if (instance == null) {
            instance = new DAOFactory(context.getApplicationContext());
        }
        return instance;
    }

    private DAOFactory(Context context) {
        this.context = context;
    }

    public PointOfInterestDAO getPointOfInterestDAO() {
        return new JsonPointOfInterestDAO(context);
    }
}
