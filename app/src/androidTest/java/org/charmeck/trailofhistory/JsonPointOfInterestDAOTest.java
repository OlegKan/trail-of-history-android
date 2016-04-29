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

package org.charmeck.trailofhistory;

import android.support.test.InstrumentationRegistry;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;

import org.charmeck.trailofhistory.dao.BaseDAO;
import org.charmeck.trailofhistory.dao.DAOFactory;
import org.charmeck.trailofhistory.dao.JsonPointOfInterestDAO;
import org.charmeck.trailofhistory.model.PointOfInterest;

import java.util.List;

public class JsonPointOfInterestDAOTest extends ActivityInstrumentationTestCase2<MainActivity> {

    private static final String TAG = "JsonPointOfInterestDAO";

    private MainActivity mActivity;

    public JsonPointOfInterestDAOTest() {
        super(MainActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        injectInstrumentation(InstrumentationRegistry.getInstrumentation());
        mActivity = getActivity();

        BaseDAO dao = new JsonPointOfInterestDAO(mActivity);
        List<PointOfInterest> list = dao.read();

        for (PointOfInterest pointOfInterest : list) {
            dao.delete(pointOfInterest);
        }
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testJsonPointOfInterestDAO() {
        Log.d(TAG, "start: testJsonPointOfInterestDAO");

        BaseDAO<PointOfInterest> dao = DAOFactory.getInstance(mActivity).getPointOfInterestDAO();

        PointOfInterest poi1 = new PointOfInterest(1, "poi name 1", "description 1", 12.34, 45.67);
        PointOfInterest poi2 = new PointOfInterest(2, "poi name 2", "description 2", 23.45, 56.78);

        // create
        int id1 = dao.create(poi1);
        int expected1 = 1;
        assertEquals("created id should be = " + expected1, expected1, id1);

        int id2 = dao.create(poi2);
        int expected2 = 2;
        assertEquals("created id should be = " + expected2, expected2, id2);

        // read all
        List<PointOfInterest> list = dao.read();

        int expectedListSize = 2;
        assertEquals("list size should be = " + expectedListSize, expectedListSize, list.size());

        assertEquals("poi1 should be = list.get(0)", poi1, list.get(0));
        assertEquals("poi2 should be = list.get(1)", poi2, list.get(1));

        // read by id
        assertEquals("poi1 should be = dao.read(1)", poi1, dao.read(1));
        assertEquals("poi2 should be = dao.read(1)", poi2, dao.read(2));

        // update
        String newName1 = "new " + poi1.getName();
        poi1.setName(newName1);
        dao.update(poi1);
        assertEquals("poi1.getName() should be = " + newName1, newName1, dao.read(1).getName());

        String newName2 = "new " + poi2.getName();
        poi2.setName(newName2);
        dao.update(poi2);
        assertEquals("poi2.getName() should be = " + newName2, newName2, dao.read(2).getName());

        // delete by object
        boolean deletedPoi2 = dao.delete(poi2);
        assertTrue("for poi2 deleted flag should be true", deletedPoi2);

        list = dao.read();

        expectedListSize = 1;
        assertEquals("list size should be = " + expectedListSize, expectedListSize, list.size());

        PointOfInterest poi3 = new PointOfInterest(3, "poi name 3", "description 3", 5.19, 10.03);
        boolean deletedPoi3 = dao.delete(poi3);
        assertTrue("for poi3 deleted flag should be false", !deletedPoi3);

        // delete by id
        boolean deletedPoi1 = dao.delete(1);
        assertTrue("for id 1 deleted flag should be true", deletedPoi1);

        list = dao.read();

        expectedListSize = 0;
        assertEquals("list size should be = " + expectedListSize, expectedListSize, list.size());

        boolean deletedPoi4 = dao.delete(4);
        assertTrue("for id 4 deleted flag should be false", !deletedPoi4);

        Log.d(TAG, "end: testJsonPointOfInterestDAO");
    }

}