package gov.va.escreening.service;

import gov.va.escreening.condition.BlockUtil;
import gov.va.escreening.constants.TemplateConstants;
import gov.va.escreening.dto.TemplateTypeDTO;
import gov.va.escreening.dto.ae.ErrorBuilder;
import gov.va.escreening.dto.template.GraphParamsDto;
import gov.va.escreening.dto.template.INode;
import gov.va.escreening.dto.template.TemplateAssessmentVariableDTO;
import gov.va.escreening.dto.template.TemplateFileDTO;
import gov.va.escreening.dto.template.TemplateVariableContent;
import gov.va.escreening.entity.AssessmentVariable;
import gov.va.escreening.entity.Battery;
import gov.va.escreening.entity.Survey;
import gov.va.escreening.entity.Template;
import gov.va.escreening.entity.VariableTemplate;
import gov.va.escreening.exception.EntityNotFoundException;
import gov.va.escreening.exception.EscreeningDataValidationException;
import gov.va.escreening.exception.IllegalSystemStateException;
import gov.va.escreening.repository.AssessmentVariableRepository;
import gov.va.escreening.repository.BatteryRepository;
import gov.va.escreening.repository.SurveyRepository;
import gov.va.escreening.repository.TemplateRepository;
import gov.va.escreening.repository.TemplateTypeRepository;
import gov.va.escreening.repository.VariableTemplateRepository;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import static gov.va.escreening.templateprocessor.TemplateTags.*;

@Service
public class TemplateServiceImpl implements TemplateService {
	private static final Logger logger = LoggerFactory.getLogger(TemplateServiceImpl.class);
	private static final String GRAPHICAL_TEMPLATE_FORMAT = 
        "<#if %s?string != DEFAULT_VALUE >\n"
        + "${MODULE_TITLE_START}%s${MODULE_TITLE_END}\n"
        + "${GRAPH_SECTION_START}\n ${GRAPH_BODY_START}\n%s\n"
        + " ${GRAPH_BODY_END}\n${GRAPH_SECTION_END}\n";
	
	private static final Pattern GRAPHICAL_TEMPLATE_PATTERN = Pattern.compile( 
	        createTagRegex(Style.XML, MODULE_TITLE_START) 
	        + "+.+" + createTagRegex(Style.XML, GRAPH_SECTION_END) + "+");

	@Autowired
	private TemplateRepository templateRepository;

	@Autowired
	private TemplateTypeRepository templateTypeRepository;

	@Autowired
	private VariableTemplateRepository variableTemplateRepository;

	@Autowired
	private SurveyRepository surveyRepository;

	@Autowired
	private BatteryRepository batteryRepository;

	@Autowired
	private AssessmentVariableRepository avRepository;
	
	@Autowired
	AssessmentVariableService assessmentVariableService;

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public void deleteTemplate(Integer templateId) {
		Template template = templateRepository.findOne(templateId);

		if (template == null) {
			throw new IllegalArgumentException();
		}

		if (TemplateConstants.isSurveyTemplate(template
				.getTemplateType().getTemplateTypeId())) {
			// need to remove this template from associated survey
			List<Survey> surveys = surveyRepository
					.findByTemplateId(templateId);

			if (surveys != null && surveys.size() > 0) {
				for (Survey survey : surveys) {
					survey.getTemplates().remove(template);
					surveyRepository.update(survey);
				}
			}
		} else {
			// need to remove this template from associated battery

			// find the survey or battery
			List<Battery> batteries = batteryRepository
					.findByTemplateId(templateId);

			if (batteries != null && batteries.size() > 0) {
				for (Battery battery : batteries) {
					battery.getTemplates().remove(template);
					batteryRepository.update(battery);
				}
			}
		}

		templateRepository.deleteById(templateId);
	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	@Override
	public void addVariableTemplate(Integer templateId,
			Integer variableTemplateId) {
		Template template = templateRepository.findOne(templateId);
		VariableTemplate variableTemplate = variableTemplateRepository
				.findOne(variableTemplateId);
		template.getVariableTemplateList().add(variableTemplate);
		templateRepository.update(template);
	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	@Override
	public void addVariableTemplates(Integer templateId,
			List<Integer> variableTemplateIds) {
		Template template = templateRepository.findOne(templateId);
		List<VariableTemplate> variableTemplates = variableTemplateRepository
				.findByIds(variableTemplateIds);
		template.getVariableTemplateList().addAll(variableTemplates);
		templateRepository.update(template);
	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	@Override
	public void removeVariableTemplateFromTemplate(Integer templateId,
			Integer variableTemplateId) {
		Template template = templateRepository.findOne(templateId);
		VariableTemplate variableTemplate = variableTemplateRepository
				.findOne(variableTemplateId);
		template.getVariableTemplateList().remove(variableTemplate);
		templateRepository.update(template);
	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	@Override
	public void removeVariableTemplatesFromTemplate(Integer templateId,
			List<Integer> variableTemplateIds) {
		Template template = templateRepository.findOne(templateId);
		List<VariableTemplate> variableTemplates = variableTemplateRepository
				.findByIds(variableTemplateIds);
		template.getVariableTemplateList().removeAll(variableTemplates);
		template.setModifiedDate(new Date());
		templateRepository.update(template);
	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	@Override
	public void setVariableTemplatesToTemplate(Integer templateId,
			List<Integer> variableTemplateIds) {
		Template template = templateRepository.findOne(templateId);
		List<VariableTemplate> variableTemplates = variableTemplateRepository
				.findByIds(variableTemplateIds);
		template.setVariableTemplateList(variableTemplates);
		template.setModifiedDate(new Date());
		templateRepository.update(template);
	}	
	
	@Override
	public TemplateFileDTO getTemplateFileAsTree(Integer templateId) {
		
	    Template t = templateRepository.findOne(templateId);

	    if (t == null)
	        return null;

	    TemplateFileDTO dto = new TemplateFileDTO();

	    dto.setJson(t.getJsonFile());
	    ObjectMapper om = new ObjectMapper();
	    
	    if(t.getJsonFile() == null && t.getGraphParams() == null){
	        ErrorBuilder.throwing(EscreeningDataValidationException.class)
                .toAdmin("Template's json file is not defined. This means it is a handwritten template and cannot be edited with editor. ID: " + templateId)
                .toUser("The editing of this template version is unsupported.")
                .throwIt();
	    }

	    if (t.getJsonFile() != null) {
	        // now parsing the template file
	        try{
	            dto.setBlocks(Arrays.asList( om.readValue(t.getJsonFile(), INode[].class)));
	        }
	        catch(IOException e){
	            //TODO: see if we should throw an illegal system state exception here
	            logger.error("Error reading json file field as template blocks. Template ID: " + t.getTemplateId(), e);
	            return null;
	        }
	    }else{
	        dto.setBlocks(Collections.<INode>emptyList());
	    }

	    if(t.getGraphParams() != null){
	        try {
	            dto.setGraph(om.readValue(t.getGraphParams(), GraphParamsDto.class));
	        }
	        catch(IOException e){
	            //TODO: see if we should throw an illegal system state exception here
	            logger.error("Error reading json graphical template parameters. Template ID: " + t.getTemplateId(), e);
	            return null;
	        }
	    }
	    else{
	        dto.setGraph(new GraphParamsDto());
	    }

	    dto.setName(t.getName());
	    dto.setId(templateId);
	    dto.setIsGraphical(t.getIsGraphical());

	    TemplateTypeDTO ttDTO = new TemplateTypeDTO();
	    dto.setType(ttDTO);
	    ttDTO.setName(t.getTemplateType().getName());
	    ttDTO.setId(t.getTemplateType().getTemplateTypeId());
	    ttDTO.setDescription(t.getTemplateType().getDescription());

	    return dto;
	}
 
	@Override
	@Transactional(readOnly=false, propagation=Propagation.REQUIRED)
	public Integer saveTemplateFileForSurvey(Integer surveyId, Integer templateTypeId, TemplateFileDTO templateFile){
		
		Survey survey = surveyRepository.findOne(surveyId);
		
		Integer templateId = createTemplate(templateTypeId, templateFile, survey.getTemplates());
		
		surveyRepository.update(survey);
		
		return templateId;
	}
	
	@Override
	public GraphParamsDto getGraphParams(Template t) throws JsonParseException, JsonMappingException, IOException{
	    if(t.getGraphParams() == null){
	        return null;
	    }
    	
    	ObjectMapper om = new ObjectMapper();
    	return om.readValue(t.getGraphParams(), GraphParamsDto.class);
	}
	
	@Override
	public String replaceGraphWith(String templateOutput, String replacement){
	    return GRAPHICAL_TEMPLATE_PATTERN.matcher(templateOutput).replaceFirst(replacement);
	}

	private String generateFreeMarkerTemplateFile(TemplateFileDTO templateDto, Set<Integer>ids, String graphParams) {
	    List<INode> blocks = templateDto.getBlocks();
	    
	    StringBuilder file = new StringBuilder();
        file.append("<#include \"clinicalnotefunctions\">\n<#-- generated file. Do not change -->\n");
        
        if(templateDto.getIsGraphical()){
            file.append(String.format(GRAPHICAL_TEMPLATE_FORMAT, 
                    templateDto.getGraph().unwrappedScore(), 
                    templateDto.getName(), 
                    graphParams));
            if(templateDto.getGraph().getVarId() != null){
                ids.add(Integer.valueOf(templateDto.getGraph().getVarId()));
            }
        }

		file.append("${MODULE_START}\n");
		for(INode block : blocks){
			file = block.appendFreeMarkerFormat(file, ids, assessmentVariableService);
		}
		file.append("\n${MODULE_END}\n");
		
		if(templateDto.getIsGraphical()){
		    file.append("</#if>");
		}
		
		return file.toString();
	}

	@Override
	@Transactional(readOnly=false, propagation=Propagation.REQUIRED)
	public void updateTemplateFile(Integer templateId,
			TemplateFileDTO templateFile) {
		Template template = templateRepository.findOne(templateId);
		
		setTemplateFields(template, templateFile);
		
		templateRepository.update(template);
	}
	
	private void setTemplateFields(Template template, TemplateFileDTO templateFile){
	    gov.va.escreening.entity.TemplateType templateType = templateTypeRepository.findOne(templateFile.getType().getId());
        template.setTemplateType(templateType);
        
        template.setModifiedDate(new Date());
        
        template.setIsGraphical(templateFile.getIsGraphical());
        template.setName(templateFile.getName());
        ObjectMapper om = new ObjectMapper();
        
        // set json encoded graphical parameters
        if(templateFile.getIsGraphical()){
            
            //set the score field if needed
            if(templateFile.getGraph().getScore() == null){
                AssessmentVariable scoreVariable = avRepository.findOne(templateFile.getGraph().getVarId());
                if(scoreVariable == null){
                    ErrorBuilder.throwing(EntityNotFoundException.class)
                    .toAdmin("There is no variable with ID: " + templateFile.getGraph().getVarId())
                    .toUser("The selected variable for this graphical template is unknown")
                    .throwIt();
                }
                
                TemplateAssessmentVariableDTO scoreVarDto = new TemplateAssessmentVariableDTO(scoreVariable);
                TemplateVariableContent scoreContent = new TemplateVariableContent(scoreVarDto); 
                String scoreFreeMarker = BlockUtil.toFreeMarker(scoreContent, Sets.<Integer>newHashSetWithExpectedSize(1));
                templateFile.getGraph().setScore(scoreFreeMarker);
            }
            
            try{
                template.setGraphParams(om.writeValueAsString(templateFile.getGraph()));
            }
            catch(IOException e) {
                logger.error("Error setting graphical template parameters for new template", e); 
                template.setGraphParams(null);
            }
        }
        else{
            template.setGraphParams(null);
        }
        
        // save raw json file to the database
        if (templateFile.getBlocks() == null || templateFile.getBlocks().isEmpty()) {
            template.setJsonFile(null);
        }
        else {
            try{
                template.setJsonFile(om.writeValueAsString(templateFile.getBlocks()));
            }
            catch(IOException e){
                logger.error("Error setting json blocks for new template", e); 
                template.setJsonFile(null);
            }
         }
        
        //generate the FreeMarker
        Set<Integer> avIds = new HashSet<Integer>();
        template.setTemplateFile(generateFreeMarkerTemplateFile(templateFile, avIds, template.getGraphParams()));
        setVariableTemplates(template, avIds);
        
        if (template.getTemplateFile()==null){
            template.setTemplateFile("");
        }
	}
	
	/**
	 * 
	 * @param templateTypeId
	 * @param templateFile
	 * @param templates
	 * @return
	 * @throws IllegalSystemStateException if a measure was found which does not have an associated assessment variable
	 */
	private Integer createTemplate(Integer templateTypeId, TemplateFileDTO templateFile, Set<Template> templates ) throws IllegalSystemStateException{
		
		Template template = new Template();
		setTemplateFields(template, templateFile);
		
		/**
		 * for survey one template per type
		 */
		for(Template t : templates){
			if (t.getTemplateType().getTemplateTypeId().equals(templateFile.getType().getId())){
				templates.remove(t);
				break;
			}
		}
		
		templates.add(template);
		return template.getTemplateId();
	}

	/**
	 * 
	 * Adds VariableTemplate objects for the given set of assessment variable IDs
	 * @param template
	 * @param avIds
	 */
	private void setVariableTemplates(Template template, Set<Integer> avIds) {
	    //Iterate over previously saved AV and create maps
		Map<Integer, VariableTemplate> currentVtMap;
		Map<Integer, AssessmentVariable> varMap;
		
		if (template.getVariableTemplateList()==null){
		    currentVtMap = Collections.emptyMap();
		    varMap = Collections.emptyMap();
			template.setVariableTemplateList(new ArrayList<VariableTemplate>());
		}
		else{
			currentVtMap = Maps.newHashMapWithExpectedSize(template.getVariableTemplateList().size());
			varMap = Maps.newHashMapWithExpectedSize(template.getVariableTemplateList().size()); 
			for(VariableTemplate vt : template.getVariableTemplateList()){
			    AssessmentVariable variable = vt.getAssessmentVariableId();
				currentVtMap.put(variable.getAssessmentVariableId(), vt);
				varMap.put(variable.getAssessmentVariableId(), variable);
			}
			template.getVariableTemplateList().clear();
		}
		
		//Collect variables and added VariableTemplates to template
		for(AssessmentVariable variable : assessmentVariableService.collectAssociatedVars(avIds, varMap)){
		    VariableTemplate vt = currentVtMap.get(variable.getAssessmentVariableId());
		    if(vt == null){
		        vt = new VariableTemplate(variable, template);
		    }
		    template.getVariableTemplateList().add(vt);
		}
	}

	@Override
	@Transactional(readOnly=false, propagation=Propagation.REQUIRED)
	public Integer saveTemplateFileForBattery(Integer batteryId,
			Integer templateTypeId, TemplateFileDTO templateFile) {
		Battery battery = batteryRepository.findOne(batteryId);
		
		Integer templateId = createTemplate(templateTypeId, templateFile, battery.getTemplates());
		batteryRepository.update(battery);
		
		return templateId;
	}
}
