<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="sec"	uri="http://www.springframework.org/security/tags" %>
<!DOCTYPE html>
<html>
<head>
  <title>User List View</title>
  <meta http-equiv="X-UA-Compatible" content="IE=edge">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  	
	<script type="text/javascript" src="<c:url value="/resources/js/jquery/jquery-1.10.2.min.js" />"></script>
	<script type="text/javascript" src="<c:url value="/resources/js/jquery/jquery.dataTables.js" />"></script>
	<script type="text/javascript" src="<c:url value="/resources/js/adminDashboardTabs.js" />"></script>
	
	<link href="<c:url value="/resources/css/jquery/jquery-ui-1.10.3.custom.min.css" />" rel="stylesheet" type="text/css" />
	
	<link href="<c:url value="/resources/images/valogo.ico" />" rel="icon" type="image/x-icon" />
	<link href="<c:url value="/resources/images/valogo.ico" />" rel="SHORTCUT ICON" type="image/x-icon" />
	
	<link href="<c:url value="/resources/css/partialpage/standardtopofpage-dashboard.css" />" rel="stylesheet" type="text/css" />
	<link href="<c:url value="/resources/css/jquery.dataTables.css" />" rel="stylesheet" type="text/css" />
	<link href="<c:url value="/resources/css/partialpage/menu-partial.css" />" rel="stylesheet" type="text/css" />
	<link href="<c:url value="/resources/css/veteranSearch.css" />" rel="stylesheet" type="text/css" />
	<link href="<c:url value="/resources/css/formButtons.css" />" rel="stylesheet" type="text/css" />
	
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

	<div class="mainDiv">
	</div>

	<div class="container left-right-shadow">
    <div class="row">
			<div class="col-md-3 pull-right">
				<s:url var="addUserUrl" value="/dashboard/userEditView" htmlEscape="true"></s:url>
				<a href="${addUserUrl}" class="btn btn-primary btn-md h1_button pull-right"><span class="glyphicon glyphicon-plus"></span> Create New User</a>
			</div>
			<div class="col-md-9 pull-left"><a name="skip" > </a ><h1>User Management</h1></div>
		</div>

		<div class="clear-fix"></div>
		
		<div id="mainContent">
			<table id="userListDataTable" name="userListDataTable" class="table table-striped  table-hover dataTables_wrapper" summary="User Management Table"> 
				<thead>
					<tr>
						<th scope="col" class="col-md-2">Login ID</th>
						<th scope="col" class="col-md-2">Last Name</th>
						<th scope="col" class="col-md-2">First Name</th>
						<th scope="col" class="col-md-2">Status</th>
						<th scope="col" class="col-md-4">Role</th>
					</tr>
				</thead>
				<tbody>
				</tbody>
			</table>
		</div>

	</div>

	<%@ include file="/WEB-INF/views/partialpage/footer.jsp" %>
  
  <script type="text/javascript" src="<c:url value="/resources/js/bootstrap/js/bootstrap.js" />"></script>
  <script type="text/javascript" src="<c:url value="/resources/js/adminTab/userListView.js" />"></script>

</body>
</html>