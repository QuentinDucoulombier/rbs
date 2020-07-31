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
import fr.wseduc.webutils.Either.Right;
import net.atos.entng.rbs.model.ExportBooking;
import net.atos.entng.rbs.model.ExportRequest;
import net.atos.entng.rbs.models.Slots;
import org.entcore.common.service.impl.SqlCrudService;
import org.entcore.common.sql.Sql;
import org.entcore.common.sql.SqlResult;
import org.entcore.common.sql.SqlStatementsBuilder;
import org.entcore.common.user.UserInfos;
import org.entcore.common.utils.DateUtils;
import org.entcore.common.utils.StringUtils;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.text.ParseException;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.time.*;
import net.atos.entng.rbs.models.Booking;
import net.atos.entng.rbs.models.Slot;
import net.atos.entng.rbs.models.Slot.SlotIterable;
import net.atos.entng.rbs.models.Unavailability;
import static net.atos.entng.rbs.BookingStatus.*;
import static net.atos.entng.rbs.BookingUtils.*;
import static org.entcore.common.sql.Sql.parseId;
import static org.entcore.common.sql.SqlResult.*;


public class UnavailabilityServiceSqlImpl extends SqlCrudService implements UnavailabilityService {
	private static final Logger log = LoggerFactory.getLogger(BookingServiceSqlImpl.class);
	private static DateTimeFormatter sqlFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
			.withZone(ZoneOffset.UTC);

	public UnavailabilityServiceSqlImpl() {
		super("rbs", "unavailability");
	}

	static String toSQLTimestamp(Long timestamp) {
		return timestamp == null ? null : sqlFormatter.format(Instant.ofEpochSecond(timestamp));
	}

	@Override
	public void createUnavailability(final Unavailability unavailability, final UserInfos user, final Handler<Either<String, JsonObject>> handler) {
		String query = "INSERT INTO rbs.unavailability (resource_id, quantity, start_date, end_date, days) " +
				"VALUES (?, ?, ?, ?, B'" + unavailability.getSelectedDaysBitString() + "') RETURNING id";

		JsonArray values = new JsonArray()
				.add(parseId(unavailability.getUnavailabilityResourceId()))
				.add(unavailability.getUnavailabilityQuantity())
				.add(toSQLTimestamp(unavailability.getStartUTC()))
				.add(toSQLTimestamp(unavailability.getEndUTC()));

		Sql.getInstance().prepared(query, values, SqlResult.validUniqueResultHandler(handler));
	}

	@Override
	public void updateUnavailability(final Unavailability unavailability, final UserInfos user, final Handler<Either<String, JsonObject>> handler) {
		String query = "UPDATE rbs.unavailability SET quantity = ?, start_date = ?, end_date = ?, days = B'" +
				unavailability.getSelectedDaysBitString() + "' WHERE id = ? RETURNING id";

		JsonArray values = new JsonArray()
				.add(unavailability.getUnavailabilityQuantity())
				.add(toSQLTimestamp(unavailability.getStartUTC()))
				.add(toSQLTimestamp(unavailability.getEndUTC()))
				.add(parseId(unavailability.getUnavailabilityId()));

		Sql.getInstance().prepared(query, values, SqlResult.validUniqueResultHandler(handler));
	}

	@Override
	public void deleteUnavailability(final Integer unavailabilityId, final UserInfos user, final Handler<Either<String, JsonObject>> handler) {
		String query =  "DELETE FROM rbs.unavailability WHERE id = " + unavailabilityId;
		Sql.getInstance().raw(query, SqlResult.validUniqueResultHandler(handler));
	}

	@Override
	public void listResourceUnavailability(final Integer resourceId, final UserInfos user, final Handler<Either<String, JsonArray>> handler) {
		String query =  "SELECT * FROM rbs.unavailability WHERE resource_id = " + resourceId + " ORDER BY start_date ASC";
		Sql.getInstance().raw(query, SqlResult.validResultHandler(handler));
	}
}
