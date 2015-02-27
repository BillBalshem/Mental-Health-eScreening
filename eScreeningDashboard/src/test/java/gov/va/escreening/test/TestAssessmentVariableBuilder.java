package gov.va.escreening.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import gov.va.escreening.delegate.CreateAssessmentDelegate;
import gov.va.escreening.domain.RoleEnum;
import gov.va.escreening.entity.AssessmentVarChildren;
import gov.va.escreening.entity.AssessmentVariable;
import gov.va.escreening.entity.AssessmentVariableType;
import gov.va.escreening.entity.Measure;
import gov.va.escreening.entity.MeasureAnswer;
import gov.va.escreening.entity.MeasureAnswerValidation;
import gov.va.escreening.entity.MeasureType;
import gov.va.escreening.entity.MeasureValidation;
import gov.va.escreening.entity.SurveyMeasureResponse;
import gov.va.escreening.entity.SystemProperty;
import gov.va.escreening.entity.User;
import gov.va.escreening.entity.Validation;
import gov.va.escreening.entity.VariableTemplate;
import gov.va.escreening.entity.Veteran;
import gov.va.escreening.entity.VeteranAssessment;
import gov.va.escreening.exception.CouldNotResolveVariableException;
import gov.va.escreening.expressionevaluator.ExpressionEvaluatorService;
import gov.va.escreening.repository.SurveyMeasureResponseRepository;
import gov.va.escreening.repository.UserRepository;
import gov.va.escreening.service.SystemPropertyService;
import gov.va.escreening.service.VeteranAssessmentService;
import gov.va.escreening.variableresolver.AssessmentVariableDto;
import gov.va.escreening.variableresolver.AssessmentVariableDtoFactory;
import gov.va.escreening.variableresolver.AssessmentVariableDtoFactoryImpl;
import gov.va.escreening.variableresolver.CustomAssessmentVariableResolver;
import gov.va.escreening.variableresolver.CustomAssessmentVariableResolverImpl;
import gov.va.escreening.variableresolver.FormulaAssessmentVariableResolver;
import gov.va.escreening.variableresolver.FormulaAssessmentVariableResolverImpl;
import gov.va.escreening.variableresolver.MeasureAnswerAssessmentVariableResolver;
import gov.va.escreening.variableresolver.MeasureAnswerAssessmentVariableResolverImpl;
import gov.va.escreening.variableresolver.MeasureAssessmentVariableResolver;
import gov.va.escreening.variableresolver.MeasureAssessmentVariableResolverImpl;
import gov.va.escreening.vista.dto.VistaVeteranAppointment;
import static org.mockito.Mockito.*;
import static com.google.common.base.Preconditions.*;
import static gov.va.escreening.constants.AssessmentConstants.*;

/**
 * Root {@link AssessmentVariableBuilder} used for integration testing.
 * 
 * @author Robin Carnow
 *
 */
public class TestAssessmentVariableBuilder implements AssessmentVariableBuilder{
    //if this value changes in ClinicalNoteTemplateFunctions.ftl then it has to change here.
    public static final String DEFAULT_VALUE = "notset";

    private static final Logger logger = LoggerFactory.getLogger(TestAssessmentVariableBuilder.class); 
    private static final AssessmentVariableType TYPE_ANSWER = new AssessmentVariableType(ASSESSMENT_VARIABLE_TYPE_MEASURE_ANSWER);
    private static final AssessmentVariableType TYPE_MEASURE = new AssessmentVariableType(ASSESSMENT_VARIABLE_TYPE_MEASURE);
    private static final AssessmentVariableType TYPE_CUSTOM = new AssessmentVariableType(ASSESSMENT_VARIABLE_TYPE_CUSTOM);

    //Tracks what is added/built
    private final Map<Integer, AssessmentVariable> avMap = new HashMap<>();
    private final Map<Integer, AssessmentVariable> measureAnswerHash = new HashMap<>();

    //resolvers - used to make sure all business rules are followed
    private final MeasureAnswerAssessmentVariableResolver answerResolver;
    private final MeasureAssessmentVariableResolver measureResolver;
    private final CustomAssessmentVariableResolver customResolver;
    private final FormulaAssessmentVariableResolver formulaResolver;
    private final AssessmentVariableDtoFactory variableResolver;

    //mocked supporting objects for resolvers
    private final SurveyMeasureResponseRepository surveyResponseRepo = mock(SurveyMeasureResponseRepository.class);
    private final ExpressionEvaluatorService expressionService = mock(ExpressionEvaluatorService.class);
    private final VeteranAssessment assessment = mock(VeteranAssessment.class);
    private final Veteran vet = mock(Veteran.class);
    private final List<VistaVeteranAppointment>appointments = new ArrayList<>();

    //used for default ID values
    private int measureId = 1;
    private int answerId = 1;
    private int avId = 1000;

    public TestAssessmentVariableBuilder(){
        answerResolver = new MeasureAnswerAssessmentVariableResolverImpl(surveyResponseRepo);
        measureResolver = new MeasureAssessmentVariableResolverImpl(answerResolver, surveyResponseRepo);
        customResolver = createCustomResolver();
        formulaResolver = new FormulaAssessmentVariableResolverImpl(expressionService, answerResolver, measureResolver);
        variableResolver = new AssessmentVariableDtoFactoryImpl(customResolver, formulaResolver, answerResolver, measureResolver); 
    }

    @Override
    public List<AssessmentVariableDto> getDTOs(){
        List<AssessmentVariableDto> dtoList = new ArrayList<>(avMap.size());
        for(AssessmentVariable av : avMap.values()){
            try{
                dtoList.add(variableResolver.createAssessmentVariableDto(av, 123, measureAnswerHash));
            }
            catch(CouldNotResolveVariableException e){
                /*ignored*/
                logger.warn(e.getLocalizedMessage());
            }
        }
        return dtoList;
    }

    @Override
    public FreeTextAvBuilder addFreeTextAV(int avId, String questionText, @Nullable String response){
        return new FreeTextAvBuilder(this, avId, questionText, response);
    }

    @Override
    public SelectAvBuilder addSelectOneAV(@Nullable Integer avId, @Nullable String questionText){
        return new SelectAvBuilder(this, MEASURE_TYPE_SELECT_ONE, avId, questionText);
    }

    @Override
    public SelectAvBuilder addSelectMultiAV(@Nullable Integer avId, @Nullable String questionText){
        return new SelectAvBuilder(this, MEASURE_TYPE_SELECT_MULTI, avId, questionText);
    }

    @Override
    public MatrixAvBuilder addSelectOneMatrixAV(@Nullable Integer avId, @Nullable String questionText){
        return new MatrixAvBuilder(this, MEASURE_TYPE_SELECT_ONE_MATRIX, avId, questionText);
    }

    @Override
    public MatrixAvBuilder addSelectMultiMatrixAV(@Nullable Integer avId, @Nullable String questionText){
        return new MatrixAvBuilder(this, MEASURE_TYPE_SELECT_MULTI_MATRIX, avId, questionText);
    }

    @Override
    public TableQuestionAvBuilder addTableQuestionAv(@Nullable Integer avId, @Nullable String questionText, 
            boolean hasNone, @Nullable Boolean noneResponse){

        return new TableQuestionAvBuilder(this, avId, questionText, hasNone, noneResponse);
    }

    @Override
    public CustomAvBuilder addCustomAV(int avId){
        return new CustomAvBuilder(this, avId);
    }


    //Builder methods 

    /**
     * It is important to note that calling this a second time with the same measure ID will overwrite the previous call.
     * @param measureId
     * @param smrs
     */
    private void setMeasureResponses(Measure measure, @Nullable Integer row, SurveyMeasureResponse... smrs){
        List<SurveyMeasureResponse> responseList;
        Integer measureId = measure.getMeasureId();

        if(smrs == null){ 
            responseList = Collections.emptyList();
        }
        else{ 
            responseList = Arrays.asList(smrs); 
        }

        //set the list on the measure
        measure.setSurveyMeasureResponseList(responseList);

        //setup service calls
        when(surveyResponseRepo.getForVeteranAssessmentAndMeasure(anyInt(), eq(measureId))).thenReturn(responseList);

        if(row != null){
            when(surveyResponseRepo.findForAssessmentIdMeasureRow(anyInt(), eq(measureId), eq(row))).thenReturn(responseList);
        }
    }

    private void addMeasureResponse(Measure measure, @Nullable Integer row, SurveyMeasureResponse response){
        List<SurveyMeasureResponse> responseList;
        Integer measureId = measure.getMeasureId();

        if(row != null){
            responseList = surveyResponseRepo.findForAssessmentIdMeasureRow(4, measureId, row);
        }
        else{
            responseList = surveyResponseRepo.getForVeteranAssessmentAndMeasure(4, measureId);	
        }

        if(responseList == null){
            responseList = new LinkedList<>();
        }

        responseList.add(response);

        //set the list on the measure
        measure.setSurveyMeasureResponseList(responseList);

        //setup service calls
        when(surveyResponseRepo.getForVeteranAssessmentAndMeasure(anyInt(), eq(measureId))).thenReturn(responseList);

        if(row != null){
            when(surveyResponseRepo.findForAssessmentIdMeasureRow(anyInt(), eq(measureId), eq(row))).thenReturn(responseList);
        }
    }

    private void setMeasureResponses(Measure measure,  SurveyMeasureResponse... smrs){
        setMeasureResponses(measure, null, smrs);
    }

    private void addMeasureResponse(Measure measure, SurveyMeasureResponse response){
        addMeasureResponse(measure, null, response);
    }

    private AssessmentVariable newAssessmentVariable(@Nullable Integer id, AssessmentVariableType type){
        AssessmentVariable var = id == null ? new AssessmentVariable(avId++) : new AssessmentVariable(id);
        List<VariableTemplate> vtList = new ArrayList<>();
        vtList.add(new VariableTemplate());
        var.setVariableTemplateList(vtList);
        var.setAssessmentVarChildrenList(new ArrayList<AssessmentVarChildren>());
        var.setAssessmentVariableTypeId(type);
        return var;
    }

    private Measure newMeasure(Integer type, String text, MeasureAnswer... maList){
        Measure m = new Measure(measureId++);
        m.setMeasureType(new MeasureType(type));
        m.setMeasureText(text);
        m.setMeasureAnswerList(new ArrayList<MeasureAnswer>());
        m.setAssessmentVariableList(new ArrayList<AssessmentVariable>());

        for(MeasureAnswer ma : maList){
            if(ma != null){
                ma.setMeasure(m);
                m.getMeasureAnswerList().add(ma);
            }
        }
        return m;
    }

    private MeasureAnswer newMeasureAnswer(){
        MeasureAnswer ma = new MeasureAnswer(answerId++);
        ma.setAssessmentVariableList(new ArrayList<AssessmentVariable>());
        ma.setMeasureAnswerValidationList(new ArrayList<MeasureAnswerValidation>());
        return ma;
    }

    /**
     * Interface for builders which create questions (aka measures)
     * 
     * @author Robin Carnow
     *
     */
    public interface MeasureAvBuilder{
        public Measure getMeasure();
    }

    /**
     * An AV builder class for the building of Table type question AVs
     * 
     * @author Robin Carnow
     *
     */
    public class TableQuestionAvBuilder extends ForwardingAssessmentVariableBuilder{
        private final Measure tableMeasure;
        private MeasureAvBuilder currentBuilder = null;
        private FreeTextAvBuilder currentFreeTextBuilder = null;
        private SelectAvBuilder currentSelectBuilder = null;

        private TableQuestionAvBuilder(AssessmentVariableBuilder rootBuilder,  
                @Nullable Integer avId, 
                @Nullable String questionText, 
                boolean hasNone, 
                @Nullable Boolean noneResponse){

            super(rootBuilder);

            MeasureAnswer answer = null;
            if(hasNone){
                answer = newMeasureAnswer();
                answer.setAnswerType("none");
            }

            tableMeasure = newMeasure(MEASURE_TYPE_TABLE, questionText, answer);

            //create av for measure
            AssessmentVariable av = newAssessmentVariable(avId, TYPE_MEASURE);
            av.setMeasure(tableMeasure);
            tableMeasure.getAssessmentVariableList().add(av);
            avMap.put(av.getAssessmentVariableId(), av);

            if(hasNone){
                //create av for none answer
                AssessmentVariable aav = newAssessmentVariable(null, TYPE_ANSWER);
                aav.setMeasureAnswer(answer);
                answer.getAssessmentVariableList().add(aav);
                measureAnswerHash.put(answer.getMeasureAnswerId(), aav);

                //create response
                if(noneResponse != null){
                    SurveyMeasureResponse srm = new SurveyMeasureResponse();
                    srm.setBooleanValue(noneResponse);
                    srm.setMeasure(tableMeasure);
                    srm.setMeasureAnswer(answer);
                    setMeasureResponses(tableMeasure, srm);
                }
            }
        }

        /**
         * 
         * @param avId
         * @param measureText
         * @param responses a list containing a response for each entry in this table. If 
         * an entry in the list is null, that means for that entry no response was given by the veteran.
         * @return this builder for chaining
         */
        public TableQuestionAvBuilder addChildFreeText(@Nullable Integer avId, 
                @Nullable String measureText, @Nullable List<String> responses){
            FreeTextAvBuilder childBuilder = new FreeTextAvBuilder(getRootBuilder(), avId, measureText, null);
            currentFreeTextBuilder = childBuilder;
            currentBuilder = childBuilder;

            Measure childMeasure = childBuilder.getMeasure();

            //associate child with this parent
            childMeasure.setParent(tableMeasure);
            tableMeasure.getChildren().add(childMeasure);

            //create responses
            if(responses != null){
                int rowIndex = 0;
                for(String response : responses){
                    if(response != null){
                        SurveyMeasureResponse srm = new SurveyMeasureResponse();
                        srm.setTextValue(response);
                        srm.setMeasure(childMeasure);
                        srm.setMeasureAnswer(childMeasure.getMeasureAnswerList().get(0));
                        srm.setTabularRow(rowIndex);
                        setMeasureResponses(childMeasure, rowIndex, srm);
                    }
                    rowIndex++;
                }
            }

            return this;
        }		

        /**
         * This will set the data type validation of the last free text AV added (via addChildFreeText)
         * @param format
         * @return this builder for chaining
         */
        public TableQuestionAvBuilder setDataTypeValidation(String format){
            if(currentFreeTextBuilder == null){
                throw new IllegalStateException("Only after a freetext AV has been added to this table can you set a validation to it.");
            }

            currentFreeTextBuilder.setDataTypeValidation(format);
            return this;
        }

        /**
         * Adds a child select one question
         * @param avId
         * @param questionText
         * @return this builder for chaining
         */
        public TableQuestionAvBuilder addChildSelectOne(@Nullable Integer avId, @Nullable String questionText){
            return addChildSelect(MEASURE_TYPE_SELECT_ONE, avId, questionText);
        }

        /**
         * Adds a child select multi question
         * @param avId
         * @param questionText
         * @return this builder for chaining
         */
        public TableQuestionAvBuilder addChildSelectMulti(@Nullable Integer avId, @Nullable String questionText){
            return addChildSelect(MEASURE_TYPE_SELECT_MULTI, avId, questionText);
        }

        /**
         * This method will add an answer to the <b>last select question added</b> (via addChildSelectOne or addChildSelectMulti).
         * @param avId
         * @param answerText
         * @param answerType
         * @param calculationValue
         * @param responses the list of responses (one per entry). Please note that for select type questions if all answers
         * are false for a give answer, then no answers will be given to the template for the question. An answer will all 
         * false answers amounts to an unanswered question, which in the context of a table question will change the entry count.
         * @param otherTextResponses this should be the same size as responses or null.  For each entry in 
         * responses (even null ones) this list can have a value which will be set on the answer for that entry.
         * @return this builder for chaining
         */
        public TableQuestionAvBuilder addChildSelectAnswer(
                @Nullable Integer avId, 
                @Nullable String answerText, 
                @Nullable String answerType, 
                @Nullable Double calculationValue,
                @Nullable List<Boolean> responses,
                @Nullable List<String> otherTextResponses){

            checkState(currentSelectBuilder != null, "A select question must be added (via a call to addChildSelectOne or addChildSelectMulti) before adding an answer");			

            currentSelectBuilder.addAnswer(avId, answerText, answerType, calculationValue, null, null);
            Measure childMeasure = currentSelectBuilder.getMeasure();
            MeasureAnswer addedAnswer = childMeasure.getMeasureAnswerList().get(childMeasure.getMeasureAnswerList().size()-1);

            //create responses for each table entry
            if(responses != null){
                int rowIndex = 0;
                Iterator<String> otherItr = otherTextResponses != null ? otherTextResponses.iterator() : null;
                for(Boolean response : responses){
                    String otherValue = otherItr != null && otherItr.hasNext() ? otherItr.next() : null;

                    if(response != null){
                        SurveyMeasureResponse srm = new SurveyMeasureResponse();
                        srm.setBooleanValue(response);
                        srm.setOtherValue(otherValue);
                        srm.setMeasure(childMeasure);
                        srm.setMeasureAnswer(addedAnswer);
                        srm.setTabularRow(rowIndex);
                        addMeasureResponse(childMeasure, rowIndex, srm);
                    }
                    rowIndex++;
                }
            }

            return this;
        }

        /**
         * @return the builder for the last child question added.
         */
        public MeasureAvBuilder getCurrentChildBuilder(){
            return currentBuilder;
        }

        private TableQuestionAvBuilder addChildSelect(Integer measureTypeId, 
                @Nullable Integer avId, @Nullable String questionText){

            SelectAvBuilder childBuilder = new SelectAvBuilder(getRootBuilder(), measureTypeId, avId, questionText);
            currentSelectBuilder = childBuilder;
            currentBuilder = childBuilder;

            Measure childMeasure = childBuilder.getMeasure();

            //associate child with this parent
            childMeasure.setParent(tableMeasure);
            tableMeasure.getChildren().add(childMeasure);

            return this;
        }
    }

    public class SelectAvBuilder extends ForwardingAssessmentVariableBuilder implements MeasureAvBuilder {
        private final Measure measure;
        private final AssessmentVariable av;
        private Double defaultCalculationValue = 0.0;
        private List<AssessmentVariable> answerAvs = new ArrayList<>();

        private SelectAvBuilder(AssessmentVariableBuilder rootBuilder, Integer measureTypeId, 
                @Nullable Integer avId, @Nullable String questionText){
            super(rootBuilder);

            if(measureTypeId != MEASURE_TYPE_SELECT_ONE 
                    && measureTypeId != MEASURE_TYPE_SELECT_MULTI){
                throw new IllegalArgumentException("measure type ID must be a select");
            }

            measure = newMeasure(measureTypeId, questionText);

            av = newAssessmentVariable(avId, TYPE_MEASURE);
            av.setMeasure(measure);
            measure.getAssessmentVariableList().add(av);
            avMap.put(av.getAssessmentVariableId(), av);
        }

        @Override
        public Measure getMeasure(){
            return measure;
        }

        /**
         * Adds an answer to this select question
         * @param avId
         * @param answerText
         * @param answerType should be "other", "none", or null
         * @param calculationValue 
         * @param response
         * @param otherTextResponse if the type of this answer is other this response is the veteran's entered text
         * @return
         */
        public SelectAvBuilder addAnswer(@Nullable Integer avId, 
                @Nullable String answerText, 
                @Nullable String answerType, 
                @Nullable Double calculationValue,
                @Nullable Boolean response,
                @Nullable String otherTextResponse){

            MeasureAnswer answer = newMeasureAnswer();
            answer.setAnswerText(answerText);
            answer.setAnswerType(answerType);
            answer.setCalculationValue(calculationValue != null ? calculationValue.toString() : defaultCalculationValue.toString());
            defaultCalculationValue++;

            measure.getMeasureAnswerList().add(answer);

            //create av for answer
            AssessmentVariable aav = newAssessmentVariable(avId, TYPE_ANSWER);
            aav.setMeasureAnswer(answer);
            answer.getAssessmentVariableList().add(aav);
            measureAnswerHash.put(answer.getMeasureAnswerId(), aav);

            answerAvs.add(aav);

            //create response
            if(response != null){
                SurveyMeasureResponse srm = new SurveyMeasureResponse();
                srm.setBooleanValue(response);
                srm.setMeasure(measure);
                srm.setMeasureAnswer(answer);
                srm.setOtherValue(otherTextResponse);
                addMeasureResponse(measure, srm);
            }

            return this;
        }

        public List<AssessmentVariable> getAnswerAvs(){
            return ImmutableList.copyOf(answerAvs);
        }
    }


    public class MatrixAvBuilder extends ForwardingAssessmentVariableBuilder{
        private final Measure matrixMeasure;
        private final AssessmentVariable av;
        private final List<SelectAvBuilder> childBuilders = new LinkedList<>();
        private final List<MeasureAnswer> columns = new LinkedList<>();

        /**
         * 
         * @param rootBuilder
         * @param measureTypeId should be either Assessment
         * @param avId assessment variable ID for the matrix
         * @param questionText
         */
        private MatrixAvBuilder(AssessmentVariableBuilder rootBuilder, Integer measureTypeId, 
                @Nullable Integer avId, @Nullable String questionText){
            super(rootBuilder);

            if(measureTypeId != MEASURE_TYPE_SELECT_ONE_MATRIX 
                    && measureTypeId != MEASURE_TYPE_SELECT_MULTI_MATRIX){
                throw new IllegalArgumentException("measure type ID must be a matrix");
            }

            matrixMeasure = newMeasure(measureTypeId, questionText);
            matrixMeasure.setChildren(new HashSet<Measure>());

            av = newAssessmentVariable(avId, TYPE_MEASURE);
            av.setMeasure(matrixMeasure);
            matrixMeasure.getAssessmentVariableList().add(av);
            avMap.put(av.getAssessmentVariableId(), av);
        }

        /**
         * Adds a new child question to the matrix.  If this is called after a column was added, answers will be added but
         * no responses will be set.
         * @param childQuestionAvId optional AV ID for the question
         * @param questionText optional question text
         * @return this MatrixAVBuilder
         */
        public MatrixAvBuilder addChildQuestion(@Nullable Integer childQuestionAvId, @Nullable String questionText){
            Integer childMeasureTypeId = matrixMeasure.getMeasureType().getMeasureTypeId() ==  MEASURE_TYPE_SELECT_ONE_MATRIX 
                    ?  MEASURE_TYPE_SELECT_ONE :  MEASURE_TYPE_SELECT_MULTI;

            SelectAvBuilder selectBuilder = new SelectAvBuilder(getRootBuilder(), childMeasureTypeId, childQuestionAvId, questionText);
            childBuilders.add(selectBuilder);

            //add any answers that have been defined
            for(MeasureAnswer col : columns){
                selectBuilder.addAnswer(null, col.getAnswerText(), null, 
                        col.getCalculationValue() == null ? null: Double.valueOf(col.getCalculationValue()), null, null);
            }

            //associate child with this parent
            Measure childMeasure = selectBuilder.getMeasure();
            childMeasure.setParent(matrixMeasure);
            matrixMeasure.getChildren().add(childMeasure);

            return this;
        }

        /**
         * Adds a new column to this matrix which means that every question which was added will have a new answer added.
         * If responses should be set then all questions should be added first and then columns.
         * @param answerText the column's (answer's) text shown to the veteran
         * @param answerType should be null, "none", or "other"
         * @param calculationValue 
         * @param responses this should be null or have a response for each question that was added to this matrix.
         * The boolean indicates whether the option is selected. 
         * @param otherResponses an optional list of the veteran's other text for the column. Represents the "other" text value
         * the veteran entered into the answer. In the context of a matrix question, other answers are used to allow the 
         * veteran to set the question they are answering (because there are added topics that the other matrix questions didn't 
         * cover). So be careful with the other type especially since it is normally only a couple of questions and not all of them.
         * 
         * @return
         */
        public MatrixAvBuilder addColumn(
                @Nullable String answerText,  
                @Nullable String answerType, 
                @Nullable Double calculationValue, 
                @Nullable Iterable<Boolean> responses,
                @Nullable Iterable<String> otherResponses){

            //save in case a new question is added after this
            MeasureAnswer ma = new MeasureAnswer();
            ma.setAnswerText(answerText);
            ma.setCalculationValue(calculationValue != null ? calculationValue.toString(): null);
            columns.add(ma);

            Iterator<Boolean> responseIter = responses == null ? null : responses.iterator();
            Iterator<String> otherResponseIter = otherResponses == null ? null : otherResponses.iterator();
            for(SelectAvBuilder childBuilder : childBuilders){
                Boolean response = responseIter != null && responseIter.hasNext() ? responseIter.next() : null;
                String otherResponse = otherResponseIter != null && otherResponseIter.hasNext() ? otherResponseIter.next() : null;
                childBuilder.addAnswer(null, answerText, answerType, calculationValue, response, otherResponse);
            }

            return this;
        }

        public List<Integer> getColumnAvs(Integer... columnIndices) {
            List<Integer> columnAvs = new ArrayList<>(childBuilders.size() * columnIndices.length);
            for(SelectAvBuilder childBuilder : childBuilders){
                List<AssessmentVariable> childAvs = childBuilder.getAnswerAvs();
                for(Integer index : columnIndices){
                    columnAvs.add(childAvs.get(index).getAssessmentVariableId());
                }
            }
            return columnAvs;
        }

    }

    public class FreeTextAvBuilder extends ForwardingAssessmentVariableBuilder implements MeasureAvBuilder{
        private final Measure measure;

        private FreeTextAvBuilder(AssessmentVariableBuilder rootBuilder, 
                @Nullable Integer avId, 
                @Nullable String measureText, 
                @Nullable String response){
            super(rootBuilder);

            //create a measure
            measure = newFreeTextMeasure(measureText);
            MeasureAnswer answer = measure.getMeasureAnswerList().get(0);

            //create av for measure
            AssessmentVariable av = newAssessmentVariable(avId, TYPE_MEASURE);
            av.setMeasure(measure);
            measure.getAssessmentVariableList().add(av);
            avMap.put(av.getAssessmentVariableId(), av);

            //create av for answer
            AssessmentVariable aav = newAssessmentVariable(null, TYPE_ANSWER);
            aav.setMeasureAnswer(answer);
            answer.getAssessmentVariableList().add(aav);
            measureAnswerHash.put(answer.getMeasureAnswerId(), aav);

            //create response
            if(response != null){
                SurveyMeasureResponse srm = new SurveyMeasureResponse();
                srm.setTextValue(response);
                srm.setMeasure(measure);
                srm.setMeasureAnswer(answer);
                setMeasureResponses(measure, srm);
            }
        }

        public FreeTextAvBuilder setDataTypeValidation(String format){
            if(format != "email" && format != "number" && format != "date"){
                throw new IllegalArgumentException("Format is invalid. Should be email, number of date");
            }
            Validation validation = new Validation(1, "dataType", "", "string", null);
            MeasureValidation mv = new MeasureValidation();
            mv.setValidation(validation);
            mv.setMeasure(measure);
            mv.setTextValue(format);
            measure.getMeasureValidationList().add(mv);

            return this;
        }

        @Override
        public Measure getMeasure(){
            return measure;
        }

        private Measure newFreeTextMeasure(String text){
            Measure m = newMeasure(MEASURE_TYPE_FREE_TEXT, text, newMeasureAnswer());
            m.setMeasureValidationList(new ArrayList<MeasureValidation>());
            return m;
        }
    }

    public class CustomAvBuilder extends ForwardingAssessmentVariableBuilder{

        private CustomAvBuilder(AssessmentVariableBuilder rootBuilder, Integer customAvId){
            super(rootBuilder);

            checkNotNull(customAvId, "For custom assessment variables, a constant ID from CustomAssessmentVariableResolver, is required");

            AssessmentVariable av = newAssessmentVariable(customAvId, TYPE_CUSTOM);
            avMap.put(customAvId, av);
        }

        public CustomAvBuilder addAppointments(VistaVeteranAppointment... appointments){
            for(VistaVeteranAppointment appointment : appointments){
                addAppointment(appointment);
            }
            return this;
        }

        public CustomAvBuilder addAppointment(VistaVeteranAppointment appointment){
            if(appointment != null){
                addVeteranAppointment(appointment);
            }
            return this;
        }

        /**
         * Adds an appointment used by the AV with ID: {@link CustomAssessmentVariableResolver.CUSTOM_VETERAN_APPOINTMENTS}
         * @param clinicName
         * @param date
         * @param status
         * @return
         */
        public CustomAvBuilder addAppointment(String clinicName, Date date, String status){
            VistaVeteranAppointment appointment = new VistaVeteranAppointment();
            appointment.setClinicName(clinicName);
            appointment.setAppointmentDate(date);
            appointment.setStatus(status);

            addVeteranAppointment(appointment);
            return this;
        }

        /**
         * Set the assigned clinician for this assessment (used by custom variables)
         * @param user
         * @return this builder
         */
        public CustomAvBuilder setClinician(User user){
            when(assessment.getClinician()).thenReturn(user);
            return this;
        }

        /**
         * Set the duration of the assessment (used by custom variables)
         * @param durationSeconds
         * @return this builder
         */
        public CustomAvBuilder setAssessmentDuration(int durationSeconds){
            when(assessment.getDuration()).thenReturn(durationSeconds);
            return this;
        }

        /**
         * Set the veteran who took this assessment (used by custom variables)
         * @param vet
         * @return this builder
         */
        public  CustomAvBuilder setVeteran(Veteran vet){
            when(assessment.getVeteran()).thenReturn(vet);
            return this;
        }

        private void addVeteranAppointment(VistaVeteranAppointment vva){
            appointments.add(vva);
        }
    }

    private CustomAssessmentVariableResolver createCustomResolver(){
        //setup packet version lookup
        SystemPropertyService sps = mock(SystemPropertyService.class);
        SystemProperty sp = mock(SystemProperty.class);
        when(sp.getTextValue()).thenReturn("test.version.number");
        when(sps.findBySystemPropertyId(anyInt())).thenReturn(sp);

        //setup assessment 
        VeteranAssessmentService vas = mock(VeteranAssessmentService.class);
        when(vas.findByVeteranAssessmentId(anyInt())).thenReturn(assessment);		
        when(vet.getVeteranIen()).thenReturn("123");
        when(assessment.getVeteran()).thenReturn(vet);

        //setup appointments
        UserRepository ur = mock(UserRepository.class);
        User mockUser = mock(User.class);
        when(ur.findByRoleId(RoleEnum.TECH_ADMIN)).thenReturn(Lists.newArrayList(mockUser));

        CreateAssessmentDelegate cad = mock(CreateAssessmentDelegate.class);
        when(cad.getVeteranAppointments(eq(mockUser), anyString())).thenReturn(appointments);

        return new CustomAssessmentVariableResolverImpl(sps, vas, ur, cad);
    }	
}
