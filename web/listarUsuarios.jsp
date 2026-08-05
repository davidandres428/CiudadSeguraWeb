<%@page import="java.util.List"%>
<%@page import="modelo.Usuario"%>
<%@page import="modelo.UsuarioDAO"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Lista de Usuarios - Ciudad Segura</title>
    </head>
    <body>
        <h2>Lista de Usuarios Registrados (JSP + JDBC)</h2>
        <table border="1">
            <thead>
                <tr>
                    <th>ID</th>
                    <th>Nombre</th>
                    <th>Correo</th>
                    <th>Teléfono</th>
                    <th>Rol</th>
                </tr>
            </thead>
            <tbody>
                <%
                    UsuarioDAO dao = new UsuarioDAO();
                    List<Usuario> lista = dao.listarUsuarios();
                    for (Usuario u : lista) {
                %>
                <tr>
                    <td><%= u.getIdUsuario() %></td>
                    <td><%= u.getNombre() %></td>
                    <td><%= u.getCorreo() %></td>
                    <td><%= u.getTelefono() %></td>
                    <td><%= u.getRol() %></td>
                </tr>
                <% } %>
            </tbody>
        </table>
        <br>
        <a href="index.html">Registrar un nuevo usuario</a>
    </body>
</html>