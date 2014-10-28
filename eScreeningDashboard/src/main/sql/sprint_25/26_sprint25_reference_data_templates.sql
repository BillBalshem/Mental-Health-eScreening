update template set template_file = '
<#include "clinicalnotefunctions"> 
<#-- Template start -->
${MODULE_TITLE_START}
TBI: 
${MODULE_TITLE_END}
${MODULE_START}

	<#assign scoreText = "negative">
	<#if var10717?? && var10717.value??>
		<#if (var10717.value?number  >=1) >
			<#assign scoreText = "positive">
		</#if>
	</#if>
	<#if (var10714?? && var10714.value=="0" && var2016.value=="false") || (var10715?? && var10715.value=="0" && var2022.value=="false")
		||(var10716?? && var10716.value=="0" && var2030.value=="false") || (var10717?? && var10717.value=="0" && var2037.value=="false") >
		<#assign scoreText = "not calculated due to the Veteran declining to answer some or all of the questions">			
	</#if>			
	The Veteran\'s TBI screen was ${scoreText}. ${NBSP}
			
	<#if var2047?? && (var2047.children)?? && ((var2047.children)?size > 0)>
		Veteran${NBSP}
		<#if var3441?? && var3441.value = "true">
		 declined
		<#elseif var3442?? && var3442.value="true">agreed to
		</#if>
		  ${NBSP}TBI consult for further evaluation. ${NBSP}
    </#if>
	
${MODULE_END}
'
where template_id = 30;

INSERT INTO variable_template(assessment_variable_id, template_id) VALUES (2016, 30);
INSERT INTO variable_template(assessment_variable_id, template_id) VALUES (2022, 30);
INSERT INTO variable_template(assessment_variable_id, template_id) VALUES (2030, 30);
INSERT INTO variable_template(assessment_variable_id, template_id) VALUES (2037, 30);

/* ********************************************** */
/* template   UPDATES */
/* ********************************************** */
/*  SCORING MATRIX  -- MST problem -- Ticket 481*/

UPDATE template SET 
	template_file = 
'<#include "clinicalnotefunctions"> 
<#-- Template start -->
${MODULE_TITLE_START}
SCORING MATRIX: 
${MODULE_TITLE_END}
${MODULE_START}

	<#macro resetRow >
		<#assign screen = "">
		<#assign status = "">
		<#assign score = "">
		<#assign cutoff = "">
	</#macro>

		
		<#assign empty = "--">
		<#assign rows = []>

		<#-- AUDIT C -->
		<#assign screen = "AUDIT C">
		<#assign status = empty>
		<#assign score = empty>
		<#assign cutoff = empty>
		<#assign rows = []>
	
		<#if var1229??>
			<#assign score = getFormulaDisplayText(var1229)>
			<#if score != "notset">
				<#assign cutoff = "3-women/4-men">
				<#assign score = score?number>
				
				<#if (score >= 0) && (score <= 2)>
					<#assign status = "Negative">
				<#elseif (score >= 4) && (score <= 998)>
					<#assign status = "Positive">
				<#elseif (score == 3 )>
					<#assign status = "Positive for women/Negative for men">
				</#if>
			<#else>
				<#assign score = empty> 
			</#if>
			<#if (score?string == empty) || (status == empty)>
				<#assign status = empty>
				<#assign score = empty>
			</#if>
		</#if>
	
		<#assign rows = rows + [[screen, status, score, cutoff]]>	
		<@resetRow/>



		<#-- TBI -->
		<#assign screen = "BTBIS (TBI)">
		<#assign status = empty>
		<#assign score = empty>
		<#assign cutoff = empty>
		
		<#if (var2047.children)?? && ((var2047.children)?size > 0)>
			
			<#if var3442?? && var3442.value="true">
				<#assign score = "N/A">
				<#assign cutoff = "N/A">
				<#assign status = "Positive">
			<#elseif var3441?? && var3441.value="true"> 
				<#assign score = "N/A">
				<#assign cutoff = "N/A">
				<#assign status ="Negative">
			</#if>
		</#if>

		<#assign rows = rows + [[screen, status, score, cutoff]]>	
		<@resetRow/>



		<#-- DAST 10 -->
		<#assign screen = "DAST-10 (Substance Abuse)">
		<#assign status = empty>
		<#assign score = empty>
		<#assign cutoff = empty>

		<#if var1010?? >
			<#assign score = getFormulaDisplayText(var1010)>
			<#if (score?length > 0) &&  score != "notset">
				<#assign cutoff = "3">
				<#if ((score?number) <= 2)> 
					<#assign status ="Negative">
				<#else> 
					<#assign status ="Positive">
				</#if> 
			</#if>
		</#if>

		<#assign rows = rows + [[screen, status, score, cutoff]]>	
		<@resetRow/>




		<#-- GAD 7 -->
		<#assign screen = "GAD-7 (Anxiety)">
		<#assign status = empty>
		<#assign score = empty>
		<#assign cutoff = empty>
		
		<#if var1749?? >
			<#assign score = getFormulaDisplayText(var1749)>
			<#if score != "notset">
					<#assign cutoff = "10">
					<#if (score?number >= cutoff?number)>
						<#assign status = "Positive">
					<#else> 
						<#assign status ="Negative">
					</#if>
			</#if>
		</#if>

		<#assign rows = rows + [[screen, status, score, cutoff]]>	
		<@resetRow/>




		<#-- HOUSING - Homelessness -->
		<#assign screen = "Homelessness">
		<#assign status = empty>
		<#assign score = empty>
		<#assign cutoff = empty>
		
		<#if (var2001?? && var2001.children?? && var2001.children?size>0) >
			<#if (var2001.children[0].key == "var772")>
				<#assign score = "N/A">
				<#assign cutoff = "N/A">
				<#assign status = "Positive">
			<#elseif (var2001.children[0].key == "var771") >
				<#assign score = "N/A">
				<#assign cutoff = "N/A">
				<#assign status = "Negative">
			</#if>
		</#if>

		<#assign rows = rows + [[screen, status, score, cutoff]]>	
		<@resetRow/>




		<#-- ISI -->
		<#assign screen = "ISI (Insomnia)">
		<#assign status = empty>
		<#assign score = empty>
		<#assign cutoff = empty>
		
		<#if var2189?? >
			<#assign score = getFormulaDisplayText(var2189)>
			<#if score != "notset">
					<#assign cutoff = "15">
					<#if (score?number >= cutoff?number)>
						<#assign status = "Positive">
					<#else> 
						<#assign status ="Negative">
					</#if>
			</#if>
		</#if>

		<#assign rows = rows + [[screen, status, score, cutoff]]>	
		<@resetRow/>




		<#-- MST -->
		<#assign screen = "MST">
		<#assign status = empty>
		<#assign score = empty>
		<#assign cutoff = empty>
		
		<#if var2003??>
			<#if isSelectedAnswer(var2003, var2005)>
				<#assign score = "N/A">
				<#assign cutoff = "N/A">
				<#assign status = "Positive">
			<#elseif isSelectedAnswer(var2003, var2004)>
				<#assign score = "N/A">
				<#assign cutoff = "N/A">
				<#assign status = "Negative">
			</#if>
		</#if>

		<#assign rows = rows + [[screen, status, score, cutoff]]>	
		<@resetRow/>




		<#-- PCLC -->
		<#assign screen = "PCL-C (PTSD)">
		<#assign status = empty>
		<#assign score = empty>
		<#assign cutoff = empty>
		
		<#if var1929?? >
			<#assign score = getFormulaDisplayText(var1929)>
			<#if score != "notset" && score != "notfound">
					<#assign cutoff = "50">
					<#if (score?number >= cutoff?number)>
						<#assign status = "Positive">
					<#else> 
						<#assign status ="Negative">
					</#if>
			</#if>
		</#if>

		<#assign rows = rows + [[screen, status, score, cutoff]]>	
		<@resetRow/>




		<#-- PTSD -->
		<#assign screen = "PC-PTSD">
		<#assign status = empty>
		<#assign score = empty>
		<#assign cutoff = empty>
		
		<#if var1989?? >
			<#assign score = getFormulaDisplayText(var1989)>
			<#if score != "notset" && score != "notfound">
					<#assign cutoff = "3">
					<#if (score?number >= cutoff?number)>
						<#assign status = "Positive">
					<#else> 
						<#assign status ="Negative">
					</#if>
			</#if>
		</#if>

		<#assign rows = rows + [[screen, status, score, cutoff]]>	
		<@resetRow/>
		


	
		<#-- PHQ 9 DEPRESSION -->
		<#assign screen = "PHQ-9 (Depression)">
		<#assign status = empty>
		<#assign score = empty>
		<#assign cutoff = empty>
		
		<#if var1599?? >
			<#assign score = getFormulaDisplayText(var1599)>
			<#if score != "notset" && score != "notfound">
					<#assign cutoff = "10">
					<#if (score?number >= cutoff?number)>
						<#assign status = "Positive">
					<#else> 
						<#assign status ="Negative">
					</#if>
			</#if>
		</#if>

		<#assign rows = rows + [[screen, status, score, cutoff]]>	
		<@resetRow/>




		<#-- Prior MH DX/TX - Prior Mental Health Treatment -->
		<#assign screen = "Prior MH DX/TX">
		<#assign status = empty>
		<#assign score = empty>
		<#assign cutoff = empty>
		<#assign acum = 0>
		<#assign Q1complete =false>
		<#assign Q2complete =false>
		<#assign Q3complete =false>
		<#assign Q4complete =false>
		<#-- var1520: ${var1520!""}<br><br>  var1530: ${var1530!""}<br><br>  var200: ${var200!""}<br><br>  var210: ${var210!""}<br><br>  -->
		<#if (var1520.children)?? && ((var1520.children)?size > 0)>
			<#assign Q1complete = true>
			<#list var1520.children as c>
				<#if ((c.key == "var1522")  || (c.key == "var1523")  || (c.key == "var1524")) && (c.value == "true")>
					<#assign acum = acum + 1>
					<#break>
				</#if>
			</#list>
		</#if>

		<#if (var1530.children)?? && ((var1530.children)?size > 0)>
			<#assign Q2complete = true>
			<#list var1530.children as c>
				<#if ((c.key == "var1532")  || (c.key == "var1533") 
						|| (c.key == "var1534") || (c.key == "var1535") 
						|| (c.key == "var1536")) && (c.value == "true") >
					<#assign acum = acum + 1>
					<#break>
				</#if>
			</#list>
		</#if>
		

		<#if (var200.children)?? && ((var200.children)?size > 0)>
			<#assign Q3complete = true>
			<#list var200.children as c>
				<#if ((c.key == "var202") && (c.value == "true"))  >
					<#assign acum = acum + 1>
					<#break>
				</#if>
			</#list>
		</#if>

		<#if (var210.children)?? && ((var210.children)?size > 0)>
			<#assign Q4complete = true>
			<#list var210.children as c>
				<#if ((c.key == "var214") && (c.value == "true"))  >
					<#assign acum = acum + 1>
					<#break>
				</#if>
			</#list>
		</#if>

		<#if Q1complete && Q2complete && Q3complete && Q3complete>
			<#assign score = "N/A">
			<#assign cutoff = "N/A">
			<#if (acum >= 3)>
				<#assign status = "Positive">
			<#elseif (acum > 0) && (acum <= 2)>
				<#assign status ="Negative">
			</#if>
		</#if>
			
		


		<#assign rows = rows + [[screen, status, score, cutoff]]>	
		<@resetRow/>
	



		<#-- TOBACCO -->
		<#assign screen = "Tobacco Use">
		<#assign status = empty>
		<#assign score = empty>
		<#assign cutoff = empty>
		
		<#if (var600.children)?? && ((var600.children)?size > 0)>
			<#if isSelectedAnswer(var600, var603)>
				<#assign score = "N/A">
				<#assign cutoff = "N/A">
				<#assign status = "Positive">
			<#elseif isSelectedAnswer(var600, var601) || isSelectedAnswer(var600, var602)>
				<#assign score = "N/A">
				<#assign cutoff = "N/A">
				<#assign status = "Negative">
			</#if>
		</#if>

		<#assign rows = rows + [[screen, status, score, cutoff]]>	
		<@resetRow/>




		<#-- VAS PAIN - BASIC PAIN -->
		<#assign screen = "VAS PAIN">
		<#assign status = empty>
		<#assign score = empty>
		<#assign cutoff = empty>

		<#if (var2300)?? >
			<#assign score = getSelectOneDisplayText(var2300)>
			<#if score != "notset" && score != "notfound">
					<#assign cutoff = "4">
					<#if (score?number >= cutoff?number)>
						<#assign status = "Positive">
					<#else> 
						<#assign status ="Negative">
					</#if>
			<#else>
				<#assign score = empty>
			</#if>
		</#if>

		<#assign rows = rows + [[screen, status, score, cutoff]]>	
		<@resetRow/>



		${MATRIX_TABLE_START}
			${MATRIX_TR_START}
				${MATRIX_TH_START}Screen${MATRIX_TH_END}
				${MATRIX_TH_START}Result${MATRIX_TH_END}
				${MATRIX_TH_START}Raw Score${MATRIX_TH_END}
				${MATRIX_TH_START}Cut-off Score${MATRIX_TH_END}
			${MATRIX_TR_END}
			${MATRIX_TR_START}
				<#list rows as row>
					${MATRIX_TR_START}
					<#list row as col>
						${MATRIX_TD_START}${col}${MATRIX_TD_END}
					</#list>
					${MATRIX_TR_END}
				</#list>
			${MATRIX_TR_END}
		${MATRIX_TABLE_END}
${MODULE_END}'
 WHERE template_id = 100;
 
 -- /* ROAS AGGRESSION UPDATE */
update template 
set template_file = '
<#include "clinicalnotefunctions"> 
<#-- Template start -->
${MODULE_TITLE_START}
AGGRESSION: 
${MODULE_TITLE_END}
${MODULE_START}

    	<#if (var3200.children)?? && (var3210.children)?? && (var3220.children)?? && (var3230.children)?? && (var3240.children)?? 
				&& (var3250.children)?? && (var3260.children)?? && (var3270.children)?? && (var3280.children)?? && (var3290.children)?? 
				&& (var3300.children)?? && (var3310.children)?? && (var3320.children)?? && (var3330.children)?? && (var3340.children)?? 
				&& (var3350.children)??
				&& ((var3200.children)?size > 0) && ((var3210.children)?size > 0) && ((var3220.children)?size > 0) && ((var3230.children)?size > 0) && ((var3240.children)?size > 0) 
				&& ((var3250.children)?size > 0) && ((var3260.children)?size > 0) && ((var3270.children)?size > 0) && ((var3280.children)?size > 0) && ((var3290.children)?size > 0) 
				&& ((var3300.children)?size > 0) && ((var3310.children)?size > 0) && ((var3320.children)?size > 0) && ((var3330.children)?size > 0) && ((var3340.children)?size > 0) 
				&& ((var3350.children)?size > 0)>
	
			<#if !(getScore(var3200) == 999 ||  getScore(var3210) == 999 ||  getScore(var3220) == 999  || getScore(var3230) == 999  ||  getScore(var3240) == 999  || getScore(var3250) == 999  ||  getScore(var3260) == 999  
				|| getScore(var3270) == 999  ||  getScore(var3280) == 999  || getScore(var3290) == 999)  ||  getScore(var3300) == 999 ||  getScore(var3310) == 999  || getScore(var3320) == 999  ||  getScore(var3330) == 999  
				|| getScore(var3340) == 999  ||  getScore(var3350) == 999  >
			
			  <#if (var3389)?? && (getFormulaDisplayText(var3389) != "notfound") && (getFormulaDisplayText(var3389)?number == 0)>       <#-- veteran answered no to all questions" -->
				${getAnsweredNeverAllText("Aggression")}.${NBSP}
			  <#else>
    			The Veteran had a score of ${getFormulaDisplayText(var3389)} on the Retrospective Overt Aggression Scale (range 0-240).${LINE_BREAK}${LINE_BREAK}
    			
    			<#assign fragments = []>
	
	
				<#assign verbalAggressionText = "">
				<#assign verbalAggressionSubText = []>
				<#if (getQuestionCalcValue(var3200) > 1) >
					<#assign verbalAggressionSubText = verbalAggressionSubText + ["shouting angrily"]>
				</#if>
				<#if (getQuestionCalcValue(var3210) > 1) >
					<#assign verbalAggressionSubText = verbalAggressionSubText + ["yelling mild insults"]>
				</#if>
				<#if (getQuestionCalcValue(var3220) > 1) >
					<#assign verbalAggressionSubText = verbalAggressionSubText + ["making vague threats"]>
				</#if>
				<#if (getQuestionCalcValue(var3230) > 1) >
					<#assign verbalAggressionSubText = verbalAggressionSubText + ["making clear threats of violence"]>
				</#if>
				<#if verbalAggressionSubText?has_content >      
					<#assign verbalAggressionText = "Verbal Aggression (" + createSentence(verbalAggressionSubText) + ")">
					<#assign fragments = fragments + [verbalAggressionText] >
				</#if>

				<#assign physicalAggressionObjectsText = "">
				<#assign physicalAggressionObjectsSubText = []>
				<#if (getQuestionCalcValue(var3240) > 1) >
					<#assign physicalAggressionObjectsSubText = physicalAggressionObjectsSubText + ["slamming doors"]>
				</#if>
				<#if (getQuestionCalcValue(var3250) > 1) >
					<#assign physicalAggressionObjectsSubText = physicalAggressionObjectsSubText + ["throwing objects"]>
				</#if>
				<#if (getQuestionCalcValue(var3260) > 1) >
					<#assign physicalAggressionObjectsSubText = physicalAggressionObjectsSubText + ["breaking objects or smashing windows"]>
				</#if>
				<#if (getQuestionCalcValue(var3270) > 1) >
					<#assign physicalAggressionObjectsSubText = physicalAggressionObjectsSubText + ["setting fires or throwing object dangerously"]>
				</#if>
				<#if physicalAggressionObjectsSubText?has_content >      
					<#assign physicalAggressionObjectsText = "Physical aggression toward objects (" + createSentence(physicalAggressionObjectsSubText) + ")">
					<#assign fragments = fragments + [physicalAggressionObjectsText] >
				</#if>



				<#assign physicalAggressionSelfText = "">
				<#assign physicalAggressionSelfSubText = []>
				<#if (getQuestionCalcValue(var3320) > 1) >
					<#assign physicalAggressionSelfSubText = physicalAggressionSelfSubText + ["hitting self or pulling own hair"]>
				</#if>
				<#if (getQuestionCalcValue(var3330) > 1) >
					<#assign physicalAggressionSelfSubText = physicalAggressionSelfSubText + ["banging head or fists against objects"]>
				</#if>
				<#if (getQuestionCalcValue(var3340) > 1) >
					<#assign physicalAggressionSelfSubText = physicalAggressionSelfSubText + ["cutting or bruising self"]>
				</#if>
				<#if (getQuestionCalcValue(var3350) > 1) >
					<#assign physicalAggressionSelfSubText = physicalAggressionSelfSubText + ["other serious self injury"]>
				</#if>
				<#if physicalAggressionSelfSubText?has_content >      
					<#assign physicalAggressionSelfText = "Physical aggression toward self (" + createSentence(physicalAggressionSelfSubText) + ")">
					<#assign fragments = fragments + [physicalAggressionSelfText] >
				</#if>

				<#assign physicalAggressionOthersText = "">
				<#assign physicalAggressionOthersSubText = []>
				<#if (getQuestionCalcValue(var3280) > 1) >
					<#assign physicalAggressionOthersSubText = physicalAggressionOthersSubText + ["swinging at people or grabbing others"]>
				</#if>
				<#if (getQuestionCalcValue(var3290) > 1) >
					<#assign physicalAggressionOthersSubText = physicalAggressionOthersSubText + ["striking or pushing on others"]>
				</#if>
				<#if (getQuestionCalcValue(var3300) > 1) >
					<#assign physicalAggressionOthersSubText = physicalAggressionOthersSubText + ["bruising or causing welts on others"]>
				</#if>
				<#if (getQuestionCalcValue(var3310) > 1) >
					<#assign physicalAggressionOthersSubText = physicalAggressionOthersSubText + ["attacking others causing severe physical injury"]>
				</#if>
				<#if physicalAggressionOthersSubText?has_content >      
					<#assign physicalAggressionOthersText = "Physical aggression towards others (" + createSentence(physicalAggressionOthersSubText) + ")">
					<#assign fragments = fragments + [physicalAggressionOthersText]  >
				</#if>
				The Veteran indicated the following concerning aggressive behaviors in the past month: ${createSentence(fragments)}.${NBSP}
			  </#if>
    		<#else>
    			${getNotCompletedText()}
    		</#if>
    	<#else>
    		${getNotCompletedText()}
    	</#if>
${MODULE_END}
'
where template_id = 38;

UPDATE template SET template_file = 
'<#include "clinicalnotefunctions"> 
<#-- Template start -->
${MODULE_TITLE_START}
ADVANCE DIRECTIVE:
${MODULE_TITLE_END}
${MODULE_START}
  <#-- var800: ${var800}<br><br>var820: ${var820}<br><br> var826: ${var826}<br><br> --> <#-- test objs -->
  <#assign nextLine = "">
  	<#if (var800.children)?? && (var800.children?size > 0) || ((var820.children)??&& (var820.children?size > 0))
  	 || ((var826.children)??  && (var826.children?size > 0))>
    <#if (var800.children)?? && (var800.children?size > 0) && hasValue(getSelectMultiDisplayText(var800)) >
    	<#-- In what language do you prefer to get your health information? -->
      ${nextLine}The Veteran requests to receive information in ${getSelectMultiDisplayText(var800)}.
      <#assign nextLine = "${LINE_BREAK}${LINE_BREAK}">
    </#if>
    <#if (var820.children)?? && (var820.children?size > 0) && hasValue(getSelectMultiDisplayText(var820)) >
      <#-- Do you have and Advance Directive or Durable Power of Attorney for Healthcare?  -->
      ${nextLine}The Veteran reported ${getSelectMultiDisplayText(var820)} an Advance Directive or Durable Power of Attorney for Healthcare.
      <#assign nextLine = "${LINE_BREAK}${LINE_BREAK}">
    <#else>
     The Veteran declined to answer some or all of the questions.
     </#if>
    	   
     <#if (var826.children)?? && (var826.children?size > 0) && hasValue(getSelectMultiDisplayText(var826)) >
      	<#-- If no, would like information about this document? -->
      	${nextLine}The Veteran ${getSelectMultiDisplayText(var826)} like information about an Advance Directive.
    </#if>     
    
    <#else>
    	${getNotCompletedText()}
    </#if>    
${MODULE_END}'
where template_id = 10;

UPDATE template SET template_file = 
'<#include "clinicalnotefunctions"> 
<#-- Template start -->
${MODULE_TITLE_START}
ALCOHOL:
${MODULE_TITLE_END}
${MODULE_START}
	<#if (var1200.children)?? && (var1210.children)?? && (var1220.children)??
			&& ((var1200.children)?size > 0) && ((var1210.children)?size > 0) && ((var1220.children)?size > 0)>
	
	<#assign score = getListScore([var1200,var1210,var1220])>
	<#assign status ="">  <#-- positive or negative -->
	<#assign gender = "">
	
		<#if !((var1200.children)?has_content) || !((var1210.children)?has_content) || !((var1220.children)?has_content) >
	 		${getNotCompletedText()}
		<#else>
			<#if (score >= 0) && (score <= 2)>
				<#assign status = "negative">
			<#elseif (score >= 4) && (score <= 998)>
				<#assign status = "positive">
			<#elseif (score == 3 )>
				<#assign status = "positive for women/negative for men">
			</#if>
			The Veteran\'s AUDIT-C screen was ${status} with a score of ${score}. ${NBSP}
    	</#if>
    	
    <#else>
    	The Veteran\'s AUDIT-C screen was not calculated due to the Veteran declining to answer some or all of the questions.
    </#if>
${MODULE_END}
'
where template_id = 27;

update template set template_file = 
'<#include "clinicalnotefunctions"> 
<#-- Template start -->
${MODULE_TITLE_START}
Health Status:
${MODULE_TITLE_END}
${MODULE_START}
	  <#if var1189?? && (var1020.children)?? && (var1030.children)?? && (var1040.children)?? && (var1050.children)?? && (var1060.children)?? 
		&& (var1070.children)?? && (var1080.children)?? && (var1090.children)?? && (var1100.children)?? && (var1110.children)?? 
		&& (var1120.children)?? && (var1130.children)?? && (var1140.children)?? && (var1150.children)?? && (var1160.children)??
		&& ((var1020.children)?size > 0) && ((var1030.children)?size > 0) && ((var1040.children)?size > 0) && ((var1050.children)?size > 0) 
		&& ((var1060.children)?size > 0) && ((var1070.children)?size > 0) && ((var1080.children)?size > 0) && ((var1090.children)?size > 0) 
		&& ((var1100.children)?size > 0) && ((var1110.children)?size > 0) && ((var1120.children)?size > 0) && ((var1130.children)?size > 0) 
		&& ((var1140.children)?size > 0) && ((var1150.children)?size > 0) && ((var1160.children)?size > 0)>  
	
	<#assign totalScore = getFormulaDisplayText(var1189)>
	<#assign scoreText = "">
	<#if totalScore != "notset" && totalScore != "notfound">
		<#assign totalScore = totalScore?number>
		<#if (totalScore <= 4)>
			<#assign scoreText = "minimal">
		<#elseif (totalScore >= 5 &&  (totalScore <= 9))>
			<#assign scoreText = "low number of">
		<#elseif (totalScore >= 10 &&  (totalScore <= 14))>
			<#assign scoreText = "medium number of">
		<#elseif (totalScore >= 15 &&  (totalScore <= 30))>
			<#assign scoreText = "high number of">
		</#if>
	</#if>

	<#-- During the past 4 weeks, how much have you been bothered by any of the following problems -->
	<#-- this is almost identical to Other Health Symptoms -->
	<#assign fragments = []> 
    <#if (getScore(var1150) > 1)>
		<#assign fragments = fragments + ["stomach pain"] >
	</#if>
    <#if (getScore(var1160) > 1)>
		<#assign fragments = fragments + ["back pain"] >
	</#if>
	<#if (getScore(var1020) > 1)>
		<#assign fragments = fragments + ["pain in arms, legs or joints (knees, hips, etc.)"] >
	</#if>
	<#if (getScore(var1030) > 1)>
		<#assign fragments = fragments + ["menstrual cramps or other problems with your periods"] >
	</#if>
	<#if (getScore(var1040) > 1)>
		<#assign fragments = fragments + ["headaches"] >
	</#if>
	<#if (getScore(var1050) > 1)>
		<#assign fragments = fragments + ["chest pain"] >
	</#if>
	<#if (getScore(var1060) > 1)>
		<#assign fragments = fragments + ["dizziness"] >
	</#if>
	<#if (getScore(var1070) > 1)>
		<#assign fragments = fragments + ["fainting spells"] >
	</#if>
	<#if (getScore(var1080) > 1)>
		<#assign fragments = fragments + ["feeling your heart pound or race"] >
	</#if>
	<#if (getScore(var1090) > 1)>
		<#assign fragments = fragments + ["shortness of breath"] >
	</#if>
	<#if (getScore(var1100) > 1)>
		<#assign fragments = fragments + ["pain or problems during sexual intercourse"] >
	</#if>
	<#if (getScore(var1110) > 1)>
		<#assign fragments = fragments + ["constipation, loose bowels or diarrhea"] >
	</#if>
	<#if (getScore(var1120) > 1)>
		<#assign fragments = fragments + ["nausea, gas or indigestion"] >
	</#if>
	<#if (getScore(var1130) > 1)>
		<#assign fragments = fragments + ["feeling tired or having low energy"] >
	</#if>
	<#if (getScore(var1140) > 1)>
		<#assign fragments = fragments + ["trouble sleeping"] >
	</#if>

	<#if fragments?has_content  >
		<#assign resolved_fragments =  createSentence(fragments)>
	<#else>
		<#assign resolved_fragments = "None">
	</#if>

	The Veteran reported a ${scoreText} somatic symptoms.${LINE_BREAK}${LINE_BREAK}
	The Veteran endorsed being bothered a lot by the following health symptoms over the past four weeks: ${resolved_fragments}.${NBSP}

	
	<#else>
		${getNotCompletedText()}
	</#if>
${MODULE_END}'
where template_id = 17;


update template set template_file = 
'<#include "clinicalnotefunctions"> 
<#-- Template start -->
${MODULE_TITLE_START}
PAIN:
${MODULE_TITLE_END}
${MODULE_START}
	<#if (var2300.children)?? && (var2300.children?size>0) || (var2330.children)?? || (var2334?? && ((var2334.value) == "true"))>  
			<#--  --> 
			<#assign fragments = []>
			<#assign text ="notset">  
	 <#if (var2300.children)?? && (var2300.children?size>0)>
			<#assign level = getSelectOneDisplayText(var2300)!("-1"?number)>
			Over the past month, the Veteran\'s reported pain level was ${level} of 10.
	 </#if>
	 <#if var2330.children?? && (var2330.children?size>0) && var2334??>
			<#assign bodyParts = getTableMeasureValueDisplayText(var2330)!"">
			<#if !(bodyParts?has_content) && ((var2334.value) == "true")>
				<#assign bodyParts = "none">
			</#if>
			
			<#if bodyParts != "none">
				The pain was located in: ${bodyParts}.
			</#if>
		</#if>
	<#else>
		${getNotCompletedText()}
	</#if>
${MODULE_END}
'
where template_id = 20;


/*tobacco decline template */
update template set template_file = '<#include "clinicalnotefunctions"> 
<#-- Template start --> 
${MODULE_TITLE_START} 
	Tobacco Use 
${MODULE_TITLE_END} 

${MODULE_START}  
	The use of tobacco causes harm to nearly every organ in the body. Quitting greatly lowers your risk of death from cancers, heart disease, stroke, and emphysema. There are many options, such as in-person and telephone counseling, nicotine replacement, and prescription medications. 

	${LINE_BREAK} 
	${LINE_BREAK} 

	<b>Results:</b>${NBSP} 
	
	<#if (var600.children)?? && ((var600.children)?size gt 0)> 
		<#if isSelectedAnswer(var600,var601) || isSelectedAnswer(var600,var602)> 
			negative screen 
		<#elseif isSelectedAnswer(var600,var603)> 
			current user 
			${LINE_BREAK} 
			<b>Recommendations:</b> Prepare a plan to reduce or quit the use of tobacco. Get support from family and friends, and ask your clinician for help if needed. 
		</#if> 
	<#else>
		${NBSP}Veteran declined to respond
	</#if> 	
${MODULE_END}' 
where template_id=306;

 
/*
t692
There is an issue with housing. The rule was: If HomelessCR_stable_house =1 then insert “stable housing”, if =0 “unstable housing/at risk”, if equal to 999 omit module. 
However, this won’t work because the Veteran could have stable housing now and be worried about the future, which would mean they need assistance. 
The changed rule is: 
====>>> if HomelessCR_stable_worry =1 then insert “unstable housing/at risk”, if =0 “no housing concerns”, if equal to 999 insert “Veteran declined to respond”.
====>>> also if user declined to respond then show Veteran declined to respond
*/
update variable_template set assessment_variable_id=771 where variable_template_id=10511;
update variable_template set assessment_variable_id=772 where variable_template_id=10512;
update variable_template set assessment_variable_id=2001 where variable_template_id=10510;
delete from variable_template where variable_template_id=10513;

update template set template_file = '<#include "clinicalnotefunctions"> 
<#-- Template start --> 
${MODULE_TITLE_START} Homelessness ${MODULE_TITLE_END} 
${MODULE_START} This is when you do not have a safe or stable place you can return to every night. The VA is committed to ending Veteran homelessness by the end of 2015. ${LINE_BREAK} 
${LINE_BREAK} 
<b>Results:</b>${NBSP} 
<#if (var2001)?? && (var2001.children)?? && ((var2001.children)?size > 0)> 
	<#if isSelectedAnswer(var2001,var772)> 
		unstable housing/at risk 
		${LINE_BREAK} 
		<b>Recommendation:</b> Call the VA\'s free National Call Center for Homeless Veterans at (877)-424-3838 and ask for help. Someone is always there to take your call. 
	<#elseif isSelectedAnswer(var2001,var771)> 
		stable housing 
	</#if>  
<#else>
	${NBSP}Veteran declined to respond
</#if>
${MODULE_END}' 
where template_id=301;

 
/* depression declined template */
update template set template_file = '<#include "clinicalnotefunctions"> 
<#assign score = getCustomVariableDisplayText(var1599)> 
<#-- Template start --> 
${MODULE_TITLE_START} Depression ${MODULE_TITLE_END} 
<#if score != "notfound">
	${GRAPH_SECTION_START} 
	${GRAPH_BODY_START} 
		{"type": "stacked", "title": "My Depression Score", "footer": "*a score of 10 or greater is a positive screen", "data": { "graphStart": 0,"ticks": [0, 4, 10, 15, 20, 27],"intervals": {"Minimal":4, "Mild":10, "Moderate":15, "Moderately Severe":20, "Severe":27 }, "score": ${score} } } 
	${GRAPH_BODY_END} 
	${GRAPH_SECTION_END}  
</#if>
${MODULE_START} 
	Depression is when you feel sad and hopeless for much of the time. It affects your body and thoughts, and interferes with daily life. There are effective treatments and resources for dealing with depression.
	${LINE_BREAK} 
	<b>Recommendation:</b> 
	<#if score = "notfound">
		${NBSP}Veteran declined to respond
	<#elseif score?number lt 10>
		Contact a clinician if in the future if you ever feel you may have a problem with depression
	<#elseif score?number gt 9>
		Ask your clinician for further evaluation and treatment options
	</#if> 
${MODULE_END}' 
where template_id=308;

/* PTSD declined template */
update template set template_file = '<#include "clinicalnotefunctions"> 
<#assign score = getCustomVariableDisplayText(var1929)>  
<#-- Template start --> 
${MODULE_TITLE_START} Post-traumatic Stress Disorder ${MODULE_TITLE_END}  

<#if score != "notfound">
	${GRAPH_SECTION_START}  
	${GRAPH_BODY_START} 
	{ "type": "stacked", "title": "My PTSD Score", "footer": "", "data": { "graphStart": 17, "ticks": [17,35,50,65,85], "intervals": { "Negative": 49, "Positive":85 }, "score": ${score} } } 
	${GRAPH_BODY_END} 
	${GRAPH_SECTION_END}  
</#if>

${MODULE_START} 
	PTSD is when remembering a traumatic event keeps you from living a normal life. It\'s also called shell shock or combat stress. Common symptoms include recurring memories or nightmares of the event, sleeplessness, and feeling angry, irresistible, or numb. 
	${LINE_BREAK} 
	<b>Recommendation:</b> 
	<#if score = "notfound">
		${NBSP}Veteran declined to respond
	<#elseif score?number gt 49>
		Ask your clinician for further evaluation and treatment options
	<#elseif score?number lt 50>
		You may ask a clinician for help in the future if you feel you may have symptoms of PTSD
	</#if> 
${MODULE_END}' 
where template_id=310;

/* TBI changes: If tbi_consult=1 then insert “You have requested further assessment” if TBI_consult =0 insert “you have declined further assessment” if =999 then omit recommendation.  */
update template set template_file = '<#include "clinicalnotefunctions"> 
<#function calcScore obj> 
	<#assign result = 0> 
	<#if (obj.children)?? && ((obj.children)?size > 0)> 
		<#list obj.children as c> 
			<#if c.overrideText != "none"> 
				<#assign result = result + 1> 
				<#break> 
		</#if> 
	</#list> 
	</#if> 
	<#return result> 
</#function>  
<#assign score = 0>  
<#assign isQ2Complete = false> 
<#assign isQ2None = false> 
<#if (var3400.children)?? && ((var3400.children)?size > 0)> 
	<#assign isQ2Complete = true> 
	<#if isSelectedAnswer(var3400, var2016)!false> 
		<#assign isQ2None = true> 
	<#else> 
		<#assign score = score + calcScore(var3400)> 
	</#if> 
</#if>  
<#assign isQ3Complete = false> 
<#assign isQ3None = false> 
<#if isQ2Complete> 
	<#if isQ2None> 
		<#assign isQ3Complete = true> 
	<#else> 
		<#if (var3410.children)?? && ((var3410.children)?size > 0) > 
			<#if isSelectedAnswer(var3410, var2022)!false> 
				<#assign isQ3None = true> 
			<#else> 
				<#assign score = score + calcScore(var3410)> 
			</#if> 
			<#assign isQ3Complete = true> 
		</#if> 
	</#if> 
</#if>  
<#assign isQ4Complete = false> 
<#assign isQ4None = false> 
<#if isQ3Complete> 
	<#if isQ2None || isQ3None> 
		<#assign isQ4Complete = true> 
	<#else> 
		<#if (var3420.children)?? && ((var3420.children)?size > 0) > 
			<#if isSelectedAnswer(var3420, var2030)!false> 
				<#assign isQ4None = true> 
			<#else> 
				<#assign score = score + calcScore(var3420)> 
			</#if> 
			<#assign isQ4Complete = true> 
		</#if> 
	</#if> 
</#if>  
<#assign isQ5Complete = false> 
<#assign isQ5None = false> 
<#if isQ4Complete> 
	<#if isQ2None || isQ3None || isQ4None> 
		<#assign isQ5Complete = true> 
	<#else> 
		<#if (var3430.children)?? && ((var3430.children)?size > 0) > 
			<#if isSelectedAnswer(var3430, var2037)!false> 
				<#assign isQ5None = true> 
			<#else> 
				<#assign score = score + calcScore(var3430)> 
			</#if> 
			<#assign isQ5Complete = true> 
		</#if> 
	</#if> 
</#if>  
<#assign isComplete = false> 
<#if isQ2Complete && isQ3Complete && isQ4Complete && isQ5Complete> 
	<#assign isComplete = true> 
</#if>  
${MODULE_TITLE_START} 
	Traumatic Brain Injury (TBI) 
${MODULE_TITLE_END} 

${MODULE_START}  
	<#assign showRec = false> <#assign tbi_consult_text = ""> 
	<#if isComplete && (var2047.children)?? && ((var2047.children)?size > 0) && isSelectedAnswer(var2047,var3442)> 
		<#assign showRec = true> 
		<#assign tbi_consult_text = "You have requested further assessment"> 
	<#elseif isComplete && (var2047.children)?? && ((var2047.children)?size > 0) && isSelectedAnswer(var2047,var3441)> 
		<#assign showRec = true> 
		<#assign tbi_consult_text = "You have declined further assessment">
	</#if> 
	A TBI is physical damage to your brain, caused by a blow to the head. Common causes are falls, fights, sports, and car accidents. A blast or shot can also cause TBI. ${LINE_BREAK} 
	${LINE_BREAK} 
	<#if isComplete> 
		<b>Results:</b>${NBSP} 
		<#if (score >= 0) && (score <= 3)> 
			negative screen 
		<#elseif (score >= 4 )> 
			positive screen
		</#if> 
	</#if>  
	<#if isComplete && showRec> 
		${LINE_BREAK} 
		<b>Recommendation:</b> 
			${tbi_consult_text}. 
	</#if> 
${MODULE_END} ' 
where template_id=307;


/****************** TICKET 680 CHANGES BELOW *********************************/
/*** Exposures skip logic tiket-589 ***/
update template set template_file = 
'<#include "clinicalnotefunctions"> 
<#-- Template start -->
${MODULE_TITLE_START}
EXPOSURES:
${MODULE_TITLE_END}
${MODULE_START}
	 <#-- var2200: ${var2200}<br><br>  var2230: ${var2230}<br><br>  var2240: ${var2240}<br><br>  var2250: ${var2250}<br><br> var2260: ${var2260}<br><br>  -->
	
    <#assign nextLine = "">
	<#if (var10540.children)??  && ((var10540.children)?size > 0) || (var2200.children)?? || ((var2230.children)?? && (var2240.children)?? && (var2250.children)??) || (var2260.children)?? || (var2289?? 
		&& getFormulaDisplayText(var2289) != "notset" && getFormulaDisplayText(var2289) != "notfound") > 
	 	
	 	<#if (var10540.children)??  && ((var10540.children)?size > 0)>
	 	<#if isSelectedAnswer(var10540,var10541)>
	 		The Veteran is not concerned about, but may have been exposed
	 	<#elseif isSelectedAnswer(var10540,var10542)>
	 		The Veteran reported persistent major concerns related to the effects of exposure
	 	</#if>
	  	<#if (var2200.children)?? && hasValue(var2200) && getSelectMultiDisplayText(var2200)!= "notfound"> 	
			${NBSP}to ${getSelectMultiDisplayText(var2200)}
            <#assign nextLine = "${LINE_BREAK}${LINE_BREAK}">
		</#if>
		.
		</#if>
	
	    <#if var2289?? 
		&& getFormulaDisplayText(var2289) != "notset" && getFormulaDisplayText(var2289) != "notfound">
		<#assign score = getFormulaDisplayText(var2289)>
		<#assign scoreText = "notset">
	
		<#if (score?number >= 1) >
				<#assign scoreText = "being exposed">
		<#elseif score?number == 0>
			<#assign scoreText = "no exposure">
		</#if> 
        ${nextLine}The Veteran reported ${scoreText} to animal contact during a deployment in the past 18 months that could result in rabies.
        <#assign nextLine = "${LINE_BREAK}${LINE_BREAK}">	
		</#if>
		<#if (var2260.children)??  && hasValue(var2260)> 	
			<#assign combat = getSelectMultiDisplayText(var2260)>
			<#if combat != "notfound">
				${nextLine}The Veteran reported the following types of combat experience: ${combat}.
			</#if>
		</#if>

	<#else>
		${getNotCompletedText()}
	</#if>
${MODULE_END}
'
where template_id = 15;

/*** ticket 680 comment #9 *********/
update template set template_file = 
'<#include "clinicalnotefunctions"> 
<#-- Template start -->
${MODULE_TITLE_START}
SLEEP:
${MODULE_TITLE_END}
${MODULE_START}
	<#if var2189?? && var2189.value??>
	<#assign score = var2189.value?number>
	<#if score??> 	
		<#if (score >= 0 && score <= 7) >
			<#assign scoreText = "negative">
			<#assign text = "no clinically significant insomnia">
		<#elseif (score >= 8) && (score <= 14)>
			<#assign scoreText = "negative">
			<#assign text = "subthreshold insomnia">
		<#elseif (score >= 15) && (score <= 21)>
			<#assign scoreText = "positive">
			<#assign text = "moderate insomnia">
		<#elseif (score >= 22) && (score <= 998)>  <#-- if score = 999 then veteran did not answer the question -->
			<#assign scoreText = "positive">
			<#assign text = "severe insomnia">
		</#if>
	</#if>
	
	<#if score??>
		<#if (score >=0) && (score <= 998)>
			<#t>The Veteran\'s ISI was ${scoreText} indicating ${text} for the past week.${NBSP}
		<#elseif score >= 999>
			<#-- this is score 999 -->
			<#t>The Veteran did not complete the ISI screen.${NBSP}>
		</#if>
	</#if>
	
	<#else>
		${getNotCompletedText()}
	</#if>
${MODULE_END}
'
where template_id = 37;

delete from variable_template where template_id = 37;
INSERT INTO variable_template(assessment_variable_id, template_id) VALUES (2189, 37);



 