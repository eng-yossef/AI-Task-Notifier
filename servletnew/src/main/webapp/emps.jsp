<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%
    List<String> fnames = (List<String>) request.getAttribute("fnames");
    List<String> lnames = (List<String>) request.getAttribute("lnames");
    List<Integer> ids = (List<Integer>) request.getAttribute("ids");
    List<Integer> salaries = (List<Integer>) request.getAttribute("salaries");
%>
<!DOCTYPE html>
<html>
<head>
    <title>Employee Table</title>
    <link rel="stylesheet" type="text/css" href="styles.css">
</head>

<body>
<center>
<h1>Employees Sheet</h1>
</center>
<h2>
<a href="Add.html">Add Employee?</a>
</h2>


 <table>
        <thead>
            <tr >
                <th>ID</th>
                <th>First Name</th>
                <th>Last Name</th>
                <th>Salary</th>
                <th>Action</th>
            </tr>
        </thead>
        <tbody>
            <%
                for(int i=0;i<fnames.size();i++) {

            %>
            <tr id="row-<%= ids.get(i) %>">
                <td><%= ids.get(i) %></td>
                <td><%= fnames.get(i) %></td>
                <td><%= lnames.get(i) %></td>
                <td><%= salaries.get(i) %></td>
                <td><button  onclick="deleteEmployee(<%= ids.get(i) %>)">Delete</button></td>

            </tr>
            <% } %>
        </tbody>
    </table>



   <script>
   function deleteEmployee(employeeId) {
       if (confirm('Are you sure you want to delete this employee?')) {
           fetch(`employees?id=${employeeId}`, {
               method: 'PUT',
           })
           .then(response => response.text())
           .then(data => {
               // Reload the page or remove the row from the table
               var emprow="row-"+employeeId;
               document.getElementById(emprow).remove();

              // location.reload();
           })
           .catch(error => console.error('Error:', error));
       }
   }
   </script>
   <script>
   /*

             function deleteEmployee(employeeId, rowId) {
                 if (confirm('Are you sure you want to delete this employee?')) {
                     fetch(`DeleteEmployeeServlet?id=${employeeId}`, {
                         method: 'PUT'
                     })
                     .then(response => response.text())
                     .then(data => {
                         if (data === "Success") {
                             // Remove the row from the table
                             document.getElementById(rowId).remove();
                         } else {
                             alert('Failed to delete employee: ' + data);
                         }
                     })
                     .catch(error => console.error('Error:', error));
                 }
             }
             */

         </script>

    </body>
    </html>