<%--
  ~ Copyright J. Craig Venter Institute, 2011
  ~
  ~ The creation of this program was supported by the U.S. National
  ~ Science Foundation grant 1048199 and the Microsoft allocation
  ~ in the MS Azure cloud.
  ~
  ~ This program is free software: you can redistribute it and/or modify
  ~ it under the terms of the GNU General Public License as published by
  ~ the Free Software Foundation, either version 3 of the License, or
  ~ (at your option) any later version.
  ~
  ~ This program is distributed in the hope that it will be useful,
  ~ but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~ GNU General Public License for more details.
  ~
  ~ You should have received a copy of the GNU General Public License
  ~ along with this program.  If not, see <http://www.gnu.org/licenses/>.
  --%>

<%--
  Created by IntelliJ IDEA.
  User: hkim
  Date: 1/3/12
  Time: 1:35 PM
  To change this template use File | Settings | File Templates.
--%>
<%@taglib prefix="sf" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="s" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>PRODDL-C Admin Main</title>

    <script src="/resources/js/jquery-1.6.2.min.js"></script>
    <script src="/resources/js/jquery-ui-1.8.16.custom.min.js"></script>
    <script>
        $(document).ready(function() {
            $("#accordion").accordion({ header: "h3" });
        });

    </script>
    <link href="/resources/css/redmond/jquery-ui-1.8.16.custom.css" rel="stylesheet" type="text/css"/>
    <link href="/resources/css/main.css" rel="stylesheet" type="text/css"/>
    <style type="text/css">
    </style>
</head>
<body>
<div id="adminMainAccordian">
    <div id="accordion">
        <div>
            <h3><a href="#">Cloud Management</a></h3>

            <div>Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet.</div>
        </div>
        <div>
            <h3><a href="#">User Management</a></h3>

            <div>Phasellus mattis tincidunt nibh.</div>
        </div>
        <div>
            <h3><a href="#">Upload PyRosetta</a></h3>

            <div>This page allows administrator to upload PyRosetta application to the cloud.</div>
        </div>
    </div>

</div>


</body>
</html>