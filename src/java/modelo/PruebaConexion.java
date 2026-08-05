package modelo;

import conexion.Conexion;
import java.util.List;

public class PruebaConexion {
    public static void main(String[] args) {
        System.out.println("Iniciando prueba completa del CRUD...\n");
        
        UsuarioDAO dao = new UsuarioDAO();

        // 1. Listar usuarios actuales
        System.out.println("--- LISTA INICIAL ---");
        List<Usuario> usuarios = dao.listarUsuarios();
        for (Usuario u : usuarios) {
            System.out.println("ID: " + u.getIdUsuario() + " | Nombre: " + u.getNombre());
        }

        // 2. Probar Actualizar (Tomamos el ID 1)
        if (!usuarios.isEmpty()) {
            Usuario usuarioAModificar = usuarios.get(0);
            usuarioAModificar.setNombre("David Actualizado");
            if (dao.actualizarUsuario(usuarioAModificar)) {
                System.out.println("\n¡Usuario actualizado con éxito!");
            }
        }

        // 3. Listar después de actualizar
        System.out.println("\n--- LISTA DESPUÉS DE ACTUALIZAR ---");
        usuarios = dao.listarUsuarios();
        for (Usuario u : usuarios) {
            System.out.println("ID: " + u.getIdUsuario() + " | Nombre: " + u.getNombre());
        }

        // 4. Probar Eliminar (Eliminamos el usuario con ID 1, o el primero de la lista)
        if (!usuarios.isEmpty()) {
            int idEliminar = usuarios.get(0).getIdUsuario();
            boolean eliminado = dao.eliminarUsuario(idEliminar);
            if (eliminado) {
                System.out.println("\n¡Usuario con ID " + idEliminar + " eliminado con éxito!");
            }
        }

        // 5. Listar final para comprobar que se borró
        System.out.println("\n--- LISTA FINAL DESPUÉS DE ELIMINAR ---");
        usuarios = dao.listarUsuarios();
        if (usuarios.isEmpty()) {
            System.out.println("La base de datos está vacía (usuario eliminado correctamente).");
        } else {
            for (Usuario u : usuarios) {
                System.out.println("ID: " + u.getIdUsuario() + " | Nombre: " + u.getNombre());
            }
        }
    }
}