<%@ page import="java.util.*,org.eclipse.help.servlet.*,org.w3c.dom.*" errorPage="err.jsp" contentType="text/html; charset=UTF-8"%>

<% 
	// calls the utility class to initialize the application
	application.getRequestDispatcher("/servlet/org.eclipse.help.servlet.InitServlet").include(request,response);

	LayoutData layout = new LayoutData(application,request);
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<!--
 (c) Copyright IBM Corp. 2000, 2002.
 All Rights Reserved.
-->

<html>

<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title><%=WebappResources.getString("Help", request)%></title>
<script language="JavaScript">
/**
 * Needs to be called from a view
 */
function setToolbarTitle(title)
{
	if(parent.DisplayFrame.ToolbarFrame){
		parent.DisplayFrame.ToolbarFrame.setTitle(title);
	}
}
</script>
</head>

<frameset id="navFrameset" rows="24,*,24"  framespacing="0" border="0"  frameborder="0" spacing="0"  scrolling="no">
   <frame name="ToolbarFrame" src='<%="navToolbar.jsp"+layout.getQuery()%>' marginwidth="0" marginheight="0" scrolling="no" frameborder="0" noresize>
   <frame name="ViewsFrame" src='<%="views.jsp"+layout.getQuery()%>' marginwidth="0" marginheight="0" scrolling="no" frameborder="0" resize=yes>
   <frame name="TabsFrame" src='<%="tabs.jsp"+layout.getQuery()%>' marginwidth="0" marginheight="0" scrolling="no" frameborder="0" noresize>
</frameset>

</html>