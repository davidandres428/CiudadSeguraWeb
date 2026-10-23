document.addEventListener('DOMContentLoaded', () => {
    // Ocultar la barra de advertencia roja de restricción
    const mensajeRestriccion = document.querySelector('.alert-danger');
    if (mensajeRestriccion) {
        mensajeRestriccion.style.display = 'none';
    }

    // Mostrar el formulario de creación de alertas para cualquier usuario
    const formularioAlerta = document.getElementById('form-nueva-alerta');
    if (formularioAlerta) {
        formularioAlerta.style.display = 'block';
    } else {
        // Si el formulario está dentro de un contenedor o sección oculta, lo forzamos a mostrarse
        const containerForm = document.querySelector('.nueva-alerta-container') || mensajeRestriccion?.parentElement;
        if (containerForm) {
            // Buscamos cualquier elemento bloqueado y lo hacemos visible
            const elementosOcultos = containerForm.querySelectorAll('form, .card-body, div');
            elementosOcultos.forEach(el => el.style.display = 'block');
        }
    }
});