package gov.va.escreening.service.export;

import gov.va.escreening.domain.ExportTypeEnum;
import gov.va.escreening.dto.dashboard.AssessmentDataExport;
import gov.va.escreening.dto.dashboard.DataExportCell;
import gov.va.escreening.dto.dashboard.DataExportFilterOptions;
import gov.va.escreening.entity.AssessmentVariable;
import gov.va.escreening.entity.ExportLog;
import gov.va.escreening.entity.ExportType;
import gov.va.escreening.entity.Program;
import gov.va.escreening.entity.SurveyMeasureResponse;
import gov.va.escreening.entity.User;
import gov.va.escreening.entity.Veteran;
import gov.va.escreening.entity.VeteranAssessment;
import gov.va.escreening.form.ExportDataFormBean;
import gov.va.escreening.repository.AssessmentVariableRepository;
import gov.va.escreening.repository.ExportLogRepository;
import gov.va.escreening.repository.ExportTypeRepository;
import gov.va.escreening.repository.ProgramRepository;
import gov.va.escreening.repository.UserRepository;
import gov.va.escreening.repository.VeteranRepository;
import gov.va.escreening.service.VeteranAssessmentService;
import gov.va.escreening.util.SurveyResponsesHelper;
import gov.va.escreening.variableresolver.AssessmentVariableDto;
import gov.va.escreening.variableresolver.VariableResolverService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Resource;

import org.bouncycastle.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;

@Service("exportDataService")
public class ExportDataServiceImpl implements ExportDataService, MessageSourceAware {
	private static final Logger logger = LoggerFactory.getLogger(ExportDataServiceImpl.class);

	private MessageSource msgSrc;

	@Resource(type = ExportLogRepository.class)
	private ExportLogRepository exportLogRepository;

	@Resource(type = ExportTypeRepository.class)
	private ExportTypeRepository exportTypeRepository;

	@Resource(type = ProgramRepository.class)
	private ProgramRepository programRepository;

	@Resource(type = UserRepository.class)
	private UserRepository userRepository;

	@Resource(type = VeteranAssessmentService.class)
	private VeteranAssessmentService veteranAssessmentService;

	@Resource(type = VeteranRepository.class)
	private VeteranRepository veteranRepository;

	@Resource(type = VariableResolverService.class)
	private VariableResolverService vrs;

	@Resource(type = AssessmentVariableRepository.class)
	private AssessmentVariableRepository avr;

	@Resource(type = DataDictionaryService.class)
	private DataDictionaryService dds;

	@Resource(type = SurveyResponsesHelper.class)
	private SurveyResponsesHelper srh;

	@Override
	@Transactional
	public AssessmentDataExport getAssessmentDataExport(
			ExportDataFormBean exportDataFormBean) {

		AssessmentDataExport assessmentDataExport = new AssessmentDataExport();

		if (exportDataFormBean.getHasParameter()) {
			// 10) Use the passed in filter criteria to pull in the matching assessments
			List<VeteranAssessment> matchingAssessments = veteranAssessmentService.searchVeteranAssessmentForExport(exportDataFormBean);

			// 11) collect all the formulae for these assessments
			Map<Integer, Map<String, String>> formulae = buildFormulaeMapForMatchingAssessments(matchingAssessments);

			// 12) get a fresh data dictionary representing the fresh meta data
			Map<String, Table<String, String, String>> dataDictionary = dds.createDataDictionary();

			// 20) prepare exportData from matching assessments
			assessmentDataExport.setTableContents(createDataExportReport(matchingAssessments, exportDataFormBean.getExportTypeId(), formulae, dataDictionary));
			DataExportFilterOptions filterOptions = createFilterOptions(exportDataFormBean);
			assessmentDataExport.setFilterOptions(filterOptions);

			// 30) log this activity
			ExportLog exportLog = logDataExport(assessmentDataExport);

			if (exportLog != null) {
				assessmentDataExport.setExportLogId(exportLog.getExportLogId());
			}
		}

		return assessmentDataExport;
	}

	private Map<Integer, Map<String, String>> buildFormulaeMapForMatchingAssessments(
			List<VeteranAssessment> matchingAssessments) {

		Map<Integer, Map<String, String>> formulaeMap = Maps.newHashMap();

		Iterable<AssessmentVariable> avFormulaeEntities = avr.findAllFormulae();

		for (VeteranAssessment va : matchingAssessments) {
			Iterable<AssessmentVariableDto> avFormulaeList = vrs.resolveVariablesFor(va.getVeteranAssessmentId(), avFormulaeEntities);
			formulaeMap.put(va.getVeteranAssessmentId(), createFormulaeMap(avFormulaeList));
		}
		return formulaeMap;
	}

	private Map<String, String> createFormulaeMap(
			Iterable<AssessmentVariableDto> avFormulaeList) {
		Map<String, String> formulaeMap = Maps.newHashMap();
		for (AssessmentVariableDto dto : avFormulaeList) {
			formulaeMap.put(dto.getDisplayName(), dto.getDisplayText());
		}
		return formulaeMap;
	}

	private DataExportFilterOptions createFilterOptions(
			ExportDataFormBean exportDataFormBean) {

		DataExportFilterOptions filterOptions = new DataExportFilterOptions();

		if (exportDataFormBean.getToAssessmentDate() != null)
			filterOptions.setAssessmentEnd(exportDataFormBean.getToAssessmentDate());

		if (exportDataFormBean.getFromAssessmentDate() != null)
			filterOptions.setAssessmentStart(exportDataFormBean.getFromAssessmentDate());

		if (exportDataFormBean.getClinicianId() != null)
			filterOptions.setClinicianUserId(exportDataFormBean.getClinicianId());

		if (exportDataFormBean.getCommentText() != null)
			filterOptions.setComment(exportDataFormBean.getCommentText());

		if (exportDataFormBean.getCreatedByUserId() != null)
			filterOptions.setCreatedByUserId(exportDataFormBean.getCreatedByUserId());

		if (exportDataFormBean.getExportedByUserId() != null)
			filterOptions.setExportedByUserId(exportDataFormBean.getExportedByUserId());

		if (exportDataFormBean.getExportLogId() != null)
			filterOptions.setExportLogId(exportDataFormBean.getExportLogId());

		if (exportDataFormBean.getExportTypeId() != null)
			filterOptions.setExportTypeId(exportDataFormBean.getExportTypeId());

		if (exportDataFormBean.getProgramId() != null)
			filterOptions.setProgramId(exportDataFormBean.getProgramId());

		if (exportDataFormBean.getVeteranId() != null)
			filterOptions.setVeteranId(exportDataFormBean.getVeteranId());

		String filePath = String.format("va_export_data_as_of_ts_%s.csv", System.nanoTime());
		filterOptions.setFilePath(filePath);

		return filterOptions;
	}

	@Override
	@Transactional
	public ExportLog logDataExport(AssessmentDataExport dataExport) {

		// Add an entry to the exportLog table
		ExportLog exportLog = createExportLogFromOptions(dataExport.getFilterOptions());

		if (addExportLogDataToExportLog(exportLog, dataExport)) {
			exportLogRepository.create(exportLog);
			return exportLog;
		}
		return null;
	}

	private boolean addExportLogDataToExportLog(ExportLog exportLog,
			AssessmentDataExport dataExport) {
		String header = createHeaderFromDataExport(dataExport);
		if (header != null) {
			List<String> data = createDataFromDataExport(dataExport);
			dataExport.setHeaderAndData(header, data);
			exportLog.setExportLogData(dataExport.getHeader(), dataExport.getData());
			return true;
		}
		return false;
	}

	private List<String> createDataFromDataExport(
			AssessmentDataExport dataExport) {

		List<List<DataExportCell>> tableContent = dataExport.getTableContent();

		if (tableContent != null && !tableContent.isEmpty()) {
			List<String> rows = new ArrayList<String>();
			for (List<DataExportCell> row : tableContent) {
				StringBuilder sb = new StringBuilder();
				for (DataExportCell cell : row) {
					sb.append(cell.getCellValue().replaceAll(",", "-"));
					sb.append(",");
				}
				if (logger.isDebugEnabled()) {
					if (logger.isDebugEnabled())
						logger.debug(String.format("row of length %s is being added [%s]", sb.length(), sb));
				}
				rows.add(sb.toString());
			}
			return rows;

		} else {
			// if export log is downloaded again
			return dataExport.getData();
		}
	}

	private String createHeaderFromDataExport(AssessmentDataExport dataExport) {
		List<List<DataExportCell>> tableContent = dataExport.getTableContent();

		String header = null;

		if (tableContent != null && !tableContent.isEmpty()) {
			List<DataExportCell> firstRow = tableContent.iterator().next();
			StringBuilder sb = new StringBuilder();
			for (DataExportCell dec : firstRow) {
				sb.append(dec.getColumnName().replaceAll(",", "-"));
				sb.append(",");
			}
			header = sb.toString();
		} else {
			// if export log is requested to be downloaded again
			header = dataExport.getHeader();
		}
		if (header != null && logger.isDebugEnabled()) {
			if (logger.isDebugEnabled())
				logger.debug(String.format("header of length %s is being added [%s]", header.length(), header));
		}
		return header;
	}

	private ExportLog createExportLogFromOptions(DataExportFilterOptions options) {
		Preconditions.checkNotNull(options, "DataExportFilterOptions value cannot be null");

		ExportLog exportLog = new ExportLog();

		if (options.getAssessmentEnd() != null) {
			exportLog.setAssessmentEndFilter(options.getAssessmentEnd());
		}

		if (options.getAssessmentStart() != null) {
			exportLog.setAssessmentStartFilter(options.getAssessmentStart());
		}

		if (options.getClinicianUserId() != null) {
			User clinician = userRepository.findOne(options.getClinicianUserId());
			exportLog.setClinician(clinician);
		}

		if (options.getComment() != null && !options.getComment().isEmpty()) {
			exportLog.setComment(options.getComment());
		}

		if (options.getCreatedByUserId() != null) {
			User createdByUser = userRepository.findOne(options.getCreatedByUserId());
			exportLog.setCreatedByUser(createdByUser);
		}

		if (options.getExportedByUserId() != null) {
			User exportedByUser = userRepository.findOne(options.getExportedByUserId());
			exportLog.setExportedByUser(exportedByUser);
		}

		if (options.getExportTypeId() != null) {
			ExportType exportType = exportTypeRepository.findOne(options.getExportTypeId());
			exportLog.setExportType(exportType);
		}

		if (options.getFilePath() != null && !options.getFilePath().isEmpty()) {
			exportLog.setFilePath(options.getFilePath());
		}

		if (options.getProgramId() != null) {
			Program program = programRepository.findOne(options.getProgramId());
			exportLog.setProgram(program);
		}

		if (options.getVeteranId() != null) {
			Veteran veteran = veteranRepository.findOne(options.getVeteranId());
			exportLog.setVeteran(veteran);
		}

		return exportLog;
	}

	private List<List<DataExportCell>> createDataExportReport(
			List<VeteranAssessment> matchingAssessments,
			Integer identifiedExportType,
			Map<Integer, Map<String, String>> vid2FormulaeMap,
			Map<String, Table<String, String, String>> dataDictionary) {

		List<List<DataExportCell>> tableContent = new ArrayList<List<DataExportCell>>();

		// build an export row for each assessment
		for (VeteranAssessment assessment : matchingAssessments) {
			Map<String, String> formulaeMap = vid2FormulaeMap.get(assessment.getVeteranAssessmentId());
			List<SurveyMeasureResponse> smrList = assessment.getSurveyMeasureResponseList();
			List<DataExportCell> exportDataRowCells = buildExportDataForOneAssessment(assessment, identifiedExportType, smrList, formulaeMap, dataDictionary);
			tableContent.add(exportDataRowCells);
		}

		return tableContent;
	}

	private List<DataExportCell> buildExportDataForOneAssessment(
			VeteranAssessment assessment, Integer identifiedExportType,
			List<SurveyMeasureResponse> smrList,
			Map<String, String> formulaeMap,
			Map<String, Table<String, String, String>> ddMap) {

		// has user asked to show the ppi info or not
		boolean show = ExportTypeEnum.DEIDENTIFIED.getExportTypeId() != identifiedExportType;

		// quickly gather mandatory columns data
		List<DataExportCell> exportDataRowCells = buildMandatoryColumns(assessment, show);

		for (String surveyName : ddMap.keySet()) {
			Map<String, String> usrRespMap = srh.prepareSurveyResponsesMap(surveyName, smrList, show);

			// get the table data (one sheet) for this survey
			Table<String, String, String> dataDictionary = ddMap.get(surveyName);

			Map<String, String> columnData = syncTableQ(usrRespMap, dataDictionary);

			// traverse through each exportName, and try to find the run time response for the exportName. In case the
			// user has not responded, leave 999
			Multimap<String, DataExportCell> multiSelectMap = HashMultimap.create();
			for (Entry<String, String> entry : columnData.entrySet()) {
				String exportName = entry.getValue();
				if (!exportName.isEmpty()) {
					DataExportCell oneCell = createExportCell(usrRespMap, formulaeMap, exportName, show);
					if (logger.isDebugEnabled()) {
						logger.debug(String.format("adding data for data dictionary column %s->%s=%s", surveyName, exportName, oneCell));
					}
					exportDataRowCells.add(oneCell);
					saveMultiSelects(multiSelectMap, entry, oneCell);
				}
			}
			adjustMultiSelects(multiSelectMap);
		}
		return exportDataRowCells;
	}

	public DataExportCell createExportCell(Map<String, String> usrRespMap,
			Map<String, String> formulaeMap, String exportName, boolean show) {

		// try to find the user response
		String exportVal = usrRespMap == null ? null : usrRespMap.get(exportName);
		// if value of export name was an formulae (sum, avg, or some other formula derived value)
		exportVal = srh.getOrMiss(exportVal == null ? formulaeMap.get(exportName) : exportVal);
		return new DataExportCell(exportName, exportVal);
	}

	/**
	 * method to traverse user responses and look for table questions
	 * 
	 * for every table question, this method will add a row in data dictionary
	 * 
	 * @param usrRespMap
	 * @param dataDictionary
	 * @return
	 */
	private Map<String, String> syncTableQ(Map<String, String> usrRespMap,
			Table<String, String, String> dataDictionary) {

		String ddColumnKey = msgSrc.getMessage("data.dict.column.var.name", null, null);
		Map<String, String> ddColumnData = dataDictionary.column(ddColumnKey);

		Map<String, String> modColumnData = Maps.newHashMap();
		List<String> toBeDelKeys = Lists.newArrayList();

		Set<String> usrRespExportNames = usrRespMap.keySet();
		for (String usrRespExportName : usrRespExportNames) {
			if (usrRespExportName.contains("~")) {
				String[] exportNamesParts = Strings.split(usrRespExportName, '~');

				String usrExportName = exportNamesParts[0];
				String usrTQIndex = exportNamesParts[1];
				// now look for this export name in the columnData []
				for (Entry<String, String> entry : ddColumnData.entrySet()) {
					if (entry.getValue().equals(usrExportName)) {
						modColumnData.put(entry.getKey() + "_" + usrTQIndex, usrRespExportName);
						toBeDelKeys.add(entry.getKey());
					}
				}
			}
		}

		// truncate the ddColumnData
		for (String key : toBeDelKeys) {
			dataDictionary.remove(key, ddColumnKey);
		}

		ddColumnData.putAll(modColumnData);

		return ddColumnData;

	}

	private void adjustMultiSelects(
			Multimap<String, DataExportCell> multiSelectMap) {
		for (String key : multiSelectMap.keySet()) {
			Collection<DataExportCell> cells = multiSelectMap.get(key);
			for (DataExportCell cell : cells) {
				if ("1".equals(cell.getCellValue())) {
					setMissingCellsToZero(cells);
					break;
				}
			}
		}
	}

	private void setMissingCellsToZero(Collection<DataExportCell> cells) {
		for (DataExportCell cell : cells) {
			if (srh.miss().equals(cell.getCellValue())) {
				cell.setCellValue("0");
			}
		}
	}

	private void saveMultiSelects(
			Multimap<String, DataExportCell> multiSelectMap,
			Entry<String, String> entry, DataExportCell oneCell) {

		String key = entry.getKey();
		key = key.substring(0, key.lastIndexOf("_"));
		multiSelectMap.put(key, oneCell);
	}

	@Override
	@Transactional
	public AssessmentDataExport downloadExportData(Integer userId,
			int exportLogId, String comment) {

		ExportLog exportLog = exportLogRepository.findOne(exportLogId);

		AssessmentDataExport ade = AssessmentDataExport.createFromExportLog(exportLog);

		ade.getFilterOptions().setComment(comment);
		ade.getFilterOptions().setCreatedByUserId(userId);

		ExportLog newExportLog = logDataExport(ade);

		ade.setExportLogId(newExportLog.getExportLogId());

		return ade;
	}

	@Override
	public void setMessageSource(MessageSource msgSrc) {
		this.msgSrc = msgSrc;
	}

	@Override
	public List<DataExportCell> buildExportDataForOneAssessment(
			VeteranAssessment va, int identifiedExportType) {

		Map<Integer, Map<String, String>> formulaeMap = buildFormulaeMapForMatchingAssessments(Arrays.asList(va));
		Map<String, Table<String, String, String>> dataDictionary = dds.createDataDictionary();
		List<SurveyMeasureResponse> smrList = va.getSurveyMeasureResponseList();
		List<DataExportCell> row = buildExportDataForOneAssessment(va, identifiedExportType, smrList, formulaeMap.get(va.getVeteranAssessmentId()), dataDictionary);
		return row;

	}

	public List<DataExportCell> buildMandatoryColumns(
			VeteranAssessment assessment, boolean show) {

		List<DataExportCell> mandatoryData = new ArrayList<DataExportCell>();

		mandatoryData.addAll(collectPpi(assessment, show));

		mandatoryData.add(new DataExportCell("assessment_id", srh.getOrMiss(srh.getStrFromInt(assessment.getVeteranAssessmentId()))));
		mandatoryData.add(new DataExportCell("created_by", srh.getOrMiss(assessment.getCreatedByUser() != null ? assessment.getCreatedByUser().getUserFullName() : null)));
		mandatoryData.add(new DataExportCell("battery_name", srh.getOrMiss(assessment.getBattery() != null ? assessment.getBattery().getName() : null)));
		mandatoryData.add(new DataExportCell("program_name", srh.getOrMiss(assessment.getProgram() != null ? assessment.getProgram().getName() : null)));
		mandatoryData.add(new DataExportCell("vista_clinic", srh.getOrMiss(assessment.getClinic() != null ? assessment.getClinic().getName() : null)));
		mandatoryData.add(new DataExportCell("note_title", srh.getOrMiss(assessment.getNoteTitle() != null ? assessment.getNoteTitle().getName() : null)));
		mandatoryData.add(new DataExportCell("clinician_name", srh.getOrMiss(assessment.getClinician() != null ? assessment.getClinician().getUserFullName() : null)));
		mandatoryData.add(new DataExportCell("date_created", srh.getOrMiss(srh.getDtAsStr(assessment.getDateCreated()))));
		mandatoryData.add(new DataExportCell("time_created", srh.getOrMiss(srh.getTmAsStr(assessment.getDateCreated()))));
		mandatoryData.add(new DataExportCell("date_completed", srh.getOrMiss(srh.getDtAsStr(assessment.getDateCompleted()))));
		mandatoryData.add(new DataExportCell("time_completed", srh.getOrMiss(srh.getTmAsStr(assessment.getDateCompleted()))));
		mandatoryData.add(new DataExportCell("duration", srh.getOrMiss(srh.getStrFromInt(assessment.getDuration()))));

		mandatoryData.add(new DataExportCell("vista_DOB", show ? srh.getOrMiss(srh.getDtAsStr(assessment.getVeteran().getBirthDate())) : srh.miss()));
		return mandatoryData;
	}

	private List<DataExportCell> collectPpi(VeteranAssessment assessment,
			boolean show) {
		Veteran v = assessment.getVeteran();

		List<DataExportCell> mandatoryIdendifiedData = new ArrayList<DataExportCell>();

		// if veteran has taken the 'Identification' survey then skip this as veteran survey response
		// will take precedence over the clinician entered data

		mandatoryIdendifiedData.addAll(Arrays.asList(new DataExportCell("vista_lastname", show ? srh.getOrMiss(v.getLastName()) : srh.miss()),//
				new DataExportCell("vista_firstname", show ? srh.getOrMiss(v.getFirstName()) : srh.miss()),//
				new DataExportCell("vista_midname", show ? srh.getOrMiss(v.getMiddleName()) : srh.miss()),//
				new DataExportCell("vista_SSN", show ? srh.getOrMiss(v.getSsnLastFour()) : srh.miss()), new DataExportCell("vista_ien", v.getVeteranIen())));
		return mandatoryIdendifiedData;
	}

}