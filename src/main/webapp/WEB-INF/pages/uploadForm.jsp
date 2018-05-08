<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<html>
<head>
<script type="text/javascript" src="jquery-1.2.6.min.js"></script>
<title>DMDM</title>
</head>
<body>

	<center>
		<h2><font color="green"><u>Excel to XML converter</u></font></h2>
		<h3><font color="blue">Please select an excel file to convert !</font></h3>
		<br />
		<form:form method="post" enctype="multipart/form-data"
			modelAttribute="uploadedFile" action="fileUpload.htm">
			<table>
				<tr>
					<td><font color="blue">Upload File:&nbsp;</font></td>
					<td><input type="file" name="file" />
					</td>
					<td style="color: red; font-style: italic;"><form:errors
							path="file" />
					</td>
				</tr>
				<tr>
					<td>&nbsp;</td>
					<td><input type="submit" value="Convert to XML" />
					</td>
					<td>&nbsp;</td>
				</tr>
			</table>
		</form:form>
	</center>
</body>
</html>
