package controlador;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import modelo.Usuario;
import modelo.UsuarioDAO;

@WebServlet(name = "UsuarioServlet", urlPatterns = {"/UsuarioServlet"})
public class UsuarioServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        
        String nombre = request.getParameter("txtNombre");
        String correo = request.getParameter("txtCorreo");
        String telefono = request.getParameter("txtTelefono");
        String rol = request.getParameter("txtRol");

        Usuario usuario = new Usuario();
        usuario.setNombre(nombre);
        usuario.setCorreo(correo);
        usuario.setTelefono(telefono);
        usuario.setRol(rol);

        UsuarioDAO dao = new UsuarioDAO();
        boolean registrado = dao.registrarUsuario(usuario);

        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html><head><title>Resultado</title></head><body>");
            if (registrado) {
                out.println("<h2>¡Usuario registrado exitosamente desde la web!</h2>");
            } else {
                out.println("<h2>Error al registrar el usuario.</h2>");
            }
            out.println("<br><a href='index.html'>Volver al inicio</a>");
            out.println("</body></html>");
        }
    }
}