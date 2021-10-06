package org.egov.service;

import org.egov.common.contract.AuditDetails;
import org.egov.common.contract.request.RequestHeader;
import org.egov.common.contract.request.UserInfo;
import org.egov.config.TestDataFormatter;
import org.egov.tracer.model.CustomException;
import org.egov.util.MasterDataServiceUtil;
import org.egov.validator.ChartOfAccountValidator;
import org.egov.web.models.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest()
class COAEnrichmentServiceTest {
    @Autowired
    private TestDataFormatter testDataFormatter;

    @InjectMocks
    private COAEnrichmentService cOAEnrichmentService;

    @Mock
    private ChartOfAccountValidator chartOfAccountValidator;

    @Mock
    private MasterDataServiceUtil masterDataServiceUtil;

    private COARequest coaRequest;
    private COASearchRequest coaSearchRequest;
    private COAResponse coaResponse;
    private COARequest headlessCoaRequest;
    private COAResponse coaCreateResponse;

    @BeforeAll
    public void init() throws IOException {
        coaRequest = testDataFormatter.getCoaRequestData();
        headlessCoaRequest = testDataFormatter.getHeadlessCoaRequestData();
        coaSearchRequest = testDataFormatter.getCoaSearchRequestData();

        coaCreateResponse = testDataFormatter.getCoaCreateResponseData();
        coaResponse = testDataFormatter.getCoaSearchResponseData();
    }

    @Test
    void testEnrichCreatePost() {
        when(this.masterDataServiceUtil.enrichAuditDetails((String) any(), (AuditDetails) any(), (Boolean) any()))
                .thenReturn(coaResponse.getChartOfAccounts().get(0).getAuditDetails());
        doNothing().when(this.chartOfAccountValidator).validateCoaCode((org.egov.web.models.COASearchCriteria) any());

        this.cOAEnrichmentService.enrichCreatePost(coaRequest);
        verify(this.masterDataServiceUtil).enrichAuditDetails((String) any(), (AuditDetails) any(), (Boolean) any());
        verify(this.chartOfAccountValidator).validateCoaCode((org.egov.web.models.COASearchCriteria) any());
    }

    @Test
    void testEnrichCreatePostWithExistingCOACode() {
        when(this.masterDataServiceUtil.enrichAuditDetails((String) any(), (AuditDetails) any(), (Boolean) any()))
                .thenReturn(coaResponse.getChartOfAccounts().get(0).getAuditDetails());
        doThrow(new CustomException()).when(this.chartOfAccountValidator).validateCoaCode((org.egov.web.models.COASearchCriteria) any());

        assertThrows(CustomException.class, () -> this.cOAEnrichmentService.enrichCreatePost(coaRequest));
    }

    @Test
    void testCreateCoaCode() {
        ChartOfAccount chartOfAccount = coaRequest.getChartOfAccount();
        this.cOAEnrichmentService.createCoaCode(chartOfAccount);
        assertEquals(coaCreateResponse.getChartOfAccounts().get(0).getCoaCode(), chartOfAccount.getCoaCode());
    }

    @Test
    void testEnrichSearchPost() {
        COASearchRequest coaSearchRequest = new COASearchRequest();
        this.cOAEnrichmentService.enrichSearchPost(coaSearchRequest);
        assertNull(coaSearchRequest.getCriteria());
        assertNull(coaSearchRequest.getRequestHeader());
    }
}
