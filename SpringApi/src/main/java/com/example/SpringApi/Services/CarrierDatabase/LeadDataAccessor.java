package com.example.SpringApi.Services.CarrierDatabase;

import com.example.SpringApi.DatabaseModels.CarrierDatabase.Address;
import com.example.SpringApi.DatabaseModels.CarrierDatabase.Lead;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Repository.CarrierDatabase.AddressRepository;
import com.example.SpringApi.Repository.CarrierDatabase.LeadRepository;
import com.example.SpringApi.Repository.CentralDatabase.CarrierRepository;
import com.example.SpringApi.Repository.CentralDatabase.UserRepository;
import com.example.SpringApi.Services.BaseDataAccessor;
import com.example.SpringApi.Services.CentralDatabase.UserLogDataAccessor;
import com.example.SpringApi.SuccessMessages;
import com.mysql.cj.x.protobuf.Mysqlx;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.tuple.Pair;
import org.example.ApiRoutes;
import org.example.CommonHelpers.DataCleaner;
import org.example.CommonHelpers.HelperUtils;
import org.example.CommonHelpers.Validations;
import org.example.Models.RequestModels.ApiRequestModels.LeadRequestModel;
import org.example.Models.RequestModels.GridRequestModels.PaginationBaseRequestModel;
import org.example.Models.ResponseModels.ApiResponseModels.LeadResponseModel;
import org.example.Models.ResponseModels.ApiResponseModels.PaginationBaseResponseModel;
import org.example.Models.ResponseModels.Response;
import org.example.Translators.CarrierDatabaseTranslators.Interfaces.ILeadSubTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;

@Service
public class LeadDataAccessor extends BaseDataAccessor implements ILeadSubTranslator {

    private final UserRepository userRepository;
    private final LeadRepository leadRepository;
    private final AddressRepository addressRepository;
    private final UserLogDataAccessor userLogDataAccessor;

    @Autowired
    public LeadDataAccessor(HttpServletRequest request, CarrierRepository carrierRepository,
                            UserRepository userRepository,
                            LeadRepository leadRepository,
                            AddressRepository addressRepository,
                            UserLogDataAccessor userLogDataAccessor) {
        super(request, carrierRepository);
        this.userRepository = userRepository;
        this.leadRepository = leadRepository;
        this.addressRepository = addressRepository;
        this.userLogDataAccessor = userLogDataAccessor;
    }

    public Pair<String, Boolean> validateLead(Lead lead, Address address) {
        // Check mandatory leads fields
        if (lead.getEmail() == null || !StringUtils.hasText(lead.getEmail())) {
            return Pair.of(ErrorMessages.LeadsErrorMessages.ER001, false);
        }
        if (lead.getFirstName() == null || !StringUtils.hasText(lead.getFirstName())) {
            return Pair.of(ErrorMessages.LeadsErrorMessages.ER002, false);
        }
        if (lead.getLastName() == null || !StringUtils.hasText(lead.getLastName())) {
            return Pair.of(ErrorMessages.LeadsErrorMessages.ER003, false);
        }
        if (lead.getPhone() == null || !StringUtils.hasText(lead.getPhone()) || !Validations.isValidPhone(lead.getPhone())) {
            return Pair.of(ErrorMessages.LeadsErrorMessages.ER004, false);
        }

        if(lead.getLeadStatus() == null
                ||  !StringUtils.hasText(lead.getLeadStatus())
                || !HelperUtils.getLeadStatuses().contains(lead.getLeadStatus())){
            return Pair.of(ErrorMessages.LeadsErrorMessages.ER007 + String.join(", ", HelperUtils.getLeadStatuses()), false);
        }

        // validate optional fields - fax, website
        if (StringUtils.hasText(lead.getFax()) && !Validations.isValidPhone(lead.getFax())) {
            return Pair.of(ErrorMessages.InvalidPhone, false);
        }
        if (StringUtils.hasText(lead.getWebsite()) && !Validations.isValidUrl(lead.getWebsite())) {
            return Pair.of(ErrorMessages.LeadsErrorMessages.ER006, false);
        }
        if(lead.getAssignedAgentId() != null && userRepository.findById(lead.getAssignedAgentId()).isEmpty()) {
            return Pair.of(ErrorMessages.LeadsErrorMessages.ER005, false);
        }

        // check mandatory address fields
        Response<Boolean> addressValidationResponse = Validations.isValidAddress(address.getLine1(), address.getState(), address.getCity(), address.getZipCode(), address.getPhoneOnAddress(), address.getNameOnAddress());
        if(!addressValidationResponse.isSuccess()){
            return Pair.of(ErrorMessages.InvalidAddress, false);
        }

        return Pair.of("Success", true);
    }

    @Override
    public Response<PaginationBaseResponseModel<LeadResponseModel>> getLeadsInBatches(PaginationBaseRequestModel paginationBaseRequestModel) {
        // validate the column names
        if(StringUtils.hasText(paginationBaseRequestModel.getColumnName())) {
            Set<String> validColumns = new HashSet<>(Arrays.asList(
                    "firstName", "lastName", "email", "address", "website",
                    "phone", "companySize", "title", "leadAssignedTo",
                    "leadCreatedBy", "leadStatus"
            ));

            if (!validColumns.contains(paginationBaseRequestModel.getColumnName())) {
                return new Response<>(false,
                        ErrorMessages.InvalidColumn + String.join(",", validColumns),
                        null);
            }
        }

        Page<Object[]> leads = leadRepository.findPaginatedLeads(paginationBaseRequestModel.getColumnName(),
                paginationBaseRequestModel.getCondition(),
                paginationBaseRequestModel.getFilterExpr(),
                paginationBaseRequestModel.isIncludeDeleted(),
                PageRequest.of(paginationBaseRequestModel.getStart() / (paginationBaseRequestModel.getEnd() - paginationBaseRequestModel.getStart()),
                        paginationBaseRequestModel.getEnd() - paginationBaseRequestModel.getStart(),
                        Sort.by("leadId").descending()));

        List<LeadResponseModel> leadResponseModels = new ArrayList<>();
        for (Object[] result : leads.getContent()) {
            Lead lead = (Lead) result[0];
            LeadResponseModel leadResponseModel = new LeadResponseModel();
            leadResponseModel.setLead(HelperUtils.copyFields(lead, org.example.Models.CommunicationModels.CarrierModels.Lead.class));
            leadResponseModel.setAddress(HelperUtils.copyFields(result[1], org.example.Models.CommunicationModels.CarrierModels.Address.class));
            userRepository.findById(lead.getCreatedById()).ifPresent(user ->
                    leadResponseModel.setCreatedBy(HelperUtils.copyFields(user, org.example.Models.CommunicationModels.CentralModels.User.class))
            );
            userRepository.findById(lead.getAssignedAgentId()).ifPresent(user ->
                    leadResponseModel.setAssignedAgent(HelperUtils.copyFields(user, org.example.Models.CommunicationModels.CentralModels.User.class))
            );
            leadResponseModels.add(leadResponseModel);
        }

        PaginationBaseResponseModel<LeadResponseModel> paginationBaseResponseModel = new PaginationBaseResponseModel<>();
        paginationBaseResponseModel.setData(leadResponseModels);
        paginationBaseResponseModel.setTotalDataCount(leads.getTotalElements());

        return new Response<>(true, SuccessMessages.LeadSuccessMessages.GetLead, paginationBaseResponseModel);
    }

    @Override
    public Response<Long> createLead(LeadRequestModel leadRequestModel) {
        Lead lead = HelperUtils.copyFields(leadRequestModel.getLead(), Lead.class);
        Address address = HelperUtils.copyFields(leadRequestModel.getAddress(), Address.class);

        // add the missing fields in address
        address.setPhoneOnAddress(StringUtils.hasText(address.getPhoneOnAddress()) ? address.getPhoneOnAddress() : lead.getPhone());
        address.setNameOnAddress(StringUtils.hasText(address.getNameOnAddress()) ? address.getNameOnAddress()
                : lead.getFirstName() + " " + lead.getLastName());

        address.setPhoneOnAddress(DataCleaner.cleanPhone(address.getPhoneOnAddress()));
        lead.setPhone(DataCleaner.cleanPhone(lead.getPhone()));

        // validate the lead request model
        Pair<String, Boolean> validation = validateLead(lead, address);
        if(!validation.getValue()){
            return new Response<>(false, validation.getKey(), null);
        }

        //clean the data
        lead.setPhone(DataCleaner.cleanPhone(lead.getPhone()));
        lead.setFax(DataCleaner.cleanPhone(lead.getFax()));
        address.setPhoneOnAddress(DataCleaner.cleanPhone(address.getPhoneOnAddress()));

        Address leadAddress = addressRepository.save(address);
        lead.setAddressId(leadAddress.getAddressId());
        lead.setCreatedById(getUserId());

        Lead savedLead = leadRepository.save(lead);
        userLogDataAccessor.logData(getUserId(),
                SuccessMessages.LeadSuccessMessages.InsertLead + " " + savedLead.getLeadId(),
                ApiRoutes.LeadsSubRoute.CREATE_LEAD);
        return new Response<>(true, SuccessMessages.LeadSuccessMessages.InsertLead, savedLead.getLeadId());
    }

    @Override
    public Response<Long> updateLead(LeadRequestModel leadRequestModel) {
        Optional<Lead> existingLeadInDb = leadRepository.findById(leadRequestModel.getLead().getLeadId());
        if(existingLeadInDb.isEmpty()){
            return new Response<>(false, ErrorMessages.LeadsErrorMessages.InvalidId, null);
        }

        Lead lead = HelperUtils.copyFields(leadRequestModel.getLead(), Lead.class);
        Address address = HelperUtils.copyFields(leadRequestModel.getAddress(), Address.class);

        // add the missing fields in address
        address.setPhoneOnAddress(StringUtils.hasText(address.getPhoneOnAddress()) ? address.getPhoneOnAddress() : lead.getPhone());
        address.setNameOnAddress(StringUtils.hasText(address.getNameOnAddress()) ? address.getNameOnAddress()
                : lead.getFirstName() + " " + lead.getLastName());

        // validate the lead request model
        Pair<String, Boolean> validation = validateLead(lead, address);
        if(!validation.getValue()){
            return new Response<>(false, validation.getKey(), null);
        }

        // overwrite the current address
        Optional<Address> leadAddress = addressRepository.findById(existingLeadInDb.get().getAddressId());
        if(leadAddress.isPresent()){
            leadAddress.get().setLine1(leadRequestModel.getAddress().getLine1());
            leadAddress.get().setLine2(leadRequestModel.getAddress().getLine2());
            leadAddress.get().setState(leadRequestModel.getAddress().getState());
            leadAddress.get().setCity(leadRequestModel.getAddress().getCity());
            leadAddress.get().setZipCode(leadRequestModel.getAddress().getZipCode());
            leadAddress.get().setNameOnAddress(leadRequestModel.getAddress().getNameOnAddress());
            leadAddress.get().setPhoneOnAddress(leadRequestModel.getAddress().getPhoneOnAddress());

            addressRepository.save(leadAddress.get());
        }
        else {
            return new Response<>(false, ErrorMessages.AddressErrorMessages.InvalidId, null);
        }

        // set the fk's
        lead.setCreatedById(getUserId());
        lead.setAddressId(leadAddress.get().getAddressId());

        Lead savedLead = leadRepository.save(lead);
        userLogDataAccessor.logData(getUserId(),
                SuccessMessages.LeadSuccessMessages.UpdateLead + " " + savedLead.getLeadId(),
                ApiRoutes.LeadsSubRoute.UPDATE_LEAD);

        return new Response<>(true, SuccessMessages.LeadSuccessMessages.UpdateLead, savedLead.getLeadId());
    }

    @Override
    public Response<LeadResponseModel> getLeadDetailsById(long id) {
        Optional<Lead> lead = leadRepository.findById(id);
        if (lead.isPresent()) {
            Optional<Address> leadAddress = addressRepository.findById(lead.get().getAddressId());
            if(leadAddress.isPresent()){
                LeadResponseModel leadResponseModel = new LeadResponseModel();
                leadResponseModel.setLead(HelperUtils.copyFields(lead.get(), org.example.Models.CommunicationModels.CarrierModels.Lead.class));
                leadResponseModel.setAddress(HelperUtils.copyFields(leadAddress.get(), org.example.Models.CommunicationModels.CarrierModels.Address.class));
                userRepository.findById(lead.get().getCreatedById())
                        .ifPresent(user ->
                                leadResponseModel.setCreatedBy(HelperUtils.copyFields(user, org.example.Models.CommunicationModels.CentralModels.User.class))
                        );
                userRepository.findById(lead.get().getAssignedAgentId())
                        .ifPresent(user ->
                                leadResponseModel.setAssignedAgent(HelperUtils.copyFields(user, org.example.Models.CommunicationModels.CentralModels.User.class))
                        );
                return new Response<>(true, SuccessMessages.LeadSuccessMessages.GetLead, leadResponseModel);
            }
            else {
                return new Response<>(false, ErrorMessages.AddressErrorMessages.InvalidId, null);
            }
        } else {
            return new Response<>(false, ErrorMessages.LeadsErrorMessages.InvalidId, null);
        }
    }

    @Override
    public Response<Boolean> toggleLead(long id) {
        Optional<Lead> lead = leadRepository.findById(id);
        if(lead.isEmpty()){
            return new Response<>(false, ErrorMessages.LeadsErrorMessages.InvalidId, false);
        }

        lead.get().setDeleted(!lead.get().isDeleted());
        leadRepository.save(lead.get());
        userLogDataAccessor.logData(getUserId(),
                SuccessMessages.LeadSuccessMessages.ToggleLead + " " + id,
                ApiRoutes.LeadsSubRoute.TOGGLE_LEAD);
        return new Response<>(true, SuccessMessages.LeadSuccessMessages.ToggleLead, true);
    }

    @Override
    public Response<LeadResponseModel> getLeadDetailsByEmail(String email) {
        Lead lead = leadRepository.findByEmail(email);
        if (lead != null) {
            Optional<Address> leadAddress = addressRepository.findById(lead.getAddressId());
            if(leadAddress.isPresent()){
                LeadResponseModel leadResponseModel = new LeadResponseModel();
                leadResponseModel.setLead(HelperUtils.copyFields(lead, org.example.Models.CommunicationModels.CarrierModels.Lead.class));
                leadResponseModel.setAddress(HelperUtils.copyFields(leadAddress.get(), org.example.Models.CommunicationModels.CarrierModels.Address.class));
                return new Response<>(true, SuccessMessages.LeadSuccessMessages.GetLead, leadResponseModel);
            }
            else {
                return new Response<>(false, ErrorMessages.AddressErrorMessages.InvalidId, null);
            }
        } else {
            return new Response<>(false, ErrorMessages.LeadsErrorMessages.InvalidId, null);
        }
    }
}