package com.example.SpringApi.Services.CarrierDatabase;

import com.example.SpringApi.DatabaseModels.CarrierDatabase.Package;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Repository.CarrierDatabase.*;
import com.example.SpringApi.Repository.CentralDatabase.CarrierRepository;
import com.example.SpringApi.Services.BaseDataAccessor;
import com.example.SpringApi.Services.CentralDatabase.UserLogDataAccessor;
import com.example.SpringApi.SuccessMessages;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.tuple.Pair;
import org.example.ApiRoutes;
import org.example.CommonHelpers.HelperUtils;
import org.example.Models.RequestModels.GridRequestModels.PaginationBaseRequestModel;
import org.example.Models.ResponseModels.ApiResponseModels.PaginationBaseResponseModel;
import org.example.Models.ResponseModels.Response;
import org.example.Translators.CarrierDatabaseTranslators.Interfaces.IPackageSubTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;

@Service
public class PackageDataAccessor extends BaseDataAccessor implements IPackageSubTranslator {
    private final PackageRepository packageRepository;
    private final UserLogDataAccessor userLogDataAccessor;


    @Autowired
    public PackageDataAccessor(HttpServletRequest request,
                               CarrierRepository carrierRepository,
                               PackageRepository packageRepository,
                               UserLogDataAccessor userLogDataAccessor) {
        super(request, carrierRepository);
        this.packageRepository = packageRepository;
        this.userLogDataAccessor = userLogDataAccessor;
    }

    public Pair<String, Boolean> validatePackage(org.example.Models.CommunicationModels.CarrierModels.Package _package) {
        /*
         * Required fields -> linches, binches, hinches, quantity,
         * */
        if(_package.getLength() <= 0 || _package.getHeight() <= 0 || _package.getBreadth() <= 0) {
            return Pair.of(ErrorMessages.PackageErrorMessages.ER001, false);
        }

        if(_package.getQuantity() <= 0) {
            return Pair.of(ErrorMessages.PackageErrorMessages.ER002, false);
        }

        return Pair.of("Success", true);
    }

    @Override
    public Response<Long> createPackage(org.example.Models.CommunicationModels.CarrierModels.Package _package) {
        Pair<String, Boolean> validation = validatePackage(_package);
        if(!validation.getValue()){
            return new Response<>(false, validation.getKey(), null);
        }

        List<Package> existingPackage = packageRepository.findByDimensions(_package.getLength(), _package.getBreadth(), _package.getHeight());
        if(existingPackage != null && !existingPackage.isEmpty()) {
            return new Response<>(false, ErrorMessages.PackageErrorMessages.ER003, null);
        }

        Package newPackage = packageRepository.save(HelperUtils.copyFields(_package, Package.class));
        userLogDataAccessor.logData(getUserId(),
                SuccessMessages.PackagesSuccessMessages.InsertPackage + " " + _package.getPackageId(),
                ApiRoutes.PackageSubRoute.CREATE_PACKAGE);

        return new Response<>(true, SuccessMessages.PackagesSuccessMessages.InsertPackage, newPackage.getPackageId());
    }

    @Override
    public Response<Long> updatePackage(org.example.Models.CommunicationModels.CarrierModels.Package _package) {
        Pair<String, Boolean> validation = validatePackage(_package);
        if(!validation.getValue()){
            return new Response<>(false, validation.getKey(), null);
        }

        Optional<Package> existingPackage = packageRepository.findById(_package.getPackageId());
        if(existingPackage.isEmpty()) {
            return new Response<>(false, ErrorMessages.PackageErrorMessages.InvalidId, null);
        }

        packageRepository.save(HelperUtils.copyFields(_package, Package.class));
        userLogDataAccessor.logData(getUserId(),
                SuccessMessages.PackagesSuccessMessages.UpdatePackage + " " + _package.getPackageId(),
                ApiRoutes.PackageSubRoute.UPDATE_PACKAGE);

        return new Response<>(true, SuccessMessages.PackagesSuccessMessages.UpdatePackage, _package.getPackageId());
    }

    @Override
    public Response<Boolean> togglePackage(long packageId) {
        Optional<Package> existingPackage = packageRepository.findById(packageId);
        if(existingPackage.isEmpty()) {
            return new Response<>(false, ErrorMessages.PackageErrorMessages.InvalidId, false);
        }

        existingPackage.get().setDeleted(!existingPackage.get().isDeleted());
        packageRepository.save(existingPackage.get());
        userLogDataAccessor.logData(getUserId(),
                SuccessMessages.PackagesSuccessMessages.TogglePackage + " " + packageId,
                ApiRoutes.PackageSubRoute.TOGGLE_PACKAGE);

        return new Response<>(true, SuccessMessages.PackagesSuccessMessages.TogglePackage, true);
    }

    @Override
    public Response<PaginationBaseResponseModel<org.example.Models.CommunicationModels.CarrierModels.Package>> getPackagesInBatches(PaginationBaseRequestModel paginationBaseRequestModel) {
        // validate the column names
        if(StringUtils.hasText(paginationBaseRequestModel.getColumnName())){
            Set<String> validColumns = new HashSet<>(Arrays.asList("dimensions", "quantity"));

            if(!validColumns.contains(paginationBaseRequestModel.getColumnName())){
                return new Response<>(false,
                        ErrorMessages.InvalidColumn + String.join(",", validColumns),
                        null);
            }
        }

        Page<Object[]> packages = packageRepository.findPaginatedPackages(paginationBaseRequestModel.getColumnName(),
                paginationBaseRequestModel.getCondition(),
                paginationBaseRequestModel.getFilterExpr(),
                paginationBaseRequestModel.isIncludeDeleted(),
                PageRequest.of(paginationBaseRequestModel.getStart() / paginationBaseRequestModel.getEnd() - paginationBaseRequestModel.getStart(),
                        paginationBaseRequestModel.getEnd() - paginationBaseRequestModel.getStart(),
                        Sort.by("packageId").ascending()));

        PaginationBaseResponseModel<org.example.Models.CommunicationModels.CarrierModels.Package> paginationBaseResponseModel = new PaginationBaseResponseModel<>();
        paginationBaseResponseModel.setData(HelperUtils.copyFields(packages.getContent(), org.example.Models.CommunicationModels.CarrierModels.Package.class));
        paginationBaseResponseModel.setTotalDataCount(packages.getTotalElements());

        return new Response<>(true, SuccessMessages.PackagesSuccessMessages.GetPackage, paginationBaseResponseModel);
    }

    @Override
    public Response<org.example.Models.CommunicationModels.CarrierModels.Package> getPackageById(long packageId) {
        Optional<Package> existingPackage = packageRepository.findById(packageId);
        return existingPackage.map(aPackage -> new Response<>(true, SuccessMessages.PackagesSuccessMessages.GetPackage,
                HelperUtils.copyFields(aPackage, org.example.Models.CommunicationModels.CarrierModels.Package.class))).orElseGet(() -> new Response<>(false, ErrorMessages.PackageErrorMessages.InvalidId, null));
    }

    @Override
    public Response<List<org.example.Models.CommunicationModels.CarrierModels.Package>> getAllPackagesInSystem() {
        return new Response<>(true,
                SuccessMessages.PackagesSuccessMessages.GetPackage,
                HelperUtils.copyFields(packageRepository.findByDeleted(false), org.example.Models.CommunicationModels.CarrierModels.Package.class));
    }
}
