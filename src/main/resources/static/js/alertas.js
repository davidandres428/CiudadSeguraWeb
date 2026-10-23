document.addEventListener('DOMContentLoaded', () => {
    const formAlerta = document.getElementById('form-alerta');
    if (formAlerta) {
        formAlerta.addEventListener('submit', async (e) => {
            e.preventDefault();
            
            const nuevaAlerta = {
                titulo: document.getElementById('alerta-titulo').value,
                mensaje: document.getElementById('alerta-mensaje').value,
                tipo: document.getElementById('alerta-tipo').value,
                severidad: document.getElementById('alerta-severidad').value
            };

            try {
                const respuesta = await fetch('/api/alertas', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(nuevaAlerta)
                });

                if (respuesta.ok) {
                    alert('¡Alerta creada exitosamente!');
                    window.location.reload();
                } else {
                    alert('Error al crear la alerta');
                }
            } catch (error) {
                console.error('Error:', error);
            }
        });
    }
});