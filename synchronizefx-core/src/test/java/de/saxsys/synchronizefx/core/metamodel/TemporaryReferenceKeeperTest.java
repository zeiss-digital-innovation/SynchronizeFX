/**
 * This file is part of SynchronizeFX.
 * 
 * Copyright (C) 2013-2014 Saxonia Systems AG
 *
 * SynchronizeFX is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SynchronizeFX is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with SynchronizeFX. If not, see <http://www.gnu.org/licenses/>.
 */

package de.saxsys.synchronizefx.core.metamodel;

import java.util.Date;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Checks if {@link TemporaryReferenceKeeper} works as expected.
 * 
 * @author Raik Bieniek
 */
@RunWith(MockitoJUnitRunner.class)
public class TemporaryReferenceKeeperTest {

    private static final Object SOME_OBJECT = "some object";
    private static final Object OTHER_SOME_OBJECT = "other some object";
    private static final Date SOME_TIME = new Date(100000);

    @Mock
    private Supplier<Date> currentTime;

    @InjectMocks
    private TemporaryReferenceKeeper cut;

    /**
     * As long as there is no time out for a kept reference, no reference is removed.
     */
    @Test
    public void keepsReferencesToInstancesAsLongAsThereIsNoTimeout() {
        when(currentTime.get()).thenReturn(SOME_TIME);
        cut.keepReferenceTo(SOME_OBJECT);

        when(currentTime.get()).thenReturn(new Date(SOME_TIME.getTime() + 2000));
        cut.keepReferenceTo(OTHER_SOME_OBJECT);

        when(currentTime.get()).thenReturn(new Date(SOME_TIME.getTime() + 50000));
        cut.cleanReferenceCache();

        assertThat(cut.getHardReferences()).containsOnly(SOME_OBJECT, OTHER_SOME_OBJECT);
    }

    /**
     * When the time references are held is over, references are removed.
     */
    @Test
    public void removesReferencesThatHaveTimedOut() {
        when(currentTime.get()).thenReturn(SOME_TIME);
        cut.keepReferenceTo(SOME_OBJECT);

        when(currentTime.get()).thenReturn(new Date(SOME_TIME.getTime() + 20000));
        cut.keepReferenceTo(OTHER_SOME_OBJECT);

        when(currentTime.get()).thenReturn(new Date(SOME_TIME.getTime() + 70000));
        cut.cleanReferenceCache();

        assertThat(cut.getHardReferences()).containsOnly(OTHER_SOME_OBJECT);
    }
}
