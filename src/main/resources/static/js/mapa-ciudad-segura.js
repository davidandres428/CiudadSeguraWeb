window.MapaCiudadSegura = (() => {
    const centroInicial = [4.711, -74.0721];

    function escaparHtml(valor) {
        return String(valor ?? "").replace(/[&<>"']/g, (caracter) => ({
            "&": "&amp;",
            "<": "&lt;",
            ">": "&gt;",
            "\"": "&quot;",
            "'": "&#39;",
        })[caracter]);
    }

    function obtenerCoordenadas(elemento) {
        const latitud = Number(elemento.latitud);
        const longitud = Number(elemento.longitud);
        if (elemento.latitud === null || elemento.latitud === undefined || elemento.latitud === ""
                || elemento.longitud === null || elemento.longitud === undefined || elemento.longitud === ""
                || !Number.isFinite(latitud) || !Number.isFinite(longitud)
                || latitud < -90 || latitud > 90 || longitud < -180 || longitud > 180) {
            return null;
        }
        return [latitud, longitud];
    }

    function popupReporte(reporte) {
        return `<strong>${escaparHtml(reporte.titulo)}</strong>
            <p>${escaparHtml(reporte.descripcion)}</p>
            <p><strong>Prioridad:</strong> ${escaparHtml(reporte.prioridad)}</p>
            <p><strong>Estado:</strong> ${escaparHtml(reporte.estado)}</p>`;
    }

    function popupZona(zona) {
        return `<strong>Zona de riesgo: ${escaparHtml(zona.nombre)}</strong>
            <p>${escaparHtml(zona.descripcion || "Sin descripción")}</p>
            <p><strong>Nivel de riesgo:</strong> ${escaparHtml(zona.nivelRiesgo)}</p>`;
    }

    function crear(contenedorId, alSeleccionarCoordenadas) {
        const contenedor = document.getElementById(contenedorId);
        if (!contenedor) {
            throw new Error(`No existe el contenedor del mapa "${contenedorId}".`);
        }
        const leaflet = window.L;
        if (!leaflet) {
            throw new Error("No se pudo cargar Leaflet. Comprueba la conexión e inténtalo de nuevo.");
        }

        const mapa = leaflet.map(contenedor).setView(centroInicial, 6);
        leaflet.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
            maxZoom: 19,
            attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>',
        }).addTo(mapa);

        const capaReportes = leaflet.layerGroup().addTo(mapa);
        const capaZonas = leaflet.layerGroup().addTo(mapa);
        let marcadorSeleccionado = null;

        function ajustarVista() {
            const capas = [capaReportes, capaZonas];
            if (marcadorSeleccionado) {
                capas.push(marcadorSeleccionado);
            }
            const grupo = leaflet.featureGroup(capas);
            if (grupo.getLayers().length > 0) {
                mapa.fitBounds(grupo.getBounds(), { padding: [24, 24], maxZoom: 14 });
            }
        }

        function seleccionarCoordenadas(latitud, longitud) {
            const coordenadas = obtenerCoordenadas({ latitud, longitud });
            if (!coordenadas) {
                return;
            }
            if (marcadorSeleccionado) {
                mapa.removeLayer(marcadorSeleccionado);
            }
            marcadorSeleccionado = leaflet.marker(coordenadas)
                .bindPopup("Ubicación seleccionada para el reporte")
                .addTo(mapa);
            mapa.panTo(coordenadas);
        }

        if (alSeleccionarCoordenadas) {
            mapa.on("click", (evento) => {
                const punto = evento?.latlng;
                if (!punto || !Number.isFinite(punto.lat) || !Number.isFinite(punto.lng)) {
                    return;
                }
                const lat = evento.latlng.lat;
                const lng = evento.latlng.lng;
                seleccionarCoordenadas(lat, lng);
                alSeleccionarCoordenadas(lat, lng);
            });
        }

        return {
            mostrarReportes(reportes, ajustar = false) {
                capaReportes.clearLayers();
                reportes.forEach((reporte) => {
                    const coordenadas = obtenerCoordenadas(reporte);
                    if (coordenadas) {
                        leaflet.marker(coordenadas)
                            .bindPopup(popupReporte(reporte))
                            .addTo(capaReportes);
                    }
                });
                if (ajustar) {
                    ajustarVista();
                }
            },
            mostrarZonas(zonas, ajustar = false) {
                capaZonas.clearLayers();
                zonas.forEach((zona) => {
                    const coordenadas = obtenerCoordenadas(zona);
                    if (coordenadas) {
                        leaflet.circleMarker(coordenadas, {
                            radius: 9,
                            color: "#8a5a00",
                            fillColor: "#f6ad32",
                            fillOpacity: 0.85,
                            weight: 2,
                        }).bindPopup(popupZona(zona)).addTo(capaZonas);
                    }
                });
                if (ajustar) {
                    ajustarVista();
                }
            },
            seleccionarCoordenadas,
        };
    }

    return { crear };
})();
