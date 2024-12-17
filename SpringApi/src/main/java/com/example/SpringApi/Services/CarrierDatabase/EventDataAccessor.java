package com.example.SpringApi.Services.CarrierDatabase;

import com.example.SpringApi.DatabaseModels.CarrierDatabase.Event;
import com.example.SpringApi.DatabaseModels.CarrierDatabase.EventUserMapping;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Repository.CarrierDatabase.EventRepository;
import com.example.SpringApi.Repository.CarrierDatabase.EventUserMappingRepository;
import com.example.SpringApi.Repository.CentralDatabase.CarrierRepository;
import com.example.SpringApi.Services.BaseDataAccessor;
import com.example.SpringApi.Services.CentralDatabase.UserLogDataAccessor;
import com.example.SpringApi.SuccessMessages;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.tuple.Pair;
import org.example.ApiRoutes;
import org.example.CommonHelpers.HelperUtils;
import org.example.Models.RequestModels.ApiRequestModels.EventRequestModel;
import org.example.Models.ResponseModels.ApiResponseModels.EventResponseModel;
import org.example.Models.ResponseModels.Response;
import org.example.Translators.CarrierDatabaseTranslators.Interfaces.IEventSubTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class EventDataAccessor extends BaseDataAccessor implements IEventSubTranslator {

    private final EventRepository eventRepository;
    private final EventUserMappingRepository eventUserMappingRepository;
    private final UserLogDataAccessor userLogDataAccessor;

    @Autowired
    public EventDataAccessor(HttpServletRequest request,
                             CarrierRepository carrierRepository,
                             EventRepository eventRepository,
                             EventUserMappingRepository eventUserMappingRepository,
                             UserLogDataAccessor userLogDataAccessor) {
        super(request, carrierRepository);
        this.eventRepository = eventRepository;
        this.eventUserMappingRepository = eventUserMappingRepository;
        this.userLogDataAccessor = userLogDataAccessor;
    }

    public Pair<String, Boolean> validateEvent(org.example.Models.CommunicationModels.CarrierModels.Event event) {
        if (event == null) {
            return Pair.of(ErrorMessages.EventErrorMessages.ER001, false);
        }

        /*
        * Required fields:
        * eventName, descriptionhtml, eventstartdatetime, eventenddatetime, timezone, locatin
        * */

        if (event.getEventName() == null || event.getEventName().isEmpty()) {
            return Pair.of(ErrorMessages.EventErrorMessages.ER002, false);
        }
        if (event.getDescriptionHtml() == null || event.getDescriptionHtml().isEmpty()) {
            return Pair.of(ErrorMessages.EventErrorMessages.ER003, false);
        }
        if (event.getStartDateTime() == null || event.getEndDateTime() == null) {
            return Pair.of(ErrorMessages.EventErrorMessages.ER003, false);
        }
        if (event.getStartDateTime().isAfter(event.getEndDateTime())) {
            return Pair.of(ErrorMessages.EventErrorMessages.ER003, false);
        }
        if (event.getTimeZone() == null
                || event.getTimeZone().isEmpty()
                || HelperUtils.getTimeZones().containsKey(event.getTimeZone())) {
            return Pair.of(ErrorMessages.EventErrorMessages.ER004, false);
        }
        if (event.getLocation() == null || event.getLocation().isEmpty()) {
            return Pair.of(ErrorMessages.EventErrorMessages.ER005, false);
        }

        return Pair.of("Success", true);
    }

    @Override
    public Response<List<EventResponseModel>> getAllEventsForUserIdBasedOnMonth(long userId, int month) {
        // fetch all event ids for the given userid
        Map<Long, Boolean> eventRsvpMappings = eventUserMappingRepository.findEventUserMappingByUserId(userId)
                .stream()
                .collect(Collectors.toMap(
                        EventUserMapping::getEventId,
                        EventUserMapping::getRsvp,
                        (existing, replacement) -> existing,
                        HashMap::new
                ));

        // filter only the non deleted events and for the given month
        List<Event> events = eventRepository
                .findEventsByIdsAndMonth(new ArrayList<>(eventRsvpMappings.keySet()), month);

        List<EventResponseModel> eventResponseModels = new ArrayList<>();
        for(Event event : events) {
            List<EventUserMapping> eventUserMappings = eventUserMappingRepository.findEventUserMappingByEventId(event.getEventId());
            eventResponseModels.add(new EventResponseModel()
                    .setEvent(HelperUtils.copyFields(event, org.example.Models.CommunicationModels.CarrierModels.Event.class))
                    .setAttendees(eventUserMappings.stream().map(EventUserMapping::getUserId).collect(Collectors.toList()))
                    .setUserIdRsvpMapping(eventUserMappings.stream().collect(Collectors.toMap(
                            EventUserMapping::getUserId,
                            EventUserMapping::getRsvp,
                            (existing, replacement) -> existing,
                            HashMap::new
                    ))));
        }

        return new Response<>(true, SuccessMessages.EventSuccessMessages.GetEvents, eventResponseModels);
    }

    @Override
    public Response<Long> createEvent(EventRequestModel eventRequestModel) {
        // validate the lead request model
        Pair<String, Boolean> validation = validateEvent(eventRequestModel.getEvent());
        if(!validation.getValue()){
            return new Response<>(false, validation.getKey(), null);
        }

        // Create Event object and populate it with data from the request model
        Event event = new Event()
                .setEventName(eventRequestModel.getEvent().getEventName())
                .setDescriptionHtml(eventRequestModel.getEvent().getDescriptionHtml())
                .setEventType(eventRequestModel.getEvent().getEventType())
                .setPriorityStatus(eventRequestModel.getEvent().getPriorityStatus())
                .setStartDateTime(eventRequestModel.getEvent().getStartDateTime())
                .setEndDateTime(eventRequestModel.getEvent().getEndDateTime())
                .setTimeZone(eventRequestModel.getEvent().getTimeZone())
                .setLocation(eventRequestModel.getEvent().getLocation())
                .setColor(eventRequestModel.getEvent().getColor())
                .setColorLabel(eventRequestModel.getEvent().getColorLabel())
                .setDeleted(false)
                .setCreatedByUserId(getUserId());

        eventRepository.save(event);

        // save the mappings
        List<Long> userIds = eventRequestModel.getAttendees();
        List<EventUserMapping> eventUserMappings = new ArrayList<>();
        if (userIds != null && !userIds.isEmpty()) {
            for (Long userId : userIds) {
                if(Objects.equals(userId, getUserId())) continue;
                eventUserMappings.add(new EventUserMapping()
                        .setEventId(event.getEventId())
                        .setUserId(userId)
                        .setRsvp(false));
            }
        }

        // the person creating the event will rsvp true to it by default
        eventUserMappings.add(new EventUserMapping()
                .setEventId(event.getEventId())
                .setUserId(getUserId())
                .setRsvp(true));

        eventUserMappingRepository.saveAll(eventUserMappings);

        userLogDataAccessor.logData(getUserId(),
                SuccessMessages.EventSuccessMessages.InsertEvent + " " + event.getEventId(),
                ApiRoutes.EventSubRoute.CREATE_EVENT);
        return new Response<>(true, SuccessMessages.EventSuccessMessages.InsertEvent, event.getEventId());
    }

    @Override
    public Response<Boolean> updateEvent(EventRequestModel eventRequestModel) {
        Pair<String, Boolean> validation = validateEvent(eventRequestModel.getEvent());
        if(!validation.getValue()){
            return new Response<>(false, validation.getKey(), null);
        }

        Optional<Event> event = eventRepository.findById(eventRequestModel.getEvent().getEventId());
        if(event.isEmpty()) {
            return new Response<>(false, ErrorMessages.EventErrorMessages.InvalidId, false);
        }

         event.get()
                 .setEventName(eventRequestModel.getEvent().getEventName())
                 .setDescriptionHtml(eventRequestModel.getEvent().getDescriptionHtml())
                 .setEventType(eventRequestModel.getEvent().getEventType())
                 .setPriorityStatus(eventRequestModel.getEvent().getPriorityStatus())
                 .setStartDateTime(eventRequestModel.getEvent().getStartDateTime())
                 .setEndDateTime(eventRequestModel.getEvent().getEndDateTime())
                 .setTimeZone(eventRequestModel.getEvent().getTimeZone())
                 .setLocation(eventRequestModel.getEvent().getLocation())
                 .setColor(eventRequestModel.getEvent().getColor())
                 .setColorLabel(eventRequestModel.getEvent().getColorLabel())
                 .setDeleted(eventRequestModel.getEvent().isDeleted());

        // remove existing mappings
        List<EventUserMapping> existingEventUserMappings = eventUserMappingRepository.findEventUserMappingByEventId(event.get().getEventId());
        eventUserMappingRepository.deleteAll(existingEventUserMappings);

        // add new mappings
        List<EventUserMapping> eventUserMappings = new ArrayList<>();
        for(long userId : eventRequestModel.getAttendees()) {
            eventUserMappings.add(new EventUserMapping()
                    .setUserId(userId)
                    .setEventId(eventRequestModel.getEvent().getEventId())
                    .setRsvp(eventRequestModel.getRsvp()));
        }
        eventUserMappingRepository.saveAll(eventUserMappings);

        userLogDataAccessor.logData(getUserId(),
                SuccessMessages.EventSuccessMessages.UpdateEvent + " " + event.get().getEventId(),
                ApiRoutes.EventSubRoute.UPDATE_EVENT);
        return new Response<>(true, SuccessMessages.EventSuccessMessages.UpdateEvent, true);
    }

    @Override
    public Response<Boolean> toggleEvent(long eventId) {
        Optional<Event> event = eventRepository.findById(eventId);
        if(event.isEmpty()){
            return new Response<>(false, ErrorMessages.EventErrorMessages.InvalidId, false);
        }

        event.get().setDeleted(!event.get().isDeleted());
        eventRepository.save(event.get());

        userLogDataAccessor.logData(getUserId(),
                SuccessMessages.EventSuccessMessages.ToggleEvent + " " + eventId,
                ApiRoutes.EventSubRoute.TOGGLE_EVENT);
        return new Response<>(true, SuccessMessages.EventSuccessMessages.ToggleEvent, true);
    }

    @Override
    public Response<EventResponseModel> getEventDetailsById(long eventId) {
        Optional<Event> event = eventRepository.findById(eventId);
        if(event.isEmpty()){
            return new Response<>(false, ErrorMessages.EventErrorMessages.InvalidId, null);
        }

        // fetch the mappings
        List<EventUserMapping> eventUserMappings = eventUserMappingRepository.findEventUserMappingByEventId(eventId);
        EventResponseModel eventResponseModel = new EventResponseModel()
                .setEvent(HelperUtils.copyFields(event.get(), org.example.Models.CommunicationModels.CarrierModels.Event.class))
                .setUserIdRsvpMapping(eventUserMappings.stream().collect(Collectors.toMap(
                                EventUserMapping::getUserId,
                                EventUserMapping::getRsvp,
                                (existing, replacement) -> existing,
                                HashMap::new
                        )))
                .setAttendees(eventUserMappings.stream().map(EventUserMapping::getUserId).toList());

        return new Response<>(true, SuccessMessages.PromoSuccessMessages.GetPromo, eventResponseModel);
    }
}