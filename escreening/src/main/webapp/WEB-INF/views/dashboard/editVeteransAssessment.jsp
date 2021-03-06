<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="sec"	uri="http://www.springframework.org/security/tags" %>

<!DOCTYPE html>
<html>
<head>
	<title>Batch Create Battery</title>
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
	<meta name="viewport" content="width=device-width, initial-scale=1">
	<script type="text/javascript" src="resources/js/jquery/jquery-1.10.2.min.js"></script>
    <script type="text/javascript" src="resources/js/jquery/jquery.dataTables.js"></script>
	<script type="text/javascript" src="resources/js/angular/angular.min.js"></script>
	<script type="text/javascript" src="resources/js/adminDashboardTabs.js"></script>

	<!-- FAVICON -->
	<link rel="SHORTCUT ICON" href="resources/images/valogo.ico" type="image/x-icon">
	<link rel="icon" href="resources/images/valogo.ico" type="image/x-icon" />    
	<link rel="apple-touch-icon" sizes="114x114" href="resources/images/favico_va_touch_114x114.png" />
	<link rel="apple-touch-icon" sizes="72x72" href="resources/images/favico_va_touch_72x72.png" />
	<link rel="apple-touch-icon" href="resources/images/favico_va_touch_57x57.png" />
	<meta name="msapplication-square310x310logo" content="resources/images/favico_va_310x310.png" />

	<link href="resources/css/jquery/jquery-ui-1.10.3.custom.min.css" rel="stylesheet" type="text/css" />
	<link href="resources/css/partialpage/standardtopofpage-dashboard.css" rel="stylesheet" type="text/css" />
	<link href="resources/css/jquery.dataTables.css" rel="stylesheet" type="text/css" />
	<link href="resources/css/partialpage/menu-partial.css" rel="stylesheet" type="text/css" />
	<link href="resources/css/veteranSearch.css" rel="stylesheet" type="text/css" />
	
	<!-- Bootstrap -->
	<link href="<c:url value="/resources/js/bootstrap/css/bootstrap.css" />" rel="stylesheet" type="text/css" />
	<link href="<c:url value="/resources/css/partialpage/standardtopofpage-dashboard_new.css" />" rel="stylesheet" type="text/css" />
</head>
<body>

<a href="#skip" class="offscreen">Skip to main content</a>
	<div id="outerPageDiv">
		<%@ include file="/WEB-INF/views/partialpage/standardtopofpage-partial.jsp"%>
		<div class="navbar navbar-default navbar-update" role="navigation">
      <div class="container bg_transparent">
        <div class="navbar-header">
          <button type="button" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-collapse">
            <span class="sr-only">Toggle navigation</span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
          </button>
        </div>
        <nav class="navbar-collapse collapse">
          <ul class="nav navbar-nav" id="tabs">
		  </ul>
        </nav><!--/.nav-collapse -->
      </div>
    </div>    
	</div>

	<div class="mainDiv"> </div>

	<div class="container left-right-shadow editVeteransAssessment">
		<div class="row">
			<div class="col-md-12">
				<a name="skip" > </a ><h1>Create Battery</h1>
				<c:if test="${!isCprsVerified}">
					<div class="alert alert-danger">
						Your VistA account information needs to be verified before you can save or read any data from VistA. 
						Search result will not include any data from VistA. 
					</div>
				</c:if>

				<c:if test="${isReadOnly}">
					<div class="alert alert-danger">
						<button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times;</button>
						This battery is read only and no longer editable.
					</div>
				</c:if>
				<div>
				
					<div class="panel-group" id="accordion" role="tablist" aria-multiselectable="true">
					  <div class="panel panel-default">
						<div class="panel-heading" role="tab" id="headingOne">
						  <h3 class="panel-title">
							<a data-toggle="collapse" data-parent="#accordion" href="#collapseOne" aria-expanded="true" aria-controls="collapseOne">
							 <strong>Selected Veterans</strong>
							</a>
						  </h3>
						</div>
						<div id="collapseOne" class="panel-collapse collapse in" role="tabpanel" aria-labelledby="headingOne">
						<div class="panel-body">
							<form:form method="post"  modelAttribute="editVeteranAssessmentFormBean">
								<table class="table table-striped table-hover" summary="Search Result Table">
									<thead>
										<tr>
											<th scope="col" class="col-md-1">SSN-4</th>
											<th scope="col" class="col-md-2">Last Name</th>
											<th scope="col" class="col-md-2">First Name</th>
											<th scope="col" class="col-md-2">Middle Name</th>
											<th scope="col" class="col-md-2">Date of Birth</th>
											<th scope="col" class="col-md-1">Appointment Date</th>
											<th scope="col" class="col-md-1 text-right">Appointment Time</th>
											<th scope="col" class="col-md-1">Clinical Reminders</th>
										</tr>
									</thead>
									<tfoot>
										<tr>
											<c:if test="${empty veterans}">
												<td colspan="6">No record found</td>
											</c:if>
											<c:if test="${not empty veterans}">
												<td colspan="8"><c:out value="${veteransSize}" /> record(s) found</td>
											</c:if>
										</tr>
									</tfoot>
									<tbody>
										<c:if test="${not empty veterans}">
											<c:forEach var="item" items="${veterans}">
												<tr>
													<td class="text-right"><c:out value="${item.ssnLastFour}" /></td>
													<td><c:out value="${item.lastName}" /></td>
													<td><c:out value="${item.firstName}" /></td>
													<td><c:out value="${item.middleName}" /></td>
													<td class="text-right"><fmt:formatDate type="date" pattern="MM/dd/yyyy" value="${item.birthDate}" /></td>
													<td class="text-right"><c:out value="${item.apptDate}" /></td>
													<td class="text-right"><c:out value="${item.apptTime}" /></td>
													<td class="text-right  text-capitalize"><c:out value="${item.dueClinicalReminders}" /></td>
												</tr>
											</c:forEach>
										</c:if>
									</tbody>
								</table>
								</form:form>
							</div>
						</div>
					  </div>
					</div>
				
				</div>
				


				<div>
					<form:form modelAttribute="batchCreateFormBean" autocomplete="off" method="post">
						<form:hidden path="veteranId"/>
						<form:hidden path="veteranAssessmentId"/>
						<form:hidden path="selectedClinicId"/>
						<form:errors path="*" element="div" cssClass="alert alert-danger" />
						<br />
						
						<div class="row">
							<div class="col-md-3">
								<div class="form-group">
									<form:label path="selectedProgramId">Program *</form:label>
									<form:select path="selectedProgramId" cssClass="form-control" disabled="${isReadOnly}"  required="true">
										<form:option value="" label="Please Select a Program"/>
										<form:options items="${programList}" itemValue="stateId" itemLabel="stateName"/>
									</form:select>
									<form:errors path="selectedProgramId" cssClass="help-inline"/>
								</div>
							</div>
							<div class="col-md-3">
								<div class="form-group">
									<div class="label">VistA Clinic</div>
									<div>${clinic}</div>
								</div>
							</div>
							<div class="col-md-3">								
								<div class="form-group">
									<form:label path="selectedNoteTitleId">Note Title *</form:label>
									<form:select path="selectedNoteTitleId" cssClass="form-control" disabled="${isReadOnly}"  required="true">
										<form:option value="" label="Please Select a Note Title"/>
										<form:options items="${noteTitleList}" itemValue="stateId" itemLabel="stateName"/>
									</form:select>
									<form:errors path="selectedNoteTitleId" cssClass="help-inline"/>
								</div>
							</div>
							<div class="col-md-3">								
								<div class="form-group">
									<form:label path="selectedClinicianId">Clinician *</form:label>
									<form:select path="selectedClinicianId" cssClass="form-control" disabled="${isReadOnly}"  required="true">
										<form:option value="" label="Please Select a Clinician"/>
										<form:options items="${clinicianList}" itemValue="stateId" itemLabel="stateName"/>
									</form:select>
									<form:errors path="selectedClinicianId" cssClass="help-inline"/>
								</div>
							</div>
						</div>



						<hr/>
						<div id="output"></div>
					</form:form>

					<div class="clear-fix"></div>
				</div>
			</div>
		</div>
	</div>
	<%@ include file="/WEB-INF/views/partialpage/footer.jsp" %>
	<!-- Scripts -->
	<script type="text/javascript" src="<c:url value="/resources/js/bootstrap/js/bootstrap.js" />"></script>
	<!-- Page Scripts -->
	<script type="text/javascript" src="<c:url value="/resources/js/dashboard/editVeteransAssessment.js" />"></script>
</body>
</html>