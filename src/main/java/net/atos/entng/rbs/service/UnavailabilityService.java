/*
 * Copyright © Région Nord Pas de Calais-Picardie,  Département 91, Région Aquitaine-Limousin-Poitou-Charentes, 2016.
 *
 * This file is part of OPEN ENT NG. OPEN ENT NG is a versatile ENT Project based on the JVM and ENT Core Project.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation (version 3 of the License).
 *
 * For the sake of explanation, any module that communicate over native
 * Web protocols, such as HTTP, with OPEN ENT NG is outside the scope of this
 * license and could be license under its own terms. This is merely considered
 * normal use of OPEN ENT NG, and does not fall under the heading of "covered work".
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package net.atos.entng.rbs.service;

import fr.wseduc.webutils.Either;
import org.entcore.common.service.CrudService;
import org.entcore.common.user.UserInfos;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import net.atos.entng.rbs.models.Unavailability;

import java.util.List;

public interface UnavailabilityService extends CrudService {

	/**
	 * Create an unavailability
	 *
	 * @param unavailability : current unavailability
	 * @param user     : information of current user logged
	 * @param handler  : handler which contains the response
	 */
	void createUnavailability(Unavailability unavailability, UserInfos user, Handler<Either<String, JsonObject>> handler);

	/**
	 * Update an unavailability
	 *
	 * @param unavailability : current unavailability
	 * @param user     : information of current user logged
	 * @param handler  : handler which contains the response
	 */
	void updateUnavailability(Unavailability unavailability, UserInfos user, Handler<Either<String, JsonObject>> handler);


	/**
	 * Delete an unavailability
	 *
	 * @param unavailabilityId : unavailability id
	 * @param user     : information of current user logged
	 * @param handler  : handler which contains the response
	 */
	void deleteUnavailability(Integer unavailabilityId, UserInfos user, Handler<Either<String, JsonObject>> handler);


	/**
	 * Delete an unavailability
	 *
	 * @param resourceId : resource id
	 * @param user     : information of current user logged
	 * @param handler  : handler which contains the response
	 */
	void listResourceUnavailability(Integer resourceId, UserInfos user, Handler<Either<String, JsonArray>> handler);
}
