document.addEventListener('DOMContentLoaded', () => {
    const contenedorMensaje = document.querySelector('.alert-danger'); // o el selector del aviso
    const formularioNuevaAlerta = document.getElementById('form-nueva-alerta');

    // Ocultar cualquier mensaje de restricción
    if (contenedorMensaje) {
        contenedorMensaje.style.display = 'none';
    }

    // Mostrar siempre el formulario para crear alertas
    if (formularioNuevaAlerta) {
        formularioNuevaAlerta.style.display = 'block';
    }
});