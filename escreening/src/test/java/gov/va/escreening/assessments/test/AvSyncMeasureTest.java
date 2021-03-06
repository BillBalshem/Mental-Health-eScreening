package gov.va.escreening.assessments.test;

import gov.va.escreening.service.AssessmentVariableService;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import com.google.gson.Gson;

@Transactional
// this is to ensure all tests do not leave trace, so they are repeatable.
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "file:src/main/webapp/WEB-INF/spring/root-context.xml" })
public class AvSyncMeasureTest {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Resource(type = AssessmentVariableService.class)
	AssessmentVariableService avs;

	@Test
	public void testAssessmentVariablesForSurveyId1() {
		Table<String, String, Object> assessments = avs.getAssessmentVarsForSurvey(1,true,false);
		showAssessmentsAsJson(1, assessments);
	}

	@Test
	public void testAssessmentVariablesForSurveyId2() {
		Table<String, String, Object> assessments = avs.getAssessmentVarsForSurvey(2,true,false);
		showAssessmentsAsJson(2, assessments);
	}

	//@Test
	public void testAssessmentVariablesForSurveyId3() {
		Table<String, String, Object> assessments = avs.getAssessmentVarsForSurvey(3,true,false);
		showAssessmentsAsJson(3, assessments);
	}

	@Test
	public void testAssessmentVariablesForSurveyId4() {
		Table<String, String, Object> assessments = avs.getAssessmentVarsForSurvey(4,true,false);
		showAssessmentsAsJson(4, assessments);
	}

	//@Test
	public void testAssessmentVariablesForSurveyId5() {
		Table<String, String, Object> assessments = avs.getAssessmentVarsForSurvey(5,true,false);
		showAssessmentsAsJson(5, assessments);
	}

	@Test
	public void testAssessmentVariablesForSurveyId6() {
		Table<String, String, Object> assessments = avs.getAssessmentVarsForSurvey(6,true,false);
		showAssessmentsAsJson(6, assessments);
	}

	//@Test
	public void testAssessmentVariablesForSurveyId7() {
		Table<String, String, Object> assessments = avs.getAssessmentVarsForSurvey(7,true,false);
		showAssessmentsAsJson(7, assessments);
	}

	@Test
	public void testAssessmentVariablesForSurveyId8() {
		Table<String, String, Object> assessments = avs.getAssessmentVarsForSurvey(8,true,false);
		showAssessmentsAsJson(8, assessments);
	}

	//@Test
	public void testAssessmentVariablesForSurveyId9() {
		Table<String, String, Object> assessments = avs.getAssessmentVarsForSurvey(9,true,false);
		showAssessmentsAsJson(9, assessments);
	}

	@Test
	public void testAssessmentVariablesForSurveyId10() {
		Table<String, String, Object> assessments = avs.getAssessmentVarsForSurvey(10,true,false);
		showAssessmentsAsJson(10, assessments);
	}

	//@Test
	public void testAssessmentVariablesForSurveyId11() {
		Table<String, String, Object> assessments = avs.getAssessmentVarsForSurvey(11,true,false);
		showAssessmentsAsJson(11, assessments);
	}

	@Test
	public void testAssessmentVariablesForSurveyId12() {
		Table<String, String, Object> assessments = avs.getAssessmentVarsForSurvey(12,true,false);
		showAssessmentsAsJson(12, assessments);
	}

	//@Test
	public void testAssessmentVariablesForSurveyId13() {
		Table<String, String, Object> assessments = avs.getAssessmentVarsForSurvey(13,true,false);
		showAssessmentsAsJson(13, assessments);
	}

	@Test
	public void testAssessmentVariablesForSurveyId14() {
		Table<String, String, Object> assessments = avs.getAssessmentVarsForSurvey(14,true,false);
		showAssessmentsAsJson(14, assessments);
	}

	//@Test
	public void testAssessmentVariablesForSurveyId15() {
		Table<String, String, Object> assessments = avs.getAssessmentVarsForSurvey(15,true,false);
		showAssessmentsAsJson(15, assessments);
	}

	@Test
	public void testAssessmentVariablesForSurveyId16() {
		Table<String, String, Object> assessments = avs.getAssessmentVarsForSurvey(16,true,false);
		showAssessmentsAsJson(16, assessments);
	}

	//@Test
	public void testAssessmentVariablesForSurveyId17() {
		Table<String, String, Object> assessments = avs.getAssessmentVarsForSurvey(17,true,false);
		showAssessmentsAsJson(17, assessments);
	}

	@Test
	public void testAssessmentVariablesForSurveyId18() {
		Table<String, String, Object> assessments = avs.getAssessmentVarsForSurvey(18,true,false);
		showAssessmentsAsJson(18, assessments);
	}

	@Test
	public void testAssessmentVariablesForSurveyId19() {
		Table<String, String, Object> assessments = avs.getAssessmentVarsForSurvey(19,true,false);
		showAssessmentsAsJson(19, assessments);
	}

	//@Test
	public void testAssessmentVariablesForSurveyId20() {
		Table<String, String, Object> assessments = avs.getAssessmentVarsForSurvey(20,true,false);
		showAssessmentsAsJson(20, assessments);
	}

	@Test
	public void testAssessmentVariablesForSurveyId21() {
		Table<String, String, Object> assessments = avs.getAssessmentVarsForSurvey(21,true,false);
		showAssessmentsAsJson(21, assessments);
	}

	//@Test
	public void testAssessmentVariablesForSurveyId22() {
		Table<String, String, Object> assessments = avs.getAssessmentVarsForSurvey(22,true,false);
		showAssessmentsAsJson(22, assessments);
	}

	@Test
	public void testAssessmentVariablesForSurveyId23() {
		Table<String, String, Object> assessments = avs.getAssessmentVarsForSurvey(23,true,false);
		showAssessmentsAsJson(23, assessments);
	}

	//@Test
	public void testAssessmentVariablesForSurveyId24() {
		Table<String, String, Object> assessments = avs.getAssessmentVarsForSurvey(24,true,false);
		showAssessmentsAsJson(24, assessments);
	}

	@Test
	public void testAssessmentVariablesForSurveyId25() {
		Table<String, String, Object> assessments = avs.getAssessmentVarsForSurvey(25,true,false);
		showAssessmentsAsJson(25, assessments);
	}

	//@Test
	public void testAssessmentVariablesForSurveyId26() {
		Table<String, String, Object> assessments = avs.getAssessmentVarsForSurvey(26,true,false);
		showAssessmentsAsJson(26, assessments);
	}

	private void showAssessmentsAsJson(int surveyId,
			Table<String, String, Object> assessments) {

		List<Map<String, Object>> avs = Lists.newArrayList();

		for (String rowKey : assessments.rowKeySet()) {
			Map<String, Object> m = Maps.newHashMap(assessments.row(rowKey));

			// replace all 0 with null
			for (Entry<String, Object> e : m.entrySet()) {
				if (e.getValue().equals(0)) {
					e.setValue(null);
				}
			}

			avs.add(m);

			if (m.get("typeId") != null && m.get("typeId").toString().equals("4")) {
				Assert.assertTrue(String.format("measureId has to be NULL for AssessmentVariable of type 4--%s", m), m.get("measureId") == null);
				Assert.assertTrue(String.format("measureTypeId has to be NULL for AssessmentVariable of type 4--%s", m), m.get("measureTypeId") == null);
				Assert.assertTrue(String.format("answerId has to be NULL for AssessmentVariable of type 4--%s", m), m.get("answerId") == null);
			}
		}

		Gson gson = new Gson();
		String json = gson.toJson(avs);
		logger.trace("surveyId => {} assessments==>%s", surveyId, json);
	}

	
}
