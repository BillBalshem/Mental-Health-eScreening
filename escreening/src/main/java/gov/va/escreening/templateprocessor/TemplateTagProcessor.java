package gov.va.escreening.templateprocessor;

import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.va.escreening.constants.TemplateConstants.ViewType;
import gov.va.escreening.controller.DashboardHomeController;
import static gov.va.escreening.templateprocessor.TemplateTags.*;

public class TemplateTagProcessor {
	private static final Logger logger = LoggerFactory.getLogger(DashboardHomeController.class);
	
	public static String resolveClinicalNoteTags(String noteText, ViewType viewType) {
		switch(viewType) {
			case HTML:
				noteText = resolveHtmlType(noteText);
				break;
			case TEXT:
				noteText = resolveTextType(noteText);
				break;
			case CPRS_PREVIEW:
				noteText = resolveCPRS(noteText);
				break;
			default:
				throw new UnsupportedOperationException(String.format("resolveClinicalNoteTags view type: %s is not supported", viewType));
		}
		return noteText;
	}
	
	private static Pattern htmlEndDivReplace = Pattern.compile(createTagRegex(Style.XML, 
			BATTERY_HEADER_END, BATTERY_FOOTER_END, SECTION_TITLE_END, SECTION_END, MODULE_COMPONENTS_END, MODULE_TITLE_END, MODULE_END,
			GRAPH_SECTION_END, GRAPH_BODY_END));
	
	private static String resolveHtmlType(String noteText) {
	    noteText = noteText.replace(BATTERY_HEADER_START.xml(), "<div class='templateHeader'>");
        
        noteText = noteText.replace(BATTERY_FOOTER_START.xml(), "<div class='templateFooter'>");
	    
	    noteText = noteText.replace(SECTION_TITLE_START.xml(), "<div class='templateSectionTitle'>");

	    noteText = noteText.replace(SECTION_START.xml(), "<div class='templateSection'>");
        
	    noteText = noteText.replace(MODULE_COMPONENTS_START.xml(), "<div class='moduleTemplate'>");
	    
		noteText = noteText.replace(MODULE_TITLE_START.xml(), "<div class='moduleTemplateTitle'>");
		
		noteText = noteText.replace(MODULE_START.xml(), "<div class='moduleTemplateText'>");
    	
    	noteText = noteText.replace(LINE_BREAK.xml(), "<br>");
    	noteText = noteText.replace(NBSP.xml(), "&nbsp;");
        noteText = noteText.replace(PRE_START.xml(), "<pre>");
        noteText = noteText.replace(PRE_END.xml(), "</pre>");
    	
    	noteText = noteText.replace(MATRIX_TABLE_START.xml(), "<table>");
    	noteText = noteText.replace(MATRIX_TABLE_END.xml(), "</table>");
    	noteText = noteText.replace(MATRIX_TH_START.xml(), "<th class='matrixTableHeader'>");
    	noteText = noteText.replace(MATRIX_TH_END.xml(), "</th>");
    	noteText = noteText.replace(MATRIX_TR_START.xml(), "<tr>");
    	noteText = noteText.replace(TABLE_TR_CTR_START.xml(), "<tr class='justifyCtrTableRow'>");
    	noteText = noteText.replace(TABLE_TR_END.xml(), "</tr>");
    	noteText = noteText.replace(MATRIX_TR_END.xml(), "</tr>");
    	noteText = noteText.replace(MATRIX_TD_START.xml(), "<td>");
    	noteText = noteText.replace(MATRIX_TD_END.xml(), "</td>");
    	//noteText = noteText.replace(TABLE_TD_RT_START.xml(), "<td class='justifyRtTableData'>");
    	noteText = noteText.replace(TABLE_TD_CTR_START.xml(), "<td class='justifyCtrTableData'>"); 
    	noteText = noteText.replace(TABLE_TD_LFT_START.xml(), "<td class='justifyLftTableData'>");
    	noteText = noteText.replace(TABLE_TD_SPACER1_START.xml(), "<td class='spacer1TableData'>");
    	noteText = noteText.replace(TABLE_TD_END.xml(), "</td>");
    	
    	noteText = noteText.replace(IMG_LOGO_VA_HC.xml(), "<img class='vetSmyImg_va_logo' src='../resources/images/logo_va_veteran_summary.gif'>");
    	noteText = noteText.replace(IMG_CESMITH_BLK_BRDR.xml(), "<img class='vetSmyImg_cesmith' src='../resources/images/cesamh_blk_border.png'>");
    	noteText = noteText.replace(IMG_VA_VET_SMRY.xml(), "<img src='../resources/images/escreening_cdsmith_QR_code_small.png'>");
    	
    	//GRAPH stuff
    	noteText = noteText.replace(GRAPH_SECTION_START.xml(), "<div class='graphSection'>");
    	noteText = noteText.replace(GRAPH_BODY_START.xml(), "<div class='graphicBody'>");
    	
    	return htmlEndDivReplace.matcher(noteText).replaceAll("</div>");
	}
	
	private static final Pattern textEmptyReplace = Pattern.compile(createTagRegex(Style.XML, 
			BATTERY_HEADER_START, PRE_START, PRE_END,
			MODULE_COMPONENTS_START, MODULE_COMPONENTS_END, MODULE_TITLE_START, MODULE_START, 
			MATRIX_TABLE_START, MATRIX_TABLE_END, MATRIX_TH_START, MATRIX_TH_END, MATRIX_TR_START, MATRIX_TR_END, MATRIX_TD_START, MATRIX_TD_END));
	
	private static final String dashedLine = "--------------------------------------------------------------------------------";
	private static final String equalLine =  "================================================================================";
	
	private static String resolveTextType(String noteText) {
        noteText = noteText.replace(BATTERY_HEADER_END.xml(), equalLine);
        
        noteText = noteText.replace(BATTERY_FOOTER_START.xml(), "\n\n");
        noteText = noteText.replace(BATTERY_FOOTER_END.xml(), "");
	    
	    noteText = noteText.replace(SECTION_TITLE_START.xml(), "\n");
	    noteText = noteText.replace(SECTION_TITLE_END.xml(), "\n" + dashedLine +"\n");
	    
	    noteText = noteText.replace(SECTION_START.xml(), "\n");
	    noteText = noteText.replace(SECTION_END.xml(), "\n" + dashedLine);
        
        noteText = noteText.replace(MODULE_TITLE_END.xml(), " ");
        noteText = noteText.replace(MODULE_END.xml(), "\n\n");
        
        noteText = noteText.replace(LINE_BREAK.xml(), "\n");
        noteText = noteText.replace(NBSP.xml(), " ");
        
        noteText = textEmptyReplace.matcher(noteText).replaceAll("");
        
        //replacing end of paragraph with new line (this can have a '\n' optionally before the end of the paragraph
        //but it should take the \n and the </p> and turn them into *one* new line)
        noteText = noteText.replaceAll("\\s*<\\s*/\\s*p\\s*>", "\n");

        //remove line feeds (normally inserted by copy and paste from Word
        noteText = noteText.replaceAll("&#[(xa)(10)(x9)(9)]+;", "");
        
        //remove other tags (this is all we expect in the wysiwyg editor)
        noteText = noteText.replaceAll("</*\\s*[BbIiuUsSp(blockquote)(pre)(ol)(li)(h1)(h2)(h3)(h4)(h5)(h6)(br)(BR)(span)(div)]+\\s*/*>", "");
        noteText = noteText.replaceAll("<\\/*[^>\n\r\u0085\u2028\u2029]+>", "");
        
        //replace all &###; with unicode equivalents  
        noteText = StringEscapeUtils.unescapeHtml4(noteText);
        
        //replace NO-BREAK SPACE (U+00A0) with regular space (U+0020)
        logger.trace("\n\n******** Before space replace *******/n{}", noteText);
        noteText = noteText.replaceAll("\\u00A0", " ");
        logger.trace("\n\n******** After space replace *******/n{}", noteText);
        
        //wrap to 80 columns
        StringBuilder wrappedText = new StringBuilder();
        String[] lines = noteText.split("\n");
        for(String line : lines){
        	wrappedText.append("\n").append(WordUtils.wrap(line, 80, "\n", true));
        }
        return wrappedText.toString();
    }
	
	private static final String longDashedLine = "——————————————————————————————————————————————";
	private static final Pattern cprsEmptyReplace = Pattern.compile(createTagRegex(Style.XML, 
			BATTERY_HEADER_START, MODULE_COMPONENTS_START, MODULE_COMPONENTS_END, MODULE_START, 
			GRAPH_SECTION_START, GRAPH_BODY_START, GRAPH_SECTION_END, GRAPH_BODY_END));
	
	/**
	 * This is a hybrid between the text version and the html version because the requirement is that
	 * we show an html view of the text that will be submitted to Vista.
	 * @param noteText
	 * @return
	 */
	private static String resolveCPRS(String noteText) {
		
		noteText = noteText.replace(BATTERY_HEADER_END.xml(), equalLine);
        
        noteText = noteText.replace(BATTERY_FOOTER_START.xml(), "<br/><br/>");
        noteText = noteText.replace(BATTERY_FOOTER_END.xml(), "");
	    
	    noteText = noteText.replace(SECTION_TITLE_START.xml(), "<br/><strong>");
	    noteText = noteText.replace(SECTION_TITLE_END.xml(), "</strong><br/>" + longDashedLine +"<br/>");
	    
	    noteText = noteText.replace(SECTION_START.xml(), "<br/>");
	    noteText = noteText.replace(SECTION_END.xml(), longDashedLine);
        
	    noteText = noteText.replace(MODULE_TITLE_START.xml(), "<strong>");
        noteText = noteText.replace(MODULE_TITLE_END.xml(), "</strong> ");
        noteText = noteText.replace(MODULE_END.xml(), "<br/><br/>");
        
        noteText = noteText.replace(LINE_BREAK.xml(), "<br/>");
    	noteText = noteText.replace(NBSP.xml(), "&nbsp;");
    	noteText = noteText.replace(PRE_START.xml(), "<pre>");
    	noteText = noteText.replace(PRE_END.xml(), "</pre>");
    	
    	noteText = noteText.replace(MATRIX_TABLE_START.xml(), "<table>");
    	noteText = noteText.replace(MATRIX_TABLE_END.xml(), "</table>");
    	noteText = noteText.replace(MATRIX_TH_START.xml(), "<th class='matrixTableHeader'>");
    	noteText = noteText.replace(MATRIX_TH_END.xml(), "</th>");
    	noteText = noteText.replace(MATRIX_TR_START.xml(), "<tr>");
    	noteText = noteText.replace(TABLE_TR_CTR_START.xml(), "<tr class='justifyCtrTableRow'>");
    	noteText = noteText.replace(TABLE_TR_END.xml(), "</tr>");
    	noteText = noteText.replace(MATRIX_TR_END.xml(), "</tr>");
    	noteText = noteText.replace(MATRIX_TD_START.xml(), "<td>");
    	noteText = noteText.replace(MATRIX_TD_END.xml(), "</td>");
    	//noteText = noteText.replace(TABLE_TD_RT_START.xml(), "<td class='justifyRtTableData'>");
    	noteText = noteText.replace(TABLE_TD_CTR_START.xml(), "<td class='justifyCtrTableData'>"); 
    	noteText = noteText.replace(TABLE_TD_LFT_START.xml(), "<td class='justifyLftTableData'>");
    	noteText = noteText.replace(TABLE_TD_SPACER1_START.xml(), "<td class='spacer1TableData'>");
    	noteText = noteText.replace(TABLE_TD_END.xml(), "</td>");
		
		noteText = cprsEmptyReplace.matcher(noteText).replaceAll("");
		
				
		StringBuilder wrappedText = new StringBuilder();
        String[] lines = noteText.split("<br/*>");
        for(String line : lines){
        	if(line.contains("<t") || line.contains("</t"))
        		wrappedText.append("<br/>").append(line);
        	else{
        	    //add a temp special space to so wrapping works as expected
        		String lineWithSpaceSub = line.replaceAll("&nbsp;", new String(Character.toChars(0x00A0)));
        		lineWithSpaceSub = WordUtils.wrap(lineWithSpaceSub, 80, "<br/>", true);
        	    wrappedText.append("<br/>").append(lineWithSpaceSub.replaceAll("\\u00A0", "&nbsp;"));
        	}
        }
        return wrappedText.toString();
		
	}
}