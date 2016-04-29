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

import org.charmeck.trailofhistory.model.PointOfInterest;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class PointOfInterestTest {
    @Test
    public void poiShouldBeEqual() throws Exception {
        PointOfInterest poi1 = new PointOfInterest(1, "new poi name", "new description", 12.34, 45.67);
        PointOfInterest poi2 = new PointOfInterest(1, "new poi name", "new description", 12.34, 45.67);
        assertTrue(poi1.equals(poi2));
    }

    @Test
    public void poiShouldNotBeEqual() throws Exception {
        PointOfInterest poi1 = new PointOfInterest(1, "new poi name 1", "new description 1", 12.34, 45.67);
        PointOfInterest poi2 = new PointOfInterest(2, "new poi name 2", "new description 2", 23.45, 56.78);
        assertTrue(!poi1.equals(poi2));
    }

    @Test
    public void poiHashCodeShouldBeEqual() throws Exception {
        PointOfInterest poi1 = new PointOfInterest(1, "new poi name", "new description", 12.34, 45.67);
        PointOfInterest poi2 = new PointOfInterest(1, "new poi name", "new description", 12.34, 45.67);
        assertTrue(poi1.hashCode() == poi2.hashCode());
    }

    @Test
    public void poiHashCodeShouldNotBeEqual() throws Exception {
        PointOfInterest poi1 = new PointOfInterest(1, "new poi name 1", "new description 1", 12.34, 45.67);
        PointOfInterest poi2 = new PointOfInterest(2, "new poi name 2", "new description 2", 23.45, 56.78);
        assertTrue(poi1.hashCode() != poi2.hashCode());
    }
}