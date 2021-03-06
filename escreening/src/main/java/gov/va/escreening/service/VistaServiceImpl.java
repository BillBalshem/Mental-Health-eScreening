package gov.va.escreening.service;

import com.google.common.collect.*;
import gov.va.escreening.domain.VeteranDto;
import gov.va.escreening.entity.*;
import gov.va.escreening.exception.VistaVerificationAlreadyMappedException;
import gov.va.escreening.repository.*;
import gov.va.escreening.vista.VistaLinkClientException;
import gov.va.escreening.vista.VistaRpcParam;
import gov.va.escreening.vista.VistaServiceContext;
import gov.va.escreening.vista.dto.DialogComponent;
import gov.va.escreening.vista.dto.DialogPrompt;
import gov.va.escreening.vista.dto.OrwpceSaveComment;
import gov.va.escreening.vista.dto.OrwpceSaveHeader;
import gov.va.escreening.vista.dto.OrwpceSaveHealthFactor;
import gov.va.escreening.vista.dto.OrwpceSaveVisit;
import gov.va.escreening.vista.dto.ProgressNoteParameters;
import gov.va.escreening.vista.dto.VisitTypeEnum;
import gov.va.escreening.vista.dto.VistaBrokerUserInfo;
import gov.va.escreening.vista.dto.VistaClinicAppointment;
import gov.va.escreening.vista.dto.VistaClinicalReminderAndCategory;
import gov.va.escreening.vista.dto.VistaLocation;
import gov.va.escreening.vista.dto.VistaNoteTitle;
import gov.va.escreening.vista.dto.VistaProgressNote;
import gov.va.escreening.vista.dto.VistaServiceCategoryEnum;
import gov.va.escreening.vista.dto.VistaVeteranAppointment;
import gov.va.escreening.vista.dto.VistaVeteranClinicalReminder;
import gov.va.escreening.vista.extractor.VistaRecord;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service("vistaService")
public class VistaServiceImpl implements VistaService {

    private static final Logger logger = LoggerFactory.getLogger(VistaServiceImpl.class);
    @Resource(name = "veteranService")
    VeteranService vs;
    private ClinicalReminderService clinicalReminderService;
    private ClinicService clinicService;
    private NoteTitleService noteTitleService;
    @Autowired
    private NoteTitleRepository noteTitleRepo;

    @Autowired
    private ClinicalReminderRepository clinicalReminderRepo;

    @Autowired
    private ClinicRepository clinicRepo;

    @Autowired
    private EventService eventService;

    @Autowired
    private HealthFactorRepository healthFactorRepo;

    @Autowired
    private SurveyRepository surveyRepo;

    @Autowired
    private ClinicalReminderSurveyRepository crSurveyRepo;

    /**
     * The primary station number of the VistA Server. If this system ever support more than one VistA Server at a time,
     * then this will have to be supplied by the end-user to know which VistA server we are working with.
     */
    private String vistaPrimaryStation;
    /**
     * The application name to use to indicate source application that is requesting authentication.
     */
    private String vistaApplicationName;
    private UserService userService;
    private VistaRepository vistaRepository;

    @Autowired
    public void setClinicalReminderService(ClinicalReminderService clinicalReminderService) {
        this.clinicalReminderService = clinicalReminderService;
    }

    @Autowired
    public void setClinicService(ClinicService clinicService) {
        this.clinicService = clinicService;
    }

    @Autowired
    public void setNoteTitleService(NoteTitleService noteTitleService) {
        this.noteTitleService = noteTitleService;
    }

    public String getVistaPrimaryStation() {
        return vistaPrimaryStation;
    }

    public void setVistaPrimaryStation(String vistaPrimaryStation) {
        this.vistaPrimaryStation = vistaPrimaryStation;
    }

    public String getVistaApplicationName() {
        return vistaApplicationName;
    }

    public void setVistaApplicationName(String vistaApplicationName) {
        this.vistaApplicationName = vistaApplicationName;
    }

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Autowired
    public void setVistaRepository(VistaRepository vistaRepository) {
        this.vistaRepository = vistaRepository;
    }

    @Override
    public String callRpc(String division, String vpid, String duz, String appProxyName, String rpcContext,
                          String rpcName, List<VistaRpcParam> vistaRpcParamList) {
        logger.trace("VistaServiceImpl::callRpc");
        return vistaRepository.callRpc(division, vpid, duz, appProxyName, rpcContext, rpcName, vistaRpcParamList);
    }

    @Override
    public Boolean getPing(String division, String vpid, String duz, String appProxyName) {
        return vistaRepository.getPing(division, vpid, duz, appProxyName);
    }

    @Override
    public List<VeteranDto> searchVeteran(String division, String vpid, String duz, String appProxyName,
                                          String lastName, String ssnLastFour) {

        String lastNameSsn = "";

        // This is optional in the vista RPC, but it needs to be in upper case.
        if (StringUtils.isNotBlank(lastName)) {
            lastNameSsn = StringUtils.left(lastName, 1).toUpperCase();
        }

        // This is required and vista RPC seem to search using all 4 characters. So no wild card.
        if (StringUtils.isNotBlank(ssnLastFour)) {
            lastNameSsn += StringUtils.right(ssnLastFour, 4);
        }

        logger.trace("VistaServiceImpl::searchVeteran " + lastNameSsn);

        List<VeteranDto> resultList = vistaRepository.searchVeteran(division, vpid, duz, appProxyName, lastNameSsn);

        if (resultList == null) {
            resultList = new ArrayList<VeteranDto>();
        } else {
            // look for these in local database too, if they are found in local database then update the veteranId,
            // as veteranId belongs to the local db PK
            for (VeteranDto vDto : resultList) {
                List<VeteranDto> localDbMatchingVeterans = vs.findVeterans(vDto);
                if (localDbMatchingVeterans != null && !localDbMatchingVeterans.isEmpty()) {
                    for (VeteranDto localDbMatchingVeteran : localDbMatchingVeterans) {
                        if (localDbMatchingVeteran.equals(vDto)) {
                            vDto.setVeteranId(localDbMatchingVeteran.getVeteranId());
                            break;
                        }
                    }
                }
            }
        }

        // VistA only filters on the last name initial. So, filter it here.
        if (StringUtils.isNotBlank(lastName)) {
            for (int i = 0; i < resultList.size(); ++i) {
                if (!StringUtils.startsWithIgnoreCase(resultList.get(i).getLastName(), lastName)) {
                    resultList.remove(i);
                    --i;
                }
            }
        }

        return resultList;
    }

    @Override
    public VeteranDto getVeteran(String division, String vpid, String duz, String appProxyName, String veteranIen) {

        logger.trace("VistaServiceImpl::getVeteran " + veteranIen);

        VeteranDto veteranDto = vistaRepository.getVeteran(division, vpid, duz, appProxyName, veteranIen);

        if (veteranDto != null) {
            // Get the cell phone, phone, and email from this other method.
            VeteranDto otherVeteranInfo = vistaRepository.getVeteranDetail(division, vpid, duz, appProxyName,
                    veteranIen);

            if (otherVeteranInfo!=null) {
                veteranDto.setCellPhone(otherVeteranInfo.getCellPhone());
                veteranDto.setPhone(otherVeteranInfo.getPhone());
                veteranDto.setWorkPhone(otherVeteranInfo.getWorkPhone());
                veteranDto.setEmail(otherVeteranInfo.getEmail());
            }

            if (veteranDto.getBirthDate() != null) {
                DateTime dateTime = new DateTime(veteranDto.getBirthDate());
                veteranDto.setBirthDateString(dateTime.toString("MM/dd/yyyy"));
            }

            veteranDto.setDateRefreshedFromVista(new Date());
        }

        return veteranDto;
    }

    @Override
    public List<VistaVeteranAppointment> getVeteranAppointments(String division, String vpid, String duz,
                                                                String appProxyName, String veteranIen) {
        logger.trace("VistaServiceImpl::getAppointments");

        return vistaRepository.getVeteranAppointments(division, vpid, duz, appProxyName, veteranIen);
    }

    @Override
    public List<VeteranDto> findVeterans(String division, String vpid, String duz, String appProxyName,
                                         String ssnLastFour, String lastName, Date birthDate, String middleName) {

        boolean isMiddleNameRequired = StringUtils.isNotBlank(middleName);
        boolean isBirthDateRequired = (birthDate != null);

        List<VeteranDto> veterans = searchVeteran(division, vpid, duz, appProxyName, lastName, ssnLastFour);

        for (int i = 0; i < veterans.size(); ++i) {

            // Lastname is always required for this method.
            if (!lastName.equalsIgnoreCase(veterans.get(i).getLastName())) {
                // remove from list
                logger.trace("Last names do not equal {} != {}", lastName, veterans.get(i).getLastName());
                veterans.remove(i);
                --i;
                continue;
            }

            if (isMiddleNameRequired) {
                if (!middleName.equalsIgnoreCase(veterans.get(i).getMiddleName())) {
                    // remove from list
                    logger.trace("Middle names do not equal {} != {}", lastName, veterans.get(i).getMiddleName());
                    veterans.remove(i);
                    --i;
                    continue;
                }
            }

            if (isBirthDateRequired) {
                if (veterans.get(i).getBirthDate() == null
                        || (DateTimeComparator.getDateOnlyInstance().compare(birthDate, veterans.get(i).getBirthDate()) != 0)) {
                    // remove from list
                    logger.trace("Birth dates do not equal {} != {}", birthDate, veterans.get(i).getBirthDate());

                    veterans.remove(i);
                    --i;
                    continue;
                }
            }

        }

        return veterans;
    }

    @Override
    public List<VistaVeteranClinicalReminder> getVeteranClinicalReminders(String division, String vpid, String duz,
                                                                          String appProxyName, String veteranIen) {

        return vistaRepository.getVeteranClinicalReminders(division, vpid, duz, appProxyName, veteranIen);
    }

    @Override
    public List<VistaNoteTitle> getNoteTitles(String division, String vpid, String duz, String appProxyName) {

        return vistaRepository.getNoteTitles(division, vpid, duz, appProxyName);
    }

    @Override
    public List<VistaClinicAppointment> getAppointmentsForClinic(String division, String vpid, String duz,
                                                                 String appProxyName, String clinicIen, Date fromAppointmentDate, Date toAppointmentDate) {

        return vistaRepository.getAppointmentsForClinic(division, vpid, duz, appProxyName, clinicIen,
                fromAppointmentDate, toAppointmentDate);
    }

    @Override
    public List<VistaClinicalReminderAndCategory> getClinicalReminderAndCategories(String division, String vpid,
                                                                                   String duz, String appProxyName) {

        return vistaRepository.getClinicalReminderAndCategories(division, vpid, duz, appProxyName);
    }

    public void saveHealthFactor(String locationIen, String clinicalNoteIen, String veteranIen,
                                 String visitLocationIen, Date visitDate, String parentVisitIen, String healthFactorIen,
                                 String healthFactorName) {

        List<VistaRecord> vistaRecordList = new ArrayList<VistaRecord>();

        OrwpceSaveHeader orwpceSaveHeader = new OrwpceSaveHeader();
        orwpceSaveHeader.setIsInpatient(false);
        orwpceSaveHeader.setVisitLocationIen(visitLocationIen);
        orwpceSaveHeader.setVisitDate(visitDate);
        orwpceSaveHeader.setVistaServiceCategory(VistaServiceCategoryEnum.E);
        vistaRecordList.add(orwpceSaveHeader);

        OrwpceSaveVisit visitDateRecord = new OrwpceSaveVisit();
        visitDateRecord.setVisitType(VisitTypeEnum.ENCOUNTER_DATE);
        visitDateRecord.setVisitDate(visitDate);
        vistaRecordList.add(visitDateRecord);

        OrwpceSaveVisit visitPatientRecord = new OrwpceSaveVisit();
        visitPatientRecord.setVisitType(VisitTypeEnum.PATIENT);
        visitPatientRecord.setPatientIen(veteranIen);
        vistaRecordList.add(visitPatientRecord);

        OrwpceSaveVisit visitEncounterRecord = new OrwpceSaveVisit();
        visitEncounterRecord.setVisitType(VisitTypeEnum.ENCOUNTER_SERVICE_CATEGORY);
        visitEncounterRecord.setVistaServiceCategory(VistaServiceCategoryEnum.E);
        vistaRecordList.add(visitEncounterRecord);

        OrwpceSaveVisit visitParentVisitIenRecord = new OrwpceSaveVisit();
        visitParentVisitIenRecord.setVisitType(VisitTypeEnum.PARENT_VISIT_IEN_HIST);
        visitParentVisitIenRecord.setParentVisitIen(parentVisitIen);
        vistaRecordList.add(visitParentVisitIenRecord);

        OrwpceSaveVisit visitOutsideLocationRecord = new OrwpceSaveVisit();
        visitOutsideLocationRecord.setVisitType(VisitTypeEnum.OUTSIDE_LOCATION_HIST);
        visitOutsideLocationRecord.setFreeText("0");
        vistaRecordList.add(visitOutsideLocationRecord);

        OrwpceSaveHealthFactor orwpceSaveHealthFactor = new OrwpceSaveHealthFactor();
        orwpceSaveHealthFactor.setRemoveHealthFactor(false);
        orwpceSaveHealthFactor.setHealthFactorIen(healthFactorIen);
        orwpceSaveHealthFactor.setHealthFactorName(healthFactorName);
        orwpceSaveHealthFactor.setSequenceNumber(1);
        vistaRecordList.add(orwpceSaveHealthFactor);

        OrwpceSaveComment orwpceSaveComment = new OrwpceSaveComment();
        orwpceSaveComment.setSequenceNumber(1);
        vistaRecordList.add(orwpceSaveComment);

        List<String> healthFactor = new ArrayList<String>();

        for (VistaRecord vistaRecord : vistaRecordList) {
            healthFactor.add(vistaRecord.toVistaRecord());
        }

        List<VistaRpcParam> vistaRpcParamList = new ArrayList<VistaRpcParam>();
        vistaRpcParamList.add(new VistaRpcParam("array", healthFactor));
        vistaRpcParamList.add(new VistaRpcParam("string", locationIen));
        vistaRpcParamList.add(new VistaRpcParam("string", clinicalNoteIen));

    }

    @Override
    public List<DialogPrompt> getDialogPrompt(String division, String vpid, String duz, String appProxyName,
                                              String dialogElementIen, Boolean isHistorical, String findingType) {

        return vistaRepository.getDialogPrompt(division, vpid, duz, appProxyName, dialogElementIen, isHistorical,
                findingType);
    }

    @Override
    public List<VistaLocation> getLocationList(String division, String vpid, String duz, String appProxyName) {

        String locationNameStartRow = "";

        List<VistaLocation> resultList = vistaRepository.getLocationList(division, vpid, duz, appProxyName,
                locationNameStartRow, true);

        if (resultList != null && resultList.size() > 0) {
            locationNameStartRow = resultList.get(resultList.size() - 1).getName();
        }

        while (locationNameStartRow != null) {

            List<VistaLocation> tempResultList = vistaRepository.getLocationList(division, vpid, duz, appProxyName,
                    locationNameStartRow, true);

            if (tempResultList != null && tempResultList.size() > 0) {

                locationNameStartRow = tempResultList.get(tempResultList.size() - 1).getName();

                resultList.addAll(tempResultList);
            } else {
                locationNameStartRow = null;
            }
        }

        return resultList;
    }

    @Override
    public VistaProgressNote saveProgressNote(VistaServiceContext<ProgressNoteParameters> vistaServiceContext)
            throws VistaLinkClientException {
        return vistaServiceContext.getVistaClient().saveProgressNote(vistaServiceContext.getRequestParameters());
    }

    @Override
    public VistaBrokerUserInfo doVistaSignon(String division, String applicationName, String accessCode,
                                             String verifyCode, String clientIp) {

        return vistaRepository.doVistaSignon(division, applicationName, accessCode, verifyCode, clientIp);
    }

    @Override
    public void verifyVistaAccount(int userId, String accessCode, String verifyCode, String clientIp) {

        VistaBrokerUserInfo vistaBrokerUserInfo = vistaRepository.doVistaSignon(vistaPrimaryStation,
                vistaApplicationName, accessCode, verifyCode, clientIp);

        String vistaDuz = vistaBrokerUserInfo.getDuz();
        String vistaDivision = vistaPrimaryStation;

        // See if someone has already been mapped to this account.
        int count = userService.getCountOfUsersMappedToDuzDivision(userId, vistaDuz, vistaDivision);

        if (count > 0) {
            throw new VistaVerificationAlreadyMappedException(
                    "Another account is already mapped to this Access/Verify code.");
        }

        // Update the database.
        userService.updateVistaFields(userId, vistaDuz, vistaDivision, true);
    }

    @Override
    @Transactional
    public Map<String, Integer> refreshClinicList(String division, String vpid, String duz, String appProxyName) {
        Map<String, Integer> responseMap = Maps.newHashMap();
        responseMap.put("updateCnt", 0);
        responseMap.put("insertCnt", 0);

        // Fetch all Clinic list from VistA.
        List<VistaLocation> vistaLocations = getLocationList(division, vpid, duz, appProxyName);

        // If we didn't get anything, then just return.
        if (vistaLocations == null || vistaLocations.size() < 1) {
            logger.warn("No clinics imported from Vista. Returning from here");
            return responseMap;
        }
        logger.trace("total clinics retrieved from vista:{}", vistaLocations.size());

        // Fetch from DB.
        List<Clinic> escreeningClinics = clinicService.getClinics();
        logger.trace("total clinics retrieved from escreening db:{}", escreeningClinics.size());

        Multimap<Long, Clinic> existingDbClinic = ArrayListMultimap.create();

        escreeningClinics.stream().filter(c -> c.getVistaIen() != null && !c.getVistaIen().isEmpty()).forEach(c -> existingDbClinic.put(Long.valueOf(c.getVistaIen().trim()), c));
        logger.trace("usable clinics retrieved from escreening db:{}", existingDbClinic.size());

        for (VistaLocation vistaLocation : vistaLocations) {
            Collection<Clinic> clinics = existingDbClinic.get(vistaLocation.getIen());
            if (clinics == null || clinics.isEmpty()) {
                existingDbClinic.put(vistaLocation.getIen(), new Clinic("" + vistaLocation.getIen(), vistaLocation.getName()));
                if (logger.isTraceEnabled()) {
                    logger.trace("vista location not found in escreening db--adding {}", vistaLocation);
                }
            } else {
                Map<String, Clinic> clinicsByName = clinics.stream().collect(Collectors.toMap(Clinic::getName, (c) -> c));
                if (clinicsByName.get(vistaLocation.getName()) == null) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("vista location is out of sync with escreening db--synching {}", vistaLocation);
                        logger.debug("escreening clinics' ien will be set to null {}", clinics);
                    }
                    clinics.stream().forEach(c -> c.setVistaIen(null));
                    clinics.add(new Clinic("" + vistaLocation.getIen(), vistaLocation.getName()));
                    if (logger.isDebugEnabled()) {
                        logger.debug("escreening clinics after synch with vista {}", clinics);
                    }
                }
            }
        }

        existingDbClinic.values().stream().forEach(c -> {
            if (c.getVistaIen() == null || c.getClinicId() == null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("clinic is being {} -- {}", c.getClinicId() == null ? "added" : "changed", c);
                }
                clinicRepo.update(c);
            }
        });

        countStats(existingDbClinic, responseMap);
        return responseMap;
    }

    private void countStats(Multimap<Long, Clinic> existingDbClinic, Map<String, Integer> responseMap) {
        existingDbClinic.values().stream().forEach(c -> {
            if (c.getVistaIen() == null) {
                responseMap.put("updateCnt", responseMap.get("updateCnt") + 1);
            } else if (c.getClinicId() == null) {
                responseMap.put("insertCnt", responseMap.get("insertCnt") + 1);
            }
        });
    }

    @Override
    @Transactional
    public int refreshClinicalReminderList(String division, String vpid, String duz, String appProxyName) {

        // Fetch all Note Title list from VistA.

        List<VistaClinicalReminderAndCategory> clinicalReminderList = getClinicalReminderAndCategories(division, vpid,
                duz, appProxyName);

        // If we didn't get anything, then just return.
        if (clinicalReminderList == null || clinicalReminderList.size() < 1) {
            logger.trace("Fetched clinical reminders from VistA: null or 0");
            return 0;
        }

        logger.trace("Fetched clinical reminders from VistA: " + clinicalReminderList.size());

        // Fetch from DB.
        List<ClinicalReminder> clinicalReminderListDb = clinicalReminderRepo.findAll();

        logger.trace("Fetched clinical reminders from DB: " + clinicalReminderListDb.size());

        HashMap<String, ClinicalReminder> existingDbClinicalReminder = new HashMap<String, ClinicalReminder>(
                clinicalReminderList.size());

        for (ClinicalReminder cr : clinicalReminderListDb) {
            existingDbClinicalReminder.put(cr.getName(), cr);
            //existingDbClinicalReminder.put(cr.getVistaIen(), cr);
        }

        int refreshCount = 0;

        Iterator<VistaClinicalReminderAndCategory> crItr = clinicalReminderList.iterator();
        while (crItr.hasNext()) {

            VistaClinicalReminderAndCategory cr = crItr.next();

            // This RPC returns both 'Category' and 'Clinical Reminder'.
            // Don't need to look at Categories.
            if (StringUtils.equalsIgnoreCase("C", cr.getClinicalReminderTypeName())) {
                crItr.remove();
            } else if (existingDbClinicalReminder.containsKey(cr.getClinicalReminderName())) {
                crItr.remove();

                // update IEN if needed
                ClinicalReminder existing = existingDbClinicalReminder.remove(cr.getClinicalReminderName());
                if (existing.getVistaIen() == null
                        || !existing.getVistaIen().equals(cr.getClinicalReminderIen())
                        || !existing.getPrintName().equals(cr.getPrintName())) {
                    existing.setVistaIen(cr.getClinicalReminderIen());
                    existing.setPrintName(cr.getPrintName());
                    clinicalReminderRepo.update(existing);
                    ++refreshCount;
                    logger.info("Updated IEN of an existing clinical reminder {}", existing);
                }
            }
        }

        //At this point only CRs that don't match in name are left to add (this assumes that IENs are unique in clinicalReminderList)
        for (VistaClinicalReminderAndCategory cr : clinicalReminderList) {
//            ClinicalReminder existing = existingDbClinicalReminder.get(cr.getClinicalReminderIen());
//            if (existing != null) {
//                if (!existing.getName().equals(cr.getClinicalReminderName()) ||
//                        !existing.getPrintName().equals(cr.getPrintName())) {
//
//                    existing.setName(cr.getClinicalReminderName());
//                    existing.setPrintName(cr.getPrintName());
//                    clinicalReminderRepo.update(existing);
//                    ++refreshCount;
//                    logger.info("Updated Name of an existing clinical reminder with same IEN {}", existing);
//                }
//            } else {
                Integer crId = clinicalReminderService.create(cr.getClinicalReminderName(),
                        cr.getClinicalReminderIen(),
                        cr.getPrintName(),
                        cr.getClinicalReminderClass());
                ++refreshCount;
                logger.info("Created a new clinical reminder (with clinical_reminder_id {}): {}", crId, cr);
            }

        if (refreshCount > 0) {
            clinicalReminderRepo.commit();
        }

        if(!existingDbClinicalReminder.isEmpty()) {
            List<ClinicalReminderSurvey> crSurveyList = crSurveyRepo.findAll();
            HashSet<Integer> crList = Sets.newHashSet();
            for(ClinicalReminderSurvey crs : crSurveyList)
            {
                crList.add(crs.getClinicalReminder().getClinicalReminderId());
            }
            //For the rest of the clinical reminders in the DB, set the IEN to null because they do not exist in VISTA
            for (ClinicalReminder cr : existingDbClinicalReminder.values()) {

                if (crList.contains(cr.getClinicalReminderId())) {
                    cr.setVistaIen(null);
                    logger.info("Set the clinical reminder's IEN to null {}", cr);
                } else {
                    clinicalReminderRepo.delete(cr);
                logger.info("deleted the clinical reminders not found in VistA {}", cr);
				}
            }
        }
        refreshMHAIens(division, vpid, duz, appProxyName);

        return refreshCount;
    }

    @Override
    @Transactional
    public int refreshNoteTitleList(String division, String vpid, String duz, String appProxyName) {

        // Fetch all Note Title list from VistA.
        List<VistaNoteTitle> noteTitleList = getNoteTitles(division, vpid, duz, appProxyName);

        // If we didn't get anything, then just return.
        if (noteTitleList == null || noteTitleList.size() < 1) {
            logger.trace("Fetched note titles from VistA: null or 0");
            return 0;
        }

        logger.trace("Fetched note titles from VistA: " + noteTitleList.size());

        // Fetch from DB.
        List<NoteTitle> noteTitleListDb = noteTitleService.findAll();

        logger.trace("Fetched note titles from DB: " + noteTitleListDb.size());

        HashMap<String, NoteTitle> existingDbNoteTitle = new HashMap<String, NoteTitle>();

        for (int i = 0; i < noteTitleListDb.size(); ++i) {
            existingDbNoteTitle.put(noteTitleListDb.get(i).getName(), noteTitleListDb.get(i));
        }

        int refreshCount = 0;

        for (VistaNoteTitle nt : noteTitleList) {
            if (!existingDbNoteTitle.containsKey(nt.getNoteTitleName())) {

                noteTitleService
                        .create(nt.getNoteTitleName(), nt.getNoteTitleIen().toString());
                ++refreshCount;
                logger.info("Added new note title: " + nt.getNoteTitleName());
            } else {
                NoteTitle noteTitle = existingDbNoteTitle.get(nt.getNoteTitleName());
                if (noteTitle.getVistaIen() == null || !noteTitle.getVistaIen().equals(nt.getNoteTitleIen().toString())) {
                    noteTitle.setVistaIen(nt.getNoteTitleIen().toString());
                    noteTitleRepo.update(noteTitle);

                    logger.info("Unpdated the IEN of Notetitle " + noteTitle.getName() + " to "
                            + noteTitle.getVistaIen());
                    ++refreshCount;
                }
            }

        }
        if (refreshCount > 0) {
            noteTitleRepo.commit();
        }

        return refreshCount;
    }

    @Override
    @Transactional
    public int refreshHealthFactors(String division, String vpid, String duz, String appProxyName) {

        List<HealthFactor> hfList = healthFactorRepo.findAll();
        int numRecord = 0;

        /** Clinical Reminder IEN is the key, HealthFactor name/healthFactor map is the value **/
        Map<String, Map<String, HealthFactor>> crToHFMap = new HashMap<String, Map<String, HealthFactor>>();

        for (HealthFactor hf : hfList) {
            String crIen = hf.getClinicalReminder().getVistaIen();

            if(crIen !=null) {

                if (!crToHFMap.containsKey(crIen)) {
                    crToHFMap.put(crIen, new HashMap<String, HealthFactor>());
                }

                crToHFMap.get(crIen).put(hf.getName(), hf);
            }
        }

        List<ClinicalReminder> clinicalReminders = clinicalReminderRepo.findAll();

        for (ClinicalReminder cr : clinicalReminders) {
            String crIen = cr.getVistaIen();
            if (crIen == null || crIen.isEmpty() || cr.getClinicalReminderSurveyList() == null
                    || cr.getClinicalReminderSurveyList().isEmpty()) {
                continue;
            }
            List<DialogComponent> componentList = vistaRepository.getClinicalReminderDialogs(division, vpid, duz,
                    appProxyName, crIen);
            Map<String, String> hfIenMap = new HashMap<String, String>();

            HashSet<String> componentIenSet = new HashSet<String>();
            for (DialogComponent comp : componentList) {
                String ien = comp.getDialogComponentIen();
                if (!componentIenSet.contains(ien)) {
                    Map<String, String> ienMap = vistaRepository.getHealthFactorIENMap(division, vpid, duz,
                            appProxyName, ien);

                    hfIenMap.putAll(ienMap);
                    componentIenSet.add(ien);
                }
            }

            Map<String, HealthFactor> healthFactorMap = crToHFMap.get(crIen);

            for (Map.Entry<String, String> entry : hfIenMap.entrySet()) {
                String name = entry.getKey();
                String ien = entry.getValue();
                if (healthFactorMap != null && healthFactorMap.containsKey(entry.getKey())) {
                    HealthFactor hf = healthFactorMap.get(name);
                    hfList.remove(hf);
                    if (hf.getVistaIen() == null || !hf.getVistaIen().equals(ien)) {
                        hf.setVistaIen(hfIenMap.get(name));
                        healthFactorRepo.update(hf);
                        eventService.updateHealthFactorEvent(hf);
                        logger.info("Updated vistaIen for HealthFactor " + hf.getName() + " to " + hfIenMap.get(name));
                        numRecord++;
                    }
                } else {
                    HealthFactor newHf = new HealthFactor();
                    newHf.setClinicalReminder(cr);
                    newHf.setDateCreated(Calendar.getInstance().getTime());
                    newHf.setName(name);
                    newHf.setVistaIen(ien);
                    newHf.setIsHistorical(false);
                    healthFactorRepo.create(newHf);
                    eventService.updateHealthFactorEvent(newHf);
                    numRecord++;
                }
            }
        }

        //Now, remove the remaining Health Factors because they do not exist in vistA.
        for (HealthFactor hf : hfList) {
            eventService.deleteEventForHealthFactor(hf);
            hf.setVistaIen(null);
            healthFactorRepo.update(hf);
            logger.info("Removed Health Factor " + hf.getName() + hf.getVistaIen());
        }

        healthFactorRepo.commit();

        return numRecord;
    }

    @Override
    @Transactional
    public int refreshMHAIens(String division, String vpid, String duz, String appProxyName) {

        int numRecord = 0;
        List<Survey> surveyList = surveyRepo.getMhaSurveyList();

        logger.trace("refreshing MHA IENs for surveys");
        for (Survey s : surveyList) {
            if (s.getClinicalReminderSurveyList() != null && !s.getClinicalReminderSurveyList().isEmpty()) {
                String ien = s.getClinicalReminderSurveyList().get(0).getClinicalReminder().getVistaIen();

                List<DialogComponent> componentList = vistaRepository.getClinicalReminderDialogs(division, vpid, duz,
                        appProxyName, ien);

                // 1^3746^1.8.1.1^D^^^^0^0^3745^
                // 2^3746^1.8.1.1^PC PTSD

                Iterator<DialogComponent> it = componentList.iterator();
                while (it.hasNext()) {
                    DialogComponent c = it.next();
                    if (!c.getResultGroupListString().isEmpty() && !c.getExcludeMentalHealthTestFromProgressNote()) {
                        String resultGrpIen = c.getResultGroupListString();

//                        RPC Name: "ORQQPXRM DIALOG PROMPTS"
//                            Input parameter 0: 663000737
//                            Input parameter 1: 0
//                            Input parameter 2: 
//                            ========RPC RESULTS======
//                            3^663000737^^MH^0^5^^AUDC^^^^^0
//                            6^663000737^^SCREEN FOR ALCOHOL (AUDIT-C)
//                            ========END RESULTS======

                        String displayText = null;


                        String[] promptResult = vistaRepository.getDialogPromptsAsString(division, vpid, duz, appProxyName, c.getDialogComponentIen());

                        if (promptResult != null) {
                            for (String str : promptResult) {
                                if (str.contains("^MH^")) {
                                    String[] pieces = StringUtils.splitPreserveAllTokens(str, '^');
                                    displayText = pieces[7];
                                    break;
                                }

                            }
                        }

                        if (displayText != null && displayText.equals(s.getMhaTestName())) {
                            logger.info("Found MHA data for " + s);
                            // now check and update resultGrpIEN
                            if (s.getMhaResultGroupIen() == null || !s.getMhaResultGroupIen().equals(resultGrpIen)) {
                                s.setMhaResultGroupIen(resultGrpIen);
                                numRecord++;
                                break;
                            }
                        }
                    }
                }

            }
        }

        if (numRecord > 0) {
            surveyRepo.commit();
        }
        return numRecord;

    }
}
