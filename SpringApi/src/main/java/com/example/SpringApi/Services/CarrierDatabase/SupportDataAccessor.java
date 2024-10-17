package com.example.SpringApi.Services.CarrierDatabase;

import com.example.SpringApi.DatabaseModels.CarrierDatabase.Support;
import com.example.SpringApi.DatabaseModels.CarrierDatabase.SupportComments;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Repository.CarrierDatabase.SupportCommentsRepository;
import com.example.SpringApi.Repository.CarrierDatabase.SupportRepository;
import com.example.SpringApi.Repository.CentralDatabase.CarrierRepository;
import com.example.SpringApi.Repository.CentralDatabase.UserRepository;
import com.example.SpringApi.Services.BaseDataAccessor;
import com.example.SpringApi.Services.CentralDatabase.UserLogDataAccessor;
import com.example.SpringApi.SuccessMessages;
import com.nimbusds.jose.shaded.gson.Gson;
import com.nimbusds.jose.shaded.gson.GsonBuilder;
import jakarta.servlet.http.HttpServletRequest;
import org.example.Adapters.DateAdapter;
import org.example.Adapters.LocalDateTimeAdapter;
import org.example.ApiRoutes;
import org.example.CommonHelpers.HelperUtils;
import org.example.CommonHelpers.ImageHelper;
import org.example.CommonHelpers.JiraHelper;
import org.example.Models.CommunicationModels.CentralModels.User;
import org.example.Models.RequestModels.ApiRequestModels.SupportRequestModel;
import org.example.Models.ResponseModels.JiraResponseModels.*;
import org.example.Models.ResponseModels.Response;
import org.example.Translators.CarrierDatabaseTranslators.Interfaces.ISupportSubTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class SupportDataAccessor extends BaseDataAccessor implements ISupportSubTranslator {

    private final UserLogDataAccessor userLogDataAccessor;
    private final SupportRepository supportRepository;
    private final SupportCommentsRepository supportCommentsRepository;
    private final UserRepository userRepository;
    private final Gson gson;

    @Autowired
    public SupportDataAccessor(HttpServletRequest request,
                               CarrierRepository carrierRepository,
                               SupportRepository supportRepository,
                               SupportCommentsRepository supportCommentsRepository,
                               UserRepository userRepository,
                               UserLogDataAccessor userLogDataAccessor) {

        super(request, carrierRepository);
        this.userLogDataAccessor = userLogDataAccessor;
        this.supportRepository = supportRepository;
        this.supportCommentsRepository = supportCommentsRepository;
        this.userRepository = userRepository;
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(Date.class, new DateAdapter())
                .create();
    }

    private void syncJiraTicketsToDB(String ticketId) {
        Support support = supportRepository.findById(ticketId).orElse(new Support());
        support.setTicketId(ticketId);

        // Set the status to "Open" only if the support object is new (i.e., not already in the database)
        if (support.getCreatedAt() == null) { // Assuming `createdAt` is set only when the object is first saved
            support.setStatus("Open");
        }

        try {
            JiraHelper jiraHelper = new JiraHelper(
                    getCarrierDetails().getJiraProjectUrl(),
                    getCarrierDetails().getJiraUserName(),
                    getCarrierDetails().getJiraPassword(),
                    getCarrierDetails().getJiraProjectKey()
            );
            Response<GetTicketDetailsResponseModel> getTicketDetailsResponse = jiraHelper.getTicketDetails(ticketId);
            if (getTicketDetailsResponse.isSuccess()) {
                String jsonDetails = gson.toJson(getTicketDetailsResponse.getItem());
                support.setRawSupportDetails(jsonDetails);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        supportRepository.save(support);
    }


    private void syncJiraTicketCommentsToDB(String ticketId, Long userId) {
        JiraHelper jiraHelper = new JiraHelper(
                getCarrierDetails().getJiraProjectUrl(),
                getCarrierDetails().getJiraUserName(),
                getCarrierDetails().getJiraPassword(),
                getCarrierDetails().getJiraProjectKey()
        );
        Response<GetTicketDetailsResponseModel> getTicketDetailsResponse = jiraHelper.getTicketDetails(ticketId);
        if (getTicketDetailsResponse.isSuccess()) {
            List<SupportComments> supportComments = new ArrayList<>();
            for(Object comment : getTicketDetailsResponse.getItem().getFields().getComment().getComments()) {
                String jsonDetails = gson.toJson(comment);
                ResponseModelObjects.Comment deserializedComment = gson.fromJson(jsonDetails, ResponseModelObjects.Comment.class);

                String commentId = deserializedComment.getId() + "-" + ticketId;
                Optional<SupportComments> existingSupportComment = supportCommentsRepository.findById(commentId);
                if(existingSupportComment.isPresent()) {
                    existingSupportComment.get().setRawCommentADF(jsonDetails);
                    supportComments.add(existingSupportComment.get());
                }
                else{
                    SupportComments supportComment = new SupportComments();
                    supportComment.setCommentId(commentId);
                    supportComment.setTicketId(ticketId);
                    supportComment.setUserId(userId);
                    supportComment.setRawCommentADF(jsonDetails);
                    supportComments.add(supportComment);
                }
            }

            supportCommentsRepository.saveAll(supportComments);
        }
    }

    @Override
    public Response<GetTicketsResponseModel> getSupportTicketsInBatches(int start, int end) {
        JiraHelper jiraHelper = new JiraHelper(
                getCarrierDetails().getJiraProjectUrl(),
                getCarrierDetails().getJiraUserName(),
                getCarrierDetails().getJiraPassword(),
                getCarrierDetails().getJiraProjectKey()
        );

        return jiraHelper.getTickets(start, end);
    }

    @Override
    public Response<GetTicketDetailsResponseModel> getTicketDetailsById(String ticketId) {
        JiraHelper jiraHelper = new JiraHelper(
                getCarrierDetails().getJiraProjectUrl(),
                getCarrierDetails().getJiraUserName(),
                getCarrierDetails().getJiraPassword(),
                getCarrierDetails().getJiraProjectKey()
        );

        // fetch the data from jira
        Response<GetTicketDetailsResponseModel> getTicketDetailsResponse = jiraHelper.getTicketDetails(ticketId);
        if(!getTicketDetailsResponse.isSuccess()) {
            return new Response<>(false, getTicketDetailsResponse.getMessage(), null);
        }
        Response<GetCommentsResponseModel> getCommentsResponse = jiraHelper.getComments(ticketId);
        if(!getCommentsResponse.isSuccess()) {
            return new Response<>(false, getCommentsResponse.getMessage(), null);
        }

        // set the comments
        getTicketDetailsResponse
                .getItem()
                .getFields()
                .getComment()
                .setComments(new ArrayList<>(getCommentsResponse
                        .getItem()
                        .getComments()));

        syncJiraTicketCommentsToDB(ticketId, null);

        List<SupportComments> supportComments = supportCommentsRepository.findByTicketId(getTicketDetailsResponse.getItem().getId());
        if(supportComments != null && !supportComments.isEmpty()) {
            getTicketDetailsResponse.getItem().setSupportComments(HelperUtils.copyFields(supportComments, org.example.Models.CommunicationModels.CarrierModels.SupportComments.class));
            getTicketDetailsResponse.getItem().setUserIdFullNameMapping(supportComments.stream()
                    .filter(comment -> comment.getUserId() != null)
                    .collect(Collectors.toMap(
                            SupportComments::getUserId,
                            comment -> {
                                Optional<com.example.SpringApi.DatabaseModels.CentralDatabase.User> userOpt = userRepository.findById(comment.getUserId());
                                return userOpt.map(user -> user.getFirstName() + " " + user.getLastName()).orElse(null);
                            },
                            (existing, replacement) -> existing // In case of duplicate keys, keep the existing value
                    )));
        }

        return getTicketDetailsResponse;
    }

    @Override
    public Response<Map<String, String>> getAttachmentFromTicket(String ticketId) {
        Response<GetTicketDetailsResponseModel> getTicketsResponse = getTicketDetailsById(ticketId);
        if (!getTicketsResponse.isSuccess()) {
            return new Response<>(false, getTicketsResponse.getMessage(), null);
        }

        JiraHelper jiraHelper = new JiraHelper(
                getCarrierDetails().getJiraProjectUrl(),
                getCarrierDetails().getJiraUserName(),
                getCarrierDetails().getJiraPassword(),
                getCarrierDetails().getJiraProjectKey()
        );

        Map<String, String> attachmentIdAttachmentBase64Mapping = new HashMap<>();
        for (ResponseModelObjects.Attachment attachment : getTicketsResponse.getItem().getFields().getAttachment()) {
            Response<String> getAttachmentResponse = jiraHelper.getAttachment(attachment.getId());
            if (!getAttachmentResponse.isSuccess()) {
                return new Response<>(false, getAttachmentResponse.getMessage(), null);
            }
            attachmentIdAttachmentBase64Mapping.put(attachment.getId(), getAttachmentResponse.getItem());
        }

        return new Response<>(true, SuccessMessages.SupportSuccessMessages.GetAttachment, attachmentIdAttachmentBase64Mapping);
    }

    @Override
    public Response<GetCommentsResponseModel> getCommentsFromTicket(String ticketId) {
        JiraHelper jiraHelper = new JiraHelper(
                getCarrierDetails().getJiraProjectUrl(),
                getCarrierDetails().getJiraUserName(),
                getCarrierDetails().getJiraPassword(),
                getCarrierDetails().getJiraProjectKey()
        );

        return jiraHelper.getComments(ticketId);
    }

    @Override
    public Response<CreateTicketResponseModel> createTicket(SupportRequestModel supportRequestModel) {
        JiraHelper jiraHelper = new JiraHelper(
                getCarrierDetails().getJiraProjectUrl(),
                getCarrierDetails().getJiraUserName(),
                getCarrierDetails().getJiraPassword(),
                getCarrierDetails().getJiraProjectKey()
        );

        // clean the data
        Map<String, byte[]> images = new HashMap<>();
        for(Map.Entry<String, String> image: supportRequestModel.getImagesBase64().entrySet()) {
            images.put(image.getKey(), ImageHelper.getByteArrayFromBase64ImageString(image.getValue()));
        }

        // create ticket
        Response<CreateTicketResponseModel> createTicketResponse = jiraHelper.createTicket(supportRequestModel.getJsonContent());
        if (!createTicketResponse.isSuccess()) {
            return new Response<>(false, createTicketResponse.getMessage(), null);
        }

        // add attachment
        boolean attachmentUploaded = true;
        Response<List<CreateAttachmentResponseModel>> addAttachmentResponse = jiraHelper.addAttachment(createTicketResponse.getItem().getId(), images);
        if(!addAttachmentResponse.isSuccess()) {
            attachmentUploaded = false;
        }

        syncJiraTicketsToDB(createTicketResponse.getItem().getId());
        userLogDataAccessor.logData(getUserId(),
                createTicketResponse.getMessage() + " " + createTicketResponse.getItem().getId(),
                ApiRoutes.SupportSubRoute.CREATE_TICKET);
        return new Response<>(true, attachmentUploaded ? createTicketResponse.getMessage() : ErrorMessages.SupportErrorMessages.ER001, createTicketResponse.getItem());
    }

    @Override
    public Response<AddCommentResponseModel> addComment(String ticketId, SupportRequestModel supportRequestModel) {
        JiraHelper jiraHelper = new JiraHelper(
                getCarrierDetails().getJiraProjectUrl(),
                getCarrierDetails().getJiraUserName(),
                getCarrierDetails().getJiraPassword(),
                getCarrierDetails().getJiraProjectKey()
        );

        // first add the attachment
        Map<String, byte[]> images = new HashMap<>();
        for(Map.Entry<String, String> image: supportRequestModel.getImagesBase64().entrySet()) {
            images.put(image.getKey(), ImageHelper.getByteArrayFromBase64ImageString(image.getValue()));
        }
        Response<List<CreateAttachmentResponseModel>> createAttachmentResponse = jiraHelper.addAttachment(ticketId, images);
        if(!createAttachmentResponse.isSuccess()) {
            return new Response<>(false, createAttachmentResponse.getMessage(), null);
        }

        // edit the json content with the added attachment
        for(CreateAttachmentResponseModel createAttachmentResponseModel : createAttachmentResponse.getItem()) {
            // get the uuid for the attachment
            Response<String> getAttachmentResponse = jiraHelper.getAttachment(createAttachmentResponseModel.getId());
            if(!getAttachmentResponse.isSuccess()) {
                return new Response<>(false, getAttachmentResponse.getMessage(), null);
            }

            String regex = "/file/([a-f0-9\\-]+)/binary";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(getAttachmentResponse.getItem());

            String fileId = "";
            if (matcher.find()) {
                // Extract the UUID
                fileId = matcher.group(1);
            } else {
                return new Response<>(false, "Error uploading attachments with the comment.", null);
            }

            // Create the attachment node
            SupportRequestModel.JSONNode attachmentNode = new SupportRequestModel.JSONNode();
            attachmentNode.setType("mediaSingle");

            SupportRequestModel.JSONNode mediaNode = new SupportRequestModel.JSONNode();
            mediaNode.setType("media");


            Map<String, Object> attrs = new HashMap<>();
            attrs.put("type", "file");
            attrs.put("id",  fileId);
            attrs.put("collection",  "");
            mediaNode.setAttrs(attrs);

            List<SupportRequestModel.JSONNode> contentArray = new ArrayList<>();
            contentArray.add(mediaNode);

            attachmentNode.setContent(contentArray);

            // Add the attachment node to the document's content
            if (supportRequestModel.getJsonDocNode().getContent() == null) {
                supportRequestModel.getJsonDocNode().setContent(new ArrayList<>());
            }

            supportRequestModel.getJsonDocNode().getContent().add(attachmentNode);
        }

        // create the comment
        String jsonContentForComment = gson.toJson(supportRequestModel.getJsonDocNode());
        Response<AddCommentResponseModel> addCommentResponse = jiraHelper.addComment(ticketId, jsonContentForComment);
        if (!addCommentResponse.isSuccess()) {
            return new Response<>(false, addCommentResponse.getMessage(), null);
        }

        // add the comment to db
        SupportComments supportComment = new SupportComments();
        supportComment.setCommentId(addCommentResponse.getItem().getId() + "-" + ticketId);
        supportComment.setRawCommentADF(jsonContentForComment);
        supportComment.setTicketId(ticketId);
        supportComment.setUserId(getUserId());
        supportCommentsRepository.save(supportComment);

        userLogDataAccessor.logData(getUserId(),
                addCommentResponse.getMessage() + " " + addCommentResponse.getItem().getId(),
                ApiRoutes.SupportSubRoute.ADD_COMMENT);

        syncJiraTicketsToDB(ticketId);
        return addCommentResponse;
    }

    @Override
    public Response<Boolean> editTicket(String ticketId, SupportRequestModel supportRequestModel) {
        JiraHelper jiraHelper = new JiraHelper(
                getCarrierDetails().getJiraProjectUrl(),
                getCarrierDetails().getJiraUserName(),
                getCarrierDetails().getJiraPassword(),
                getCarrierDetails().getJiraProjectKey()
        );

        // clean the data
        Map<String, byte[]> images = new HashMap<>();
        for(Map.Entry<String, String> image: supportRequestModel.getImagesBase64().entrySet()) {
            images.put(image.getKey(), ImageHelper.getByteArrayFromBase64ImageString(image.getValue()));
        }

        Response<Boolean> editTicketResponse = jiraHelper.editTicket(ticketId, supportRequestModel.getJsonContent());
        if (!editTicketResponse.isSuccess()) {
            return new Response<>(false, editTicketResponse.getMessage(), null);
        }

        // add attachment
        boolean attachmentUploaded = true;
        Response<List<CreateAttachmentResponseModel>> addAttachmentResponse = jiraHelper.addAttachment(ticketId, images);
        if(!addAttachmentResponse.isSuccess()) {
            attachmentUploaded = false;
        }

        // delete the ticket in the db
        supportRepository.deleteById(ticketId);

        // sync the ticket contents in the db
        syncJiraTicketsToDB(ticketId);
        return editTicketResponse;
    }

    @Override
    public Response<Boolean> deleteTicket(String ticketId) {
        JiraHelper jiraHelper = new JiraHelper(
                getCarrierDetails().getJiraProjectUrl(),
                getCarrierDetails().getJiraUserName(),
                getCarrierDetails().getJiraPassword(),
                getCarrierDetails().getJiraProjectKey()
        );

        Response<Boolean> deleteTicketResponse = jiraHelper.deleteTicket(ticketId);
        if (!deleteTicketResponse.isSuccess()) {
            return new Response<>(false, deleteTicketResponse.getMessage(), null);
        }

        supportRepository.deleteById(ticketId);
        return deleteTicketResponse;
    }

    @Override
    public Response<Boolean> deleteComment(String ticketId, String commentId) {
        JiraHelper jiraHelper = new JiraHelper(
                getCarrierDetails().getJiraProjectUrl(),
                getCarrierDetails().getJiraUserName(),
                getCarrierDetails().getJiraPassword(),
                getCarrierDetails().getJiraProjectKey()
        );

        // delete ticket from jira
        Response<Boolean> deleteCommentResponse = jiraHelper.deleteComment(ticketId, commentId);
        if (!deleteCommentResponse.isSuccess()) {
            return new Response<>(false, deleteCommentResponse.getMessage(), null);
        }

        // delete ticket from system
        supportCommentsRepository.deleteById(commentId + "-" + ticketId);
        return deleteCommentResponse;
    }

    @Override
    public Response<Boolean> editComment(String ticketId, String commentId, SupportRequestModel supportRequestModel) {
        JiraHelper jiraHelper = new JiraHelper(
                getCarrierDetails().getJiraProjectUrl(),
                getCarrierDetails().getJiraUserName(),
                getCarrierDetails().getJiraPassword(),
                getCarrierDetails().getJiraProjectKey()
        );

        // first add the attachment
        Map<String, byte[]> images = new HashMap<>();
        for(Map.Entry<String, String> image: supportRequestModel.getImagesBase64().entrySet()) {
            images.put(image.getKey(), ImageHelper.getByteArrayFromBase64ImageString(image.getValue()));
        }
        Response<List<CreateAttachmentResponseModel>> createAttachmentResponse = jiraHelper.addAttachment(ticketId, images);
        if(!createAttachmentResponse.isSuccess()) {
            return new Response<>(false, createAttachmentResponse.getMessage(), null);
        }

        // edit the json content with the added attachment
        for(CreateAttachmentResponseModel createAttachmentResponseModel : createAttachmentResponse.getItem()) {
            // get the uuid for the attachment
            Response<String> getAttachmentResponse = jiraHelper.getAttachment(createAttachmentResponseModel.getId());
            if(!getAttachmentResponse.isSuccess()) {
                return new Response<>(false, getAttachmentResponse.getMessage(), null);
            }

            String regex = "/file/([a-f0-9\\-]+)/binary";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(getAttachmentResponse.getItem());

            String fileId = "";
            if (matcher.find()) {
                // Extract the UUID
                fileId = matcher.group(1);
            } else {
                return new Response<>(false, "Error uploading attachments with the comment.", null);
            }

            // Create the attachment node
            SupportRequestModel.JSONNode attachmentNode = new SupportRequestModel.JSONNode();
            attachmentNode.setType("mediaSingle");

            SupportRequestModel.JSONNode mediaNode = new SupportRequestModel.JSONNode();
            mediaNode.setType("media");


            Map<String, Object> attrs = new HashMap<>();
            attrs.put("type", "file");
            attrs.put("id",  fileId);
            attrs.put("collection",  "");
            mediaNode.setAttrs(attrs);

            List<SupportRequestModel.JSONNode> contentArray = new ArrayList<>();
            contentArray.add(mediaNode);

            attachmentNode.setContent(contentArray);

            // Add the attachment node to the document's content
            if (supportRequestModel.getJsonDocNode().getContent() == null) {
                supportRequestModel.getJsonDocNode().setContent(new ArrayList<>());
            }

            supportRequestModel.getJsonDocNode().getContent().add(attachmentNode);
        }

        // create the comment
        String jsonContentForComment = gson.toJson(supportRequestModel.getJsonDocNode());

        // edit the commment on jira
        Response<Boolean> editCommentResponse = jiraHelper.editComment(ticketId, commentId, jsonContentForComment);
        if (!editCommentResponse.isSuccess()) {
            return new Response<>(false, editCommentResponse.getMessage(), null);
        }

        // delete ticket from system
        supportCommentsRepository.deleteById(commentId + "-" + ticketId);

        syncJiraTicketCommentsToDB(ticketId, getUserId());
        return editCommentResponse;
    }
}