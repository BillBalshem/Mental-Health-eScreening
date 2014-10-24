package gov.va.escreening.dto.template;

public class TemplateVariableContent extends TemplateBaseContent{

	private TemplateAssessmentVariableDTO content;
	
	public TemplateVariableContent()
	{
		setType("var");
	}
	
	public TemplateAssessmentVariableDTO getContent() {
		return content;
	}

	public void setContent(TemplateAssessmentVariableDTO content) {
		this.content = content;
	}

}
