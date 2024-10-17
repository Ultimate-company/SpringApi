package com.example.SpringApi.Services.CarrierDatabase;

import com.example.SpringApi.DatabaseModels.CarrierDatabase.MessageUserGroupMap;
import com.example.SpringApi.DatabaseModels.CarrierDatabase.MessageUserReadMap;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Repository.CarrierDatabase.*;
import com.example.SpringApi.Repository.CentralDatabase.CarrierRepository;
import com.example.SpringApi.Repository.CentralDatabase.UserRepository;
import com.example.SpringApi.Services.BaseDataAccessor;
import com.example.SpringApi.Services.CentralDatabase.UserLogDataAccessor;
import com.example.SpringApi.SuccessMessages;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.tuple.Pair;
import org.example.ApiRoutes;
import org.example.CommonHelpers.EmailHelper;
import org.example.CommonHelpers.HTMLHelper;
import com.example.SpringApi.DatabaseModels.CarrierDatabase.Message;
import com.example.SpringApi.DatabaseModels.CarrierDatabase.MessageUserMap;
import com.example.SpringApi.DatabaseModels.CentralDatabase.Carrier;
import com.example.SpringApi.DatabaseModels.CentralDatabase.User;
import org.example.CommonHelpers.HelperUtils;
import org.example.Models.RequestModels.ApiRequestModels.MessageRequestModel;
import org.example.Models.RequestModels.GridRequestModels.PaginationBaseRequestModel;
import org.example.Models.RequestModels.SendEmailRequest;
import org.example.Models.ResponseModels.ApiResponseModels.MessageResponseModel;
import org.example.Models.ResponseModels.ApiResponseModels.PaginationBaseResponseModel;
import org.example.Models.ResponseModels.Response;
import org.example.Translators.CarrierDatabaseTranslators.Interfaces.IMessageSubTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

import static org.example.CommonHelpers.DateHelper.isDateLessThanCurrentUTC;

@Service
public class MessageDataAccessor extends BaseDataAccessor implements IMessageSubTranslator {

    private final MessageRepository messageRepository;
    private final MessageUserMapRepository messageUserMapRepository;
    private final MessageUserGroupMapRepository messageUserGroupMapRepository;
    private final UserRepository userRepository;
    private final MessageUserReadMapRepository messageUserReadMapRepository;
    private final UserGroupRepository userGroupRepository;
    private final UserLogDataAccessor userLogDataAccessor;
    private EmailHelper emailHelper;
    @Autowired
    public MessageDataAccessor(HttpServletRequest request,
                               CarrierRepository carrierRepository,
                               MessageRepository messageRepository,
                               MessageUserMapRepository messageUserMapRepository,
                               MessageUserGroupMapRepository messageUserGroupMapRepository,
                               UserRepository userRepository,
                               MessageUserReadMapRepository messageUserReadMapRepository,
                               UserGroupRepository userGroupRepository,
                               UserLogDataAccessor userLogDataAccessor) {
        super(request, carrierRepository);
        this.messageRepository = messageRepository;
        this.messageUserMapRepository = messageUserMapRepository;
        this.messageUserGroupMapRepository = messageUserGroupMapRepository;
        this.userRepository = userRepository;
        this.messageUserReadMapRepository = messageUserReadMapRepository;
        this.userGroupRepository = userGroupRepository;
        this.userLogDataAccessor = userLogDataAccessor;
    }

    private Response<String> scheduleBatch(Message message){
        // get all the user ids associated with the message
        List<Long> userIds = messageUserMapRepository.findByMessageId(message.getMessageId())
                .stream()
                .map(MessageUserMap::getUserId)
                .toList();

        // get all the user emails
        List<String> emails = userRepository.findAllById(userIds)
                .stream()
                .map(User::getLoginName)
                .toList();

        Carrier carrier = getCarrierDetails();
        this.emailHelper = new EmailHelper(carrier.getSendgridEmailAddress(), carrier.getSendgridSenderName(), carrier.getSendgridApikey());
        Response<String> batchIdResponse = emailHelper.generateBatchId();
        if(!batchIdResponse.isSuccess()){
            return new Response<>(false, batchIdResponse.getMessage(), null);
        }

        SendEmailRequest sendEmailRequest = new SendEmailRequest();
        sendEmailRequest.setToAddress(emails);
        sendEmailRequest.setSubject(message.getTitle());
        sendEmailRequest.setPlainTextContent(HTMLHelper.stripHtml(message.getDescriptionHtml()));
        sendEmailRequest.setSendAt(message.getPublishDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        sendEmailRequest.setBatchId(batchIdResponse.getItem());

        Response<Boolean> emailResponse = this.emailHelper.sendEmail(sendEmailRequest);
        if(!emailResponse.isSuccess()){
            return new Response<>(false, emailResponse.getMessage(), null);
        }
        message.setSendgridEmailBatchId(batchIdResponse.getMessage());

        return new Response<>(true, "success", batchIdResponse.getMessage());
    }

    public Pair<String, Boolean> validateMessage(Message message, List<Long> userIds) {
        /*
        * Required fields -> title, publish date, description, descriptionHtml, descriptionMarkdown
        * */
        if(!StringUtils.hasText(message.getTitle())) {
            return Pair.of(ErrorMessages.MessagesErrorMessages.ER003, false);
        }
        if(message.getPublishDate() == null || isDateLessThanCurrentUTC(message.getPublishDate())) {
            return Pair.of(ErrorMessages.MessagesErrorMessages.ER005, false);
        }
        if(!StringUtils.hasText(message.getDescriptionHtml())) {
            return Pair.of(ErrorMessages.MessagesErrorMessages.ER007, false);
        }

        /*
        * Atleast one user id is required.
        * */
        if(userIds == null || userIds.isEmpty()){
            return Pair.of(ErrorMessages.MessagesErrorMessages.ER008, false);
        }

        return Pair.of("Success", true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response<Long> createMessage(MessageRequestModel messageRequestModel) {
        Message message = HelperUtils.copyFields(messageRequestModel.getMessage(), Message.class);

        // validate the message request model
        Pair<String, Boolean> validation = validateMessage(message, messageRequestModel.getUserIds());
        if(!validation.getValue()){
            return new Response<>(false, validation.getKey(), null);
        }

        if(messageRequestModel.getMessage().isSendAsEmail()){
            Response<String> scheduleBatchResponse = scheduleBatch(HelperUtils.copyFields(messageRequestModel.getMessage(), Message.class));
            if(!scheduleBatchResponse.isSuccess()){
                return new Response<>(false, scheduleBatchResponse.getMessage(), null);
            }
            messageRequestModel.getMessage().setSendgridEmailBatchId(scheduleBatchResponse.getMessage());
        }

        // save the message
        message.setCreatedByUserId(getUserId());
        Message savedMessage = messageRepository.save(message);

        // save the message and user id mapping
        if(messageRequestModel.getUserIds() != null && !messageRequestModel.getUserIds().isEmpty())
        {
            List<MessageUserMap> messagesUserMaps = new ArrayList<>();
            for(long userId : messageRequestModel.getUserIds()){
                MessageUserMap messagesUserMap = new MessageUserMap();
                messagesUserMap.setMessageId(savedMessage.getMessageId());
                messagesUserMap.setUserId(userId);
                messagesUserMaps.add(messagesUserMap);
            }
            messageUserMapRepository.saveAll(messagesUserMaps);
        }

        // save the message and user group id mapping
        if(messageRequestModel.getUserGroupIds() != null && !messageRequestModel.getUserGroupIds().isEmpty())
        {
            List<MessageUserGroupMap> messageUserGroupMaps = new ArrayList<>();
            for(long userGroupId : messageRequestModel.getUserGroupIds()){
                MessageUserGroupMap messageUserGroupMap = new MessageUserGroupMap();
                messageUserGroupMap.setMessageId(savedMessage.getMessageId());
                messageUserGroupMap.setUserGroupId(userGroupId);
                messageUserGroupMaps.add(messageUserGroupMap);
            }
            messageUserGroupMapRepository.saveAll(messageUserGroupMaps);
        }

        userLogDataAccessor.logData(getUserId(),
                SuccessMessages.MessagesSuccessMessages.InsertMessage + " " + savedMessage.getMessageId(),
                ApiRoutes.MessagesSubRoute.CREATE_MESSAGE);

        return new Response<>(true, SuccessMessages.MessagesSuccessMessages.InsertMessage, savedMessage.getMessageId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response<Long> updateMessage(MessageRequestModel messageRequestModel) {
        Message message = HelperUtils.copyFields(messageRequestModel.getMessage(), Message.class);

        // validate the message request model
        Pair<String, Boolean> validation = validateMessage(message, messageRequestModel.getUserIds());
        if(!validation.getValue()){
            return new Response<>(false, validation.getKey(), null);
        }

        Optional<Message> existingMessage = messageRepository.findById(message.getMessageId());
        if(existingMessage.isEmpty()){
            return new Response<>(false, ErrorMessages.MessagesErrorMessages.InvalidId, null);
        }

        // Check if the existing message was to be sent as an email
        if (existingMessage.get().isSendAsEmail()) {
            LocalDateTime addTimes = LocalDateTime.now().plusMinutes(11);

            // Check if the publishing date is within the cancellation window
            Instant publishInstant = existingMessage.get().getPublishDate().toInstant();

            // Convert LocalDateTime to ZonedDateTime
            ZoneId zoneId = ZoneId.systemDefault(); // replace with the appropriate time zone if necessary
            ZonedDateTime zdt = addTimes.atZone(zoneId);

            // Convert ZonedDateTime to Instant
            Instant addTimesInstant = zdt.toInstant();
            if (publishInstant.isBefore(addTimesInstant)) {
                return new Response<>(false, ErrorMessages.MessagesErrorMessages.ER001, null);
            }

            // Cancel the email
            Response<Boolean> cancelEmailResponse = this.emailHelper.cancelEmail(existingMessage.get().getSendgridEmailBatchId());

            // Check if the email was cancelled successfully
            if (!cancelEmailResponse.isSuccess()) {
                return new Response<>(false, ErrorMessages.MessagesErrorMessages.ER002, null);
            }
        }

        if(messageRequestModel.getMessage().isSendAsEmail()){
            Response<String> scheduleBatchResponse = scheduleBatch(HelperUtils.copyFields(messageRequestModel.getMessage(), Message.class));
            if(!scheduleBatchResponse.isSuccess()){
                return new Response<>(false, scheduleBatchResponse.getMessage(), null);
            }
            messageRequestModel.getMessage().setSendgridEmailBatchId(scheduleBatchResponse.getMessage());
        }

        // save the message details
        existingMessage.get().setTitle(message.getTitle());
        existingMessage.get().setDescriptionHtml(message.getDescriptionHtml());
        existingMessage.get().setDescription(message.getDescription());
        existingMessage.get().setDescriptionMarkDown(message.getDescriptionMarkDown());
        existingMessage.get().setPublishDate(message.getPublishDate());
        existingMessage.get().setSendAsEmail(message.isSendAsEmail());
        existingMessage.get().setUpdated(true);
        Message savedMessage = messageRepository.save(existingMessage.get());

        // remove the old message userids mapping
        List<MessageUserMap> existingMessageUserMap = messageUserMapRepository.findByMessageId(savedMessage.getMessageId());
        messageUserMapRepository.deleteAll(existingMessageUserMap);

        // remove the old message usergroupIds mapping
        List<MessageUserGroupMap> existingMessageUserGroupMap = messageUserGroupMapRepository.findByMessageId(savedMessage.getMessageId());
        messageUserGroupMapRepository.deleteAll(existingMessageUserGroupMap);

        // save the message and user id mapping
        if(messageRequestModel.getUserIds() != null && !messageRequestModel.getUserIds().isEmpty())
        {
            List<MessageUserMap> messagesUserMaps = new ArrayList<>();
            for(long userId : messageRequestModel.getUserIds()){
                MessageUserMap messagesUserMap = new MessageUserMap();
                messagesUserMap.setMessageId(savedMessage.getMessageId());
                messagesUserMap.setUserId(userId);
                messagesUserMaps.add(messagesUserMap);
            }
            messageUserMapRepository.saveAll(messagesUserMaps);
        }

        // save the message and user group id mapping
        if(messageRequestModel.getUserGroupIds() != null && !messageRequestModel.getUserGroupIds().isEmpty())
        {
            List<MessageUserGroupMap> messageUserGroupMaps = new ArrayList<>();
            for(long userGroupId : messageRequestModel.getUserGroupIds()){
                MessageUserGroupMap messageUserGroupMap = new MessageUserGroupMap();
                messageUserGroupMap.setMessageId(savedMessage.getMessageId());
                messageUserGroupMap.setUserGroupId(userGroupId);
                messageUserGroupMaps.add(messageUserGroupMap);
            }
            messageUserGroupMapRepository.saveAll(messageUserGroupMaps);
        }

        userLogDataAccessor.logData(getUserId(),
                SuccessMessages.MessagesSuccessMessages.UpdateMessage + " " + savedMessage.getMessageId(),
                ApiRoutes.MessagesSubRoute.UPDATE_MESSAGE);
        return new Response<>(true, SuccessMessages.MessagesSuccessMessages.UpdateMessage, savedMessage.getMessageId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response<Boolean> toggleMessage(long messageId) {
        Optional<Message> message = messageRepository.findById(messageId);
        if(message.isEmpty()){
            return new Response<>(false, ErrorMessages.MessagesErrorMessages.InvalidId, false);
        }

        message.get().setDeleted(!message.get().isDeleted());
        messageRepository.save(message.get());
        userLogDataAccessor.logData(getUserId(),
                SuccessMessages.MessagesSuccessMessages.ToggleMessage + " " + messageId,
                ApiRoutes.MessagesSubRoute.TOGGLE_MESSAGE);
        return new Response<>(true, SuccessMessages.MessagesSuccessMessages.ToggleMessage, true);
    }

    @Override
    public Response<PaginationBaseResponseModel<MessageResponseModel>> getMessagesInBatches(PaginationBaseRequestModel paginationBaseRequestModel) {
        // validate the column names
        if(StringUtils.hasText(paginationBaseRequestModel.getColumnName())){
            Set<String> validColumns = new HashSet<>(Arrays.asList("title", "publishDate"));

            if(!validColumns.contains(paginationBaseRequestModel.getColumnName())){
                return new Response<>(false,
                        ErrorMessages.InvalidColumn + String.join(",", validColumns),
                        null);
            }
        }

        Page<Object[]> messages = messageRepository.findPaginatedMessages(paginationBaseRequestModel.getColumnName(),
                paginationBaseRequestModel.getCondition(),
                paginationBaseRequestModel.getFilterExpr(),
                paginationBaseRequestModel.isIncludeDeleted(),
                PageRequest.of(paginationBaseRequestModel.getStart() / (paginationBaseRequestModel.getEnd() - paginationBaseRequestModel.getStart()),
                        paginationBaseRequestModel.getEnd() - paginationBaseRequestModel.getStart(),
                        Sort.by("messageId").descending()));

        List<MessageResponseModel> messageResponseModels = new ArrayList<>();
        for(Object[] result : messages.getContent()){
            MessageResponseModel messageResponseModel = new MessageResponseModel();
            messageResponseModel.setMessage(HelperUtils.copyFields(result[0], org.example.Models.CommunicationModels.CarrierModels.Message.class));
            messageResponseModel.setTotalUsers((int)(long)result[1]);
            messageResponseModel.setTotalUserGroups((int)(long)result[2]);
            messageResponseModels.add(messageResponseModel);
        }

        PaginationBaseResponseModel<MessageResponseModel> paginationBaseResponseModel = new PaginationBaseResponseModel<>();
        paginationBaseResponseModel.setData(messageResponseModels);
        paginationBaseResponseModel.setTotalDataCount(messages.getTotalElements());

        return new Response<>(true, SuccessMessages.MessagesSuccessMessages.GetMessages, paginationBaseResponseModel);
    }

    @Override
    public Response<MessageResponseModel> getMessageDetailsById(long messageId) {
        MessageResponseModel messageResponseModel = new MessageResponseModel();
        Optional<Message> message = messageRepository.findById(messageId);
        if(message.isPresent()) {
            Response<List<Long>> getUsersInMessagesResponse = getUsersInMessages(messageId);
            if(getUsersInMessagesResponse.isSuccess()){
                Response<List<Long>> getUserGroupsInMessagesResponse = getUserGroupsInMessage(messageId);
                if(getUserGroupsInMessagesResponse.isSuccess()) {
                    messageResponseModel.setMessage(HelperUtils.copyFields(message.get(), org.example.Models.CommunicationModels.CarrierModels.Message.class));
                    messageResponseModel.setUserIds(new HashSet<>(getUsersInMessagesResponse.getItem()));
                    messageResponseModel.setUserGroupIds(new HashSet<>(getUserGroupsInMessagesResponse.getItem()));
                    Optional<User> userInDb = userRepository.findById(message.get().getCreatedByUserId());
                    if(userInDb.isPresent()) {
                        messageResponseModel.setUser(HelperUtils.copyFields(userInDb.get(), org.example.Models.CommunicationModels.CentralModels.User.class));
                        return new Response<>(true, SuccessMessages.MessagesSuccessMessages.GetMessages, messageResponseModel);
                    }
                    else{
                        return new Response<>(false, ErrorMessages.UserErrorMessages.InvalidId, null);
                    }
                }
                else {
                    return new Response<>(false, getUserGroupsInMessagesResponse.getMessage(), null);
                }
            }
            else {
                return new Response<>(false, getUsersInMessagesResponse.getMessage(), null);
            }
        }
        else {
            return new Response<>(false, ErrorMessages.MessagesErrorMessages.InvalidId, null);
        }
    }

    @Override
    public Response<List<Long>> getUsersInMessages(long messageId) {
        Optional<Message> message = messageRepository.findById(messageId);
        if(message.isEmpty()){
            return new Response<>(false, ErrorMessages.MessagesErrorMessages.InvalidId, null);
        }

        List<Long> userIds = messageUserMapRepository.findByMessageId(message.get().getMessageId())
                .stream()
                .map(MessageUserMap::getUserId)
                .toList();

        return new Response<>(true, SuccessMessages.MessagesSuccessMessages.GotMessageDetails, userIds);
    }

    @Override
    public Response<List<Long>> getUserGroupsInMessage(long messageId) {
        Optional<Message> message = messageRepository.findById(messageId);
        if(message.isEmpty()){
            return new Response<>(false, ErrorMessages.MessagesErrorMessages.InvalidId, null);
        }

        List<Long> userGroupIds = messageUserGroupMapRepository.findByMessageId(message.get().getMessageId())
                .stream()
                .map(MessageUserGroupMap::getUserGroupId)
                .toList();

        return new Response<>(true, SuccessMessages.MessagesSuccessMessages.GotMessageDetails, userGroupIds);
    }

    @Override
    public Response<Boolean> setMessageReadByUserIdAndMessageId(long userId, long messageId) {
        Optional<User> user = userRepository.findById(userId);
        if(user.isEmpty()) {
            return new Response<>(false, ErrorMessages.UserErrorMessages.InvalidId, false);
        }

        Optional<Message> message = messageRepository.findById(messageId);
        if(message.isEmpty()){
            return new Response<>(false, ErrorMessages.MessagesErrorMessages.InvalidId, false);
        }

        MessageUserReadMap existingMessageUserReadMap = messageUserReadMapRepository.findMessageUserReadMapByUserIdAndMessageId(userId, messageId);
        if(existingMessageUserReadMap == null) {
            MessageUserReadMap messageUserReadMap = new MessageUserReadMap();
            messageUserReadMap.setUserId(userId);
            messageUserReadMap.setMessageId(messageId);
            messageUserReadMapRepository.save(messageUserReadMap);
        }

        return new Response<>(true, SuccessMessages.Success, true);
    }

    @Override
    public Response<List<MessageResponseModel>> getMessagesByUserId(long userId) {
        List<MessageResponseModel> result = new ArrayList<>();
        Set<Long> messageIds = new HashSet<>();

        Optional<User> user = userRepository.findById(userId);
        if(user.isEmpty()) {
            return new Response<>(false, ErrorMessages.UserErrorMessages.InvalidId, null);
        }

        // Get current date without time
        Calendar currentDateCal = Calendar.getInstance();
        currentDateCal.set(Calendar.HOUR_OF_DAY, 0);
        currentDateCal.set(Calendar.MINUTE, 0);
        currentDateCal.set(Calendar.SECOND, 0);
        currentDateCal.set(Calendar.MILLISECOND, 0);
        Date currentDate = currentDateCal.getTime();

        // get messages assigned to the user
        List<MessageUserMap> messagesUserMaps = messageUserMapRepository.findByUserId(userId);
        if(messagesUserMaps != null && !messagesUserMaps.isEmpty()) {
            messageIds.addAll(messagesUserMaps.stream()
                    .map(MessageUserMap::getMessageId)
                    .collect(Collectors.toSet()));

            List<Message> messages = messageRepository.findAllById(messageIds);
            for(Message message : messages) {
                if(message.isDeleted()) continue;

                // Get message publish date without time
                Calendar publishDateCal = Calendar.getInstance();
                publishDateCal.setTime(message.getPublishDate());
                publishDateCal.set(Calendar.HOUR_OF_DAY, 0);
                publishDateCal.set(Calendar.MINUTE, 0);
                publishDateCal.set(Calendar.SECOND, 0);
                publishDateCal.set(Calendar.MILLISECOND, 0);
                Date publishDate = publishDateCal.getTime();

                // Check if publish date is after current date
                if (publishDate.after(currentDate)) {
                    continue;
                }

                MessageResponseModel messageResponseModel = new MessageResponseModel();
                messageResponseModel.setMessage(HelperUtils.copyFields(message, org.example.Models.CommunicationModels.CarrierModels.Message.class));

                // check if the current user has read the given message
                MessageUserReadMap messageUserReadMap = messageUserReadMapRepository.findMessageUserReadMapByUserIdAndMessageId(userId, message.getMessageId());
                if(messageUserReadMap != null) {
                    messageResponseModel.setRead(true);
                }
                Optional<User> userInDb = userRepository.findById(message.getCreatedByUserId());
                if(userInDb.isPresent()) {
                    messageResponseModel.setUser(HelperUtils.copyFields(userInDb.get(), org.example.Models.CommunicationModels.CentralModels.User.class));
                    result.add(messageResponseModel);
                }
                else {
                    return new Response<>(false, ErrorMessages.UserErrorMessages.InvalidId, null);
                }
            }
        }

        // get user groups user is a part of
        List<Long> userGroupIds = userGroupRepository.getUserGroupIdsFromUserId(userId);

        // get messages assigned to all the user groups
        List<MessageUserGroupMap> messageUserGroupMaps = messageUserGroupMapRepository.findByUserGroupIds(userGroupIds);
        if(messageUserGroupMaps != null && !messageUserGroupMaps.isEmpty()) {
            messageIds.addAll(messageUserGroupMaps.stream()
                    .map(MessageUserGroupMap::getMessageId)
                    .collect(Collectors.toSet()));

            List<Message> messages = messageRepository.findAllById(messageIds);
            for(Message message : messages) {
                if (message.isDeleted()) continue;
                if (result.stream().anyMatch(x -> Objects.equals(x.getMessage().getMessageId(), message.getMessageId()))) continue;

                // Get message publish date without time
                Calendar publishDateCal = Calendar.getInstance();
                publishDateCal.setTime(message.getPublishDate());
                publishDateCal.set(Calendar.HOUR_OF_DAY, 0);
                publishDateCal.set(Calendar.MINUTE, 0);
                publishDateCal.set(Calendar.SECOND, 0);
                publishDateCal.set(Calendar.MILLISECOND, 0);
                Date publishDate = publishDateCal.getTime();

                // Check if publish date is after current date
                if (publishDate.after(currentDate)) {
                    continue;
                }

                MessageResponseModel messageResponseModel = new MessageResponseModel();
                messageResponseModel.setMessage(HelperUtils.copyFields(message, org.example.Models.CommunicationModels.CarrierModels.Message.class));

                MessageUserReadMap messageUserReadMap = messageUserReadMapRepository.findMessageUserReadMapByUserIdAndMessageId(userId, message.getMessageId());
                if(messageUserReadMap != null) {
                    messageResponseModel.setRead(true);
                }
                Optional<User> userInDb = userRepository.findById(message.getCreatedByUserId());
                if(userInDb.isPresent()) {
                    messageResponseModel.setUser(HelperUtils.copyFields(userInDb.get(), org.example.Models.CommunicationModels.CentralModels.User.class));
                    result.add(messageResponseModel);
                }
                else {
                    return new Response<>(false, ErrorMessages.UserErrorMessages.InvalidId, null);
                }
            }
        }

        result.sort(Comparator.comparingLong((MessageResponseModel m) -> m.getMessage().getMessageId()).reversed());
        return new Response<>(true, SuccessMessages.MessagesSuccessMessages.GetMessages, result);
    }
}