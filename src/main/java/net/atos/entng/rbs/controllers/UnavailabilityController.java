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

package net.atos.entng.rbs.controllers;

import static org.entcore.common.http.response.DefaultResponseHandler.arrayResponseHandler;
import org.entcore.common.controller.ControllerHelper;
import org.entcore.common.http.filter.ResourceFilter;
import org.entcore.common.user.UserInfos;
import org.entcore.common.user.UserUtils;

import fr.wseduc.rs.ApiDoc;
import fr.wseduc.rs.Delete;
import fr.wseduc.rs.Get;
import fr.wseduc.rs.Post;
import fr.wseduc.security.ActionType;
import fr.wseduc.security.SecuredAction;
import fr.wseduc.webutils.Either;
import fr.wseduc.webutils.http.Renders;
import fr.wseduc.webutils.request.RequestUtils;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.core.eventbus.EventBus;

import net.atos.entng.rbs.filters.TypeAndResourceAppendPolicy;
import net.atos.entng.rbs.service.*;
import net.atos.entng.rbs.models.Unavailability;
import net.atos.entng.rbs.service.ResourceService;
import net.atos.entng.rbs.service.ResourceServiceSqlImpl;

public class UnavailabilityController extends ControllerHelper {

	private final UnavailabilityService unavailabilityService;
	private final ResourceService resourceService;

	public UnavailabilityController(EventBus eb) {
		unavailabilityService = new UnavailabilityServiceSqlImpl();
		resourceService = new ResourceServiceSqlImpl();
	}

//	@Override
//	public void init(Vertx vertx, JsonObject config, RouteMatcher rm, Map<String, fr.wseduc.webutils.security.SecuredAction> securedActions) {
//		super.init(vertx, config, rm, securedActions);
//	}

	@Post("/resource/:id/unavailability") // Parameter "id" is the resourceId
	@ApiDoc("Create unavailability for a given resource")
	@SecuredAction(value = "rbs.manager", type = ActionType.RESOURCE)
	@ResourceFilter(TypeAndResourceAppendPolicy.class)
	public void createUnavailability(final HttpServerRequest request) {
		UserUtils.getUserInfos(eb, request, user -> {
			if (user != null) {
				RequestUtils.bodyToJson(request, pathPrefix + "createUnavailability",
						getUnavailabilityHandler(user, request, true));
			} else {
				log.debug("User not found in session.");
				Renders.unauthorized(request);
			}
		});
	}

	@Post("/resource/:id/unavailability/:unavailabilityId") // Parameter "id" is the resourceId
	@ApiDoc("Update unavailability for a given resource")
	@SecuredAction(value = "rbs.manager", type = ActionType.RESOURCE)
	@ResourceFilter(TypeAndResourceAppendPolicy.class)
	public void updateUnavailability(final HttpServerRequest request) {
		UserUtils.getUserInfos(eb, request, user -> {
			if (user != null) {
				RequestUtils.bodyToJson(request, pathPrefix + "updateUnavailability",
						getUnavailabilityHandler(user, request, false));
			} else {
				log.debug("User not found in session.");
				Renders.unauthorized(request);
			}
		});
	}

	@Delete("/resource/:id/unavailability/:unavailabilityId")
	@ApiDoc("Delete resource")
	@SecuredAction(value = "rbs.manager", type = ActionType.RESOURCE)
	@ResourceFilter(TypeAndResourceAppendPolicy.class)
	public void deleteUnavailability(final HttpServerRequest request) {
		UserUtils.getUserInfos(eb, request, user -> {
			if (user != null) {
				Integer unavailabilityId = Integer.parseInt(request.params().get("unavailabilityId"));

				unavailabilityService.deleteUnavailability(unavailabilityId, user, event -> {
					if (event.isRight()) {
							renderJson(request, event.right().getValue(), 200);
					}
					else {
						badRequest(request, event.left().getValue());
					}
				});

			} else {
				log.debug("User not found in session.");
				Renders.unauthorized(request);
			}
		});
	}

	@Get("/resource/:id/unavailability") // Parameter "id" is the resourceId
	@ApiDoc("List all unavailability of a resource")
	public void listResourceUnavailability(final HttpServerRequest request) {
		UserUtils.getUserInfos(eb, request, user -> {
			if (user != null) {
				Integer resourceId = Integer.parseInt(request.params().get("id"));
				unavailabilityService.listResourceUnavailability(resourceId, user, arrayResponseHandler(request));
			} else {
				log.debug("User not found in session.");
				unauthorized(request);
			}
		});
	}

	private Handler<JsonObject> getUnavailabilityHandler(final UserInfos user, final HttpServerRequest request, final boolean isCreation) {
		return json -> {
			String resourceId = request.params().get("id");
			String unavailabilityId = request.params().get("unavailabilityId");
			json.put("resource_id", resourceId);

			Unavailability unavailability = new Unavailability(json, unavailabilityId);

			// Store boolean array (selected days) as a bit string
			try {
				unavailability.computeSelectedDaysAsBitString();
			} catch (Exception e) {
				log.error("Error during processing of array 'days'", e);
				renderError(request);
				return;
			}

			Handler<Either<String, JsonObject>> handler = new Handler<Either<String, JsonObject>>() {
				@Override
				public void handle(Either<String, JsonObject> event) {
					if (event.isRight()) {
						if (event.right().getValue() != null && event.right().getValue().size() > 0) {
							renderJson(request, event.right().getValue(), 200);
						}
						else {
//								String errorMessage = isCreation ? "rbs.booking.create.conflict" : "rbs.booking.update.conflict";
							JsonObject error = new JsonObject().put("error", "error creation"); // TODO
							renderJson(request, error, 409);
						}
					}
					else {
						badRequest(request, event.left().getValue());
					}
				}
			};

			if (isCreation) {
				unavailabilityService.createUnavailability(unavailability, user, handler);
			} else {
				unavailabilityService.updateUnavailability(unavailability, user, handler);
			}
		};
	}
}
