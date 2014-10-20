/**
 * This file is part of SynchronizeFX.
 * 
 * Copyright (C) 2013-2014 Saxonia Systems AG
 *
 * SynchronizeFX is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SynchronizeFX is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SynchronizeFX. If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Contains the classes that evaluate incoming change commands to synchronize the local models with
 * remote models.
 * 
 *  <p>The executors in this package are execute changes in a way that all models are eventually synchronous.
 *  That means that these classes repair non-synchronous states on their managed properties on the own.
 *  Non-synchronous states can happen when multiple peers change the same property at the same time.
 *  Non-synchronous states can be detected because the server does not only send change events of other peers
 *  but also change events of the own peer in the order that they where executed on the server.</p>
 */
package de.saxsys.synchronizefx.core.metamodel.executors;