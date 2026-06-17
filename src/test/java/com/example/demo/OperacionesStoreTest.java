package com.example.demo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OperacionesStore - pruebas unitarias")
class OperacionesStoreTest {

    private OperacionesStore store;

    @BeforeEach
    void setUp() {
        store = new OperacionesStore();
        store.cargarDatosIniciales();
    }

    // ─── Tours ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Carga inicial: debe haber tours activos e inactivos")
    void datosIniciales_toursPresentes() {
        List<Tour> tours = store.findTours();
        assertTrue(tours.size() >= 4, "Se esperan al menos 4 tours iniciales");

        boolean hayActivos = tours.stream().anyMatch(Tour::activo);
        boolean hayInactivos = tours.stream().anyMatch(t -> !t.activo());
        assertTrue(hayActivos, "Debe haber tours activos (disponibles)");
        assertTrue(hayInactivos, "Debe haber tours inactivos (no disponibles)");
    }

    @Test
    @DisplayName("createTour: asigna id y persiste el tour")
    void createTour_asignaId() {
        Tour input = new Tour(null, "Tour Unit", "Descripcion", 10, new BigDecimal("30000"), true);
        Tour saved = store.createTour(input);

        assertNotNull(saved.id());
        assertEquals("Tour Unit", saved.nombre());
        assertTrue(saved.activo());
        assertTrue(store.findTour(saved.id()).isPresent());
    }

    @Test
    @DisplayName("findTour: retorna vacío si no existe")
    void findTour_inexistente() {
        assertTrue(store.findTour(9999L).isEmpty());
    }

    @Test
    @DisplayName("updateTour: actualiza nombre y precio")
    void updateTour_actualiza() {
        Long id = store.findTours().get(0).id();
        Tour update = new Tour(null, "Tour Modificado", "Desc", 5, new BigDecimal("20000"), false);
        Optional<Tour> result = store.updateTour(id, update);

        assertTrue(result.isPresent());
        assertEquals("Tour Modificado", result.get().nombre());
        assertFalse(result.get().activo());
    }

    @Test
    @DisplayName("updateTour: retorna vacío si no existe")
    void updateTour_inexistente() {
        Tour update = new Tour(null, "X", null, 1, BigDecimal.ONE, true);
        assertTrue(store.updateTour(9999L, update).isEmpty());
    }

    // ─── Salidas ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Carga inicial: debe haber salidas PROGRAMADA y CANCELADA")
    void datosIniciales_salidasPresentes() {
        List<SalidaTour> salidas = store.findSalidas(null);
        assertTrue(salidas.size() >= 6, "Se esperan al menos 6 salidas iniciales");

        boolean hayProgramadas = salidas.stream().anyMatch(s -> "PROGRAMADA".equals(s.estado()));
        boolean hayCanceladas = salidas.stream().anyMatch(s -> "CANCELADA".equals(s.estado()));
        assertTrue(hayProgramadas, "Debe haber salidas PROGRAMADA");
        assertTrue(hayCanceladas, "Debe haber salidas CANCELADA");
    }

    @Test
    @DisplayName("createSalida: crea con estado PROGRAMADA y cupoDisponible = cupoTotal")
    void createSalida_creaCorrectamente() {
        Long tourId = store.findTours().get(0).id();
        String fecha = LocalDate.now().plusDays(30).toString();

        SalidaTour input = new SalidaTour(null, tourId, fecha, "09:00", "13:00",
                10, null, "PROGRAMADA", null, null, List.of());
        SalidaTour saved = store.createSalida(input);

        assertNotNull(saved.id());
        assertEquals("PROGRAMADA", saved.estado());
        assertEquals(10, saved.cupoTotal());
        assertEquals(10, saved.cupoDisponible());
    }

    @Test
    @DisplayName("cancelarSalida: cambia estado a CANCELADA")
    void cancelarSalida_exitosa() {
        Long tourId = store.findTours().get(0).id();
        SalidaTour salida = store.createSalida(new SalidaTour(null, tourId,
                LocalDate.now().plusDays(40).toString(), "10:00", "14:00",
                8, null, "PROGRAMADA", null, null, List.of()));

        Optional<SalidaTour> cancelada = store.cancelarSalida(salida.id());
        assertTrue(cancelada.isPresent());
        assertEquals("CANCELADA", cancelada.get().estado());
    }

    @Test
    @DisplayName("cancelarSalida: retorna vacío si no existe")
    void cancelarSalida_inexistente() {
        assertTrue(store.cancelarSalida(9999L).isEmpty());
    }

    @Test
    @DisplayName("findSalidas: filtra por fecha exacta")
    void findSalidas_filtraPorFecha() {
        Long tourId = store.findTours().get(0).id();
        String fechaEspecifica = "2031-03-15";
        store.createSalida(new SalidaTour(null, tourId, fechaEspecifica, "08:00", "12:00",
                5, null, "PROGRAMADA", null, null, List.of()));

        List<SalidaTour> resultado = store.findSalidas(fechaEspecifica);
        assertTrue(resultado.stream().allMatch(s -> fechaEspecifica.equals(s.fecha())));
        assertFalse(resultado.isEmpty());
    }

    // ─── ReservaTour ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("createReservaTour: descuenta cupos y retorna ACTIVA")
    void createReservaTour_exitosa() {
        Long tourId = store.findTours().stream().filter(Tour::activo).findFirst().get().id();
        SalidaTour salida = store.createSalida(new SalidaTour(null, tourId,
                LocalDate.now().plusDays(50).toString(), "09:00", "13:00",
                10, null, "PROGRAMADA", null, null, List.of()));

        ReservaTour input = new ReservaTour(null, salida.id(), 1L, "Cliente Test", 3, null);
        Optional<ReservaTour> result = store.createReservaTour(input);

        assertTrue(result.isPresent());
        assertEquals("ACTIVA", result.get().estado());

        SalidaTour actualizada = store.findSalida(salida.id()).get();
        assertEquals(7, actualizada.cupoDisponible(), "Debe haber descontado 3 cupos");
    }

    @Test
    @DisplayName("createReservaTour: rechaza si cupo insuficiente")
    void createReservaTour_sinCupo() {
        Long tourId = store.findTours().stream().filter(Tour::activo).findFirst().get().id();
        SalidaTour salida = store.createSalida(new SalidaTour(null, tourId,
                LocalDate.now().plusDays(55).toString(), "09:00", "13:00",
                2, null, "PROGRAMADA", null, null, List.of()));

        ReservaTour input = new ReservaTour(null, salida.id(), 1L, "Grupo Grande", 10, null);
        Optional<ReservaTour> result = store.createReservaTour(input);
        assertTrue(result.isEmpty(), "Debe rechazar cuando no hay cupo suficiente");
    }

    @Test
    @DisplayName("createReservaTour: rechaza si la salida esta CANCELADA")
    void createReservaTour_salidaCancelada() {
        Long tourId = store.findTours().stream().filter(Tour::activo).findFirst().get().id();
        SalidaTour salida = store.createSalida(new SalidaTour(null, tourId,
                LocalDate.now().plusDays(60).toString(), "09:00", "13:00",
                8, null, "PROGRAMADA", null, null, List.of()));
        store.cancelarSalida(salida.id());

        ReservaTour input = new ReservaTour(null, salida.id(), 1L, "Test", 1, null);
        Optional<ReservaTour> result = store.createReservaTour(input);
        assertTrue(result.isEmpty(), "Debe rechazar reserva en salida CANCELADA");
    }

    @Test
    @DisplayName("cancelarReservaTour: devuelve cupos a la salida")
    void cancelarReservaTour_devuelveCupos() {
        Long tourId = store.findTours().stream().filter(Tour::activo).findFirst().get().id();
        SalidaTour salida = store.createSalida(new SalidaTour(null, tourId,
                LocalDate.now().plusDays(65).toString(), "09:00", "13:00",
                10, null, "PROGRAMADA", null, null, List.of()));

        ReservaTour reserva = store.createReservaTour(
                new ReservaTour(null, salida.id(), 1L, "A", 4, null)).get();
        assertEquals(6, store.findSalida(salida.id()).get().cupoDisponible());

        store.cancelarReservaTour(reserva.id());
        assertEquals(10, store.findSalida(salida.id()).get().cupoDisponible(),
                "Los cupos deben volver al cancelar");
    }

    @Test
    @DisplayName("cancelarReservaTour: retorna vacío si no existe")
    void cancelarReservaTour_inexistente() {
        assertTrue(store.cancelarReservaTour(9999L).isEmpty());
    }

    // ─── Recursos ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Carga inicial: hay guias, vehiculos y bicicletas")
    void datosIniciales_recursosPresentes() {
        assertTrue(store.findGuias().size() >= 1, "Debe haber al menos 1 guia inicial");
        assertTrue(store.findVehiculos().size() >= 1, "Debe haber al menos 1 vehiculo inicial");
        assertTrue(store.findBicicletas().size() >= 1, "Debe haber al menos 1 bicicleta inicial");
    }

    @Test
    @DisplayName("createGuia: asigna id y persiste")
    void createGuia_asignaId() {
        Guia saved = store.createGuia(new Guia(null, "Test Guia", "+56900000000", true));
        assertNotNull(saved.id());
        assertEquals("Test Guia", saved.nombre());
    }

    @Test
    @DisplayName("createVehiculo: asigna id y persiste")
    void createVehiculo_asignaId() {
        Vehiculo saved = store.createVehiculo(new Vehiculo(null, "TEST-001", "Van", 8, true));
        assertNotNull(saved.id());
        assertEquals("TEST-001", saved.patente());
    }

    @Test
    @DisplayName("createBicicleta: asigna id y persiste")
    void createBicicleta_asignaId() {
        Bicicleta saved = store.createBicicleta(new Bicicleta(null, "BIKE-TEST", "DISPONIBLE"));
        assertNotNull(saved.id());
        assertEquals("BIKE-TEST", saved.codigo());
    }

    @Test
    @DisplayName("assignGuia: asigna guia a salida existente")
    void assignGuia_exitosa() {
        Long tourId = store.findTours().get(0).id();
        Long guiaId = store.findGuias().get(0).id();
        SalidaTour salida = store.createSalida(new SalidaTour(null, tourId,
                LocalDate.now().plusDays(70).toString(), "09:00", "13:00",
                8, null, "PROGRAMADA", null, null, List.of()));

        Optional<SalidaTour> result = store.assignGuia(salida.id(), guiaId);
        assertTrue(result.isPresent());
        assertEquals(guiaId, result.get().guiaId());
    }

    @Test
    @DisplayName("findAgendaGuia: retorna salidas ordenadas por fecha")
    void findAgendaGuia_ordenadaPorFecha() {
        Guia guia = store.createGuia(new Guia(null, "Guia Agenda", "+56900000001", true));
        Long tourId = store.findTours().get(0).id();

        SalidaTour s1 = store.createSalida(new SalidaTour(null, tourId, "2031-06-10",
                "09:00", "13:00", 8, null, "PROGRAMADA", guia.id(), null, List.of()));
        SalidaTour s2 = store.createSalida(new SalidaTour(null, tourId, "2031-06-05",
                "09:00", "13:00", 8, null, "PROGRAMADA", guia.id(), null, List.of()));

        List<SalidaTour> agenda = store.findAgendaGuia(guia.id());
        assertEquals(2, agenda.size());
        assertTrue(agenda.get(0).fecha().compareTo(agenda.get(1).fecha()) <= 0,
                "La agenda debe estar ordenada por fecha ascendente");
    }
}
