package net.atos.entng.rbs.models;

import static net.atos.entng.rbs.BookingUtils.getLocalAdminScope;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.time.ZonedDateTime;
import java.util.Iterator;

import org.entcore.common.user.UserInfos;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class Unavailability {
	private final String unavailabilityId;
	private final JsonObject json;
	private String selectedDaysBitString;
	private final ZonedDateTime start;
	private final ZonedDateTime end;
//	private Resource resource;

	public Unavailability(JsonObject json){
		this(json,json.getLong("start_date", 0l), json.getLong("end_date", 0l), json.getString("iana"), null);
	}

	public Unavailability(JsonObject json, final String id) {
		this(json,json.getLong("start_date", 0l), json.getLong("end_date", 0l), json.getString("iana"), id);
	}

	public Unavailability(JsonObject json, Long start,Long end, String iana, final String id){
		super();
		this.json = json;
		this.unavailabilityId = id;
		this.start = BookingDateUtils.localDateTimeForTimestampSecondsAndIana(start, iana );
		this.end = BookingDateUtils.localDateTimeForTimestampSecondsAndIana(end, iana );
	}



	public String getUnavailabilityId() {
		return unavailabilityId;
	}

	public long getStartUTC() {
		return this.start.toEpochSecond();
	}

	public long getEndUTC() {
		return this.end.toEpochSecond();
	}

	public String getUnavailabilityResourceId() {
		return json.getString("resource_id");
	}

	public Integer getUnavailabilityQuantity() {
		return json.getInteger("quantity");
	}

	public Object getRawStartDate() {
		return this.json.getValue("start_date");
	}

	public Object getRawEndDate() {
		return this.json.getValue("end_date");
	}

	public Optional<JsonArray> getDays() {
		return Optional.ofNullable(json.getJsonArray("days", null));
	}

	public String getSelectedDaysBitString() {
		if (selectedDaysBitString == null) {
			computeSelectedDaysAsBitString();
		}
		return selectedDaysBitString;
	}

	public void computeSelectedDaysAsBitString() {
		JsonArray selectedDaysArray = getDays().get();
		StringBuilder selectedDays = new StringBuilder();
		for (Object day : selectedDaysArray) {
			int isSelectedDay = ((Boolean) day) ? 1 : 0;
			selectedDays.append(isSelectedDay);
		}
		selectedDaysBitString = selectedDays.toString();
	}

//	public void setResource(Resource resource) {
//		this.resource = resource;
//	}
}
